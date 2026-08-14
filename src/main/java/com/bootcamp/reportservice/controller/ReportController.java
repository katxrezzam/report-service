package com.bootcamp.reportservice.controller;

import com.bootcamp.reportservice.dto.CardLastMovementsResponse;
import com.bootcamp.reportservice.dto.CustomerReportResponse;
import com.bootcamp.reportservice.service.ReportService;
import java.time.Instant;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/** Endpoints REST de los dos reportes del enunciado. Delega toda la composicion en
 * {@link ReportService}. */
@RestController
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /** from/to son obligatorios ("intervalo de tiempo especificado por el usuario" - enunciado
     * literal): sin ellos, WebFlux devuelve 400 antes de llegar aca (ver
     * GlobalExceptionHandler#handleServerWebInputException). */
    @GetMapping("/customers/{customerId}")
    public Mono<CustomerReportResponse> customerReport(
            @PathVariable String customerId,
            @RequestParam Instant from,
            @RequestParam Instant to) {
        return reportService.generateCustomerReport(customerId, from, to);
    }

    @GetMapping("/cards/{cardId}/last-movements")
    public Mono<CardLastMovementsResponse> lastCardMovements(@PathVariable String cardId) {
        return reportService.lastCardMovements(cardId);
    }
}
