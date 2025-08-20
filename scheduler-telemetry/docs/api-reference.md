# API Reference - Scheduler Telemetry Library

## 📚 **Visão Geral**

Esta documentação descreve todas as APIs públicas da biblioteca `scheduler-telemetry`, incluindo interfaces, classes, anotações e configurações.

## 🔧 **Configuração**

### TelemetryProperties

Classe de configuração que define todas as propriedades configuráveis da biblioteca.

```kotlin
@ConfigurationProperties(prefix = "scheduler.telemetry")
data class TelemetryProperties(
    val enabled: Boolean = true,
    val manager: ManagerProperties = ManagerProperties(),
    val retry: RetryProperties = RetryProperties(),
    val queue: QueueProperties = QueueProperties(),
    val progress: ProgressProperties = ProgressProperties(),
    val sequence: SequenceProperties = SequenceProperties(),
    val registration: RegistrationProperties = RegistrationProperties()
)
```

#### Propriedades de Configuração

| Propriedade | Padrão | Descrição |
|-------------|--------|-----------|
| `scheduler.telemetry.enabled` | `true` | Habilita/desabilita a telemetria |
| `scheduler.telemetry.manager.url` | `http://localhost:8080` | URL do Gerenciador Central |
| `scheduler.telemetry.manager.auth.token` | `null` | Token de autenticação |
| `scheduler.telemetry.retry.max-retries` | `5` | Número máximo de tentativas |
| `scheduler.telemetry.retry.base-backoff-ms` | `500` | Backoff base em milissegundos |
| `scheduler.telemetry.progress.update-interval` | `1000` | Intervalo de atualização em ms |
| `scheduler.telemetry.sequence.validation` | `true` | Validação de sequência |

## 🎯 **Anotações**

### @MonitoredScheduled

Anotação para marcar métodos de scheduler que devem ser monitorados.

```kotlin
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class MonitoredScheduled(
    val jobId: String,
    val description: String = "",
    val enableProgressTracking: Boolean = true,
    val progressUpdateInterval: Long = 1000L,
    val enablePerformanceMetrics: Boolean = true,
    val customMetadata: Array<String> = [],
    val captureFullStackTrace: Boolean = true,
    val maxItemBufferSize: Int = 1000,
    val autoRegister: Boolean = true,
    val messagePriority: String = "NORMAL",
    val tags: Array<String> = []
)
```

#### Parâmetros

| Parâmetro | Tipo | Padrão | Descrição |
|-----------|------|--------|-----------|
| `jobId` | `String` | **Obrigatório** | ID único do job |
| `description` | `String` | `""` | Descrição para logs |
| `enableProgressTracking` | `Boolean` | `true` | Habilita rastreamento de progresso |
| `progressUpdateInterval` | `Long` | `1000L` | Intervalo de atualização em ms |
| `enablePerformanceMetrics` | `Boolean` | `true` | Habilita métricas de performance |
| `customMetadata` | `Array<String>` | `[]` | Metadados customizados |
| `captureFullStackTrace` | `Boolean` | `true` | Captura stack trace completo |
| `maxItemBufferSize` | `Int` | `1000` | Tamanho máximo do buffer |
| `autoRegister` | `Boolean` | `true` | Registro automático no startup |
| `messagePriority` | `String` | `"NORMAL"` | Prioridade das mensagens |
| `tags` | `Array<String>` | `[]` | Tags para categorização |

## 🏗️ **Componentes Core**

### SchedulerTelemetry

Interface principal para interação com a telemetria durante a execução.

```kotlin
interface SchedulerTelemetry {
    fun setPlannedTotal(total: Long)
    fun addItem(key: String?, metadata: Map<String, Any?> = emptyMap())
    fun addFailedItem(key: String?, metadata: Map<String, Any?>, throwable: Throwable?)
    fun addSkippedItem(key: String?, metadata: Map<String, Any?>, reason: String?)
    fun putMetadata(key: String, value: Any?)
    fun addException(throwable: Throwable)
    fun finalizeExecutionContext(): ExecutionContext?
}
```

