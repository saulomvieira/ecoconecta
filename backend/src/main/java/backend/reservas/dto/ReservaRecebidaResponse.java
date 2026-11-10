package backend.reservas.dto;

import backend.reservas.model.Reserva;
import backend.reservas.model.Reserva.StatusReserva;

import java.time.LocalDateTime;

// Visão do dono do resíduo sobre uma reserva feita por outra pessoa.
// O telefone só é exposto aqui: é o contato para combinar a coleta.
public record ReservaRecebidaResponse(
        Long id,
        StatusReserva status,
        LocalDateTime dataReserva,
        LocalDateTime dataExpiracao,
        Long residuoId,
        String reservadoPor,
        String telefone
) {
    public static ReservaRecebidaResponse de(Reserva reserva) {
        return new ReservaRecebidaResponse(
                reserva.getId(),
                reserva.getStatus(),
                reserva.getDataReserva(),
                reserva.getDataExpiracao(),
                reserva.getResiduo().getId(),
                reserva.getUsuario().getNome(),
                reserva.getUsuario().getTelefone()
        );
    }
}
