package com.schedkiwi.centraltelemetry.application.services

import com.schedkiwi.centraltelemetry.domain.ports.ApplicationTokenRepository
import com.schedkiwi.centraltelemetry.domain.valueobjects.TokenValidationResult
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.security.MessageDigest
import java.time.Instant
import java.util.*

/**
 * Serviço para validação de tokens de aplicação
 * 
 * Este serviço valida tokens enviados pelas aplicações clientes
 * e retorna informações sobre a validade e aplicação associada
 */
@Service
class TokenValidationService(
    private val applicationTokenRepository: ApplicationTokenRepository
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Valida um token de aplicação
     * 
     * @param token Token em texto plano enviado pela aplicação
     * @return Resultado da validação com informações da aplicação
     */
    fun validateToken(token: String): TokenValidationResult {
        try {
            // Gera hash SHA-256 do token para comparação com o banco
            val tokenHash = generateTokenHash(token)
            
            // Busca o token no banco de dados
            val tokenEntity = applicationTokenRepository.findByTokenHash(tokenHash)
            
            if (tokenEntity == null) {
                logger.debug("Token não encontrado no banco: ${tokenHash.take(8)}...")
                return TokenValidationResult.Invalid("Token não encontrado")
            }

            // Verifica se o token está ativo
            if (!tokenEntity.isActive) {
                logger.debug("Token inativo para aplicação: ${tokenEntity.appName}")
                return TokenValidationResult.Invalid("Token inativo")
            }

            // Verifica se o token não expirou
            if (tokenEntity.expiresAt != null && tokenEntity.expiresAt.isBefore(Instant.now())) {
                logger.debug("Token expirado para aplicação: ${tokenEntity.appName}")
                return TokenValidationResult.Expired(tokenEntity.appName)
            }

            // Atualiza último uso do token
            applicationTokenRepository.updateLastUsedAt(tokenEntity.id, Instant.now())
            
            logger.debug("Token válido para aplicação: ${tokenEntity.appName}")
            return TokenValidationResult.Valid(
                appName = tokenEntity.appName,
                applicationId = tokenEntity.applicationId
            )

        } catch (e: Exception) {
            logger.error("Erro ao validar token: ${e.message}", e)
            return TokenValidationResult.Invalid("Erro interno na validação")
        }
    }

    /**
     * Gera hash SHA-256 de um token
     * 
     * @param token Token em texto plano
     * @return Hash SHA-256 do token
     */
    private fun generateTokenHash(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(token.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
