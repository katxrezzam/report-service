package com.bootcamp.reportservice.client;

import com.bootcamp.reportservice.dto.CardInfo;
import com.bootcamp.reportservice.dto.CardMovementInfo;
import com.bootcamp.reportservice.exception.CardNotFoundException;
import com.bootcamp.reportservice.exception.CardServiceUnavailableException;
import java.time.Instant;
import java.util.Optional;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Cliente REST hacia card-service, protegido con circuit breaker + timeout de 2s.
 * getMovements() distingue el 404 de negocio (tarjeta inexistente) de una falla tecnica -
 * GET /reports/cards/{id}/last-movements recibe el id directo del usuario, asi que necesita
 * poder devolver un 404 limpio (mismo criterio que CustomerClient), no un 503 generico.
 */
@Component
public class CardClient {

    private static final String CIRCUIT_BREAKER_ID = "card-service";

    private final WebClient webClient;
    private final ReactiveCircuitBreaker circuitBreaker;

    public CardClient(
            WebClient cardServiceWebClient,
            ReactiveCircuitBreakerFactory circuitBreakerFactory) {
        this.webClient = cardServiceWebClient;
        this.circuitBreaker = circuitBreakerFactory.create(CIRCUIT_BREAKER_ID);
    }

    /** Tarjetas del cliente (GET /cards?customerId=). Lista vacia si no tiene ninguna. */
    public Flux<CardInfo> getCardsByCustomer(String customerId) {
        Flux<CardInfo> call = webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/cards")
                        .queryParam("customerId", customerId)
                        .build())
                .retrieve()
                .bodyToFlux(CardInfo.class);

        return circuitBreaker.run(call, throwable -> Flux.error(
                new CardServiceUnavailableException("tarjetas de " + customerId, throwable)));
    }

    /** Movimientos de una tarjeta: con from/to filtra por intervalo (reporte general); con
     * limit devuelve los ultimos N (reporte de ultimos movimientos). GET
     * /cards/{id}/movements?from=&to=&limit=. */
    public Flux<CardMovementInfo> getMovements(
            String cardId, Instant from, Instant to, Integer limit) {
        Flux<CardMovementInfo> call = webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/cards/{id}/movements")
                        .queryParamIfPresent("from", Optional.ofNullable(from))
                        .queryParamIfPresent("to", Optional.ofNullable(to))
                        .queryParamIfPresent("limit", Optional.ofNullable(limit))
                        .build(cardId))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        response -> Mono.error(new CardNotFoundException(cardId)))
                .bodyToFlux(CardMovementInfo.class);

        return circuitBreaker.run(call, throwable -> mapFallback(cardId, throwable));
    }

    private Flux<CardMovementInfo> mapFallback(String cardId, Throwable throwable) {
        if (throwable instanceof CardNotFoundException) {
            return Flux.error(throwable);
        }
        return Flux.error(new CardServiceUnavailableException(
                "movimientos de la tarjeta " + cardId, throwable));
    }
}
