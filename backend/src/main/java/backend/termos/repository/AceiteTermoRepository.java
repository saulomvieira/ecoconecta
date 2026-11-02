package backend.termos.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import backend.termos.model.AceiteTermo;

public interface AceiteTermoRepository extends JpaRepository<AceiteTermo,Long> {
    boolean existsByUsuarioIdAndTermoId(Long usuarioId, Long termoId);
}
