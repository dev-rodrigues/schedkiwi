# Scheduler Telemetry Library

[![Maven Central](https://img.shields.io/maven-central/v/com.schedkiwi/scheduler-telemetry.svg)](https://search.maven.org/artifact/com.schedkiwi/scheduler-telemetry)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-blue.svg)](https://kotlinlang.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-green.svg)](https://spring.io/projects/spring-boot)

Uma biblioteca Maven em Kotlin para instrumentação automática de schedulers Spring Boot via AOP, com telemetria em tempo real, rastreamento de progresso e comunicação assíncrona com um Gerenciador Central.

## 🚀 **Características Principais**

- **🔍 AOP-First**: Interceptação automática de métodos `@Scheduled` sem interferir no comportamento
- **📊 Telemetria em Tempo Real**: Rastreamento contínuo de progresso e performance
- **🔒 Garantia de Ordem**: Sistema de sequência e checksums para mensagens
- **⚡ Assíncrono**: Comunicação não-bloqueante com filas e retry inteligente
- **🔌 Auto-configuração**: Integração automática via Spring Boot auto-configuration
- **📱 Sem Conflitos**: Não depende de `spring-boot-starter-web`

## 📋 **Requisitos**

- **Java**: 17+
- **Kotlin**: 1.9.22+
- **Spring Boot**: 3.2.0+
- **Maven**: 3.6+

## 📦 **Instalação**

### Maven
```xml
<dependency>
    <groupId>com.schedkiwi</groupId>
    <artifactId>scheduler-telemetry</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle
```gradle
implementation 'com.schedkiwi:scheduler-telemetry:1.0.0'
```

## 🎯 **Uso Rápido**

### 1. Anotar seu Scheduler
```kotlin
@Component
class DataProcessingScheduler {
    
    @MonitoredScheduled(
        jobId = "data-processing",
        description = "Processamento de dados em lote",
        enableProgressTracking = true,
        progressUpdateInterval = 1000
    )
    @Scheduled(fixedRate = 300000) // 5 minutos
    fun processDataBatch() {
        // Seu código de processamento aqui
        // A telemetria é capturada automaticamente!
    }
}
```

### 2. Configurar Properties
```properties
# application.properties
scheduler.telemetry.enabled=true
scheduler.telemetry.manager-url=http://localhost:8080
scheduler.telemetry.auth-token=your-token
scheduler.telemetry.progress.update-interval=1000
scheduler.telemetry.sequence.validation=true
```

### 3. API Programática (Opcional)
```kotlin
@Autowired
private lateinit var telemetry: SchedulerTelemetry

fun processItem(item: DataItem) {
    // Definir total esperado
    telemetry.setPlannedTotal(1000L)
    
    // Adicionar item processado
    telemetry.addItem(
        key = item.id,
        metadata = mapOf("type" to item.type, "size" to item.size)
    )
    
    // Adicionar exceção se houver erro
    try {
        processItem(item)
    } catch (e: Exception) {
        telemetry.addFailedItem(item.id, mapOf("error" to e.message), e)
        throw e // Re-propagar exceção
    }
}
```

## ⚙️ **Configuração**

### Properties Principais
```properties
# Habilitar/desabilitar telemetria
scheduler.telemetry.enabled=true

# URL do Gerenciador Central
scheduler.telemetry.manager-url=http://localhost:8080

# Autenticação
scheduler.telemetry.auth.token=your-token

# Configurações de retry
scheduler.telemetry.retry.max-retries=5
scheduler.telemetry.retry.base-backoff-ms=500

# Configurações de progresso
scheduler.telemetry.progress.update-interval=1000
scheduler.telemetry.progress.enable-performance-metrics=true

# Configurações de sequência
scheduler.telemetry.sequence.validation=true
scheduler.telemetry.sequence.out-of-order-tolerance-ms=1000
```

### Anotação @MonitoredScheduled
```kotlin
@MonitoredScheduled(
    jobId = "unique-job-id",                    // ID único do job
    description = "Descrição do job",           // Descrição para logs
    enableProgressTracking = true,              // Rastrear progresso
    progressUpdateInterval = 1000,              // Intervalo de atualização (ms)
    enablePerformanceMetrics = true,            // Métricas de performance
    customMetadata = ["env" to "prod"],         // Metadados customizados
    captureFullStackTrace = true,               // Stack trace completo
    maxItemBufferSize = 1000,                   // Tamanho do buffer
    autoRegister = true,                        // Registro automático
    messagePriority = "HIGH",                   // Prioridade das mensagens
    tags = ["batch", "critical"]                // Tags para categorização
)
```

## 🏗️ **Arquitetura**

### Componentes Principais
```
┌─────────────────────────────────────────────────────────────┐
│                    Scheduler Application                    │
├─────────────────────────────────────────────────────────────┤
│  @MonitoredScheduled + @Scheduled                          │
│  ↓                                                         │
│  MonitoredScheduledAspect (AOP)                           │
│  ↓                                                         │
│  ExecutionContext + SchedulerTelemetry                     │
│  ↓                                                         │
│  SequenceManager + ProgressTracker                         │
│  ↓                                                         │
│  Dispatchers (Progress, Report, Sequence)                 │
│  ↓                                                         │
│  HttpClientFactory → Gerenciador Central                   │
└─────────────────────────────────────────────────────────────┘
```

### Fluxo de Execução
1. **Interceptação**: Aspecto AOP captura execução do scheduler
2. **Contexto**: Criação de `ExecutionContext` com metadados
3. **Telemetria**: Coleta de dados durante execução
4. **Progresso**: Atualizações periódicas para o Gerenciador Central
5. **Finalização**: Relatório final com estatísticas completas
6. **Comunicação**: Envio assíncrono via filas e retry

## 📡 **Endpoints do Gerenciador Central**

A biblioteca envia dados para os seguintes endpoints que devem ser implementados pelo Gerenciador Central:

### 1. Registro de Aplicação
```http
POST /api/projects/register
Content-Type: application/json

{
  "appName": "my-app",
  "host": "localhost",
  "port": 8080,
  "scheduledJobs": [...]
}
```

### 2. Relatório de Execução
```http
POST /api/executions/report
Content-Type: application/json

{
  "jobId": "data-processing",
  "runId": "uuid",
  "status": "SUCCESS",
  "processedItems": 1000,
  "itemMetadata": [...],
  "exceptions": [...]
}
```

### 3. Atualização de Progresso
```http
POST /api/executions/progress
Content-Type: application/json

{
  "jobId": "data-processing",
  "runId": "uuid",
  "currentItem": 500,
  "totalItems": 1000,
  "progressPercentage": 50.0
}
```

### 4. Sincronização de Sequência
```http
POST /api/executions/sequence
Content-Type: application/json

{
  "runId": "uuid",
  "sequenceNumber": 1,
  "messageType": "PROGRESS",
  "checksum": "sha256-hash"
}
```

## 🧪 **Testes**

### Executar Testes
```bash
# Todos os testes
mvn test

# Testes específicos
mvn test -Dtest=*Test

# Com relatório detalhado
mvn test -Dtest=*Test -Dsurefire.useFile=false
```

### Cobertura de Testes
- **Total**: 26 testes
- **Integração**: 9 testes
- **SequenceManager**: 8 testes
- **HttpClientFactory**: 9 testes
- **Status**: ✅ Todos passando

## 🔧 **Desenvolvimento**

### Build
```bash
# Build completo
mvn clean package

# Build com testes
mvn clean verify

# Build de release
mvn clean verify -P release
```

### Estrutura do Projeto
```
scheduler-telemetry/
├── src/main/kotlin/
│   ├── core/           # Componentes principais
│   ├── aop/            # Aspectos AOP
│   ├── net/            # Comunicação HTTP
│   ├── scan/           # Scanner de schedulers
│   └── config/         # Auto-configuração
├── src/main/resources/
│   └── META-INF/       # Configuração Spring Boot
└── src/test/           # Testes unitários e integração
```

## 📚 **Documentação**

- [Guia de Publicação Maven Central](MAVEN_CENTRAL_PUBLICATION.md)
- [API Reference](docs/api-reference.md)
- [Exemplos de Uso](docs/examples.md)
- [Troubleshooting](docs/troubleshooting.md)

## 🤝 **Contribuição**

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## 📄 **Licença**

Este projeto está licenciado sob a Licença MIT - veja o arquivo [LICENSE](LICENSE) para detalhes.

## 🆘 **Suporte**

- **Issues**: [GitHub Issues](https://github.com/dev-rodrigues/schedkiwi/issues)
- **Discussions**: [GitHub Discussions](https://github.com/dev-rodrigues/schedkiwi/discussions)
- **Email**: carlos.henrique.rodrigues@gmail.com

## 🙏 **Agradecimentos**

- [Spring Boot](https://spring.io/projects/spring-boot) - Framework base
- [Kotlin](https://kotlinlang.org/) - Linguagem de programação
- [AspectJ](https://www.eclipse.org/aspectj/) - AOP framework
- [Jackson](https://github.com/FasterXML/jackson) - Serialização JSON

---

**Desenvolvido com ❤️ por [Carlos Henrique Rodrigues](https://github.com/dev-rodrigues)**
