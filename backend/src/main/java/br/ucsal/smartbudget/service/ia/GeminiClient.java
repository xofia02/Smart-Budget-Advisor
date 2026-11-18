package br.ucsal.smartbudget.service.ia;

import br.ucsal.smartbudget.excecao.ApiException;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
public class GeminiClient {
    private static final Logger log = LoggerFactory.getLogger(GeminiClient.class);

    private final String apiKey;
    private final String modelo;
    private final RestClient restClient;

    public GeminiClient(@Value("${gemini.api-key}") String apiKey,
                        @Value("${gemini.model}") String modelo,
                        @Value("${gemini.base-url}") String baseUrl,
                        @Value("${gemini.timeout-segundos}") int timeoutSegundos) {
        this.apiKey = apiKey;
        this.modelo = modelo;

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
        if (!estaConfigurado()) {
            throw iaIndisponivel("O assistente de IA não está configurado. Preencha GEMINI_API_KEY no arquivo .env.");
        }

        Map<String, Object> corpo = Map.of("contents", List.of(Map.of("parts", List.of(Map.of("text", texto)))));

        try {
            JsonNode resposta = restClient.post()
                    .uri("/models/{modelo}:generateContent", modelo)
                    .header("x-goog-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(corpo)
                    .retrieve()
                    .body(JsonNode.class);

            JsonNode resultado = resposta == null ? null
                    : resposta.path("candidates").path(0).path("content").path("parts").path(0).path("text");
            if (resultado == null || resultado.isMissingNode() || resultado.asText().isBlank()) {
                throw iaIndisponivel("O assistente de IA não conseguiu responder a este pedido.");
            }
            return resultado.asText();
        } catch (RestClientException e) {
            log.error("Falha ao chamar o Gemini (modelo {}): {}", modelo, e.getMessage());
            throw iaIndisponivel("O assistente de IA está indisponível no momento. Tente novamente em instantes.");
        }
    }

    private ApiException iaIndisponivel(String mensagem) {
        return new ApiException(HttpStatus.SERVICE_UNAVAILABLE, mensagem);
    }
}
