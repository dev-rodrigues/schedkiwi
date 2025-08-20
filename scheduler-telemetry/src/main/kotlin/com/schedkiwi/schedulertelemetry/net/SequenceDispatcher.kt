package com.schedkiwi.schedulertelemetry.net

import com.schedkiwi.schedulertelemetry.core.SequenceManager
import com.schedkiwi.schedulertelemetry.core.SequenceValidationResult
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.concurrent.CompletableFuture
import java.util.concurrent.PriorityBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Dispatcher para mensagens sequenciais com garantia de ordem.
 * 
 * Este componente mantém uma fila ordenada por sequenceNumber e
 * envia mensagens na ordem correta para o Gerenciador Central.
 */
class SequenceDispatcher(
    private val httpClientFactory: HttpClientFactory,
    private val sequenceManager: SequenceManager,
    private val baseUrl: String,
    private val endpoint: String,
    private val headers: Map<String, String> = emptyMap(),
    private val maxRetries: Int = 5,
    private val baseBackoffMs: Long = 500L
) {
    
    private val logger = LoggerFactory.getLogger(SequenceDispatcher::class.java)
    
    /**
     * Fila ordenada por sequenceNumber para garantir ordem de envio
     */
    private val messageQueue = PriorityBlockingQueue<QueuedMessage>(1000) { msg1, msg2 ->
        msg1.sequenceNumber.compareTo(msg2.sequenceNumber)
    }
    
    /**
     * Flag para controlar se o dispatcher está ativo
     */
    private val isActive = AtomicBoolean(true)
    
    /**
     * Worker thread para processar mensagens da fila
     */
    private val workerThread = Thread {
        processMessageQueue()
    }.apply {
        name = "SequenceDispatcher-Worker"
        isDaemon = true
        start()
    }
    
    /**
     * Enfileira uma mensagem para envio sequencial
     */
    fun enqueueMessage(
        runId: String,
        message: OutboundMessage,
        priority: MessagePriority = MessagePriority.NORMAL
    ): CompletableFuture<Boolean> {
        if (!isActive.get()) {
            return CompletableFuture.completedFuture(false)
        }
        
        val queuedMessage = QueuedMessage(
            message = message,
            sequenceNumber = message.sequenceNumber,
            priority = priority,
            enqueueTime = Instant.now(),
            retryCount = 0
        )
        
        val success = messageQueue.offer(queuedMessage)
        
        if (success) {
            logger.debug("Mensagem enfileirada: runId={}, sequence={}, priority={}", 
                runId, message.sequenceNumber, priority)
        } else {
            logger.warn("Falha ao enfileirar mensagem: runId={}, sequence={}", 
                runId, message.sequenceNumber)
        }
        
        return CompletableFuture.completedFuture(success)
    }
    
    /**
     * Processa mensagens da fila em ordem sequencial
     */
    private fun processMessageQueue() {
        while (isActive.get()) {
            try {
                val queuedMessage = messageQueue.take()
                processMessage(queuedMessage)
            } catch (e: InterruptedException) {
                logger.info("SequenceDispatcher worker thread interrompida")
                break
            } catch (e: Exception) {
                logger.error("Erro ao processar mensagem da fila", e)
            }
        }
    }
    
    /**
     * Processa uma mensagem individual
     */
    private fun processMessage(queuedMessage: QueuedMessage) {
        val message = queuedMessage.message
        val runId = message.runId
        
        try {
            // Valida a sequência antes do envio
            val validationResult = sequenceManager.validateSequence(
                runId, 
                message.sequenceNumber, 
                message.timestamp
            )
            
            when (validationResult) {
                SequenceValidationResult.VALID -> {
                    // Envia a mensagem
                    sendMessage(queuedMessage)
                }
                SequenceValidationResult.DUPLICATE -> {
                    logger.debug("Mensagem duplicada ignorada: runId={}, sequence={}", 
                        runId, message.sequenceNumber)
                }
                SequenceValidationResult.GAP -> {
                    logger.warn("Gap na sequência detectado: runId={}, sequence={}", 
                        runId, message.sequenceNumber)
                    // Recoloca a mensagem no topo da fila para retry
                    messageQueue.offer(queuedMessage)
                }
                SequenceValidationResult.INVALID -> {
                    logger.error("Mensagem inválida: runId={}, sequence={}", 
                        runId, message.sequenceNumber)
                }
            }
            
        } catch (e: Exception) {
            logger.error("Erro ao processar mensagem: runId={}, sequence={}", 
                runId, message.sequenceNumber, e)
            
            // Tenta retry se ainda não excedeu o limite
            if (queuedMessage.retryCount < maxRetries) {
                handleRetry(queuedMessage)
            } else {
                logger.error("Mensagem falhou após {} tentativas: runId={}, sequence={}", 
                    maxRetries, runId, message.sequenceNumber)
                // TODO: Implementar dead letter queue
            }
        }
    }
    
    /**
     * Envia uma mensagem para o Gerenciador Central
     */
    private fun sendMessage(queuedMessage: QueuedMessage) {
        val message = queuedMessage.message
        val url = "$baseUrl$endpoint"
        
        httpClientFactory.postMessage(url, message, headers)
            .thenAccept { response ->
                if (response.statusCode() in 200..299) {
                    logger.debug("Mensagem enviada com sucesso: runId={}, sequence={}, status={}", 
                        message.runId, message.sequenceNumber, response.statusCode())
                    
                    // Adiciona a mensagem ao buffer para sincronização
                    sequenceManager.addMessageToBuffer(
                        message.runId,
                        com.schedkiwi.schedulertelemetry.core.SequencedMessage(
                            runId = message.runId,
                            sequenceNumber = message.sequenceNumber,
                            timestamp = message.timestamp,
                            messageType = message.javaClass.simpleName,
                            payload = message.toString(),
                            checksum = message.checksum
                        )
                    )
                } else {
                    throw RuntimeException("HTTP ${response.statusCode()}: ${response.body()}")
                }
            }
            .exceptionally { throwable ->
                logger.error("Falha ao enviar mensagem: runId={}, sequence={}", 
                    message.runId, message.sequenceNumber, throwable)
                throw throwable
            }
    }
    
    /**
     * Trata retry de mensagens falhadas
     */
    private fun handleRetry(queuedMessage: QueuedMessage) {
        val retryCount = queuedMessage.retryCount + 1
        val backoffMs = baseBackoffMs * (1L shl (retryCount - 1)) // Backoff exponencial
        
        logger.info("Agendando retry {} para mensagem: runId={}, sequence={}, backoff={}ms", 
            retryCount, queuedMessage.message.runId, queuedMessage.sequenceNumber, backoffMs)
        
        // Agenda retry com backoff exponencial
        Thread {
            try {
                Thread.sleep(backoffMs)
                val retryMessage = queuedMessage.copy(retryCount = retryCount)
                messageQueue.offer(retryMessage)
            } catch (e: InterruptedException) {
                logger.debug("Retry interrompido para mensagem: runId={}, sequence={}", 
                    queuedMessage.message.runId, queuedMessage.message.sequenceNumber)
            }
        }.start()
    }
    
    /**
     * Obtém estatísticas da fila
     */
    fun getQueueStats(): QueueStats {
        return QueueStats(
            queueSize = messageQueue.size,
            isActive = isActive.get(),
            workerThreadAlive = workerThread.isAlive,
            maxRetries = maxRetries,
            baseBackoffMs = baseBackoffMs
        )
    }
    
    /**
     * Para o dispatcher de forma graciosa
     */
    fun shutdown() {
        logger.info("Shutdown do SequenceDispatcher iniciado")
        isActive.set(false)
        workerThread.interrupt()
        
        try {
            workerThread.join(5000) // Aguarda até 5 segundos
            if (workerThread.isAlive) {
                logger.warn("Worker thread não parou graciosamente")
            }
        } catch (e: InterruptedException) {
            logger.warn("Interrupção durante shutdown")
        }
        
        logger.info("SequenceDispatcher parado")
    }
    
    /**
     * Limpa a fila de mensagens
     */
    fun clearQueue() {
        messageQueue.clear()
        logger.info("Fila de mensagens limpa")
    }
}

/**
 * Mensagem enfileirada com metadados para processamento
 */
data class QueuedMessage(
    val message: OutboundMessage,
    val sequenceNumber: Long,
    val priority: MessagePriority,
    val enqueueTime: Instant,
    val retryCount: Int
)

/**
 * Prioridade da mensagem
 */
enum class MessagePriority {
    HIGH,      // Prioridade alta (ex: exceções críticas)
    NORMAL,    // Prioridade normal (ex: progresso)
    LOW        // Prioridade baixa (ex: metadados)
}

/**
 * Estatísticas da fila de mensagens
 */
data class QueueStats(
    val queueSize: Int,
    val isActive: Boolean,
    val workerThreadAlive: Boolean,
    val maxRetries: Int,
    val baseBackoffMs: Long
)
