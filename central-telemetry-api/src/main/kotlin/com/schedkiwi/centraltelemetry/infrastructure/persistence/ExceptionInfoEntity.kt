package com.schedkiwi.centraltelemetry.infrastructure.persistence

import com.schedkiwi.centraltelemetry.domain.valueobjects.ExceptionSeverity
import jakarta.persistence.*
import java.time.Instant
import java.util.*

/**
 * Entidade JPA para persistência de informações de exceções
 */
@Entity
@Table(name = "execution_exceptions")
class ExceptionInfoEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),
    
    @Column(columnDefinition = "TEXT", nullable = false)
    val message: String,
    
    @Column(nullable = false)
    val type: String,
    
    @Column(name = "stack_trace", columnDefinition = "TEXT")
    val stackTrace: String? = null,
    
    @Column(name = "captured_at", nullable = false)
    val capturedAt: Instant = Instant.now(),
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val severity: ExceptionSeverity = ExceptionSeverity.ERROR
) {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "execution_id", nullable = false)
    var execution: ExecutionEntity? = null
}
