package br.ucsal.smartbudget.controller;

import br.ucsal.smartbudget.dto.AuthDto.CadastroRequest;
import br.ucsal.smartbudget.dto.AuthDto.LoginRequest;
import br.ucsal.smartbudget.dto.AuthDto.LoginResponse;
import br.ucsal.smartbudget.dto.AuthDto.UsuarioResponse;
import br.ucsal.smartbudget.model.Usuario;
import br.ucsal.smartbudget.seguranca.JwtService;
import br.ucsal.smartbudget.seguranca.UsuarioAutenticado;
import br.ucsal.smartbudget.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "1. Autenticação")
public class AuthController {
    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/auth/cadastro")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria uma conta de viajante")
    public UsuarioResponse cadastrar(@Valid @RequestBody CadastroRequest dados) {
        return UsuarioResponse.de(authService.cadastrar(dados));
    }

    @PostMapping("/auth/login")
    @Operation(summary = "Faz login e devolve o token")
    public LoginResponse login(@Valid @RequestBody LoginRequest dados) {
        Usuario usuario = authService.autenticar(dados);
        return new LoginResponse(jwtService.gerarToken(usuario), jwtService.getExpiracaoMinutos(),
                UsuarioResponse.de(usuario));
    }

    @PostMapping("/auth/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Encerra a sessão")
    public void logout(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        authService.encerrarSessao(usuario.id());
    }

    @DeleteMapping("/usuarios/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Exclui a própria conta e todos os dados (LGPD)")
    public void excluirConta(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        authService.excluirConta(usuario.id());
    }
}
