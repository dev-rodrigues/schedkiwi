package com.schedkiwi.centraltelemetry.application.usecases

import com.schedkiwi.centraltelemetry.application.dto.ProgressUpdateDto
import com.schedkiwi.centraltelemetry.domain.entities.Execution
import com.schedkiwi.centraltelemetry.domain.ports.ExecutionRepository
import com.schedkiwi.centraltelemetry.infrastructure.persistence.ExecutionProgressEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Instant
import java.util.*

/**
 * Use Case para processamento de atualizações de progresso
 */
@Service
@Transactional
class ProcessProgressUpdateUseCase(
    private val executionRepository: ExecutionRepository
) {

    /**
     * Processa uma atualização de progresso
     */
    fun execute(dto: ProgressUpdateDto): Boolean {
        // Verificar se a execução existe
        val execution = executionRepository.findByRunId(dto.runId)
            ?: throw IllegalArgumentException("Execução com runId '${dto.runId}' não encontrada")

        // Verificar se a execução ainda está rodando
        if (execution.status.name != "RUNNING") {
            throw IllegalStateException("Execução '${dto.runId}' não está mais rodando (status: ${execution.status})")
        }

        // Criar entidade de progresso
        val progressEntity = ExecutionProgressEntity(
            sequenceNumber = dto.sequenceNumber,
            currentItems = dto.currentItems,
            totalItems = dto.totalItems,
            progressPercentage = calculateProgressPercentage(dto.currentItems, dto.totalItems),
            statusMessage = dto.statusMessage,
            capturedAt = Instant.now()
        )

        // Adicionar metadados de progresso
        execution.putMetadata("progress_sequence_${dto.sequenceNumber}", dto.currentItems.toString())
        execution.putMetadata("progress_total_${dto.sequenceNumber}", dto.totalItems.toString())
        execution.putMetadata("progress_message_${dto.sequenceNumber}", dto.statusMessage ?: "")
        execution.putMetadata("last_progress_update", Instant.now().toString())

        // Salvar execução (que salvará o progresso via cascade)
        executionRepository.save(execution)

        return true
    }

    /**
     * Calcula a porcentagem de progresso
     */
    private fun calculateProgressPercentage(current: Long, total: Long): BigDecimal {
        return if (total > 0) {
            BigDecimal.valueOf(current).divide(BigDecimal.valueOf(total), 2, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100))
        } else {
            BigDecimal.ZERO
        }
    }
}
