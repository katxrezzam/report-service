package com.bootcamp.reportservice.exception;

/** La tarjeta pedida (GET /reports/cards/{id}/last-movements) no existe en card-service. */
public class CardNotFoundException extends RuntimeException {
    public CardNotFoundException(String cardId) {
        super("No existe una tarjeta con id " + cardId);
    }
}
