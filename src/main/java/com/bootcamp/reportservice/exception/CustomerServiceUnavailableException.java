package com.bootcamp.reportservice.exception;

/** customer-service no respondio (caido, timeout, error 5xx, circuito abierto). */
public class CustomerServiceUnavailableException extends RuntimeException {
    public CustomerServiceUnavailableException(String customerId, Throwable cause) {
        super("No se pudo validar el cliente " + customerId + " contra customer-service", cause);
    }
}
