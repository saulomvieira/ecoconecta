package backend.admin.dto;

import backend.usuarios.model.Perfil;
import backend.usuarios.model.Usuario;

public record UsuarioAdminResponse(Long id, String nome, String email,
                                   String regiao, Perfil perfil, Boolean ativo) {
    public static UsuarioAdminResponse from(Usuario usuario) {
        return new UsuarioAdminResponse(usuario.getId(), usuario.getNome(),
                usuario.getEmail(), usuario.getRegiao(), usuario.getPerfil(), usuario.getAtivo());
    }
}
