package backend.planos.repository;

import backend.planos.model.Plano;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanoRepository extends JpaRepository<Plano, Long> {

}
