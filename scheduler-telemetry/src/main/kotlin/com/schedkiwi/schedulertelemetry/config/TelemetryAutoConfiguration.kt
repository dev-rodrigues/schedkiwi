package com.schedkiwi.schedulertelemetry.config

import com.schedkiwi.schedulertelemetry.aop.MonitoredScheduledAspect
import com.schedkiwi.schedulertelemetry.core.SchedulerTelemetryImpl
import com.schedkiwi.schedulertelemetry.core.SequenceManager
import com.schedkiwi.schedulertelemetry.net.*
import com.schedkiwi.schedulertelemetry.scan.ScheduledScanner
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.EnableAspectJAutoProxy
import jakarta.annotation.PreDestroy

/**
 * Auto-configuração principal para telemetria de schedulers.
 * 
 * Esta configuração cria e configura automaticamente todos os beans
 * necessários para o funcionamento da biblioteca quando as condições
 * são atendidas.
 */
@AutoConfiguration
@ConditionalOnClass(SchedulerTelemetryImpl::class)
@ConditionalOnProperty(
    prefix = "scheduler.telemetry",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = true
)
@EnableConfigurationProperties(TelemetryProperties::class)
@EnableAspectJAutoProxy
class TelemetryAutoConfiguration {
    
    private val logger = LoggerFactory.getLogger(TelemetryAutoConfiguration::class.java)
    
    init {
        logger.info("Inicializando auto-configuração de telemetria de schedulers")
    }
    
    /**
     * Configura as propriedades de telemetria
     */
    @Bean
    @ConditionalOnMissingBean
    fun telemetryProperties(): TelemetryProperties {
        return TelemetryProperties()
    }
    
    /**
     * Configura o gerenciador de sequência
     */
    @Bean
    @ConditionalOnMissingBean
    fun sequenceManager(properties: TelemetryProperties): SequenceManager {
        logger.debug("Configurando SequenceManager com bufferSize={}, outOfOrderTolerance={}ms",
            properties.sequence.bufferSize, properties.sequence.outOfOrderToleranceMs)
        
        return SequenceManager(
            outOfOrderToleranceMs = properties.sequence.outOfOrderToleranceMs,
            bufferSize = properties.sequence.bufferSize
        )
    }
    
    /**
     * Configura o factory de clientes HTTP
     */
    @Bean
    @ConditionalOnMissingBean
    fun httpClientFactory(properties: TelemetryProperties): HttpClientFactory {
        logger.debug("Configurando HttpClientFactory com maxRetries={}, baseBackoff={}ms",
            properties.retry.maxRetries, properties.retry.baseBackoffMs)
        
        return HttpClientFactory(
            connectionTimeoutMs = properties.auth.timeoutMs,
            readTimeoutMs = properties.auth.timeoutMs,
            maxRetries = properties.retry.maxRetries,
            retryDelayMs = properties.retry.baseBackoffMs
        )
    }
    
    /**
     * Configura a implementação de telemetria
     */
    @Bean
    @ConditionalOnMissingBean
    fun schedulerTelemetryImpl(): SchedulerTelemetryImpl {
        logger.debug("Configurando SchedulerTelemetryImpl")
        return SchedulerTelemetryImpl()
    }
    
    /**
     * Configura o scanner de jobs agendados
     */
    @Bean
    @ConditionalOnMissingBean
    fun scheduledScanner(): ScheduledScanner {
        logger.debug("Configurando ScheduledScanner")
        // O scanner será criado pelo Spring automaticamente com @Component
        return ScheduledScanner(org.springframework.context.ApplicationContext::class.java.cast(null))
    }
    
    /**
     * Configura o dispatcher de sequência
     */
    @Bean
    @ConditionalOnMissingBean
    fun sequenceDispatcher(
        httpClientFactory: HttpClientFactory,
        sequenceManager: SequenceManager,
        properties: TelemetryProperties
    ): SequenceDispatcher {
        logger.debug("Configurando SequenceDispatcher")
        
        val headers = mutableMapOf<String, String>()
        if (properties.auth.token.isNotEmpty()) {
            headers["Authorization"] = "Bearer ${properties.auth.token}"
        }
        
        return SequenceDispatcher(
            httpClientFactory = httpClientFactory,
            sequenceManager = sequenceManager,
            baseUrl = properties.managerUrl,
            endpoint = properties.endpoints.sequencePath,
            headers = headers,
            maxRetries = properties.retry.maxRetries,
            baseBackoffMs = properties.retry.baseBackoffMs
        )
    }
    
