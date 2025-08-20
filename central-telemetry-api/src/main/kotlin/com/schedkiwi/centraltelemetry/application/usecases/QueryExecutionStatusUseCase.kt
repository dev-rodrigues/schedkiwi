package com.schedkiwi.centraltelemetry.application.usecases

import com.schedkiwi.centraltelemetry.application.dto.ExecutionStatusResponseDto
import com.schedkiwi.centraltelemetry.domain.entities.Execution
import com.schedkiwi.centraltelemetry.domain.ports.ExecutionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant

/**
 * Use Case para consulta de status de execução
 */
@Service
@Transactional(readOnly = true)
class QueryExecutionStatusUseCase(
    private val executionRepository: ExecutionRepository
) {

    /**
     * Consulta o status de uma execução
     */
    fun execute(runId: String): ExecutionStatusResponseDto {
        // Buscar execução
        val execution = executionRepository.findByRunId(runId)
            ?: throw IllegalArgumentException("Execução com runId '$runId' não encontrada")

        // Calcular duração
        val duration = calculateDuration(execution.startTime, execution.endTime)

        // Calcular porcentagem de progresso
        val progressPercentage = calculateProgressPercentage(execution.processedItems, execution.plannedTotal)

        return ExecutionStatusResponseDto(
            runId = execution.runId,
            status = execution.status.name,
            startTime = execution.startTime,
            endTime = execution.endTime,
            duration = duration,
            plannedTotal = execution.plannedTotal,
            processedItems = execution.processedItems,
            failedItems = execution.failedItems,
            skippedItems = execution.skippedItems,
            progressPercentage = progressPercentage,
            lastUpdate = execution.endTime ?: execution.startTime
        )
    }

    /**
     * Calcula a duração da execução
     */
    private fun calculateDuration(startTime: Instant, endTime: Instant?): String {
        val end = endTime ?: Instant.now()
        val duration = Duration.between(startTime, end)
        
        return when {
            duration.toHours() > 0 -> "${duration.toHours()}h ${duration.toMinutesPart()}m ${duration.toSecondsPart()}s"
            duration.toMinutes() > 0 -> "${duration.toMinutes()}m ${duration.toSecondsPart()}s"
            else -> "${duration.seconds}s"
        }
    }

    /**
     * Calcula a porcentagem de progresso
     */
    private fun calculateProgressPercentage(processed: Long, planned: Long): Double {
        return if (planned > 0) {
            (processed.toDouble() / planned) * 100.0
        } else {
            0.0
        }
    }
}
