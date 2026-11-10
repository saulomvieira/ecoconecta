
package backend.reservas.repository;

import backend.reservas.model.Reserva;
import backend.reservas.model.Reserva.StatusReserva;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

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

    // Listar reservas recebidas pelo dono dos resíduos
    List<Reserva> findByResiduoUsuarioIdAndStatusOrderByDataReservaDesc(
            Long usuarioId,
            StatusReserva status
    );

    // Verificar se um resíduo possui qualquer reserva associada
    boolean existsByResiduoId(Long residuoId);

    // Contar reservas por status
    long countByStatus(StatusReserva status);

    List<Reserva> findTop10ByOrderByDataReservaDesc();

    // Busca a reserva travando a linha, para que cancelar, coletar, estender e
    // expirar não se sobreponham
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Reserva r WHERE r.id = :id")
    Optional<Reserva> buscarPorIdComBloqueio(@Param("id") Long id);

    // Expira, em uma única instrução, as reservas ativas com prazo vencido
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE Reserva r
            SET r.status = :expirada
            WHERE r.status = :ativa AND r.dataExpiracao <= :agora
            """)
    int expirarVencidas(
            @Param("ativa") StatusReserva ativa,
            @Param("expirada") StatusReserva expirada,
            @Param("agora") LocalDateTime agora
    );
}
