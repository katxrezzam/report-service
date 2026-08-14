package com.bootcamp.reportservice.exception;

/** card-service no respondio (caido, timeout, error 5xx, circuito abierto). */
public class CardServiceUnavailableException extends RuntimeException {
    public CardServiceUnavailableException(String detail, Throwable cause) {
        super("No se pudo componer el reporte de tarjetas (" + detail
                + ") contra card-service", cause);
    }
}
