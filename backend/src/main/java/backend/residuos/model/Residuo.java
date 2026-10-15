
package backend.residuos.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import backend.usuarios.model.Usuario;

import java.math.BigDecimal;

@Entity
@Table(name = "residuos")
@Getter
@Setter
@NoArgsConstructor
public class Residuo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome é obrigatório")
    @Column(nullable = false)
    private String nome;

    @NotBlank(message = "A descrição é obrigatória")
    @Column(nullable = false, length = 1000)
    private String descricao;

    @NotBlank(message = "A categoria é obrigatória")
    @Column(nullable = false)
    private String categoria;

    @NotNull(message = "A quantidade é obrigatória")
    @DecimalMin(value = "0.01", message = "A quantidade deve ser maior que zero")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantidade;

    @NotBlank(message = "A unidade de medida é obrigatória")
    @Column(nullable = false)
    private String unidadeMedida;

    private String regiao;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
}
