# Tipo da Tarefa
feature

# Descrição
Projetar e implementar uma API Central para consumir dados de telemetria enviados pela biblioteca `scheduler-telemetry`. A aplicação deve funcionar como um Gerenciador Central que recebe, processa, armazena e disponibiliza dados de execução de schedulers Spring Boot para monitoramento e análise em tempo real.

**Localização**: Pasta `central-telemetry-api/` na raiz do projeto (código do gerenciador central)

# Contexto (arquivos/trechos)
- **Biblioteca**: `scheduler-telemetry/` (pasta na raiz do projeto)
  - scheduler-telemetry/docs/api-reference.md (endpoints e payloads)
  - scheduler-telemetry/docs/examples.md (casos de uso)
  - scheduler-telemetry/README.md (especificações dos endpoints)
  - scheduler-telemetry/src/main/kotlin/com/schedkiwi/schedulertelemetry/net/OutboundMessage.kt (estruturas de mensagem)
- **API Central**: `central-telemetry-api/` (pasta na raiz do projeto)
  - Código fonte da aplicação gerenciadora central

# Critérios de Aceite
- [ ] API Central recebe dados de múltiplas aplicações integradas
- [ ] Sistema de autenticação via tokens para identificação das aplicações
- [ ] Armazenamento em PostgreSQL com histórico completo de execuções
- [ ] Endpoints RESTful para frontend consumir dados
- [ ] **Navegação fluida entre aplicações, jobs e execuções**
- [ ] **Links HATEOAS para facilitar navegação entre entidades**
- [ ] Filtro de execuções válidas (mais de 1 item processado)
- [ ] Rastreamento de início/parada das aplicações
- [ ] Métricas agregadas por execução (total processado, falhas, sucessos)
- [ ] Documentação com diagramas, casos de uso e fluxos
- [ ] Sistema de monitoramento em tempo real
- [ ] API para gerenciamento de tokens de aplicação

## 📋 **PLANO DE IMPLEMENTAÇÃO**

### **Fase 1: Análise e Design (Concluída ✅)**
- ✅ Análise dos endpoints da biblioteca scheduler-telemetry
- ✅ Projeto da arquitetura da API Central
- ✅ Definição dos 6 endpoints do Grupo 1 (4 POST + 2 GET)
- ✅ Definição dos endpoints do Grupo 2 para frontend
- ✅ Modelagem de dados PostgreSQL (8 tabelas)
- ✅ Definição dos 11 componentes principais
- ✅ Casos de uso e fluxos de funcionamento

### **Fase 2: Modelagem de Dados e Arquitetura Hexagonal (Concluída ✅)**
- ✅ Criação do esquema PostgreSQL
- ✅ Implementação das entidades de domínio (Domain Layer)
- ✅ Definição dos Ports (interfaces) no Domain Layer
- ✅ Implementação das entidades JPA (Infrastructure Layer)
- ✅ Configuração de relacionamentos
- ✅ Scripts de migração
- ✅ Dados de seed para testes

### **Fase 3: Implementação Core com Mappers (Concluída ✅)**
- ✅ Implementação dos Use Cases (Application Layer)
- ✅ Criação dos DTOs de entrada/saída
- ✅ Implementação dos Mappers com MapStruct
- ✅ Endpoints do Grupo 1 (recebimento e consultas)
- ✅ Sistema de autenticação com tokens
- ✅ Validação e processamento de dados
- ✅ Persistência no PostgreSQL via Repositories

### **Fase 4: API Frontend e Navegação**
- [ ] Endpoints do Grupo 2 para consulta
- [ ] Implementação HATEOAS
- [ ] Sistema de navegação fluida
- [ ] Filtros e paginação
- [ ] Métricas agregadas
- [ ] Validação de separação de camadas

