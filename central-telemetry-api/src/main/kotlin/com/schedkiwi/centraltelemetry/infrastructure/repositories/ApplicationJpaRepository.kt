package com.schedkiwi.centraltelemetry.infrastructure.repositories

import com.schedkiwi.centraltelemetry.infrastructure.persistence.ApplicationEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

/**
 * Repositório Spring Data JPA para entidades Application
 */
@Repository
interface ApplicationJpaRepository : JpaRepository<ApplicationEntity, UUID> {
    
    /**
     * Busca aplicação por nome
     */
    fun findByAppName(appName: String): ApplicationEntity?
    
    /**
     * Busca aplicações ativas
     */
    fun findByIsActiveTrue(): List<ApplicationEntity>
    
    /**
     * Busca aplicações por ambiente
     */
    fun findByEnvironment(environment: String): List<ApplicationEntity>
    
    /**
     * Busca aplicações por host e porta
     */
    fun findByHostAndPort(host: String, port: Int): ApplicationEntity?
    
    /**
     * Busca aplicações que não enviaram heartbeat recentemente
     */
    @Query("SELECT a FROM ApplicationEntity a WHERE a.lastHeartbeat < :threshold OR a.lastHeartbeat IS NULL")
    fun findInactiveApplications(@Param("threshold") threshold: java.time.Instant): List<ApplicationEntity>
    
    /**
     * Verifica se existe aplicação com o nome especificado
     */
    fun existsByAppName(appName: String): Boolean
}
