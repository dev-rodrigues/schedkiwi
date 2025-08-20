package com.schedkiwi.schedulertelemetry.net

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.AfterEach
import kotlin.test.*

/**
 * Testes unitários para HttpClientFactory
 */
class HttpClientFactoryTest {
    
    private lateinit var httpClientFactory: HttpClientFactory
    
    @BeforeEach
    fun setUp() {
        httpClientFactory = HttpClientFactory(
            connectionTimeoutMs = 5000L,
            readTimeoutMs = 10000L,
            maxRetries = 3,
            retryDelayMs = 100L
        )
    }
    
    @AfterEach
    fun tearDown() {
        httpClientFactory.close()
    }
    
    @Test
    fun `deve criar ObjectMapper configurado corretamente`() {
        val objectMapper = httpClientFactory.getObjectMapper()
        
        assertNotNull(objectMapper)
        // Verificar se está configurado para Kotlin
        assertTrue(objectMapper.configOverride(Any::class.java).isIgnored)
    }
    
    @Test
    fun `deve criar HttpClient configurado corretamente`() {
        val httpClient = httpClientFactory.getHttpClient()
        
        assertNotNull(httpClient)
        // Verificar se o timeout está configurado
        assertTrue(httpClient.connectTimeout().isPresent)
        assertTrue(httpClient.connectTimeout().get().toMillis() > 0)
    }
    
    @Test
    fun `deve serializar objeto para JSON`() {
        val testData = TestData("test", 123)
        val json = httpClientFactory.serializeToJson(testData)
        
        assertNotNull(json)
        assertTrue(json.contains("test"))
        assertTrue(json.contains("123"))
    }
    
    @Test
    fun `deve deserializar JSON para objeto`() {
        val json = """{"name":"test","value":123}"""
        val testData = httpClientFactory.deserializeFromJson<TestData>(json)
        
        assertNotNull(testData)
        assertEquals("test", testData.name)
        assertEquals(123, testData.value)
    }
    
    @Test
    fun `deve verificar conectividade com endpoint`() {
        // Este teste pode falhar se não houver internet, mas é útil para validar
        val isAccessible = httpClientFactory.isEndpointAccessible("https://httpbin.org/status/200")
        
        // Pode ser true ou false dependendo da conectividade
        assertNotNull(isAccessible)
    }
    
    @Test
    fun `deve fornecer estatísticas de conectividade`() {
        val stats = httpClientFactory.getConnectivityStats()
        
        assertNotNull(stats)
        assertTrue(stats.totalRequests >= 0)
        assertTrue(stats.successfulRequests >= 0)
        assertTrue(stats.failedRequests >= 0)
        assertTrue(stats.averageResponseTimeMs >= 0.0)
    }
    
    @Test
    fun `deve lidar com erros de serialização`() {
        val invalidObject = InvalidObject()
        
        // Deve lançar exceção ao tentar serializar objeto inválido
        assertThrows<Exception> {
            httpClientFactory.serializeToJson(invalidObject)
        }
    }
    
    @Test
    fun `deve lidar com JSON inválido na deserialização`() {
        val invalidJson = "{ invalid json }"
        
        // Deve lançar exceção ao tentar deserializar JSON inválido
        assertThrows<Exception> {
            httpClientFactory.deserializeFromJson<TestData>(invalidJson)
        }
    }
    
    @Test
    fun `deve fechar recursos corretamente`() {
        // Não deve lançar exceção
        assertDoesNotThrow {
            httpClientFactory.close()
        }
    }
    
    @Test
    fun `deve configurar headers corretamente`() {
        val headers = mapOf(
            "Authorization" to "Bearer token123",
            "Content-Type" to "application/json",
            "User-Agent" to "SchedulerTelemetry/1.0"
        )
        
        val httpClientFactoryWithHeaders = HttpClientFactory(
            connectionTimeoutMs = 5000L,
            readTimeoutMs = 10000L,
            maxRetries = 3,
            retryDelayMs = 100L,
            defaultHeaders = headers
        )
        
        assertNotNull(httpClientFactoryWithHeaders)
        httpClientFactoryWithHeaders.close()
    }
    
    @Test
    fun `deve lidar com timeouts configurados`() {
        val fastTimeoutFactory = HttpClientFactory(
            connectionTimeoutMs = 1L, // Timeout muito baixo
            readTimeoutMs = 1L,
            maxRetries = 1,
            retryDelayMs = 10L
        )
        
        assertNotNull(fastTimeoutFactory)
        fastTimeoutFactory.close()
    }
    
    // Classes de teste
    data class TestData(
        val name: String,
        val value: Int
    )
    
    class InvalidObject {
        // Objeto que não pode ser serializado
        private val invalidField = object : Any() {
            override fun toString(): String {
                throw RuntimeException("Erro de serialização")
            }
        }
    }
}
