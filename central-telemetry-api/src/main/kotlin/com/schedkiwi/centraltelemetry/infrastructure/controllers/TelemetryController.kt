package com.schedkiwi.centraltelemetry.infrastructure.controllers

import com.schedkiwi.centraltelemetry.application.dto.*
import com.schedkiwi.centraltelemetry.application.usecases.*
import com.schedkiwi.centraltelemetry.domain.entities.Application
import com.schedkiwi.centraltelemetry.domain.entities.Execution
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Controller para endpoints de telemetria (Grupo 1 - Inbound)
 * Recebe dados da biblioteca scheduler-telemetry
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Telemetria", description = "Endpoints para receber dados de telemetria das aplicações")
class TelemetryController(
    private val registerApplicationUseCase: RegisterApplicationUseCase,
    private val processExecutionReportUseCase: ProcessExecutionReportUseCase,
    private val processProgressUpdateUseCase: ProcessProgressUpdateUseCase,
    private val processSequenceMessageUseCase: ProcessSequenceMessageUseCase,
    private val queryExecutionStatusUseCase: QueryExecutionStatusUseCase,
    private val queryExecutionSyncUseCase: QueryExecutionSyncUseCase
) {
    
    /**
     * POST /api/projects/register
     * Registra uma nova aplicação
     */
    @PostMapping("/projects/register")
    @Operation(
        summary = "Registrar aplicação",
        description = "Registra uma nova aplicação com seus jobs agendados"
    )
    fun registerApplication(
        @Valid @RequestBody dto: ApplicationRegistrationDto
    ): ResponseEntity<Application> {
        val application = registerApplicationUseCase.execute(dto)
        return ResponseEntity.status(HttpStatus.CREATED).body(application)
    }
    
    /**
     * POST /api/executions/report
     * Recebe relatório de execução
     */
    @PostMapping("/executions/report")
    @Operation(
        summary = "Relatório de execução",
        description = "Recebe relatório final de execução de um scheduler"
    )
    fun reportExecution(
        @Valid @RequestBody dto: ExecutionReportDto
    ): ResponseEntity<Execution> {
        val execution = processExecutionReportUseCase.execute(dto)
        return ResponseEntity.status(HttpStatus.CREATED).body(execution)
    }
    
    /**
     * POST /api/executions/progress
     * Recebe atualização de progresso
     */
    @PostMapping("/executions/progress")
    @Operation(
        summary = "Atualização de progresso",
        description = "Recebe atualização de progresso em tempo real"
    )
    fun updateProgress(
        @Valid @RequestBody dto: ProgressUpdateDto
    ): ResponseEntity<Map<String, String>> {
        val success = processProgressUpdateUseCase.execute(dto)
        return if (success) {
            ResponseEntity.ok(mapOf("status" to "progress_updated", "message" to "Progresso atualizado com sucesso"))
        } else {
            ResponseEntity.badRequest().body(mapOf("status" to "error", "message" to "Falha ao atualizar progresso"))
        }
    }
    
    /**
     * POST /api/executions/sequence
     * Recebe sincronização de sequência
     */
    @PostMapping("/executions/sequence")
    @Operation(
        summary = "Sincronização de sequência",
        description = "Recebe mensagem para garantir ordem sequencial"
    )
    fun syncSequence(
        @Valid @RequestBody dto: SequenceMessageDto
    ): ResponseEntity<Map<String, String>> {
        val success = processSequenceMessageUseCase.execute(dto)
        return if (success) {
            ResponseEntity.ok(mapOf("status" to "sequence_synced", "message" to "Sequência sincronizada com sucesso"))
        } else {
            ResponseEntity.badRequest().body(mapOf("status" to "error", "message" to "Falha ao sincronizar sequência"))
        }
    }
    
    /**
     * GET /api/executions/{runId}/status
     * Retorna status da execução
     */
    @GetMapping("/executions/{runId}/status")
    @Operation(
        summary = "Status da execução",
        description = "Retorna status atual de uma execução"
    )
    fun getExecutionStatus(
        @PathVariable runId: String
    ): ResponseEntity<ExecutionStatusResponseDto> {
        val status = queryExecutionStatusUseCase.execute(runId)
        return ResponseEntity.ok(status)
    }
    
    /**
     * GET /api/executions/{runId}/sync
     * Retorna estado de sincronização
     */
    @GetMapping("/executions/{runId}/sync")
    @Operation(
        summary = "Estado de sincronização",
        description = "Retorna estado de sincronização para uma execução"
    )
    fun getSyncStatus(
        @PathVariable runId: String
    ): ResponseEntity<ExecutionSyncResponseDto> {
        val syncStatus = queryExecutionSyncUseCase.execute(runId)
        return ResponseEntity.ok(syncStatus)
    }
}
