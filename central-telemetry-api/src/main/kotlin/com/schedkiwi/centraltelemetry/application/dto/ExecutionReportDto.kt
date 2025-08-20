package com.schedkiwi.centraltelemetry.application.dto

import com.schedkiwi.centraltelemetry.domain.valueobjects.ExecutionStatus
import com.schedkiwi.centraltelemetry.domain.valueobjects.ItemOutcome
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PositiveOrZero
import jakarta.validation.constraints.Size
import java.time.Instant

/**
 * DTO para relatório de execução
 */
data class ExecutionReportDto(
    @field:NotBlank(message = "ID do job é obrigatório")
    @field:Size(max = 255, message = "ID do job deve ter no máximo 255 caracteres")
    val jobId: String,
    
    @field:NotBlank(message = "Run ID é obrigatório")
    @field:Size(max = 255, message = "Run ID deve ter no máximo 255 caracteres")
    val runId: String,
    
    @field:NotBlank(message = "Nome da aplicação é obrigatório")
    @field:Size(max = 255, message = "Nome da aplicação deve ter no máximo 255 caracteres")
    val appName: String,
    
    @field:NotNull(message = "Status é obrigatório")
    val status: ExecutionStatus,
    
    @field:NotNull(message = "Data de início é obrigatória")
    val startTime: Instant,
    
    @field:NotNull(message = "Data de fim é obrigatória")
    val endTime: Instant,
    
    @field:NotNull(message = "Total planejado é obrigatório")
    @field:PositiveOrZero(message = "Total planejado deve ser zero ou positivo")
    val plannedTotal: Long,
    
    @field:NotNull(message = "Itens processados é obrigatório")
    @field:PositiveOrZero(message = "Itens processados deve ser zero ou positivo")
    val processedItems: Long,
    
    @field:NotNull(message = "Itens falhados é obrigatório")
    @field:PositiveOrZero(message = "Itens falhados deve ser zero ou positivo")
    val failedItems: Long,
    
    @field:NotNull(message = "Itens pulados é obrigatório")
    @field:PositiveOrZero(message = "Itens pulados deve ser zero ou positivo")
    val skippedItems: Long,
    
    @field:NotNull(message = "Metadados dos itens são obrigatórios")
    val itemMetadata: List<ItemMetadataDto>,
    
    @field:NotNull(message = "Exceções são obrigatórias")
    val exceptions: List<ExceptionInfoDto>,
    
    @field:NotNull(message = "Metadados gerais são obrigatórios")
    val generalMetadata: Map<String, Any?>
)

/**
 * DTO para metadados de item
 */
data class ItemMetadataDto(
    @field:Size(max = 255, message = "Chave deve ter no máximo 255 caracteres")
    val key: String?,
    
    @field:NotNull(message = "Metadados são obrigatórios")
    val metadata: Map<String, Any?>,
    
    @field:NotNull(message = "Resultado é obrigatório")
    val outcome: ItemOutcome,
    
    @field:PositiveOrZero(message = "Tempo de processamento deve ser zero ou positivo")
    val processingTimeMs: Long? = null,
    
    @field:Size(max = 1000, message = "Mensagem de erro deve ter no máximo 1000 caracteres")
    val errorMessage: String? = null,
    
    @field:Size(max = 10000, message = "Stack trace deve ter no máximo 10000 caracteres")
    val stackTrace: String? = null
)

/**
 * DTO para informações de exceção
 */
data class ExceptionInfoDto(
    @field:NotBlank(message = "Mensagem é obrigatória")
    @field:Size(max = 1000, message = "Mensagem deve ter no máximo 1000 caracteres")
    val message: String,
    
    @field:NotBlank(message = "Tipo é obrigatório")
    @field:Size(max = 255, message = "Tipo deve ter no máximo 255 caracteres")
    val type: String,
    
    @field:Size(max = 10000, message = "Stack trace deve ter no máximo 10000 caracteres")
    val stackTrace: String? = null
)
