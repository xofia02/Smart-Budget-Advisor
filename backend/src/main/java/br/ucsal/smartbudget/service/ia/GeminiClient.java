package br.ucsal.smartbudget.service.ia;

import br.ucsal.smartbudget.excecao.ApiException;
import br.ucsal.smartbudget.model.DicaEconomia;
import br.ucsal.smartbudget.model.MensagemChat;
import br.ucsal.smartbudget.model.PrevisaoCategoria;
import br.ucsal.smartbudget.model.PrevisaoCusto;
import br.ucsal.smartbudget.model.Viagem;
import br.ucsal.smartbudget.model.Viajante;
import br.ucsal.smartbudget.model.enums.CategoriaGasto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.Normalizer;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

@Component
public class GeminiClient implements ServicoIA {
    private static final Logger log = LoggerFactory.getLogger(GeminiClient.class);

    private static final String INSTRUCAO_JSON = """
            Você é um especialista em planejamento financeiro de viagens que atende viajantes brasileiros.
            Os valores são sempre em reais (BRL), com preços atuais de mercado.
            Responda somente com JSON válido, sem nenhum texto antes ou depois.
            """;

    private static final String INSTRUCAO_CHAT = """
            Você é o assistente do Smart Budget Advisor, que ajuda viajantes brasileiros a controlar
            os gastos de uma viagem. Responda em português do Brasil, de forma curta e prática
            (no máximo 3 parágrafos), com valores em reais. Se a pergunta não for sobre viagem ou
            gastos de viagem, diga educadamente que só pode ajudar com isso.
            """;

    private static final String INSTRUCAO_CHAT_GERAL = INSTRUCAO_CHAT + """
            O usuário ainda não informou destino, período nem orçamento: dê apenas orientações
            genéricas e sugira que ele crie uma viagem no sistema para respostas personalizadas.
            """;

    private static final int MENSAGENS_DE_CONTEXTO = 10;
    private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final String apiKey;
    private final String modelo;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public GeminiClient(@Value("${gemini.api-key}") String apiKey,
                        @Value("${gemini.model}") String modelo,
                        @Value("${gemini.base-url}") String baseUrl,
                        @Value("${gemini.timeout-segundos}") int timeoutSegundos,
                        ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.modelo = modelo;
        this.objectMapper = objectMapper;

        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(Duration.ofSeconds(3));
        fabrica.setReadTimeout(Duration.ofSeconds(timeoutSegundos));
        this.restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(fabrica).build();
    }

    public boolean estaConfigurado() {
        return apiKey != null && !apiKey.isBlank();
    }

    public String getModelo() {
        return modelo;
    }

    public String perguntar(String texto) {
        return chamar(null, List.of(new Turno(true, texto)), false);
    }

    @Override
    public PrevisaoCusto preverCustos(Viagem viagem) {
        DadosViagem dados = anonimizar(viagem);
        boolean personalizada = !dados.historico().isEmpty();

        String prompt = "Estime quanto vai custar esta viagem, por categoria de gasto.\n\n" + descrever(dados);
        if (personalizada) {
            prompt += "Viagens anteriores do viajante (use para calibrar o padrão de gasto):\n"
                    + String.join("\n", dados.historico()) + "\n";
        }
        prompt += """

                Para cada uma das 6 categorias, informe o custo TOTAL da viagem (não por dia) em reais:
                mínimo, recomendado e máximo, com minimo <= recomendado <= maximo.
                Categorias: HOSPEDAGEM, ALIMENTACAO, TRANSPORTE, PASSEIOS, COMPRAS, OUTROS.
                TRANSPORTE inclui a ida até o destino e o deslocamento nele.
                Formato: {"categorias": [{"categoria": "HOSPEDAGEM", "minimo": 0.00, "recomendado": 0.00, "maximo": 0.00}]}
                """;

        JsonNode raiz = lerJson(chamar(INSTRUCAO_JSON, List.of(new Turno(true, prompt)), true));
        JsonNode lista = raiz.isArray() ? raiz : raiz.path("categorias");

        Map<CategoriaGasto, PrevisaoCategoria> itens = new EnumMap<>(CategoriaGasto.class);
        for (JsonNode item : lista) {
            CategoriaGasto categoria = converterCategoria(item.path("categoria").asText());
            BigDecimal[] valores = {lerValor(item.path("minimo")), lerValor(item.path("recomendado")),
                    lerValor(item.path("maximo"))};
            if (categoria == null || valores[0] == null || valores[1] == null || valores[2] == null) {
                continue;
            }
            Arrays.sort(valores);
            itens.put(categoria, new PrevisaoCategoria(categoria, valores[0], valores[1], valores[2]));
        }
        if (itens.size() != CategoriaGasto.values().length) {
            throw iaIndisponivel("O assistente de IA devolveu uma previsão incompleta. Tente gerar novamente.");
        }

        PrevisaoCusto previsao = new PrevisaoCusto();
        previsao.setPersonalizada(personalizada);
        previsao.setItens(new ArrayList<>(itens.values()));
        return previsao;
    }

