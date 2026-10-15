package backend.planos.repository;

import backend.planos.model.Assinatura;
import backend.planos.model.StatusAssinatura;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssinaturaRepository extends JpaRepository<Assinatura, Long> {

    List<Assinatura> findByStatus(StatusAssinatura status);

    boolean existsByUsuarioIdAndStatus(Long usuarioId, StatusAssinatura status);
}
