package com.schedkiwi.schedulertelemetry.core

import java.time.Duration
import java.time.Instant
import java.util.concurrent.atomic.AtomicLong

/**
 * Rastreador de progresso em tempo real para execuções de schedulers.
 * 
 * Este componente calcula estatísticas de progresso, estimativas de tempo
 * e mantém histórico de performance para análise.
 */
class ProgressTracker(
    private val context: ExecutionContext,
    private val updateIntervalMs: Long = 1000L
) {
    
    /**
     * Timestamp da última atualização de progresso
     */
    private var lastProgressUpdate: Instant = Instant.now()
    
    /**
     * Histórico de progresso para cálculo de estimativas
     */
    private val progressHistory = mutableListOf<ProgressSnapshot>()
    
    /**
     * Contador de itens processados desde a última atualização
     */
    private val itemsSinceLastUpdate = AtomicLong(0L)
    
    /**
     * Obtém informações de progresso atuais
     */
    fun getCurrentProgress(): ProgressInfo {
        val currentItem = context.getTotalProcessed()
        val totalItems = context.plannedTotal
        val progressPercentage = context.getProgressPercentage()
        
        return ProgressInfo(
            currentItem = currentItem,
            totalItems = totalItems,
            progressPercentage = progressPercentage,
            processedItems = context.processedItems.get(),
            failedItems = context.failedItems.get(),
            skippedItems = context.skippedItems.get(),
            estimatedTimeRemaining = calculateEstimatedTimeRemaining()
        )
    }
    
    /**
     * Atualiza o progresso e registra snapshot se necessário
     */
    fun updateProgress() {
        val now = Instant.now()
        val itemsProcessed = context.getTotalProcessed()
        
        // Verifica se é hora de registrar um snapshot
        if (Duration.between(lastProgressUpdate, now).toMillis() >= updateIntervalMs) {
            val snapshot = ProgressSnapshot(
                timestamp = now,
                itemsProcessed = itemsProcessed,
                itemsSinceLastUpdate = itemsSinceLastUpdate.get(),
                durationSinceLastUpdate = Duration.between(lastProgressUpdate, now)
            )
            
            progressHistory.add(snapshot)
            
            // Mantém apenas os últimos 100 snapshots para evitar crescimento excessivo
            if (progressHistory.size > 100) {
                progressHistory.removeAt(0)
            }
            
            lastProgressUpdate = now
            itemsSinceLastUpdate.set(0L)
        }
    }
    
    /**
     * Marca um item como processado
     */
    fun markItemProcessed() {
        itemsSinceLastUpdate.incrementAndGet()
        updateProgress()
    }
    
    /**
     * Calcula o tempo estimado restante baseado no histórico de progresso
     */
    fun calculateEstimatedTimeRemaining(): Long? {
        if (progressHistory.size < 2) return null
        
        val totalItems = context.plannedTotal
        val itemsProcessed = context.getTotalProcessed()
        val remainingItems = totalItems - itemsProcessed
        
        if (remainingItems <= 0) return 0L
        
        // Calcula a taxa média de processamento dos últimos snapshots
        val recentSnapshots = progressHistory.takeLast(5)
        val totalItemsProcessed = recentSnapshots.sumOf { it.itemsSinceLastUpdate }
        val totalDuration = recentSnapshots.sumOf { it.durationSinceLastUpdate.toMillis() }
        
        if (totalDuration == 0L || totalItemsProcessed == 0L) return null
        
        val itemsPerMs = totalItemsProcessed.toDouble() / totalDuration.toDouble()
        val estimatedMs = remainingItems / itemsPerMs
        
        return (estimatedMs / 1000).toLong() // Retorna em segundos
    }
    
    /**
     * Obtém estatísticas de performance
     */
    fun getPerformanceStats(): PerformanceStats {
        val totalDuration = Duration.between(context.startTime, Instant.now())
        val totalItems = context.getTotalProcessed()
        
        val itemsPerSecond = if (totalDuration.toSeconds() > 0) {
            totalItems.toDouble() / totalDuration.toSeconds()
        } else 0.0
        
        val averageProcessingTime = if (totalItems > 0) {
            totalDuration.toMillis() / totalItems
        } else 0L
        
        return PerformanceStats(
            totalDuration = totalDuration,
            totalItems = totalItems,
            itemsPerSecond = itemsPerSecond,
            averageProcessingTimeMs = averageProcessingTime,
            progressHistorySize = progressHistory.size
        )
    }
    
    /**
     * Obtém o histórico de progresso
     */
    fun getProgressHistory(): List<ProgressSnapshot> = progressHistory.toList()
    
    /**
     * Limpa o histórico de progresso
     */
    fun clearProgressHistory() {
        progressHistory.clear()
        lastProgressUpdate = Instant.now()
        itemsSinceLastUpdate.set(0L)
    }
    
    /**
     * Verifica se o progresso está estagnado (sem atualizações recentes)
     */
    fun isProgressStagnant(stagnantThresholdMs: Long = 30000L): Boolean {
        val timeSinceLastUpdate = Duration.between(lastProgressUpdate, Instant.now())
        return timeSinceLastUpdate.toMillis() > stagnantThresholdMs
    }
    
    /**
     * Obtém alertas de progresso
     */
    fun getProgressAlerts(): List<ProgressAlert> {
        val alerts = mutableListOf<ProgressAlert>()
        
        // Alerta se o progresso está estagnado
        if (isProgressStagnant()) {
            alerts.add(ProgressAlert.STAGNANT_PROGRESS)
        }
        
        // Alerta se há muitas falhas
        val failureRate = if (context.plannedTotal > 0) {
            context.failedItems.get().toDouble() / context.plannedTotal.toDouble()
        } else 0.0
        
        if (failureRate > 0.1) { // Mais de 10% de falhas
            alerts.add(ProgressAlert.HIGH_FAILURE_RATE)
        }
        
        // Alerta se o progresso está muito lento
        val performanceStats = getPerformanceStats()
        if (performanceStats.itemsPerSecond < 0.1) { // Menos de 0.1 itens por segundo
            alerts.add(ProgressAlert.SLOW_PROGRESS)
        }
        
        return alerts
    }
}

/**
 * Snapshot do progresso em um momento específico
 */
data class ProgressSnapshot(
    val timestamp: Instant,
    val itemsProcessed: Long,
    val itemsSinceLastUpdate: Long,
    val durationSinceLastUpdate: Duration
)

/**
 * Estatísticas de performance da execução
 */
data class PerformanceStats(
    val totalDuration: Duration,
    val totalItems: Long,
    val itemsPerSecond: Double,
    val averageProcessingTimeMs: Long,
    val progressHistorySize: Int
)

/**
 * Alertas de progresso
 */
enum class ProgressAlert {
    STAGNANT_PROGRESS,    // Progresso estagnado
    HIGH_FAILURE_RATE,    // Taxa alta de falhas
    SLOW_PROGRESS         // Progresso muito lento
}
