package com.bootcamp.reportservice.exception;

/** El cliente pedido para el reporte no existe en customer-service. */
public class CustomerNotFoundException extends RuntimeException {
    public CustomerNotFoundException(String customerId) {
        super("No existe un cliente con id " + customerId);
    }
}
