package com.schedkiwi.centraltelemetry.domain.valueobjects

import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals

/**
 * Testes unitários para TokenValidationResult sealed class
 */
class TokenValidationResultTest {

    @Test
    fun `deve criar TokenValidationResult Valid`() {
        // Given
        val appName = "test-app"
        val applicationId = UUID.randomUUID()

        // When
        val result = TokenValidationResult.Valid(appName, applicationId)

        // Then
        assertIs<TokenValidationResult.Valid>(result)
        assertEquals(appName, result.appName)
        assertEquals(applicationId, result.applicationId)
    }

    @Test
    fun `deve criar TokenValidationResult Invalid`() {
        // Given
        val reason = "Token not found"

        // When
        val result = TokenValidationResult.Invalid(reason)

        // Then
        assertIs<TokenValidationResult.Invalid>(result)
        assertEquals(reason, result.reason)
    }

    @Test
    fun `deve criar TokenValidationResult Expired`() {
        // Given
        val appName = "expired-app"

        // When
        val result = TokenValidationResult.Expired(appName)

        // Then
        assertIs<TokenValidationResult.Expired>(result)
        assertEquals(appName, result.appName)
    }

    @Test
    fun `deve distinguir diferentes tipos de TokenValidationResult`() {
        // Given
        val validResult = TokenValidationResult.Valid("app1", UUID.randomUUID())
        val invalidResult = TokenValidationResult.Invalid("Token invalid")
        val expiredResult = TokenValidationResult.Expired("app2")

        // When & Then
        assertIs<TokenValidationResult.Valid>(validResult)
        assertIs<TokenValidationResult.Invalid>(invalidResult)
        assertIs<TokenValidationResult.Expired>(expiredResult)
    }

    @Test
    fun `deve trabalhar com igualdade para TokenValidationResult Valid`() {
        // Given
        val applicationId = UUID.randomUUID()
        val result1 = TokenValidationResult.Valid("app-name", applicationId)
        val result2 = TokenValidationResult.Valid("app-name", applicationId)
        val result3 = TokenValidationResult.Valid("different-app", applicationId)

        // When & Then
        assertEquals(result1, result2)
        assertNotEquals(result1, result3)
    }

    @Test
    fun `deve trabalhar com igualdade para TokenValidationResult Invalid`() {
        // Given
        val result1 = TokenValidationResult.Invalid("Same reason")
        val result2 = TokenValidationResult.Invalid("Same reason")
        val result3 = TokenValidationResult.Invalid("Different reason")

        // When & Then
        assertEquals(result1, result2)
        assertNotEquals(result1, result3)
    }

    @Test
    fun `deve trabalhar com igualdade para TokenValidationResult Expired`() {
        // Given
        val result1 = TokenValidationResult.Expired("app-name")
        val result2 = TokenValidationResult.Expired("app-name")
        val result3 = TokenValidationResult.Expired("different-app")

        // When & Then
        assertEquals(result1, result2)
        assertNotEquals(result1, result3)
    }

    @Test
    fun `deve suportar diferentes cenários de validação`() {
        // Given & When
        val scenarios = listOf(
            TokenValidationResult.Valid("prod-app", UUID.randomUUID()),
            TokenValidationResult.Valid("test-app", UUID.randomUUID()),
            TokenValidationResult.Invalid("Token not found"),
            TokenValidationResult.Invalid("Token malformed"),
            TokenValidationResult.Invalid("Database error"),
            TokenValidationResult.Expired("old-app"),
            TokenValidationResult.Expired("legacy-system")
        )

        // Then
        assertEquals(7, scenarios.size)
        assertEquals(2, scenarios.count { it is TokenValidationResult.Valid })
        assertEquals(3, scenarios.count { it is TokenValidationResult.Invalid })
        assertEquals(2, scenarios.count { it is TokenValidationResult.Expired })
    }

    @Test
    fun `deve funcionar com when expression`() {
        // Given
        val results = listOf(
            TokenValidationResult.Valid("app", UUID.randomUUID()),
            TokenValidationResult.Invalid("error"),
            TokenValidationResult.Expired("expired-app")
        )

        // When & Then
        results.forEach { result ->
            val message = when (result) {
                is TokenValidationResult.Valid -> "Valid for ${result.appName}"
                is TokenValidationResult.Invalid -> "Invalid: ${result.reason}"
                is TokenValidationResult.Expired -> "Expired for ${result.appName}"
            }
            
            // Verifica que a mensagem foi gerada corretamente
            when (result) {
                is TokenValidationResult.Valid -> assertEquals("Valid for app", message)
                is TokenValidationResult.Invalid -> assertEquals("Invalid: error", message)
                is TokenValidationResult.Expired -> assertEquals("Expired for expired-app", message)
            }
        }
    }
}
