package com.schedkiwi.centraltelemetry.domain.entities

import com.schedkiwi.centraltelemetry.domain.valueobjects.ExecutionStatus
import com.schedkiwi.centraltelemetry.domain.valueobjects.ItemMetadata
import com.schedkiwi.centraltelemetry.domain.valueobjects.ExceptionInfo
import java.time.Instant
import java.util.*

/**
 * Entidade de domínio que representa uma execução de scheduler
 */
data class Execution(
    val id: UUID = UUID.randomUUID(),
    val runId: String,
    val jobId: String,
    val appName: String,
    val status: ExecutionStatus,
    val startTime: Instant,
    val endTime: Instant? = null,
    val plannedTotal: Long = 0,
    val processedItems: Long = 0,
    val failedItems: Long = 0,
    val skippedItems: Long = 0,
    val applicationId: UUID,
    val scheduledJobId: UUID,
    val generalMetadata: MutableMap<String, Any> = mutableMapOf(),
    val itemMetadata: MutableList<ItemMetadata> = mutableListOf(),
    val exceptions: MutableList<ExceptionInfo> = mutableListOf()
) {
    fun addItemMetadata(metadata: ItemMetadata) {
        itemMetadata.add(metadata)
    }

    fun addException(exception: ExceptionInfo) {
        exceptions.add(exception)
    }

    fun putMetadata(key: String, value: Any) {
        generalMetadata[key] = value
    }

    fun updateStatus(newStatus: ExecutionStatus) {
        // Em uma implementação real, isso seria feito via método que atualiza o status
    }
}
