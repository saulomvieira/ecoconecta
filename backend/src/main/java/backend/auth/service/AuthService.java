package backend.auth.service;

import backend.usuarios.model.Usuario;
import backend.usuarios.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Optional<Usuario> autenticar(String email, String senha) {

        Optional<Usuario> usuario = usuarioRepository.findByEmail(email);

        if (usuario.isEmpty()) {
            return Optional.empty();
        }

        if (!Boolean.TRUE.equals(usuario.get().getAtivo()) || senha == null
                || !passwordEncoder.matches(senha, usuario.get().getSenha())) {
            return Optional.empty();
        }

        return usuario;
    }
}