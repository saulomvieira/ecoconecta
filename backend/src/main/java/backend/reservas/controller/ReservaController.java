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

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    // Cadastrar reserva
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Reserva cadastrar(
            @Valid @RequestBody NovaReservaRequest request) {

        return reservaService.cadastrar(
                request.usuarioId(),
                request.residuoId()
        );
    }

    // Listar reservas
    @GetMapping
    public List<Reserva> listar() {
        return reservaService.listar();
    }

    // Buscar reserva pelo ID
    @GetMapping("/{id}")
    public Reserva buscarPorId(@PathVariable Long id) {
        return reservaService.buscarPorId(id);
    }

    // Atualizar reserva
    @PutMapping("/{id}")
    public Reserva atualizar(
            @PathVariable Long id,
            @Valid @RequestBody AtualizarReservaRequest request) {

        return reservaService.atualizar(
                id,
                request.status()
        );
    }

    // Cancelar reserva
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        reservaService.excluir(id);
    }

    public record NovaReservaRequest(

            @NotNull(message = "O usuário é obrigatório")
            Long usuarioId,

            @NotNull(message = "O resíduo é obrigatório")
            Long residuoId

    ) {}

    public record AtualizarReservaRequest(

            @NotNull(message = "O status é obrigatório")
            StatusReserva status

    ) {}
}
