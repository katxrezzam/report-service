package com.bootcamp.reportservice.client;

import com.bootcamp.reportservice.dto.CreditInfo;
import com.bootcamp.reportservice.dto.CreditPaymentInfo;
import com.bootcamp.reportservice.exception.CreditServiceUnavailableException;
import java.time.Instant;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

/** Cliente REST hacia credit-service, protegido con circuit breaker + timeout de 2s. */
@Component
public class CreditClient {

    private static final String CIRCUIT_BREAKER_ID = "credit-service";

    private final WebClient webClient;
    private final ReactiveCircuitBreaker circuitBreaker;

    public CreditClient(
            WebClient creditServiceWebClient,
            ReactiveCircuitBreakerFactory circuitBreakerFactory) {
        this.webClient = creditServiceWebClient;
        this.circuitBreaker = circuitBreakerFactory.create(CIRCUIT_BREAKER_ID);
    }

    /** Creditos del cliente (GET /credits?customerId=). Lista vacia si no tiene ninguno - el
     * enunciado permite tener creditos sin cuenta y viceversa. */
    public Flux<CreditInfo> getCreditsByCustomer(String customerId) {
        Flux<CreditInfo> call = webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/credits")
                        .queryParam("customerId", customerId)
                        .build())
                .retrieve()
                .bodyToFlux(CreditInfo.class);

        return circuitBreaker.run(call, throwable -> Flux.error(
                new CreditServiceUnavailableException("creditos de " + customerId, throwable)));
    }

    /** Pagos de un credito en el intervalo [from, to] (GET
     * /credits/{id}/payments?from=&to=). */
    public Flux<CreditPaymentInfo> getPayments(String creditId, Instant from, Instant to) {
        Flux<CreditPaymentInfo> call = webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/credits/{id}/payments")
                        .queryParam("from", from)
                        .queryParam("to", to)
                        .build(creditId))
                .retrieve()
                .bodyToFlux(CreditPaymentInfo.class);

        return circuitBreaker.run(call, throwable -> Flux.error(
                new CreditServiceUnavailableException(
                        "pagos del credito " + creditId, throwable)));
    }
}
