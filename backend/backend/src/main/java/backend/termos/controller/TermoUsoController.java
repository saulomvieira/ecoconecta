package backend.termos.controller;

import backend.termos.model.TermoUso;
import backend.termos.service.TermoUsoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/termos")
public class TermoUsoController {

    private final TermoUsoService service;

    public TermoUsoController(TermoUsoService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TermoUso publicar(@Valid @RequestBody TermoUso termo) {
        return service.publicar(termo);
    }

    @GetMapping("/atual")
    public TermoUso buscarAtual() {
        return service.buscarAtual();
    }

    @GetMapping
    public List<TermoUso> listarHistorico() {
        return service.listarHistorico();
    }

    @GetMapping("/{id}")
    public TermoUso buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }
}
