package com.schedkiwi.centraltelemetry.infrastructure.persistence

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant
import java.util.*

/**
 * Entidade JPA para persistência de progresso de execução
 */
@Entity
@Table(name = "execution_progress")
class ExecutionProgressEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "sequence_number", nullable = false)
    val sequenceNumber: Long,

    @Column(name = "current_items", nullable = false)
    val currentItems: Long = 0,

    @Column(name = "total_items", nullable = false)
    val totalItems: Long = 0,

    @Column(name = "progress_percentage", nullable = false, precision = 5, scale = 2)
    val progressPercentage: BigDecimal = BigDecimal.ZERO,

    @Column(name = "status_message", columnDefinition = "TEXT")
    val statusMessage: String? = null,

    @Column(name = "captured_at", nullable = false)
    val capturedAt: Instant = Instant.now()
) {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "execution_id", nullable = false)
    var execution: ExecutionEntity? = null
}
