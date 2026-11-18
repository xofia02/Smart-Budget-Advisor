package br.ucsal.smartbudget.controller;

import br.ucsal.smartbudget.dto.IaDto;
import br.ucsal.smartbudget.dto.IaDto.ChatGeralResponse;
import br.ucsal.smartbudget.dto.IaDto.ChatResponse;
import br.ucsal.smartbudget.dto.IaDto.DicaResponse;
import br.ucsal.smartbudget.dto.IaDto.MensagemResponse;
import br.ucsal.smartbudget.dto.IaDto.PerguntaRequest;
import br.ucsal.smartbudget.dto.IaDto.PrevisaoResponse;
import br.ucsal.smartbudget.excecao.ApiException;
import br.ucsal.smartbudget.model.MensagemChat;
import br.ucsal.smartbudget.model.Viagem;
import br.ucsal.smartbudget.seguranca.UsuarioAutenticado;
import br.ucsal.smartbudget.service.IaService;
import br.ucsal.smartbudget.service.ServicoOrcamento;
import br.ucsal.smartbudget.service.ViagemService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "5. Inteligência Artificial")
public class IaController {
    private final ViagemService viagemService;
    private final ServicoOrcamento servicoOrcamento;
    private final IaService iaService;

    public IaController(ViagemService viagemService, ServicoOrcamento servicoOrcamento, IaService iaService) {
        this.viagemService = viagemService;
        this.servicoOrcamento = servicoOrcamento;
        this.iaService = iaService;
    }

    @PostMapping("/viagens/{viagemId}/previsao")
    public PrevisaoResponse gerarPrevisao(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                          @PathVariable Long viagemId) {
        Viagem viagem = viagemService.buscarDoViajante(usuario.id(), viagemId);
        return PrevisaoResponse.de(servicoOrcamento.gerarPrevisao(viagem));
    }

    @GetMapping("/viagens/{viagemId}/previsao")
    public PrevisaoResponse ultimaPrevisao(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                           @PathVariable Long viagemId) {
        Viagem viagem = viagemService.buscarDoViajante(usuario.id(), viagemId);
        return servicoOrcamento.buscarUltimaPrevisao(viagem)
                .map(PrevisaoResponse::de)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        "Esta viagem ainda não tem previsão. Gere uma previsão primeiro."));
    }

    @PostMapping("/viagens/{viagemId}/dicas")
    public List<DicaResponse> gerarDicas(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                         @PathVariable Long viagemId) {
        Viagem viagem = viagemService.buscarDoViajante(usuario.id(), viagemId);
        return iaService.gerarDicas(viagem).stream().map(DicaResponse::de).toList();
    }

    @GetMapping("/viagens/{viagemId}/dicas")
    public List<DicaResponse> listarDicas(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                          @PathVariable Long viagemId) {
        Viagem viagem = viagemService.buscarDoViajante(usuario.id(), viagemId);
        return iaService.listarDicas(viagem).stream().map(DicaResponse::de).toList();
    }

    @GetMapping("/viagens/{viagemId}/chat")
    public List<MensagemResponse> historicoChat(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                                @PathVariable Long viagemId) {
        Viagem viagem = viagemService.buscarDoViajante(usuario.id(), viagemId);
        return iaService.historico(viagem).stream().map(MensagemResponse::de).toList();
    }

    @PostMapping("/viagens/{viagemId}/chat")
    public ChatResponse perguntar(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Long viagemId,
                                  @Valid @RequestBody PerguntaRequest dados) {
        Viagem viagem = viagemService.buscarDoViajante(usuario.id(), viagemId);
        List<MensagemChat> mensagens = iaService.perguntar(viagem, dados.pergunta().trim());
        return new ChatResponse(MensagemResponse.de(mensagens.get(0)), MensagemResponse.de(mensagens.get(1)));
    }

    @PostMapping("/chat")
    public ChatGeralResponse perguntarSemViagem(@Valid @RequestBody PerguntaRequest dados) {
        return new ChatGeralResponse(iaService.perguntarSemViagem(dados.pergunta().trim()), IaDto.AVISO_CHAT_GENERICO);
    }
}
