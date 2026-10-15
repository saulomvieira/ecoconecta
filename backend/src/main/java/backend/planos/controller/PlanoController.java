package backend.planos.controller;

import backend.planos.model.Plano;
import backend.planos.service.PlanoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/planos")
public class PlanoController {

    private final PlanoService planoService;

    public PlanoController(PlanoService planoService) {
        this.planoService = planoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Plano cadastrar(@Valid @RequestBody Plano plano) {
        return planoService.cadastrar(plano);
    }

    @GetMapping
    public List<Plano> listarTodos() {
        return planoService.listarTodos();
    }

    @GetMapping("/{id}")
    public Plano buscarPorId(@PathVariable Long id) {
        return planoService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public Plano atualizar(@PathVariable Long id,
                           @Valid @RequestBody Plano plano) {
        return planoService.atualizar(id, plano);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        planoService.excluir(id);
    }
}
