package com.schedkiwi.schedulertelemetry.net

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.concurrent.CompletableFuture

/**
 * Factory para criar e configurar HttpClient para comunicação com o Gerenciador Central.
 * 
 * Esta implementação usa java.net.http.HttpClient (Java 11+) para evitar
 * dependências com spring-boot-starter-web e possíveis conflitos.
 */
class HttpClientFactory(
    private val connectionTimeoutMs: Long = 5000L,
    private val readTimeoutMs: Long = 10000L,
    private val maxRetries: Int = 3,
    private val retryDelayMs: Long = 1000L
) {
    
    /**
     * Jackson ObjectMapper configurado para Kotlin e Java Time
     */
    val objectMapper = ObjectMapper()
        .registerModule(KotlinModule.Builder().build())
        .registerModule(JavaTimeModule())
    
    /**
     * HttpClient configurado com timeouts e retry
     */
    private val httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofMillis(connectionTimeoutMs))
        .build()
    
    /**
     * Obtém o ObjectMapper configurado
     */
    fun getConfiguredObjectMapper(): ObjectMapper = objectMapper

    /**
     * Obtém o HttpClient configurado
     */
    fun getHttpClient(): HttpClient = httpClient

    /**
     * Serializa um objeto para JSON
     */
    fun serializeToJson(obj: Any): String {
        return objectMapper.writeValueAsString(obj)
    }

    /**
     * Deserializa JSON para um objeto do tipo especificado
     */
    inline fun <reified T> deserializeFromJson(json: String): T {
        return objectMapper.readValue(json, objectMapper.typeFactory.constructType(T::class.java))
    }

    /**
     * Envia uma mensagem POST para o endpoint especificado
     */
    fun postMessage(
        url: String,
        message: OutboundMessage,
        headers: Map<String, String> = emptyMap()
    ): CompletableFuture<HttpResponse<String>> {
        val jsonPayload = objectMapper.writeValueAsString(message)
        
        val requestBuilder = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofMillis(readTimeoutMs))
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
        
        // Adiciona headers customizados
        headers.forEach { (key, value) ->
            requestBuilder.header(key, value)
        }
        
        val request = requestBuilder
            .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
            .build()
        
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
    }
    
    /**
     * Envia uma mensagem POST com retry automático
     */
    fun postMessageWithRetry(
        url: String,
        message: OutboundMessage,
        headers: Map<String, String> = emptyMap()
    ): CompletableFuture<HttpResponse<String>> {
        return postMessageWithRetryInternal(url, message, headers, 0)
    }
    
    /**
     * Implementação interna do retry
     */
    private fun postMessageWithRetryInternal(
        url: String,
        message: OutboundMessage,
        headers: Map<String, String>,
        attempt: Int
    ): CompletableFuture<HttpResponse<String>> {
        return postMessage(url, message, headers)
            .thenApply { response ->
                if (response.statusCode() in 200..299) {
                    response
                } else {
                    throw HttpException("HTTP ${response.statusCode()}: ${response.body()}")
                }
            }
            .exceptionally { throwable ->
                if (attempt < maxRetries) {
                    // Aguarda antes de tentar novamente
                    Thread.sleep(retryDelayMs * (attempt + 1))
                    return@exceptionally postMessageWithRetryInternal(url, message, headers, attempt + 1).join()
                } else {
                    throw RuntimeException("Failed after $maxRetries attempts", throwable)
                }
            }
    }
    
    /**
     * Envia uma mensagem POST de forma síncrona
     */
    fun postMessageSync(
        url: String,
        message: OutboundMessage,
        headers: Map<String, String> = emptyMap()
    ): HttpResponse<String> {
        return postMessage(url, message, headers).join()
    }
    
    /**
     * Envia uma mensagem POST com retry de forma síncrona
     */
    fun postMessageWithRetrySync(
        url: String,
        message: OutboundMessage,
        headers: Map<String, String> = emptyMap()
    ): HttpResponse<String> {
        return postMessageWithRetry(url, message, headers).join()
    }
    
    /**
     * Faz uma requisição GET para o endpoint especificado
     */
    fun getMessage(
        url: String,
        headers: Map<String, String> = emptyMap()
    ): CompletableFuture<HttpResponse<String>> {
        val requestBuilder = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofMillis(readTimeoutMs))
            .header("Accept", "application/json")
        
        // Adiciona headers customizados
        headers.forEach { (key, value) ->
            requestBuilder.header(key, value)
        }
        
        val request = requestBuilder
            .GET()
            .build()
        
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
    }
    
    /**
     * Faz uma requisição GET de forma síncrona
     */
    fun getMessageSync(
        url: String,
        headers: Map<String, String> = emptyMap()
    ): HttpResponse<String> {
        return getMessage(url, headers).join()
    }
    
    /**
     * Verifica se o endpoint está acessível
     */
    fun isEndpointAccessible(url: String): Boolean {
        return try {
            val response = getMessageSync(url)
            response.statusCode() in 200..299
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Obtém estatísticas de conectividade
     */
    fun getConnectivityStats(): ConnectivityStats {
        return ConnectivityStats(
            connectionTimeoutMs = connectionTimeoutMs,
            readTimeoutMs = readTimeoutMs,
            maxRetries = maxRetries,
            retryDelayMs = retryDelayMs
        )
    }
    
    /**
     * Fecha o HttpClient (útil para cleanup)
     */
    fun close() {
        // HttpClient não precisa ser fechado explicitamente
        // mas podemos limpar recursos se necessário
    }
}

/**
 * Exceção para erros HTTP
 */
class HttpException(message: String) : RuntimeException(message)

/**
 * Estatísticas de conectividade
 */
data class ConnectivityStats(
    val connectionTimeoutMs: Long,
    val readTimeoutMs: Long,
    val maxRetries: Int,
    val retryDelayMs: Long
)
