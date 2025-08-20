package com.schedkiwi.centraltelemetry.domain.valueobjects

import java.util.*

/**
 * Resultado da validação de um token de aplicação
 * 
 * Esta sealed class representa os diferentes estados possíveis
 * de validação de um token enviado por uma aplicação cliente
 */
sealed class TokenValidationResult {
    
    /**
     * Token válido e ativo
     * 
     * @param appName Nome da aplicação associada ao token
     * @param applicationId ID único da aplicação no sistema
     */
    data class Valid(
        val appName: String,
        val applicationId: UUID
    ) : TokenValidationResult()
    
    /**
     * Token inválido
     * 
     * @param reason Motivo da invalidação
     */
    data class Invalid(
        val reason: String
    ) : TokenValidationResult()
    
    /**
     * Token expirado
     * 
     * @param appName Nome da aplicação associada ao token expirado
     */
    data class Expired(
        val appName: String
    ) : TokenValidationResult()
}
