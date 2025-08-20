package com.schedkiwi.centraltelemetry.infrastructure.controllers

import com.schedkiwi.centraltelemetry.application.dto.*
import com.schedkiwi.centraltelemetry.application.services.FrontendQueryService
import com.schedkiwi.centraltelemetry.application.services.HateoasService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.PagedModel
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
    private val frontendQueryService: FrontendQueryService,
    private val hateoasService: HateoasService
) {
    
    /**
     * Helper para criar PagedModel com PageMetadata correto
     */
    private fun <T> createPagedModel(
        content: List<T>,
        pageable: Pageable,
        totalElements: Long
    ): PagedModel<T> {
        return PagedModel.of(
            content,
            org.springframework.hateoas.PagedModel.PageMetadata(
                pageable.pageSize.toLong(),
                pageable.pageNumber.toLong(),
                totalElements
            )
        )
    }

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
    ): ResponseEntity<PagedModel<EntityModel<ApplicationSummaryDto>>> {
        val applications = frontendQueryService.listApplications(pageable)
        
        // Converte para EntityModel com HATEOAS
        val entityModels = applications.content.map { dto ->
            hateoasService.addLinksToApplicationSummary(dto)
        }
        
        // Cria PagedModel com HATEOAS
        val pagedModel = createPagedModel(
            entityModels,
            applications.pageable,
            applications.totalElements
        )
        
        // Adiciona links da página
        val enhancedPage = hateoasService.addLinksToApplicationsPage(pagedModel)
        
        return ResponseEntity.ok(enhancedPage)
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
    fun getApplication(@PathVariable id: UUID): ResponseEntity<EntityModel<ApplicationDetailDto>> {
        val application = frontendQueryService.getApplicationById(id)
        val entityModel = hateoasService.addLinksToApplicationDetail(application)
        return ResponseEntity.ok(entityModel)
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
    fun getApplicationStatus(@PathVariable id: UUID): ResponseEntity<EntityModel<ApplicationStatusDto>> {
        val status = frontendQueryService.getApplicationStatus(id)
        val entityModel = EntityModel.of(status)
        
        // Adiciona links básicos
        entityModel.add(
            org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo(
                org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn(FrontendController::class.java).getApplication(id)
            ).withRel("application")
        )
        
        return ResponseEntity.ok(entityModel)
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
    fun getApplicationJobs(@PathVariable id: UUID): ResponseEntity<List<EntityModel<JobSummaryDto>>> {
        val jobs = frontendQueryService.getApplicationJobs(id)
        val entityModels = jobs.map { job ->
            hateoasService.addLinksToJobSummary(job, id)
        }
        return ResponseEntity.ok(entityModels)
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
    ): ResponseEntity<PagedModel<EntityModel<ExecutionSummaryDto>>> {
        val executions = frontendQueryService.getApplicationExecutions(
            applicationId = id,
            status = status,
            startDate = startDate,
            endDate = endDate,
            pageable = pageable
        )
        
        // Converte para EntityModel com HATEOAS
        val entityModels = executions.content.map { dto ->
            hateoasService.addLinksToExecutionSummary(dto)
        }
        
        // Cria PagedModel com HATEOAS
        val pagedModel = createPagedModel(
            entityModels,
            executions.pageable,
            executions.totalElements
        )
        
        // Adiciona links da página
        val enhancedPage = hateoasService.addLinksToExecutionsPage(pagedModel)
        
        return ResponseEntity.ok(enhancedPage)
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
    ): ResponseEntity<PagedModel<EntityModel<ExecutionSummaryDto>>> {
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
        
        // Converte para EntityModel com HATEOAS
        val entityModels = executions.content.map { dto ->
            hateoasService.addLinksToExecutionSummary(dto)
        }
        
        // Cria PagedModel com HATEOAS
        val pagedModel = createPagedModel(
            entityModels,
            executions.pageable,
            executions.totalElements
        )
        
        // Adiciona links da página
        val enhancedPage = hateoasService.addLinksToExecutionsPage(pagedModel)
        
        return ResponseEntity.ok(enhancedPage)
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
    fun getExecution(@PathVariable id: UUID): ResponseEntity<EntityModel<ExecutionDetailDto>> {
        val execution = frontendQueryService.getExecutionById(id)
        val entityModel = hateoasService.addLinksToExecutionDetail(execution)
        return ResponseEntity.ok(entityModel)
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
    fun getExecutionProgress(@PathVariable id: UUID): ResponseEntity<EntityModel<ExecutionProgressDto>> {
        val progress = frontendQueryService.getExecutionProgress(id)
        val entityModel = EntityModel.of(progress)
        
        // Adiciona links básicos
        entityModel.add(
            org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo(
                org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn(FrontendController::class.java).getExecution(id)
            ).withRel("execution")
        )
        
        return ResponseEntity.ok(entityModel)
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
    fun getApplicationMetrics(@PathVariable id: UUID): ResponseEntity<EntityModel<ApplicationMetricsDto>> {
        val metrics = frontendQueryService.getApplicationMetrics(id)
        val entityModel = hateoasService.addLinksToMetrics(metrics, "application")
        return ResponseEntity.ok(entityModel as EntityModel<ApplicationMetricsDto>)
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
    fun getAggregatedMetrics(): ResponseEntity<EntityModel<AggregatedMetricsDto>> {
        val metrics = frontendQueryService.getAggregatedMetrics()
        val entityModel = hateoasService.addLinksToMetrics(metrics, "aggregated")
        return ResponseEntity.ok(entityModel as EntityModel<AggregatedMetricsDto>)
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
    fun getPerformanceMetrics(): ResponseEntity<EntityModel<PerformanceMetricsDto>> {
        val metrics = frontendQueryService.getPerformanceMetrics()
        val entityModel = hateoasService.addLinksToMetrics(metrics, "performance")
        return ResponseEntity.ok(entityModel as EntityModel<PerformanceMetricsDto>)
    }
}
