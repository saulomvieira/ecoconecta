
package backend.reservas.service;

import backend.assinaturas.model.Assinatura;
import backend.assinaturas.model.StatusAssinatura;
import backend.assinaturas.repository.AssinaturaRepository;
import backend.planos.model.Plano;
import backend.reservas.model.Reserva;
import backend.reservas.model.Reserva.StatusReserva;
import backend.reservas.repository.ReservaRepository;
import backend.residuos.model.Residuo;
import backend.residuos.repository.ResiduoRepository;
import backend.usuarios.model.Usuario;
import backend.usuarios.repository.UsuarioRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ResiduoRepository residuoRepository;
    private final AssinaturaRepository assinaturaRepository;

    public ReservaService(
            ReservaRepository reservaRepository,
            UsuarioRepository usuarioRepository,
            ResiduoRepository residuoRepository,
            AssinaturaRepository assinaturaRepository) {

        this.reservaRepository = reservaRepository;
        this.usuarioRepository = usuarioRepository;
        this.residuoRepository = residuoRepository;
        this.assinaturaRepository = assinaturaRepository;
    }

    // Cadastrar reserva
    public Reserva cadastrar(Long usuarioId, Long residuoId) {

        if (!ehAdmin() && !usuarioAutenticadoId().equals(usuarioId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Você não pode criar reservas em nome de outro usuário"
            );
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuário não encontrado"
                ));

        Residuo residuo = residuoRepository.findById(residuoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Resíduo não encontrado"
                ));

        // Impede reservar o próprio resíduo
        if (residuo.getUsuario().getId().equals(usuarioId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "O usuário não pode reservar o próprio resíduo"
            );
        }

        // Impede reservas ativas duplicadas
        boolean reservaExistente =
                reservaRepository.existsByUsuarioIdAndResiduoIdAndStatus(
                        usuarioId,
                        residuoId,
                        StatusReserva.RESERVADA
                );

        if (reservaExistente) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Este usuário já possui uma reserva ativa para este resíduo"
            );
        }

        // Impede reservar resíduos já reservados ou coletados
        boolean residuoIndisponivel =
                reservaRepository.existsByResiduoIdAndStatusIn(
                        residuoId,
                        List.of(
                                StatusReserva.RESERVADA,
                                StatusReserva.COLETADA
                        )
                );

        if (residuoIndisponivel) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Este resíduo já está reservado ou foi coletado"
            );
        }

        // Verifica os limites do plano antes de reservar
        verificarLimitesColeta(usuarioId);

        Reserva reserva = new Reserva();
        reserva.setUsuario(usuario);
        reserva.setResiduo(residuo);
        reserva.setStatus(StatusReserva.RESERVADA);
        reserva.setDataReserva(LocalDateTime.now());

        return reservaRepository.save(reserva);
    }

    // Listar todas as reservas - somente ADMIN
    public List<Reserva> listar() {

        if (!ehAdmin()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Acesso permitido apenas para administradores"
            );
        }

        return reservaRepository.findAll();
    }

    // Buscar reserva por ID
    public Reserva buscarPorId(Long id) {

        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Reserva não encontrada"
                ));

        exigirProprietarioOuAdmin(reserva);

        return reserva;
    }

    // Atualizar status da reserva
    public Reserva atualizar(Long id, StatusReserva novoStatus) {

        Reserva reserva = buscarPorId(id);

        if (reserva.getStatus() == StatusReserva.CANCELADA ||
                reserva.getStatus() == StatusReserva.COLETADA) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Não é possível alterar uma reserva cancelada ou coletada"
            );
        }

        reserva.setStatus(novoStatus);

        return reservaRepository.save(reserva);
    }

    // Cancelar reserva
    public void excluir(Long id) {

        Reserva reserva = buscarPorId(id);

        if (reserva.getStatus() == StatusReserva.COLETADA) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Não é possível cancelar uma reserva já coletada"
            );
        }

        if (reserva.getStatus() == StatusReserva.CANCELADA) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Esta reserva já está cancelada"
            );
        }

        reserva.setStatus(StatusReserva.CANCELADA);
        reservaRepository.save(reserva);
    }

    // Verificar limites mensais e semanais do plano
    private void verificarLimitesColeta(Long usuarioId) {

        Assinatura assinatura = assinaturaRepository
                .findByUsuarioIdAndStatus(
                        usuarioId,
                        StatusAssinatura.ATIVA
                )
                .orElse(null);

        // Plano Gratuito por padrão
        Integer limiteMensal = 4;
        Integer limiteSemanal = 1;

        if (assinatura != null) {

            Plano plano = assinatura.getPlano();

            if (!Boolean.TRUE.equals(plano.getAtivo())) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "O plano da assinatura está inativo"
                );
            }

            limiteMensal = plano.getLimiteColetasMes();
            limiteSemanal = plano.getLimiteColetasSemana();
        }

        // Reservas canceladas não entram na contagem
        List<StatusReserva> statusValidos = List.of(
                StatusReserva.RESERVADA,
                StatusReserva.COLETADA
        );

        LocalDate hoje = LocalDate.now();

        // Início e fim do mês atual
        LocalDateTime inicioMes =
                hoje.withDayOfMonth(1).atStartOfDay();

        LocalDateTime fimMes =
                hoje.withDayOfMonth(1)
                        .plusMonths(1)
                        .atStartOfDay();

        // Início e fim da semana atual
        LocalDate inicioSemana = hoje.with(
                TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)
        );

        LocalDateTime inicioSemanaData =
                inicioSemana.atStartOfDay();

        LocalDateTime fimSemanaData =
                inicioSemana.plusWeeks(1).atStartOfDay();

        // Verifica limite mensal
        if (limiteMensal != null) {

            long coletasMes = reservaRepository
                    .countByUsuarioIdAndStatusInAndDataReservaGreaterThanEqualAndDataReservaLessThan(
                            usuarioId,
                            statusValidos,
                            inicioMes,
                            fimMes
                    );

            if (coletasMes >= limiteMensal) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Limite mensal de coletas atingido"
                );
            }
        }

        // Verifica limite semanal
        if (limiteSemanal != null) {

            long coletasSemana = reservaRepository
                    .countByUsuarioIdAndStatusInAndDataReservaGreaterThanEqualAndDataReservaLessThan(
                            usuarioId,
                            statusValidos,
                            inicioSemanaData,
                            fimSemanaData
                    );

            if (coletasSemana >= limiteSemanal) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Limite semanal de coletas atingido"
                );
            }
        }
    }

    // Recuperar JWT do usuário autenticado
    private Jwt obterJwt() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !(authentication.getPrincipal() instanceof Jwt jwt)) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Autenticação necessária"
            );
        }

        return jwt;
    }

    // Recuperar ID do usuário autenticado
    private Long usuarioAutenticadoId() {
        return Long.valueOf(obterJwt().getSubject());
    }

    // Verificar se é administrador
    private boolean ehAdmin() {
        return "ADMIN".equals(
                obterJwt().getClaimAsString("perfil")
        );
    }

    // Verificar proprietário ou administrador
    private void exigirProprietarioOuAdmin(Reserva reserva) {

        if (!ehAdmin() &&
                !reserva.getUsuario().getId()
                        .equals(usuarioAutenticadoId())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Você não tem permissão para acessar esta reserva"
            );
        }
    }
}