### **Fase 5: Documentação e Testes Arquiteturais**
- [ ] 10 diagramas de arquitetura e fluxos
- [ ] Documentação OpenAPI/Swagger
- [ ] Testes unitários para todas as camadas
- [ ] Testes de Mappers e conversões
- [ ] Testes de Ports e contratos
- [ ] Testes de integração
- [ ] Testes de performance e sincronização
- [ ] Testes de arquitetura (verificação de camadas)

### **Fase 6: Deployment e Validação**
- [ ] Containerização Docker
- [ ] Configuração de monitoramento
- [ ] Testes de carga
- [ ] Validação com biblioteca scheduler-telemetry
- [ ] Validação de separação de responsabilidades

# Testes
- Unit: Serviços de telemetria, autenticação e métricas
- Integration: Endpoints de recebimento e consulta de dados
- E2E: Fluxo completo de registro → execução → consulta

---

## 📋 **REQUISITOS DETALHADOS**

### 🎯 **Objetivos Principais**
- **Centralizar telemetria** de múltiplas aplicações Spring Boot
- **Monitorar execuções** de schedulers em tempo real
- **Fornecer insights** sobre performance e falhas
- **Gerenciar aplicações** integradas via tokens de autenticação

### 🔌 **Endpoints da Aplicação**

A API Central deve prover **dois grupos distintos de endpoints** com responsabilidades diferentes:

#### **Grupo 1: Endpoints para Receber Dados e Responder Consultas da Biblioteca (Inbound)**
Estes endpoints recebem dados enviados pela biblioteca `scheduler-telemetry` das aplicações clientes e também respondem consultas de status e sincronização solicitadas pela biblioteca:

##### **1.1 Registro de Aplicação**
```http
POST /api/projects/register
Content-Type: application/json
Authorization: Bearer {token}

{
  "appName": "my-app",
  "host": "localhost", 
  "port": 8080,
  "scheduledJobs": [
    {
      "jobId": "data-processing",
      "methodName": "processData",
      "className": "DataScheduler",
      "cronExpression": "0 */5 * * * ?",
      "description": "Processamento de dados a cada 5 minutos"
    }
  ]
}
```

##### **1.2 Relatório de Execução**
```http
POST /api/executions/report
Content-Type: application/json
Authorization: Bearer {token}

{
  "jobId": "data-processing",
  "runId": "uuid-123",
  "status": "SUCCESS",
  "processedItems": 1000,
  "failedItems": 5,
  "skippedItems": 2,
  "itemMetadata": [...],
  "exceptions": [...],
  "startTime": "2024-01-19T10:00:00Z",
  "endTime": "2024-01-19T10:05:00Z"
}
```

##### **1.3 Atualização de Progresso**
```http
POST /api/executions/progress
Content-Type: application/json
Authorization: Bearer {token}

{
  "jobId": "data-processing",
  "runId": "uuid-123",
  "currentItem": 500,
  "totalItems": 1000,
  "progressPercentage": 50.0,
  "processedItems": 500,
  "failedItems": 2,
  "itemsPerSecond": 10.5
}
```

##### **1.4 Sincronização de Sequência**
```http
POST /api/executions/sequence
Content-Type: application/json
Authorization: Bearer {token}

{
  "runId": "uuid-123",
  "sequenceNumber": 1,
  "messageType": "PROGRESS",
  "checksum": "sha256-hash"
}
```

##### **1.5 Status da Execução (Consulta pela Biblioteca)**
```http
GET /api/executions/{runId}/status
Authorization: Bearer {token}

Resposta:
{
  "runId": "uuid-123",
  "jobId": "data-processing",
  "appName": "my-app",
  "status": "RUNNING|COMPLETED|FAILED|PAUSED",
  "startTime": "2024-01-19T10:00:00Z",
  "currentProgress": {
    "currentItem": 500,
    "totalItems": 1000,
    "progressPercentage": 50.0,
    "processedItems": 500,
    "failedItems": 2,
    "skippedItems": 0
  },
  "lastUpdate": "2024-01-19T10:02:30Z",
  "estimatedCompletion": "2024-01-19T10:05:00Z"
}
```

