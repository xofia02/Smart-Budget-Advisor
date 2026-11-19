package br.ucsal.smartbudget.service;

import br.ucsal.smartbudget.dto.AuthDto.CadastroRequest;
import br.ucsal.smartbudget.dto.AuthDto.LoginRequest;
import br.ucsal.smartbudget.excecao.ApiException;
import br.ucsal.smartbudget.model.Administrador;
import br.ucsal.smartbudget.model.Usuario;
import br.ucsal.smartbudget.model.Viajante;
import br.ucsal.smartbudget.repository.UsuarioRepository;
import br.ucsal.smartbudget.seguranca.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public Usuario cadastrar(CadastroRequest dados) {
        String email = dados.email().trim().toLowerCase(Locale.ROOT);
        if (usuarioRepository.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "Já existe uma conta com este e-mail.");
        }
        Viajante viajante = new Viajante();
        viajante.setNome(dados.nome().trim());
        viajante.setEmail(email);
        viajante.setSenhaHash(passwordEncoder.encode(dados.senha()));
        viajante.setConsentimentoLGPD(dados.consentimentoLGPD());
        return usuarioRepository.save(viajante);
    }

    public Usuario autenticar(LoginRequest dados) {
        Usuario usuario = usuarioRepository.findByEmail(dados.email().trim().toLowerCase(Locale.ROOT))
                .orElse(null);
        if (usuario == null || !passwordEncoder.matches(dados.senha(), usuario.getSenhaHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "E-mail ou senha incorretos.");
        }
        return usuario;
    }

    public String gerarToken(Usuario usuario) {
        return jwtService.gerarToken(usuario);
    }

    @Transactional
    public void encerrarSessao(Long usuarioId) {
        buscar(usuarioId).invalidarTokens();
    }

    @Transactional
    public void excluirConta(Long usuarioId) {
        Usuario usuario = buscar(usuarioId);
        if (usuario instanceof Administrador) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "A conta de administrador não pode ser excluída por aqui.");
        }
        usuarioRepository.delete(usuario);
    }

    private Usuario buscar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
    }
}
