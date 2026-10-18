package backend.admin.controller;

import backend.admin.dto.UsuarioAdminResponse;
import backend.usuarios.service.UsuarioService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import backend.usuarios.model.Perfil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/usuarios")
public class AdminUsuarioController {
    private final UsuarioService usuarioService;

    public AdminUsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public List<UsuarioAdminResponse> buscar(
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) String perfil,
            @RequestParam(required = false) Boolean ativo) {
        return usuarioService.buscarUsuariosAdmin(busca, perfil)
                .stream().filter(u -> ativo == null || ativo.equals(u.getAtivo()))
                .map(UsuarioAdminResponse::from).toList();
    }
    @GetMapping("/{id}")
    public UsuarioAdminResponse detalhar(@PathVariable Long id) {
        return UsuarioAdminResponse.from(usuarioService.buscarPorId(id));
    }

    @PatchMapping("/{id}/perfil")
    public UsuarioAdminResponse alterarPerfil(@PathVariable Long id,
            @Valid @RequestBody AlterarPerfilRequest request) {
        return UsuarioAdminResponse.from(usuarioService.alterarPerfilAdmin(id, request.perfil()));
    }

    @PatchMapping("/{id}/ativo")
    public UsuarioAdminResponse alterarAtivo(@PathVariable Long id,
            @Valid @RequestBody AlterarAtivoRequest request) {
        return UsuarioAdminResponse.from(usuarioService.alterarAtivoAdmin(id, request.ativo()));
    }
    public record AlterarAtivoRequest(@NotNull Boolean ativo) {}

    @PostMapping
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public UsuarioAdminResponse cadastrar(@Valid @RequestBody backend.usuarios.model.Usuario usuario) {
        return UsuarioAdminResponse.from(usuarioService.cadastrar(usuario));
    }

    @PutMapping("/{id}")
    public UsuarioAdminResponse atualizar(@PathVariable Long id,
            @Valid @RequestBody backend.usuarios.model.Usuario usuario) {
        return UsuarioAdminResponse.from(usuarioService.atualizar(id, usuario));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void desativar(@PathVariable Long id) {
        usuarioService.alterarAtivoAdmin(id, false);
    }

    public record AlterarPerfilRequest(@NotNull Perfil perfil) {}
}
