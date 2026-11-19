package br.ucsal.smartbudget.seguranca;

import br.ucsal.smartbudget.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

@Service
public class JwtService {
    private final SecretKey chave;
    private final long expiracaoMinutos;

    public JwtService(@Value("${jwt.secret}") String segredo,
                      @Value("${jwt.expiracao-minutos}") long expiracaoMinutos) {
        byte[] bytes = segredo.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET precisa ter pelo menos 32 caracteres.");
        }
        this.chave = Keys.hmacShaKeyFor(bytes);
        this.expiracaoMinutos = expiracaoMinutos;
    }

    public long getExpiracaoMinutos() {
        return expiracaoMinutos;
    }

    public String gerarToken(Usuario usuario) {
        Instant agora = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(usuario.getId()))
                .claim("tipo", usuario.tipo())
                .claim("ver", usuario.getVersaoToken())
                .issuedAt(Date.from(agora))
                .expiration(Date.from(agora.plus(expiracaoMinutos, ChronoUnit.MINUTES)))
                .signWith(chave)
                .compact();
    }

    public Optional<DadosToken> validar(String token) {
        try {
            Claims dados = Jwts.parser().verifyWith(chave).build().parseSignedClaims(token).getPayload();
            return Optional.of(new DadosToken(Long.valueOf(dados.getSubject()), dados.get("ver", Integer.class)));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public record DadosToken(Long usuarioId, Integer versao) {
    }
}
