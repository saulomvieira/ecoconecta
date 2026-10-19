package backend.admin.dto;
import backend.reservas.model.Reserva;
import backend.reservas.model.Reserva.StatusReserva;
import java.time.LocalDateTime;
public record ReservaAdminResponse(Long id, Long usuarioId, String usuarioNome,
        Long residuoId, String residuoNome, StatusReserva status, LocalDateTime dataReserva) {
    public static ReservaAdminResponse from(Reserva r) {
        return new ReservaAdminResponse(r.getId(), r.getUsuario().getId(),
                r.getUsuario().getNome(), r.getResiduo().getId(), r.getResiduo().getNome(),
                r.getStatus(), r.getDataReserva());
    }
}
