package com.schedkiwi.centraltelemetry.domain.ports

import com.schedkiwi.centraltelemetry.domain.entities.Execution
import com.schedkiwi.centraltelemetry.domain.valueobjects.ExecutionStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.Instant
import java.util.*

/**
 * Port (interface) para repositório de execuções
 */
interface ExecutionRepository {
    fun save(execution: Execution): Execution
    fun findById(id: UUID): Execution?
    fun findByRunId(runId: String): Execution?
    fun findByApplicationId(applicationId: UUID): List<Execution>
    fun findByJobId(jobId: String): List<Execution>
    fun findByStatus(status: ExecutionStatus): List<Execution>
    fun findByDateRange(startDate: Instant, endDate: Instant): List<Execution>
    fun findValidExecutions(minItemsProcessed: Long): List<Execution>
    fun updateStatus(id: UUID, status: ExecutionStatus): Boolean
    fun delete(id: UUID): Boolean
    fun findByApplicationAndDateRange(applicationId: UUID, startDate: Instant, endDate: Instant): List<Execution>
    fun findAll(pageable: Pageable): Page<Execution>
    fun findByApplicationId(applicationId: UUID, pageable: Pageable): Page<Execution>
    fun countByStatus(status: ExecutionStatus): Long
    fun countByApplicationId(applicationId: UUID): Long
}
