package backend.usuarios.controller;
import backend.usuarios.dto.MeuPerfilResponse;
import backend.usuarios.service.MeuPerfilService;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/usuarios/me")
public class MeuPerfilController {
    private final MeuPerfilService service;
    public MeuPerfilController(MeuPerfilService service) {this.service=service;}
    @GetMapping
    public MeuPerfilResponse consultar() {return service.consultar();}
}
