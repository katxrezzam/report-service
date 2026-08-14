package com.bootcamp.reportservice.service;

import com.bootcamp.reportservice.client.AccountClient;
import com.bootcamp.reportservice.client.CardClient;
import com.bootcamp.reportservice.client.CreditClient;
import com.bootcamp.reportservice.client.CustomerClient;
import com.bootcamp.reportservice.dto.AccountReportEntry;
import com.bootcamp.reportservice.dto.CardLastMovementsResponse;
import com.bootcamp.reportservice.dto.CardReportEntry;
import com.bootcamp.reportservice.dto.CreditReportEntry;
import com.bootcamp.reportservice.dto.CustomerReportResponse;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Implementacion de {@link ReportService}. Sin base de datos propia: compone en el momento
 * contra los cuatro servicios reales (D4, Fase II - en Fase III pasa a un modelo de lectura por
 * eventos). El reporte general hace fan-out reactivo: por cada producto del cliente
 * (cuenta/credito/tarjeta), trae sus movimientos en paralelo via flatMap.
 */
@Service
public class ReportServiceImpl implements ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportServiceImpl.class);

    private final CustomerClient customerClient;
    private final AccountClient accountClient;
    private final CreditClient creditClient;
    private final CardClient cardClient;
    private final int lastMovementsLimit;

    /** lastMovementsLimit viene de la config externalizada: el enunciado pide "los ultimos 10"
     * pero no hay razon para hardcodear el numero (D8: mismo criterio que el resto de los
     * parametros de negocio que el enunciado no fija). */
    public ReportServiceImpl(
            CustomerClient customerClient,
            AccountClient accountClient,
            CreditClient creditClient,
            CardClient cardClient,
            @Value("${report.card.last-movements-limit}") int lastMovementsLimit) {
        this.customerClient = customerClient;
        this.accountClient = accountClient;
        this.creditClient = creditClient;
        this.cardClient = cardClient;
        this.lastMovementsLimit = lastMovementsLimit;
    }

    @Override
    public Mono<CustomerReportResponse> generateCustomerReport(
            String customerId, Instant from, Instant to) {
        // Mono.defer: sin esto, Java evalua los argumentos de Mono.zip (y por lo tanto llama a
        // los tres clients) al construir la expresion, ANTES de que la validacion del cliente
        // pueda fallar y cortar la cadena (misma leccion que en account-service/customer-service,
        // ver CONVENTIONS.md).
        return customerClient.getCustomer(customerId)
                .then(Mono.defer(() -> Mono.zip(
                        buildAccountEntries(customerId, from, to).collectList(),
                        buildCreditEntries(customerId, from, to).collectList(),
                        buildCardEntries(customerId, from, to).collectList())))
                .map(tuple -> new CustomerReportResponse(
                        customerId, from, to, tuple.getT1(), tuple.getT2(), tuple.getT3()))
                .doOnNext(response -> log.info(
                        "Reporte generado customerId={} cuentas={} creditos={} tarjetas={}",
                        customerId, response.accounts().size(), response.credits().size(),
                        response.cards().size()));
    }

    private Flux<AccountReportEntry> buildAccountEntries(
            String customerId, Instant from, Instant to) {
        return accountClient.getAccountsByHolder(customerId)
                .flatMap(account -> accountClient.getMovements(account.id(), from, to)
                        .collectList()
                        .map(movements -> new AccountReportEntry(account, movements)));
    }

    private Flux<CreditReportEntry> buildCreditEntries(
            String customerId, Instant from, Instant to) {
        return creditClient.getCreditsByCustomer(customerId)
                .flatMap(credit -> creditClient.getPayments(credit.id(), from, to)
                        .collectList()
                        .map(payments -> new CreditReportEntry(credit, payments)));
    }

    private Flux<CardReportEntry> buildCardEntries(String customerId, Instant from, Instant to) {
        return cardClient.getCardsByCustomer(customerId)
                .flatMap(card -> cardClient.getMovements(card.id(), from, to, null)
                        .collectList()
                        .map(movements -> new CardReportEntry(card, movements)));
    }

    @Override
    public Mono<CardLastMovementsResponse> lastCardMovements(String cardId) {
        return cardClient.getMovements(cardId, null, null, lastMovementsLimit)
                .collectList()
                .map(movements -> new CardLastMovementsResponse(cardId, movements))
                .doOnNext(response -> log.info(
                        "Ultimos movimientos consultados cardId={} cantidad={}",
                        cardId, response.movements().size()));
    }
}