##### **1.6 Sincronização de Estado (Consulta pela Biblioteca)**
```http
GET /api/executions/{runId}/sync
Authorization: Bearer {token}

Resposta:
{
  "runId": "uuid-123",
  "lastReceivedSequence": 15,
  "missingSequences": [12, 13],
  "status": "SYNC_REQUIRED|SYNCED|ERROR",
  "message": "Missing sequences detected, retransmission required"
}
```

#### **Grupo 2: Endpoints para Fornecer Dados ao Frontend (Outbound)**
Estes endpoints fornecem dados para o frontend consumir e exibir informações sobre aplicações, execuções e métricas:

##### **2.1 Navegação por Aplicações**
- `GET /api/applications` - Lista todas as aplicações integradas
- `GET /api/applications/{id}` - Acessa uma aplicação específica pelo ID
- `GET /api/applications/{id}/status` - Status atual da aplicação
- `GET /api/applications/{id}/jobs` - Lista jobs agendados da aplicação

##### **2.2 Navegação por Execuções**
- `GET /api/applications/{id}/executions` - Lista execuções de uma aplicação específica
- `GET /api/executions` - Lista todas as execuções (com filtros)
- `GET /api/executions/{id}` - Acessa uma execução específica pelo ID
- `GET /api/executions/{id}/progress` - Progresso detalhado de uma execução
- `GET /api/executions/{id}/items` - Itens processados em uma execução
- `GET /api/executions/{id}/exceptions` - Exceções capturadas em uma execução

##### **2.3 Navegação por Jobs**
- `GET /api/applications/{id}/jobs/{jobId}` - Acessa um job específico de uma aplicação
- `GET /api/applications/{id}/jobs/{jobId}/executions` - Execuções de um job específico
- `GET /api/jobs/{jobId}/executions` - Execuções de um job específico (todas as aplicações)

##### **2.4 Navegação por Métricas**
- `GET /api/applications/{id}/metrics` - Métricas agregadas de uma aplicação
- `GET /api/applications/{id}/jobs/{jobId}/metrics` - Métricas de um job específico
- `GET /api/metrics/aggregated` - Métricas agregadas de todas as aplicações
- `GET /api/metrics/performance` - Métricas de performance globais

##### **2.5 Gerenciamento de Tokens (Admin)**
- `POST /api/admin/tokens` - Cria novo token para aplicação
- `GET /api/admin/tokens` - Lista todos os tokens ativos
- `PUT /api/admin/tokens/{id}` - Atualiza token existente
- `DELETE /api/admin/tokens/{id}` - Revoga token

### 🔄 **Fluxo de Dados entre os Grupos**

```
┌─────────────────┐    Grupo 1 (Inbound)    ┌─────────────────┐
│   Aplicação     │ ←───────────────────────→ │   API Central   │
│   Cliente       │    (scheduler-telemetry) │                 │
│                 │    Recebe telemetria     │                 │
│                 │    + Responde consultas  │                 │
└─────────────────┘                           └─────────────────┘
                                                         │
                                                         ▼
                                                ┌─────────────────┐
                                                │   PostgreSQL    │
                                                │   (Storage)     │
                                                └─────────────────┘
                                                         │
                                                         ▼
┌─────────────────┐    Grupo 2 (Outbound)    ┌─────────────────┐
│   Frontend      │ ←──────────────────────── │   API Central   │
│   (Dashboard)   │    (Dados processados)   │                 │
└─────────────────┘                           └─────────────────┘
```

### 📊 **Diferenças entre os Grupos**

