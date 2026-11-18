package br.ucsal.smartbudget.controller;

import br.ucsal.smartbudget.dto.DespesaDto.AlertaResponse;
import br.ucsal.smartbudget.dto.DespesaDto.DespesaRequest;
import br.ucsal.smartbudget.dto.DespesaDto.DespesaResponse;
import br.ucsal.smartbudget.model.Viagem;
import br.ucsal.smartbudget.model.enums.CategoriaGasto;
import br.ucsal.smartbudget.seguranca.UsuarioAutenticado;
import br.ucsal.smartbudget.service.DespesaService;
import br.ucsal.smartbudget.service.ViagemService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/viagens/{viagemId}")
@Tag(name = "4. Despesas e alertas")
public class DespesaController {
    private final DespesaService despesaService;
    private final ViagemService viagemService;

    public DespesaController(DespesaService despesaService, ViagemService viagemService) {
        this.despesaService = despesaService;
        this.viagemService = viagemService;
    }

    @GetMapping("/despesas")
    public List<DespesaResponse> listar(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                        @PathVariable Long viagemId,
                                        @RequestParam(required = false) CategoriaGasto categoria,
                                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
                                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        Viagem viagem = viagemService.buscarDoViajante(usuario.id(), viagemId);
        return despesaService.listar(viagem, categoria, inicio, fim).stream().map(DespesaResponse::de).toList();
    }

    @PostMapping("/despesas")
    @ResponseStatus(HttpStatus.CREATED)
    public DespesaResponse criar(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Long viagemId,
                                 @Valid @RequestBody DespesaRequest dados) {
        return despesaService.criar(viagemService.buscarDoViajante(usuario.id(), viagemId), dados);
    }

    @PutMapping("/despesas/{despesaId}")
    public DespesaResponse atualizar(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Long viagemId,
                                     @PathVariable Long despesaId, @Valid @RequestBody DespesaRequest dados) {
        return despesaService.atualizar(viagemService.buscarDoViajante(usuario.id(), viagemId), despesaId, dados);
    }

    @DeleteMapping("/despesas/{despesaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Long viagemId,
                        @PathVariable Long despesaId) {
        despesaService.excluir(viagemService.buscarDoViajante(usuario.id(), viagemId), despesaId);
    }

    @GetMapping("/alertas")
    public List<AlertaResponse> alertas(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                        @PathVariable Long viagemId) {
        Viagem viagem = viagemService.buscarDoViajante(usuario.id(), viagemId);
        return despesaService.listarAlertas(viagem).stream().map(AlertaResponse::de).toList();
    }
}
