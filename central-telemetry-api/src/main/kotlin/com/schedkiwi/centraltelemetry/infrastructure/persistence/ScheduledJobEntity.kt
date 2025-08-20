package com.schedkiwi.centraltelemetry.infrastructure.persistence

import jakarta.persistence.*
import java.time.Instant
import java.util.*

/**
 * Entidade JPA para persistência de jobs agendados
 */
@Entity
@Table(name = "scheduled_jobs")
class ScheduledJobEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),
    
    @Column(name = "job_id", nullable = false)
    val jobId: String,
    
    @Column(name = "method_name", nullable = false)
    val methodName: String,
    
    @Column(name = "class_name", nullable = false)
    val className: String,
    
    @Column(name = "cron_expression")
    val cronExpression: String? = null,
    
    @Column(name = "fixed_rate")
    val fixedRate: Long? = null,
    
    @Column(name = "fixed_delay")
    val fixedDelay: Long? = null,
    
    @Column(name = "time_unit")
    val timeUnit: String? = null,
    
    @Column(columnDefinition = "TEXT")
    val description: String? = null,
    
    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),
    
    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant = Instant.now()
) {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    var application: ApplicationEntity? = null
    
    @OneToMany(mappedBy = "scheduledJob", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val executions: MutableList<ExecutionEntity> = mutableListOf()
    
    fun addExecution(execution: ExecutionEntity) {
        executions.add(execution)
        execution.scheduledJob = this
    }
}
