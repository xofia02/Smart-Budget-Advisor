package br.ucsal.smartbudget.service;

import br.ucsal.smartbudget.model.DicaEconomia;
import br.ucsal.smartbudget.model.MensagemChat;
import br.ucsal.smartbudget.model.Viagem;
import br.ucsal.smartbudget.repository.DicaEconomiaRepository;
import br.ucsal.smartbudget.repository.MensagemChatRepository;
import br.ucsal.smartbudget.service.ia.ServicoIA;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class IaService {
    private final ServicoIA servicoIA;
    private final DicaEconomiaRepository dicaRepository;
    private final MensagemChatRepository mensagemRepository;

    public IaService(ServicoIA servicoIA, DicaEconomiaRepository dicaRepository,
                     MensagemChatRepository mensagemRepository) {
        this.servicoIA = servicoIA;
        this.dicaRepository = dicaRepository;
        this.mensagemRepository = mensagemRepository;
    }

    @Transactional
    public List<DicaEconomia> gerarDicas(Viagem viagem) {
        List<DicaEconomia> novas = servicoIA.gerarDicas(viagem);
        viagem.getDicas().clear();
        for (DicaEconomia dica : novas) {
            dica.setViagem(viagem);
            viagem.getDicas().add(dica);
        }
        return dicaRepository.saveAll(novas);
    }

    public List<DicaEconomia> listarDicas(Viagem viagem) {
        return dicaRepository.findByViagemId(viagem.getId());
    }

    public List<MensagemChat> historico(Viagem viagem) {
        return mensagemRepository.findByViagemIdOrderByEnviadaEmAsc(viagem.getId());
    }

    @Transactional
    public List<MensagemChat> perguntar(Viagem viagem, String pergunta) {
        String resposta = servicoIA.responderChat(pergunta, viagem);
        LocalDateTime agora = LocalDateTime.now();
        MensagemChat msgPergunta = salvarMensagem(viagem, MensagemChat.PAPEL_USUARIO, pergunta, agora);
        MensagemChat msgResposta = salvarMensagem(viagem, MensagemChat.PAPEL_ASSISTENTE, resposta, agora.plusNanos(1_000_000));
        return List.of(msgPergunta, msgResposta);
    }

    public String perguntarSemViagem(String pergunta) {
        return servicoIA.responderChat(pergunta, null);
    }

    private MensagemChat salvarMensagem(Viagem viagem, String papel, String conteudo, LocalDateTime quando) {
        MensagemChat mensagem = new MensagemChat();
        mensagem.setViagem(viagem);
        mensagem.setPapel(papel);
        mensagem.setConteudo(conteudo);
        mensagem.setEnviadaEm(quando);
        viagem.getMensagens().add(mensagem);
        return mensagemRepository.save(mensagem);
    }
}
