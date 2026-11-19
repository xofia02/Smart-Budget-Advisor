package br.ucsal.smartbudget.model;

import br.ucsal.smartbudget.model.enums.CategoriaGasto;
import br.ucsal.smartbudget.model.enums.TipoAlerta;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CategoriaOrcamentoTest {
    private CategoriaOrcamento categoriaCom(String gasto) {
        CategoriaOrcamento c = new CategoriaOrcamento();
        c.setCategoria(CategoriaGasto.ALIMENTACAO);
        c.setValorAlocado(new BigDecimal("1000.00"));
        Despesa d = new Despesa();
        d.setValor(new BigDecimal(gasto));
        d.setData(LocalDate.of(2026, 12, 10));
        c.adicionarDespesa(d);
        return c;
    }

    @Test
    void abaixoDe80NaoTemAlerta() {
        assertNull(categoriaCom("799.99").verificarAlerta());
    }

    @Test
    void com80TemAlertaDeAtencao() {
        assertEquals(TipoAlerta.ATENCAO_80, categoriaCom("800.00").verificarAlerta().getTipo());
    }

    @Test
    void com100AindaEAtencao() {
        assertEquals(TipoAlerta.ATENCAO_80, categoriaCom("1000.00").verificarAlerta().getTipo());
    }

    @Test
    void acimaDe100EExcedidoComOValor() {
        Alerta alerta = categoriaCom("1120.00").verificarAlerta();
        assertEquals(TipoAlerta.EXCEDIDO_100, alerta.getTipo());
        assertEquals(new BigDecimal("120.00"), alerta.getValorExcedido());
    }
}
