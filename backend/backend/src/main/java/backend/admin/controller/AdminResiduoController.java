package backend.admin.controller;

import backend.admin.dto.ResiduoAdminResponse;
import backend.residuos.model.Residuo;
import backend.residuos.service.ResiduoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/admin/residuos")
public class AdminResiduoController {
    private final ResiduoService service;

    public AdminResiduoController(ResiduoService service) {
        this.service = service;
    }

    @GetMapping
    public List<ResiduoAdminResponse> buscar(
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String status) {
        return service.buscarResiduosAdmin(busca, categoria, status);
    }

    @GetMapping("/{id}")
    public ResiduoAdminResponse detalhar(@PathVariable Long id) {
        return service.detalharResiduoAdmin(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Residuo cadastrar(@Valid @RequestBody Residuo dados) {
        return service.cadastrar(dados);
    }

    @PutMapping("/{id}")
    public Residuo atualizar(@PathVariable Long id, @Valid @RequestBody Residuo dados) {
        return service.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}
