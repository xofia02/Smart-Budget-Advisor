package br.ucsal.smartbudget.model;

import br.ucsal.smartbudget.model.enums.EstiloViagem;
import br.ucsal.smartbudget.model.enums.TipoViagem;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("VIAJANTE")
@Getter
@Setter
@NoArgsConstructor
public class Viajante extends Usuario {
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private EstiloViagem estilo;

    @OneToMany(mappedBy = "viajante", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ViagemAnterior> historico = new ArrayList<>();

    @OneToMany(mappedBy = "viajante", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Viagem> viagens = new ArrayList<>();

    @Override
    public String tipo() {
        return "VIAJANTE";
    }

    public void atualizarPerfil(EstiloViagem estilo) {
        this.estilo = estilo;
    }

    public Viagem criarViagem(String destino, LocalDate inicio, LocalDate fim, BigDecimal orcamento, TipoViagem tipo) {
        Viagem viagem = new Viagem();
        viagem.setDestino(destino);
        viagem.setDataInicio(inicio);
        viagem.setDataFim(fim);
        viagem.setOrcamentoTotal(orcamento);
        viagem.setTipo(tipo);
        viagem.setViajante(this);
        viagem.criarCategoriasPadrao();
        viagens.add(viagem);
        return viagem;
    }

    public void adicionarViagemAnterior(ViagemAnterior anterior) {
        anterior.setViajante(this);
        historico.add(anterior);
    }

    public boolean temHistoricoSuficiente() {
        return historico.stream().filter(v -> v.getValorTotal() != null).count() >= 2;
    }
}
