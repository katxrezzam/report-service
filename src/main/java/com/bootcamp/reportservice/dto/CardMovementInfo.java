package com.bootcamp.reportservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.Instant;

/** Subconjunto de la respuesta de GET /cards/{id}/movements que interesa para el reporte
 * (aplica tanto al reporte general como al de ultimos movimientos). Hoy solo tarjeta de credito
 * - de debito es Fase III, se agrega cuando ese producto exista. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CardMovementInfo(
        String id, String type, BigDecimal amount, BigDecimal usedAmountAfter, Instant timestamp) {
}
