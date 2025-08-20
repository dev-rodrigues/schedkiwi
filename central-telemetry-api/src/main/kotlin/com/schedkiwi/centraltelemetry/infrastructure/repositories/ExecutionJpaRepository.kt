package com.schedkiwi.centraltelemetry.infrastructure.repositories

import com.schedkiwi.centraltelemetry.domain.valueobjects.ExecutionStatus
import com.schedkiwi.centraltelemetry.infrastructure.persistence.ExecutionEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.*

/**
 * Repositório Spring Data JPA para entidades Execution
 */
@Repository
interface ExecutionJpaRepository : JpaRepository<ExecutionEntity, UUID> {
    
    /**
     * Busca execução por runId
     */
    fun findByRunId(runId: String): ExecutionEntity?
    
    /**
     * Busca execuções por jobId
     */
    fun findByJobId(jobId: String): List<ExecutionEntity>
    
    /**
     * Busca execuções por status
     */
    fun findByStatus(status: ExecutionStatus): List<ExecutionEntity>
    
    /**
     * Busca execuções por aplicação
     */
    fun findByApplicationId(applicationId: UUID): List<ExecutionEntity>
    
    /**
     * Busca execuções por job agendado
     */
    fun findByScheduledJobId(scheduledJobId: UUID): List<ExecutionEntity>
    
    /**
     * Busca execuções por período
     */
    fun findByStartTimeBetween(startDate: Instant, endDate: Instant): List<ExecutionEntity>
    
    /**
     * Busca execuções válidas (com mais de X itens processados)
     */
    fun findByProcessedItemsGreaterThan(minItems: Long): List<ExecutionEntity>
    
    /**
     * Busca execuções por aplicação e período
     */
    @Query("SELECT e FROM ExecutionEntity e WHERE e.application.id = :applicationId AND e.startTime BETWEEN :startDate AND :endDate")
    fun findByApplicationAndDateRange(
        @Param("applicationId") applicationId: UUID,
        @Param("startDate") startDate: Instant,
        @Param("endDate") endDate: Instant
    ): List<ExecutionEntity>
    
    /**
     * Busca execuções com paginação
     */
    override fun findAll(pageable: Pageable): Page<ExecutionEntity>
    
    /**
     * Busca execuções por aplicação com paginação
     */
    fun findByApplicationId(applicationId: UUID, pageable: Pageable): Page<ExecutionEntity>
    
    /**
     * Conta execuções por status
     */
    fun countByStatus(status: ExecutionStatus): Long
    
    /**
     * Conta execuções por aplicação
     */
    fun countByApplicationId(applicationId: UUID): Long
}
