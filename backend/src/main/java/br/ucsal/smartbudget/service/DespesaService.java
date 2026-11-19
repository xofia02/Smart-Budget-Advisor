package br.ucsal.smartbudget.service;

import br.ucsal.smartbudget.dto.DespesaDto.DespesaRequest;
import br.ucsal.smartbudget.dto.DespesaDto.DespesaResponse;
import br.ucsal.smartbudget.excecao.ApiException;
import br.ucsal.smartbudget.model.Alerta;
import br.ucsal.smartbudget.model.CategoriaOrcamento;
import br.ucsal.smartbudget.model.Despesa;
import br.ucsal.smartbudget.model.Viagem;
import br.ucsal.smartbudget.model.enums.CategoriaGasto;
import br.ucsal.smartbudget.repository.AlertaRepository;
import br.ucsal.smartbudget.repository.DespesaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class DespesaService {
    private final DespesaRepository despesaRepository;
    private final AlertaRepository alertaRepository;
    private final ServicoOrcamento servicoOrcamento;

    public DespesaService(DespesaRepository despesaRepository, AlertaRepository alertaRepository,
                          ServicoOrcamento servicoOrcamento) {
        this.despesaRepository = despesaRepository;
        this.alertaRepository = alertaRepository;
        this.servicoOrcamento = servicoOrcamento;
    }

    public List<Despesa> listar(Viagem viagem, CategoriaGasto categoria, LocalDate inicio, LocalDate fim) {
        return despesaRepository.findByCategoriaOrcamentoViagemIdOrderByDataDescIdDesc(viagem.getId()).stream()
                .filter(d -> categoria == null || d.getCategoriaOrcamento().getCategoria() == categoria)
                .filter(d -> inicio == null || !d.getData().isBefore(inicio))
                .filter(d -> fim == null || !d.getData().isAfter(fim))
                .toList();
    }

    @Transactional
    public DespesaResponse criar(Viagem viagem, DespesaRequest dados) {
        Despesa despesa = new Despesa();
        preencher(despesa, dados);
        despesa.setCategoriaOrcamento(viagem.buscarCategoria(dados.categoria()));
        Alerta alerta = servicoOrcamento.registrarDespesa(despesa);
        return DespesaResponse.de(despesa, alerta);
    }

    @Transactional
    public DespesaResponse atualizar(Viagem viagem, Long despesaId, DespesaRequest dados) {
        Despesa despesa = buscar(viagem, despesaId);
        preencher(despesa, dados);
        servicoOrcamento.conferirData(despesa, viagem);

        CategoriaOrcamento nova = viagem.buscarCategoria(dados.categoria());
        if (!despesa.getCategoriaOrcamento().getId().equals(nova.getId())) {
            Despesa copia = new Despesa();
            preencher(copia, dados);
            despesa.getCategoriaOrcamento().getDespesas().remove(despesa);
            nova.adicionarDespesa(copia);
            despesa = copia;
        }
        despesa = despesaRepository.save(despesa);

        viagem.reavaliarBloqueio();
        Alerta alerta = servicoOrcamento.gerarAlerta(nova);
        return DespesaResponse.de(despesa, alerta);
    }

    @Transactional
    public void excluir(Viagem viagem, Long despesaId) {
        Despesa despesa = buscar(viagem, despesaId);
        despesa.getCategoriaOrcamento().getDespesas().remove(despesa);
        viagem.reavaliarBloqueio();
    }

    public List<Alerta> listarAlertas(Viagem viagem) {
        return alertaRepository.findByCategoriaOrcamentoViagemIdOrderByGeradoEmDesc(viagem.getId());
    }

    private Despesa buscar(Viagem viagem, Long despesaId) {
        return despesaRepository.findByIdAndCategoriaOrcamentoViagemId(despesaId, viagem.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Despesa não encontrada."));
    }

    private void preencher(Despesa despesa, DespesaRequest dados) {
        despesa.setValor(dados.valor());
        despesa.setData(dados.data());
        despesa.setDescricao(dados.descricao() == null ? null : dados.descricao().trim());
    }
}
