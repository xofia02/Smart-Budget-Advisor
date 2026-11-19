package br.ucsal.smartbudget.seguranca;

import br.ucsal.smartbudget.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class JwtAuthFilter extends OncePerRequestFilter {
    public static final String CABECALHO_TOKEN_RENOVADO = "X-Token-Renovado";

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public JwtAuthFilter(JwtService jwtService, UsuarioRepository usuarioRepository) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest requisicao, HttpServletResponse resposta, FilterChain cadeia)
            throws ServletException, IOException {
        String cabecalho = requisicao.getHeader("Authorization");
        if (cabecalho != null && cabecalho.startsWith("Bearer ")) {
            jwtService.validar(cabecalho.substring(7))
                    .flatMap(dados -> usuarioRepository.findById(dados.usuarioId())
                            .filter(u -> Objects.equals(u.getVersaoToken(), dados.versao())))
                    .ifPresent(usuario -> {
                        UsuarioAutenticado logado = UsuarioAutenticado.de(usuario);
                        var permissoes = List.of(new SimpleGrantedAuthority("ROLE_" + logado.tipo()));
                        SecurityContextHolder.getContext().setAuthentication(
                                new UsernamePasswordAuthenticationToken(logado, null, permissoes));
                        resposta.setHeader(CABECALHO_TOKEN_RENOVADO, jwtService.gerarToken(usuario));
                    });
        }
        cadeia.doFilter(requisicao, resposta);
    }
}