    @Override
    public List<DicaEconomia> gerarDicas(Viagem viagem) {
        String prompt = "Dê dicas práticas para economizar nesta viagem.\n\n" + descrever(anonimizar(viagem)) + """

                Gere de 5 a 8 dicas curtas e específicas para esse destino e época do ano.
                Pelo menos 2 devem alertar sobre cobranças abusivas ou golpes comuns contra turistas
                nesse destino; marque essas com "alertaTurista": true.
                Formato: {"dicas": [{"texto": "...", "alertaTurista": false}]}
                """;

        JsonNode raiz = lerJson(chamar(INSTRUCAO_JSON, List.of(new Turno(true, prompt)), true));
        JsonNode lista = raiz.isArray() ? raiz : raiz.path("dicas");

        List<DicaEconomia> dicas = new ArrayList<>();
        for (JsonNode item : lista) {
            String texto = item.path("texto").asText("").trim();
            if (!texto.isEmpty()) {
                DicaEconomia dica = new DicaEconomia();
                dica.setTexto(texto);
                dica.setAlertaTurista(item.path("alertaTurista").asBoolean(false));
                dicas.add(dica);
            }
        }
        if (dicas.isEmpty()) {
            throw iaIndisponivel("O assistente de IA não gerou dicas. Tente novamente.");
        }
        return dicas;
    }

    @Override
    public String responderChat(String pergunta, Viagem contexto) {
        if (contexto == null) {
            return chamar(INSTRUCAO_CHAT_GERAL, List.of(new Turno(true, pergunta)), false);
        }

        DadosViagem dados = anonimizar(contexto);
        String instrucao = INSTRUCAO_CHAT + "\nDados da viagem atual:\n" + descrever(dados)
                + "Orçamento total: " + reais(dados.orcamentoTotal()) + "\n"
                + "Total já gasto: " + reais(dados.totalGasto()) + "\n"
                + "Por categoria:\n" + String.join("\n", dados.categorias()) + "\n";

        List<MensagemChat> todas = contexto.getMensagens();
        List<Turno> conversa = new ArrayList<>();
        for (MensagemChat m : todas.subList(Math.max(0, todas.size() - MENSAGENS_DE_CONTEXTO), todas.size())) {
            boolean doUsuario = MensagemChat.PAPEL_USUARIO.equals(m.getPapel());
            if (!conversa.isEmpty() || doUsuario) {
                conversa.add(new Turno(doUsuario, m.getConteudo()));
            }
        }
        conversa.add(new Turno(true, pergunta));
        return chamar(instrucao, conversa, false);
    }

