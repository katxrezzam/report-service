package com.bootcamp.reportservice.dto;

import java.time.Instant;
import java.util.List;

/** Reporte general por cliente en un intervalo (D8/Parte II): cuentas, creditos y tarjetas del
 * cliente, cada uno con sus movimientos/pagos dentro de [from, to]. */
public record CustomerReportResponse(
        String customerId,
        Instant from,
        Instant to,
        List<AccountReportEntry> accounts,
        List<CreditReportEntry> credits,
        List<CardReportEntry> cards) {
}
