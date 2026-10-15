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
    public Reserva cadastrar(Long usuarioId, Long residuoId) {

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

        // Verifica se já existe uma reserva ativa
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

        Reserva reserva = new Reserva();

        reserva.setUsuario(usuario);
        reserva.setResiduo(residuo);
        reserva.setStatus(StatusReserva.RESERVADA);
        reserva.setDataReserva(LocalDateTime.now());

        return reservaRepository.save(reserva);
    }

    // Listar todas as reservas
    public List<Reserva> listar() {
        return reservaRepository.findAll();
    }

    // Buscar uma reserva pelo ID
    public Reserva buscarPorId(Long id) {

        return reservaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Reserva não encontrada"
                ));
    }

    // Atualizar o status da reserva
    public Reserva atualizar(Long id, StatusReserva novoStatus) {

        Reserva reserva = buscarPorId(id);

        reserva.setStatus(novoStatus);

        return reservaRepository.save(reserva);
    }

    // Cancelar reserva
    public void excluir(Long id) {

        Reserva reserva = buscarPorId(id);

        reserva.setStatus(StatusReserva.CANCELADA);

        reservaRepository.save(reserva);
    }
}
