# Tipo da Tarefa
feature

# Descrição
Criar uma biblioteca Maven em Kotlin que instrumenta métodos @Scheduled via AOP para coletar telemetria e enviar relatórios ao Gerenciador Central, sem interferir no comportamento dos schedulers existentes.

**Localização**: Pasta `scheduler-telemetry/` na raiz do projeto (código fonte da biblioteca)

# Contexto (arquivos/trechos)
- **Biblioteca**: `scheduler-telemetry/` (pasta na raiz do projeto)
  - Novo módulo Maven: `scheduler-telemetry/`
  - Estrutura de pacotes: `com.schedkiwi.schedulertelemetry`
  - Integração com Spring Boot via auto-configuração
- **Gerenciador Central**: `central-telemetry-api/` (pasta na raiz do projeto)
  - Comunicação REST com Gerenciador Central existente

# Critérios de Aceite
- [ ] Biblioteca Maven compilável e testável
- [ ] Auto-configuração Spring Boot funcional
- [ ] Anotação @MonitoredScheduled implementada
- [ ] Aspecto AOP captura execução sem interferir no comportamento
- [ ] API ThreadLocal-safe para telemetria em runtime
- [ ] Registro automático da aplicação no startup
- [ ] Dispatcher assíncrono com fila e retry
- [ ] Configuração via properties
- [ ] Testes unitários com 100% de cobertura
- [ ] Publicação em repositório público Maven
- [ ] Sem conflitos de dependência com spring-boot-starter-web
- [ ] Especificação completa dos endpoints REST que o Gerenciador Central deve implementar
- [ ] Documentação dos payloads e contratos de API para integração
- [ ] Suporte a progresso em tempo real com atualizações por item processado
- [ ] Endpoints para consulta de status e progresso de execuções ativas
- [ ] Garantia de ordem sequencial das mensagens com numeração e validação
- [ ] Sistema de retry inteligente que preserva a ordem cronológica dos eventos

# Plano (curto)
1) Criar estrutura do projeto Maven com Kotlin
2) Implementar classes core (ExecutionContext, SchedulerTelemetry)
3) Implementar anotação e aspecto AOP
4) Definir contratos de API e endpoints que o Gerenciador Central deve implementar
5) Implementar comunicação REST com Gerenciador Central
6) Implementar auto-configuração Spring Boot
7) Implementar testes unitários
8) Configurar publicação Maven
9) Validar integração e funcionamento
10) Documentar especificações de API para implementação no Gerenciador Central

# Testes
- Unit: Todas as classes com MockK, cobertura 100%
- Integration: Testes de auto-configuração Spring Boot
- E2E: Validação da integração completa com scheduler

# Arquitetura
```
scheduler-telemetry/
├─ src/main/kotlin/com/schedkiwi/schedulertelemetry/
│  ├─ config/
│  │  ├─ TelemetryAutoConfiguration.kt
│  │  └─ TelemetryProperties.kt
│  ├─ aop/
│  │  ├─ MonitoredScheduled.kt
│  │  └─ MonitoredScheduledAspect.kt
│  ├─ core/
│  │  ├─ ExecutionContext.kt
│  │  ├─ ExecutionContextHolder.kt
│  │  ├─ ItemOutcome.kt
│  │  ├─ SchedulerTelemetry.kt
│  │  ├─ SchedulerTelemetryImpl.kt
│  │  ├─ ProgressTracker.kt
│  │  └─ SequenceManager.kt
│  ├─ net/
│  │  ├─ HttpClientFactory.kt
│  │  ├─ OutboundMessage.kt
│  │  ├─ ReportDispatcher.kt
│  │  ├─ ProgressDispatcher.kt
│  │  ├─ SequenceDispatcher.kt
│  │  └─ Registrar.kt
│  └─ scan/
│     └─ ScheduledScanner.kt
├─ src/main/resources/
│  └─ META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
└─ pom.xml
```

# Configuração Properties
```properties
scheduler.telemetry.enabled=true
scheduler.telemetry.manager-url=http://IP:PORT
scheduler.telemetry.register-path=/api/projects/register
scheduler.telemetry.report-path=/api/executions/report
scheduler.telemetry.progress-path=/api/executions/progress
scheduler.telemetry.status-path=/api/executions/{runId}/status
scheduler.telemetry.sequence-path=/api/executions/sequence
scheduler.telemetry.auth-token=<opcional>
scheduler.telemetry.max-retry=5
scheduler.telemetry.base-backoff-ms=500
scheduler.telemetry.queue-capacity=10000
scheduler.telemetry.progress-update-interval=1000
scheduler.telemetry.sequence-validation=true
scheduler.telemetry.out-of-order-tolerance=1000
```

