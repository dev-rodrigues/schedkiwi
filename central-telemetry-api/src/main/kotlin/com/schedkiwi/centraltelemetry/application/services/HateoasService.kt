package com.schedkiwi.centraltelemetry.application.services

import com.schedkiwi.centraltelemetry.application.dto.*
import com.schedkiwi.centraltelemetry.infrastructure.controllers.FrontendController
import org.springframework.data.domain.PageRequest
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.Link
import org.springframework.hateoas.PagedModel
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn
import org.springframework.stereotype.Service
import java.util.*

/**
 * Serviço HATEOAS para gerenciar links de navegação
 * 
 * Este serviço adiciona links de navegação automáticos aos DTOs,
 * permitindo navegação fluida entre as entidades da API
 */
@Service
class HateoasService {
    
    companion object {
        private val DEFAULT_PAGEABLE = PageRequest.of(0, 20)
    }

    /**
     * Adiciona links HATEOAS a um ApplicationSummaryDto
     */
    fun addLinksToApplicationSummary(dto: ApplicationSummaryDto): EntityModel<ApplicationSummaryDto> {
        val entityModel = EntityModel.of(dto)
        
        // Link para detalhes da aplicação
        entityModel.add(
            linkTo(methodOn(FrontendController::class.java).getApplication(dto.id)).withRel("self")
        )
        
        // Link para status da aplicação
        entityModel.add(
            linkTo(methodOn(FrontendController::class.java).getApplicationStatus(dto.id)).withRel("status")
        )
        
        // Link para jobs da aplicação
        entityModel.add(
            linkTo(methodOn(FrontendController::class.java).getApplicationJobs(dto.id)).withRel("jobs")
        )
        
        // Link para execuções da aplicação
        entityModel.add(
            linkTo(methodOn(FrontendController::class.java).getApplicationExecutions(dto.id, null, null, null, DEFAULT_PAGEABLE)).withRel("executions")
        )
        
        // Link para métricas da aplicação
        entityModel.add(
            linkTo(methodOn(FrontendController::class.java).getApplicationMetrics(dto.id)).withRel("metrics")
        )
        
        return entityModel
    }

    /**
     * Adiciona links HATEOAS a um ApplicationDetailDto
     */
    fun addLinksToApplicationDetail(dto: ApplicationDetailDto): EntityModel<ApplicationDetailDto> {
        val entityModel = EntityModel.of(dto)
        
        // Link para a própria aplicação
        entityModel.add(
            linkTo(methodOn(FrontendController::class.java).getApplication(dto.id)).withSelfRel()
        )
        
        // Link para status
        entityModel.add(
            linkTo(methodOn(FrontendController::class.java).getApplicationStatus(dto.id)).withRel("status")
        )
        
        // Link para jobs
        entityModel.add(
            linkTo(methodOn(FrontendController::class.java).getApplicationJobs(dto.id)).withRel("jobs")
        )
        
        // Link para execuções
        entityModel.add(
            linkTo(methodOn(FrontendController::class.java).getApplicationExecutions(dto.id, null, null, null, DEFAULT_PAGEABLE)).withRel("executions")
        )
        
        // Link para métricas
        entityModel.add(
            linkTo(methodOn(FrontendController::class.java).getApplicationMetrics(dto.id)).withRel("metrics")
        )
        
        // Link para lista de aplicações
        entityModel.add(
            linkTo(methodOn(FrontendController::class.java).listApplications(DEFAULT_PAGEABLE)).withRel("applications")
        )
        
        return entityModel
    }

    /**
     * Adiciona links HATEOAS a um ExecutionSummaryDto
     */
    fun addLinksToExecutionSummary(dto: ExecutionSummaryDto): EntityModel<ExecutionSummaryDto> {
        val entityModel = EntityModel.of(dto)
        
        // Link para detalhes da execução
        entityModel.add(
            linkTo(methodOn(FrontendController::class.java).getExecution(dto.id)).withRel("self")
        )
        
        // Link para progresso da execução
        entityModel.add(
            linkTo(methodOn(FrontendController::class.java).getExecutionProgress(dto.id)).withRel("progress")
        )
        
        // Link para itens da execução
        entityModel.add(
            linkTo(methodOn(FrontendController::class.java).getExecutionItems(dto.id, DEFAULT_PAGEABLE)).withRel("items")
        )
        
        // Link para exceções da execução
        entityModel.add(
            linkTo(methodOn(FrontendController::class.java).getExecutionExceptions(dto.id)).withRel("exceptions")
        )
        
        // Link para execuções da aplicação
        entityModel.add(
            linkTo(methodOn(FrontendController::class.java).getApplicationExecutions(dto.id, null, null, null, DEFAULT_PAGEABLE)).withRel("application-executions")
        )
        
        return entityModel
    }

