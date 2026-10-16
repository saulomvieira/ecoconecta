
package backend.usuarios.service;

import backend.usuarios.model.Perfil;
import backend.usuarios.model.Usuario;
import backend.usuarios.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UsuarioService {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final UsuarioRepository repository;

    public UsuarioService(UsuarioRepository repository) {
        this.repository = repository;
    }

    // Cadastro público: ninguém pode escolher ser ADMIN
    public Usuario cadastrar(Usuario usuario) {

        if (repository.existsByEmail(usuario.getEmail())) {
            throw new IllegalArgumentException("Este e-mail já está cadastrado");
        }

        usuario.setId(null);
        usuario.setPerfil(Perfil.USUARIO);
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));

        return repository.save(usuario);
    }

    // Apenas ADMIN (também protegido no SecurityConfig)
    public List<Usuario> listar() {
        exigirAdmin();
        return repository.findAll();
    }

    // Usuário consulta a si mesmo; ADMIN consulta qualquer usuário
    public Usuario buscarPorId(Long id) {
        exigirProprietarioOuAdmin(id);

        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuário não encontrado"
                ));
    }

    // Usuário atualiza a si mesmo; ADMIN atualiza qualquer usuário
    public Usuario atualizar(Long id, Usuario dados) {
        exigirProprietarioOuAdmin(id);

        Usuario usuario = buscarPorId(id);

        if (!usuario.getEmail().equals(dados.getEmail())
                && repository.existsByEmail(dados.getEmail())) {
            throw new IllegalArgumentException("Este e-mail já está cadastrado");
        }

        usuario.setNome(dados.getNome());
        usuario.setEmail(dados.getEmail());
        usuario.setRegiao(dados.getRegiao());

        // Senha e perfil não são alterados por este endpoint.
        return repository.save(usuario);
    }

    // Apenas ADMIN
    public void excluir(Long id) {
        exigirAdmin();

        Usuario usuario = buscarPorId(id);
        repository.delete(usuario);
    }

    private void exigirProprietarioOuAdmin(Long usuarioId) {
        Jwt jwt = obterJwt();

        boolean admin = "ADMIN".equals(jwt.getClaimAsString("perfil"));
        boolean proprietario = jwt.getSubject().equals(usuarioId.toString());

        if (!admin && !proprietario) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Você não tem permissão para acessar este usuário"
            );
        }
    }

    private void exigirAdmin() {
        Jwt jwt = obterJwt();

        if (!"ADMIN".equals(jwt.getClaimAsString("perfil"))) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Acesso permitido apenas para administradores"
            );
        }
    }

    private Jwt obterJwt() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Autenticação necessária"
            );
        }

        return jwt;
    }
}
