package com.schedkiwi.schedulertelemetry.core

/**
 * Representa o resultado do processamento de um item individual no scheduler.
 * 
 * @property OK Item processado com sucesso
 * @property ERROR Item falhou durante o processamento
 * @property SKIPPED Item foi pulado (não processado)
 */
enum class ItemOutcome {
    /**
     * Item processado com sucesso
     */
    OK,
    
    /**
     * Item falhou durante o processamento
     */
    ERROR,
    
    /**
     * Item foi pulado (não processado)
     */
    SKIPPED
}
