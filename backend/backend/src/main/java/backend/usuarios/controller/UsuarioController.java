
package backend.usuarios.controller;

import backend.usuarios.model.Usuario;
import backend.usuarios.service.UsuarioService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    // Cadastrar usuário
    @PostMapping
    public Usuario cadastrar(@Valid @RequestBody Usuario usuario) {
        return service.cadastrar(usuario);
    }

    // Listar usuários
    @GetMapping
    public List<Usuario> listar() {
        return service.listar();
    }

    // Buscar usuário por ID
    @GetMapping("/{id}")
    public Usuario buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    // Atualizar dados do usuário
    @PutMapping("/{id}")
    public Usuario atualizar(
            @PathVariable Long id,
            @Valid @RequestBody Usuario usuario) {

        return service.atualizar(id, usuario);
    }

    // Excluir usuário
    @DeleteMapping("/{id}")
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }

    // Alterar a senha do próprio usuário
    @PatchMapping("/{id}/senha")
    public ResponseEntity<Void> alterarSenha(
            @PathVariable Long id,
            @Valid @RequestBody AlterarSenhaRequest request) {

        service.alterarSenha(
                id,
                request.senhaAtual(),
                request.novaSenha()
        );

        return ResponseEntity.noContent().build();
    }

    // Dados necessários para alterar a senha
    public record AlterarSenhaRequest(
            String senhaAtual,
            String novaSenha
    ) {
    }
}
