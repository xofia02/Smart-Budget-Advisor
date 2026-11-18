package br.ucsal.smartbudget.service;

import br.ucsal.smartbudget.dto.ViagemDto.ViagemRequest;
import br.ucsal.smartbudget.excecao.ApiException;
import br.ucsal.smartbudget.model.Viagem;
import br.ucsal.smartbudget.model.Viajante;
import br.ucsal.smartbudget.repository.ViagemRepository;
import br.ucsal.smartbudget.repository.ViajanteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ViagemService {
    private final ViagemRepository viagemRepository;
    private final ViajanteRepository viajanteRepository;

    public ViagemService(ViagemRepository viagemRepository, ViajanteRepository viajanteRepository) {
        this.viagemRepository = viagemRepository;
        this.viajanteRepository = viajanteRepository;
    }

    public List<Viagem> listar(Long viajanteId) {
        return viagemRepository.findByViajanteIdOrderByDataInicioDesc(viajanteId);
    }

    public Viagem buscarDoViajante(Long viajanteId, Long viagemId) {
        return viagemRepository.findByIdAndViajanteId(viagemId, viajanteId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Viagem não encontrada."));
    }

    @Transactional
    public Viagem criar(Long viajanteId, ViagemRequest dados) {
        Viajante viajante = viajanteRepository.findById(viajanteId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Viajante não encontrado."));

        Viagem viagem = viajante.criarViagem(dados.destino().trim(), dados.dataInicio(), dados.dataFim(),
                dados.orcamentoTotal(), dados.tipo());
        validar(viagem);
        return viagemRepository.save(viagem);
    }

    @Transactional
    public Viagem atualizar(Long viajanteId, Long viagemId, ViagemRequest dados) {
        Viagem viagem = buscarDoViajante(viajanteId, viagemId);
        viagem.setDestino(dados.destino().trim());
        viagem.setDataInicio(dados.dataInicio());
        viagem.setDataFim(dados.dataFim());
        viagem.setOrcamentoTotal(dados.orcamentoTotal());
        viagem.setTipo(dados.tipo());
        validar(viagem);

        boolean despesaFora = viagem.getCategorias().stream()
                .flatMap(c -> c.getDespesas().stream())
                .anyMatch(d -> !d.validarData(viagem));
        if (despesaFora) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Há despesas fora do novo período. Ajuste ou exclua essas despesas antes de mudar as datas.");
        }

        viagem.reavaliarBloqueio();
        return viagem;
    }

    @Transactional
    public void excluir(Long viajanteId, Long viagemId) {
        Viagem viagem = buscarDoViajante(viajanteId, viagemId);
        viagem.getViajante().getViagens().remove(viagem);
        viagemRepository.delete(viagem);
    }

    private void validar(Viagem viagem) {
        if (!viagem.validarRegras()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, viagem.listarErros());
        }
    }
}
