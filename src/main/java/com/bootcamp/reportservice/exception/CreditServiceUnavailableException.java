package com.bootcamp.reportservice.exception;

/** credit-service no respondio (caido, timeout, error 5xx, circuito abierto). */
public class CreditServiceUnavailableException extends RuntimeException {
    public CreditServiceUnavailableException(String detail, Throwable cause) {
        super("No se pudo componer el reporte de creditos (" + detail
                + ") contra credit-service", cause);
    }
}
