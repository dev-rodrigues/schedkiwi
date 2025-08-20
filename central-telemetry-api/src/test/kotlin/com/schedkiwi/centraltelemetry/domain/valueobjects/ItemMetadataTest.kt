package com.schedkiwi.centraltelemetry.domain.valueobjects

import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Testes unitários para ItemMetadata value object
 */
class ItemMetadataTest {

    @Test
    fun `deve criar ItemMetadata com valores padrão`() {
        // Given
        val key = "item-123"

        // When
        val itemMetadata = ItemMetadata(key = key)

        // Then
        assertNotNull(itemMetadata.id)
        assertEquals(key, itemMetadata.key)
        assertTrue(itemMetadata.metadata.isEmpty())
        assertEquals(ItemOutcome.OK, itemMetadata.outcome)
        assertTrue(itemMetadata.processedAt.isBefore(Instant.now().plusSeconds(1)))
        assertEquals(0L, itemMetadata.processingTimeMs)
        assertNull(itemMetadata.errorMessage)
        assertNull(itemMetadata.stackTrace)
    }

    @Test
    fun `deve criar ItemMetadata com todos os valores`() {
        // Given
        val id = UUID.randomUUID()
        val key = "item-456"
        val metadata = mapOf("field1" to "value1", "field2" to 42, "field3" to true)
        val outcome = ItemOutcome.ERROR
        val processedAt = Instant.now().minusSeconds(60)
        val processingTimeMs = 1500L
        val errorMessage = "Processing failed"
        val stackTrace = "at com.example.Service.process(Service.kt:123)"

        // When
        val itemMetadata = ItemMetadata(
            id = id,
            key = key,
            metadata = metadata,
            outcome = outcome,
            processedAt = processedAt,
            processingTimeMs = processingTimeMs,
            errorMessage = errorMessage,
            stackTrace = stackTrace
        )

        // Then
        assertEquals(id, itemMetadata.id)
        assertEquals(key, itemMetadata.key)
        assertEquals(metadata, itemMetadata.metadata)
        assertEquals(outcome, itemMetadata.outcome)
        assertEquals(processedAt, itemMetadata.processedAt)
        assertEquals(processingTimeMs, itemMetadata.processingTimeMs)
        assertEquals(errorMessage, itemMetadata.errorMessage)
        assertEquals(stackTrace, itemMetadata.stackTrace)
    }

    @Test
    fun `deve criar ItemMetadata com key nula`() {
        // When
        val itemMetadata = ItemMetadata(key = null)

        // Then
        assertNull(itemMetadata.key)
        assertEquals(ItemOutcome.OK, itemMetadata.outcome)
        assertTrue(itemMetadata.metadata.isEmpty())
    }

    @Test
    fun `deve trabalhar com diferentes tipos de metadata`() {
        // Given
        val metadata = mapOf(
            "string" to "text value",
            "number" to 42,
            "boolean" to true,
            "null_value" to null,
            "list" to listOf("a", "b", "c"),
            "nested_map" to mapOf("inner" to "value")
        )

        // When
        val itemMetadata = ItemMetadata(
            key = "complex-item",
            metadata = metadata
        )

        // Then
        assertEquals("text value", itemMetadata.metadata["string"])
        assertEquals(42, itemMetadata.metadata["number"])
        assertEquals(true, itemMetadata.metadata["boolean"])
        assertEquals(null, itemMetadata.metadata["null_value"])
        assertEquals(listOf("a", "b", "c"), itemMetadata.metadata["list"])
        assertEquals(mapOf("inner" to "value"), itemMetadata.metadata["nested_map"])
    }

    @Test
    fun `deve trabalhar com todos os tipos de ItemOutcome`() {
        // Given & When
        val okItem = ItemMetadata(key = "ok-item", outcome = ItemOutcome.OK)
        val errorItem = ItemMetadata(key = "error-item", outcome = ItemOutcome.ERROR)
        val skippedItem = ItemMetadata(key = "skipped-item", outcome = ItemOutcome.SKIPPED)

        // Then
        assertEquals(ItemOutcome.OK, okItem.outcome)
        assertEquals(ItemOutcome.ERROR, errorItem.outcome)
        assertEquals(ItemOutcome.SKIPPED, skippedItem.outcome)
    }

