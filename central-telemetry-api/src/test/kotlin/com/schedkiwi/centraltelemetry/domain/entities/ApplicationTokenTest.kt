package com.schedkiwi.centraltelemetry.domain.entities

import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Testes unitários para a entidade ApplicationToken
 */
class ApplicationTokenTest {

    @Test
    fun `deve criar token com valores obrigatórios`() {
        // Given
        val appName = "test-app"
        val applicationId = UUID.randomUUID()
        val tokenHash = "abc123hash"

        // When
        val token = ApplicationToken(
            appName = appName,
            applicationId = applicationId,
            tokenHash = tokenHash
        )

        // Then
        assertEquals(appName, token.appName)
        assertEquals(applicationId, token.applicationId)
        assertEquals(tokenHash, token.tokenHash)
        assertNull(token.description)
        assertTrue(token.isActive)
        assertEquals("system", token.createdBy)
        assertTrue(token.id.toString().isNotEmpty())
        assertTrue(token.createdAt.isBefore(Instant.now().plusSeconds(1)))
        assertNull(token.expiresAt)
        assertNull(token.lastUsedAt)
    }

    @Test
    fun `deve criar token com todos os campos`() {
        // Given
        val id = UUID.randomUUID()
        val appName = "test-app"
        val applicationId = UUID.randomUUID()
        val tokenHash = "abc123hash"
        val description = "Token de teste"
        val isActive = false
        val createdAt = Instant.now().minusSeconds(3600)
        val expiresAt = Instant.now().plusSeconds(3600)
        val lastUsedAt = Instant.now().minusSeconds(60)
        val createdBy = "admin"

        // When
        val token = ApplicationToken(
            id = id,
            appName = appName,
            applicationId = applicationId,
            tokenHash = tokenHash,
            description = description,
            isActive = isActive,
            createdAt = createdAt,
            expiresAt = expiresAt,
            lastUsedAt = lastUsedAt,
            createdBy = createdBy
        )

        // Then
        assertEquals(id, token.id)
        assertEquals(appName, token.appName)
        assertEquals(applicationId, token.applicationId)
        assertEquals(tokenHash, token.tokenHash)
        assertEquals(description, token.description)
        assertEquals(isActive, token.isActive)
        assertEquals(createdAt, token.createdAt)
        assertEquals(expiresAt, token.expiresAt)
        assertEquals(lastUsedAt, token.lastUsedAt)
        assertEquals(createdBy, token.createdBy)
    }

    @Test
    fun `deve verificar se token não está expirado quando expiresAt é null`() {
        // Given
        val token = ApplicationToken(
            appName = "test-app",
            applicationId = UUID.randomUUID(),
            tokenHash = "abc123hash",
            expiresAt = null
        )

        // When & Then
        assertFalse(token.isExpired())
    }

    @Test
    fun `deve verificar se token não está expirado quando expiresAt é futuro`() {
        // Given
        val token = ApplicationToken(
            appName = "test-app",
            applicationId = UUID.randomUUID(),
            tokenHash = "abc123hash",
            expiresAt = Instant.now().plusSeconds(3600)
        )

        // When & Then
        assertFalse(token.isExpired())
    }

    @Test
    fun `deve verificar se token está expirado quando expiresAt é passado`() {
        // Given
        val token = ApplicationToken(
            appName = "test-app",
            applicationId = UUID.randomUUID(),
            tokenHash = "abc123hash",
            expiresAt = Instant.now().minusSeconds(60)
        )

        // When & Then
        assertTrue(token.isExpired())
    }

    @Test
    fun `deve verificar se token pode ser usado quando ativo e não expirado`() {
        // Given
        val token = ApplicationToken(
            appName = "test-app",
            applicationId = UUID.randomUUID(),
            tokenHash = "abc123hash",
            isActive = true,
            expiresAt = Instant.now().plusSeconds(3600)
        )

        // When & Then
        assertTrue(token.canBeUsed())
    }

    @Test
    fun `deve verificar se token pode ser usado quando ativo e sem expiração`() {
        // Given
        val token = ApplicationToken(
            appName = "test-app",
            applicationId = UUID.randomUUID(),
            tokenHash = "abc123hash",
            isActive = true,
            expiresAt = null
        )

        // When & Then
        assertTrue(token.canBeUsed())
    }

    @Test
    fun `não deve permitir uso de token quando inativo`() {
        // Given
        val token = ApplicationToken(
            appName = "test-app",
            applicationId = UUID.randomUUID(),
            tokenHash = "abc123hash",
            isActive = false,
            expiresAt = Instant.now().plusSeconds(3600)
        )

        // When & Then
        assertFalse(token.canBeUsed())
    }

    @Test
    fun `não deve permitir uso de token quando expirado`() {
        // Given
        val token = ApplicationToken(
            appName = "test-app",
            applicationId = UUID.randomUUID(),
            tokenHash = "abc123hash",
            isActive = true,
            expiresAt = Instant.now().minusSeconds(60)
        )

        // When & Then
        assertFalse(token.canBeUsed())
    }

    @Test
    fun `deve marcar token como usado`() {
        // Given
        val token = ApplicationToken(
            appName = "test-app",
            applicationId = UUID.randomUUID(),
            tokenHash = "abc123hash",
            lastUsedAt = null
        )
        val beforeMarkAsUsed = Instant.now()

        // When
        val updatedToken = token.markAsUsed()

        // Then
        assertNull(token.lastUsedAt) // original não deve ser modificado
        assertTrue(updatedToken.lastUsedAt != null)
        assertTrue(updatedToken.lastUsedAt!!.isAfter(beforeMarkAsUsed.minusSeconds(1)))
        assertTrue(updatedToken.lastUsedAt!!.isBefore(Instant.now().plusSeconds(1)))
    }

