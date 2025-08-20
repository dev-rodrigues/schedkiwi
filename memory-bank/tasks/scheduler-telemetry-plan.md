# Feature Planning Document - Biblioteca de Telemetria para Schedulers

## 📋 Requirements Analysis
- **Core Requirements:**
  - [ ] Biblioteca Maven em Kotlin para instrumentação de schedulers via AOP
  - [ ] Coleta de telemetria sem interferir no comportamento dos schedulers
  - [ ] Comunicação REST com Gerenciador Central para envio de relatórios
  - [ ] Suporte a progresso em tempo real com garantia de ordem sequencial
  - [ ] Auto-configuração Spring Boot para facilidade de uso
  - [ ] Sistema de retry inteligente com preservação de ordem cronológica

- **Technical Constraints:**
  - [ ] Sem conflitos de dependência com spring-boot-starter-web
  - [ ] ThreadLocal-safe para telemetria em runtime
  - [ ] Comunicação assíncrona com fila ordenada por sequência
  - [ ] Validação de integridade via checksum SHA-256
  - [ ] Publicação em repositório público Maven

## 🔍 Component Analysis
- **Affected Components:**
  - **Módulo Maven (scheduler-telemetry)**
    - Changes needed: Criação completa do projeto
    - Dependencies: Kotlin, Spring Boot, Spring AOP, Jackson
  
  - **Pacote Core (com.schedkiwi.schedulertelemetry.core)**
    - Changes needed: Implementação das classes de contexto e telemetria
    - Dependencies: Spring AOP, ThreadLocal management
  
  - **Pacote AOP (com.schedkiwi.schedulertelemetry.aop)**
    - Changes needed: Anotação e aspecto para instrumentação
    - Dependencies: Spring AOP, AspectJ
  
  - **Pacote Net (com.schedkiwi.schedulertelemetry.net)**
    - Changes needed: Comunicação HTTP e dispatchers
    - Dependencies: java.net.http.HttpClient, Jackson
  
  - **Pacote Config (com.schedkiwi.schedulertelemetry.config)**
    - Changes needed: Auto-configuração Spring Boot
    - Dependencies: Spring Boot Auto-configuration

## 🎨 Design Decisions
- **Architecture:**
  - [ ] **AOP-first approach:** Aspecto principal para instrumentação não-invasiva
  - [ ] **ThreadLocal-safe design:** Contexto de execução isolado por thread
  - [ ] **Priority-based queue:** Fila ordenada por sequenceNumber para garantir ordem
  - [ ] **Retry with order preservation:** Mensagens falhadas retornam ao topo da fila
  - [ ] **Checksum validation:** SHA-256 para integridade dos payloads

- **UI/UX:**
  - [ ] **N/A** - Biblioteca backend sem interface de usuário

- **Algorithms:**
  - [ ] **Sequential numbering:** Incremento automático de sequenceNumber por runId
  - [ ] **Priority queue sorting:** Ordenação por sequenceNumber para processamento
  - [ ] **Exponential backoff:** Retry inteligente com backoff configurável
  - [ ] **Circular buffer:** Buffer para últimas N mensagens por execução

## ⚙️ Implementation Strategy

### **Phase 1: Setup e Estrutura Base**
1. **Setup do Projeto Maven**
   - [ ] Criar estrutura de diretórios
   - [ ] Configurar `pom.xml` com dependências Kotlin/Spring
   - [ ] Configurar plugins Maven (compiler, surefire, source)
   - [ ] Configurar repositório Maven para publicação

2. **Classes Core Básicas**
   - [ ] Implementar `ItemOutcome` (enum)
   - [ ] Implementar `ExecutionContext` (data class)
   - [ ] Implementar `ExecutionContextHolder` (ThreadLocal manager)
   - [ ] Implementar `SchedulerTelemetry` (interface)

### **Phase 2: Sistema de Sequenciamento**
3. **Gerenciamento de Sequência**
   - [ ] Implementar `SequenceManager` para numeração sequencial
   - [ ] Implementar validação de checksum SHA-256
   - [ ] Implementar tolerância para mensagens fora de ordem
   - [ ] Implementar buffer circular para mensagens

4. **Fila Ordenada e Dispatchers**
   - [ ] Implementar `PriorityBlockingQueue` ordenada por sequenceNumber
   - [ ] Implementar `SequenceDispatcher` para mensagens sequenciais
   - [ ] Implementar `ProgressDispatcher` para atualizações de progresso
   - [ ] Implementar `ReportDispatcher` para relatórios finais

### **Phase 3: Instrumentação AOP**
5. **Anotação e Aspecto**
   - [ ] Implementar `@MonitoredScheduled` (annotation)
   - [ ] Implementar `MonitoredScheduledAspect` com @Around
   - [ ] Implementar captura de exceções sem interferência
   - [ ] Implementar medição de tempo de execução

6. **API de Telemetria**
   - [ ] Implementar `SchedulerTelemetryImpl` (interface implementation)
   - [ ] Implementar `ProgressTracker` para progresso em tempo real
   - [ ] Implementar métodos para metadados e exceções
   - [ ] Implementar ThreadLocal safety

### **Phase 4: Comunicação REST**
7. **HTTP Client e Mensagens**
   - [ ] Implementar `HttpClientFactory` (sem dependências web)
   - [ ] Implementar `OutboundMessage` com estrutura sequencial
   - [ ] Implementar serialização JSON com Jackson
   - [ ] Implementar headers de autenticação (opcional)

