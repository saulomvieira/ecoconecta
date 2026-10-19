package backend.termos.repository;
import backend.termos.model.AceiteTermo;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AceiteTermoRepository extends JpaRepository<AceiteTermo,Long> {
    boolean existsByUsuarioIdAndTermoId(Long usuarioId, Long termoId);
}
