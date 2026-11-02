package backend.termos.model;

import java.time.LocalDateTime;

import backend.usuarios.model.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @Column(nullable = false)
    private Boolean ativo;

    @Column(nullable = false)
    private LocalDateTime dataAtualizacao;

    @ManyToOne
    @JoinColumn(name = "atualizado_por_id", nullable = false)
    private Usuario atualizadoPor;
}
