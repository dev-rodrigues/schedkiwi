package com.schedkiwi.centraltelemetry.infrastructure.persistence

import jakarta.persistence.*
import java.time.Instant
import java.util.*

/**
 * Entidade JPA para persistência de aplicações
 */
@Entity
@Table(name = "applications")
class ApplicationEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),
    
    @Column(name = "app_name", nullable = false, unique = true)
    val appName: String,
    
    @Column(nullable = false)
    val host: String,
    
    @Column(nullable = false)
    val port: Int,
    
    @Column(columnDefinition = "TEXT")
    val description: String? = null,
    
    @Column
    val version: String? = null,
    
    @Column
    val environment: String? = null,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),
    
    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant = Instant.now(),
    
    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,
    
    @Column(name = "last_heartbeat")
    val lastHeartbeat: Instant? = null
) {
    @OneToMany(mappedBy = "application", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val scheduledJobs: MutableList<ScheduledJobEntity> = mutableListOf()
    
    @OneToMany(mappedBy = "application", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val tokens: MutableList<ApplicationTokenEntity> = mutableListOf()
    
    fun addScheduledJob(job: ScheduledJobEntity) {
        scheduledJobs.add(job)
        job.application = this
    }
}
