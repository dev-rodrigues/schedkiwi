package com.schedkiwi.centraltelemetry.domain.entities

import java.time.Instant
import java.util.*

/**
 * Entidade de domínio que representa um job agendado
 */
data class ScheduledJob(
    val id: UUID = UUID.randomUUID(),
    val jobId: String,
    val methodName: String,
    val className: String,
    val cronExpression: String? = null,
    val fixedRate: Long? = null,
    val fixedDelay: Long? = null,
    val timeUnit: String = "MILLISECONDS",
    val applicationId: UUID,
    val createdAt: Instant = Instant.now(),
    val executions: MutableList<Execution> = mutableListOf()
) {
    fun addExecution(execution: Execution) {
        executions.add(execution)
    }
}