| Aspecto | Grupo 1 (Inbound) | Grupo 2 (Outbound) |
|---------|-------------------|-------------------|
| **Direção** | Recebe dados + Consultas da biblioteca | Fornece dados ao frontend |
| **Cliente** | Aplicações Spring Boot | Frontend/Dashboard |
| **Autenticação** | Token de aplicação | Token de usuário/admin |
| **Frequência** | Alta (execuções em tempo real) | Baixa (consultas sob demanda) |
| **Payload** | Dados de telemetria + Consultas de status | Dados processados e agregados |
| **Cache** | Não aplicável | Aplicável para performance |
| **Rate Limiting** | Configurável por aplicação | Configurável por usuário |
| **Endpoints** | 6 endpoints (4 POST + 2 GET) | Múltiplos endpoints GET |
| **Propósito** | Receber telemetria e responder consultas | Fornecer dados para visualização |

### 🔗 **Navegação Fluida e Links HATEOAS**

#### **Respostas com Links de Navegação**
Cada resposta do Grupo 2 deve incluir links HATEOAS para facilitar a navegação:

```json
{
  "id": 123,
  "appName": "data-processor",
  "host": "prod-server-01",
  "port": 8080,
  "status": "ACTIVE",
  "lastSeen": "2024-01-19T15:30:00Z",
  "_links": {
    "self": { "href": "/api/applications/123" },
    "jobs": { "href": "/api/applications/123/jobs" },
    "executions": { "href": "/api/applications/123/executions" },
    "metrics": { "href": "/api/applications/123/metrics" },
    "status": { "href": "/api/applications/123/status" }
  }
}
```

#### **Exemplos de Navegação Fluida**

##### **Fluxo 1: Explorar Aplicação → Jobs → Execuções**
```
1. GET /api/applications → Lista todas as aplicações
2. GET /api/applications/123 → Acessa aplicação "data-processor"
3. GET /api/applications/123/jobs → Lista jobs da aplicação
4. GET /api/applications/123/jobs/batch-processing → Acessa job específico
5. GET /api/applications/123/jobs/batch-processing/executions → Execuções do job
6. GET /api/executions/456 → Detalhes de uma execução específica
7. GET /api/executions/456/progress → Progresso da execução
8. GET /api/executions/456/items → Itens processados
```

##### **Fluxo 2: Explorar Execuções → Aplicação → Jobs**
```
1. GET /api/executions → Lista todas as execuções
2. GET /api/executions/789 → Acessa execução específica
3. GET /api/applications/123 → Acessa aplicação da execução
4. GET /api/applications/123/jobs → Lista jobs da aplicação
5. GET /api/applications/123/jobs/real-time-processing → Acessa outro job
6. GET /api/applications/123/jobs/real-time-processing/executions → Execuções do job
```

##### **Fluxo 3: Análise de Performance → Aplicação → Job → Execuções**
```
1. GET /api/metrics/performance → Métricas de performance globais
2. GET /api/applications/456 → Acessa aplicação com melhor performance
3. GET /api/applications/456/metrics → Métricas detalhadas da aplicação
4. GET /api/applications/456/jobs/optimized-batch → Acessa job otimizado
5. GET /api/applications/456/jobs/optimized-batch/executions → Execuções do job
6. GET /api/executions/101 → Acessa execução com melhor performance
7. GET /api/executions/101/items → Itens processados com sucesso
```

### 🔍 **Filtros e Paginação (Grupo 2)**

#### **Filtros Disponíveis**
- **Por aplicação**: `?applicationId=123`
- **Por job**: `?jobId=data-processing`
- **Por período**: `?startDate=2024-01-01&endDate=2024-01-31`
- **Por status**: `?status=SUCCESS|FAILED|RUNNING`
- **Por performance**: `?minItemsPerSecond=10&maxErrorRate=0.05`
- **Por duração**: `?minDuration=30000&maxDuration=300000` (em ms)

#### **Paginação e Ordenação**
- **Paginação**: `?page=0&size=20`
- **Ordenação**: `?sort=processedItems,desc&sort=startTime,asc`
- **Ordenação por performance**: `?sort=itemsPerSecond,desc`
- **Ordenação por falhas**: `?sort=failedItems,desc`

### 🗄️ **Modelo de Dados PostgreSQL**

