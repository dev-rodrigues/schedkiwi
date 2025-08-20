package com.schedkiwi.centraltelemetry.infrastructure.persistence

import com.schedkiwi.centraltelemetry.domain.valueobjects.ExecutionStatus
import jakarta.persistence.*
import java.time.Instant
import java.util.*

/**
 * Entidade JPA para persistência de execuções
 */
@Entity
@Table(name = "executions")
class ExecutionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),
    
    @Column(name = "run_id", nullable = false, unique = true)
    val runId: String,
    
    @Column(name = "job_id", nullable = false)
    val jobId: String,
    
    @Column(name = "app_name", nullable = false)
    val appName: String,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: ExecutionStatus,
    
    @Column(name = "start_time", nullable = false)
    val startTime: Instant,
    
    @Column(name = "end_time")
    val endTime: Instant? = null,
    
    @Column(name = "planned_total", nullable = false)
    val plannedTotal: Long = 0L,
    
    @Column(name = "processed_items", nullable = false)
    val processedItems: Long = 0L,
    
    @Column(name = "failed_items", nullable = false)
    val failedItems: Long = 0L,
    
    @Column(name = "skipped_items", nullable = false)
    val skippedItems: Long = 0L,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),
    
    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant = Instant.now()
) {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    var application: ApplicationEntity? = null
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scheduled_job_id", nullable = false)
    var scheduledJob: ScheduledJobEntity? = null
    
    @OneToMany(mappedBy = "execution", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val itemMetadata: MutableList<ItemMetadataEntity> = mutableListOf()
    
    @OneToMany(mappedBy = "execution", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val exceptions: MutableList<ExceptionInfoEntity> = mutableListOf()
    
    @OneToMany(mappedBy = "execution", cascade = [CascadeType.ALL], orphanRemoval = true)
    val generalMetadata: MutableList<ExecutionGeneralMetadataEntity> = mutableListOf()
    
    @OneToMany(mappedBy = "execution", cascade = [CascadeType.ALL], orphanRemoval = true)
    val progressUpdates: MutableList<ExecutionProgressEntity> = mutableListOf()
    
    fun addItemMetadata(item: ItemMetadataEntity) {
        itemMetadata.add(item)
        item.execution = this
    }
    
    fun addException(exception: ExceptionInfoEntity) {
        exceptions.add(exception)
        exception.execution = this
    }
    
    fun putMetadata(key: String, value: String) {
        val metadataEntity = ExecutionGeneralMetadataEntity(
            metadataKey = key,
            metadataValue = value
        )
        metadataEntity.execution = this
        generalMetadata.add(metadataEntity)
    }
    
    fun addProgressUpdate(progress: ExecutionProgressEntity) {
        progressUpdates.add(progress)
        progress.execution = this
    }
}
