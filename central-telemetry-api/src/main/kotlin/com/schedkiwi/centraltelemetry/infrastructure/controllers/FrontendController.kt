package com.schedkiwi.centraltelemetry.infrastructure.controllers

import com.schedkiwi.centraltelemetry.application.dto.*
import com.schedkiwi.centraltelemetry.application.services.FrontendQueryService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.util.*

/**
 * Controller para endpoints do frontend (Grupo 2 - Outbound)
 * 
 * Este controller fornece dados processados e agregados para o frontend
 * consumir e exibir informações sobre aplicações, execuções e métricas
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Frontend", description = "Endpoints para consulta de dados pelo frontend")
class FrontendController(
    private val frontendQueryService: FrontendQueryService
) {

    /**
     * GET /api/applications
     * Lista todas as aplicações integradas com paginação
     */
    @GetMapping("/applications")
    @Operation(
        summary = "Listar aplicações",
        description = "Lista todas as aplicações integradas com paginação"
    )
    fun listApplications(
        pageable: Pageable
    ): ResponseEntity<Page<ApplicationSummaryDto>> {
        val applications = frontendQueryService.listApplications(pageable)
        return ResponseEntity.ok(applications)
    }

    /**
     * GET /api/applications/{id}
     * Acessa uma aplicação específica pelo ID
     */
    @GetMapping("/applications/{id}")
    @Operation(
        summary = "Obter aplicação",
        description = "Retorna detalhes de uma aplicação específica"
    )
    fun getApplication(@PathVariable id: UUID): ResponseEntity<ApplicationDetailDto> {
        val application = frontendQueryService.getApplicationById(id)
        return ResponseEntity.ok(application)
    }

    /**
     * GET /api/applications/{id}/status
     * Status atual da aplicação
     */
    @GetMapping("/applications/{id}/status")
    @Operation(
        summary = "Status da aplicação",
        description = "Retorna o status atual de uma aplicação"
    )
    fun getApplicationStatus(@PathVariable id: UUID): ResponseEntity<ApplicationStatusDto> {
        val status = frontendQueryService.getApplicationStatus(id)
        return ResponseEntity.ok(status)
    }

    /**
     * GET /api/applications/{id}/jobs
     * Lista jobs agendados da aplicação
     */
    @GetMapping("/applications/{id}/jobs")
    @Operation(
        summary = "Jobs da aplicação",
        description = "Lista todos os jobs agendados de uma aplicação específica"
    )
    fun getApplicationJobs(@PathVariable id: UUID): ResponseEntity<List<JobSummaryDto>> {
        val jobs = frontendQueryService.getApplicationJobs(id)
        return ResponseEntity.ok(jobs)
    }

    /**
     * GET /api/applications/{id}/executions
     * Lista execuções de uma aplicação específica
     */
    @GetMapping("/applications/{id}/executions")
    @Operation(
        summary = "Execuções da aplicação",
        description = "Lista execuções de uma aplicação com filtros e paginação"
    )
    fun getApplicationExecutions(
        @PathVariable id: UUID,
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) startDate: LocalDateTime?,
        @RequestParam(required = false) endDate: LocalDateTime?,
        pageable: Pageable
    ): ResponseEntity<Page<ExecutionSummaryDto>> {
        val executions = frontendQueryService.getApplicationExecutions(
            applicationId = id,
            status = status,
            startDate = startDate,
            endDate = endDate,
            pageable = pageable
        )
        return ResponseEntity.ok(executions)
    }

    /**
     * GET /api/executions
     * Lista todas as execuções (com filtros)
     */
    @GetMapping("/executions")
    @Operation(
        summary = "Listar execuções",
        description = "Lista todas as execuções com filtros, paginação e ordenação"
    )
    fun listExecutions(
        @RequestParam(required = false) applicationId: UUID?,
        @RequestParam(required = false) jobId: String?,
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) startDate: LocalDateTime?,
        @RequestParam(required = false) endDate: LocalDateTime?,
        @RequestParam(required = false) minItemsPerSecond: Double?,
        @RequestParam(required = false) maxErrorRate: Double?,
        pageable: Pageable
    ): ResponseEntity<Page<ExecutionSummaryDto>> {
        val executions = frontendQueryService.listExecutions(
            applicationId = applicationId,
            jobId = jobId,
            status = status,
            startDate = startDate,
            endDate = endDate,
            minItemsPerSecond = minItemsPerSecond,
            maxErrorRate = maxErrorRate,
            pageable = pageable
        )
        return ResponseEntity.ok(executions)
    }

    /**
     * GET /api/executions/{id}
     * Acessa uma execução específica pelo ID
     */
    @GetMapping("/executions/{id}")
    @Operation(
        summary = "Obter execução",
        description = "Retorna detalhes de uma execução específica"
    )
    fun getExecution(@PathVariable id: UUID): ResponseEntity<ExecutionDetailDto> {
        val execution = frontendQueryService.getExecutionById(id)
        return ResponseEntity.ok(execution)
    }

    /**
     * GET /api/executions/{id}/progress
     * Progresso detalhado de uma execução
     */
    @GetMapping("/executions/{id}/progress")
    @Operation(
        summary = "Progresso da execução",
        description = "Retorna o progresso detalhado de uma execução"
    )
    fun getExecutionProgress(@PathVariable id: UUID): ResponseEntity<ExecutionProgressDto> {
        val progress = frontendQueryService.getExecutionProgress(id)
        return ResponseEntity.ok(progress)
    }

    /**
     * GET /api/executions/{id}/items
     * Itens processados em uma execução
     */
    @GetMapping("/executions/{id}/items")
    @Operation(
        summary = "Itens da execução",
        description = "Lista itens processados em uma execução com paginação"
    )
    fun getExecutionItems(
        @PathVariable id: UUID,
        pageable: Pageable
    ): ResponseEntity<Page<ItemDetailDto>> {
        val items = frontendQueryService.getExecutionItems(id, pageable)
        return ResponseEntity.ok(items)
    }

    /**
     * GET /api/executions/{id}/exceptions
     * Exceções capturadas em uma execução
     */
    @GetMapping("/executions/{id}/exceptions")
    @Operation(
        summary = "Exceções da execução",
        description = "Lista exceções capturadas em uma execução"
    )
    fun getExecutionExceptions(@PathVariable id: UUID): ResponseEntity<List<ExceptionDetailDto>> {
        val exceptions = frontendQueryService.getExecutionExceptions(id)
        return ResponseEntity.ok(exceptions)
    }

    /**
     * GET /api/applications/{id}/metrics
     * Métricas agregadas de uma aplicação
     */
    @GetMapping("/applications/{id}/metrics")
    @Operation(
        summary = "Métricas da aplicação",
        description = "Retorna métricas agregadas de uma aplicação específica"
    )
    fun getApplicationMetrics(@PathVariable id: UUID): ResponseEntity<ApplicationMetricsDto> {
        val metrics = frontendQueryService.getApplicationMetrics(id)
        return ResponseEntity.ok(metrics)
    }

    /**
     * GET /api/metrics/aggregated
     * Métricas agregadas de todas as aplicações
     */
    @GetMapping("/metrics/aggregated")
    @Operation(
        summary = "Métricas agregadas",
        description = "Retorna métricas agregadas de todas as aplicações"
    )
    fun getAggregatedMetrics(): ResponseEntity<AggregatedMetricsDto> {
        val metrics = frontendQueryService.getAggregatedMetrics()
        return ResponseEntity.ok(metrics)
    }

    /**
     * GET /api/metrics/performance
     * Métricas de performance globais
     */
    @GetMapping("/metrics/performance")
    @Operation(
        summary = "Métricas de performance",
        description = "Retorna métricas de performance globais do sistema"
    )
    fun getPerformanceMetrics(): ResponseEntity<PerformanceMetricsDto> {
        val metrics = frontendQueryService.getPerformanceMetrics()
        return ResponseEntity.ok(metrics)
    }
}
