package backend.usuarios.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome é obrigatório")
    @Column(nullable = false)
    private String nome;

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "O e-mail deve ser válido")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "A senha é obrigatória")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    private String senha;

    private String regiao;

    // Telefone de contato (só dígitos, DDD + número). Nunca é serializado junto
    // com a entidade, porque o feed de resíduos é público; é exposto apenas por
    // DTOs específicos (perfil do próprio usuário e reservas recebidas).
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(length = 11)
    private String telefone;

    @Transient
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Boolean aceitouTermos;


    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "perfil", nullable = false)
    private Perfil perfil = Perfil.USUARIO;
}