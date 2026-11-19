package br.ucsal.smartbudget.dto;

import br.ucsal.smartbudget.model.DicaEconomia;
import br.ucsal.smartbudget.model.MensagemChat;
import br.ucsal.smartbudget.model.PrevisaoCategoria;
import br.ucsal.smartbudget.model.PrevisaoCusto;
import br.ucsal.smartbudget.model.enums.CategoriaGasto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class IaDto {
    public static final String AVISO_PREVISAO_GENERICA = "Previsão genérica: cadastre ao menos 2 viagens "
            + "anteriores com o valor gasto no seu perfil para receber uma previsão personalizada.";

    public static final String AVISO_CHAT_GENERICO = "Sem uma viagem selecionada, as respostas são orientações "
            + "genéricas. Crie ou abra uma viagem para receber respostas com base no seu destino e orçamento.";

    private IaDto() {
    }

    public record ItemPrevisao(CategoriaGasto categoria, BigDecimal minimo, BigDecimal recomendado,
                               BigDecimal maximo) {
        public static ItemPrevisao de(PrevisaoCategoria p) {
            return new ItemPrevisao(p.getCategoria(), p.getMinimo(), p.getRecomendado(), p.getMaximo());
        }
    }

    public record PrevisaoResponse(Long id, boolean personalizada, LocalDateTime geradaEm, String aviso,
                                   List<ItemPrevisao> itens) {
        public static PrevisaoResponse de(PrevisaoCusto p) {
            List<ItemPrevisao> itens = p.getItens().stream()
                    .sorted((a, b) -> a.getCategoria().compareTo(b.getCategoria()))
                    .map(ItemPrevisao::de)
                    .toList();
            return new PrevisaoResponse(p.getId(), p.isPersonalizada(), p.getGeradaEm(),
                    p.isPersonalizada() ? null : AVISO_PREVISAO_GENERICA, itens);
        }
    }

    public record DicaResponse(Long id, String texto, boolean alertaTurista) {
        public static DicaResponse de(DicaEconomia d) {
            return new DicaResponse(d.getId(), d.getTexto(), d.isAlertaTurista());
        }
    }

    public record PerguntaRequest(
            @NotBlank(message = "Digite uma pergunta.")
            @Size(max = 1000, message = "A pergunta pode ter no máximo 1000 caracteres.")
            String pergunta
    ) {
    }

    public record MensagemResponse(Long id, String papel, String conteudo, LocalDateTime enviadaEm) {
        public static MensagemResponse de(MensagemChat m) {
            return new MensagemResponse(m.getId(), m.getPapel(), m.getConteudo(), m.getEnviadaEm());
        }
    }

    public record ChatResponse(MensagemResponse pergunta, MensagemResponse resposta) {
    }

    public record ChatGeralResponse(String resposta, String aviso) {
    }
}
