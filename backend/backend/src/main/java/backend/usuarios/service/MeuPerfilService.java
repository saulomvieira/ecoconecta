package backend.usuarios.service;
import backend.usuarios.dto.MeuPerfilResponse;
import backend.usuarios.model.Usuario;
import backend.usuarios.repository.UsuarioRepository;
import backend.assinaturas.repository.AssinaturaRepository;
import backend.assinaturas.model.StatusAssinatura;
import backend.reservas.repository.ReservaRepository;
import backend.reservas.model.Reserva.StatusReserva;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
@Service
public class MeuPerfilService {
    private final UsuarioRepository usuarios;
    private final AssinaturaRepository assinaturas;
    private final ReservaRepository reservas;
    public MeuPerfilService(UsuarioRepository usuarios, AssinaturaRepository assinaturas,
            ReservaRepository reservas) {
        this.usuarios=usuarios; this.assinaturas=assinaturas; this.reservas=reservas;
    }
    public MeuPerfilResponse consultar() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Autenticação necessária");
        }
        Long id=Long.valueOf(jwt.getSubject());
        Usuario u=usuarios.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,"Usuário não encontrado"));
        if (!Boolean.TRUE.equals(u.getAtivo())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Conta desativada");
        }
        LocalDate hoje=LocalDate.now();
        LocalDateTime inicioMes=hoje.withDayOfMonth(1).atStartOfDay();
        LocalDateTime fimMes=hoje.withDayOfMonth(1).plusMonths(1).atStartOfDay();
        LocalDateTime inicioSemana=hoje.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay();
        LocalDateTime fimSemana=inicioSemana.plusWeeks(1);
        var statuses=List.of(StatusReserva.RESERVADA,StatusReserva.COLETADA);
        long mes=reservas.countByUsuarioIdAndStatusInAndDataReservaGreaterThanEqualAndDataReservaLessThan(id,statuses,inicioMes,fimMes);
        long semana=reservas.countByUsuarioIdAndStatusInAndDataReservaGreaterThanEqualAndDataReservaLessThan(id,statuses,inicioSemana,fimSemana);
        var assinatura=assinaturas.findByUsuarioIdAndStatus(id,StatusAssinatura.ATIVA)
                .filter(a -> a.getDataFim()!=null && a.getDataFim().isAfter(hoje)).orElse(null);
        if (assinatura==null) return MeuPerfilResponse.semPlano(u,mes,semana);
        var plano=assinatura.getPlano();
        Integer lm=plano.getLimiteColetasMes();
        Integer ls=plano.getLimiteColetasSemana();
        return new MeuPerfilResponse(u.getId(),u.getNome(),u.getEmail(),u.getRegiao(),
                u.getPerfil().name(),u.getAtivo(),plano.getNome(),assinatura.getDataFim(),
                lm,ls,mes,semana,lm==null?null:Math.max(0L,lm-mes),
                ls==null?null:Math.max(0L,ls-semana));
    }
}
