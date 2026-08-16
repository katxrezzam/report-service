# Diagramas de secuencia — report-service

Requerimiento no funcional (Parte I): *"Elaborar diagramas de secuencia de cada microservicio."*

## Reporte general por cliente (composición de APIs, D4)

`report-service` no tiene base de datos propia: compone en el momento contra los 4 servicios de
dominio. El cliente se valida primero (con `Mono.defer` en las tres llamadas siguientes, para no
dispararlas antes de confirmar que existe).

```mermaid
sequenceDiagram
    actor Cliente
    participant GW as api-gateway
    participant RS as report-service
    participant CustS as customer-service
    participant AS as account-service
    participant CrS as credit-service
    participant CardS as card-service

    Cliente->>GW: GET /reports/customers/{id}?from=&to=
    GW->>RS: forward
    RS->>CustS: GET /customers/{id} (valida que exista)
    CustS-->>RS: Customer
    par Mono.zip - en paralelo, recién tras validar el cliente
        RS->>AS: GET /accounts?holderId= + movimientos por cuenta (from/to)
        AS-->>RS: cuentas + movimientos
    and
        RS->>CrS: GET /credits?customerId= + pagos por crédito (from/to)
        CrS-->>RS: créditos + pagos
    and
        RS->>CardS: GET /cards?customerId= + movimientos por tarjeta (from/to)
        CardS-->>RS: tarjetas + movimientos
    end
    RS->>RS: compone el reporte (cuentas/créditos/tarjetas con sus movimientos anidados)
    RS-->>GW: 200 (reporte compuesto)
    GW-->>Cliente: respuesta
```

## Últimos movimientos de una tarjeta (crédito o débito)

```mermaid
sequenceDiagram
    actor Cliente
    participant GW as api-gateway
    participant RS as report-service
    participant CardS as card-service

    Cliente->>GW: GET /reports/cards/{id}/last-movements
    GW->>RS: forward
    RS->>CardS: GET /cards/{id} (valida que exista)
    alt tarjeta inexistente
        CardS-->>RS: 404
        RS-->>GW: 404
    else existe
        CardS-->>RS: Card
        RS->>CardS: GET /cards/{id}/movements?limit=N (orden descendente)
        CardS-->>RS: últimos N movimientos
        RS-->>GW: 200
    end
    GW-->>Cliente: respuesta
```
