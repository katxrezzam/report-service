package com.bootcamp.reportservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

/** Subconjunto de la respuesta de GET /accounts?holderId= que interesa para el reporte. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AccountInfo(String id, String accountType, BigDecimal balance) {
}
