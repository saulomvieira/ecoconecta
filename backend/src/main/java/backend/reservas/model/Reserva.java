package backend.reservas.model;

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

    // Situação da reserva: ativa, coletada ou cancelada
    public enum StatusReserva {
        RESERVADA,
        COLETADA,
        CANCELADA
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Usuário que reservou a coleta
    @ManyToOne(optional = false)
    private Usuario usuario;

    // Id do resíduo reservado (o módulo de resíduos ainda não é uma entidade JPA)
    @Column(nullable = false)
    private Long residuoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusReserva status;

    @Column(nullable = false)
    private LocalDateTime dataReserva;
}
