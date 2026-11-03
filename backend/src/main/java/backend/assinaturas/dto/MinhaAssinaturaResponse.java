package backend.assinaturas.dto;

import java.time.LocalDate;

/** Resumo da assinatura sem dados pessoais ou campos administrativos. */
public record MinhaAssinaturaResponse(
        Long id,
        Long planoId,
        String planoNome,
        String status,
        LocalDate dataInicio,
        LocalDate dataFim
) {}
