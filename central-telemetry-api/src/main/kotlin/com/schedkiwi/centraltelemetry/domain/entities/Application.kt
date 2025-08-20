package com.schedkiwi.centraltelemetry.domain.entities

import java.time.Instant
import java.util.*

/**
 * Entidade de domínio que representa uma aplicação integrada
 */
data class Application(
    val id: UUID = UUID.randomUUID(),
    val appName: String,
    val host: String,
    val port: Int,
    val environment: String = "production",
    val version: String = "1.0.0",
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
    val isActive: Boolean = true,
    val lastHeartbeat: Instant = Instant.now(),
    val scheduledJobs: MutableList<ScheduledJob> = mutableListOf()
) {
    fun addScheduledJob(job: ScheduledJob) {
        scheduledJobs.add(job)
    }

    fun updateHeartbeat() {
        // Em uma implementação real, isso seria feito via método que atualiza o lastHeartbeat
    }

    fun deactivate() {
        // Em uma implementação real, isso seria feito via método que atualiza o isActive para false
    }
}
