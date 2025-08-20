package com.schedkiwi.centraltelemetry.domain.valueobjects

import java.time.Instant
import java.util.*

/**
 * Value Object que representa metadados de um item processado
 */
data class ItemMetadata(
    val id: UUID = UUID.randomUUID(),
    val key: String?,
    val metadata: Map<String, Any?> = emptyMap(),
    val outcome: ItemOutcome = ItemOutcome.OK,
    val processedAt: Instant = Instant.now(),
    val processingTimeMs: Long = 0,
    val errorMessage: String? = null,
    val stackTrace: String? = null
)
