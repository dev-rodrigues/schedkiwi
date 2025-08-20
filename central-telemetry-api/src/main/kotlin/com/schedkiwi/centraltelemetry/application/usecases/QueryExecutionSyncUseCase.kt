package com.schedkiwi.centraltelemetry.application.usecases

import com.schedkiwi.centraltelemetry.application.dto.ExecutionSyncResponseDto
import com.schedkiwi.centraltelemetry.domain.entities.Execution
import com.schedkiwi.centraltelemetry.domain.ports.ExecutionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Use Case para consulta de sincronização de execução
 */
@Service
@Transactional(readOnly = true)
class QueryExecutionSyncUseCase(
    private val executionRepository: ExecutionRepository
) {

    /**
     * Consulta a sincronização de uma execução
     */
    fun execute(runId: String): ExecutionSyncResponseDto {
        // Buscar execução
        val execution = executionRepository.findByRunId(runId)
            ?: throw IllegalArgumentException("Execução com runId '$runId' não encontrada")

        // Extrair informações de sequência dos metadados
        val lastSequenceNumber = extractLastSequenceNumber(execution)
        val missingSequences = calculateMissingSequences(execution, lastSequenceNumber)

        // Determinar status de sincronização
        val syncStatus = determineSyncStatus(missingSequences)
        val message = generateSyncMessage(syncStatus, missingSequences)

        return ExecutionSyncResponseDto(
            runId = execution.runId,
            lastReceivedSequence = lastSequenceNumber,
            missingSequences = missingSequences,
            status = syncStatus,
            message = message
        )
    }

    /**
     * Extrai o último número de sequência dos metadados
     */
    private fun extractLastSequenceNumber(execution: Execution): Long {
        val lastSequenceStr = execution.generalMetadata["last_sequence_number"]?.toString()
        return lastSequenceStr?.toLongOrNull() ?: 0L
    }

    /**
     * Calcula sequências faltantes
     */
    private fun calculateMissingSequences(execution: Execution, lastSequence: Long): List<Long> {
        if (lastSequence <= 0) return emptyList()

        val missing = mutableListOf<Long>()
        for (i in 1..lastSequence) {
            val sequenceExists = execution.generalMetadata.any { 
                it.key == "sequence_$i" || it.key == "last_sequence_number" && it.value.toString().toLongOrNull() == i
            }
            if (!sequenceExists) {
                missing.add(i)
            }
        }
        return missing
    }

    /**
     * Determina o status de sincronização
     */
    private fun determineSyncStatus(missingSequences: List<Long>): String {
        return when {
            missingSequences.isEmpty() -> "SYNCED"
            missingSequences.size <= 3 -> "SYNC_REQUIRED"
            else -> "ERROR"
        }
    }

    /**
     * Gera mensagem de sincronização
     */
    private fun generateSyncMessage(syncStatus: String, missingSequences: List<Long>): String {
        return when (syncStatus) {
            "SYNCED" -> "All sequences received and synchronized"
            "SYNC_REQUIRED" -> "Missing sequences detected: ${missingSequences.joinToString(", ")}. Retransmission required."
            else -> "Critical synchronization error. Multiple sequences missing: ${missingSequences.size} gaps detected."
        }
    }
}
