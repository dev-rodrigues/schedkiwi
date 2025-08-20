package com.schedkiwi.schedulertelemetry.core

import java.security.MessageDigest
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * Gerenciador de sequência para garantir ordem cronológica das mensagens.
 * 
 * Este componente é responsável por:
 * - Gerar números de sequência únicos por runId
 * - Validar checksums SHA-256 para integridade
 * - Gerenciar tolerância para mensagens fora de ordem
 * - Manter buffer circular para sincronização
 */
class SequenceManager(
    private val outOfOrderToleranceMs: Long = 1000L,
    private val bufferSize: Int = 1000
) {
    
    /**
     * Contadores de sequência por runId
     */
    private val sequenceCounters = ConcurrentHashMap<String, AtomicLong>()
    
    /**
     * Buffer circular para últimas mensagens por runId
     */
    private val messageBuffers = ConcurrentHashMap<String, CircularBuffer<SequencedMessage>>()
    
    /**
     * Gera o próximo número de sequência para um runId específico
     */
    fun getNextSequenceNumber(runId: String): Long {
        return sequenceCounters
            .computeIfAbsent(runId) { AtomicLong(0L) }
            .incrementAndGet()
    }
    
    /**
     * Valida se uma mensagem está na ordem correta
     */
    fun validateSequence(runId: String, sequenceNumber: Long, timestamp: Instant): SequenceValidationResult {
        val expectedSequence = sequenceCounters[runId]?.get() ?: 0L
        
        return when {
            sequenceNumber == expectedSequence + 1 -> {
                // Sequência correta
                SequenceValidationResult.VALID
            }
            sequenceNumber <= expectedSequence -> {
                // Mensagem duplicada ou já processada
                SequenceValidationResult.DUPLICATE
            }
            sequenceNumber > expectedSequence + 1 -> {
                // Gap na sequência
                SequenceValidationResult.GAP
            }
            else -> {
                // Sequência válida
                SequenceValidationResult.VALID
            }
        }
    }
    
    /**
     * Valida o checksum SHA-256 de uma mensagem
     */
    fun validateChecksum(payload: String, expectedChecksum: String): Boolean {
        val actualChecksum = calculateChecksum(payload)
        return actualChecksum == expectedChecksum
    }
    
    /**
     * Armazena uma mensagem no buffer circular
     */
    fun storeMessage(runId: String, message: String) {
        val sequencedMessage = SequencedMessage(
            runId = runId,
            sequenceNumber = getNextSequenceNumber(runId),
            timestamp = Instant.now(),
            messageType = "TEST",
            payload = message,
            checksum = calculateChecksum(message)
        )
        addMessageToBuffer(runId, sequencedMessage)
    }

    /**
     * Obtém uma mensagem do buffer por runId e sequência
     */
    fun getMessage(runId: String, sequenceNumber: Long): String? {
        val buffer = messageBuffers[runId] ?: return null
        return buffer.getAll()
            .find { it.sequenceNumber == sequenceNumber }
            ?.payload
    }

    /**
     * Obtém estatísticas gerais de todos os runIds
     */
    fun getStats(): GeneralStats {
        val totalRunIds = sequenceCounters.size
        val totalMessages = sequenceCounters.values.sumOf { it.get() }
        val averageMessagesPerRunId = if (totalRunIds > 0) totalMessages.toDouble() / totalRunIds else 0.0

        return GeneralStats(
            totalRunIds = totalRunIds,
            totalMessages = totalMessages,
            averageMessagesPerRunId = averageMessagesPerRunId
        )
    }

    /**
     * Limpa todas as estatísticas
     */
    fun clearStats() {
        sequenceCounters.clear()
        messageBuffers.clear()
    }
    
    /**
     * Calcula o checksum SHA-256 de um payload
     */
    fun calculateChecksum(payload: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(payload.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Adiciona uma mensagem ao buffer circular
     */
    fun addMessageToBuffer(runId: String, message: SequencedMessage) {
        val buffer = messageBuffers.computeIfAbsent(runId) { CircularBuffer(bufferSize) }
        buffer.add(message)
    }
    
    /**
     * Obtém mensagens do buffer para sincronização
     */
    fun getMessagesFromBuffer(runId: String, fromSequence: Long): List<SequencedMessage> {
        val buffer = messageBuffers[runId] ?: return emptyList()
        return buffer.getAll().filter { it.sequenceNumber >= fromSequence }
    }
    
    /**
     * Obtém estatísticas de sequência para um runId
     */
    fun getSequenceStats(runId: String): SequenceStats? {
        val counter = sequenceCounters[runId] ?: return null
        val buffer = messageBuffers[runId]
        
        return SequenceStats(
            runId = runId,
            lastSequenceNumber = counter.get(),
            bufferSize = buffer?.size() ?: 0,
            bufferCapacity = bufferSize,
            outOfOrderToleranceMs = outOfOrderToleranceMs
        )
    }
    
    /**
     * Limpa recursos para um runId específico
     */
    fun cleanupRunId(runId: String) {
        sequenceCounters.remove(runId)
        messageBuffers.remove(runId)
    }
    
    /**
     * Limpa todos os recursos
     */
    fun cleanup() {
        sequenceCounters.clear()
        messageBuffers.clear()
    }
}

/**
 * Resultado da validação de sequência
 */
enum class SequenceValidationResult {
    VALID,      // Sequência válida
    DUPLICATE,  // Mensagem duplicada
    GAP,        // Gap na sequência
    INVALID     // Sequência inválida
}

/**
 * Mensagem com numeração sequencial
 */
data class SequencedMessage(
    val runId: String,
    val sequenceNumber: Long,
    val timestamp: Instant,
    val messageType: String,
    val payload: String,
    val checksum: String
)

/**
 * Estatísticas de sequência para um runId
 */
data class SequenceStats(
    val runId: String,
    val lastSequenceNumber: Long,
    val bufferSize: Int,
    val bufferCapacity: Int,
    val outOfOrderToleranceMs: Long
)

/**
 * Estatísticas gerais de todos os runIds
 */
data class GeneralStats(
    val totalRunIds: Int,
    val totalMessages: Long,
    val averageMessagesPerRunId: Double
)

/**
 * Buffer circular thread-safe para armazenar mensagens
 */
class CircularBuffer<T>(private val capacity: Int) {
    private val buffer = ArrayDeque<T>(capacity)
    private val lock = Any()
    
    fun add(element: T) {
        synchronized(lock) {
            if (buffer.size >= capacity) {
                buffer.removeFirst()
            }
            buffer.addLast(element)
        }
    }
    
    fun getAll(): List<T> {
        synchronized(lock) {
            return buffer.toList()
        }
    }
    
    fun size(): Int {
        synchronized(lock) {
            return buffer.size
        }
    }
    
    fun clear() {
        synchronized(lock) {
            buffer.clear()
        }
    }
}
