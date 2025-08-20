package com.schedkiwi.schedulertelemetry.net

import com.schedkiwi.schedulertelemetry.core.ExecutionContext
import com.schedkiwi.schedulertelemetry.core.ProgressTracker
import com.schedkiwi.schedulertelemetry.core.SequenceManager
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.Instant
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Dispatcher para mensagens de progresso em tempo real.
 * 
 * Este componente envia atualizações de progresso periodicamente
 * para o Gerenciador Central, permitindo monitoramento em tempo real
 * sem impactar a performance do scheduler.
 */
class ProgressDispatcher(
    private val httpClientFactory: HttpClientFactory,
    private val sequenceManager: SequenceManager,
    private val baseUrl: String,
    private val endpoint: String = "/api/executions/progress",
    private val headers: Map<String, String> = emptyMap(),
    private val updateIntervalMs: Long = 1000L,
    private val maxRetries: Int = 3,
    private val baseBackoffMs: Long = 500L
) {
    
    private val logger = LoggerFactory.getLogger(ProgressDispatcher::class.java)
    
    /**
     * Executor para agendar atualizações de progresso
     */
    private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(2)
    
    /**
     * Flag para controlar se o dispatcher está ativo
     */
    private val isActive = AtomicBoolean(true)
    
    /**
     * Cache de trackers de progresso por runId
     */
    private val progressTrackers = mutableMapOf<String, ProgressTracker>()
    
    /**
     * Cache de contextos de execução por runId
     */
    private val executionContexts = mutableMapOf<String, ExecutionContext>()
    
    /**
     * Inicia o monitoramento de progresso para um contexto de execução
     */
    fun startProgressTracking(
        context: ExecutionContext,
        updateIntervalMs: Long = this.updateIntervalMs
    ): CompletableFuture<Boolean> {
        if (!isActive.get()) {
            return CompletableFuture.completedFuture(false)
        }
        
        val runId = context.runId
        
        try {
            // Cria ProgressTracker para o contexto
            val tracker = ProgressTracker(context, updateIntervalMs)
            progressTrackers[runId] = tracker
            executionContexts[runId] = context
            
            // Agenda atualizações periódicas
            val task = scheduler.scheduleAtFixedRate({
                sendProgressUpdate(runId, tracker)
            }, updateIntervalMs, updateIntervalMs, TimeUnit.MILLISECONDS)
            
            logger.info("Monitoramento de progresso iniciado: runId={}, interval={}ms", 
                runId, updateIntervalMs)
            
            return CompletableFuture.completedFuture(true)
            
        } catch (e: Exception) {
            logger.error("Falha ao iniciar monitoramento de progresso: runId={}", runId, e)
            return CompletableFuture.completedFuture(false)
        }
    }
    
    /**
     * Para o monitoramento de progresso para um contexto específico
     */
    fun stopProgressTracking(runId: String): CompletableFuture<Boolean> {
        try {
            progressTrackers.remove(runId)
            executionContexts.remove(runId)
            
            logger.info("Monitoramento de progresso parado: runId={}", runId)
            return CompletableFuture.completedFuture(true)
            
        } catch (e: Exception) {
            logger.error("Falha ao parar monitoramento de progresso: runId={}", runId, e)
            return CompletableFuture.completedFuture(false)
        }
    }
    
    /**
     * Envia atualização de progresso para o Gerenciador Central
     */
    private fun sendProgressUpdate(runId: String, tracker: ProgressTracker) {
        try {
            val context = executionContexts[runId] ?: return
            val progressInfo = tracker.getCurrentProgress()
            
            // Atualiza o progresso no tracker
            tracker.updateProgress()
            
            // Gera número de sequência
            val sequenceNumber = sequenceManager.getNextSequenceNumber(runId)
            
            // Cria metadados do item atual (se disponível)
            val currentItemMetadata = getCurrentItemMetadata(context)
            
            // Cria mensagem de progresso
            val progressMessage = OutboundMessageFactory.createProgressMessage(
                context = context,
                currentItem = progressInfo.currentItem,
                currentItemMetadata = currentItemMetadata,
                estimatedTimeRemaining = progressInfo.estimatedTimeRemaining,
                sequenceNumber = sequenceNumber,
                checksum = calculateChecksum("${runId}_${sequenceNumber}_${Instant.now()}")
            )
            
            // Envia mensagem via HTTP
            val url = "$baseUrl$endpoint"
            httpClientFactory.postMessageWithRetry(url, progressMessage, headers)
                .thenAccept { response ->
                    if (response.statusCode() in 200..299) {
                        logger.trace("Progresso enviado: runId={}, sequence={}, progress={}%", 
                            runId, sequenceNumber, progressInfo.progressPercentage)
                        
                        // Adiciona mensagem ao buffer para sincronização
                        sequenceManager.addMessageToBuffer(
                            runId,
                            com.schedkiwi.schedulertelemetry.core.SequencedMessage(
                                runId = runId,
                                sequenceNumber = sequenceNumber,
                                timestamp = Instant.now(),
                                messageType = "PROGRESS",
                                payload = "${runId}_${sequenceNumber}_${Instant.now()}",
                                checksum = progressMessage.checksum
                            )
                        )
                    } else {
                        logger.warn("Falha ao enviar progresso: runId={}, status={}", 
                            runId, response.statusCode())
                    }
                }
                .exceptionally { throwable ->
                    logger.error("Erro ao enviar progresso: runId={}", runId, throwable)
                    null
                }
                
        } catch (e: Exception) {
            logger.error("Falha ao processar atualização de progresso: runId={}", runId, e)
        }
    }
    
    /**
     * Obtém metadados do item atual sendo processado
     */
    private fun getCurrentItemMetadata(context: ExecutionContext): CurrentItemMetadata? {
        val itemMetadata = context.itemMetadata.lastOrNull() ?: return null
        
        return CurrentItemMetadata(
            key = itemMetadata.key,
            metadata = itemMetadata.metadata,
            status = when (itemMetadata.outcome) {
                com.schedkiwi.schedulertelemetry.core.ItemOutcome.OK -> "COMPLETED"
                com.schedkiwi.schedulertelemetry.core.ItemOutcome.ERROR -> "ERROR"
                com.schedkiwi.schedulertelemetry.core.ItemOutcome.SKIPPED -> "SKIPPED"
            }
        )
    }
    
    /**
     * Calcula checksum SHA-256 da mensagem
     */
    private fun calculateChecksum(messageString: String): String {
        return sequenceManager.calculateChecksum(messageString)
    }
    

    
    /**
     * Obtém estatísticas de progresso para um runId
     */
    fun getProgressStats(runId: String): ProgressStats? {
        val tracker = progressTrackers[runId] ?: return null
        val context = executionContexts[runId] ?: return null
        
        val performanceStats = tracker.getPerformanceStats()
        val alerts = tracker.getProgressAlerts()
        
        return ProgressStats(
            runId = runId,
            isTracking = true,
            lastUpdate = Instant.now(),
            progressInfo = tracker.getCurrentProgress(),
            performanceStats = performanceStats,
            alerts = alerts
        )
    }
    
    /**
     * Obtém estatísticas de todos os trackers ativos
     */
    fun getAllProgressStats(): List<ProgressStats> {
        return progressTrackers.keys.mapNotNull { runId ->
            getProgressStats(runId)
        }
    }
    
    /**
     * Verifica se um runId está sendo monitorado
     */
    fun isTrackingProgress(runId: String): Boolean {
        return progressTrackers.containsKey(runId)
    }
    
    /**
     * Obtém estatísticas gerais do dispatcher
     */
    fun getDispatcherStats(): DispatcherStats {
        return DispatcherStats(
            activeTrackers = progressTrackers.size,
            isActive = isActive.get(),
            updateIntervalMs = updateIntervalMs,
            maxRetries = maxRetries,
            baseBackoffMs = baseBackoffMs
        )
    }
    
    /**
     * Para o dispatcher de forma graciosa
     */
    fun shutdown() {
        logger.info("Shutdown do ProgressDispatcher iniciado")
        isActive.set(false)
        
        // Para todos os trackers ativos
        progressTrackers.keys.forEach { runId ->
            stopProgressTracking(runId)
        }
        
        // Para o scheduler
        scheduler.shutdown()
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow()
            }
        } catch (e: InterruptedException) {
            scheduler.shutdownNow()
        }
        
        logger.info("ProgressDispatcher parado")
    }
    
    /**
     * Limpa todos os recursos
     */
    fun clearAll() {
        progressTrackers.clear()
        executionContexts.clear()
        logger.info("Todos os recursos do ProgressDispatcher limpos")
    }
}

/**
 * Estatísticas de progresso para um runId
 */
data class ProgressStats(
    val runId: String,
    val isTracking: Boolean,
    val lastUpdate: Instant,
    val progressInfo: com.schedkiwi.schedulertelemetry.core.ProgressInfo,
    val performanceStats: com.schedkiwi.schedulertelemetry.core.PerformanceStats,
    val alerts: List<com.schedkiwi.schedulertelemetry.core.ProgressAlert>
)

/**
 * Estatísticas gerais do dispatcher
 */
data class DispatcherStats(
    val activeTrackers: Int,
    val isActive: Boolean,
    val updateIntervalMs: Long,
    val maxRetries: Int,
    val baseBackoffMs: Long
)
