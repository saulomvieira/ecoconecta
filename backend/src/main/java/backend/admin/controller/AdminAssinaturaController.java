package backend.admin.controller;

import backend.admin.dto.AssinaturaAdminResponse;
import backend.assinaturas.model.Assinatura;
import backend.assinaturas.model.StatusAssinatura;
import backend.assinaturas.service.AssinaturaService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/admin/assinaturas")
public class AdminAssinaturaController {
    private final AssinaturaService service;

    public AdminAssinaturaController(AssinaturaService service) {
        this.service = service;
    }

    @GetMapping
    public List<AssinaturaAdminResponse> listar(
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) Long planoId,
            @RequestParam(required = false) StatusAssinatura status) {
        return service.listar(null).stream()
                .map(AssinaturaAdminResponse::from)
                .filter(a -> usuarioId == null || usuarioId.equals(a.usuarioId()))
                .filter(a -> planoId == null || planoId.equals(a.planoId()))
                .filter(a -> status == null || status == a.status())
                .toList();
    }

    @GetMapping("/{id}")
    public AssinaturaAdminResponse detalhar(@PathVariable Long id) {
        return AssinaturaAdminResponse.from(service.buscarPorId(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AssinaturaAdminResponse cadastrar(@RequestBody Assinatura dados) {
        return AssinaturaAdminResponse.from(service.cadastrar(dados));
    }

    @PutMapping("/{id}")
    public AssinaturaAdminResponse atualizar(@PathVariable Long id,
                                              @RequestBody Assinatura dados) {
        return AssinaturaAdminResponse.from(service.atualizar(id, dados));
    }

    @PatchMapping("/{id}/cancelar")
    public AssinaturaAdminResponse cancelar(@PathVariable Long id) {
        return AssinaturaAdminResponse.from(service.cancelar(id));
    }

    @PatchMapping("/{id}/renovar")
    public AssinaturaAdminResponse renovar(@PathVariable Long id) {
        return AssinaturaAdminResponse.from(service.renovar(id));
    }
}
