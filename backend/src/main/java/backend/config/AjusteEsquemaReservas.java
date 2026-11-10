package backend.config;

import backend.reservas.model.Reserva.StatusReserva;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

// O projeto usa `ddl-auto: update`, que cria colunas novas mas não altera a
// constraint CHECK que o Hibernate gera para o enum de status. Sem este ajuste,
// bancos criados antes do status EXPIRADA recusariam o valor novo.
// Tudo aqui é idempotente e roda a cada inicialização, depois do ddl-auto.
@Component
public class AjusteEsquemaReservas implements ApplicationRunner {

    private final JdbcTemplate jdbc;

    public AjusteEsquemaReservas(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void run(ApplicationArguments args) {

        // Recria a constraint a partir dos valores atuais do enum
        String valores = Arrays.stream(StatusReserva.values())
                .map(status -> "'" + status.name() + "'")
                .collect(Collectors.joining(", "));

        jdbc.execute("ALTER TABLE reservas DROP CONSTRAINT IF EXISTS reservas_status_check");
        jdbc.execute("ALTER TABLE reservas ADD CONSTRAINT reservas_status_check "
                + "CHECK (status IN (" + valores + "))");

        // Reservas anteriores ao prazo ganham 7 dias a contar da data da reserva
        jdbc.update("UPDATE reservas SET data_expiracao = data_reserva + INTERVAL '7 days' "
                + "WHERE data_expiracao IS NULL");
    }
}
