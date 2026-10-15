package backend.reservas.repository;

import backend.reservas.model.Reserva;
import backend.reservas.model.Reserva.StatusReserva;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    boolean existsByResiduoIdAndStatusIn(Long residuoId, Collection<StatusReserva> status);

    long countByUsuarioIdAndStatusNotAndDataReservaGreaterThanEqual(Long usuarioId, StatusReserva status, LocalDateTime inicio);

}
