
package backend.assinaturas.repository;

import backend.assinaturas.model.Assinatura;
import backend.assinaturas.model.StatusAssinatura;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssinaturaRepository extends JpaRepository<Assinatura, Long> {

    List<Assinatura> findByStatus(StatusAssinatura status);

    boolean existsByUsuarioIdAndStatus(
            Long usuarioId,
            StatusAssinatura status
    );

    boolean existsByUsuarioIdAndStatusAndIdNot(
            Long usuarioId,
            StatusAssinatura status,
            Long assinaturaId
    );

    Optional<Assinatura> findByUsuarioIdAndStatus(
            Long usuarioId,
            StatusAssinatura status
    );

    // Contar assinaturas por status
    long countByStatus(StatusAssinatura status);
}
