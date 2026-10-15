package backend.reservas.repository;

import backend.reservas.model.Reserva;
import backend.reservas.model.Reserva.StatusReserva;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    boolean existsByUsuarioIdAndResiduoIdAndStatus(
            Long usuarioId,
            Long residuoId,
            StatusReserva status
    );
}