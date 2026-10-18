package backend.auth.repository;
import backend.auth.model.RecuperacaoSenha;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface RecuperacaoSenhaRepository extends JpaRepository<RecuperacaoSenha,Long> {
    Optional<RecuperacaoSenha> findByTokenHashAndUtilizadoFalse(String tokenHash);
}
