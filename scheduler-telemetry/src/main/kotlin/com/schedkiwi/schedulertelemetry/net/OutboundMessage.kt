package com.schedkiwi.schedulertelemetry.net

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import com.schedkiwi.schedulertelemetry.core.ExecutionContext
import com.schedkiwi.schedulertelemetry.core.ItemOutcome
import java.time.Instant

/**
 * Mensagem base para comunicação com o Gerenciador Central.
 * Todas as mensagens herdam desta classe para garantir consistência.
 */
sealed class OutboundMessage {
    
    abstract val runId: String
    abstract val jobId: String
    abstract val appName: String
    abstract val timestamp: Instant
    abstract val sequenceNumber: Long
    abstract val checksum: String
}

/**
 * Mensagem de registro da aplicação no startup
 */
data class RegistrationMessage(
    override val runId: String,
    override val jobId: String,
    override val appName: String,
    override val timestamp: Instant,
    override val sequenceNumber: Long,
    override val checksum: String,
    
    @JsonProperty("host")
    val host: String,
    
    @JsonProperty("port")
    val port: Int,
    
    @JsonProperty("scheduledJobs")
    val scheduledJobs: List<ScheduledJobInfo>
) : OutboundMessage()

/**
 * Informações sobre um job agendado
 */
data class ScheduledJobInfo(
    @JsonProperty("jobId")
    val jobId: String,
    
    @JsonProperty("methodName")
    val methodName: String,
    
    @JsonProperty("className")
    val className: String,
    
    @JsonProperty("cronExpression")
    val cronExpression: String?,
    
    @JsonProperty("fixedRate")
    val fixedRate: Long?,
    
    @JsonProperty("fixedDelay")
    val fixedDelay: Long?,
    
    @JsonProperty("initialDelay")
    val initialDelay: Long?,
    
    @JsonProperty("timeUnit")
    val timeUnit: String?,
    
    @JsonProperty("isMonitored")
    val isMonitored: Boolean,
    
    @JsonProperty("description")
    val description: String,
    
    @JsonProperty("tags")
    val tags: List<String>
)

/**
 * Mensagem de relatório final de execução
 */
data class ExecutionReportMessage(
    override val runId: String,
    override val jobId: String,
    override val appName: String,
    override val timestamp: Instant,
    override val sequenceNumber: Long,
    override val checksum: String,
    
    @JsonProperty("startTime")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    val startTime: Instant,
    
    @JsonProperty("endTime")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    val endTime: Instant,
    
    @JsonProperty("status")
    val status: String,
    
    @JsonProperty("plannedTotal")
    val plannedTotal: Long,
    
    @JsonProperty("processedItems")
    val processedItems: Long,
    
    @JsonProperty("itemMetadata")
    val itemMetadata: List<ItemMetadataDto>,
    
    @JsonProperty("exceptions")
    val exceptions: List<ExceptionInfoDto>
) : OutboundMessage()

/**
 * DTO para metadados de item
 */
data class ItemMetadataDto(
    @JsonProperty("key")
    val key: String?,
    
    @JsonProperty("metadata")
    val metadata: Map<String, Any?>,
    
    @JsonProperty("outcome")
    val outcome: ItemOutcome,
    
    @JsonProperty("timestamp")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    val timestamp: Instant
)

/**
 * DTO para informações de exceção
 */
data class ExceptionInfoDto(
    @JsonProperty("message")
    val message: String,
    
    @JsonProperty("type")
    val type: String,
    
    @JsonProperty("stackTrace")
    val stackTrace: String,
    
    @JsonProperty("timestamp")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    val timestamp: Instant
)

/**
 * Mensagem de progresso em tempo real
 */
data class ProgressMessage(
    override val runId: String,
    override val jobId: String,
    override val appName: String,
    override val timestamp: Instant,
    override val sequenceNumber: Long,
    override val checksum: String,
    
    @JsonProperty("currentItem")
    val currentItem: Long,
    
    @JsonProperty("totalItems")
    val totalItems: Long,
    
    @JsonProperty("progressPercentage")
    val progressPercentage: Double,
    
    @JsonProperty("currentItemMetadata")
    val currentItemMetadata: CurrentItemMetadata?,
    
    @JsonProperty("estimatedTimeRemaining")
    val estimatedTimeRemaining: Long? // em segundos
) : OutboundMessage()

/**
 * Metadados do item atual sendo processado
 */
data class CurrentItemMetadata(
    @JsonProperty("key")
    val key: String?,
    
    @JsonProperty("metadata")
    val metadata: Map<String, Any?>,
    
    @JsonProperty("status")
    val status: String // PROCESSING, COMPLETED, ERROR
)