#### Métodos

| Método | Descrição |
|--------|-----------|
| `setPlannedTotal(total)` | Define o total esperado de itens |
| `addItem(key, metadata)` | Adiciona item processado com sucesso |
| `addFailedItem(key, metadata, throwable)` | Adiciona item que falhou |
| `addSkippedItem(key, metadata, reason)` | Adiciona item pulado |
| `putMetadata(key, value)` | Adiciona metadados gerais |
| `addException(throwable)` | Adiciona exceção ao contexto |
| `finalizeExecutionContext()` | Finaliza o contexto e retorna relatório |

### ExecutionContext

Classe que representa o contexto de execução de um scheduler.

```kotlin
data class ExecutionContext(
    val runId: String,
    val jobId: String,
    val appName: String,
    val startTime: Instant = Instant.now(),
    val plannedTotal: Long = 0L,
    val processedItems: AtomicLong = AtomicLong(0L),
    val failedItems: AtomicLong = AtomicLong(0L),
    val skippedItems: AtomicLong = AtomicLong(0L),
    val itemMetadata: MutableList<ItemMetadata> = mutableListOf(),
    val exceptions: MutableList<ExceptionInfo> = mutableListOf(),
    val generalMetadata: MutableMap<String, Any?> = mutableMapOf()
)
```

#### Propriedades

| Propriedade | Tipo | Descrição |
|-------------|------|-----------|
| `runId` | `String` | ID único da execução |
| `jobId` | `String` | ID do job do scheduler |
| `appName` | `String` | Nome da aplicação |
| `startTime` | `Instant` | Timestamp de início |
| `plannedTotal` | `Long` | Total esperado de itens |
| `processedItems` | `AtomicLong` | Contador de itens processados |
| `failedItems` | `AtomicLong` | Contador de itens falhados |
| `skippedItems` | `AtomicLong` | Contador de itens pulados |
| `itemMetadata` | `List<ItemMetadata>` | Metadados dos itens |
| `exceptions` | `List<ExceptionInfo>` | Lista de exceções |
| `generalMetadata` | `Map<String, Any?>` | Metadados gerais |

#### Métodos

| Método | Retorno | Descrição |
|--------|---------|-----------|
| `getTotalProcessed()` | `Long` | Total de itens processados |
| `getProgressPercentage()` | `Double` | Porcentagem de progresso |
| `isComplete()` | `Boolean` | Se a execução está completa |
| `getStatus()` | `ExecutionStatus` | Status da execução |

### ExecutionStatus

Enum que representa o status de uma execução.

```kotlin
enum class ExecutionStatus {
    RUNNING,    // Execução em andamento
    COMPLETED,  // Execução concluída com sucesso
    FAILED      // Execução falhou
}
```

## 🔄 **Gerenciamento de Sequência**

### SequenceManager

Gerencia a numeração sequencial das mensagens para garantir ordem.

```kotlin
class SequenceManager(
    private val outOfOrderToleranceMs: Long = 1000L,
    private val bufferSize: Int = 1000
)
```

#### Métodos

| Método | Retorno | Descrição |
|--------|---------|-----------|
| `getNextSequenceNumber(runId)` | `Long` | Próximo número de sequência |
| `validateSequence(runId, seq, timestamp)` | `SequenceValidationResult` | Valida sequência |
| `calculateChecksum(payload)` | `String` | Calcula checksum SHA-256 |
| `validateChecksum(payload, checksum)` | `Boolean` | Valida checksum |
| `storeMessage(runId, message)` | `Unit` | Armazena mensagem no buffer |
| `getMessage(runId, sequence)` | `String?` | Recupera mensagem do buffer |
| `getStats()` | `GeneralStats` | Estatísticas gerais |

### SequenceValidationResult

Enum para resultado da validação de sequência.

```kotlin
enum class SequenceValidationResult {
    VALID,      // Sequência válida
    DUPLICATE,  // Mensagem duplicada
    GAP,        // Gap na sequência
    INVALID     // Sequência inválida
}
```

