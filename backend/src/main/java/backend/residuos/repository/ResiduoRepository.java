
package backend.residuos.repository;

import backend.residuos.model.Residuo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import java.util.Optional;

import java.util.List;

public interface ResiduoRepository extends JpaRepository<Residuo, Long> {

    // Listar resíduos publicados por um usuário
    List<Residuo> findByUsuarioId(Long usuarioId);

    // Listar resíduos de uma região
    List<Residuo> findByRegiaoIgnoreCase(String regiao);

    // Priorizar os resíduos mais recentes
    List<Residuo> findAllByOrderByIdDesc();


    // Priorizar resíduos de uma região e exibir os demais depois
    @Query("""
            SELECT r FROM Residuo r
            ORDER BY
                CASE
                    WHEN LOWER(r.regiao) = LOWER(:regiao) THEN 0
                    ELSE 1
                END,
                r.id DESC
            """)
    List<Residuo> findAllPriorizandoRegiao(
            @Param("regiao") String regiao
    );

    @Query(value = """
            SELECT
                TO_CHAR(data_cadastro, 'YYYY-MM') AS mes,
                COUNT(*) AS total
            FROM residuos
            WHERE data_cadastro IS NOT NULL
            GROUP BY TO_CHAR(data_cadastro, 'YYYY-MM')
            ORDER BY mes
            """, nativeQuery = true)
    List<Object[]> contarResiduosPorMes();

    List<Residuo> findTop10ByDataCadastroIsNotNullOrderByDataCadastroDesc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Residuo r WHERE r.id = :id")
    Optional<Residuo> buscarPorIdComBloqueio(@Param("id") Long id);

}
