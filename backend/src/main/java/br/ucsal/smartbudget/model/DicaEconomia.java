package br.ucsal.smartbudget.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "dicas_economia")
@Getter
@Setter
@NoArgsConstructor
public class DicaEconomia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String texto;

    @Column(name = "alerta_turista", nullable = false)
    private boolean alertaTurista;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "viagem_id", nullable = false)
    private Viagem viagem;
}
