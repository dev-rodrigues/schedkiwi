package com.schedkiwi.centraltelemetry.domain.entities

import java.time.Instant
import java.util.*

/**
 * Token de autenticação para uma aplicação
 * 
 * Esta entidade representa um token de acesso que permite
 * que uma aplicação cliente se autentique na API Central
 */
data class ApplicationToken(
    val id: UUID = UUID.randomUUID(),
    val appName: String,
    val applicationId: UUID,
    val tokenHash: String,
    val description: String? = null,
    val isActive: Boolean = true,
    val createdAt: Instant = Instant.now(),
    val expiresAt: Instant? = null,
    val lastUsedAt: Instant? = null,
    val createdBy: String = "system"
) {
    
    /**
     * Verifica se o token está expirado
     * 
     * @return true se o token estiver expirado, false caso contrário
     */
    fun isExpired(): Boolean {
        return expiresAt != null && expiresAt.isBefore(Instant.now())
    }
    
    /**
     * Verifica se o token pode ser usado
     * 
     * @return true se o token estiver ativo e não expirado
     */
    fun canBeUsed(): Boolean {
        return isActive && !isExpired()
    }
    
    /**
     * Marca o token como usado
     * 
     * @return Nova instância com lastUsedAt atualizado
     */
    fun markAsUsed(): ApplicationToken {
        return copy(lastUsedAt = Instant.now())
    }
    
    /**
     * Desativa o token
     * 
     * @return Nova instância com isActive = false
     */
    fun deactivate(): ApplicationToken {
        return copy(isActive = false)
    }
    
    /**
     * Renova o token com nova data de expiração
     * 
     * @param newExpiresAt Nova data de expiração
     * @return Nova instância com nova data de expiração
     */
    fun renew(newExpiresAt: Instant): ApplicationToken {
        return copy(
            expiresAt = newExpiresAt,
            lastUsedAt = Instant.now()
        )
    }
}