    /**
     * Configura o dispatcher de progresso
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
        prefix = "scheduler.telemetry.progress",
        name = ["enabledByDefault"],
        havingValue = "true",
        matchIfMissing = true
    )
    fun progressDispatcher(
        httpClientFactory: HttpClientFactory,
        sequenceManager: SequenceManager,
        properties: TelemetryProperties
    ): ProgressDispatcher {
        logger.debug("Configurando ProgressDispatcher com updateInterval={}ms",
            properties.progress.updateInterval)
        
        val headers = mutableMapOf<String, String>()
        if (properties.auth.token.isNotEmpty()) {
            headers["Authorization"] = "Bearer ${properties.auth.token}"
        }
        
        return ProgressDispatcher(
            httpClientFactory = httpClientFactory,
            sequenceManager = sequenceManager,
            baseUrl = properties.managerUrl,
            endpoint = properties.endpoints.progressPath,
            headers = headers,
            updateIntervalMs = properties.progress.updateInterval,
            maxRetries = properties.retry.maxRetries,
            baseBackoffMs = properties.retry.baseBackoffMs
        )
    }
    
    /**
     * Configura o dispatcher de relatórios
     */
    @Bean
    @ConditionalOnMissingBean
    fun reportDispatcher(
        httpClientFactory: HttpClientFactory,
        sequenceManager: SequenceManager,
        properties: TelemetryProperties
    ): ReportDispatcher {
        logger.debug("Configurando ReportDispatcher com queueCapacity={}, maxWorkers={}",
            properties.queue.capacity, properties.queue.maxWorkers)
        
        val headers = mutableMapOf<String, String>()
        if (properties.auth.token.isNotEmpty()) {
            headers["Authorization"] = "Bearer ${properties.auth.token}"
        }
        
        return ReportDispatcher(
            httpClientFactory = httpClientFactory,
            sequenceManager = sequenceManager,
            baseUrl = properties.managerUrl,
            endpoint = properties.endpoints.reportPath,
            headers = headers,
            maxRetries = properties.retry.maxRetries,
            baseBackoffMs = properties.retry.baseBackoffMs,
            maxConcurrentReports = properties.queue.maxWorkers,
            queueCapacity = properties.queue.capacity
        )
    }
    
    /**
     * Configura o registrar automático
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
        prefix = "scheduler.telemetry.registration",
        name = ["autoRegister"],
        havingValue = "true",
        matchIfMissing = true
    )
    fun registrar(
        httpClientFactory: HttpClientFactory,
        sequenceManager: SequenceManager,
        scheduledScanner: ScheduledScanner,
        properties: TelemetryProperties
    ): Registrar {
        logger.debug("Configurando Registrar com autoRegister={}, maxAttempts={}",
            properties.registration.autoRegister, properties.registration.maxAttempts)
        
        return Registrar(
            httpClientFactory = httpClientFactory,
            sequenceManager = sequenceManager,
            scheduledScanner = scheduledScanner,
            managerUrl = properties.managerUrl,
            registerPath = properties.endpoints.registerPath,
            authToken = properties.auth.token,
            autoRegister = properties.registration.autoRegister,
            retryIntervalMs = properties.registration.retryIntervalMs,
            maxAttempts = properties.registration.maxAttempts
        )
    }
    
    /**
     * Configura o aspecto AOP
     */
    @Bean
    @ConditionalOnMissingBean
    fun monitoredScheduledAspect(
        telemetryImpl: SchedulerTelemetryImpl,
        sequenceDispatcher: SequenceDispatcher
    ): MonitoredScheduledAspect {
        logger.debug("Configurando MonitoredScheduledAspect")
        return MonitoredScheduledAspect(telemetryImpl, sequenceDispatcher)
    }
    
    /**
     * Bean de limpeza para shutdown gracioso
     */
    @Bean
    @ConditionalOnMissingBean
    fun telemetryShutdownManager(
        sequenceDispatcher: SequenceDispatcher,
        progressDispatcher: ProgressDispatcher?,
        reportDispatcher: ReportDispatcher,
        registrar: Registrar?
    ): TelemetryShutdownManager {
        logger.debug("Configurando TelemetryShutdownManager para shutdown gracioso")
        return TelemetryShutdownManager(
            sequenceDispatcher,
            progressDispatcher,
            reportDispatcher,
            registrar
        )
    }
}

/**
 * Gerenciador de shutdown para limpeza de recursos
 */
class TelemetryShutdownManager(
    private val sequenceDispatcher: SequenceDispatcher,
    private val progressDispatcher: ProgressDispatcher?,
    private val reportDispatcher: ReportDispatcher,
    private val registrar: Registrar?
) {
    
    private val logger = LoggerFactory.getLogger(TelemetryShutdownManager::class.java)
    
    /**
     * Shutdown hook registrado automaticamente pelo Spring
     */
    @PreDestroy
    fun shutdown() {
        logger.info("Iniciando shutdown gracioso da telemetria")
        
        try {
            // Para progressDispatcher se existir
            progressDispatcher?.shutdown()
            
            // Para reportDispatcher
            reportDispatcher.shutdown()
            
            // Para sequenceDispatcher
            sequenceDispatcher.shutdown()
            
            // Para registrar se existir
            registrar?.shutdown()
            
            logger.info("Shutdown gracioso da telemetria concluído")
            
        } catch (e: Exception) {
            logger.error("Erro durante shutdown da telemetria", e)
        }
    }
}
