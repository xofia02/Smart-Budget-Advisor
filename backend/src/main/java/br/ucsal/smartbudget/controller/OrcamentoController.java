package br.ucsal.smartbudget.controller;

import br.ucsal.smartbudget.dto.OrcamentoDto.DistribuicaoRequest;
import br.ucsal.smartbudget.dto.OrcamentoDto.RelatorioResponse;
import br.ucsal.smartbudget.dto.ViagemDto.ViagemDetalheResponse;
import br.ucsal.smartbudget.model.Viagem;
import br.ucsal.smartbudget.seguranca.UsuarioAutenticado;
import br.ucsal.smartbudget.service.ServicoOrcamento;
import br.ucsal.smartbudget.service.ViagemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/viagens/{viagemId}")
@Tag(name = "6. Orçamento e relatório")
public class OrcamentoController {
    private final ViagemService viagemService;
    private final ServicoOrcamento servicoOrcamento;

    public OrcamentoController(ViagemService viagemService, ServicoOrcamento servicoOrcamento) {
        this.viagemService = viagemService;
        this.servicoOrcamento = servicoOrcamento;
    }

    @PostMapping("/orcamento/distribuir")
    @Operation(summary = "Divide o orçamento entre as categorias com base na última previsão")
    public ViagemDetalheResponse distribuir(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                            @PathVariable Long viagemId) {
        Viagem viagem = viagemService.buscarDoViajante(usuario.id(), viagemId);
        servicoOrcamento.distribuirOrcamento(viagem);
        return ViagemDetalheResponse.de(viagem);
    }

    @PutMapping("/orcamento")
    @Operation(summary = "Ajusta a divisão manualmente (a soma não pode passar do total)")
    public ViagemDetalheResponse ajustar(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                         @PathVariable Long viagemId, @Valid @RequestBody DistribuicaoRequest dados) {
        Viagem viagem = viagemService.buscarDoViajante(usuario.id(), viagemId);
        servicoOrcamento.salvarDistribuicao(viagem, dados.categorias());
        return ViagemDetalheResponse.de(viagem);
    }

    @GetMapping("/relatorio")
    @Operation(summary = "Relatório planejado x gasto")
    public RelatorioResponse relatorio(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                       @PathVariable Long viagemId) {
        Viagem viagem = viagemService.buscarDoViajante(usuario.id(), viagemId);
        return RelatorioResponse.de(servicoOrcamento.gerarRelatorio(viagem));
    }
}
