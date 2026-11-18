package br.ucsal.smartbudget.controller;

import br.ucsal.smartbudget.dto.AdminDto.IndicadoresResponse;
import br.ucsal.smartbudget.dto.AdminDto.UsuarioAdminResponse;
import br.ucsal.smartbudget.service.AdminService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "7. Administrador")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/usuarios")
    public List<UsuarioAdminResponse> listarContas() {
        return adminService.listarContas().stream().map(UsuarioAdminResponse::de).toList();
    }

    @DeleteMapping("/usuarios/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluirConta(@PathVariable Long id) {
        adminService.excluirConta(id);
    }

    @GetMapping("/indicadores")
    public IndicadoresResponse indicadores() {
        return adminService.consultarIndicadoresAnonimos();
    }
}