# Dependências Principais
- Kotlin 1.9+
- Spring Boot 3.x
- Spring AOP
- Jackson para serialização
- MockK para testes
- JUnit 5 para testes

# Estratégias para Garantir Ordem e Consistência

## 1. Numeração Sequencial
- Cada mensagem recebe um `sequenceNumber` incremental por `runId`
- O Gerenciador Central valida e rejeita mensagens fora de ordem
- Tolerância configurável para pequenas variações de timing

## 2. Fila Ordenada por Sequência
- `PriorityBlockingQueue` ordenada por `sequenceNumber`
- Mensagens com número menor têm prioridade de envio
- Worker processa mensagens em ordem sequencial

## 3. Retry Inteligente
- Mensagens falhadas retornam para o topo da fila (preservando ordem)
- Backoff exponencial por mensagem individual
- Dead letter queue para mensagens com falhas persistentes

## 4. Validação de Integridade
- Checksum SHA-256 para cada payload
- Validação de timestamp e sequência no Gerenciador Central
- Rejeição de mensagens duplicadas ou corrompidas

## 5. Sincronização de Estado
- Endpoint `/api/executions/{runId}/sync` para sincronização
- Gerenciador Central pode solicitar reenvio de mensagens perdidas
- Biblioteca mantém buffer circular das últimas N mensagens por execução

# Especificações de API para Gerenciador Central
A biblioteca deve definir e documentar os seguintes endpoints que o Gerenciador Central deve implementar para suportar monitoramento em tempo real com progresso e **garantia de ordem sequencial**:

## POST /api/projects/register
**Payload de Registro:**
```json
{
  "appName": "string",
  "host": "string", 
  "port": "number",
  "scheduledJobs": [
    {
      "jobId": "string",
      "methodName": "string",
      "className": "string",
      "cronExpression": "string"
    }
  ]
}
```

## POST /api/executions/report
**Payload de Relatório Final:**
```json
{
  "jobId": "string",
  "runId": "string",
  "appName": "string",
  "startTime": "ISO-8601",
  "endTime": "ISO-8601",
  "status": "SUCCESS|FAILED",
  "plannedTotal": "number",
  "processedItems": "number",
  "itemMetadata": [
    {
      "key": "string",
      "metadata": "object",
      "outcome": "OK|ERROR|SKIPPED"
    }
  ],
  "exceptions": [
    {
      "message": "string",
      "type": "string",
      "stackTrace": "string"
    }
  ]
}
```

## POST /api/executions/progress
**Payload de Progresso em Tempo Real:**
```json
{
  "jobId": "string",
  "runId": "string",
  "appName": "string",
  "timestamp": "ISO-8601",
  "currentItem": "number",
  "totalItems": "number",
  "progressPercentage": "number",
  "currentItemMetadata": {
    "key": "string",
    "metadata": "object",
    "status": "PROCESSING|COMPLETED|ERROR"
  },
  "estimatedTimeRemaining": "number" // em segundos
}
```

## GET /api/executions/{runId}/status
**Resposta de Status da Execução:**
```json
{
  "runId": "string",
  "jobId": "string",
  "appName": "string",
  "status": "RUNNING|COMPLETED|FAILED|PAUSED",
  "startTime": "ISO-8601",
  "currentProgress": {
    "currentItem": "number",
    "totalItems": "number",
    "progressPercentage": "number",
    "processedItems": "number",
    "failedItems": "number",
    "skippedItems": "number"
  },
  "lastUpdate": "ISO-8601",
  "estimatedCompletion": "ISO-8601"
}
```

## POST /api/executions/sequence
**Payload para Garantir Ordem Sequencial:**
```json
{
  "runId": "string",
  "jobId": "string",
  "appName": "string",
  "sequenceNumber": "number",
  "timestamp": "ISO-8601",
  "messageType": "PROGRESS|REPORT|EXCEPTION",
  "payload": "object",
  "checksum": "string" // SHA-256 do payload para validação
}
```

## GET /api/executions/{runId}/sync
**Resposta de Sincronização:**
```json
{
  "runId": "string",
  "lastReceivedSequence": "number",
  "missingSequences": ["number"],
  "status": "SYNC_REQUIRED|SYNCED|ERROR",
  "message": "string"
}
```

# Entregáveis
- Módulo Maven: scheduler-telemetry (jar)
- Auto-configuração Spring Boot
- Código-fonte Kotlin com testes
- Biblioteca disponível em repositório público Maven
- Documentação de uso e integração
- Especificação dos endpoints e funcionamentos que o Gerenciador Central deve implementar