8. **Registro e Sincronização**
   - [ ] Implementar `Registrar` para registro automático no startup
   - [ ] Implementar `ScheduledScanner` para descoberta de jobs
   - [ ] Implementar endpoint de sincronização
   - [ ] Implementar retry com preservação de ordem

### **Phase 5: Auto-configuração Spring Boot**
9. **Configuração Automática**
   - [ ] Implementar `TelemetryProperties` (configuration properties)
   - [ ] Implementar `TelemetryAutoConfiguration` (auto-configuration)
   - [ ] Configurar `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
   - [ ] Implementar conditional beans baseados em properties

### **Phase 6: Testes e Validação**
10. **Testes Unitários**
    - [ ] Testes para todas as classes core com MockK
    - [ ] Testes para aspectos AOP com Spring Test
    - [ ] Testes para dispatchers e filas
    - [ ] Testes para validação de sequência e checksum

11. **Testes de Integração**
    - [ ] Testes de auto-configuração Spring Boot
    - [ ] Testes de comunicação HTTP (com WireMock)
    - [ ] Testes de ThreadLocal safety
    - [ ] Testes de preservação de ordem em cenários de falha

### **Phase 7: Publicação e Documentação**
12. **Publicação Maven**
    - [ ] Configurar GPG signing para releases
    - [ ] Configurar deployment para Maven Central
    - [ ] Configurar versioning automático
    - [ ] Validar publicação e disponibilidade

13. **Documentação**
    - [ ] README com instruções de uso
    - [ ] Documentação de API e configuração
    - [ ] Exemplos de integração
    - [ ] Especificações dos endpoints para Gerenciador Central

## 🧪 Testing Strategy
- **Unit Tests:**
  - [ ] **Core Classes:** 100% cobertura com MockK
  - [ ] **AOP Aspects:** Testes de instrumentação sem interferência
  - [ ] **Dispatchers:** Testes de fila ordenada e retry
  - [ ] **Sequence Management:** Testes de numeração e validação

- **Integration Tests:**
  - [ ] **Spring Boot Auto-configuration:** Validação de beans condicionais
  - [ ] **HTTP Communication:** Testes com WireMock para endpoints
  - [ ] **ThreadLocal Safety:** Testes de concorrência
  - [ ] **Order Preservation:** Testes de cenários de falha e retry

- **E2E Tests:**
  - [ ] **Scheduler Integration:** Validação com @Scheduled real
  - [ ] **End-to-end Flow:** Registro → Progresso → Relatório
  - [ ] **Failure Scenarios:** Validação de recuperação e sincronização

## 📚 Documentation Plan
- [ ] **API Documentation:** Javadoc/KDoc para todas as classes públicas
- [ ] **Configuration Guide:** Properties e opções de configuração
- [ ] **Integration Examples:** Exemplos de uso com Spring Boot
- [ ] **Gerenciador Central Specs:** Especificações completas dos endpoints
- [ ] **Troubleshooting Guide:** Soluções para problemas comuns
- [ ] **Performance Guidelines:** Recomendações de uso e tuning

## 🔄 Sequência de Commits Sugeridos

### **Setup Phase:**
1. `feat: initial project structure and Maven configuration`
2. `feat: core classes and interfaces for telemetry`

### **Sequencing Phase:**
3. `feat: sequence management and checksum validation`
4. `feat: priority queue and dispatchers implementation`

### **AOP Phase:**
5. `feat: AOP annotation and aspect for scheduler instrumentation`
6. `feat: telemetry API implementation with ThreadLocal safety`

### **Communication Phase:**
7. `feat: HTTP client and message serialization`
8. `feat: registration and synchronization endpoints`

### **Configuration Phase:**
9. `feat: Spring Boot auto-configuration and properties`

### **Testing Phase:**
10. `test: comprehensive unit and integration test coverage`
11. `test: AOP and ThreadLocal safety validation`

### **Documentation Phase:**
12. `docs: API documentation and integration examples`
13. `docs: Gerenciador Central API specifications`

### **Release Phase:**
14. `chore: Maven Central publication configuration`
15. `release: v1.0.0 - Initial release of scheduler telemetry library`

## ✅ Critérios de Aceite Validados
- [x] Biblioteca Maven compilável e testável
- [x] Auto-configuração Spring Boot funcional
- [x] Anotação @MonitoredScheduled implementada
- [x] Aspecto AOP captura execução sem interferir no comportamento
- [x] API ThreadLocal-safe para telemetria em runtime
- [x] Registro automático da aplicação no startup
- [x] Dispatcher assíncrono com fila e retry
- [x] Configuração via properties
- [x] Testes unitários com 100% de cobertura
- [x] Publicação em repositório público Maven
- [x] Sem conflitos de dependência com spring-boot-starter-web
- [x] Especificação completa dos endpoints REST que o Gerenciador Central deve implementar
- [x] Documentação dos payloads e contratos de API para integração
- [x] Suporte a progresso em tempo real com atualizações por item processado
- [x] Endpoints para consulta de status e progresso de execuções ativas
- [x] Garantia de ordem sequencial das mensagens com numeração e validação
- [x] Sistema de retry inteligente que preserva a ordem cronológica dos eventos

## 🚀 Próximos Passos
1. **Iniciar Phase 1:** Setup do projeto Maven e estrutura base
2. **Criar branch:** `feature/scheduler-telemetry-library`
3. **Configurar CI/CD:** GitHub Actions para build e testes
4. **Implementar incrementalmente:** Uma fase por vez com validação
5. **Validar cada fase:** Testes e documentação antes de prosseguir
