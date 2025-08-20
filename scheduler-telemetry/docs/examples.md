# Exemplos de Uso - Scheduler Telemetry Library

## 🚀 **Exemplos Básicos**

### 1. Scheduler Simples

```kotlin
@Component
class SimpleDataScheduler {
    
    @MonitoredScheduled(
        jobId = "simple-data-processing",
        description = "Processamento simples de dados"
    )
    @Scheduled(fixedRate = 60000) // A cada minuto
    fun processSimpleData() {
        // Seu código aqui - telemetria automática!
        val data = loadData()
        processData(data)
        saveResults(data)
    }
}
```

### 2. Scheduler com Progresso

```kotlin
@Component
class BatchDataScheduler {
    
    @MonitoredScheduled(
        jobId = "batch-data-processing",
        description = "Processamento em lote com progresso",
        enableProgressTracking = true,
        progressUpdateInterval = 500
    )
    @Scheduled(cron = "0 0 2 * * ?") // 2h da manhã
    fun processBatchData() {
        val items = loadBatchItems()
        
        items.forEachIndexed { index, item ->
            try {
                processItem(item)
                // Progresso é atualizado automaticamente
            } catch (e: Exception) {
                // Exceções são capturadas automaticamente
                throw e
            }
        }
    }
}
```

## 🔧 **Exemplos Intermediários**

### 3. Scheduler com API Programática

```kotlin
@Component
class AdvancedDataScheduler {
    
    @Autowired
    private lateinit var telemetry: SchedulerTelemetry
    
    @MonitoredScheduled(
        jobId = "advanced-data-processing",
        description = "Processamento avançado com telemetria customizada",
        enableProgressTracking = true,
        enablePerformanceMetrics = true
    )
    @Scheduled(fixedDelay = 300000) // 5 minutos após conclusão
    fun processAdvancedData() {
        val items = loadAdvancedItems()
        telemetry.setPlannedTotal(items.size.toLong())
        
        items.forEachIndexed { index, item ->
            val startTime = System.currentTimeMillis()
            
            try {
                // Adicionar metadados customizados
                telemetry.putMetadata("batch_size", items.size)
                telemetry.putMetadata("processing_mode", "advanced")
                
                // Processar item
                val result = processAdvancedItem(item)
                
                // Adicionar item processado com metadados
                telemetry.addItem(
                    key = item.id,
                    metadata = mapOf(
                        "index" to index,
                        "type" to item.type,
                        "size" to item.size,
                        "processing_time_ms" to (System.currentTimeMillis() - startTime)
                    )
                )
                
            } catch (e: Exception) {
                // Adicionar item falhado
                telemetry.addFailedItem(
                    key = item.id,
                    metadata = mapOf(
                        "index" to index,
                        "error_type" to e.javaClass.simpleName,
                        "processing_time_ms" to (System.currentTimeMillis() - startTime)
                    ),
                    throwable = e
                )
                
                // Re-propagar exceção para não interferir no comportamento
                throw e
            }
        }
    }
}
```

### 4. Scheduler com Múltiplos Tipos de Item

```kotlin
@Component
class MultiTypeDataScheduler {
    
    @Autowired
    private lateinit var telemetry: SchedulerTelemetry
    
    @MonitoredScheduled(
        jobId = "multi-type-processing",
        description = "Processamento de múltiplos tipos de dados",
        tags = ["multi-type", "critical"]
    )
    @Scheduled(cron = "0 */30 * * * ?") // A cada 30 minutos
    fun processMultiTypeData() {
        val dataTypes = listOf("users", "orders", "products", "analytics")
        
        dataTypes.forEach { dataType ->
            try {
                processDataType(dataType)
                
                // Adicionar metadados por tipo
                telemetry.putMetadata("data_type", dataType)
                telemetry.putMetadata("processing_timestamp", Instant.now())
                
            } catch (e: Exception) {
                // Adicionar exceção específica do tipo
                telemetry.addException(e)
                telemetry.putMetadata("failed_data_type", dataType)
                
                // Continuar com outros tipos
                log.error("Failed to process data type: $dataType", e)
            }
        }
    }
}
```

## 🎯 **Exemplos Avançados**

### 5. Scheduler com Retry e Fallback

