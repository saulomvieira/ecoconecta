package backend.reservas.model;
import backend.residuos.model.Residuo;
import backend.usuarios.model.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservas")
@Getter
@Setter
@NoArgsConstructor
public class Reserva {

    // Prazo da reserva e de cada extensão feita pelo dono do resíduo
    public static final int DIAS_VALIDADE = 7;

    public enum StatusReserva {
        RESERVADA,
        COLETADA,
        CANCELADA,
        // Prazo vencido sem coleta: o resíduo volta a ficar disponível
        EXPIRADA
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(optional = false)
    @JoinColumn(name = "residuo_id", nullable = false)
    private Residuo residuo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusReserva status;

    @Column(name = "data_reserva", nullable = false)
    private LocalDateTime dataReserva;

    // Fim do prazo da reserva (7 dias após a reserva, ampliável pelo dono do resíduo)
    @Column(name = "data_expiracao")
    private LocalDateTime dataExpiracao;
}
