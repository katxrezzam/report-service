package com.bootcamp.reportservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.Instant;

/** Subconjunto de la respuesta de GET /credits/{id}/payments que interesa para el reporte. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CreditPaymentInfo(
        String id, int installmentNumber, BigDecimal amount, Instant timestamp) {
}