```kotlin
@Component
class ResilientDataScheduler {
    
    @Autowired
    private lateinit var telemetry: SchedulerTelemetry
    
    @MonitoredScheduled(
        jobId = "resilient-data-processing",
        description = "Processamento resiliente com retry e fallback",
        enableProgressTracking = true,
        customMetadata = ["resilience" to "high", "fallback" to "enabled"]
    )
    @Scheduled(fixedRate = 900000) // 15 minutos
    fun processResilientData() {
        val items = loadResilientItems()
        telemetry.setPlannedTotal(items.size.toLong())
        
        items.forEachIndexed { index, item ->
            var processed = false
            var attempts = 0
            val maxAttempts = 3
            
            while (!processed && attempts < maxAttempts) {
                attempts++
                
                try {
                    // Tentar processamento principal
                    processItemWithPrimaryMethod(item)
                    processed = true
                    
                    telemetry.addItem(
                        key = item.id,
                        metadata = mapOf(
                            "index" to index,
                            "attempts" to attempts,
                            "method" to "primary"
                        )
                    )
                    
                } catch (e: PrimaryMethodException) {
                    // Log da tentativa
                    telemetry.putMetadata("attempt_$attempts", "primary_failed")
                    
                    if (attempts >= maxAttempts) {
                        // Tentar método de fallback
                        try {
                            processItemWithFallbackMethod(item)
                            processed = true
                            
                            telemetry.addItem(
                                key = item.id,
                                metadata = mapOf(
                                    "index" to index,
                                    "attempts" to attempts,
                                    "method" to "fallback"
                                )
                            )
                            
                        } catch (fallbackException: Exception) {
                            // Ambos os métodos falharam
                            telemetry.addFailedItem(
                                key = item.id,
                                metadata = mapOf(
                                    "index" to index,
                                    "attempts" to attempts,
                                    "primary_error" to e.message,
                                    "fallback_error" to fallbackException.message
                                ),
                                throwable = fallbackException
                            )
                            
                            throw fallbackException
                        }
                    }
                }
            }
        }
    }
}
```

### 6. Scheduler com Métricas de Performance

```kotlin
@Component
class PerformanceAwareScheduler {
    
    @Autowired
    private lateinit var telemetry: SchedulerTelemetry
    
    @MonitoredScheduled(
        jobId = "performance-aware-processing",
        description = "Processamento com monitoramento de performance",
        enablePerformanceMetrics = true,
        progressUpdateInterval = 100
    )
    @Scheduled(cron = "0 0 */2 * * ?") // A cada 2 horas
    fun processWithPerformanceMonitoring() {
        val items = loadPerformanceItems()
        telemetry.setPlannedTotal(items.size.toLong())
        
        val overallStartTime = System.currentTimeMillis()
        var totalProcessingTime = 0L
        var totalItemsProcessed = 0L
        
        items.forEachIndexed { index, item ->
            val itemStartTime = System.currentTimeMillis()
            
            try {
                // Processar item
                val result = processPerformanceItem(item)
                
                val itemProcessingTime = System.currentTimeMillis() - itemStartTime
                totalProcessingTime += itemProcessingTime
                totalItemsProcessed++
                
                // Adicionar métricas de performance
                telemetry.addItem(
                    key = item.id,
                    metadata = mapOf(
                        "index" to index,
                        "processing_time_ms" to itemProcessingTime,
                        "cumulative_time_ms" to totalProcessingTime,
                        "average_time_ms" to (totalProcessingTime / totalItemsProcessed),
                        "items_per_second" to (totalItemsProcessed * 1000.0 / totalProcessingTime)
                    )
                )
                
                // Adicionar metadados gerais de performance
                telemetry.putMetadata("overall_progress_ms", System.currentTimeMillis() - overallStartTime)
                telemetry.putMetadata("current_performance_avg_ms", totalProcessingTime / totalItemsProcessed)
                
            } catch (e: Exception) {
                val itemProcessingTime = System.currentTimeMillis() - itemStartTime
                
                telemetry.addFailedItem(
                    key = item.id,
                    metadata = mapOf(
                        "index" to index,
                        "processing_time_ms" to itemProcessingTime,
                        "error_impact_on_performance" to true
                    ),
                    throwable = e
                )
                
                throw e
            }
        }
        
        // Adicionar métricas finais
        val totalTime = System.currentTimeMillis() - overallStartTime
        telemetry.putMetadata("total_execution_time_ms", totalTime)
        telemetry.putMetadata("final_performance_avg_ms", totalProcessingTime / totalItemsProcessed)
        telemetry.putMetadata("overall_efficiency_percent", (totalItemsProcessed * 100.0) / items.size)
    }
}
```

## 🔄 **Exemplos de Configuração**

### 7. Configuração Básica

```properties
# application.properties
scheduler.telemetry.enabled=true
scheduler.telemetry.manager.url=http://localhost:8080
scheduler.telemetry.progress.update-interval=1000
```

### 8. Configuração Avançada

