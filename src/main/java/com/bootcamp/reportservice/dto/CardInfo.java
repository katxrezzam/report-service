package com.bootcamp.reportservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

/** Subconjunto de la respuesta de GET /cards?customerId= que interesa para el reporte. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CardInfo(String id, BigDecimal creditLimit, BigDecimal usedAmount) {
}
