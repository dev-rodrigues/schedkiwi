package com.schedkiwi.centraltelemetry.infrastructure.interceptors

import com.schedkiwi.centraltelemetry.application.services.TokenValidationService
import com.schedkiwi.centraltelemetry.domain.valueobjects.TokenValidationResult
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

/**
 * Interceptor para validação automática de tokens de aplicação
 * 
 * Este interceptor intercepta todas as requisições para endpoints protegidos
 * e valida automaticamente o token Bearer no header Authorization
 */
@Component
class TokenValidationInterceptor(
    private val tokenValidationService: TokenValidationService
) : HandlerInterceptor {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        val authHeader = request.getHeader(HttpHeaders.AUTHORIZATION)
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("Requisição sem token de autorização: ${request.requestURI}")
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token de autorização obrigatório")
            return false
        }

        val token = authHeader.substring(7) // Remove "Bearer " prefix
        val validationResult = tokenValidationService.validateToken(token)

        return when (validationResult) {
            is TokenValidationResult.Valid -> {
                // Adiciona informações da aplicação ao request para uso posterior
                request.setAttribute("appName", validationResult.appName)
                request.setAttribute("applicationId", validationResult.applicationId)
                logger.debug("Token válido para aplicação: ${validationResult.appName}")
                true
            }
            is TokenValidationResult.Invalid -> {
                logger.warn("Token inválido: ${validationResult.reason}")
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido: ${validationResult.reason}")
                false
            }
            is TokenValidationResult.Expired -> {
                logger.warn("Token expirado para aplicação: ${validationResult.appName}")
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token expirado")
                false
            }
        }
    }
}
