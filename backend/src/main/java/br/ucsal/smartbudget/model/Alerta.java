package br.ucsal.smartbudget.model;

import br.ucsal.smartbudget.model.enums.TipoAlerta;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "alertas")
@Getter
@Setter
@NoArgsConstructor
public class Alerta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoAlerta tipo;

    @Column(name = "valor_excedido", precision = 12, scale = 2)
    private BigDecimal valorExcedido = BigDecimal.ZERO;

    @Column(name = "gerado_em", nullable = false)
    private LocalDateTime geradoEm;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_orcamento_id", nullable = false)
    private CategoriaOrcamento categoriaOrcamento;

    @PrePersist
    protected void aoCriar() {
        if (geradoEm == null) {
            geradoEm = LocalDateTime.now();
        }
    }
}
