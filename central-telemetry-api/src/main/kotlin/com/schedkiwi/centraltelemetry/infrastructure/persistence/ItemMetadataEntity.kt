package com.schedkiwi.centraltelemetry.infrastructure.persistence

import com.schedkiwi.centraltelemetry.domain.valueobjects.ItemOutcome
import jakarta.persistence.*
import java.time.Instant
import java.util.*

/**
 * Entidade JPA para persistência de metadados de itens
 */
@Entity
@Table(name = "execution_item_metadata")
class ItemMetadataEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),
    
    @Column
    val key: String?,
    
    @Column(columnDefinition = "TEXT")
    val metadata: String, // JSON serializado
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val outcome: ItemOutcome,
    
    @Column(name = "processed_at", nullable = false)
    val processedAt: Instant = Instant.now(),
    
    @Column(name = "processing_time_ms")
    val processingTimeMs: Long? = null,
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    val errorMessage: String? = null,
    
    @Column(name = "stack_trace", columnDefinition = "TEXT")
    val stackTrace: String? = null
) {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "execution_id", nullable = false)
    var execution: ExecutionEntity? = null
}
