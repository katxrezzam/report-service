package com.bootcamp.reportservice.service;

import com.bootcamp.reportservice.dto.CardLastMovementsResponse;
import com.bootcamp.reportservice.dto.CustomerReportResponse;
import java.time.Instant;
import reactor.core.publisher.Mono;

/** Casos de uso de report-service: los dos reportes del enunciado, por composicion de API
 * (D4, Fase II). */
public interface ReportService {

    /** Reporte general por cliente en [from, to]: cuentas, creditos y tarjetas con sus
     * movimientos/pagos en el intervalo. */
    Mono<CustomerReportResponse> generateCustomerReport(
            String customerId, Instant from, Instant to);

    /** Ultimos N movimientos de una tarjeta puntual (N configurable, default 10). */
    Mono<CardLastMovementsResponse> lastCardMovements(String cardId);
}
