package backend.usuarios.controller;
import backend.usuarios.dto.MeuPerfilResponse;
import backend.usuarios.service.MeuPerfilService;
import backend.usuarios.service.UsuarioService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/usuarios/me")
public class MeuPerfilController {
    private final MeuPerfilService service;
    private final UsuarioService usuarioService;
    public MeuPerfilController(MeuPerfilService service, UsuarioService usuarioService) {
        this.service=service;
        this.usuarioService=usuarioService;
    }
    @GetMapping
    public MeuPerfilResponse consultar() {return service.consultar();}

    // Atualizar nome, e-mail, região e telefone do próprio usuário
    @PutMapping
    public MeuPerfilResponse atualizar(@Valid @RequestBody AtualizarPerfilRequest request) {
        usuarioService.atualizarPerfil(request.nome().trim(), request.email().trim(),
                request.regiao(), request.telefone());
        return service.consultar();
    }

    public record AtualizarPerfilRequest(
            @NotBlank(message = "O nome é obrigatório") String nome,
            @NotBlank(message = "O e-mail é obrigatório") @Email(message = "O e-mail deve ser válido") String email,
            String regiao,
            @NotBlank(message = "O telefone é obrigatório") String telefone
    ) {}
}