#### **Tabelas Principais**
1. **applications** - Aplicações registradas
2. **application_tokens** - Tokens de autenticação
3. **scheduled_jobs** - Jobs agendados por aplicação
4. **executions** - Execuções de jobs
5. **execution_progress** - Progresso das execuções
6. **execution_items** - Itens processados por execução
7. **execution_exceptions** - Exceções capturadas
8. **application_status** - Status de início/parada das aplicações

### 🔐 **Sistema de Autenticação**

#### **Gerenciamento de Tokens**
- **Endpoint para registrar token**: `POST /api/admin/tokens`
- **Validação automática** em todas as requisições de telemetria (Grupo 1)
- **Associação token ↔ aplicação** para rastreamento
- **Renovação e revogação** de tokens
- **Validação de usuários** para endpoints do Grupo 2

### 🎯 **Regras de Negócio**

#### **Filtro de Execuções Válidas**
- **Execuções válidas**: Mais de 1 item processado
- **Execuções ignoradas**: 0 ou 1 item processado (execuções vazias)
- **Motivo**: Evitar ruído de schedulers que executam mas não processam dados

#### **Rastreamento de Status**
- **Registro de início**: Quando aplicação se registra
- **Registro de parada**: Quando aplicação para de enviar telemetria
- **Heartbeat**: Verificação periódica de aplicações ativas

---

## 🏗️ **ARQUITETURA PROPOSTA**

### **Padrão Arquitetural: Arquitetura Hexagonal (Ports & Adapters)**
A aplicação deve seguir a **Arquitetura Hexagonal** com separação clara entre:
- **Domain Layer** - Regras de negócio e entidades
- **Application Layer** - Casos de uso e serviços de aplicação
- **Infrastructure Layer** - Implementações concretas (banco, HTTP, etc.)

### **Princípios de Design**
- **Separação de Responsabilidades** entre camadas
- **Inversão de Dependência** (domain não depende de infrastructure)
- **Mappers** para conversão entre entidades de domínio e DTOs
- **Ports** (interfaces) definidos no domain
- **Adapters** (implementações) na infrastructure

### **Componentes Principais por Camada**

#### **Domain Layer (Core)**
1. **Entities** - Entidades de domínio (Application, Execution, Job, etc.)
2. **Value Objects** - Objetos de valor (Token, Status, Progress, etc.)
3. **Ports** - Interfaces para serviços externos
4. **Domain Services** - Regras de negócio complexas

#### **Application Layer (Use Cases)**
1. **Use Cases** - Casos de uso da aplicação
2. **Application Services** - Orquestração de operações
3. **DTOs** - Objetos de transferência de dados
4. **Mappers** - Conversão entre entidades e DTOs

#### **Infrastructure Layer (Adapters)**
1. **Controllers** - Endpoints HTTP REST
2. **Repositories** - Implementações de acesso a dados
3. **External Services** - Clientes para serviços externos
4. **Configuration** - Configurações e beans Spring

### **Mappers entre Camadas**
- **Entity ↔ DTO**: Conversão entre entidades de domínio e DTOs de API
- **Domain ↔ Persistence**: Conversão entre entidades e entidades JPA
- **External ↔ Internal**: Conversão entre formatos externos e internos
- **Request ↔ Command**: Conversão entre requests HTTP e comandos de domínio

### **Componentes Específicos da Aplicação**
1. **TelemetryReceiver** - Recebe dados da biblioteca (Infrastructure)
2. **AuthenticationService** - Valida tokens de aplicação (Application)
3. **TelemetryProcessor** - Processa e valida dados recebidos (Application)
4. **DataPersistenceService** - Persiste dados no PostgreSQL (Infrastructure)
5. **MetricsAggregator** - Calcula métricas agregadas (Application)
6. **FrontendAPI** - Endpoints para consulta de dados (Infrastructure)
7. **ApplicationMonitor** - Monitora status das aplicações (Application)
8. **NavigationService** - Gerencia links HATEOAS e navegação fluida (Application)
9. **RelationshipResolver** - Resolve relacionamentos entre entidades (Application)
10. **StatusQueryService** - Responde consultas de status da biblioteca (Application)
11. **SyncQueryService** - Responde consultas de sincronização da biblioteca (Application)

