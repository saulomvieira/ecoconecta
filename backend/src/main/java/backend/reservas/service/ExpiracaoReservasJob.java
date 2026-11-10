package backend.reservas.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// Devolve ao feed os resíduos cujas reservas passaram do prazo.
@Component
public class ExpiracaoReservasJob {

    private static final Logger log = LoggerFactory.getLogger(ExpiracaoReservasJob.class);

    private final ReservaService reservaService;

    public ExpiracaoReservasJob(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    // A cada minuto. O delay inicial deixa o AjusteEsquemaReservas rodar antes.
    @Scheduled(fixedDelay = 60_000, initialDelay = 30_000)
    public void expirarReservas() {

        int expiradas = reservaService.expirarVencidas();

        if (expiradas > 0) {
            log.info("{} reserva(s) expirada(s); resíduos liberados no feed", expiradas);
        }
    }
}
