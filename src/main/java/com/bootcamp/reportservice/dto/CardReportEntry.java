package com.bootcamp.reportservice.dto;

import java.util.List;

/** Una tarjeta del cliente con sus movimientos en el intervalo pedido. */
public record CardReportEntry(CardInfo card, List<CardMovementInfo> movements) {
}
