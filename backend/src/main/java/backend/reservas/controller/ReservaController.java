package backend.reservas.controller;

import backend.reservas.model.Reserva;
import backend.reservas.model.Reserva.StatusReserva;
import backend.reservas.service.ReservaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reservas")
public class ReservaController {

    private final ReservaService service;

    public ReservaController(ReservaService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Reserva reservar(@Valid @RequestBody NovaReservaRequest request) {
        return service.reservar(request.usuarioId(), request.residuoId());
    }

    @GetMapping
    public List<Reserva> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public Reserva buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public Reserva atualizar(@PathVariable Long id, @Valid @RequestBody AtualizarReservaRequest request) {
        return service.atualizarStatus(id, request.status());
    }

    @PatchMapping("/{id}/cancelar")
    public Reserva cancelar(@PathVariable Long id) {
        return service.cancelar(id);
    }

    // Dados recebidos para criar uma reserva
    public record NovaReservaRequest(
            @NotNull(message = "O usuário é obrigatório") Long usuarioId,
            @NotNull(message = "O resíduo é obrigatório") Long residuoId
    ) {}

    // Dados recebidos para atualizar uma reserva
    public record AtualizarReservaRequest(
            @NotNull(message = "O status é obrigatório") StatusReserva status
    ) {}
}
