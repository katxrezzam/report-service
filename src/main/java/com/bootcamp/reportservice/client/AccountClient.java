package com.bootcamp.reportservice.client;

import com.bootcamp.reportservice.dto.AccountInfo;
import com.bootcamp.reportservice.dto.AccountMovementInfo;
import com.bootcamp.reportservice.exception.AccountServiceUnavailableException;
import java.time.Instant;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

/** Cliente REST hacia account-service, protegido con circuit breaker + timeout de 2s. */
@Component
public class AccountClient {

    private static final String CIRCUIT_BREAKER_ID = "account-service";

    private final WebClient webClient;
    private final ReactiveCircuitBreaker circuitBreaker;

    public AccountClient(
            WebClient accountServiceWebClient,
            ReactiveCircuitBreakerFactory circuitBreakerFactory) {
        this.webClient = accountServiceWebClient;
        this.circuitBreaker = circuitBreakerFactory.create(CIRCUIT_BREAKER_ID);
    }

    /** Cuentas del cliente (GET /accounts?holderId=). Lista vacia si no tiene ninguna - no es
     * un error de negocio. */
    public Flux<AccountInfo> getAccountsByHolder(String customerId) {
        Flux<AccountInfo> call = webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/accounts")
                        .queryParam("holderId", customerId)
                        .build())
                .retrieve()
                .bodyToFlux(AccountInfo.class);

        return circuitBreaker.run(call, throwable -> Flux.error(
                new AccountServiceUnavailableException("cuentas de " + customerId, throwable)));
    }

    /** Movimientos de una cuenta en el intervalo [from, to] (GET
     * /accounts/{id}/movements?from=&to=). */
    public Flux<AccountMovementInfo> getMovements(String accountId, Instant from, Instant to) {
        Flux<AccountMovementInfo> call = webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/accounts/{id}/movements")
                        .queryParam("from", from)
                        .queryParam("to", to)
                        .build(accountId))
                .retrieve()
                .bodyToFlux(AccountMovementInfo.class);

        return circuitBreaker.run(call, throwable -> Flux.error(
                new AccountServiceUnavailableException(
                        "movimientos de la cuenta " + accountId, throwable)));
    }
}
