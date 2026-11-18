package br.ucsal.smartbudget.dto;

import br.ucsal.smartbudget.model.Usuario;

import java.time.LocalDateTime;

public final class AdminDto {
    private AdminDto() {
    }

    public record UsuarioAdminResponse(Long id, String nome, String email, String tipo, LocalDateTime criadoEm) {
        public static UsuarioAdminResponse de(Usuario u) {
            return new UsuarioAdminResponse(u.getId(), u.getNome(), u.getEmail(), u.tipo(), u.getCriadoEm());
        }
    }

    public record IndicadoresResponse(long totalUsuarios, long totalViajantes, long totalViagens,
                                      long viagensEmAndamento, long previsoesGeradas, long mensagensNoChat) {
    }
}
