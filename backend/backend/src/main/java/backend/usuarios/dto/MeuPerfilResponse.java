package backend.usuarios.dto;
import backend.usuarios.model.Usuario;
import java.time.LocalDate;
public record MeuPerfilResponse(Long id, String nome, String email, String regiao,
        String perfil, Boolean ativo, String plano, LocalDate assinaturaAte,
        Integer limiteMensal, Integer limiteSemanal, long coletasMes, long coletasSemana,
        Long restantesMes, Long restantesSemana) {
    public static MeuPerfilResponse semPlano(Usuario u, long mes, long semana) {
        return new MeuPerfilResponse(u.getId(), u.getNome(), u.getEmail(), u.getRegiao(),
                u.getPerfil().name(), u.getAtivo(), "FREE", null, 4, 1, mes, semana,
                Math.max(0, 4-mes), Math.max(0, 1-semana));
    }
}
