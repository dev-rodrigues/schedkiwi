package com.schedkiwi.schedulertelemetry.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty

/**
 * Propriedades de configuração para telemetria de schedulers.
 * 
 * Essas propriedades permitem customizar o comportamento da biblioteca
 * através do application.properties ou application.yml.
 */
@ConfigurationProperties(prefix = "scheduler.telemetry")
data class TelemetryProperties(
    
    /**
     * Se a telemetria está habilitada
     */
    var enabled: Boolean = true,
    
    /**
     * URL base do Gerenciador Central
     */
    var managerUrl: String = "http://localhost:8080",
    
    /**
     * Configurações de endpoints
     */
    @NestedConfigurationProperty
    var endpoints: EndpointProperties = EndpointProperties(),
    
    /**
     * Configurações de autenticação
     */
    @NestedConfigurationProperty
    var auth: AuthProperties = AuthProperties(),
    
    /**
     * Configurações de retry
     */
    @NestedConfigurationProperty
    var retry: RetryProperties = RetryProperties(),
    
    /**
     * Configurações de fila
     */
    @NestedConfigurationProperty
    var queue: QueueProperties = QueueProperties(),
    
    /**
     * Configurações de progresso
     */
    @NestedConfigurationProperty
    var progress: ProgressProperties = ProgressProperties(),
    
    /**
     * Configurações de sequência
     */
    @NestedConfigurationProperty
    var sequence: SequenceProperties = SequenceProperties(),
    
    /**
     * Configurações de registro automático
     */
    @NestedConfigurationProperty
    var registration: RegistrationProperties = RegistrationProperties()
)

/**
 * Configurações de endpoints
 */
data class EndpointProperties(
    /**
     * Path para registro de aplicações
     */
    var registerPath: String = "/api/projects/register",
    
    /**
     * Path para relatórios de execução
     */
    var reportPath: String = "/api/executions/report",
    
    /**
     * Path para atualizações de progresso
     */
    var progressPath: String = "/api/executions/progress",
    
    /**
     * Path para consulta de status
     */
    var statusPath: String = "/api/executions/{runId}/status",
    
    /**
     * Path para mensagens sequenciais
     */
    var sequencePath: String = "/api/executions/sequence",
    
    /**
     * Path para sincronização
     */
    var syncPath: String = "/api/executions/{runId}/sync"
)

/**
 * Configurações de autenticação
 */
data class AuthProperties(
    /**
     * Token de autenticação (Bearer)
     */
    var token: String = "",
    
    /**
     * Timeout de autenticação em ms
     */
    var timeoutMs: Long = 5000L
)

/**
 * Configurações de retry
 */
data class RetryProperties(
    /**
     * Número máximo de tentativas
     */
    var maxRetries: Int = 5,
    
    /**
     * Backoff base em milissegundos
     */
    var baseBackoffMs: Long = 500L,
    
    /**
     * Multiplicador de backoff exponencial
     */
    var backoffMultiplier: Double = 2.0,
    
    /**
     * Backoff máximo em milissegundos
     */
    var maxBackoffMs: Long = 30000L
)

/**
 * Configurações de fila
 */
data class QueueProperties(
    /**
     * Capacidade da fila principal
     */
    var capacity: Int = 10000,
    
    /**
     * Número máximo de workers concorrentes
     */
    var maxWorkers: Int = 10,
    
    /**
     * Timeout de polling em milissegundos
     */
    var pollTimeoutMs: Long = 1000L
)

/**
 * Configurações de progresso
 */
data class ProgressProperties(
    /**
     * Intervalo de atualização em milissegundos
     */
    var updateInterval: Long = 1000L,
    
    /**
     * Se deve rastrear progresso por padrão
     */
    var enabledByDefault: Boolean = true,
    
    /**
     * Número máximo de snapshots de histórico
     */
    var maxHistorySnapshots: Int = 100,
    
    /**
     * Threshold para progresso estagnado em ms
     */
    var stagnantThresholdMs: Long = 30000L
)

/**
 * Configurações de sequência
 */
data class SequenceProperties(
    /**
     * Se deve validar sequência
     */
    var validation: Boolean = true,
    
    /**
     * Tolerância para mensagens fora de ordem em ms
     */
    var outOfOrderToleranceMs: Long = 1000L,
    
    /**
     * Tamanho do buffer circular
     */
    var bufferSize: Int = 1000,
    
    /**
     * Se deve calcular checksum
     */
    var enableChecksum: Boolean = true
)

/**
 * Configurações de registro automático
 */
data class RegistrationProperties(
    /**
     * Se deve registrar automaticamente no startup
     */
    var autoRegister: Boolean = true,
    
    /**
     * Intervalo entre tentativas de registro em ms
     */
    var retryIntervalMs: Long = 30000L,
    
    /**
     * Número máximo de tentativas de registro
     */
    var maxAttempts: Int = 5,
    
    /**
     * Delay inicial antes do primeiro registro em ms
     */
    var initialDelayMs: Long = 2000L
)
