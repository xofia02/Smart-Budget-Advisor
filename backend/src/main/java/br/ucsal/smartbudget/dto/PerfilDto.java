package br.ucsal.smartbudget.dto;

import br.ucsal.smartbudget.model.ViagemAnterior;
import br.ucsal.smartbudget.model.Viajante;
import br.ucsal.smartbudget.model.enums.EstiloViagem;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class PerfilDto {
    private PerfilDto() {
    }

    public record PerfilResponse(Long id, String nome, String email, EstiloViagem estilo,
                                 int totalViagensAnteriores) {
        public static PerfilResponse de(Viajante v) {
            return new PerfilResponse(v.getId(), v.getNome(), v.getEmail(), v.getEstilo(), v.getHistorico().size());
        }
    }

    public record AtualizarPerfilRequest(
            @NotNull(message = "Informe o estilo de viagem.")
            EstiloViagem estilo
    ) {
    }

    public record ViagemAnteriorRequest(
            @NotBlank(message = "Informe o destino.")
            @Size(max = 120, message = "O destino pode ter no máximo 120 caracteres.")
            String destino,

            @NotNull(message = "Informe a data de início.")
            LocalDate dataInicio,

            @NotNull(message = "Informe a data de término.")
            LocalDate dataFim,

            @NotNull(message = "Informe quanto foi gasto no total.")
            @Positive(message = "O valor total deve ser maior que zero.")
            @Digits(integer = 10, fraction = 2, message = "Valor inválido.")
            BigDecimal valorTotal
    ) {
    }

    public record ViagemAnteriorResponse(Long id, String destino, LocalDate dataInicio,
                                         LocalDate dataFim, BigDecimal valorTotal) {
        public static ViagemAnteriorResponse de(ViagemAnterior v) {
            return new ViagemAnteriorResponse(v.getId(), v.getDestino(), v.getDataInicio(),
                    v.getDataFim(), v.getValorTotal());
        }
    }
}
