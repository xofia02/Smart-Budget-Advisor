package br.ucsal.smartbudget.dto;

import br.ucsal.smartbudget.model.Alerta;
import br.ucsal.smartbudget.model.Despesa;
import br.ucsal.smartbudget.model.enums.CategoriaGasto;
import br.ucsal.smartbudget.model.enums.TipoAlerta;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class DespesaDto {
    private DespesaDto() {
    }

    public record DespesaRequest(
            @NotNull(message = "Informe o valor.")
            @Positive(message = "O valor deve ser maior que zero.")
            @Digits(integer = 10, fraction = 2, message = "Valor inválido.")
            BigDecimal valor,

            @NotNull(message = "Informe a categoria.")
            CategoriaGasto categoria,

            @NotNull(message = "Informe a data da despesa.")
            LocalDate data,

            @Size(max = 255, message = "A descrição pode ter no máximo 255 caracteres.")
            String descricao
    ) {
    }

    public record DespesaResponse(Long id, BigDecimal valor, CategoriaGasto categoria, LocalDate data,
                                  String descricao, AlertaResponse alertaGerado) {
        public static DespesaResponse de(Despesa d, Alerta alerta) {
            return new DespesaResponse(d.getId(), d.getValor(), d.getCategoriaOrcamento().getCategoria(),
                    d.getData(), d.getDescricao(), alerta == null ? null : AlertaResponse.de(alerta));
        }

        public static DespesaResponse de(Despesa d) {
            return de(d, null);
        }
    }

    public record AlertaResponse(Long id, TipoAlerta tipo, CategoriaGasto categoria,
                                 BigDecimal valorExcedido, LocalDateTime geradoEm) {
        public static AlertaResponse de(Alerta a) {
            return new AlertaResponse(a.getId(), a.getTipo(), a.getCategoriaOrcamento().getCategoria(),
                    a.getValorExcedido(), a.getGeradoEm());
        }
    }
}
