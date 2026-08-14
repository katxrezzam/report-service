package com.bootcamp.reportservice.exception;

import com.bootcamp.reportservice.config.CorrelationIdWebFilter;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;

/**
 * Traduce cada excepcion de dominio a una respuesta HTTP consistente, siempre con
 * {@code correlationId} para poder auditar/rastrear el error.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCustomerNotFound(
            CustomerNotFoundException ex, ServerWebExchange exchange) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), exchange);
    }

    @ExceptionHandler(CardNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCardNotFound(
            CardNotFoundException ex, ServerWebExchange exchange) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), exchange);
    }

    @ExceptionHandler(CustomerServiceUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleCustomerServiceUnavailable(
            CustomerServiceUnavailableException ex, ServerWebExchange exchange) {
        log.error("customer-service no disponible, correlationId={}", correlationId(exchange), ex);
        return build(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), exchange);
    }

    @ExceptionHandler(AccountServiceUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleAccountServiceUnavailable(
            AccountServiceUnavailableException ex, ServerWebExchange exchange) {
        log.error("account-service no disponible, correlationId={}", correlationId(exchange), ex);
        return build(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), exchange);
    }

    @ExceptionHandler(CreditServiceUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleCreditServiceUnavailable(
            CreditServiceUnavailableException ex, ServerWebExchange exchange) {
        log.error("credit-service no disponible, correlationId={}", correlationId(exchange), ex);
        return build(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), exchange);
    }

    @ExceptionHandler(CardServiceUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleCardServiceUnavailable(
            CardServiceUnavailableException ex, ServerWebExchange exchange) {
        log.error("card-service no disponible, correlationId={}", correlationId(exchange), ex);
        return build(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), exchange);
    }

    /** GET /reports/customers/{id}?from=&to= exige from/to: sin ellos, WebFlux lanza esto ANTES
     * de llegar al controller. Sin este handler caeria en el catch-all generico y devolveria 500
     * en vez de 400 (mismo bug ya encontrado y documentado en Fase 1 con NoResourceFoundException
     * - ver CONVENTIONS.md). */
    @ExceptionHandler(ServerWebInputException.class)
    public ResponseEntity<ErrorResponse> handleServerWebInputException(
            ServerWebInputException ex, ServerWebExchange exchange) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) {
            status = HttpStatus.BAD_REQUEST;
        }
        return build(status, ex.getReason() != null ? ex.getReason() : ex.getMessage(), exchange);
    }

    /** Excepciones propias de Spring que ya traen su propio status code correcto (ej:
     * NoResourceFoundException). */
    @ExceptionHandler(ErrorResponseException.class)
    public ResponseEntity<ErrorResponse> handleErrorResponseException(
            ErrorResponseException ex, ServerWebExchange exchange) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return build(status, ex.getMessage(), exchange);
    }

    /** Cualquier excepcion no prevista: no se filtra su mensaje interno al cliente, solo se
     * loguea. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(
            Exception ex, ServerWebExchange exchange) {
        log.error("Error no controlado, correlationId={}", correlationId(exchange), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error inesperado", exchange);
    }

    private ResponseEntity<ErrorResponse> build(
            HttpStatus status, String message, ServerWebExchange exchange) {
        ErrorResponse body = new ErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                correlationId(exchange),
                exchange.getRequest().getPath().value());
        return ResponseEntity.status(status).body(body);
    }

    private String correlationId(ServerWebExchange exchange) {
        Object value = exchange.getAttribute(CorrelationIdWebFilter.CORRELATION_ID_ATTRIBUTE);
        return value != null ? value.toString() : "unknown";
    }
}
