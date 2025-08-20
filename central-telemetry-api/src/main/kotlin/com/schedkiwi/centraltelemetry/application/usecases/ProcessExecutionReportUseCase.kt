package com.schedkiwi.centraltelemetry.application.usecases

import com.schedkiwi.centraltelemetry.application.dto.ExecutionReportDto
import com.schedkiwi.centraltelemetry.application.dto.ItemMetadataDto
import com.schedkiwi.centraltelemetry.application.dto.ExceptionInfoDto
import com.schedkiwi.centraltelemetry.domain.entities.Execution
import com.schedkiwi.centraltelemetry.domain.valueobjects.ItemMetadata
import com.schedkiwi.centraltelemetry.domain.valueobjects.ExceptionInfo
import com.schedkiwi.centraltelemetry.domain.valueobjects.ExceptionSeverity
import com.schedkiwi.centraltelemetry.domain.ports.ExecutionRepository
import com.schedkiwi.centraltelemetry.domain.ports.ApplicationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Use Case para processamento de relatório de execução
 */
@Service
@Transactional
class ProcessExecutionReportUseCase(
    private val executionRepository: ExecutionRepository,
    private val applicationRepository: ApplicationRepository
) {

    /**
     * Processa um relatório de execução
     */
    fun execute(dto: ExecutionReportDto): Execution {
        // Verificar se a aplicação existe
        val application = applicationRepository.findByAppName(dto.appName)
            ?: throw IllegalArgumentException("Aplicação '${dto.appName}' não encontrada")

        // Verificar se já existe execução com este runId
        val existingExecution = executionRepository.findByRunId(dto.runId)
        if (existingExecution != null) {
            throw IllegalArgumentException("Execução com runId '${dto.runId}' já existe")
        }

        // Criar entidade de domínio
        val execution = Execution(
            runId = dto.runId,
            jobId = dto.jobId,
            appName = dto.appName,
            status = dto.status,
            startTime = dto.startTime,
            endTime = dto.endTime,
            plannedTotal = dto.plannedTotal,
            processedItems = dto.processedItems,
            failedItems = dto.failedItems,
            skippedItems = dto.skippedItems,
            applicationId = application.id,
            scheduledJobId = UUID.randomUUID() // TODO: Buscar job real
        )

        // Adicionar metadados dos itens
        dto.itemMetadata.forEach { itemDto ->
            val itemMetadata = createItemMetadata(itemDto)
            execution.addItemMetadata(itemMetadata)
        }

        // Adicionar exceções
        dto.exceptions.forEach { exceptionDto ->
            val exceptionInfo = createExceptionInfo(exceptionDto)
            execution.addException(exceptionInfo)
        }

        // Adicionar metadados gerais
        dto.generalMetadata.forEach { (key, value) ->
            execution.putMetadata(key, value.toString())
        }

        // Salvar execução
        return executionRepository.save(execution)
    }

    /**
     * Cria metadados de item a partir do DTO
     */
    private fun createItemMetadata(dto: ItemMetadataDto): ItemMetadata {
        return ItemMetadata(
            key = dto.key,
            metadata = dto.metadata,
            outcome = dto.outcome,
            processingTimeMs = dto.processingTimeMs ?: 0,
            errorMessage = dto.errorMessage,
            stackTrace = dto.stackTrace
        )
    }

    /**
     * Cria informações de exceção a partir do DTO
     */
    private fun createExceptionInfo(dto: ExceptionInfoDto): ExceptionInfo {
        return ExceptionInfo(
            message = dto.message,
            type = dto.type,
            stackTrace = dto.stackTrace,
            severity = ExceptionSeverity.HIGH // TODO: Determinar severidade baseada no tipo
        )
    }
}
