package backend.termos.model;
import backend.usuarios.model.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
@Entity
@Table(name="aceites_termos", uniqueConstraints=@UniqueConstraint(columnNames={"usuario_id","termo_id"}))
@Getter @Setter
public class AceiteTermo {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional=false) @JoinColumn(name="usuario_id",nullable=false)
    private Usuario usuario;
    @ManyToOne(optional=false) @JoinColumn(name="termo_id",nullable=false)
    private TermoUso termo;
    @Column(nullable=false)
    private LocalDateTime dataAceite;
}
