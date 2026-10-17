package backend.residuos.dto;

import backend.residuos.model.Residuo;

public record ResiduoDisponibilidadeResponse(
        Residuo residuo,
        String status
) {
}
