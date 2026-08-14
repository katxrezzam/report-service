package com.bootcamp.reportservice.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bootcamp.reportservice.client.AccountClient;
import com.bootcamp.reportservice.client.CardClient;
import com.bootcamp.reportservice.client.CreditClient;
import com.bootcamp.reportservice.client.CustomerClient;
import com.bootcamp.reportservice.dto.AccountInfo;
import com.bootcamp.reportservice.dto.AccountMovementInfo;
import com.bootcamp.reportservice.dto.CardInfo;
import com.bootcamp.reportservice.dto.CardMovementInfo;
import com.bootcamp.reportservice.dto.CreditInfo;
import com.bootcamp.reportservice.dto.CreditPaymentInfo;
import com.bootcamp.reportservice.dto.CustomerInfo;
import com.bootcamp.reportservice.exception.CardNotFoundException;
import com.bootcamp.reportservice.exception.CustomerNotFoundException;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock
    private CustomerClient customerClient;
    @Mock
    private AccountClient accountClient;
    @Mock
    private CreditClient creditClient;
    @Mock
    private CardClient cardClient;

    private static final int LAST_MOVEMENTS_LIMIT = 10;

    private ReportServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ReportServiceImpl(
                customerClient, accountClient, creditClient, cardClient, LAST_MOVEMENTS_LIMIT);
    }

    // ---------- generateCustomerReport ----------

    @Test
    void generateCustomerReport_clienteInexistente_fallaSinConsultarElResto() {
        when(customerClient.getCustomer("no-existe"))
                .thenReturn(Mono.error(new CustomerNotFoundException("no-existe")));

        StepVerifier.create(service.generateCustomerReport(
                        "no-existe", Instant.EPOCH, Instant.now()))
                .expectError(CustomerNotFoundException.class)
                .verify();

        verify(accountClient, never()).getAccountsByHolder(any());
        verify(creditClient, never()).getCreditsByCustomer(any());
        verify(cardClient, never()).getCardsByCustomer(any());
    }

    @Test
    void generateCustomerReport_clienteConProductos_componeCuentasCreditosYTarjetas() {
        Instant from = Instant.EPOCH;
        Instant to = Instant.now();

        AccountInfo account = new AccountInfo("acc1", "SAVINGS", new BigDecimal("100.00"));
        AccountMovementInfo accountMovement = new AccountMovementInfo(
                "mv1", "DEPOSIT", new BigDecimal("10.00"), new BigDecimal("110.00"),
                Instant.now(), null, null);
        CreditInfo credit = new CreditInfo("cred1", new BigDecimal("300.00"), "ACTIVE");
        CreditPaymentInfo payment = new CreditPaymentInfo(
                "pay1", 1, new BigDecimal("100.00"), Instant.now());
        CardInfo card = new CardInfo("card1", new BigDecimal("500.00"), BigDecimal.ZERO);
        CardMovementInfo cardMovement = new CardMovementInfo(
                "cmv1", "CONSUMPTION", new BigDecimal("50.00"), new BigDecimal("50.00"),
                Instant.now());

        when(customerClient.getCustomer("cust1")).thenReturn(Mono.just(new CustomerInfo("cust1")));
        when(accountClient.getAccountsByHolder("cust1")).thenReturn(Flux.just(account));
        when(accountClient.getMovements("acc1", from, to)).thenReturn(Flux.just(accountMovement));
        when(creditClient.getCreditsByCustomer("cust1")).thenReturn(Flux.just(credit));
        when(creditClient.getPayments("cred1", from, to)).thenReturn(Flux.just(payment));
        when(cardClient.getCardsByCustomer("cust1")).thenReturn(Flux.just(card));
        when(cardClient.getMovements(eq("card1"), eq(from), eq(to), isNull()))
                .thenReturn(Flux.just(cardMovement));

        StepVerifier.create(service.generateCustomerReport("cust1", from, to))
                .expectNextMatches(response ->
                        response.customerId().equals("cust1")
                                && response.accounts().size() == 1
                                && response.accounts().get(0).movements().size() == 1
                                && response.credits().size() == 1
                                && response.credits().get(0).payments().size() == 1
                                && response.cards().size() == 1
                                && response.cards().get(0).movements().size() == 1)
                .verifyComplete();
    }

    @Test
    void generateCustomerReport_clienteSinProductos_devuelveListasVacias() {
        Instant from = Instant.EPOCH;
        Instant to = Instant.now();

        when(customerClient.getCustomer("cust2")).thenReturn(Mono.just(new CustomerInfo("cust2")));
        when(accountClient.getAccountsByHolder("cust2")).thenReturn(Flux.empty());
        when(creditClient.getCreditsByCustomer("cust2")).thenReturn(Flux.empty());
        when(cardClient.getCardsByCustomer("cust2")).thenReturn(Flux.empty());

        StepVerifier.create(service.generateCustomerReport("cust2", from, to))
                .expectNextMatches(response ->
                        response.accounts().isEmpty()
                                && response.credits().isEmpty()
                                && response.cards().isEmpty())
                .verifyComplete();
    }

    // ---------- lastCardMovements ----------

    @Test
    void lastCardMovements_tarjetaExistente_devuelveMovimientosConElLimiteConfigurado() {
        CardMovementInfo movement = new CardMovementInfo(
                "cmv1", "CONSUMPTION", new BigDecimal("50.00"), new BigDecimal("50.00"),
                Instant.now());
        when(cardClient.getMovements("card1", null, null, LAST_MOVEMENTS_LIMIT))
                .thenReturn(Flux.just(movement));

        StepVerifier.create(service.lastCardMovements("card1"))
                .expectNextMatches(response ->
                        "card1".equals(response.cardId()) && response.movements().size() == 1)
                .verifyComplete();
    }

    @Test
    void lastCardMovements_tarjetaInexistente_falla() {
        when(cardClient.getMovements("no-existe", null, null, LAST_MOVEMENTS_LIMIT))
                .thenReturn(Flux.error(new CardNotFoundException("no-existe")));

        StepVerifier.create(service.lastCardMovements("no-existe"))
                .expectError(CardNotFoundException.class)
                .verify();
    }
}
