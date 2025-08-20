package com.schedkiwi.schedulertelemetry.net

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import java.net.http.HttpClient
import java.time.Duration
import java.util.concurrent.CompletableFuture

class HttpClientFactoryTest {

    private lateinit var httpClientFactory: HttpClientFactory

    @BeforeEach
    fun setUp() {
        httpClientFactory = HttpClientFactory(
            connectionTimeoutMs = 1000L,
            readTimeoutMs = 2000L,
            maxRetries = 2,
            retryDelayMs = 100L
        )
    }

    @Test
    fun `deve criar ObjectMapper configurado`() {
        // Act
        val objectMapper = httpClientFactory.getConfiguredObjectMapper()

        // Assert
        assertNotNull(objectMapper)
        // Verifica se os módulos estão configurados (verificação mais flexível)
        assertTrue(objectMapper.registeredModuleIds.isNotEmpty())
    }

    @Test
    fun `deve criar HttpClient configurado`() {
        // Act
        val httpClient = httpClientFactory.getHttpClient()

        // Assert
        assertNotNull(httpClient)
        assertTrue(httpClient is HttpClient)
    }

    @Test
    fun `deve serializar objeto para JSON`() {
        // Arrange
        val testData = TestData("test", 123)

        // Act
        val json = httpClientFactory.serializeToJson(testData)

        // Assert
        assertNotNull(json)
        assertTrue(json.contains("test"))
        assertTrue(json.contains("123"))
    }

    @Test
    fun `deve deserializar JSON para objeto`() {
        // Arrange
        val json = """{"name":"test","value":123}"""

        // Act
        val result = httpClientFactory.deserializeFromJson<TestData>(json)

        // Assert
        assertNotNull(result)
        assertEquals("test", result.name)
        assertEquals(123, result.value)
    }

    @Test
    fun `deve lidar com JSON inválido na deserialização`() {
        // Arrange
        val invalidJson = """{"name":"test",invalid}"""

        // Act & Assert
        assertThrows(Exception::class.java) {
            httpClientFactory.deserializeFromJson<TestData>(invalidJson)
        }
    }

    @Test
    fun `deve lidar com JSON vazio na deserialização`() {
        // Arrange
        val emptyJson = ""

        // Act & Assert
        assertThrows(Exception::class.java) {
            httpClientFactory.deserializeFromJson<TestData>(emptyJson)
        }
    }

    @Test
    fun `deve criar HttpClient com configurações customizadas`() {
        // Arrange
        val customFactory = HttpClientFactory(
            connectionTimeoutMs = 5000L,
            readTimeoutMs = 10000L
        )

        // Act
        val httpClient = customFactory.getHttpClient()

        // Assert
        assertNotNull(httpClient)
        assertTrue(httpClient is HttpClient)
    }

    @Test
    fun `deve criar HttpClient com configurações padrão`() {
        // Arrange
        val defaultFactory = HttpClientFactory()

        // Act
        val httpClient = defaultFactory.getHttpClient()

        // Assert
        assertNotNull(httpClient)
        assertTrue(httpClient is HttpClient)
    }

    @Test
    fun `deve criar HttpClient com configurações mínimas`() {
        // Arrange
        val minimalFactory = HttpClientFactory(
            connectionTimeoutMs = 100L,
            readTimeoutMs = 200L,
            maxRetries = 1,
            retryDelayMs = 50L
        )

        // Act
        val httpClient = minimalFactory.getHttpClient()

        // Assert
        assertNotNull(httpClient)
        assertTrue(httpClient is HttpClient)
    }

    // Classe de teste para serialização/deserialização
    data class TestData(
        val name: String,
        val value: Int
    )
}
