package backend.reservas.service;

import backend.reservas.model.Reserva;
import backend.reservas.model.Reserva.StatusReserva;
import backend.reservas.repository.ReservaRepository;
import backend.residuos.model.Residuo;
import backend.residuos.repository.ResiduoRepository;
import backend.usuarios.model.Usuario;
import backend.usuarios.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ResiduoRepository residuoRepository;

    public ReservaService(
            ReservaRepository reservaRepository,
            UsuarioRepository usuarioRepository,
            ResiduoRepository residuoRepository) {

        this.reservaRepository = reservaRepository;
        this.usuarioRepository = usuarioRepository;
        this.residuoRepository = residuoRepository;
    }

    // Cadastrar reserva
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

        // Impede que o usuário reserve o próprio resíduo
        if (residuo.getUsuario().getId().equals(usuarioId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "O usuário não pode reservar o próprio resíduo"
            );
        }

        // Impede que o mesmo usuário tenha reservas ativas duplicadas
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

        // Cria a reserva
        Reserva reserva = new Reserva();

        reserva.setUsuario(usuario);
        reserva.setResiduo(residuo);
        reserva.setStatus(StatusReserva.RESERVADA);
        reserva.setDataReserva(LocalDateTime.now());

        return reservaRepository.save(reserva);
    }

    // Listar todas as reservas
    public List<Reserva> listar() {
        if (!ehAdmin()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Acesso permitido apenas para administradores"
            );
        }

        return reservaRepository.findAll();
    }

    // Buscar uma reserva pelo ID
    public Reserva buscarPorId(Long id) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Reserva não encontrada"
                ));

        exigirProprietarioOuAdmin(reserva);

        return reserva;
    }

    // Atualizar o status da reserva
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

    private Jwt obterJwt() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Autenticação necessária"
            );
        }

        return jwt;
    }

    private Long usuarioAutenticadoId() {
        return Long.valueOf(obterJwt().getSubject());
    }

    private boolean ehAdmin() {
        return "ADMIN".equals(obterJwt().getClaimAsString("perfil"));
    }

    private void exigirProprietarioOuAdmin(Reserva reserva) {
        if (!ehAdmin()
                && !reserva.getUsuario().getId().equals(usuarioAutenticadoId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Você não tem permissão para acessar esta reserva"
            );
        }
    }
}
