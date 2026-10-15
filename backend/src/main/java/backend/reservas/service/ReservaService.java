package backend.reservas.service;

import backend.reservas.model.Reserva;
import backend.reservas.model.Reserva.StatusReserva;
import backend.reservas.repository.ReservaRepository;
import backend.usuarios.model.Usuario;
import backend.usuarios.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservaService {

    // Plano Gratuito: máximo de coletas por mês (RGN09)
    private static final int LIMITE_COLETAS_MES = 4;

    private final ReservaRepository repository;
    private final UsuarioService usuarioService;

    public ReservaService(ReservaRepository repository, UsuarioService usuarioService) {
        this.repository = repository;
        this.usuarioService = usuarioService;
    }

    // Reservar coleta
    public Reserva reservar(Long usuarioId, Long residuoId) {
        Usuario usuario = usuarioService.buscarPorId(usuarioId);

        // Resíduo já reservado ou coletado não pode ser reservado novamente
        if (repository.existsByResiduoIdAndStatusIn(
                residuoId, List.of(StatusReserva.RESERVADA, StatusReserva.COLETADA))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Resíduo indisponível para reserva");
        }

        // Conta as coletas do mês, ignorando as canceladas
        LocalDateTime inicioDoMes = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        long coletasNoMes = repository.countByUsuarioIdAndStatusNotAndDataReservaGreaterThanEqual(
                usuarioId, StatusReserva.CANCELADA, inicioDoMes);
        if (coletasNoMes >= LIMITE_COLETAS_MES) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Limite de coletas do mês atingido no plano Gratuito");
        }

        Reserva reserva = new Reserva();
        reserva.setUsuario(usuario);
        reserva.setResiduoId(residuoId);
        reserva.setStatus(StatusReserva.RESERVADA);
        reserva.setDataReserva(LocalDateTime.now());

        return repository.save(reserva);
    }

    // Listar reservas
    public List<Reserva> listar() {
        return repository.findAll();
    }

    // Buscar reserva pelo ID
    public Reserva buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Reserva não encontrada"
                ));
    }

    // Atualizar status da reserva (registrar coleta)
    public Reserva atualizarStatus(Long id, StatusReserva novoStatus) {
        Reserva reserva = buscarPorId(id);
        verificarReservada(reserva);

        if (novoStatus != StatusReserva.COLETADA) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A atualização aceita apenas o status COLETADA. Para cancelar, use PATCH /reservas/{id}/cancelar"
            );
        }

        reserva.setStatus(novoStatus);
        return repository.save(reserva);
    }

    // Cancelar reserva (o registro é mantido para histórico)
    public Reserva cancelar(Long id) {
        Reserva reserva = buscarPorId(id);
        verificarReservada(reserva);

        reserva.setStatus(StatusReserva.CANCELADA);
        return repository.save(reserva);
    }

    // Apenas reservas ativas podem ser alteradas
    private void verificarReservada(Reserva reserva) {
        if (reserva.getStatus() != StatusReserva.RESERVADA) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Só é possível alterar reservas com status RESERVADA"
            );
        }
    }
}

