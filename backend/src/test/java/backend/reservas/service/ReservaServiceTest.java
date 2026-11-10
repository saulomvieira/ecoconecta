package backend.reservas.service;

import backend.assinaturas.repository.AssinaturaRepository;
import backend.reservas.dto.ReservaRecebidaResponse;
import backend.reservas.model.Reserva;
import backend.reservas.model.Reserva.StatusReserva;
import backend.reservas.repository.ReservaRepository;
import backend.residuos.model.Residuo;
import backend.residuos.repository.ResiduoRepository;
import backend.usuarios.model.Usuario;
import backend.usuarios.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReservaServiceTest {

    private static final long DONO_ID = 1L;
    private static final long RESERVANTE_ID = 2L;
    private static final long RESIDUO_ID = 10L;
    private static final long RESERVA_ID = 100L;

    private ReservaRepository reservaRepository;
    private UsuarioRepository usuarioRepository;
    private ResiduoRepository residuoRepository;
    private ReservaService service;

    private Usuario dono;
    private Usuario reservante;
    private Residuo residuo;

    @BeforeEach
    void preparar() {
        reservaRepository = mock(ReservaRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        residuoRepository = mock(ResiduoRepository.class);
        service = new ReservaService(
                reservaRepository, usuarioRepository, residuoRepository,
                mock(AssinaturaRepository.class));

        dono = usuario(DONO_ID, "Maria Dona", "85988887777");
        reservante = usuario(RESERVANTE_ID, "João Reserva", "85999990000");

        residuo = new Residuo();
        residuo.setId(RESIDUO_ID);
        residuo.setUsuario(dono);

        when(reservaRepository.save(any(Reserva.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));
    }

    @AfterEach
    void limpar() {
        SecurityContextHolder.clearContext();
    }

    // ---------- reservar ----------

    @Test
    void reservaNovaVenceEmSeteDiasAPartirDaReserva() {
        autenticarComo(RESERVANTE_ID, "USUARIO");
        liberarResiduoParaReserva();

        Reserva reserva = service.cadastrar(RESERVANTE_ID, RESIDUO_ID);

        assertThat(reserva.getStatus()).isEqualTo(StatusReserva.RESERVADA);
        assertThat(Duration.between(reserva.getDataReserva(), reserva.getDataExpiracao()))
                .isEqualTo(Duration.ofDays(7));
    }

    @Test
    void reservaExpiraAsReservasVencidasAntesDeValidar() {
        autenticarComo(RESERVANTE_ID, "USUARIO");
        liberarResiduoParaReserva();

        service.cadastrar(RESERVANTE_ID, RESIDUO_ID);

        verify(reservaRepository).expirarVencidas(
                eq(StatusReserva.RESERVADA),
                eq(StatusReserva.EXPIRADA),
                any(LocalDateTime.class));
    }

    @Test
    void naoReservaSemTelefoneCadastrado() {
        autenticarComo(RESERVANTE_ID, "USUARIO");
        reservante.setTelefone(null);
        liberarResiduoParaReserva();

        assertThatThrownBy(() -> service.cadastrar(RESERVANTE_ID, RESIDUO_ID))
                .isInstanceOfSatisfying(ResponseStatusException.class, e -> {
                    assertThat(e.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(e.getReason()).contains("telefone");
                });
    }

    // ---------- estender ----------

    @Test
    void donoDoResiduoEstendeOPrazoEmSeteDias() {
        autenticarComo(DONO_ID, "USUARIO");
        LocalDateTime prazoAtual = LocalDateTime.now().plusDays(2);
        Reserva reserva = reservaAtiva(prazoAtual);

        ReservaRecebidaResponse resposta = service.estender(RESERVA_ID);

        assertThat(resposta.dataExpiracao()).isEqualTo(prazoAtual.plusDays(7));
        assertThat(reserva.getDataExpiracao()).isEqualTo(prazoAtual.plusDays(7));
    }

    @Test
    void naoHaLimiteDeExtensoes() {
        autenticarComo(DONO_ID, "USUARIO");
        LocalDateTime prazoInicial = LocalDateTime.now().plusDays(1);
        Reserva reserva = reservaAtiva(prazoInicial);

        for (int i = 0; i < 5; i++) {
            service.estender(RESERVA_ID);
        }

        assertThat(reserva.getDataExpiracao()).isEqualTo(prazoInicial.plusDays(35));
    }

    @Test
    void adminPodeEstender() {
        autenticarComo(99L, "ADMIN");
        LocalDateTime prazoAtual = LocalDateTime.now().plusDays(1);
        reservaAtiva(prazoAtual);

        assertThat(service.estender(RESERVA_ID).dataExpiracao())
                .isEqualTo(prazoAtual.plusDays(7));
    }

    @Test
    void quemReservouNaoPodeEstender() {
        autenticarComo(RESERVANTE_ID, "USUARIO");
        Reserva reserva = reservaAtiva(LocalDateTime.now().plusDays(1));
        LocalDateTime prazoOriginal = reserva.getDataExpiracao();

        assertThatThrownBy(() -> service.estender(RESERVA_ID))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN));
        assertThat(reserva.getDataExpiracao()).isEqualTo(prazoOriginal);
    }

    @Test
    void naoEstendeReservaQueJaExpirouFoiCanceladaOuColetada() {
        autenticarComo(DONO_ID, "USUARIO");

        for (StatusReserva status : new StatusReserva[]{
                StatusReserva.EXPIRADA, StatusReserva.CANCELADA, StatusReserva.COLETADA}) {

            reservaAtiva(LocalDateTime.now().minusDays(1)).setStatus(status);

            assertThatThrownBy(() -> service.estender(RESERVA_ID))
                    .as("status %s", status)
                    .isInstanceOfSatisfying(ResponseStatusException.class,
                            e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.CONFLICT));
        }
    }

    @Test
    void estenderReservaInexistenteRetorna404() {
        autenticarComo(DONO_ID, "USUARIO");
        when(reservaRepository.buscarPorIdComBloqueio(RESERVA_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.estender(RESERVA_ID))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    // ---------- concluir coleta ----------

    @Test
    void naoConcluiColetaDeReservaExpirada() {
        autenticarComo(DONO_ID, "USUARIO");
        reservaAtiva(LocalDateTime.now().minusDays(1)).setStatus(StatusReserva.EXPIRADA);

        assertThatThrownBy(() -> service.atualizar(RESERVA_ID, StatusReserva.COLETADA))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void donoConcluiColetaDeReservaAtiva() {
        autenticarComo(DONO_ID, "USUARIO");
        reservaAtiva(LocalDateTime.now().plusDays(3));

        Reserva atualizada = service.atualizar(RESERVA_ID, StatusReserva.COLETADA);

        assertThat(atualizada.getStatus()).isEqualTo(StatusReserva.COLETADA);
    }

    // ---------- telefone para o dono do resíduo ----------

    @Test
    void donoDoResiduoVeOTelefoneDeQuemReservou() {
        Reserva reserva = reservaAtiva(LocalDateTime.now().plusDays(3));

        ReservaRecebidaResponse resposta = ReservaRecebidaResponse.de(reserva);

        assertThat(resposta.reservadoPor()).isEqualTo("João Reserva");
        assertThat(resposta.telefone()).isEqualTo("85999990000");
        assertThat(resposta.dataExpiracao()).isEqualTo(reserva.getDataExpiracao());
    }

    // ---------- apoio ----------

    private Usuario usuario(long id, String nome, String telefone) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setNome(nome);
        u.setTelefone(telefone);
        return u;
    }

    private void liberarResiduoParaReserva() {
        when(usuarioRepository.findById(RESERVANTE_ID)).thenReturn(Optional.of(reservante));
        when(residuoRepository.buscarPorIdComBloqueio(RESIDUO_ID)).thenReturn(Optional.of(residuo));
    }

    private Reserva reservaAtiva(LocalDateTime dataExpiracao) {
        Reserva reserva = new Reserva();
        reserva.setId(RESERVA_ID);
        reserva.setUsuario(reservante);
        reserva.setResiduo(residuo);
        reserva.setStatus(StatusReserva.RESERVADA);
        reserva.setDataReserva(dataExpiracao.minusDays(7));
        reserva.setDataExpiracao(dataExpiracao);
        when(reservaRepository.buscarPorIdComBloqueio(RESERVA_ID)).thenReturn(Optional.of(reserva));
        return reserva;
    }

    private void autenticarComo(long usuarioId, String perfil) {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject(String.valueOf(usuarioId))
                .claim("perfil", perfil)
                .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
    }
}
