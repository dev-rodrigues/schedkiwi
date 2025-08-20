package com.schedkiwi.centraltelemetry.application.dto

import java.time.Instant

/**
 * DTO de resposta para status de execução
 */
data class ExecutionStatusResponseDto(
    val runId: String,
    val status: String,
    val startTime: Instant,
    val endTime: Instant?,
    val duration: String,
    val plannedTotal: Long,
    val processedItems: Long,
    val failedItems: Long,
    val skippedItems: Long,
    val progressPercentage: Double,
    val lastUpdate: Instant
)
