# Tipo da Tarefa
feature

# Descrição
Projetar e implementar uma API Central (Central Manager) para consumir e gerenciar os dados de telemetria enviados pela biblioteca `scheduler-telemetry`. A API deve receber dados em tempo real de múltiplas aplicações Spring Boot, armazenar telemetria, fornecer dashboards de monitoramento e gerenciar o ciclo de vida dos jobs agendados.

# Contexto (arquivos/trechos)
- scheduler-telemetry/docs/api-reference.md - Definição dos tipos de mensagens
- scheduler-telemetry/docs/examples.md - Exemplos de payloads
- scheduler-telemetry/README.md - Endpoints e payloads definidos
- scheduler-telemetry/src/main/kotlin/com/schedkiwi/schedulertelemetry/net/OutboundMessage.kt - Estrutura das mensagens

# Critérios de Aceite
- [ ] API REST implementada com todos os endpoints definidos pela biblioteca
- [ ] Sistema de autenticação e autorização para aplicações clientes
- [ ] Armazenamento persistente de telemetria (banco de dados)
- [ ] Dashboard em tempo real para monitoramento de jobs
- [ ] Sistema de alertas para falhas e performance degradada
- [ ] API para consulta de histórico e estatísticas
- [ ] Validação de checksums e sequência das mensagens
- [ ] Sistema de notificações para eventos críticos
- [ ] Documentação OpenAPI/Swagger completa
- [ ] Testes de integração com a biblioteca real
- [ ] Métricas de performance da própria API
- [ ] Sistema de backup e retenção de dados

# Plano (curto)
1) **Análise e Design** - Definir arquitetura, modelo de dados e APIs
2) **Implementação Core** - Desenvolver endpoints, serviços e persistência
3) **Dashboard e UI** - Criar interface de monitoramento em tempo real
4) **Sistema de Alertas** - Implementar notificações e alertas
5) **Testes e Validação** - Testar com biblioteca real e validar funcionalidades
6) **Documentação e Deploy** - Documentar APIs e preparar para produção

# Testes
- Unit: Testes unitários para todos os serviços e controllers
- Integration: Testes de integração com banco de dados e serviços externos
- E2E: Testes end-to-end simulando aplicações clientes reais
- Performance: Testes de carga e stress para validar escalabilidade

# Endpoints a Implementar

## 1. POST /api/projects/register
**Payload de Entrada:**
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
      "cronExpression": "string?",
      "fixedRate": "number?",
      "fixedDelay": "number?",
      "timeUnit": "string?",
      "description": "string"
    }
  ]
}
```

**Response:**
```json
{
  "projectId": "string",
  "status": "REGISTERED",
  "message": "string",
  "timestamp": "ISO-8601"
}
```

## 2. POST /api/executions/report
**Payload de Entrada:**
```json
{
  "jobId": "string",
  "runId": "string",
  "status": "SUCCESS|FAILED|RUNNING",
  "processedItems": "number",
  "failedItems": "number",
  "skippedItems": "number",
  "itemMetadata": [
    {
      "key": "string?",
      "metadata": "object",
      "outcome": "OK|ERROR|SKIPPED",
      "timestamp": "ISO-8601"
    }
  ],
  "exceptions": [
    {
      "message": "string",
      "stackTrace": "string?",
      "timestamp": "ISO-8601"
    }
  ],
  "startTime": "ISO-8601",
  "endTime": "ISO-8601",
  "generalMetadata": "object"
}
```

**Response:**
```json
{
  "executionId": "string",
  "status": "RECEIVED",
  "message": "string",
  "timestamp": "ISO-8601"
}
```

## 3. POST /api/executions/progress
**Payload de Entrada:**
```json
{
  "jobId": "string",
  "runId": "string",
  "currentItem": "number",
  "totalItems": "number",
  "progressPercentage": "number",
  "processedItems": "number",
  "failedItems": "number",
  "skippedItems": "number",
  "estimatedTimeRemaining": "number?",
  "itemsPerSecond": "number?",
  "timestamp": "ISO-8601"
}
```

**Response:**
```json
{
  "progressId": "string",
  "status": "UPDATED",
  "message": "string",
  "timestamp": "ISO-8601"
}
```

## 4. POST /api/executions/sequence
**Payload de Entrada:**
```json
{
  "runId": "string",
  "sequenceNumber": "number",
  "messageType": "PROGRESS|REPORT|STATUS|SYNC",
  "checksum": "string",
  "timestamp": "ISO-8601"
}
```

**Response:**
```json
{
  "sequenceId": "string",
  "validation": "VALID|INVALID|DUPLICATE|GAP",
  "message": "string",
  "timestamp": "ISO-8601"
}
```

## 5. GET /api/executions/{runId}/status
**Response:**
```json
{
  "runId": "string",
  "jobId": "string",
  "status": "RUNNING|COMPLETED|FAILED",
  "progress": {
    "currentItem": "number",
    "totalItems": "number",
    "percentage": "number",
    "estimatedTimeRemaining": "number?"
  },
  "statistics": {
    "processedItems": "number",
    "failedItems": "number",
    "skippedItems": "number",
    "startTime": "ISO-8601",
    "lastUpdate": "ISO-8601"
  }
}
```

## 6. GET /api/executions/{runId}/sync
**Response:**
```json
{
  "runId": "string",
  "lastSequenceNumber": "number",
  "missingSequences": ["number"],
  "bufferState": [
    {
      "sequenceNumber": "number",
      "messageType": "string",
      "timestamp": "ISO-8601",
      "checksum": "string"
    }
  ]
}
```

# Funcionalidades Adicionais

## Dashboard em Tempo Real
- Visualização de jobs ativos
- Gráficos de progresso em tempo real
- Métricas de performance
- Histórico de execuções
- Alertas e notificações

## Sistema de Alertas
- Falhas de execução
- Performance degradada
- Jobs estagnados
- Taxa de erro alta
- Tempo de execução anormal

## Relatórios e Analytics
- Relatórios de performance por período
- Análise de tendências
- Comparação entre aplicações
- Métricas de SLA
- Análise de falhas e exceções

## Gerenciamento de Aplicações
- Registro e configuração de aplicações
- Gerenciamento de jobs por aplicação
- Configurações de alertas por aplicação
- Histórico de mudanças
- Controle de acesso e permissões
