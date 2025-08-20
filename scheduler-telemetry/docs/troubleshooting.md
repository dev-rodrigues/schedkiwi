# Troubleshooting - Scheduler Telemetry Library

## 🚨 **Problemas Comuns e Soluções**

Este guia ajuda a resolver os problemas mais frequentes encontrados ao usar a biblioteca `scheduler-telemetry`.

## 🔍 **Diagnóstico Básico**

### 1. Verificar se a Telemetria está Habilitada

```bash
# Verificar logs de inicialização
grep -i "telemetry" logs/application.log

# Verificar se os beans foram criados
grep -i "TelemetryAutoConfiguration" logs/application.log
```

**Sintomas:**
- Não há logs de telemetria
- Métodos `@MonitoredScheduled` não são interceptados
- Beans de telemetria não estão disponíveis

**Soluções:**
```properties
# application.properties
scheduler.telemetry.enabled=true
logging.level.com.schedkiwi.schedulertelemetry=DEBUG
```

### 2. Verificar Configuração do Gerenciador Central

```bash
# Testar conectividade
curl -v http://localhost:8080/actuator/health

# Verificar se a URL está correta
grep "manager.url" application.properties
```

**Sintomas:**
- Erros de conexão nos logs
- Falhas ao enviar telemetria
- Timeouts nas requisições HTTP

**Soluções:**
```properties
# application.properties
scheduler.telemetry.manager.url=http://localhost:8080
scheduler.telemetry.retry.max-retries=5
scheduler.telemetry.retry.base-backoff-ms=1000
```

## ⚠️ **Problemas Específicos**

### 3. Telemetria não está sendo Coletada

**Sintomas:**
- Métodos `@MonitoredScheduled` executam sem telemetria
- Não há logs de interceptação AOP
- `ExecutionContext` não é criado

**Causas Possíveis:**
1. **AOP não está habilitado**
2. **Método não tem `@Scheduled`**
3. **Dependências faltando**

**Soluções:**

#### Verificar AOP
```kotlin
@SpringBootApplication
@EnableAspectJAutoProxy  // Adicionar se necessário
class Application
```

#### Verificar Anotações
```kotlin
// ❌ ERRADO - falta @Scheduled
@MonitoredScheduled(jobId = "test")
fun processData() { }

// ✅ CORRETO - ambas as anotações
@MonitoredScheduled(jobId = "test")
@Scheduled(fixedRate = 60000)
fun processData() { }
```

#### Verificar Dependências
```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```

### 4. Erros de Compilação AOP

**Sintomas:**
- Erro: "Cannot resolve symbol 'MonitoredScheduled'"
- Erro: "Aspect not found"
- Falha na compilação

**Soluções:**

#### Verificar Imports
```kotlin
import com.schedkiwi.schedulertelemetry.aop.MonitoredScheduled
import org.springframework.scheduling.annotation.Scheduled
```

#### Verificar Dependências Maven
```bash
# Limpar e recompilar
mvn clean compile

# Verificar dependências
mvn dependency:tree | grep scheduler-telemetry
```

### 5. Falhas na Comunicação HTTP

**Sintomas:**
- Timeouts nas requisições
- Erros de conexão recusada
- Falhas ao enviar telemetria

**Soluções:**

#### Ajustar Timeouts
```properties
# application.properties
scheduler.telemetry.retry.max-retries=10
scheduler.telemetry.retry.base-backoff-ms=2000
```

#### Verificar Firewall/Proxy
```bash
# Testar conectividade
telnet telemetry-manager.example.com 443

# Verificar variáveis de ambiente
echo $HTTP_PROXY
echo $HTTPS_PROXY
```

#### Configurar Retry Inteligente
```properties
# application.properties
scheduler.telemetry.retry.exponential-backoff=true
scheduler.telemetry.retry.max-backoff-ms=30000
```

### 6. Mensagens Fora de Ordem

**Sintomas:**
- Logs de validação de sequência falhando
- Mensagens sendo rejeitadas
- Inconsistências nos dados

**Soluções:**

#### Ajustar Tolerância de Ordem
```properties
# application.properties
scheduler.telemetry.sequence.out-of-order-tolerance-ms=5000
scheduler.telemetry.sequence.validation=true
```

#### Verificar Configuração de Fila
```properties
# application.properties
scheduler.telemetry.queue.capacity=10000
scheduler.telemetry.queue.processing-threads=2
```

### 7. Performance Degradada

**Sintomas:**
- Aplicação mais lenta com telemetria
- Alto uso de memória
- Logs de performance

**Soluções:**

#### Ajustar Intervalo de Atualização
```properties
# application.properties
scheduler.telemetry.progress.update-interval=5000  # Aumentar para 5s
scheduler.telemetry.progress.enable-performance-metrics=false
```

#### Otimizar Buffer
```properties
# application.properties
scheduler.telemetry.sequence.buffer-size=100
scheduler.telemetry.queue.capacity=1000
```

#### Desabilitar Funcionalidades em Dev
```properties
# application-dev.properties
scheduler.telemetry.sequence.validation=false
scheduler.telemetry.progress.enable-performance-metrics=false
```

### 8. Problemas de Memória

**Sintomas:**
- OutOfMemoryError
- Alto uso de heap
- Garbage collection frequente

**Soluções:**

