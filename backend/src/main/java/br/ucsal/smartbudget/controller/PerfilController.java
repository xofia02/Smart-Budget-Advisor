package br.ucsal.smartbudget.controller;

import br.ucsal.smartbudget.dto.PerfilDto.AtualizarPerfilRequest;
import br.ucsal.smartbudget.dto.PerfilDto.PerfilResponse;
import br.ucsal.smartbudget.dto.PerfilDto.ViagemAnteriorRequest;
import br.ucsal.smartbudget.dto.PerfilDto.ViagemAnteriorResponse;
import br.ucsal.smartbudget.seguranca.UsuarioAutenticado;
import br.ucsal.smartbudget.service.PerfilService;
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
@RequestMapping("/api/perfil")
@Tag(name = "2. Perfil")
public class PerfilController {
    private final PerfilService perfilService;

    public PerfilController(PerfilService perfilService) {
        this.perfilService = perfilService;
    }

    @GetMapping
    public PerfilResponse buscar(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return PerfilResponse.de(perfilService.buscarViajante(usuario.id()));
    }

    @PutMapping
    public PerfilResponse atualizar(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                    @Valid @RequestBody AtualizarPerfilRequest dados) {
        return PerfilResponse.de(perfilService.atualizarPerfil(usuario.id(), dados.estilo()));
    }

    @GetMapping("/viagens-anteriores")
    public List<ViagemAnteriorResponse> listarAnteriores(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return perfilService.listarViagensAnteriores(usuario.id()).stream().map(ViagemAnteriorResponse::de).toList();
    }

    @PostMapping("/viagens-anteriores")
    @ResponseStatus(HttpStatus.CREATED)
    public ViagemAnteriorResponse adicionarAnterior(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                                    @Valid @RequestBody ViagemAnteriorRequest dados) {
        return ViagemAnteriorResponse.de(perfilService.adicionarViagemAnterior(usuario.id(), dados));
    }

    @PutMapping("/viagens-anteriores/{id}")
    public ViagemAnteriorResponse atualizarAnterior(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                                    @PathVariable Long id,
                                                    @Valid @RequestBody ViagemAnteriorRequest dados) {
        return ViagemAnteriorResponse.de(perfilService.atualizarViagemAnterior(usuario.id(), id, dados));
    }

    @DeleteMapping("/viagens-anteriores/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluirAnterior(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Long id) {
        perfilService.excluirViagemAnterior(usuario.id(), id);
    }
}
