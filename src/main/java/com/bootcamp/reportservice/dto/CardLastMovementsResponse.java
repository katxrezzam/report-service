package com.bootcamp.reportservice.dto;

import java.util.List;

/** Ultimos N movimientos de una tarjeta puntual (enunciado: "ultimos 10 movimientos de la
 * tarjeta de debito y de credito" - hoy solo credito, debito es Fase III). */
public record CardLastMovementsResponse(String cardId, List<CardMovementInfo> movements) {
}