    private DadosViagem anonimizar(Viagem viagem) {
        Viajante viajante = viagem.getViajante();

        List<String> historico = List.of();
        if (viajante.temHistoricoSuficiente()) {
            historico = viajante.getHistorico().stream()
                    .filter(v -> v.getValorTotal() != null)
                    .map(v -> "- " + v.getDestino() + ", "
                            + (ChronoUnit.DAYS.between(v.getDataInicio(), v.getDataFim()) + 1)
                            + " dias, gasto total " + reais(v.getValorTotal()))
                    .toList();
        }

        List<String> categorias = viagem.getCategorias().stream()
                .sorted((a, b) -> a.getCategoria().compareTo(b.getCategoria()))
                .map(c -> "- " + c.getCategoria() + ": reservado " + reais(c.getValorAlocado())
                        + ", gasto " + reais(c.totalGasto()))
                .toList();

        return new DadosViagem(viagem.getDestino(), viagem.getDataInicio(), viagem.getDataFim(),
                viagem.duracaoEmDias(), viagem.getTipo(), viajante.getEstilo(), viagem.getOrcamentoTotal(),
                viagem.totalGasto(), categorias, historico);
    }

    private String descrever(DadosViagem dados) {
        String quem = switch (dados.tipo()) {
            case INDIVIDUAL -> "1 pessoa";
            case CASAL -> "um casal";
            case FAMILIA -> "uma família (considere 2 adultos e 2 crianças)";
        };
        String texto = "Destino: " + dados.destino() + "\n"
                + "Período: " + dados.dataInicio().format(DATA_BR) + " a " + dados.dataFim().format(DATA_BR)
                + " (" + dados.dias() + " dias)\n"
                + "Quem vai viajar: " + quem + "\n";
        if (dados.estilo() != null) {
            texto += "Estilo de viagem: " + dados.estilo().name().toLowerCase(Locale.ROOT) + "\n";
        }
        return texto;
    }

