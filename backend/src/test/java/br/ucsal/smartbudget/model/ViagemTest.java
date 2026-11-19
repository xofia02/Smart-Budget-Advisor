package br.ucsal.smartbudget.model;

import br.ucsal.smartbudget.model.enums.CategoriaGasto;
import br.ucsal.smartbudget.model.enums.TipoViagem;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ViagemTest {
    private Viagem viagemValida() {
        Viajante ana = new Viajante();
        return ana.criarViagem("Gramado", LocalDate.of(2026, 12, 10), LocalDate.of(2026, 12, 14),
                new BigDecimal("6000.00"), TipoViagem.CASAL);
    }

    private void gastar(Viagem v, CategoriaGasto categoria, String valor) {
        Despesa d = new Despesa();
        d.setValor(new BigDecimal(valor));
        d.setData(v.getDataInicio());
        v.buscarCategoria(categoria).adicionarDespesa(d);
    }

    @Test
    void viagemValidaPassaNasRegras() {
        assertTrue(viagemValida().validarRegras());
    }

    @Test
    void rn001OrcamentoMinimo() {
        Viagem v = viagemValida();
        v.setOrcamentoTotal(new BigDecimal("499.99"));
        assertFalse(v.validarRegras());
        assertEquals(List.of("O orçamento total deve ser de pelo menos R$ 500,00."), v.listarErros());
    }

    @Test
    void rn003ViagemDeUmDiaNaoPode() {
        Viagem v = viagemValida();
        v.setDataFim(v.getDataInicio());
        assertEquals(List.of("A viagem deve durar entre 2 e 365 dias."), v.listarErros());
    }

    @Test
    void rn003AceitaExatamente365Dias() {
        Viagem v = viagemValida();
        v.setDataFim(v.getDataInicio().plusDays(364));
        assertEquals(365, v.duracaoEmDias());
        assertTrue(v.validarRegras());
    }

    @Test
    void rn004SeisCategoriasSemDuplicar() {
        Viagem v = viagemValida();
        v.criarCategoriasPadrao();
        assertEquals(6, v.getCategorias().size());
    }

    @Test
    void rn005CategoriasNaoPassamDoTotal() {
        Viagem v = viagemValida();
        v.buscarCategoria(CategoriaGasto.HOSPEDAGEM).setValorAlocado(new BigDecimal("6000.01"));
        assertEquals(List.of("A soma das categorias ultrapassa o orçamento total da viagem."), v.listarErros());
    }

    @Test
    void rn006BloqueiaAcimaDe120AteRevisar() {
        Viagem v = viagemValida();
        gastar(v, CategoriaGasto.HOSPEDAGEM, "7200.00");
        assertFalse(v.estaBloqueadaPorEstouro());

        gastar(v, CategoriaGasto.OUTROS, "0.01");
        assertTrue(v.estaBloqueadaPorEstouro());

        v.registrarRevisaoDoOrcamento();
        assertFalse(v.estaBloqueadaPorEstouro());
    }

    @Test
    void rn006VoltaABloquearNumNovoEstouro() {
        Viagem v = viagemValida();
        gastar(v, CategoriaGasto.HOSPEDAGEM, "8000.00");
        v.registrarRevisaoDoOrcamento();

        v.setOrcamentoTotal(new BigDecimal("10000.00"));
        v.reavaliarBloqueio();
        v.setOrcamentoTotal(new BigDecimal("6000.00"));
        assertTrue(v.estaBloqueadaPorEstouro());
    }

    @Test
    void rn008DataDentroDoPeriodo() {
        Viagem v = viagemValida();
        assertTrue(v.dataDentroDoPeriodo(LocalDate.of(2026, 12, 10)));
        assertTrue(v.dataDentroDoPeriodo(LocalDate.of(2026, 12, 14)));
        assertFalse(v.dataDentroDoPeriodo(LocalDate.of(2026, 12, 15)));
    }

    @Test
    void percentualConsumido() {
        Viagem v = viagemValida();
        gastar(v, CategoriaGasto.ALIMENTACAO, "1500.00");
        gastar(v, CategoriaGasto.PASSEIOS, "3000.00");
        assertEquals(new BigDecimal("4500.00"), v.totalGasto());
        assertEquals(new BigDecimal("75.00"), v.percentualConsumido());
    }

    @Test
    void relatorioCalculaDesvio() {
        Viagem v = viagemValida();
        gastar(v, CategoriaGasto.HOSPEDAGEM, "6300.00");
        assertEquals(new BigDecimal("5.00"), RelatorioViagem.gerarDe(v).getPercentualDesvio());
    }
}
