package com.schedkiwi.centraltelemetry.domain.ports

import com.schedkiwi.centraltelemetry.domain.entities.Application
import java.time.Instant
import java.util.*

/**
 * Port (interface) para repositório de aplicações
 */
interface ApplicationRepository {
    fun save(application: Application): Application
    fun findById(id: UUID): Application?
    fun findByAppName(appName: String): Application?
    fun findAll(): List<Application>
    fun findActive(): List<Application>
    fun findByEnvironment(environment: String): List<Application>
    fun findByHostAndPort(host: String, port: Int): Application?
    fun updateHeartbeat(id: UUID): Boolean
    fun deactivate(id: UUID): Boolean
    fun delete(id: UUID): Boolean
    fun findInactiveApplications(since: Instant): List<Application>
}