    private String chamar(String instrucao, List<Turno> conversa, boolean respostaJson) {
        if (!estaConfigurado()) {
            throw iaIndisponivel("O assistente de IA não está configurado. Preencha GEMINI_API_KEY no arquivo .env.");
        }

        Map<String, Object> corpo = new LinkedHashMap<>();
        if (instrucao != null) {
            corpo.put("systemInstruction", Map.of("parts", List.of(Map.of("text", instrucao))));
        }
        corpo.put("contents", conversa.stream()
                .map(t -> Map.<String, Object>of(
                        "role", t.usuario() ? "user" : "model",
                        "parts", List.of(Map.of("text", t.texto()))))
                .toList());
        if (respostaJson) {
            corpo.put("generationConfig", Map.of("responseMimeType", "application/json"));
        }

        RestClientException ultimaRede = null;
        for (int tentativa = 1; tentativa <= 3; tentativa++) {
            try {
                return restClient.post()
                        .uri("/models/{modelo}:generateContent", modelo)
                        .header("x-goog-api-key", apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .body(corpo)
                        .exchange((pedido, respostaHttp) -> lerRespostaGemini(respostaHttp));
            } catch (GeminiOcupado e) {
                log.warn("Gemini ocupado (modelo {}), tentativa {}/3", modelo, tentativa);
                esperar(tentativa);
            } catch (ApiException e) {
                throw e;
            } catch (RestClientException e) {
                ultimaRede = e;
                log.warn("Falha de rede no Gemini (modelo {}), tentativa {}/3: {}", modelo, tentativa, e.getMessage());
                esperar(tentativa);
            }
        }
        if (ultimaRede != null) {
            log.error("Falha ao chamar o Gemini (modelo {}): {}", modelo, ultimaRede.getMessage());
        }
        throw iaIndisponivel("O assistente de IA está indisponível no momento. Tente novamente em instantes.");
    }

    private void esperar(int tentativa) {
        try {
            Thread.sleep(2000L * tentativa);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw iaIndisponivel("O assistente de IA está indisponível no momento. Tente novamente em instantes.");
        }
    }

    private String lerRespostaGemini(RestClient.RequestHeadersSpec.ConvertibleClientHttpResponse respostaHttp)
            throws IOException {
        int status = respostaHttp.getStatusCode().value();
        String tipo = respostaHttp.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
        String bruto = new String(respostaHttp.getBody().readAllBytes(), StandardCharsets.UTF_8);
        String recorte = recortar(bruto);

        if (respostaHttp.getStatusCode().isError()) {
            log.error("Gemini HTTP {} modelo {} tipo {}: {}", status, modelo, tipo, recorte);
            if (status == 503) {
                throw new GeminiOcupado();
            }
            throw iaIndisponivel(mensagemErroGemini(bruto, status));
        }

        JsonNode resposta;
        try {
            resposta = objectMapper.readTree(bruto.isBlank() ? "{}" : bruto);
        } catch (JsonProcessingException e) {
            log.error("Gemini HTTP {} modelo {} corpo não-JSON tipo {}: {}", status, modelo, tipo, recorte);
            throw iaIndisponivel("O assistente de IA está indisponível no momento. Tente novamente em instantes.");
        }

        if (resposta.has("error")) {
            String detalhe = resposta.path("error").path("message").asText("");
            log.error("Gemini HTTP {} modelo {}: {}", status, modelo, recortar(detalhe));
            throw iaIndisponivel(mensagemErroGemini(bruto, status));
        }

        StringBuilder texto = new StringBuilder();
        for (JsonNode parte : resposta.path("candidates").path(0).path("content").path("parts")) {
            if (!parte.path("thought").asBoolean(false)) {
                texto.append(parte.path("text").asText(""));
            }
        }
        if (texto.toString().isBlank()) {
            log.warn("Resposta do Gemini sem texto (HTTP {}, tipo {}): {}", status, tipo, recorte);
            throw iaIndisponivel("O assistente de IA não conseguiu responder a este pedido.");
        }
        return texto.toString();
    }

    private String mensagemErroGemini(String corpo, int status) {
        try {
            String detalhe = objectMapper.readTree(corpo).path("error").path("message").asText("");
            if (detalhe.toLowerCase(Locale.ROOT).contains("not found")
                    || detalhe.toLowerCase(Locale.ROOT).contains("is not found")) {
                return "O modelo de IA configurado não está disponível para esta chave. Confira GEMINI_MODEL no .env.";
            }
            if (status == 401 || status == 403) {
                return "A chave do Gemini foi recusada. Confira GEMINI_API_KEY no arquivo .env.";
            }
        } catch (JsonProcessingException ignored) {
            // corpo não é JSON; mensagem genérica abaixo
        }
        return "O assistente de IA está indisponível no momento. Tente novamente em instantes.";
    }

    private String recortar(String texto) {
        if (texto == null) {
            return "";
        }
        String seguro = apiKey == null || apiKey.isBlank() ? texto : texto.replace(apiKey, "***");
        return seguro.length() <= 400 ? seguro : seguro.substring(0, 400) + "…";
    }

    private JsonNode lerJson(String texto) {
        String limpo = texto.trim();
        if (limpo.startsWith("```")) {
            limpo = limpo.replaceFirst("^```[a-zA-Z]*\\s*", "").replaceFirst("\\s*```$", "");
        }
        try {
            return objectMapper.readTree(limpo);
        } catch (JsonProcessingException e) {
            log.warn("JSON inválido da IA: {}", texto);
            throw iaIndisponivel("O assistente de IA respondeu num formato inesperado. Tente novamente.");
        }
    }

    private BigDecimal lerValor(JsonNode campo) {
        try {
            BigDecimal valor = campo.isNumber() ? campo.decimalValue()
                    : new BigDecimal(campo.asText().replace("R$", "").replace(".", "").replace(",", ".").trim());
            return valor.signum() < 0 ? null : valor.setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private CategoriaGasto converterCategoria(String texto) {
        String semAcento = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        try {
            return CategoriaGasto.valueOf(semAcento.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String reais(BigDecimal valor) {
        return NumberFormat.getCurrencyInstance(Locale.of("pt", "BR")).format(valor).replace('\u00a0', ' ');
    }

    private ApiException iaIndisponivel(String mensagem) {
        return new ApiException(HttpStatus.SERVICE_UNAVAILABLE, mensagem);
    }

    private record Turno(boolean usuario, String texto) {
    }

    private static class GeminiOcupado extends RuntimeException {
    }
}
