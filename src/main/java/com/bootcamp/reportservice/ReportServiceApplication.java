package com.bootcamp.reportservice;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Punto de entrada de report-service: reportes por composicion de APIs (D4, Fase II). Contrato
 * OpenAPI generado en /v3/api-docs, explorable en /swagger-ui.html. */
@OpenAPIDefinition(info = @Info(
        title = "report-service",
        version = "v1",
        description = "Reporte general por cliente en un intervalo (cuentas/creditos/tarjetas "
                + "con sus movimientos) y ultimos movimientos de tarjeta, sin base propia."))
@SpringBootApplication
public class ReportServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReportServiceApplication.class, args);
    }
}