    @Test
    fun `deve criar ItemMetadata com informações de erro`() {
        // Given
        val key = "error-item"
        val errorMessage = "Validation failed: missing required field 'name'"
        val stackTrace = """
            at com.example.ValidationService.validate(ValidationService.kt:45)
            at com.example.ProcessorService.processItem(ProcessorService.kt:123)
            at com.example.BatchProcessor.run(BatchProcessor.kt:89)
        """.trimIndent()

        // When
        val itemMetadata = ItemMetadata(
            key = key,
            outcome = ItemOutcome.ERROR,
            processingTimeMs = 250L,
            errorMessage = errorMessage,
            stackTrace = stackTrace
        )

        // Then
        assertEquals(key, itemMetadata.key)
        assertEquals(ItemOutcome.ERROR, itemMetadata.outcome)
        assertEquals(250L, itemMetadata.processingTimeMs)
        assertEquals(errorMessage, itemMetadata.errorMessage)
        assertEquals(stackTrace, itemMetadata.stackTrace)
    }

    @Test
    fun `deve criar ItemMetadata para item pulado`() {
        // Given
        val key = "skipped-item"
        val metadata = mapOf("reason" to "duplicate", "original_id" to 123)

        // When
        val itemMetadata = ItemMetadata(
            key = key,
            metadata = metadata,
            outcome = ItemOutcome.SKIPPED,
            processingTimeMs = 10L
        )

        // Then
        assertEquals(key, itemMetadata.key)
        assertEquals(ItemOutcome.SKIPPED, itemMetadata.outcome)
        assertEquals(10L, itemMetadata.processingTimeMs)
        assertEquals("duplicate", itemMetadata.metadata["reason"])
        assertEquals(123, itemMetadata.metadata["original_id"])
        assertNull(itemMetadata.errorMessage)
        assertNull(itemMetadata.stackTrace)
    }

    @Test
    fun `deve preservar imutabilidade com copy`() {
        // Given
        val originalItem = ItemMetadata(
            key = "original",
            outcome = ItemOutcome.OK,
            processingTimeMs = 100L
        )

        // When
        val modifiedItem = originalItem.copy(
            outcome = ItemOutcome.ERROR,
            errorMessage = "Something went wrong"
        )

        // Then
        assertEquals("original", originalItem.key)
        assertEquals(ItemOutcome.OK, originalItem.outcome)
        assertEquals(100L, originalItem.processingTimeMs)
        assertNull(originalItem.errorMessage)

        assertEquals("original", modifiedItem.key)
        assertEquals(ItemOutcome.ERROR, modifiedItem.outcome)
        assertEquals(100L, modifiedItem.processingTimeMs)
        assertEquals("Something went wrong", modifiedItem.errorMessage)
    }

    @Test
    fun `deve funcionar com processedAt em diferentes tempos`() {
        // Given
        val now = Instant.now()
        val pastTime = now.minusSeconds(3600)
        val futureTime = now.plusSeconds(3600)

        // When
        val currentItem = ItemMetadata(key = "current", processedAt = now)
        val pastItem = ItemMetadata(key = "past", processedAt = pastTime)
        val futureItem = ItemMetadata(key = "future", processedAt = futureTime)

        // Then
        assertEquals(now, currentItem.processedAt)
        assertEquals(pastTime, pastItem.processedAt)
        assertEquals(futureTime, futureItem.processedAt)
        
        assertTrue(pastItem.processedAt.isBefore(currentItem.processedAt))
        assertTrue(currentItem.processedAt.isBefore(futureItem.processedAt))
    }

    @Test
    fun `deve trabalhar com processingTimeMs diferentes`() {
        // Given & When
        val fastItem = ItemMetadata(key = "fast", processingTimeMs = 5L)
        val normalItem = ItemMetadata(key = "normal", processingTimeMs = 150L)
        val slowItem = ItemMetadata(key = "slow", processingTimeMs = 5000L)

        // Then
        assertEquals(5L, fastItem.processingTimeMs)
        assertEquals(150L, normalItem.processingTimeMs)
        assertEquals(5000L, slowItem.processingTimeMs)
        
        assertTrue(fastItem.processingTimeMs < normalItem.processingTimeMs)
        assertTrue(normalItem.processingTimeMs < slowItem.processingTimeMs)
    }
}
