package com.bootcamp.reportservice.dto;

import java.util.List;

/** Un credito del cliente con sus pagos en el intervalo pedido. */
public record CreditReportEntry(CreditInfo credit, List<CreditPaymentInfo> payments) {
}