## 📡 **Comunicação HTTP**

### HttpClientFactory

Factory para criação de clientes HTTP com retry e configurações.

```kotlin
class HttpClientFactory(
    private val connectionTimeoutMs: Long = 5000L,
    private val readTimeoutMs: Long = 10000L,
    private val maxRetries: Int = 3,
    private val retryDelayMs: Long = 1000L
)
```

#### Métodos

| Método | Retorno | Descrição |
|--------|---------|-----------|
| `postMessage(url, message, headers)` | `CompletableFuture<HttpResponse<String>>` | POST assíncrono |
| `postMessageSync(url, message, headers)` | `HttpResponse<String>` | POST síncrono |
| `getMessage(url, headers)` | `CompletableFuture<HttpResponse<String>>` | GET assíncrono |
| `getMessageSync(url, headers)` | `HttpResponse<String>` | GET síncrono |
| `isEndpointAccessible(url)` | `Boolean` | Verifica acessibilidade |
| `getConnectivityStats()` | `ConnectivityStats` | Estatísticas de conectividade |

### OutboundMessage

Hierarquia de mensagens enviadas ao Gerenciador Central.

```kotlin
sealed class OutboundMessage(
    val runId: String,
    val jobId: String,
    val appName: String,
    val timestamp: Instant,
    val sequenceNumber: Long,
    val checksum: String
)
```

#### Tipos de Mensagem

| Tipo | Descrição | Payload |
|------|-----------|---------|
| `RegistrationMessage` | Registro de aplicação | Informações da app e jobs |
| `ExecutionReportMessage` | Relatório final | Estatísticas completas |
| `ProgressMessage` | Atualização de progresso | Progresso atual |
| `StatusMessage` | Status da execução | Estado atual |
| `SyncMessage` | Sincronização | Estado de sincronização |

## 🔍 **Scanner de Schedulers**

### ScheduledScanner

Descobre automaticamente jobs agendados na aplicação.

```kotlin
class ScheduledScanner(
    private val applicationContext: ApplicationContext
)
```

#### Métodos

| Método | Retorno | Descrição |
|--------|---------|-----------|
| `discoverScheduledJobs()` | `List<ScheduledJobInfo>` | Descobre jobs agendados |
| `getJobsByAnnotation()` | `List<ScheduledJobInfo>` | Jobs com @MonitoredScheduled |
| `getJobsByScheduled()` | `List<ScheduledJobInfo>` | Jobs com @Scheduled |

### ScheduledJobInfo

Informações sobre um job agendado descoberto.

```kotlin
data class ScheduledJobInfo(
    val jobId: String,
    val methodName: String,
    val className: String,
    val cronExpression: String?,
    val fixedRate: Long?,
    val fixedDelay: Long?,
    val timeUnit: String?,
    val description: String
)
```

## 📊 **Rastreamento de Progresso**

### ProgressTracker

Rastreia o progresso de uma execução em tempo real.

```kotlin
class ProgressTracker(
    private val updateIntervalMs: Long = 1000L,
    private val enablePerformanceMetrics: Boolean = true
)
```

#### Métodos

| Método | Retorno | Descrição |
|--------|---------|-----------|
| `updateProgress(current, total)` | `ProgressInfo` | Atualiza progresso |
| `getProgressInfo()` | `ProgressInfo?` | Informações de progresso |
| `getPerformanceStats()` | `PerformanceStats?` | Estatísticas de performance |
| `isStagnant()` | `Boolean` | Se o progresso está estagnado |

### ProgressInfo

Informações sobre o progresso atual.

```kotlin
data class ProgressInfo(
    val currentItem: Long,
    val totalItems: Long,
    val progressPercentage: Double,
    val processedItems: Long,
    val failedItems: Long,
    val skippedItems: Long,
    val estimatedTimeRemaining: Long?,
    val itemsPerSecond: Double?
)
```

## 🚀 **Auto-configuração**

### TelemetryAutoConfiguration

Configuração automática Spring Boot para a biblioteca.

