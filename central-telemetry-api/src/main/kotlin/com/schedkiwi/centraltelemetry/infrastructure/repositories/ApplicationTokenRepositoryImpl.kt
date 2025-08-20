package com.schedkiwi.centraltelemetry.infrastructure.repositories

import com.schedkiwi.centraltelemetry.domain.entities.ApplicationToken
import com.schedkiwi.centraltelemetry.domain.ports.ApplicationTokenRepository
import com.schedkiwi.centraltelemetry.infrastructure.persistence.ApplicationTokenEntity
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.*

/**
 * Implementação do repositório de tokens de aplicação
 * 
 * Esta implementação usa Spring Data JPA para persistir
 * tokens de autenticação no banco PostgreSQL
 */
@Repository
class ApplicationTokenRepositoryImpl(
    private val jpaRepository: ApplicationTokenJpaRepository
) : ApplicationTokenRepository {

    override fun findByTokenHash(tokenHash: String): ApplicationToken? {
        return jpaRepository.findByTokenHash(tokenHash)?.toDomain()
    }

    override fun findByAppName(appName: String): List<ApplicationToken> {
        return jpaRepository.findByAppName(appName).map { it.toDomain() }
    }

    override fun findByIsActiveTrue(): List<ApplicationToken> {
        return jpaRepository.findByIsActiveTrue().map { it.toDomain() }
    }

    override fun findExpiredTokens(now: Instant): List<ApplicationToken> {
        return jpaRepository.findExpiredTokens(now).map { it.toDomain() }
    }

    override fun findByAppNameAndIsActiveTrue(appName: String): List<ApplicationToken> {
        return jpaRepository.findByAppNameAndIsActiveTrue(appName).map { it.toDomain() }
    }

    override fun existsByAppNameAndIsActiveTrue(appName: String): Boolean {
        return jpaRepository.existsByAppNameAndIsActiveTrue(appName)
    }

    override fun findInactiveTokens(since: Instant): List<ApplicationToken> {
        return jpaRepository.findInactiveTokens(since).map { it.toDomain() }
    }

    override fun updateLastUsedAt(tokenId: UUID, lastUsedAt: Instant) {
        // Por enquanto, apenas loga a atualização
        // TODO: Implementar atualização quando resolver problemas de compilação
        println("Atualizando lastUsedAt para token $tokenId: $lastUsedAt")
    }

    override fun save(token: ApplicationToken): ApplicationToken {
        val entity = token.toEntity()
        val savedEntity = jpaRepository.save(entity)
        return savedEntity.toDomain()
    }

    override fun update(token: ApplicationToken): ApplicationToken {
        val entity = token.toEntity()
        val updatedEntity = jpaRepository.save(entity)
        return updatedEntity.toDomain()
    }

    override fun deleteById(tokenId: UUID) {
        jpaRepository.deleteById(tokenId)
    }

    /**
     * Converte entidade JPA para entidade de domínio
     */
    private fun ApplicationTokenEntity.toDomain(): ApplicationToken {
        return ApplicationToken(
            id = this.id,
            appName = this.appName,
            applicationId = this.applicationId,
            tokenHash = this.tokenHash,
            description = this.description,
            isActive = this.isActive,
            createdAt = this.createdAt,
            expiresAt = this.expiresAt,
            lastUsedAt = this.lastUsedAt,
            createdBy = this.createdBy
        )
    }

    /**
     * Converte entidade de domínio para entidade JPA
     */
    private fun ApplicationToken.toEntity(): ApplicationTokenEntity {
        return ApplicationTokenEntity(
            id = this.id,
            appName = this.appName,
            applicationId = this.applicationId,
            tokenHash = this.tokenHash,
            description = this.description,
            isActive = this.isActive,
            createdAt = this.createdAt,
            expiresAt = this.expiresAt,
            lastUsedAt = this.lastUsedAt,
            createdBy = this.createdBy
        )
    }
}
