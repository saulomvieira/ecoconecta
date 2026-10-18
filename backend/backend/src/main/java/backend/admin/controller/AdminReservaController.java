package backend.admin.controller;
import backend.admin.dto.ReservaAdminResponse;
import backend.reservas.model.Reserva;
import backend.reservas.model.Reserva.StatusReserva;
import backend.reservas.service.ReservaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.Comparator;
import java.util.List;
@RestController
@RequestMapping("/admin/reservas")
public class AdminReservaController {
    private final ReservaService service;
    public AdminReservaController(ReservaService service) { this.service = service; }
    @GetMapping
    public List<ReservaAdminResponse> listar(@RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) Long residuoId,
            @RequestParam(required = false) StatusReserva status) {
        return service.listar().stream()
                .filter(r -> usuarioId == null || usuarioId.equals(r.getUsuario().getId()))
                .filter(r -> residuoId == null || residuoId.equals(r.getResiduo().getId()))
                .filter(r -> status == null || status == r.getStatus())
                .sorted(Comparator.comparing(Reserva::getDataReserva,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .map(ReservaAdminResponse::from).toList();
    }
    @GetMapping("/{id}")
    public ReservaAdminResponse detalhar(@PathVariable Long id) {
        return ReservaAdminResponse.from(service.buscarPorId(id));
    }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservaAdminResponse cadastrar(@Valid @RequestBody NovaReservaRequest dados) {
        return ReservaAdminResponse.from(service.cadastrar(dados.usuarioId(), dados.residuoId()));
    }
    @PatchMapping("/{id}/status")
    public ReservaAdminResponse alterarStatus(@PathVariable Long id,
            @Valid @RequestBody AlterarStatusRequest dados) {
        return ReservaAdminResponse.from(service.atualizar(id, dados.status()));
    }
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelar(@PathVariable Long id) { service.excluir(id); }
    public record NovaReservaRequest(@NotNull Long usuarioId, @NotNull Long residuoId) {}
    public record AlterarStatusRequest(@NotNull StatusReserva status) {}
}
