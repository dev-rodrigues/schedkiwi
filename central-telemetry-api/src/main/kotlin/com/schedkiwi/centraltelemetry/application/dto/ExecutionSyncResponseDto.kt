package com.schedkiwi.centraltelemetry.application.dto

/**
 * DTO de resposta para sincronização de execução
 */
data class ExecutionSyncResponseDto(
    val runId: String,
    val lastReceivedSequence: Long,
    val missingSequences: List<Long>,
    val status: String,
    val message: String
)