    @Test
    fun `deve desativar token`() {
        // Given
        val token = ApplicationToken(
            appName = "test-app",
            applicationId = UUID.randomUUID(),
            tokenHash = "abc123hash",
            isActive = true
        )

        // When
        val deactivatedToken = token.deactivate()

        // Then
        assertTrue(token.isActive) // original não deve ser modificado
        assertFalse(deactivatedToken.isActive)
        assertEquals(token.appName, deactivatedToken.appName)
        assertEquals(token.tokenHash, deactivatedToken.tokenHash)
    }

    @Test
    fun `deve renovar token com nova data de expiração`() {
        // Given
        val token = ApplicationToken(
            appName = "test-app",
            applicationId = UUID.randomUUID(),
            tokenHash = "abc123hash",
            expiresAt = Instant.now().plusSeconds(1800),
            lastUsedAt = null
        )
        val newExpiresAt = Instant.now().plusSeconds(7200)
        val beforeRenew = Instant.now()

        // When
        val renewedToken = token.renew(newExpiresAt)

        // Then
        // Token original não deve ser modificado
        assertNotEquals(newExpiresAt, token.expiresAt)
        assertNull(token.lastUsedAt)
        
        // Token renovado deve ter novos valores
        assertEquals(newExpiresAt, renewedToken.expiresAt)
        assertTrue(renewedToken.lastUsedAt != null)
        assertTrue(renewedToken.lastUsedAt!!.isAfter(beforeRenew.minusSeconds(1)))
        assertTrue(renewedToken.lastUsedAt!!.isBefore(Instant.now().plusSeconds(1)))
        assertEquals(token.appName, renewedToken.appName)
        assertEquals(token.tokenHash, renewedToken.tokenHash)
    }

    @Test
    fun `deve preservar outros campos ao marcar como usado`() {
        // Given
        val originalToken = ApplicationToken(
            appName = "test-app",
            applicationId = UUID.randomUUID(),
            tokenHash = "abc123hash",
            description = "Token de teste",
            isActive = true,
            expiresAt = Instant.now().plusSeconds(3600),
            createdBy = "admin"
        )

        // When
        val markedToken = originalToken.markAsUsed()

        // Then
        assertEquals(originalToken.appName, markedToken.appName)
        assertEquals(originalToken.applicationId, markedToken.applicationId)
        assertEquals(originalToken.tokenHash, markedToken.tokenHash)
        assertEquals(originalToken.description, markedToken.description)
        assertEquals(originalToken.isActive, markedToken.isActive)
        assertEquals(originalToken.expiresAt, markedToken.expiresAt)
        assertEquals(originalToken.createdBy, markedToken.createdBy)
    }

    @Test
    fun `deve preservar outros campos ao desativar`() {
        // Given
        val originalToken = ApplicationToken(
            appName = "test-app",
            applicationId = UUID.randomUUID(),
            tokenHash = "abc123hash",
            description = "Token de teste",
            expiresAt = Instant.now().plusSeconds(3600),
            lastUsedAt = Instant.now().minusSeconds(300),
            createdBy = "admin"
        )

        // When
        val deactivatedToken = originalToken.deactivate()

        // Then
        assertEquals(originalToken.appName, deactivatedToken.appName)
        assertEquals(originalToken.applicationId, deactivatedToken.applicationId)
        assertEquals(originalToken.tokenHash, deactivatedToken.tokenHash)
        assertEquals(originalToken.description, deactivatedToken.description)
        assertEquals(originalToken.expiresAt, deactivatedToken.expiresAt)
        assertEquals(originalToken.lastUsedAt, deactivatedToken.lastUsedAt)
        assertEquals(originalToken.createdBy, deactivatedToken.createdBy)
    }

    @Test
    fun `deve preservar outros campos ao renovar`() {
        // Given
        val originalToken = ApplicationToken(
            appName = "test-app",
            applicationId = UUID.randomUUID(),
            tokenHash = "abc123hash",
            description = "Token de teste",
            isActive = true,
            createdBy = "admin"
        )
        val newExpiresAt = Instant.now().plusSeconds(7200)

        // When
        val renewedToken = originalToken.renew(newExpiresAt)

        // Then
        assertEquals(originalToken.appName, renewedToken.appName)
        assertEquals(originalToken.applicationId, renewedToken.applicationId)
        assertEquals(originalToken.tokenHash, renewedToken.tokenHash)
        assertEquals(originalToken.description, renewedToken.description)
        assertEquals(originalToken.isActive, renewedToken.isActive)
        assertEquals(originalToken.createdBy, renewedToken.createdBy)
    }

    @Test
    fun `deve trabalhar com copy devido ao data class`() {
        // Given
        val token = ApplicationToken(
            appName = "test-app",
            applicationId = UUID.randomUUID(),
            tokenHash = "abc123hash"
        )

        // When
        val copiedToken = token.copy(description = "Nova descrição")

        // Then
        assertEquals("Nova descrição", copiedToken.description)
        assertEquals(token.appName, copiedToken.appName)
        assertEquals(token.tokenHash, copiedToken.tokenHash)
        assertNull(token.description) // original deve permanecer inalterado
    }
}
