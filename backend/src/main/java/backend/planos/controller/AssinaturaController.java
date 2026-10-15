package backend.planos.controller;

import backend.planos.model.Assinatura;
import backend.planos.model.StatusAssinatura;
import backend.planos.service.AssinaturaService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/assinaturas")
public class AssinaturaController {

    private final AssinaturaService service;

    public AssinaturaController(AssinaturaService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Assinatura cadastrar(@RequestBody Assinatura assinatura) {
        return service.cadastrar(assinatura);
    }

    @GetMapping
    public List<Assinatura> listar(@RequestParam(required = false) StatusAssinatura status) {
        return service.listar(status);
    }

    @GetMapping("/{id}")
    public Assinatura buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public Assinatura atualizar(@PathVariable Long id, @RequestBody Assinatura assinatura) {
        return service.atualizar(id, assinatura);
    }

    @PatchMapping("/{id}/cancelar")
    public Assinatura cancelar(@PathVariable Long id) {
        return service.cancelar(id);
    }
}
