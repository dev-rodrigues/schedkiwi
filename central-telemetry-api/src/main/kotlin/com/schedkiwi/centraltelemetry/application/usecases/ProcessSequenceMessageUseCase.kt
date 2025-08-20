package com.schedkiwi.centraltelemetry.application.usecases

import com.schedkiwi.centraltelemetry.application.dto.SequenceMessageDto
import com.schedkiwi.centraltelemetry.domain.entities.Execution
import com.schedkiwi.centraltelemetry.domain.ports.ExecutionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * Use Case para processamento de mensagens de sequência
 */
@Service
@Transactional
class ProcessSequenceMessageUseCase(
    private val executionRepository: ExecutionRepository
) {

    /**
     * Processa uma mensagem de sequência
     */
    fun execute(dto: SequenceMessageDto): Boolean {
        // Verificar se a execução existe
        val execution = executionRepository.findByRunId(dto.runId)
            ?: throw IllegalArgumentException("Execução com runId '${dto.runId}' não encontrada")

        // Verificar se a execução ainda está rodando
        if (execution.status.name != "RUNNING") {
            throw IllegalStateException("Execução '${dto.runId}' não está mais rodando (status: ${execution.status})")
        }

        // Adicionar metadados de sequência
        execution.putMetadata("last_sequence_number", dto.sequenceNumber.toString())
        execution.putMetadata("last_sequence_timestamp", Instant.now().toString())
        execution.putMetadata("sequence_message", dto.message ?: "")

        // Salvar execução
        executionRepository.save(execution)

        return true
    }
}
