package backend.admin.dto;

import backend.residuos.model.Residuo;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ResiduoAdminResponse(
        Long id, String nome, String descricao, String categoria,
        BigDecimal quantidade, String unidadeMedida, String regiao,
        String enderecoColeta, String imagemUrl, Long usuarioId,
        String status, LocalDateTime dataCadastro) {

    public static ResiduoAdminResponse from(Residuo residuo, String status) {
        return new ResiduoAdminResponse(
                residuo.getId(), residuo.getNome(), residuo.getDescricao(),
                residuo.getCategoria(), residuo.getQuantidade(),
                residuo.getUnidadeMedida(), residuo.getRegiao(),
                residuo.getEnderecoColeta(), residuo.getImagemUrl(),
                residuo.getUsuario().getId(), status, residuo.getDataCadastro());
    }
}
