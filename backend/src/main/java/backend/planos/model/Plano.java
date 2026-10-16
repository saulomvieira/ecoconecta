package backend.planos.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "planos")
@Getter
@Setter
@NoArgsConstructor
public class Plano {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome é obrigatório")
    @Column(nullable = false)
    private String nome;

    @NotNull(message = "O preço é obrigatório")
    @DecimalMin(value = "0.0", message = "O preço não pode ser negativo")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    @Min(value = 0, message = "O limite de coletas não pode ser negativo")
    private Integer limiteColetasMes;

    @Min(value = 0, message = "O limite semanal não pode ser negativo")
    private Integer limiteColetasSemana;

    @NotBlank(message = "Os benefícios são obrigatórios")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String beneficios;

    @NotNull(message = "O status ativo é obrigatório")
    @Column(nullable = false)
    private Boolean ativo;
}
