package com.schedkiwi.centraltelemetry.domain.valueobjects

/**
 * Value Object que representa o resultado do processamento de um item
 */
enum class ItemOutcome {
    OK,      // Processado com sucesso
    ERROR,   // Erro no processamento
    SKIPPED  // Item pulado
}