```kotlin
@AutoConfiguration
@ConditionalOnClass(SchedulerTelemetry::class)
@ConditionalOnProperty(prefix = "scheduler.telemetry", name = ["enabled"], havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(TelemetryProperties::class)
@EnableAspectJAutoProxy
class TelemetryAutoConfiguration
```

#### Beans Configurados

| Bean | Tipo | Descrição |
|------|------|-----------|
| `sequenceManager` | `SequenceManager` | Gerenciador de sequência |
| `httpClientFactory` | `HttpClientFactory` | Factory de clientes HTTP |
| `schedulerTelemetry` | `SchedulerTelemetryImpl` | Implementação da telemetria |
| `scheduledScanner` | `ScheduledScanner` | Scanner de jobs agendados |
| `monitoredScheduledAspect` | `MonitoredScheduledAspect` | Aspecto AOP |
| `registrar` | `Registrar` | Registro automático |

## 📝 **Exemplos de Uso**

### Exemplo Básico

```kotlin
@Component
class DataProcessingScheduler {
    
    @MonitoredScheduled(
        jobId = "data-processing",
        description = "Processamento de dados em lote"
    )
    @Scheduled(fixedRate = 300000)
    fun processDataBatch() {
        // Telemetria é capturada automaticamente
        processItems()
    }
}
```

### Exemplo com API Programática

```kotlin
@Component
class AdvancedDataScheduler {
    
    @Autowired
    private lateinit var telemetry: SchedulerTelemetry
    
    @MonitoredScheduled(
        jobId = "advanced-data-processing",
        enableProgressTracking = true
    )
    @Scheduled(cron = "0 0 2 * * ?")
    fun processAdvancedData() {
        val items = loadItems()
        telemetry.setPlannedTotal(items.size.toLong())
        
        items.forEachIndexed { index, item ->
            try {
                processItem(item)
                telemetry.addItem(
                    key = item.id,
                    metadata = mapOf("index" to index, "type" to item.type)
                )
            } catch (e: Exception) {
                telemetry.addFailedItem(
                    key = item.id,
                    metadata = mapOf("index" to index),
                    throwable = e
                )
                throw e
            }
        }
    }
}
```

### Exemplo de Configuração

```properties
# application.properties
scheduler.telemetry.enabled=true
scheduler.telemetry.manager.url=https://telemetry-manager.example.com
scheduler.telemetry.manager.auth.token=${TELEMETRY_TOKEN}
scheduler.telemetry.progress.update-interval=500
scheduler.telemetry.sequence.validation=true
scheduler.telemetry.retry.max-retries=3
scheduler.telemetry.retry.base-backoff-ms=1000
```

## 🔧 **Troubleshooting**

### Problemas Comuns

#### 1. Telemetria não está funcionando
- Verificar se `scheduler.telemetry.enabled=true`
- Verificar se há métodos com `@MonitoredScheduled`
- Verificar logs de inicialização

#### 2. Erro de conexão com Gerenciador Central
- Verificar URL em `scheduler.telemetry.manager.url`
- Verificar token de autenticação
- Verificar conectividade de rede

#### 3. Mensagens fora de ordem
- Verificar se `scheduler.telemetry.sequence.validation=true`
- Verificar configuração de tolerância
- Verificar logs de validação

#### 4. Performance degradada
- Ajustar `scheduler.telemetry.progress.update-interval`
- Verificar tamanho do buffer
- Monitorar uso de memória

### Logs de Debug

```properties
# application.properties
logging.level.com.schedkiwi.schedulertelemetry=DEBUG
logging.level.org.springframework.aop=DEBUG
```

### Métricas de Monitoramento

A biblioteca expõe as seguintes métricas internas:

- **Contadores**: itens processados, falhados, pulados
- **Timers**: tempo de execução, tempo de resposta HTTP
- **Gauges**: tamanho das filas, uso de memória
- **Histograms**: distribuição de latência, tamanho de mensagens

---

**Para mais informações, consulte o [README principal](../README.md) e os [exemplos de uso](examples.md).**