    /**
     * Adiciona links HATEOAS a um ExecutionDetailDto
     */
    fun addLinksToExecutionDetail(dto: ExecutionDetailDto): EntityModel<ExecutionDetailDto> {
        val entityModel = EntityModel.of(dto)
        
        // Link para a própria execução
        entityModel.add(
            linkTo(methodOn(FrontendController::class.java).getExecution(dto.id)).withSelfRel()
        )
        
        // Link para progresso
        entityModel.add(
            linkTo(methodOn(FrontendController::class.java).getExecutionProgress(dto.id)).withRel("progress")
        )
        
        // Link para itens
        entityModel.add(
            linkTo(methodOn(FrontendController::class.java).getExecutionItems(dto.id, DEFAULT_PAGEABLE)).withRel("items")
        )
        
        // Link para exceções
        entityModel.add(
            linkTo(methodOn(FrontendController::class.java).getExecutionExceptions(dto.id)).withRel("exceptions")
        )
        
        // Link para lista de execuções
        entityModel.add(
            linkTo(methodOn(FrontendController::class.java).listExecutions(null, null, null, null, null, null, null, DEFAULT_PAGEABLE)).withRel("executions")
        )
        
        return entityModel
    }

    /**
     * Adiciona links HATEOAS a uma lista paginada de aplicações
     */
    fun addLinksToApplicationsPage(page: PagedModel<EntityModel<ApplicationSummaryDto>>): PagedModel<EntityModel<ApplicationSummaryDto>> {
        // Link para métricas agregadas
        page.add(
            linkTo(methodOn(FrontendController::class.java).getAggregatedMetrics()).withRel("aggregated-metrics")
        )
        
        // Link para métricas de performance
        page.add(
            linkTo(methodOn(FrontendController::class.java).getPerformanceMetrics()).withRel("performance-metrics")
        )
        
        return page
    }

    /**
     * Adiciona links HATEOAS a uma lista paginada de execuções
     */
    fun addLinksToExecutionsPage(page: PagedModel<EntityModel<ExecutionSummaryDto>>): PagedModel<EntityModel<ExecutionSummaryDto>> {
        // Link para métricas agregadas
        page.add(
            linkTo(methodOn(FrontendController::class.java).getAggregatedMetrics()).withRel("aggregated-metrics")
        )
        
        // Link para métricas de performance
        page.add(
            linkTo(methodOn(FrontendController::class.java).getPerformanceMetrics()).withRel("performance-metrics")
        )
        
        return page
    }

    /**
     * Adiciona links HATEOAS a um JobSummaryDto
     */
    fun addLinksToJobSummary(dto: JobSummaryDto, applicationId: UUID): EntityModel<JobSummaryDto> {
        val entityModel = EntityModel.of(dto)
        
        // Link para execuções deste job
        entityModel.add(
            linkTo(methodOn(FrontendController::class.java).listExecutions(applicationId, dto.jobId, null, null, null, null, null, DEFAULT_PAGEABLE)).withRel("executions")
        )
        
        // Link para a aplicação
        entityModel.add(
            linkTo(methodOn(FrontendController::class.java).getApplication(applicationId)).withRel("application")
        )
        
        return entityModel
    }

    /**
     * Adiciona links HATEOAS a um ItemDetailDto
     */
    fun addLinksToItemDetail(dto: ItemDetailDto, executionId: UUID): EntityModel<ItemDetailDto> {
        val entityModel = EntityModel.of(dto)
        
        // Link para a execução
        entityModel.add(
            linkTo(methodOn(FrontendController::class.java).getExecution(executionId)).withRel("execution")
        )
        
        // Link para itens da execução
        entityModel.add(
            linkTo(methodOn(FrontendController::class.java).getExecutionItems(executionId, DEFAULT_PAGEABLE)).withRel("items")
        )
        
        return entityModel
    }

    /**
     * Adiciona links HATEOAS a um ExceptionDetailDto
     */
    fun addLinksToExceptionDetail(dto: ExceptionDetailDto, executionId: UUID): EntityModel<ExceptionDetailDto> {
        val entityModel = EntityModel.of(dto)
        
        // Link para a execução
        entityModel.add(
            linkTo(methodOn(FrontendController::class.java).getExecution(executionId)).withRel("execution")
        )
        
        // Link para exceções da execução
        entityModel.add(
            linkTo(methodOn(FrontendController::class.java).getExecutionExceptions(executionId)).withRel("exceptions")
        )
        
        return entityModel
    }

    /**
     * Adiciona links HATEOAS a métricas
     */
    fun addLinksToMetrics(metrics: Any, type: String): EntityModel<Any> {
        val entityModel = EntityModel.of(metrics)
        
        when (type) {
            "application" -> {
                entityModel.add(
                    linkTo(methodOn(FrontendController::class.java).listApplications(DEFAULT_PAGEABLE)).withRel("applications")
                )
                entityModel.add(
                    linkTo(methodOn(FrontendController::class.java).getAggregatedMetrics()).withRel("aggregated-metrics")
                )
            }
            "aggregated" -> {
                entityModel.add(
                    linkTo(methodOn(FrontendController::class.java).listApplications(DEFAULT_PAGEABLE)).withRel("applications")
                )
                entityModel.add(
                    linkTo(methodOn(FrontendController::class.java).getPerformanceMetrics()).withRel("performance-metrics")
                )
            }
            "performance" -> {
                entityModel.add(
                    linkTo(methodOn(FrontendController::class.java).listApplications(DEFAULT_PAGEABLE)).withRel("applications")
                )
                entityModel.add(
                    linkTo(methodOn(FrontendController::class.java).getAggregatedMetrics()).withRel("aggregated-metrics")
                )
            }
        }
        
        return entityModel
    }
}
