package br.ucsal.smartbudget.service;

import br.ucsal.smartbudget.dto.AdminDto.IndicadoresResponse;
import br.ucsal.smartbudget.excecao.ApiException;
import br.ucsal.smartbudget.model.Administrador;
import br.ucsal.smartbudget.model.Usuario;
import br.ucsal.smartbudget.repository.MensagemChatRepository;
import br.ucsal.smartbudget.repository.PrevisaoCustoRepository;
import br.ucsal.smartbudget.repository.UsuarioRepository;
import br.ucsal.smartbudget.repository.ViagemRepository;
import br.ucsal.smartbudget.repository.ViajanteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class AdminService {
    private final UsuarioRepository usuarioRepository;
    private final ViajanteRepository viajanteRepository;
    private final ViagemRepository viagemRepository;
    private final PrevisaoCustoRepository previsaoRepository;
    private final MensagemChatRepository mensagemRepository;

    public AdminService(UsuarioRepository usuarioRepository, ViajanteRepository viajanteRepository,
                        ViagemRepository viagemRepository, PrevisaoCustoRepository previsaoRepository,
                        MensagemChatRepository mensagemRepository) {
        this.usuarioRepository = usuarioRepository;
        this.viajanteRepository = viajanteRepository;
        this.viagemRepository = viagemRepository;
        this.previsaoRepository = previsaoRepository;
        this.mensagemRepository = mensagemRepository;
    }

    public List<Usuario> listarContas() {
        return usuarioRepository.findAllByOrderByIdAsc();
    }

    @Transactional
    public void excluirConta(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
        if (usuario instanceof Administrador) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Contas de administrador não podem ser excluídas por aqui.");
        }
        usuarioRepository.delete(usuario);
    }

    public IndicadoresResponse consultarIndicadoresAnonimos() {
        LocalDate hoje = LocalDate.now();
        return new IndicadoresResponse(
                usuarioRepository.count(),
                viajanteRepository.count(),
                viagemRepository.count(),
                viagemRepository.countByDataInicioLessThanEqualAndDataFimGreaterThanEqual(hoje, hoje),
                previsaoRepository.count(),
                mensagemRepository.count());
    }
}
