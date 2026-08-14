package com.bootcamp.reportservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.bootcamp.reportservice.dto.CardLastMovementsResponse;
import com.bootcamp.reportservice.dto.CustomerReportResponse;
import com.bootcamp.reportservice.exception.CardNotFoundException;
import com.bootcamp.reportservice.exception.CustomerNotFoundException;
import com.bootcamp.reportservice.exception.GlobalExceptionHandler;
import com.bootcamp.reportservice.service.ReportService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@WebFluxTest(controllers = ReportController.class)
@Import(GlobalExceptionHandler.class)
class ReportControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private ReportService reportService;

    @Test
    void customerReport_clienteValido_retorna200() {
        CustomerReportResponse response = new CustomerReportResponse(
                "cust1", Instant.EPOCH, Instant.now(), List.of(), List.of(), List.of());
        when(reportService.generateCustomerReport(eq("cust1"), any(), any()))
                .thenReturn(Mono.just(response));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/reports/customers/cust1")
                        .queryParam("from", "1970-01-01T00:00:00Z")
                        .queryParam("to", "2026-08-14T00:00:00Z")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.customerId").isEqualTo("cust1");
    }

    @Test
    void customerReport_sinFromNiTo_retorna400NoQuinientos() {
        webTestClient.get().uri("/reports/customers/cust1")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.correlationId").exists();
    }

    @Test
    void customerReport_clienteInexistente_retorna404() {
        when(reportService.generateCustomerReport(eq("no-existe"), any(), any()))
                .thenReturn(Mono.error(new CustomerNotFoundException("no-existe")));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/reports/customers/no-existe")
                        .queryParam("from", "1970-01-01T00:00:00Z")
                        .queryParam("to", "2026-08-14T00:00:00Z")
                        .build())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void lastCardMovements_tarjetaValida_retorna200() {
        CardLastMovementsResponse response = new CardLastMovementsResponse("card1", List.of());
        when(reportService.lastCardMovements("card1")).thenReturn(Mono.just(response));

        webTestClient.get().uri("/reports/cards/card1/last-movements")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.cardId").isEqualTo("card1");
    }

    @Test
    void lastCardMovements_tarjetaInexistente_retorna404() {
        when(reportService.lastCardMovements("no-existe"))
                .thenReturn(Mono.error(new CardNotFoundException("no-existe")));

        webTestClient.get().uri("/reports/cards/no-existe/last-movements")
                .exchange()
                .expectStatus().isNotFound();
    }
}
