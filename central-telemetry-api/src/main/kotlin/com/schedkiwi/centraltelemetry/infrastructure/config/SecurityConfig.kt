package com.schedkiwi.centraltelemetry.infrastructure.config

import com.schedkiwi.centraltelemetry.infrastructure.interceptors.TokenValidationInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Configuração de segurança para validação de tokens de aplicação
 * 
 * Esta configuração adiciona um interceptor que valida automaticamente
 * os tokens Bearer em todas as requisições para endpoints protegidos
 */
@Configuration
class SecurityConfig(
    private val tokenValidationInterceptor: TokenValidationInterceptor
) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(tokenValidationInterceptor)
            .addPathPatterns("/api/**") // Protege todos os endpoints da API
            .excludePathPatterns("/api/admin/**") // Exclui endpoints administrativos
    }
}
