package br.ucsal.smartbudget.dto;

import br.ucsal.smartbudget.model.RelatorioViagem;
import br.ucsal.smartbudget.model.enums.CategoriaGasto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class OrcamentoDto {
    private OrcamentoDto() {
    }

    public record ItemDistribuicao(
            @NotNull(message = "Informe a categoria.")
            CategoriaGasto categoria,

            @NotNull(message = "Informe o valor de cada categoria.")
            @PositiveOrZero(message = "O valor de uma categoria não pode ser negativo.")
            @Digits(integer = 10, fraction = 2, message = "Valor inválido.")
            BigDecimal valorAlocado
    ) {
    }

    public record DistribuicaoRequest(
            @NotNull(message = "Envie a lista de categorias.")
            @Size(min = 6, max = 6, message = "Envie exatamente as 6 categorias.")
            List<@Valid ItemDistribuicao> categorias
    ) {
    }

    public record RelatorioCategoria(CategoriaGasto categoria, BigDecimal planejado, BigDecimal gasto,
                                     BigDecimal percentualDesvio) {
    }

    public record RelatorioResponse(BigDecimal totalPlanejado, BigDecimal totalGasto, BigDecimal percentualDesvio,
                                    boolean viagemEncerrada, LocalDateTime geradoEm,
                                    List<RelatorioCategoria> categorias) {
        public static RelatorioResponse de(RelatorioViagem r) {
            List<RelatorioCategoria> categorias = r.getViagem().getCategorias().stream()
                    .sorted((a, b) -> a.getCategoria().compareTo(b.getCategoria()))
                    .map(c -> new RelatorioCategoria(c.getCategoria(), c.getValorAlocado(), c.totalGasto(),
                            RelatorioViagem.calcularDesvio(c.getValorAlocado(), c.totalGasto())))
                    .toList();
            boolean encerrada = r.getViagem().getDataFim().isBefore(LocalDate.now());
            return new RelatorioResponse(r.getTotalPlanejado(), r.getTotalGasto(), r.getPercentualDesvio(),
                    encerrada, r.getGeradoEm(), categorias);
        }
    }
}