/**
 * Mensagem de status da execução
 */
data class StatusMessage(
    override val runId: String,
    override val jobId: String,
    override val appName: String,
    override val timestamp: Instant,
    override val sequenceNumber: Long,
    override val checksum: String,
    
    @JsonProperty("status")
    val status: String,
    
    @JsonProperty("startTime")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    val startTime: Instant,
    
    @JsonProperty("currentProgress")
    val currentProgress: CurrentProgress,
    
    @JsonProperty("lastUpdate")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    val lastUpdate: Instant,
    
    @JsonProperty("estimatedCompletion")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    val estimatedCompletion: Instant?
) : OutboundMessage()

/**
 * Progresso atual da execução
 */
data class CurrentProgress(
    @JsonProperty("currentItem")
    val currentItem: Long,
    
    @JsonProperty("totalItems")
    val totalItems: Long,
    
    @JsonProperty("progressPercentage")
    val progressPercentage: Double,
    
    @JsonProperty("processedItems")
    val processedItems: Long,
    
    @JsonProperty("failedItems")
    val failedItems: Long,
    
    @JsonProperty("skippedItems")
    val skippedItems: Long
)

/**
 * Mensagem de sincronização
 */
data class SyncMessage(
    override val runId: String,
    override val jobId: String,
    override val appName: String,
    override val timestamp: Instant,
    override val sequenceNumber: Long,
    override val checksum: String,
    
    @JsonProperty("lastReceivedSequence")
    val lastReceivedSequence: Long,
    
    @JsonProperty("missingSequences")
    val missingSequences: List<Long>,
    
    @JsonProperty("status")
    val status: String, // SYNC_REQUIRED, SYNCED, ERROR
    
    @JsonProperty("message")
    val message: String
) : OutboundMessage()

/**
 * Factory para criar mensagens a partir de ExecutionContext
 */
object OutboundMessageFactory {
    
    /**
     * Cria uma mensagem de registro
     */
    fun createRegistrationMessage(
        runId: String,
        jobId: String,
        appName: String,
        host: String,
        port: Int,
        scheduledJobs: List<ScheduledJobInfo>,
        sequenceNumber: Long,
        checksum: String
    ): RegistrationMessage {
        return RegistrationMessage(
            runId = runId,
            jobId = jobId,
            appName = appName,
            timestamp = Instant.now(),
            sequenceNumber = sequenceNumber,
            checksum = checksum,
            host = host,
            port = port,
            scheduledJobs = scheduledJobs
        )
    }
    
    /**
     * Cria uma mensagem de relatório de execução
     */
    fun createExecutionReportMessage(
        context: ExecutionContext,
        endTime: Instant,
        sequenceNumber: Long,
        checksum: String
    ): ExecutionReportMessage {
        return ExecutionReportMessage(
            runId = context.runId,
            jobId = context.jobId,
            appName = context.appName,
            timestamp = Instant.now(),
            sequenceNumber = sequenceNumber,
            checksum = checksum,
            startTime = context.startTime,
            endTime = endTime,
            status = context.getStatus().name,
            plannedTotal = context.plannedTotal,
            processedItems = context.processedItems.get(),
            itemMetadata = context.itemMetadata.map { item ->
                ItemMetadataDto(
                    key = item.key,
                    metadata = item.metadata,
                    outcome = item.outcome,
                    timestamp = item.timestamp
                )
            },
            exceptions = context.exceptions.map { ex ->
                ExceptionInfoDto(
                    message = ex.message,
                    type = ex.type,
                    stackTrace = ex.stackTrace,
                    timestamp = ex.timestamp
                )
            }
        )
    }
    
    /**
     * Cria uma mensagem de progresso
     */
    fun createProgressMessage(
        context: ExecutionContext,
        currentItem: Long,
        currentItemMetadata: CurrentItemMetadata?,
        estimatedTimeRemaining: Long?,
        sequenceNumber: Long,
        checksum: String
    ): ProgressMessage {
        return ProgressMessage(
            runId = context.runId,
            jobId = context.jobId,
            appName = context.appName,
            timestamp = Instant.now(),
            sequenceNumber = sequenceNumber,
            checksum = checksum,
            currentItem = currentItem,
            totalItems = context.plannedTotal,
            progressPercentage = context.getProgressPercentage(),
            currentItemMetadata = currentItemMetadata,
            estimatedTimeRemaining = estimatedTimeRemaining
        )
    }
}
