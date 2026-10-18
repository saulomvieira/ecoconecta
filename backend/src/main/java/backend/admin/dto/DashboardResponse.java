package backend.admin.dto;

public record DashboardResponse(
        long totalUsuarios,
        long totalResiduos,
        long reservasAtivas,
        long assinaturasAtivas
) {
}
