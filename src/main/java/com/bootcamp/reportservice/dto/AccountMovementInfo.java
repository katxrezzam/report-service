package com.bootcamp.reportservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.Instant;

/** Subconjunto de la respuesta de GET /accounts/{id}/movements que interesa para el reporte. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AccountMovementInfo(
        String id,
        String type,
        BigDecimal amount,
        BigDecimal balanceAfter,
        Instant timestamp,
        String description,
        String counterpartyAccountId) {
}
