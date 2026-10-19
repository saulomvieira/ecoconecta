package backend.admin.dto;

import backend.assinaturas.model.Assinatura;
import backend.assinaturas.model.StatusAssinatura;
import java.time.LocalDate;

public record AssinaturaAdminResponse(
        Long id, Long usuarioId, String usuarioNome, String usuarioEmail,
        Long planoId, String planoNome, StatusAssinatura status,
        LocalDate dataInicio, LocalDate dataFim) {

    public static AssinaturaAdminResponse from(Assinatura assinatura) {
        StatusAssinatura status = assinatura.getStatus();
        if (status == StatusAssinatura.ATIVA
                && assinatura.getDataFim() != null
                && !LocalDate.now().isBefore(assinatura.getDataFim())) {
            status = StatusAssinatura.EXPIRADA;
        }
        return new AssinaturaAdminResponse(
                assinatura.getId(),
                assinatura.getUsuario().getId(),
                assinatura.getUsuario().getNome(),
                assinatura.getUsuario().getEmail(),
                assinatura.getPlano().getId(),
                assinatura.getPlano().getNome(),
                status, assinatura.getDataInicio(), assinatura.getDataFim());
    }
}
