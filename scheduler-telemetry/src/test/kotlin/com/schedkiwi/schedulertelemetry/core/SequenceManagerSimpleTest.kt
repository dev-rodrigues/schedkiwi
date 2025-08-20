package com.schedkiwi.schedulertelemetry.core

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import kotlin.test.*

/**
 * Teste simples para SequenceManager
 */
class SequenceManagerSimpleTest {
    
    private lateinit var sequenceManager: SequenceManager
    
    @BeforeEach
    fun setUp() {
        sequenceManager = SequenceManager(
            outOfOrderToleranceMs = 1000L,
            bufferSize = 10
        )
    }
    
    @Test
    fun `deve gerar números sequenciais únicos`() {
        val runId1 = "run-1"
        val runId2 = "run-2"
        
        val seq1 = sequenceManager.getNextSequenceNumber(runId1)
        val seq2 = sequenceManager.getNextSequenceNumber(runId1)
        val seq3 = sequenceManager.getNextSequenceNumber(runId2)
        
        assertEquals(1L, seq1)
        assertEquals(2L, seq2)
        assertEquals(1L, seq3)
    }
    
    @Test
    fun `deve calcular checksum`() {
        val payload = "test-payload-123"
        val checksum = sequenceManager.calculateChecksum(payload)
        
        assertNotNull(checksum)
        assertTrue(checksum.length > 0)
        assertTrue(checksum.matches(Regex("[a-f0-9]{64}"))) // SHA-256 format
    }
    
    @Test
    fun `deve fornecer estatísticas`() {
        val stats = sequenceManager.getSequenceStats()
        
        assertNotNull(stats)
        assertTrue(stats.totalRunIds >= 0)
        assertTrue(stats.totalMessages >= 0)
        assertTrue(stats.averageMessagesPerRunId >= 0.0)
    }
}
