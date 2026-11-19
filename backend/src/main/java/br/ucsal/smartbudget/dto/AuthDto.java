package br.ucsal.smartbudget.dto;

import br.ucsal.smartbudget.model.Usuario;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDto {
    private AuthDto() {
    }

    public record CadastroRequest(
            @NotBlank(message = "Informe o nome.")
            @Size(max = 120, message = "O nome pode ter no máximo 120 caracteres.")
            String nome,

            @NotBlank(message = "Informe o e-mail.")
            @Email(message = "Informe um e-mail válido.")
            String email,

            @NotBlank(message = "Informe a senha.")
            @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres.")
            String senha,

            @AssertTrue(message = "É preciso aceitar os termos de uso de dados (LGPD) para criar a conta.")
            boolean consentimentoLGPD
    ) {
    }

    public record LoginRequest(
            @NotBlank(message = "Informe o e-mail.")
            @Email(message = "Informe um e-mail válido.")
            String email,

            @NotBlank(message = "Informe a senha.")
            String senha
    ) {
    }

    public record UsuarioResponse(Long id, String nome, String email, String tipo) {
        public static UsuarioResponse de(Usuario u) {
            return new UsuarioResponse(u.getId(), u.getNome(), u.getEmail(), u.tipo());
        }
    }

    public record LoginResponse(String token, long expiraEmMinutos, UsuarioResponse usuario) {
    }
}
