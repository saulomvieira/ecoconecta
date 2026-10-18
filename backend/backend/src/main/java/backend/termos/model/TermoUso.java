package backend.termos.model;

import backend.usuarios.model.Usuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "termos_uso")
@Getter
@Setter
@NoArgsConstructor
public class TermoUso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String versao;

    @NotBlank(message = "O conteúdo é obrigatório")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String conteudo;

    // Apenas a versão mais recente fica ativa
    @Column(nullable = false)
    private Boolean ativo;

    @Column(nullable = false)
    private LocalDateTime dataAtualizacao;

    @ManyToOne
    @JoinColumn(name = "atualizado_por_id", nullable = false)
    private Usuario atualizadoPor;
}
