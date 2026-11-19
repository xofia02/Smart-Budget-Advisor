package br.ucsal.smartbudget.controller;

import br.ucsal.smartbudget.dto.ViagemDto.ViagemDetalheResponse;
import br.ucsal.smartbudget.dto.ViagemDto.ViagemRequest;
import br.ucsal.smartbudget.dto.ViagemDto.ViagemResumoResponse;
import br.ucsal.smartbudget.seguranca.UsuarioAutenticado;
import br.ucsal.smartbudget.service.ViagemService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/viagens")
@Tag(name = "3. Viagens")
public class ViagemController {
    private final ViagemService viagemService;

    public ViagemController(ViagemService viagemService) {
        this.viagemService = viagemService;
    }

    @GetMapping
    public List<ViagemResumoResponse> listar(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return viagemService.listar(usuario.id()).stream().map(ViagemResumoResponse::de).toList();
    }

    @GetMapping("/{id}")
    public ViagemDetalheResponse detalhar(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Long id) {
        return ViagemDetalheResponse.de(viagemService.buscarDoViajante(usuario.id(), id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ViagemDetalheResponse criar(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                       @Valid @RequestBody ViagemRequest dados) {
        return ViagemDetalheResponse.de(viagemService.criar(usuario.id(), dados));
    }

    @PutMapping("/{id}")
    public ViagemDetalheResponse atualizar(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                           @PathVariable Long id, @Valid @RequestBody ViagemRequest dados) {
        return ViagemDetalheResponse.de(viagemService.atualizar(usuario.id(), id, dados));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Long id) {
        viagemService.excluir(usuario.id(), id);
    }
}
