package backend.admin.dto;

import java.time.LocalDateTime;

public record AtividadeRecenteResponse(
        String tipo,
        String descricao,
        LocalDateTime data
) {
}