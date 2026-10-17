package backend.admin.service;

import backend.admin.dto.DashboardResponse;
import backend.assinaturas.model.StatusAssinatura;
import backend.assinaturas.repository.AssinaturaRepository;
import backend.reservas.model.Reserva.StatusReserva;
import backend.reservas.repository.ReservaRepository;
import backend.residuos.repository.ResiduoRepository;
import backend.usuarios.repository.UsuarioRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class DashboardService {

    private final UsuarioRepository usuarioRepository;
    private final ResiduoRepository residuoRepository;
    private final ReservaRepository reservaRepository;
    private final AssinaturaRepository assinaturaRepository;

    public DashboardService(
            UsuarioRepository usuarioRepository,
            ResiduoRepository residuoRepository,
            ReservaRepository reservaRepository,
            AssinaturaRepository assinaturaRepository) {

        this.usuarioRepository = usuarioRepository;
        this.residuoRepository = residuoRepository;
        this.reservaRepository = reservaRepository;
        this.assinaturaRepository = assinaturaRepository;
    }

    public DashboardResponse obterEstatisticas() {

        long totalUsuarios = usuarioRepository.count();

        long totalResiduos = residuoRepository.count();

        long reservasAtivas = reservaRepository.countByStatus(
                StatusReserva.RESERVADA
        );

        // Considera apenas assinaturas ativas e não vencidas
        long assinaturasAtivas = assinaturaRepository
                .findByStatus(StatusAssinatura.ATIVA)
                .stream()
                .filter(assinatura ->
                        assinatura.getDataFim() != null
                                && assinatura.getDataFim().isAfter(LocalDate.now()))
                .count();

        return new DashboardResponse(
                totalUsuarios,
                totalResiduos,
                reservasAtivas,
                assinaturasAtivas
        );
    }
}
