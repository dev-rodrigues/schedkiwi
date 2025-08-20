package com.schedkiwi.centraltelemetry

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Aplicação principal da API Central de Telemetria
 *
 * Esta aplicação recebe e processa telemetria de schedulers Spring Boot,
 * implementando arquitetura hexagonal com separação clara entre camadas.
 */
@SpringBootApplication
class CentralTelemetryApiApplication

fun main(args: Array<String>) {
    runApplication<CentralTelemetryApiApplication>(*args)
}
