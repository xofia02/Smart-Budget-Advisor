package br.ucsal.smartbudget.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "previsoes_custo")
@Getter
@Setter
@NoArgsConstructor
public class PrevisaoCusto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private boolean personalizada;

    @Column(name = "gerada_em", nullable = false)
    private LocalDateTime geradaEm;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "viagem_id", nullable = false)
    private Viagem viagem;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "previsao_categorias", joinColumns = @JoinColumn(name = "previsao_id"))
    private List<PrevisaoCategoria> itens = new ArrayList<>();

    @PrePersist
    protected void aoCriar() {
        if (geradaEm == null) {
            geradaEm = LocalDateTime.now();
        }
    }
}
