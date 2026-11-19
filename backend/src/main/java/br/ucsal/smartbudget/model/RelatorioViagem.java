package br.ucsal.smartbudget.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Entity
@Table(name = "relatorios_viagem")
@Getter
@Setter
@NoArgsConstructor
public class RelatorioViagem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "total_planejado", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPlanejado;

    @Column(name = "total_gasto", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalGasto;

    @Column(name = "percentual_desvio", nullable = false, precision = 7, scale = 2)
    private BigDecimal percentualDesvio;

    @Column(name = "gerado_em", nullable = false)
    private LocalDateTime geradoEm;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "viagem_id", nullable = false, unique = true)
    private Viagem viagem;

    public static RelatorioViagem gerarDe(Viagem viagem) {
        RelatorioViagem r = new RelatorioViagem();
        r.preencherCom(viagem);
        return r;
    }

    public void preencherCom(Viagem viagem) {
        this.viagem = viagem;
        this.totalPlanejado = viagem.getOrcamentoTotal();
        this.totalGasto = viagem.totalGasto();
        BigDecimal desvio = calcularDesvio(totalPlanejado, totalGasto);
        this.percentualDesvio = desvio == null ? BigDecimal.ZERO : desvio;
        this.geradoEm = LocalDateTime.now();
    }

    public static BigDecimal calcularDesvio(BigDecimal planejado, BigDecimal gasto) {
        if (planejado == null || planejado.signum() == 0) {
            return null;
        }
        return gasto.subtract(planejado)
                .multiply(BigDecimal.valueOf(100))
                .divide(planejado, 2, RoundingMode.HALF_UP);
    }
}
