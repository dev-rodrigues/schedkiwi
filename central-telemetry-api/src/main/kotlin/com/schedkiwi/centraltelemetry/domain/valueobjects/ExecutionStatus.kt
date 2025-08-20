package com.schedkiwi.centraltelemetry.domain.valueobjects

/**
 * Value Object que representa o status de uma execução
 */
enum class ExecutionStatus {
    RUNNING,    // Executando
    COMPLETED,  // Concluída com sucesso
    FAILED,     // Falhou
    PAUSED      // Pausada
}
