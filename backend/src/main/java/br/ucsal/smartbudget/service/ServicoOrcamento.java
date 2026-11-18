package br.ucsal.smartbudget.service;

import br.ucsal.smartbudget.excecao.ApiException;
import br.ucsal.smartbudget.model.Alerta;
import br.ucsal.smartbudget.model.CategoriaOrcamento;
import br.ucsal.smartbudget.model.Despesa;
import br.ucsal.smartbudget.model.Viagem;
import br.ucsal.smartbudget.repository.AlertaRepository;
import br.ucsal.smartbudget.repository.DespesaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;

@Service
public class ServicoOrcamento {
    private final DespesaRepository despesaRepository;
    private final AlertaRepository alertaRepository;

    public ServicoOrcamento(DespesaRepository despesaRepository, AlertaRepository alertaRepository) {
        this.despesaRepository = despesaRepository;
        this.alertaRepository = alertaRepository;
    }

    @Transactional
    public Alerta registrarDespesa(Despesa despesa) {
        CategoriaOrcamento categoria = despesa.getCategoriaOrcamento();
        Viagem viagem = categoria.getViagem();

        if (bloquearSeEstouro(viagem)) {
            throw new ApiException(HttpStatus.LOCKED, "Os gastos passaram de 120% do orçamento. "
                    + "Revise a distribuição do orçamento ou aumente o orçamento total para lançar novas despesas.");
        }
        conferirData(despesa, viagem);

        categoria.adicionarDespesa(despesa);
        despesaRepository.save(despesa);
        return gerarAlerta(categoria);
    }

    public boolean bloquearSeEstouro(Viagem viagem) {
        return viagem.estaBloqueadaPorEstouro();
    }

    @Transactional
    public Alerta gerarAlerta(CategoriaOrcamento categoria) {
        if (!categoria.getViagem().temOrcamentoDistribuido()) {
            return null;
        }
        Alerta alerta = categoria.verificarAlerta();
        if (alerta == null || alertaRepository.existsByCategoriaOrcamentoIdAndTipo(categoria.getId(), alerta.getTipo())) {
            return null;
        }
        categoria.getAlertas().add(alerta);
        return alertaRepository.save(alerta);
    }

    public void conferirData(Despesa despesa, Viagem viagem) {
        if (!despesa.validarData(viagem)) {
            DateTimeFormatter br = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            throw new ApiException(HttpStatus.BAD_REQUEST, "A data da despesa precisa estar entre "
                    + viagem.getDataInicio().format(br) + " e " + viagem.getDataFim().format(br) + ".");
        }
    }
}
