package com.bootcamp.reportservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Subconjunto de la respuesta de GET /customers/{id} que a report-service le interesa: solo
 * confirmar que el cliente existe antes de componer el reporte. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CustomerInfo(String id) {
}
