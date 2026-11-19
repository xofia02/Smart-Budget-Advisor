package br.ucsal.smartbudget.service;

import br.ucsal.smartbudget.dto.PerfilDto.ViagemAnteriorRequest;
import br.ucsal.smartbudget.excecao.ApiException;
import br.ucsal.smartbudget.model.ViagemAnterior;
import br.ucsal.smartbudget.model.Viajante;
import br.ucsal.smartbudget.model.enums.EstiloViagem;
import br.ucsal.smartbudget.repository.ViagemAnteriorRepository;
import br.ucsal.smartbudget.repository.ViajanteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class PerfilService {
    private final ViajanteRepository viajanteRepository;
    private final ViagemAnteriorRepository viagemAnteriorRepository;

    public PerfilService(ViajanteRepository viajanteRepository, ViagemAnteriorRepository viagemAnteriorRepository) {
        this.viajanteRepository = viajanteRepository;
        this.viagemAnteriorRepository = viagemAnteriorRepository;
    }

    public Viajante buscarViajante(Long id) {
        return viajanteRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Viajante não encontrado."));
    }

    @Transactional
    public Viajante atualizarPerfil(Long viajanteId, EstiloViagem estilo) {
        Viajante viajante = buscarViajante(viajanteId);
        viajante.atualizarPerfil(estilo);
        return viajante;
    }

    public List<ViagemAnterior> listarViagensAnteriores(Long viajanteId) {
        return viagemAnteriorRepository.findByViajanteIdOrderByDataInicioDesc(viajanteId);
    }

    @Transactional
    public ViagemAnterior adicionarViagemAnterior(Long viajanteId, ViagemAnteriorRequest dados) {
        ViagemAnterior anterior = new ViagemAnterior();
        preencher(anterior, dados);
        buscarViajante(viajanteId).adicionarViagemAnterior(anterior);
        return viagemAnteriorRepository.save(anterior);
    }

    @Transactional
    public ViagemAnterior atualizarViagemAnterior(Long viajanteId, Long id, ViagemAnteriorRequest dados) {
        ViagemAnterior anterior = buscarViagemAnterior(viajanteId, id);
        preencher(anterior, dados);
        return anterior;
    }

    @Transactional
    public void excluirViagemAnterior(Long viajanteId, Long id) {
        ViagemAnterior anterior = buscarViagemAnterior(viajanteId, id);
        anterior.getViajante().getHistorico().remove(anterior);
    }

    private void preencher(ViagemAnterior anterior, ViagemAnteriorRequest dados) {
        if (dados.dataFim().isBefore(dados.dataInicio())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "A data de término não pode ser antes da data de início.");
        }
        if (dados.dataFim().isAfter(LocalDate.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Viagens anteriores precisam já ter terminado. Para planejar, crie uma nova viagem.");
        }
        anterior.setDestino(dados.destino().trim());
        anterior.setDataInicio(dados.dataInicio());
        anterior.setDataFim(dados.dataFim());
        anterior.setValorTotal(dados.valorTotal());
    }

    private ViagemAnterior buscarViagemAnterior(Long viajanteId, Long id) {
        return viagemAnteriorRepository.findByIdAndViajanteId(id, viajanteId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Viagem anterior não encontrada."));
    }
}
