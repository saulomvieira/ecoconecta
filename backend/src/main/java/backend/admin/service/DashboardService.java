package backend.admin.service;

import backend.admin.dto.DashboardResponse;
import backend.assinaturas.model.StatusAssinatura;
import backend.assinaturas.repository.AssinaturaRepository;
import backend.reservas.model.Reserva.StatusReserva;
import backend.reservas.repository.ReservaRepository;
import backend.residuos.repository.ResiduoRepository;
import backend.usuarios.repository.UsuarioRepository;
import backend.admin.dto.ResiduoMensalResponse;

import java.util.List;

import backend.admin.dto.AtividadeRecenteResponse;
import backend.reservas.model.Reserva;
import backend.residuos.model.Residuo;

import java.util.ArrayList;
import java.util.Comparator;

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

    public List<ResiduoMensalResponse> listarResiduosPorMes() {

        List<Object[]> resultados = residuoRepository.contarResiduosPorMes();

        return resultados.stream()
                .map(linha -> new ResiduoMensalResponse(
                        (String) linha[0],
                        ((Number) linha[1]).longValue()
                ))
                .toList();
    }

    public List<AtividadeRecenteResponse> listarAtividadesRecentes() {

        List<AtividadeRecenteResponse> atividades = new ArrayList<>();

        // Buscar os 10 resíduos mais recentes
        List<Residuo> residuos = residuoRepository
                .findTop10ByDataCadastroIsNotNullOrderByDataCadastroDesc();

        for (Residuo residuo : residuos) {
            atividades.add(new AtividadeRecenteResponse(
                    "RESIDUO_CADASTRADO",
                    "Novo resíduo cadastrado: " + residuo.getNome(),
                    residuo.getDataCadastro()
            ));
        }

        // Buscar as 10 reservas mais recentes
        List<Reserva> reservas = reservaRepository
                .findTop10ByOrderByDataReservaDesc();

        for (Reserva reserva : reservas) {
            atividades.add(new AtividadeRecenteResponse(
                    "RESERVA_REALIZADA",
                    "Nova reserva realizada para o resíduo: "
                            + reserva.getResiduo().getNome(),
                    reserva.getDataReserva()
            ));
        }

        // Ordenar todas as atividades da mais recente para a mais antiga
        atividades.sort(
                Comparator.comparing(
                        AtividadeRecenteResponse::data,
                        Comparator.nullsLast(Comparator.reverseOrder())
                )
        );

        // Retornar somente as 10 atividades mais recentes
        return atividades.stream()
                .limit(10)
                .toList();
    }
}
