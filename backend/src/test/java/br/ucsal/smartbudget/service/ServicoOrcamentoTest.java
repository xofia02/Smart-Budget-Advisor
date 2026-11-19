package br.ucsal.smartbudget.service;

import br.ucsal.smartbudget.model.enums.CategoriaGasto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ServicoOrcamentoTest {
    private BigDecimal soma(Map<CategoriaGasto, BigDecimal> valores) {
        return valores.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Test
    void divideNaProporcaoDoRecomendado() {
        Map<CategoriaGasto, BigDecimal> recomendado = new EnumMap<>(CategoriaGasto.class);
        recomendado.put(CategoriaGasto.HOSPEDAGEM, new BigDecimal("2200"));
        recomendado.put(CategoriaGasto.ALIMENTACAO, new BigDecimal("1200"));
        recomendado.put(CategoriaGasto.TRANSPORTE, new BigDecimal("600"));
        recomendado.put(CategoriaGasto.PASSEIOS, new BigDecimal("1000"));
        recomendado.put(CategoriaGasto.COMPRAS, new BigDecimal("700"));
        recomendado.put(CategoriaGasto.OUTROS, new BigDecimal("300"));

        Map<CategoriaGasto, BigDecimal> r = ServicoOrcamento.dividirProporcionalmente(recomendado, new BigDecimal("3000.00"));
        assertEquals(new BigDecimal("1100.00"), r.get(CategoriaGasto.HOSPEDAGEM));
        assertEquals(new BigDecimal("150.00"), r.get(CategoriaGasto.OUTROS));
        assertEquals(new BigDecimal("3000.00"), soma(r));
    }

    @Test
    void somaFechaMesmoComDizima() {
        Map<CategoriaGasto, BigDecimal> pesos = new EnumMap<>(CategoriaGasto.class);
        pesos.put(CategoriaGasto.HOSPEDAGEM, BigDecimal.ONE);
        pesos.put(CategoriaGasto.ALIMENTACAO, BigDecimal.ONE);
        pesos.put(CategoriaGasto.TRANSPORTE, BigDecimal.ONE);

        Map<CategoriaGasto, BigDecimal> r = ServicoOrcamento.dividirProporcionalmente(pesos, new BigDecimal("1000.00"));
        assertEquals(new BigDecimal("1000.00"), soma(r));
        assertEquals(new BigDecimal("0.00"), r.get(CategoriaGasto.COMPRAS));
    }

    @Test
    void pesosPadrao() {
        Map<CategoriaGasto, BigDecimal> r =
                ServicoOrcamento.dividirProporcionalmente(ServicoOrcamento.PESOS_PADRAO, new BigDecimal("6000.00"));
        assertEquals(new BigDecimal("2100.00"), r.get(CategoriaGasto.HOSPEDAGEM));
        assertEquals(new BigDecimal("6000.00"), soma(r));
    }
}
