package br.ucsal.smartbudget.controller;

import br.ucsal.smartbudget.repository.UsuarioRepository;
import br.ucsal.smartbudget.service.ia.GeminiClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "0. Status")
public class StatusController {
    private final UsuarioRepository usuarioRepository;
    private final GeminiClient geminiClient;

    public StatusController(UsuarioRepository usuarioRepository, GeminiClient geminiClient) {
        this.usuarioRepository = usuarioRepository;
        this.geminiClient = geminiClient;
    }

    @GetMapping("/status")
    @Operation(summary = "Mostra se a API, o banco e a chave do Gemini estão ok")
    public Map<String, Object> status() {
        String banco;
        try {
            usuarioRepository.count();
            banco = "OK";
        } catch (Exception e) {
            banco = "ERRO";
        }
        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("api", "OK");
        resposta.put("banco", banco);
        resposta.put("iaConfigurada", geminiClient.estaConfigurado());
        resposta.put("modeloIa", geminiClient.getModelo());
        return resposta;
    }

    @GetMapping("/teste/gemini")
    @Operation(summary = "Manda uma pergunta ao Gemini (teste)")
    public Map<String, Object> testarGemini(@RequestParam(defaultValue =
            "Em uma frase, dê uma dica para economizar em uma viagem a Salvador.") String pergunta) {
        long inicio = System.currentTimeMillis();
        String resposta = geminiClient.perguntar(pergunta);
        return Map.of("resposta", resposta, "duracaoMs", System.currentTimeMillis() - inicio);
    }
}
