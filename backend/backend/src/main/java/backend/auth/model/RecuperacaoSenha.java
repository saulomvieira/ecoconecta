package backend.auth.model;
import backend.usuarios.model.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
@Entity
@Table(name="recuperacoes_senha", indexes=@Index(name="idx_recuperacao_hash",columnList="tokenHash",unique=true))
@Getter @Setter
public class RecuperacaoSenha {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional=false) @JoinColumn(name="usuario_id",nullable=false) private Usuario usuario;
    @Column(nullable=false,unique=true,length=64) private String tokenHash;
    @Column(nullable=false) private LocalDateTime expiraEm;
    @Column(nullable=false) private boolean utilizado;
}
