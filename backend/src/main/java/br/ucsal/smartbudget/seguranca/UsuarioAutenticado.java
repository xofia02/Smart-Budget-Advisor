package br.ucsal.smartbudget.seguranca;

import br.ucsal.smartbudget.model.Usuario;

public record UsuarioAutenticado(Long id, String email, String tipo) {
    public static UsuarioAutenticado de(Usuario usuario) {
        return new UsuarioAutenticado(usuario.getId(), usuario.getEmail(), usuario.tipo());
    }
}