### **Estrutura de Pacotes**
```
com.schedkiwi.centraltelemetry/
├── domain/                    # Camada de Domínio
│   ├── entities/             # Entidades de domínio
│   ├── valueobjects/         # Objetos de valor
│   ├── ports/                # Interfaces (Ports)
│   └── services/             # Serviços de domínio
├── application/               # Camada de Aplicação
│   ├── usecases/             # Casos de uso
│   ├── services/             # Serviços de aplicação
│   ├── dto/                  # DTOs de entrada/saída
│   └── mappers/              # Mappers entre camadas
└── infrastructure/            # Camada de Infraestrutura
    ├── controllers/           # Controllers REST
    ├── repositories/          # Implementações de repositórios
    ├── persistence/           # Entidades JPA e configurações
    ├── external/              # Clientes para serviços externos
    └── config/                # Configurações Spring
```

### **Tecnologias para Arquitetura Hexagonal**
- **Backend**: Spring Boot 3.x + Kotlin
- **Banco**: PostgreSQL 15+
- **ORM**: Spring Data JPA + Hibernate
- **Mappers**: MapStruct para conversão automática entre camadas
- **Injeção de Dependência**: Spring IoC Container
- **Documentação**: OpenAPI 3.0 (Swagger)
- **Testes**: JUnit 5 + MockK + TestContainers
- **Validação**: Bean Validation (JSR-303)
- **Serialização**: Jackson com configuração para Kotlin

---

## 📚 **DOCUMENTAÇÃO REQUERIDA**

### **Diagramas**
1. **Arquitetura Hexagonal Geral** - Visão de alto nível com 3 camadas e mappers
2. **Fluxo de Dados Bidirecional** - Como os dados fluem da lib para a API Central e vice-versa
3. **Modelo de Dados** - ERD do PostgreSQL com todas as tabelas e relacionamentos
4. **Sequência de Operações** - Fluxo completo de registro → execução → telemetria → consulta → resposta
5. **Fluxo de Sincronização** - Como a biblioteca garante ordem e consistência dos dados
6. **Fluxo de Consulta de Status** - Como a biblioteca consulta status em tempo real
7. **Casos de Uso** - Cenários principais de utilização com endpoints específicos
8. **Arquitetura de Componentes por Camada** - Detalhamento dos componentes em cada camada
9. **Fluxo de Navegação HATEOAS** - Como os links relacionados funcionam para navegação fluida
10. **Modelo de Autenticação** - Como os tokens são validados e associados às aplicações
11. **Diagrama de Mappers** - Como as conversões acontecem entre camadas
12. **Diagrama de Ports e Adapters** - Interfaces e implementações da arquitetura hexagonal

### **Casos de Uso**
1. **Registro de Nova Aplicação** - Aplicação se registra via `POST /api/projects/register` e obtém token de autenticação
2. **Monitoramento de Execução em Tempo Real** - Acompanhamento de progresso via `POST /api/executions/progress` e consulta de status via `GET /api/executions/{runId}/status`
3. **Sincronização e Consistência de Dados** - Garantia de ordem via `POST /api/executions/sequence` e verificação de sincronização via `GET /api/executions/{runId}/sync`
4. **Relatório Final de Execução** - Envio de estatísticas completas via `POST /api/executions/report` após conclusão do scheduler
5. **Análise de Performance Histórica** - Comparação de execuções ao longo do tempo usando endpoints do Grupo 2
6. **Detecção de Falhas e Alertas** - Identificação de problemas através de exceções capturadas e métricas de falha
7. **Dashboard de Métricas Agregadas** - Visão consolidada de todas as aplicações via endpoints do Grupo 2
8. **Navegação Fluida entre Entidades** - Exploração intuitiva de aplicações, jobs e execuções usando HATEOAS
9. **Investigação de Problemas** - Rastreamento de falhas desde aplicação até item específico usando endpoints de consulta
10. **Comparação de Aplicações** - Análise de performance entre diferentes sistemas integrados
11. **Monitoramento de Saúde das Aplicações** - Detecção de aplicações inativas e problemas de conectividade
12. **Auditoria e Compliance** - Rastreamento completo de todas as execuções e mudanças de status

