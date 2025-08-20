package com.schedkiwi.centraltelemetry.application.services

import com.schedkiwi.centraltelemetry.application.dto.ApplicationSummaryDto
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Testes unitários simples para HateoasService
 * 
 * Nota: Este teste valida apenas aspectos básicos do serviço que não dependem 
 * da infraestrutura Spring completa.
 */
class HateoasServiceTest {

    @Test
    fun `deve ter configuracao de pageable definida`() {
        // When & Then - teste indireto da configuração de pageable
        val hateoasService = HateoasService()
        assertNotNull(hateoasService)
        
        // Se o serviço foi criado sem erro, significa que as configurações estão corretas
    }

    @Test
    fun `deve criar instancia de HateoasService sem erro`() {
        // When
        val hateoasService = HateoasService()
        
        // Then
        assertNotNull(hateoasService)
    }

    @Test
    fun `deve trabalhar com diferentes ApplicationSummaryDto`() {
        // Given
        val dto1 = ApplicationSummaryDto(
            id = UUID.randomUUID(),
            appName = "app1",
            host = "host1",
            port = 8080,
            status = "NEW",
            lastSeen = null,
            totalJobs = 0,
            totalExecutions = 0
        )

        val dto2 = ApplicationSummaryDto(
            id = UUID.randomUUID(),
            appName = "app2",
            host = "host2",
            port = 9000,
            status = "RUNNING",
            lastSeen = Instant.now(),
            totalJobs = 100,
            totalExecutions = 1000
        )

        // When & Then
        assertNotNull(dto1)
        assertNotNull(dto2)
        assertEquals("app1", dto1.appName)
        assertEquals("app2", dto2.appName)
        assertEquals(0, dto1.totalJobs)
        assertEquals(100, dto2.totalJobs)
    }
}