#### Ajustar Tamanho do Buffer
```properties
# application.properties
scheduler.telemetry.sequence.buffer-size=100
scheduler.telemetry.progress.max-item-buffer-size=100
```

#### Configurar Cleanup
```properties
# application.properties
scheduler.telemetry.cleanup.enabled=true
scheduler.telemetry.cleanup.interval-ms=300000  # 5 minutos
```

### 9. Falhas no Registro Automático

**Sintomas:**
- Aplicação não se registra no startup
- Erros de registro nos logs
- Falta de jobs descobertos

**Soluções:**

#### Verificar Descoberta de Jobs
```kotlin
@Component
class JobDiscoveryDebug {
    
    @EventListener
    fun onApplicationReady(event: ApplicationReadyEvent) {
        val scanner = ScheduledScanner(event.applicationContext)
        val jobs = scanner.discoverScheduledJobs()
        println("Jobs descobertos: ${jobs.size}")
        jobs.forEach { println("Job: ${it.jobId}") }
    }
}
```

#### Configurar Registro Manual
```properties
# application.properties
scheduler.telemetry.registration.auto-register=false
scheduler.telemetry.registration.manual-register=true
```

## 🔧 **Ferramentas de Debug**

### 1. Logs de Debug

```properties
# application.properties
logging.level.com.schedkiwi.schedulertelemetry=DEBUG
logging.level.org.springframework.aop=DEBUG
logging.level.org.springframework.scheduling=DEBUG
```

### 2. Métricas de Monitoramento

```kotlin
@Component
class TelemetryDebugger {
    
    @Autowired
    private lateinit var telemetry: SchedulerTelemetry
    
    @EventListener
    fun onTelemetryEvent(event: TelemetryEvent) {
        println("Telemetry Event: $event")
    }
}
```

### 3. Health Check Customizado

```kotlin
@Component
class TelemetryHealthIndicator : HealthIndicator {
    
    @Autowired
    private lateinit var telemetry: SchedulerTelemetry
    
    override fun health(): Health {
        return try {
            // Verificar se a telemetria está funcionando
            Health.up()
                .withDetail("telemetry_enabled", true)
                .withDetail("contexts_active", ExecutionContextHolder.getActiveContextsCount())
                .build()
        } catch (e: Exception) {
            Health.down()
                .withDetail("error", e.message)
                .build()
        }
    }
}
```

## 📊 **Monitoramento e Alertas**

### 1. Métricas Prometheus

```kotlin
@Component
class TelemetryMetrics {
    
    private val itemsProcessed = Counter.builder("scheduler_telemetry_items_processed")
        .description("Total de itens processados")
        .register(Metrics.globalRegistry)
    
    private val processingDuration = Timer.builder("scheduler_telemetry_processing_duration")
        .description("Duração do processamento")
        .register(Metrics.globalRegistry)
    
    @EventListener
    fun onItemProcessed(event: ItemProcessedEvent) {
        itemsProcessed.increment()
        processingDuration.record(event.processingTime, TimeUnit.MILLISECONDS)
    }
}
```

### 2. Alertas de Sistema

```kotlin
@Component
class TelemetryAlerting {
    
    @EventListener
    fun onTelemetryFailure(event: TelemetryFailureEvent) {
        when {
            event.errorRate > 0.1 -> sendAlert("HIGH_ERROR_RATE", "Error rate: ${event.errorRate}")
            event.isStagnant -> sendAlert("PROGRESS_STAGNANT", "Progress is stagnant")
            event.performanceDegraded -> sendAlert("LOW_PERFORMANCE", "Performance degraded")
        }
    }
    
    private fun sendAlert(type: String, message: String) {
        // Implementar envio de alertas
    }
}
```

## 🚀 **Otimizações de Performance**

### 1. Configurações para Produção

```properties
# application-prod.properties
scheduler.telemetry.progress.update-interval=1000
scheduler.telemetry.sequence.validation=true
scheduler.telemetry.retry.max-retries=10
scheduler.telemetry.queue.capacity=50000
scheduler.telemetry.sequence.buffer-size=1000
```

### 2. Configurações para Desenvolvimento

```properties
# application-dev.properties
scheduler.telemetry.progress.update-interval=5000
scheduler.telemetry.sequence.validation=false
scheduler.telemetry.progress.enable-performance-metrics=false
scheduler.telemetry.queue.capacity=1000
```

### 3. Configurações para Testes

```properties
# application-test.properties
scheduler.telemetry.enabled=false
# Ou usar configuração mínima
scheduler.telemetry.progress.update-interval=10000
scheduler.telemetry.sequence.validation=false
```

## 📚 **Recursos Adicionais**

### 1. Documentação Oficial
- [API Reference](api-reference.md)
- [Exemplos de Uso](examples.md)
- [README Principal](../README.md)

### 2. Comunidade
- [GitHub Issues](https://github.com/dev-rodrigues/schedkiwi/issues)
- [GitHub Discussions](https://github.com/dev-rodrigues/schedkiwi/discussions)

### 3. Ferramentas de Debug
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Micrometer](https://micrometer.io/) para métricas
- [Spring Boot DevTools](https://docs.spring.io/spring-boot/docs/current/reference/html/using.html#using.devtools)

---

**Se o problema persistir, abra uma issue no GitHub com:**
1. **Descrição detalhada** do problema
2. **Logs de erro** completos
3. **Configuração** atual
4. **Versões** das dependências
5. **Passos para reproduzir** o problema
