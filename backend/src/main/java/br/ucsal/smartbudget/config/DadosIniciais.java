package br.ucsal.smartbudget.config;

import br.ucsal.smartbudget.model.Administrador;
import br.ucsal.smartbudget.model.ViagemAnterior;
import br.ucsal.smartbudget.model.Viajante;
import br.ucsal.smartbudget.model.enums.EstiloViagem;
import br.ucsal.smartbudget.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class DadosIniciais implements CommandLineRunner {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String emailAdmin;

    @Value("${app.admin.senha}")
    private String senhaAdmin;

    @Value("${app.dados-demo}")
    private boolean criarDemo;

    public DadosIniciais(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!usuarioRepository.existsByEmail(emailAdmin)) {
            Administrador admin = new Administrador();
            admin.setNome("Administrador");
            admin.setEmail(emailAdmin);
            admin.setSenhaHash(passwordEncoder.encode(senhaAdmin));
            admin.setConsentimentoLGPD(true);
            usuarioRepository.save(admin);
        }

        if (criarDemo && !usuarioRepository.existsByEmail("ana@demo.com")) {
            Viajante ana = new Viajante();
            ana.setNome("Ana Souza");
            ana.setEmail("ana@demo.com");
            ana.setSenhaHash(passwordEncoder.encode("demo12345"));
            ana.setConsentimentoLGPD(true);
            ana.setEstilo(EstiloViagem.CONFORTAVEL);
            ana.adicionarViagemAnterior(viagemAnterior("Porto Seguro", "2025-07-10", "2025-07-15", "4200.00"));
            ana.adicionarViagemAnterior(viagemAnterior("Chapada Diamantina", "2025-01-05", "2025-01-09", "3100.00"));
            usuarioRepository.save(ana);
        }
    }

    private ViagemAnterior viagemAnterior(String destino, String inicio, String fim, String valor) {
        ViagemAnterior v = new ViagemAnterior();
        v.setDestino(destino);
        v.setDataInicio(LocalDate.parse(inicio));
        v.setDataFim(LocalDate.parse(fim));
        v.setValorTotal(new BigDecimal(valor));
        return v;
    }
}
