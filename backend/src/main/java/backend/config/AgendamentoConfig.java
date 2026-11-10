package backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

// Habilita as tarefas @Scheduled (ex.: expiração de reservas)
@Configuration
@EnableScheduling
public class AgendamentoConfig {
}