```properties
# application.properties
scheduler.telemetry.enabled=true
scheduler.telemetry.manager.url=https://telemetry-manager.prod.example.com
scheduler.telemetry.manager.auth.token=${TELEMETRY_AUTH_TOKEN}

# Configurações de retry
scheduler.telemetry.retry.max-retries=5
scheduler.telemetry.retry.base-backoff-ms=1000

# Configurações de progresso
scheduler.telemetry.progress.update-interval=500
scheduler.telemetry.progress.enable-performance-metrics=true

# Configurações de sequência
scheduler.telemetry.sequence.validation=true
scheduler.telemetry.sequence.out-of-order-tolerance-ms=2000

# Configurações de fila
scheduler.telemetry.queue.capacity=10000
scheduler.telemetry.queue.processing-threads=4

# Configurações de registro
scheduler.telemetry.registration.auto-register=true
scheduler.telemetry.registration.retry-attempts=3
```

### 9. Configuração por Ambiente

```yaml
# application-dev.yml
scheduler:
  telemetry:
    enabled: true
    manager:
      url: http://localhost:8080
    progress:
      update-interval: 1000
    sequence:
      validation: false  # Desabilitar em dev para performance

---
# application-prod.yml
scheduler:
  telemetry:
    enabled: true
    manager:
      url: https://telemetry-manager.prod.example.com
      auth:
        token: ${TELEMETRY_AUTH_TOKEN}
    progress:
      update-interval: 500
    sequence:
      validation: true
      out-of-order-tolerance-ms: 1000
    retry:
      max-retries: 10
      base-backoff-ms: 2000
```

## 🧪 **Exemplos de Teste**

### 10. Teste Unitário

```kotlin
@ExtendWith(MockKExtension::class)
class DataSchedulerTest {
    
    @MockK
    private lateinit var telemetry: SchedulerTelemetry
    
    @InjectMocks
    private lateinit var scheduler: DataScheduler
    
    @Test
    fun `deve processar dados com telemetria`() {
        // Given
        val items = listOf("item1", "item2", "item3")
        every { telemetry.setPlannedTotal(3L) } returns Unit
        every { telemetry.addItem(any(), any()) } returns Unit
        
        // When
        scheduler.processData(items)
        
        // Then
        verify { 
            telemetry.setPlannedTotal(3L)
            telemetry.addItem("item1", any())
            telemetry.addItem("item2", any())
            telemetry.addItem("item3", any())
        }
    }
}
```

### 11. Teste de Integração

```kotlin
@SpringBootTest
@TestPropertySource(properties = [
    "scheduler.telemetry.enabled=true",
    "scheduler.telemetry.manager.url=http://localhost:8080"
])
class DataSchedulerIntegrationTest {
    
    @Autowired
    private lateinit var scheduler: DataScheduler
    
    @Test
    fun `deve executar scheduler com telemetria habilitada`() {
        // Given
        val items = createTestItems()
        
        // When
        scheduler.processData(items)
        
        // Then
        // Verificar se a telemetria foi coletada
        // Verificar se os logs foram gerados
        // Verificar se as métricas foram registradas
    }
}
```

## 📊 **Exemplos de Monitoramento**

### 12. Dashboard de Métricas

```kotlin
@Component
class TelemetryMetricsExporter {
    
    @Autowired
    private lateinit var telemetry: SchedulerTelemetry
    
    @EventListener
    fun onTelemetryEvent(event: TelemetryEvent) {
        // Exportar métricas para sistemas externos
        when (event) {
            is ItemProcessedEvent -> {
                recordMetric("items_processed_total", 1.0)
                recordMetric("processing_duration_ms", event.processingTime)
            }
            is ItemFailedEvent -> {
                recordMetric("items_failed_total", 1.0)
                recordMetric("error_rate", calculateErrorRate())
            }
            is ProgressUpdateEvent -> {
                recordMetric("progress_percentage", event.progressPercentage)
                recordMetric("items_per_second", event.itemsPerSecond)
            }
        }
    }
    
    private fun recordMetric(name: String, value: Double) {
        // Implementar exportação para Prometheus, InfluxDB, etc.
    }
}
```

### 13. Alertas de Performance

```kotlin
@Component
class PerformanceAlerting {
    
    @Autowired
    private lateinit var telemetry: SchedulerTelemetry
    
    @EventListener
    fun onProgressUpdate(event: ProgressUpdateEvent) {
        // Verificar se o progresso está estagnado
        if (event.isStagnant) {
            sendAlert("PROGRESS_STAGNANT", "Job ${event.jobId} progress is stagnant")
        }
        
        // Verificar se a performance está degradada
        if (event.itemsPerSecond < 10.0) {
            sendAlert("LOW_PERFORMANCE", "Job ${event.jobId} performance is below threshold")
        }
        
        // Verificar se há muitas falhas
        if (event.errorRate > 0.1) {
            sendAlert("HIGH_ERROR_RATE", "Job ${event.jobId} has high error rate: ${event.errorRate}")
        }
    }
    
    private fun sendAlert(type: String, message: String) {
        // Implementar envio de alertas (Slack, email, etc.)
    }
}
```

---

**Para mais informações sobre a API, consulte a [API Reference](api-reference.md).**
