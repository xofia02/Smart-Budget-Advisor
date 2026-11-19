package br.ucsal.smartbudget.model;

import br.ucsal.smartbudget.model.enums.CategoriaGasto;
import br.ucsal.smartbudget.model.enums.TipoAlerta;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categorias_orcamento",
        uniqueConstraints = @UniqueConstraint(columnNames = {"viagem_id", "categoria"}))
@Getter
@Setter
@NoArgsConstructor
public class CategoriaOrcamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CategoriaGasto categoria;

    @Column(name = "valor_alocado", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorAlocado = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "viagem_id", nullable = false)
    private Viagem viagem;

    @OneToMany(mappedBy = "categoriaOrcamento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Despesa> despesas = new ArrayList<>();

    @OneToMany(mappedBy = "categoriaOrcamento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Alerta> alertas = new ArrayList<>();

    public void adicionarDespesa(Despesa despesa) {
        despesa.setCategoriaOrcamento(this);
        despesas.add(despesa);
    }

    public BigDecimal totalGasto() {
        return despesas.stream()
                .map(Despesa::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal percentualUsado() {
        BigDecimal gasto = totalGasto();
        if (valorAlocado.signum() == 0) {
            return gasto.signum() > 0 ? new BigDecimal("100.01") : BigDecimal.ZERO;
        }
        return gasto.multiply(BigDecimal.valueOf(100))
                .divide(valorAlocado, 2, RoundingMode.HALF_UP);
    }

    public BigDecimal valorExcedido() {
        BigDecimal diferenca = totalGasto().subtract(valorAlocado);
        return diferenca.signum() > 0 ? diferenca : BigDecimal.ZERO;
    }

    public Alerta verificarAlerta() {
        BigDecimal gasto = totalGasto();
        TipoAlerta tipo = null;
        if (gasto.compareTo(valorAlocado) > 0) {
            tipo = TipoAlerta.EXCEDIDO_100;
        } else if (gasto.signum() > 0 && gasto.multiply(BigDecimal.valueOf(100))
                .compareTo(valorAlocado.multiply(BigDecimal.valueOf(80))) >= 0) {
            tipo = TipoAlerta.ATENCAO_80;
        }
        if (tipo == null) {
            return null;
        }
        Alerta alerta = new Alerta();
        alerta.setTipo(tipo);
        alerta.setValorExcedido(valorExcedido());
        alerta.setCategoriaOrcamento(this);
        return alerta;
    }
}
