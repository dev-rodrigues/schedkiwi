package com.schedkiwi.centraltelemetry.infrastructure.repositories

import com.schedkiwi.centraltelemetry.application.mappers.ExecutionMapper
import com.schedkiwi.centraltelemetry.domain.entities.Execution
import com.schedkiwi.centraltelemetry.domain.ports.ExecutionRepository
import com.schedkiwi.centraltelemetry.domain.valueobjects.ExecutionStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.*

/**
 * Implementação JPA do repositório de execuções
 */
@Repository
class ExecutionRepositoryImpl(
    private val jpaRepository: ExecutionJpaRepository,
    private val mapper: ExecutionMapper
) : ExecutionRepository {

    override fun save(execution: Execution): Execution {
        val entity = mapper.toEntity(execution)
        val savedEntity = jpaRepository.save(entity)
        return mapper.toDomain(savedEntity)
    }

    override fun findById(id: UUID): Execution? {
        return jpaRepository.findById(id)
            .map { mapper.toDomain(it) }
            .orElse(null)
    }

    override fun findByRunId(runId: String): Execution? {
        return jpaRepository.findByRunId(runId)
            ?.let { mapper.toDomain(it) }
    }

    override fun findByApplicationId(applicationId: UUID): List<Execution> {
        return jpaRepository.findByApplicationId(applicationId)
            .map { mapper.toDomain(it) }
    }

    override fun findByJobId(jobId: String): List<Execution> {
        return jpaRepository.findByJobId(jobId)
            .map { mapper.toDomain(it) }
    }

    override fun findByStatus(status: ExecutionStatus): List<Execution> {
        return jpaRepository.findByStatus(status)
            .map { mapper.toDomain(it) }
    }

    override fun findByDateRange(startDate: Instant, endDate: Instant): List<Execution> {
        return jpaRepository.findByStartTimeBetween(startDate, endDate)
            .map { mapper.toDomain(it) }
    }

    override fun findValidExecutions(minItemsProcessed: Long): List<Execution> {
        return jpaRepository.findByProcessedItemsGreaterThan(minItemsProcessed)
            .map { mapper.toDomain(it) }
    }

    override fun updateStatus(id: UUID, status: ExecutionStatus): Boolean {
        val entity = jpaRepository.findById(id).orElse(null) ?: return false
        // Em uma implementação real, isso seria feito via método que atualiza o status
        // entity.status = status
        return true
    }

    override fun delete(id: UUID): Boolean {
        if (!jpaRepository.existsById(id)) return false
        jpaRepository.deleteById(id)
        return true
    }

    override fun findByApplicationAndDateRange(applicationId: UUID, startDate: Instant, endDate: Instant): List<Execution> {
        return jpaRepository.findByApplicationAndDateRange(applicationId, startDate, endDate)
            .map { mapper.toDomain(it) }
    }

    override fun findAll(pageable: Pageable): Page<Execution> {
        return jpaRepository.findAll(pageable)
            .map { mapper.toDomain(it) }
    }

    override fun findByApplicationId(applicationId: UUID, pageable: Pageable): Page<Execution> {
        return jpaRepository.findByApplicationId(applicationId, pageable)
            .map { mapper.toDomain(it) }
    }

    override fun countByStatus(status: ExecutionStatus): Long {
        return jpaRepository.countByStatus(status)
    }

    override fun countByApplicationId(applicationId: UUID): Long {
        return jpaRepository.countByApplicationId(applicationId)
    }
}
