package br.ucsal.smartbudget.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "mensagens_chat")
@Getter
@Setter
@NoArgsConstructor
public class MensagemChat {
    public static final String PAPEL_USUARIO = "USUARIO";
    public static final String PAPEL_ASSISTENTE = "ASSISTENTE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String papel;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String conteudo;

    @Column(name = "enviada_em", nullable = false)
    private LocalDateTime enviadaEm;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "viagem_id", nullable = false)
    private Viagem viagem;

    @PrePersist
    protected void aoCriar() {
        if (enviadaEm == null) {
            enviadaEm = LocalDateTime.now();
        }
    }
}
