package com.schedkiwi.schedulertelemetry.net

import com.schedkiwi.schedulertelemetry.core.SequenceManager
import com.schedkiwi.schedulertelemetry.scan.ScheduledScanner
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.net.InetAddress
import java.time.Instant
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * Componente para registro automático da aplicação no Gerenciador Central.
 * 
 * Este componente é executado no startup da aplicação e registra
 * automaticamente informações sobre a aplicação e seus jobs agendados.
 */
@Component
class Registrar @Autowired constructor(
    private val httpClientFactory: HttpClientFactory,
    private val sequenceManager: SequenceManager,
    private val scheduledScanner: ScheduledScanner,
    @Value("\${scheduler.telemetry.manager-url:http://localhost:8080}")
    private val managerUrl: String,
    @Value("\${scheduler.telemetry.register-path:/api/projects/register}")
    private val registerPath: String,
    @Value("\${scheduler.telemetry.auth-token:}")
    private val authToken: String,
    @Value("\${scheduler.telemetry.auto-register:true}")
    private val autoRegister: Boolean,
    @Value("\${scheduler.telemetry.register-retry-interval:30000}")
    private val retryIntervalMs: Long,
    @Value("\${scheduler.telemetry.max-register-attempts:5}")
    private val maxAttempts: Int
) {
    
    private val logger = LoggerFactory.getLogger(Registrar::class.java)
    
    /**
     * Executor para tentativas de registro em background
     */
    private val retryExecutor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    
    /**
     * Flag para controlar se o registro foi bem-sucedido
     */
    private var isRegistered = false
    
    /**
     * Contador de tentativas de registro
     */
    private var registerAttempts = 0
    
    /**
     * Informações da aplicação coletadas no startup
     */
    private var appInfo: ApplicationInfo? = null
    
    /**
     * Executado quando a aplicação está pronta
     */
    @EventListener(ApplicationReadyEvent::class)
    fun onApplicationReady() {
        if (!autoRegister) {
            logger.info("Registro automático desabilitado")
            return
        }
        
        logger.info("Aplicação pronta, iniciando registro no Gerenciador Central")
        
        try {
            // Coleta informações da aplicação
            collectApplicationInfo()
            
            // Tenta registro imediato
            performRegistration()
            
        } catch (e: Exception) {
            logger.error("Erro durante registro inicial", e)
            scheduleRetry()
        }
    }
    
    /**
     * Coleta informações da aplicação
     */
    private fun collectApplicationInfo() {
        val hostname = try {
            InetAddress.getLocalHost().hostName
        } catch (e: Exception) {
            "unknown-host"
        }
        
        val appName = System.getProperty("spring.application.name", "unknown-app")
        val port = System.getProperty("server.port", "8080").toIntOrNull() ?: 8080
        
        appInfo = ApplicationInfo(
            appName = appName,
            hostname = hostname,
            port = port,
            startupTime = Instant.now(),
            javaVersion = System.getProperty("java.version"),
            springVersion = org.springframework.core.SpringVersion.getVersion()
        )
        
        logger.debug("Informações da aplicação coletadas: {}", appInfo)
    }
    
    /**
     * Executa o registro da aplicação
     */
    private fun performRegistration(): CompletableFuture<Boolean> {
        if (isRegistered) {
            return CompletableFuture.completedFuture(true)
        }
        
        val appInfo = this.appInfo ?: throw IllegalStateException("Informações da aplicação não coletadas")
        
        try {
            // Descobre jobs agendados
            val scheduledJobs = scheduledScanner.discoverScheduledJobs()
            
            // Gera runId único para o registro
            val runId = "registration_${System.currentTimeMillis()}"
            val sequenceNumber = sequenceManager.getNextSequenceNumber(runId)
            
            // Cria mensagem de registro
            val registrationMessage = OutboundMessageFactory.createRegistrationMessage(
                runId = runId,
                jobId = "app-registration",
                appName = appInfo.appName,
                host = appInfo.hostname,
                port = appInfo.port,
                scheduledJobs = scheduledJobs,
                sequenceNumber = sequenceNumber,
                checksum = sequenceManager.calculateChecksum("$runId$sequenceNumber${appInfo.appName}")
            )
            
            // Prepara headers de autenticação
            val headers = mutableMapOf<String, String>()
            if (authToken.isNotEmpty()) {
                headers["Authorization"] = "Bearer $authToken"
            }
            
            // Envia registro
            val url = "$managerUrl$registerPath"
            return httpClientFactory.postMessageWithRetry(url, registrationMessage, headers)
                .thenApply { response ->
                    if (response.statusCode() in 200..299) {
                        isRegistered = true
                        registerAttempts = 0
                        
                        logger.info("Aplicação registrada com sucesso no Gerenciador Central: {} jobs descobertos", 
                            scheduledJobs.size)
                        
                        // Adiciona mensagem ao buffer para sincronização
                        sequenceManager.addMessageToBuffer(
                            runId,
                            com.schedkiwi.schedulertelemetry.core.SequencedMessage(
                                runId = runId,
                                sequenceNumber = sequenceNumber,
                                timestamp = Instant.now(),
                                messageType = "REGISTRATION",
                                payload = "App registration successful",
                                checksum = registrationMessage.checksum
                            )
                        )
                        
                        true
                    } else {
                        throw RuntimeException("HTTP ${response.statusCode()}: ${response.body()}")
                    }
                }
                .exceptionally { throwable ->
                    logger.error("Falha no registro da aplicação", throwable)
                    registerAttempts++
                    
                    if (registerAttempts < maxAttempts) {
                        scheduleRetry()
                    } else {
                        logger.error("Número máximo de tentativas de registro atingido")
                    }
                    
                    false
                }
                
        } catch (e: Exception) {
            logger.error("Erro durante registro da aplicação", e)
            registerAttempts++
            
            if (registerAttempts < maxAttempts) {
                scheduleRetry()
            }
            
            return CompletableFuture.completedFuture(false)
        }
    }
    
    /**
     * Agenda nova tentativa de registro
     */
    private fun scheduleRetry() {
        if (isRegistered || registerAttempts >= maxAttempts) {
            return
        }
        
        logger.info("Agendando nova tentativa de registro em {}ms (tentativa {}/{})", 
            retryIntervalMs, registerAttempts + 1, maxAttempts)
        
        retryExecutor.schedule({
            performRegistration()
        }, retryIntervalMs, TimeUnit.MILLISECONDS)
    }
    
    /**
     * Força novo registro (útil para testes ou reconfiguração)
     */
    fun forceRegistration(): CompletableFuture<Boolean> {
        logger.info("Forçando novo registro da aplicação")
        isRegistered = false
        registerAttempts = 0
        return performRegistration()
    }
    
    /**
     * Verifica se a aplicação está registrada
     */
    fun isApplicationRegistered(): Boolean {
        return isRegistered
    }
    
    /**
     * Obtém informações da aplicação
     */
    fun getApplicationInfo(): ApplicationInfo? {
        return appInfo
    }
    
    /**
     * Obtém estatísticas de registro
     */
    fun getRegistrationStats(): RegistrationStats {
        return RegistrationStats(
            isRegistered = isRegistered,
            registerAttempts = registerAttempts,
            maxAttempts = maxAttempts,
            lastAttemptTime = if (registerAttempts > 0) Instant.now() else null,
            appInfo = appInfo
        )
    }
    
    /**
     * Para o executor de retry
     */
    fun shutdown() {
        logger.info("Shutdown do Registrar iniciado")
        retryExecutor.shutdown()
        
        try {
            if (!retryExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                retryExecutor.shutdownNow()
            }
        } catch (e: InterruptedException) {
            retryExecutor.shutdownNow()
        }
        
        logger.info("Registrar parado")
    }
}

/**
 * Informações da aplicação
 */
data class ApplicationInfo(
    val appName: String,
    val hostname: String,
    val port: Int,
    val startupTime: Instant,
    val javaVersion: String,
    val springVersion: String
)

/**
 * Estatísticas de registro
 */
data class RegistrationStats(
    val isRegistered: Boolean,
    val registerAttempts: Int,
    val maxAttempts: Int,
    val lastAttemptTime: Instant?,
    val appInfo: ApplicationInfo?
)
