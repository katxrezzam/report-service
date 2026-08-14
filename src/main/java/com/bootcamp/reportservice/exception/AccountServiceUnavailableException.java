package com.bootcamp.reportservice.exception;

/** account-service no respondio (caido, timeout, error 5xx, circuito abierto). */
public class AccountServiceUnavailableException extends RuntimeException {
    public AccountServiceUnavailableException(String detail, Throwable cause) {
        super("No se pudo componer el reporte de cuentas (" + detail
                + ") contra account-service", cause);
    }
}