### **Fluxos de Funcionamento**
1. **Fluxo de Registro** - Aplicação se registra via `POST /api/projects/register` e obtém token de autenticação
2. **Fluxo de Execução e Telemetria** - Dados de telemetria são enviados via `POST /api/executions/progress` e `POST /api/executions/report`
3. **Fluxo de Sincronização** - Garantia de ordem via `POST /api/executions/sequence` e verificação via `GET /api/executions/{runId}/sync`
4. **Fluxo de Consulta de Status** - Biblioteca consulta status via `GET /api/executions/{runId}/status` para monitoramento em tempo real
5. **Fluxo de Consulta Frontend** - Frontend consulta dados via endpoints do Grupo 2 para exibição e análise
6. **Fluxo de Monitoramento** - Sistema detecta aplicações inativas através de análise de telemetria e heartbeat
7. **Fluxo de Navegação por Aplicação** - Explorar aplicação → jobs → execuções → detalhes usando HATEOAS
8. **Fluxo de Navegação por Execução** - Explorar execução → aplicação → jobs → outras execuções usando links relacionados
9. **Fluxo de Navegação por Performance** - Análise de métricas → aplicação → job → execuções usando endpoints de métricas
10. **Fluxo de Investigação** - Detecção de problema → rastreamento via telemetria → análise → solução
11. **Fluxo de Sincronização de Estado** - Biblioteca verifica estado de sincronização e solicita retransmissão se necessário
12. **Fluxo de Auditoria** - Rastreamento completo de todas as operações para compliance e debugging

---

## 🚀 **ENTREGÁVEIS**

### **Código Fonte**
- API Central completa em Spring Boot + Kotlin com **Arquitetura Hexagonal**
- **3 camadas bem definidas**: Domain, Application, Infrastructure
- **Mappers MapStruct** para conversão automática entre camadas
- Scripts de criação do banco PostgreSQL com todas as 8 tabelas
- Configurações de ambiente e deployment
- Implementação completa dos 6 endpoints do Grupo 1 (4 POST + 2 GET)
- Implementação completa dos endpoints do Grupo 2 para frontend
- Sistema de autenticação com tokens de aplicação
- Implementação HATEOAS para navegação fluida
- **Ports e Adapters** implementados conforme padrão hexagonal

### **Documentação**
- Diagramas de arquitetura e fluxos (10 diagramas)
- Casos de uso detalhados (12 casos de uso)
- Fluxos de funcionamento (12 fluxos)
- Guia de API (OpenAPI/Swagger) para ambos os grupos de endpoints
- Manual de deployment e configuração
- Documentação de integração com a biblioteca scheduler-telemetry

### **Testes**
- **Testes Unitários**: Para todos os componentes de cada camada
- **Testes de Mappers**: Validação de conversão entre entidades e DTOs
- **Testes de Ports**: Verificação de contratos de interface
- **Testes de Integração**: Para endpoints do Grupo 1 e Grupo 2
- **Testes de Autenticação**: Validação de tokens e autorização
- **Testes de Performance**: Carga e sincronização de dados
- **Testes de Arquitetura**: Verificação de separação de camadas
- **Testes de Mappers**: Validação de conversões automáticas MapStruct

### **Deployment**
- **Docker**: Imagem containerizada com Spring Boot
- **Banco de Dados**: Scripts de migração PostgreSQL
- **Logs**: Configuração de logging estruturado e centralizado
