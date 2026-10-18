package backend.usuarios.repository;

import backend.usuarios.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    boolean existsByEmail(String email);

    Optional<Usuario> findByEmail(String email);

    @Query("""
            SELECT u FROM Usuario u
            WHERE (:busca IS NULL OR LOWER(u.nome) LIKE LOWER(CONCAT('%', :busca, '%'))
                   OR LOWER(u.email) LIKE LOWER(CONCAT('%', :busca, '%')))
              AND (:perfil IS NULL OR u.perfil = :perfil)
            ORDER BY u.id DESC
            """)
    List<Usuario> buscarUsuarios(@Param("busca") String busca,
                                @Param("perfil") backend.usuarios.model.Perfil perfil);
}
