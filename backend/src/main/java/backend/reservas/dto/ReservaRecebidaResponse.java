package backend.reservas.dto;

import backend.reservas.model.Reserva;
import backend.reservas.model.Reserva.StatusReserva;

import java.time.LocalDateTime;

public record ReservaRecebidaResponse(
        Long id,
        StatusReserva status,
        LocalDateTime dataReserva,
        Long residuoId,
        String reservadoPor
) {
    public static ReservaRecebidaResponse de(Reserva reserva) {
        return new ReservaRecebidaResponse(
                reserva.getId(),
                reserva.getStatus(),
                reserva.getDataReserva(),
                reserva.getResiduo().getId(),
                reserva.getUsuario().getNome()
        );
    }
}
