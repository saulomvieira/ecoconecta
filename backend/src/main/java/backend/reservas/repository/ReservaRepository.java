
package backend.reservas.repository;

import backend.reservas.model.Reserva;
import backend.reservas.model.Reserva.StatusReserva;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;

import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    boolean existsByUsuarioIdAndResiduoIdAndStatus(
            Long usuarioId,
            Long residuoId,
            StatusReserva status
    );

    boolean existsByResiduoIdAndStatusIn(
            Long residuoId,
            Collection<StatusReserva> status
    );

    long countByUsuarioIdAndStatusInAndDataReservaGreaterThanEqualAndDataReservaLessThan(
            Long usuarioId,
            Collection<StatusReserva> status,
            LocalDateTime inicio,
            LocalDateTime fim
    );

    // Verificar se o resíduo possui uma reserva com determinado status
    boolean existsByResiduoIdAndStatus(
            Long residuoId,
            StatusReserva status
    );

    List<Reserva> findByStatusIn(Collection<StatusReserva> status);

    List<Reserva> findByResiduoIdInAndStatusIn(
            Collection<Long> residuoIds,
            Collection<StatusReserva> status
    );

    // Listar reservas realizadas por um usuário
    List<Reserva> findByUsuarioIdOrderByDataReservaDesc(Long usuarioId);

    // Verificar se um resíduo possui qualquer reserva associada
    boolean existsByResiduoId(Long residuoId);

    // Contar reservas por status
    long countByStatus(StatusReserva status);
}
