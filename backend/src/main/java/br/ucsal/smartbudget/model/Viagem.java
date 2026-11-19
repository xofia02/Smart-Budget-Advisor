package br.ucsal.smartbudget.model;

import br.ucsal.smartbudget.model.enums.CategoriaGasto;
import br.ucsal.smartbudget.model.enums.TipoViagem;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "viagens")
@Getter
@Setter
@NoArgsConstructor
public class Viagem {
    public static final BigDecimal ORCAMENTO_MINIMO = new BigDecimal("500.00");
    public static final BigDecimal LIMITE_BLOQUEIO = new BigDecimal("120");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String destino;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim", nullable = false)
    private LocalDate dataFim;

    @Column(name = "orcamento_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal orcamentoTotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoViagem tipo;

    @Column(name = "estouro_revisado", nullable = false, columnDefinition = "boolean default false")
    private boolean estouroRevisado;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "viajante_id", nullable = false)
    private Viajante viajante;

    @OneToMany(mappedBy = "viagem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CategoriaOrcamento> categorias = new ArrayList<>();

    @OneToMany(mappedBy = "viagem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PrevisaoCusto> previsoes = new ArrayList<>();

    @OneToMany(mappedBy = "viagem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DicaEconomia> dicas = new ArrayList<>();

    @OneToMany(mappedBy = "viagem", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("enviadaEm ASC")
    private List<MensagemChat> mensagens = new ArrayList<>();

    @OneToOne(mappedBy = "viagem", cascade = CascadeType.ALL, orphanRemoval = true)
    private RelatorioViagem relatorio;

    public void criarCategoriasPadrao() {
        if (!categorias.isEmpty()) {
            return;
        }
        for (CategoriaGasto cat : CategoriaGasto.values()) {
            CategoriaOrcamento categoria = new CategoriaOrcamento();
            categoria.setCategoria(cat);
            categoria.setValorAlocado(BigDecimal.ZERO);
            categoria.setViagem(this);
            categorias.add(categoria);
        }
    }

    public int duracaoEmDias() {
        return (int) ChronoUnit.DAYS.between(dataInicio, dataFim) + 1;
    }

    public BigDecimal totalGasto() {
        return categorias.stream()
                .map(CategoriaOrcamento::totalGasto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal totalAlocado() {
        return categorias.stream()
                .map(CategoriaOrcamento::getValorAlocado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal percentualConsumido() {
        if (orcamentoTotal == null || orcamentoTotal.signum() == 0) {
            return BigDecimal.ZERO;
        }
        return totalGasto().multiply(BigDecimal.valueOf(100))
                .divide(orcamentoTotal, 2, RoundingMode.HALF_UP);
    }

    public boolean validarRegras() {
        return listarErros().isEmpty();
    }

    public List<String> listarErros() {
        List<String> erros = new ArrayList<>();

        if (orcamentoTotal == null || orcamentoTotal.compareTo(ORCAMENTO_MINIMO) < 0) {
            erros.add("O orçamento total deve ser de pelo menos R$ 500,00.");
        }

        if (dataInicio == null || dataFim == null) {
            erros.add("Informe a data de início e a data de término.");
        } else if (dataFim.isBefore(dataInicio)) {
            erros.add("A data de término não pode ser antes da data de início.");
        } else if (duracaoEmDias() < 2 || duracaoEmDias() > 365) {
            erros.add("A viagem deve durar entre 2 e 365 dias.");
        }

        if (orcamentoTotal != null && totalAlocado().compareTo(orcamentoTotal) > 0) {
            erros.add("A soma das categorias ultrapassa o orçamento total da viagem.");
        }
        return erros;
    }

    public boolean passouDoLimiteDeBloqueio() {
        if (orcamentoTotal == null) {
            return false;
        }
        return totalGasto().multiply(BigDecimal.valueOf(100))
                .compareTo(orcamentoTotal.multiply(LIMITE_BLOQUEIO)) > 0;
    }

    public boolean estaBloqueadaPorEstouro() {
        return passouDoLimiteDeBloqueio() && !estouroRevisado;
    }

    public void registrarRevisaoDoOrcamento() {
        if (passouDoLimiteDeBloqueio()) {
            estouroRevisado = true;
        }
    }

    public void reavaliarBloqueio() {
        if (!passouDoLimiteDeBloqueio()) {
            estouroRevisado = false;
        }
    }

    public boolean temOrcamentoDistribuido() {
        return totalAlocado().signum() > 0;
    }

    public boolean dataDentroDoPeriodo(LocalDate data) {
        return data != null && !data.isBefore(dataInicio) && !data.isAfter(dataFim);
    }

    public CategoriaOrcamento buscarCategoria(CategoriaGasto categoria) {
        return categorias.stream()
                .filter(c -> c.getCategoria() == categoria)
                .findFirst()
                .orElseThrow();
    }
}
