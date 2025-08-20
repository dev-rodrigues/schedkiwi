package com.schedkiwi.schedulertelemetry.core

import java.time.Instant
import java.util.concurrent.atomic.AtomicLong

/**
 * Contexto de execução de um scheduler, contendo todas as informações
 * necessárias para telemetria e relatórios.
 * 
 * @property runId Identificador único da execução
 * @property jobId Identificador do job do scheduler
 * @property appName Nome da aplicação
 * @property startTime Timestamp de início da execução
 * @property plannedTotal Total esperado de itens para processar
 * @property processedItems Contador de itens processados
 * @property failedItems Contador de itens que falharam
 * @property skippedItems Contador de itens pulados
 * @property itemMetadata Metadados dos itens processados
 * @property exceptions Lista de exceções capturadas
 * @property generalMetadata Metadados gerais da execução
 */
data class ExecutionContext(
    val runId: String,
    val jobId: String,
    val appName: String,
    val startTime: Instant = Instant.now(),
    val plannedTotal: Long = 0L,
    val processedItems: AtomicLong = AtomicLong(0L),
    val failedItems: AtomicLong = AtomicLong(0L),
    val skippedItems: AtomicLong = AtomicLong(0L),
    val itemMetadata: MutableList<ItemMetadata> = mutableListOf(),
    val exceptions: MutableList<ExceptionInfo> = mutableListOf(),
    val generalMetadata: MutableMap<String, Any?> = mutableMapOf()
) {
    /**
     * Adiciona metadados de um item processado
     */
    fun addItemMetadata(key: String?, metadata: Map<String, Any?>, outcome: ItemOutcome) {
        itemMetadata.add(
            ItemMetadata(
                key = key,
                metadata = metadata,
                outcome = outcome,
                timestamp = Instant.now()
            )
        )
    }

    /**
     * Adiciona uma exceção ao contexto
     */
    fun addException(throwable: Throwable) {
        exceptions.add(
            ExceptionInfo(
                message = throwable.message ?: "Unknown error",
                type = throwable.javaClass.simpleName,
                stackTrace = throwable.stackTraceToString(),
                timestamp = Instant.now()
            )
        )
    }

    /**
     * Adiciona metadados gerais à execução
     */
    fun putMetadata(key: String, value: Any?) {
        generalMetadata[key] = value
    }

    /**
     * Obtém o total de itens processados (incluindo falhas e pulados)
     */
    fun getTotalProcessed(): Long = processedItems.get() + failedItems.get() + skippedItems.get()

    /**
     * Obtém a porcentagem de progresso baseada no total planejado
     */
    fun getProgressPercentage(): Double {
        if (plannedTotal <= 0) return 0.0
        return (getTotalProcessed().toDouble() / plannedTotal.toDouble()) * 100.0
    }

    /**
     * Verifica se a execução está completa
     */
    fun isComplete(): Boolean = getTotalProcessed() >= plannedTotal

    /**
     * Obtém o status da execução
     */
    fun getStatus(): ExecutionStatus {
        return when {
            exceptions.isNotEmpty() -> ExecutionStatus.FAILED
            isComplete() -> ExecutionStatus.COMPLETED
            else -> ExecutionStatus.RUNNING
        }
    }
}

/**
 * Metadados de um item individual processado
 */
data class ItemMetadata(
    val key: String?,
    val metadata: Map<String, Any?>,
    val outcome: ItemOutcome,
    val timestamp: Instant
)

/**
 * Informações sobre uma exceção capturada
 */
data class ExceptionInfo(
    val message: String,
    val type: String,
    val stackTrace: String,
    val timestamp: Instant
)

/**
 * Status da execução do scheduler
 */
enum class ExecutionStatus {
    RUNNING,
    COMPLETED,
    FAILED,
    PAUSED
}
