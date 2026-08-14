package com.bootcamp.reportservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Clientes HTTP hacia los cuatro servicios que report-service compone (D4: Fase II resuelve
 * reportes por composicion de API, no eventos). A diferencia de los demas servicios - que solo
 * tienen uno o dos clientes cross-service y cada uno vive en su propia clase de config -
 * report-service depende de cuatro, asi que se consolidan aca en una sola clase para no repetir
 * cuatro clases casi identicas de un solo bean cada una.
 */
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient customerServiceWebClient(
            @Value("${customer-service.base-url}") String baseUrl) {
        return WebClient.builder().baseUrl(baseUrl).build();
    }

    @Bean
    public WebClient accountServiceWebClient(
            @Value("${account-service.base-url}") String baseUrl) {
        return WebClient.builder().baseUrl(baseUrl).build();
    }

    @Bean
    public WebClient creditServiceWebClient(
            @Value("${credit-service.base-url}") String baseUrl) {
        return WebClient.builder().baseUrl(baseUrl).build();
    }

    @Bean
    public WebClient cardServiceWebClient(
            @Value("${card-service.base-url}") String baseUrl) {
        return WebClient.builder().baseUrl(baseUrl).build();
    }
}
