package com.schedkiwi.centraltelemetry.domain.ports

import com.schedkiwi.centraltelemetry.domain.entities.ApplicationToken
import java.time.Instant
import java.util.*

/**
 * Porta para repositório de tokens de aplicação
 * 
 * Esta interface define as operações necessárias para gerenciar
 * tokens de autenticação das aplicações clientes
 */
interface ApplicationTokenRepository {
    
    /**
     * Busca um token pelo seu hash
     * 
     * @param tokenHash Hash SHA-256 do token
     * @return Token encontrado ou null se não existir
     */
    fun findByTokenHash(tokenHash: String): ApplicationToken?
    
    /**
     * Busca tokens por nome da aplicação
     * 
     * @param appName Nome da aplicação
     * @return Lista de tokens associados à aplicação
     */
    fun findByAppName(appName: String): List<ApplicationToken>
    
    /**
     * Busca tokens ativos
     * 
     * @return Lista de todos os tokens ativos
     */
    fun findByIsActiveTrue(): List<ApplicationToken>
    
    /**
     * Busca tokens expirados
     * 
     * @param now Timestamp atual para comparação
     * @return Lista de tokens expirados
     */
    fun findExpiredTokens(now: Instant): List<ApplicationToken>
    
    /**
     * Busca tokens ativos por nome da aplicação
     * 
     * @param appName Nome da aplicação
     * @return Lista de tokens ativos da aplicação
     */
    fun findByAppNameAndIsActiveTrue(appName: String): List<ApplicationToken>
    
    /**
     * Verifica se existe token ativo para uma aplicação
     * 
     * @param appName Nome da aplicação
     * @return true se existir token ativo, false caso contrário
     */
    fun existsByAppNameAndIsActiveTrue(appName: String): Boolean
    
    /**
     * Busca tokens não utilizados há muito tempo
     * 
     * @param since Timestamp desde quando o token não foi usado
     * @return Lista de tokens inativos
     */
    fun findInactiveTokens(since: Instant): List<ApplicationToken>
    
    /**
     * Atualiza o timestamp de último uso de um token
     * 
     * @param tokenId ID do token
     * @param lastUsedAt Timestamp do último uso
     */
    fun updateLastUsedAt(tokenId: UUID, lastUsedAt: Instant)
    
    /**
     * Salva um novo token
     * 
     * @param token Token a ser salvo
     * @return Token salvo com ID gerado
     */
    fun save(token: ApplicationToken): ApplicationToken
    
    /**
     * Atualiza um token existente
     * 
     * @param token Token a ser atualizado
     * @return Token atualizado
     */
    fun update(token: ApplicationToken): ApplicationToken
    
    /**
     * Remove um token
     * 
     * @param tokenId ID do token a ser removido
     */
    fun deleteById(tokenId: UUID)
}
