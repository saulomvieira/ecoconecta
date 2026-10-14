
package backend.residuos.controller;

import backend.residuos.model.Residuo;
import backend.residuos.service.ResiduoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/residuos")
public class ResiduoController {

    private final ResiduoService service;

    public ResiduoController(ResiduoService service) {
        this.service = service;
    }

    @PostMapping
    public Residuo cadastrar(@Valid @RequestBody Residuo residuo) {
        return service.cadastrar(residuo);
    }

    @GetMapping
    public List<Residuo> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public Residuo buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public Residuo atualizar(
            @PathVariable Long id,
            @Valid @RequestBody Residuo residuo) {
        return service.atualizar(id, residuo);
    }

    @DeleteMapping("/{id}")
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}
