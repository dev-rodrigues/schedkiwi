package com.schedkiwi.schedulertelemetry.core

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import java.util.*

class SequenceManagerSimpleTest {

    private lateinit var sequenceManager: SequenceManager

    @BeforeEach
    fun setUp() {
        sequenceManager = SequenceManager()
    }

    @Test
    fun `deve gerar números de sequência incrementais`() {
        // Arrange
        val runId = UUID.randomUUID().toString()

        // Act
        val seq1 = sequenceManager.getNextSequenceNumber(runId)
        val seq2 = sequenceManager.getNextSequenceNumber(runId)
        val seq3 = sequenceManager.getNextSequenceNumber(runId)

        // Assert
        assertEquals(1L, seq1)
        assertEquals(2L, seq2)
        assertEquals(3L, seq3)
    }

    @Test
    fun `deve gerar números de sequência independentes por runId`() {
        // Arrange
        val runId1 = UUID.randomUUID().toString()
        val runId2 = UUID.randomUUID().toString()

        // Act
        val seq1 = sequenceManager.getNextSequenceNumber(runId1)
        val seq2 = sequenceManager.getNextSequenceNumber(runId2)
        val seq3 = sequenceManager.getNextSequenceNumber(runId1)

        // Assert
        assertEquals(1L, seq1)
        assertEquals(1L, seq2)
        assertEquals(2L, seq3)
    }

    @Test
    fun `deve calcular checksum SHA-256`() {
        // Arrange
        val data = "test data for checksum"
        val runId = UUID.randomUUID().toString()

        // Act
        val checksum = sequenceManager.calculateChecksum(data)

        // Assert
        assertNotNull(checksum)
        assertTrue(checksum.length == 64) // SHA-256 tem 64 caracteres hex
        assertTrue(checksum.matches(Regex("[a-f0-9]{64}")))
    }

    @Test
    fun `deve validar checksum correto`() {
        // Arrange
        val data = "test data for validation"
        val runId = UUID.randomUUID().toString()

        // Act
        val checksum = sequenceManager.calculateChecksum(data)
        val isValid = sequenceManager.validateChecksum(data, checksum)

        // Assert
        assertTrue(isValid)
    }

    @Test
    fun `deve rejeitar checksum incorreto`() {
        // Arrange
        val data = "test data for validation"
        val wrongChecksum = "a".repeat(64) // Checksum incorreto

        // Act
        val isValid = sequenceManager.validateChecksum(data, wrongChecksum)

        // Assert
        assertFalse(isValid)
    }

    @Test
    fun `deve armazenar mensagem no buffer circular`() {
        // Arrange
        val runId = UUID.randomUUID().toString()
        val message = "test message"

        // Act
        sequenceManager.storeMessage(runId, message)
        val retrievedMessage = sequenceManager.getMessage(runId, 1L)

        // Assert
        assertNotNull(retrievedMessage)
        assertEquals(message, retrievedMessage)
    }

    @Test
    fun `deve retornar estatísticas corretas`() {
        // Arrange
        val runId1 = UUID.randomUUID().toString()
        val runId2 = UUID.randomUUID().toString()

        // Act
        repeat(3) { sequenceManager.getNextSequenceNumber(runId1) }
        repeat(2) { sequenceManager.getNextSequenceNumber(runId2) }

        val stats = sequenceManager.getStats()

        // Assert
        assertNotNull(stats)
        assertTrue(stats.totalRunIds >= 2)
        assertTrue(stats.totalMessages >= 5)
        assertTrue(stats.averageMessagesPerRunId >= 2.0)
    }

    @Test
    fun `deve limpar estatísticas`() {
        // Arrange
        val runId = UUID.randomUUID().toString()
        sequenceManager.getNextSequenceNumber(runId)

        // Act
        sequenceManager.clearStats()
        val stats = sequenceManager.getStats()

        // Assert
        assertNotNull(stats)
        assertEquals(0, stats.totalRunIds)
        assertEquals(0, stats.totalMessages)
        assertEquals(0.0, stats.averageMessagesPerRunId)
    }
}
