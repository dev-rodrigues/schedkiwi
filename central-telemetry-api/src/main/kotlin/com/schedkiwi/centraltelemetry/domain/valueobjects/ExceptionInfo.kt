package com.schedkiwi.centraltelemetry.domain.valueobjects

import java.time.Instant
import java.util.*

/**
 * Value Object que representa informações de uma exceção capturada
 */
data class ExceptionInfo(
    val id: UUID = UUID.randomUUID(),
    val message: String,
    val type: String,
    val stackTrace: String? = null,
    val capturedAt: Instant = Instant.now(),
    val severity: ExceptionSeverity = ExceptionSeverity.HIGH
)

/**
 * Value Object que representa a severidade de uma exceção
 */
enum class ExceptionSeverity {
    LOW,     // Baixa severidade
    MEDIUM,  // Severidade média
    HIGH,    // Alta severidade
    CRITICAL, // Severidade crítica
    ERROR    // Erro padrão
}
