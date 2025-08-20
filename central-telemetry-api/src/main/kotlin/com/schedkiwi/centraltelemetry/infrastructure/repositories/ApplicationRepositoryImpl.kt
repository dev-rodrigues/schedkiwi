package com.schedkiwi.centraltelemetry.infrastructure.repositories

import com.schedkiwi.centraltelemetry.application.mappers.ApplicationMapper
import com.schedkiwi.centraltelemetry.domain.entities.Application
import com.schedkiwi.centraltelemetry.domain.ports.ApplicationRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.*

/**
 * Implementação JPA do repositório de aplicações
 */
@Repository
class ApplicationRepositoryImpl(
    private val jpaRepository: ApplicationJpaRepository,
    private val mapper: ApplicationMapper
) : ApplicationRepository {

    override fun save(application: Application): Application {
        val entity = mapper.toEntity(application)
        val savedEntity = jpaRepository.save(entity)
        return mapper.toDomain(savedEntity)
    }

    override fun findById(id: UUID): Application? {
        return jpaRepository.findById(id)
            .map { mapper.toDomain(it) }
            .orElse(null)
    }

    override fun findByAppName(appName: String): Application? {
        return jpaRepository.findByAppName(appName)
            ?.let { mapper.toDomain(it) }
    }

    override fun findAll(): List<Application> {
        return jpaRepository.findAll()
            .map { mapper.toDomain(it) }
    }

    override fun findActive(): List<Application> {
        return jpaRepository.findByIsActiveTrue()
            .map { mapper.toDomain(it) }
    }

    override fun findByEnvironment(environment: String): List<Application> {
        return jpaRepository.findByEnvironment(environment)
            .map { mapper.toDomain(it) }
    }

    override fun findByHostAndPort(host: String, port: Int): Application? {
        return jpaRepository.findByHostAndPort(host, port)
            ?.let { mapper.toDomain(it) }
    }

    override fun updateHeartbeat(id: UUID): Boolean {
        val entity = jpaRepository.findById(id).orElse(null) ?: return false
        // Em uma implementação real, isso seria feito via método que atualiza o lastHeartbeat
        // entity.lastHeartbeat = Instant.now()
        return true
    }

    override fun deactivate(id: UUID): Boolean {
        val entity = jpaRepository.findById(id).orElse(null) ?: return false
        // Em uma implementação real, isso seria feito via método que atualiza o isActive para false
        // entity.isActive = false
        return true
    }

    override fun delete(id: UUID): Boolean {
        if (!jpaRepository.existsById(id)) return false
        jpaRepository.deleteById(id)
        return true
    }

    override fun findInactiveApplications(since: Instant): List<Application> {
        return jpaRepository.findInactiveApplications(since)
            .map { mapper.toDomain(it) }
    }
}
