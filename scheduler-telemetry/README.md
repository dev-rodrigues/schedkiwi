# Scheduler Telemetry Library

Biblioteca Maven em Kotlin para instrumentação de schedulers via AOP com telemetria e progresso em tempo real.

## 🎯 Objetivo

Esta biblioteca permite que aplicações Spring Boot com schedulers coletem telemetria detalhada sobre a execução dos jobs, incluindo:

- **Progresso em tempo real** com atualizações por item processado
- **Metadados detalhados** de cada item processado
- **Captura de exceções** sem interferir no comportamento dos schedulers
- **Relatórios assíncronos** enviados ao Gerenciador Central
- **Garantia de ordem sequencial** das mensagens com validação de integridade

## 🏗️ Arquitetura

```
scheduler-telemetry/
├─ src/main/kotlin/com/schedkiwi/schedulertelemetry/
│  ├─ config/          # Auto-configuração Spring Boot
│  ├─ aop/            # Anotação e aspecto para instrumentação
│  ├─ core/           # Classes core de telemetria e contexto
│  ├─ net/            # Comunicação HTTP e dispatchers
│  └─ scan/           # Scanner para descoberta de jobs
```

## 🚀 Uso Rápido

### 1. Adicionar Dependência

```xml
<dependency>
    <groupId>com.schedkiwi</groupId>
    <artifactId>scheduler-telemetry</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 2. Anotar Método do Scheduler

```kotlin
@Service
class MeuScheduler {
    
    @Scheduled(fixedRate = 60000)
    @MonitoredScheduled(jobId = "processamento-dados")
    fun processarDados() {
        val telemetry = SchedulerTelemetryHolder.getTelemetry()
        
        // Definir total esperado
        telemetry.setPlannedTotal(1000L)
        
        // Processar itens
        for (i in 1..1000) {
            try {
                // Lógica de processamento
                telemetry.addItem("item-$i", mapOf("index" to i))
            } catch (e: Exception) {
                telemetry.addFailedItem("item-$i", mapOf("index" to i), e)
            }
        }
    }
}
```

### 3. Configuração

```properties
# Habilitar telemetria
scheduler.telemetry.enabled=true

# URL do Gerenciador Central
scheduler.telemetry.manager-url=http://localhost:8080

# Endpoints
scheduler.telemetry.register-path=/api/projects/register
scheduler.telemetry.report-path=/api/executions/report
scheduler.telemetry.progress-path=/api/executions/progress

# Configurações de retry
scheduler.telemetry.max-retry=5
scheduler.telemetry.base-backoff-ms=500
```

## 🔧 Funcionalidades

### ✅ **Telemetria em Tempo Real**
- Progresso atualizado a cada item processado
- Metadados customizáveis por item
- Estatísticas de sucesso, falha e itens pulados

### ✅ **ThreadLocal-Safe**
- Múltiplos schedulers simultâneos
- Contexto isolado por thread
- Sem interferência entre execuções

### ✅ **Garantia de Ordem**
- Numeração sequencial das mensagens
- Validação de checksum SHA-256
- Retry inteligente com preservação de ordem

### ✅ **Auto-configuração**
- Configuração automática Spring Boot
- Sem necessidade de configuração manual
- Compatível com aplicações existentes

## 📡 Endpoints do Gerenciador Central

A biblioteca envia dados para os seguintes endpoints:

- **POST /api/projects/register** - Registro da aplicação
- **POST /api/executions/report** - Relatório final de execução
- **POST /api/executions/progress** - Atualizações de progresso
- **GET /api/executions/{runId}/status** - Status da execução
- **GET /api/executions/{runId}/sync** - Sincronização de estado

## 🧪 Testes

```bash
# Executar todos os testes
mvn test

# Executar testes com cobertura
mvn jacoco:report

# Executar testes específicos
mvn test -Dtest=ExecutionContextTest
```

## 📦 Build

```bash
# Compilar
mvn clean compile

# Testar
mvn test

# Empacotar
mvn package

# Instalar no repositório local
mvn install
```

## 🔍 Desenvolvimento

### Estrutura do Projeto

- **Core**: Classes fundamentais de telemetria e contexto
- **AOP**: Instrumentação via aspectos
- **Net**: Comunicação HTTP e dispatchers
- **Config**: Auto-configuração Spring Boot

### Padrões Utilizados

- **AOP (Aspect-Oriented Programming)** para instrumentação
- **ThreadLocal** para isolamento de contexto
- **Priority Queue** para ordenação de mensagens
- **Retry Pattern** com backoff exponencial
- **Builder Pattern** para construção de mensagens

## 📚 Documentação

- [Guia de Configuração](docs/configuration-guide.md)
- [Exemplos de Uso](docs/usage-examples.md)
- [API Reference](docs/api-reference.md)
- [Troubleshooting](docs/troubleshooting.md)

## 🤝 Contribuição

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## 📄 Licença

Este projeto está licenciado sob a Licença MIT - veja o arquivo [LICENSE](LICENSE) para detalhes.

## 🆘 Suporte

- **Issues**: [GitHub Issues](https://github.com/schedkiwi/scheduler-telemetry/issues)
- **Documentação**: [Wiki](https://github.com/schedkiwi/scheduler-telemetry/wiki)
- **Email**: support@schedkiwi.com

---

**Desenvolvido com ❤️ pela equipe SchedKiwi**
