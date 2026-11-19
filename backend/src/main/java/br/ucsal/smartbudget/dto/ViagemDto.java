package br.ucsal.smartbudget.dto;

import br.ucsal.smartbudget.model.Alerta;
import br.ucsal.smartbudget.model.CategoriaOrcamento;
import br.ucsal.smartbudget.model.Viagem;
import br.ucsal.smartbudget.model.enums.CategoriaGasto;
import br.ucsal.smartbudget.model.enums.TipoAlerta;
import br.ucsal.smartbudget.model.enums.TipoViagem;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class ViagemDto {
    private ViagemDto() {
    }

    public record ViagemRequest(
            @NotBlank(message = "Informe o destino.")
            @Size(max = 120, message = "O destino pode ter no máximo 120 caracteres.")
            String destino,

            @NotNull(message = "Informe a data de início.")
            LocalDate dataInicio,

            @NotNull(message = "Informe a data de término.")
            LocalDate dataFim,

            @NotNull(message = "Informe o orçamento total.")
            @Digits(integer = 10, fraction = 2, message = "Orçamento inválido.")
            BigDecimal orcamentoTotal,

            @NotNull(message = "Informe o tipo da viagem (INDIVIDUAL, CASAL ou FAMILIA).")
            TipoViagem tipo
    ) {
    }

    public record ViagemResumoResponse(Long id, String destino, LocalDate dataInicio, LocalDate dataFim,
                                       TipoViagem tipo, BigDecimal orcamentoTotal, BigDecimal totalGasto,
                                       BigDecimal percentualConsumido, boolean bloqueada) {
        public static ViagemResumoResponse de(Viagem v) {
            return new ViagemResumoResponse(v.getId(), v.getDestino(), v.getDataInicio(), v.getDataFim(),
                    v.getTipo(), v.getOrcamentoTotal(), v.totalGasto(), v.percentualConsumido(),
                    v.estaBloqueadaPorEstouro());
        }
    }

    public record CategoriaResponse(Long id, CategoriaGasto categoria, BigDecimal valorAlocado,
                                    BigDecimal totalGasto, BigDecimal percentualUsado,
                                    BigDecimal valorExcedido, TipoAlerta alerta) {
        public static CategoriaResponse de(CategoriaOrcamento c) {
            Alerta alerta = c.getViagem().temOrcamentoDistribuido() ? c.verificarAlerta() : null;
            return new CategoriaResponse(c.getId(), c.getCategoria(), c.getValorAlocado(), c.totalGasto(),
                    c.percentualUsado(), c.valorExcedido(), alerta == null ? null : alerta.getTipo());
        }
    }

    public record ViagemDetalheResponse(Long id, String destino, LocalDate dataInicio, LocalDate dataFim,
                                        int duracaoEmDias, TipoViagem tipo, BigDecimal orcamentoTotal,
                                        BigDecimal totalAlocado, BigDecimal totalGasto,
                                        BigDecimal percentualConsumido, boolean bloqueada,
                                        List<CategoriaResponse> categorias) {
        public static ViagemDetalheResponse de(Viagem v) {
            List<CategoriaResponse> categorias = v.getCategorias().stream()
                    .sorted((a, b) -> a.getCategoria().compareTo(b.getCategoria()))
                    .map(CategoriaResponse::de)
                    .toList();
            return new ViagemDetalheResponse(v.getId(), v.getDestino(), v.getDataInicio(), v.getDataFim(),
                    v.duracaoEmDias(), v.getTipo(), v.getOrcamentoTotal(), v.totalAlocado(), v.totalGasto(),
                    v.percentualConsumido(), v.estaBloqueadaPorEstouro(), categorias);
        }
    }
}
