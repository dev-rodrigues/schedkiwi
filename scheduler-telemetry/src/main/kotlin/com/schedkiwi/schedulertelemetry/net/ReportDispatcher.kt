package com.schedkiwi.schedulertelemetry.net

import com.schedkiwi.schedulertelemetry.core.ExecutionContext
import com.schedkiwi.schedulertelemetry.core.SequenceManager
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

/**
 * Dispatcher para relatórios finais de execução.
 * 
 * Este componente processa relatórios de execução em background
 * com retry automático e preservação de ordem sequencial.
 */
class ReportDispatcher(
    private val httpClientFactory: HttpClientFactory,
    private val sequenceManager: SequenceManager,
    private val baseUrl: String,
    private val endpoint: String = "/api/executions/report",
    private val headers: Map<String, String> = emptyMap(),
    private val maxRetries: Int = 5,
    private val baseBackoffMs: Long = 500L,
    private val maxConcurrentReports: Int = 10,
    private val queueCapacity: Int = 1000
) {
    
    private val logger = LoggerFactory.getLogger(ReportDispatcher::class.java)
    
    /**
     * Fila de relatórios pendentes
     */
    private val reportQueue = LinkedBlockingQueue<QueuedReport>(queueCapacity)
    
    /**
     * Executor para processar relatórios em background
     */
    private val executor = ThreadPoolExecutor(
        maxConcurrentReports / 2,
        maxConcurrentReports,
        60L, TimeUnit.SECONDS,
        LinkedBlockingQueue(maxConcurrentReports),
        ThreadPoolExecutor.CallerRunsPolicy()
    )
    
    /**
     * Flag para controlar se o dispatcher está ativo
     */
    private val isActive = AtomicBoolean(true)
    
    /**
     * Contador de relatórios processados
     */
    private val processedReports = AtomicLong(0L)
    
    /**
     * Contador de relatórios falhados
     */
    private val failedReports = AtomicLong(0L)
    
    /**
     * Inicia o processamento de relatórios
     */
    init {
        startReportProcessing()
    }
    
    /**
     * Enfileira um relatório para envio
     */
    fun enqueueReport(
        context: ExecutionContext,
        endTime: Instant,
        priority: MessagePriority = MessagePriority.NORMAL
    ): CompletableFuture<Boolean> {
        if (!isActive.get()) {
            return CompletableFuture.completedFuture(false)
        }
        
        try {
            val queuedReport = QueuedReport(
                context = context,
                endTime = endTime,
                priority = priority,
                enqueueTime = Instant.now(),
                retryCount = 0
            )
            
            val success = reportQueue.offer(queuedReport)
            
            if (success) {
                logger.debug("Relatório enfileirado: runId={}, jobId={}, priority={}", 
                    context.runId, context.jobId, priority)
            } else {
                logger.warn("Falha ao enfileirar relatório: runId={}, jobId={}", 
                    context.runId, context.jobId)
            }
            
            return CompletableFuture.completedFuture(success)
            
        } catch (e: Exception) {
            logger.error("Erro ao enfileirar relatório: runId={}", context.runId, e)
            return CompletableFuture.completedFuture(false)
        }
    }
    
    /**
     * Inicia o processamento de relatórios em background
     */
    private fun startReportProcessing() {
        for (i in 0 until maxConcurrentReports) {
            executor.submit {
                processReportQueue()
            }
        }
        
        logger.info("Processamento de relatórios iniciado com {} workers", maxConcurrentReports)
    }
    
    /**
     * Processa relatórios da fila
     */
    private fun processReportQueue() {
        while (isActive.get()) {
            try {
                val queuedReport = reportQueue.poll(1, TimeUnit.SECONDS)
                if (queuedReport != null) {
                    processReport(queuedReport)
                }
            } catch (e: InterruptedException) {
                logger.debug("Worker thread interrompida")
                break
            } catch (e: Exception) {
                logger.error("Erro ao processar fila de relatórios", e)
            }
        }
    }
    
    /**
     * Processa um relatório individual
     */
    private fun processReport(queuedReport: QueuedReport) {
        val context = queuedReport.context
        val runId = context.runId
        
        try {
            // Gera número de sequência
            val sequenceNumber = sequenceManager.getNextSequenceNumber(runId)
            
            // Cria mensagem de relatório
            val reportMessage = OutboundMessageFactory.createExecutionReportMessage(
                context = context,
                endTime = queuedReport.endTime,
                sequenceNumber = sequenceNumber,
                checksum = calculateChecksum("${runId}_${sequenceNumber}_${Instant.now()}")
            )
            
            // Envia relatório via HTTP
            val url = "$baseUrl$endpoint"
            httpClientFactory.postMessageWithRetry(url, reportMessage, headers)
                .thenAccept { response ->
                    if (response.statusCode() in 200..299) {
                        logger.info("Relatório enviado com sucesso: runId={}, sequence={}, status={}", 
                            runId, sequenceNumber, response.statusCode())
                        
                        processedReports.incrementAndGet()
                        
                        // Adiciona mensagem ao buffer para sincronização
                        sequenceManager.addMessageToBuffer(
                            runId,
                            com.schedkiwi.schedulertelemetry.core.SequencedMessage(
                                runId = runId,
                                sequenceNumber = sequenceNumber,
                                timestamp = Instant.now(),
                                messageType = "REPORT",
                                payload = "${runId}_${sequenceNumber}_${Instant.now()}",
                                checksum = reportMessage.checksum
                            )
                        )
                        
                    } else {
                        throw RuntimeException("HTTP ${response.statusCode()}: ${response.body()}")
                    }
                }
                .exceptionally { throwable ->
                    logger.error("Falha ao enviar relatório: runId={}, sequence={}", 
                        runId, sequenceNumber, throwable)
                    
                    failedReports.incrementAndGet()
                    
                    // Tenta retry se ainda não excedeu o limite
                    if (queuedReport.retryCount < maxRetries) {
                        handleRetry(queuedReport)
                    } else {
                        logger.error("Relatório falhou após {} tentativas: runId={}", 
                            maxRetries, runId)
                        // TODO: Implementar dead letter queue
                    }
                    
                    null
                }
                
        } catch (e: Exception) {
            logger.error("Erro ao processar relatório: runId={}", runId, e)
            failedReports.incrementAndGet()
        }
    }
    
    /**
     * Trata retry de relatórios falhados
     */
    private fun handleRetry(queuedReport: QueuedReport) {
        val retryCount = queuedReport.retryCount + 1
        val backoffMs = baseBackoffMs * (1L shl (retryCount - 1)) // Backoff exponencial
        
        logger.info("Agendando retry {} para relatório: runId={}, backoff={}ms", 
            retryCount, queuedReport.context.runId, backoffMs)
        
        // Agenda retry com backoff exponencial
        executor.submit {
            try {
                Thread.sleep(backoffMs)
                val retryReport = queuedReport.copy(retryCount = retryCount)
                reportQueue.offer(retryReport)
            } catch (e: InterruptedException) {
                logger.debug("Retry interrompido para relatório: runId={}", queuedReport.context.runId)
            }
        }
    }
    
    /**
     * Calcula checksum SHA-256 da mensagem
     */
    private fun calculateChecksum(messageString: String): String {
        return sequenceManager.calculateChecksum(messageString)
    }
    

    
    /**
     * Obtém estatísticas da fila de relatórios
     */
    fun getQueueStats(): ReportQueueStats {
        return ReportQueueStats(
            queueSize = reportQueue.size,
            processedReports = processedReports.get(),
            failedReports = failedReports.get(),
            isActive = isActive.get(),
            maxConcurrentReports = maxConcurrentReports,
            queueCapacity = queueCapacity
        )
    }
    
    /**
     * Obtém estatísticas do executor
     */
    fun getExecutorStats(): ExecutorStats {
        return ExecutorStats(
            activeThreads = executor.activeCount,
            poolSize = executor.poolSize,
            corePoolSize = executor.corePoolSize,
            maximumPoolSize = executor.maximumPoolSize,
            completedTasks = executor.completedTaskCount,
            queueSize = executor.queue.size
        )
    }
    
    /**
     * Para o dispatcher de forma graciosa
     */
    fun shutdown() {
        logger.info("Shutdown do ReportDispatcher iniciado")
        isActive.set(false)
        
        // Para o executor
        executor.shutdown()
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow()
            }
        } catch (e: InterruptedException) {
            executor.shutdownNow()
        }
        
        logger.info("ReportDispatcher parado")
    }
    
    /**
     * Limpa a fila de relatórios
     */
    fun clearQueue() {
        reportQueue.clear()
        logger.info("Fila de relatórios limpa")
    }
    
    /**
     * Obtém estatísticas gerais do dispatcher
     */
    fun getDispatcherStats(): ReportDispatcherStats {
        return ReportDispatcherStats(
            queueStats = getQueueStats(),
            executorStats = getExecutorStats(),
            maxRetries = maxRetries,
            baseBackoffMs = baseBackoffMs
        )
    }
}

/**
 * Relatório enfileirado com metadados para processamento
 */
data class QueuedReport(
    val context: ExecutionContext,
    val endTime: Instant,
    val priority: MessagePriority,
    val enqueueTime: Instant,
    val retryCount: Int
)

/**
 * Estatísticas da fila de relatórios
 */
data class ReportQueueStats(
    val queueSize: Int,
    val processedReports: Long,
    val failedReports: Long,
    val isActive: Boolean,
    val maxConcurrentReports: Int,
    val queueCapacity: Int
)

/**
 * Estatísticas do executor
 */
data class ExecutorStats(
    val activeThreads: Int,
    val poolSize: Int,
    val corePoolSize: Int,
    val maximumPoolSize: Int,
    val completedTasks: Long,
    val queueSize: Int
)

/**
 * Estatísticas gerais do dispatcher
 */
data class ReportDispatcherStats(
    val queueStats: ReportQueueStats,
    val executorStats: ExecutorStats,
    val maxRetries: Int,
    val baseBackoffMs: Long
)
