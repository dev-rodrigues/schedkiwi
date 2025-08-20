package com.schedkiwi.centraltelemetry.infrastructure.repositories

import com.schedkiwi.centraltelemetry.infrastructure.persistence.ApplicationTokenEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.*

/**
 * Repositório JPA para tokens de aplicação
 */
@Repository
interface ApplicationTokenJpaRepository : JpaRepository<ApplicationTokenEntity, UUID> {

    /**
     * Busca token por hash
     */
    fun findByTokenHash(tokenHash: String): ApplicationTokenEntity?

    /**
     * Busca tokens por nome da aplicação
     */
    fun findByAppName(appName: String): List<ApplicationTokenEntity>

    /**
     * Busca tokens ativos
     */
    fun findByIsActiveTrue(): List<ApplicationTokenEntity>

    /**
     * Busca tokens expirados
     */
    @Query("SELECT t FROM ApplicationTokenEntity t WHERE t.expiresAt < :now")
    fun findExpiredTokens(@Param("now") now: Instant): List<ApplicationTokenEntity>

    /**
     * Busca tokens por nome da aplicação e status ativo
     */
    fun findByAppNameAndIsActiveTrue(appName: String): List<ApplicationTokenEntity>

    /**
     * Verifica se existe token ativo para uma aplicação
     */
    fun existsByAppNameAndIsActiveTrue(appName: String): Boolean

    /**
     * Busca tokens não utilizados há muito tempo
     */
    @Query("SELECT t FROM ApplicationTokenEntity t WHERE t.lastUsedAt < :since AND t.isActive = true")
    fun findInactiveTokens(@Param("since") since: Instant): List<ApplicationTokenEntity>
}
