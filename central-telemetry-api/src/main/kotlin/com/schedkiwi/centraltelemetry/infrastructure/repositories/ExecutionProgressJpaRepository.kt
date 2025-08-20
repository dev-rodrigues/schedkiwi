package com.schedkiwi.centraltelemetry.infrastructure.repositories

import com.schedkiwi.centraltelemetry.infrastructure.persistence.ExecutionProgressEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

/**
 * Repositório JPA para progresso de execução
 */
@Repository
interface ExecutionProgressJpaRepository : JpaRepository<ExecutionProgressEntity, UUID> {

    /**
     * Busca progresso por ID da execução
     */
    fun findByExecutionId(executionId: UUID): List<ExecutionProgressEntity>

    /**
     * Busca progresso por ID da execução ordenado por sequência
     */
    fun findByExecutionIdOrderBySequenceNumberAsc(executionId: UUID): List<ExecutionProgressEntity>

    /**
     * Busca último progresso de uma execução
     */
    @Query("SELECT p FROM ExecutionProgressEntity p WHERE p.execution.id = :executionId ORDER BY p.sequenceNumber DESC")
    fun findLatestProgressByExecutionId(@Param("executionId") executionId: UUID): ExecutionProgressEntity?

    /**
     * Busca progresso por número de sequência e execução
     */
    fun findByExecutionIdAndSequenceNumber(executionId: UUID, sequenceNumber: Long): ExecutionProgressEntity?

    /**
     * Busca progresso por execução com sequência maior que o número especificado
     */
    @Query("SELECT p FROM ExecutionProgressEntity p WHERE p.execution.id = :executionId AND p.sequenceNumber > :sequenceNumber ORDER BY p.sequenceNumber ASC")
    fun findProgressAfterSequence(@Param("executionId") executionId: UUID, @Param("sequenceNumber") sequenceNumber: Long): List<ExecutionProgressEntity>

    /**
     * Busca progresso por execução com sequência menor ou igual ao número especificado
     */
    @Query("SELECT p FROM ExecutionProgressEntity p WHERE p.execution.id = :executionId AND p.sequenceNumber <= :sequenceNumber ORDER BY p.sequenceNumber DESC")
    fun findProgressUpToSequence(@Param("executionId") executionId: UUID, @Param("sequenceNumber") sequenceNumber: Long): List<ExecutionProgressEntity>

    /**
     * Verifica se existe progresso para uma execução
     */
    fun existsByExecutionId(executionId: UUID): Boolean

    /**
     * Conta progressos por execução
     */
    fun countByExecutionId(executionId: UUID): Long
}
