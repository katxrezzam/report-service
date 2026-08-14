package com.bootcamp.reportservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

/** Subconjunto de la respuesta de GET /credits?customerId= que interesa para el reporte. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CreditInfo(String id, BigDecimal totalAmount, String status) {
}
