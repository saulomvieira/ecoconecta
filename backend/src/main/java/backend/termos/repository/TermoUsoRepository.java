package backend.termos.repository;

import backend.termos.model.TermoUso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TermoUsoRepository extends JpaRepository<TermoUso, Long> {

    Optional<TermoUso> findFirstByAtivoTrueOrderByDataAtualizacaoDesc();

    List<TermoUso> findAllByOrderByDataAtualizacaoDesc();
}
