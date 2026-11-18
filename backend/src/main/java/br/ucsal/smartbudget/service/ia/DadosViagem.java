package br.ucsal.smartbudget.service.ia;

import br.ucsal.smartbudget.model.enums.EstiloViagem;
import br.ucsal.smartbudget.model.enums.TipoViagem;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DadosViagem(
        String destino,
        LocalDate dataInicio,
        LocalDate dataFim,
        int dias,
        TipoViagem tipo,
        EstiloViagem estilo,
        BigDecimal orcamentoTotal,
        BigDecimal totalGasto,
        List<String> categorias,
        List<String> historico
) {
}
