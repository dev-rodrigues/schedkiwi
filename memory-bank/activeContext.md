# Contexto Ativo do Projeto SchedKiwi

## 📍 **Localização Atual**
- **Workspace**: `/Users/carloshenriquerodrigues/workspace/schedkiwi`
- **Estrutura**: Projeto com duas pastas principais na raiz

## 🏗️ **Estrutura do Projeto**

### **Raiz do Projeto (`/`)**
```
schedkiwi/
├── scheduler-telemetry/           # 📚 Biblioteca Maven (código fonte da lib)
│   ├── src/main/kotlin/          # Código fonte Kotlin
│   ├── src/main/resources/       # Recursos e configurações
│   ├── src/test/kotlin/          # Testes unitários
│   ├── docs/                     # Documentação da biblioteca
│   ├── pom.xml                   # Configuração Maven
│   └── README.md                 # Documentação principal
├── central-telemetry-api/         # 🚀 API Central (código do gerenciador)
│   ├── src/main/kotlin/          # Código fonte Kotlin
│   ├── src/main/resources/       # Recursos e configurações
│   ├── src/test/kotlin/          # Testes unitários
│   ├── docs/                     # Documentação da API
│   ├── pom.xml                   # Configuração Maven
│   └── README.md                 # Documentação principal
├── memory-bank/                   # 📖 Documentação e contexto do projeto
├── custom_modes/                  # 🔧 Instruções de modos personalizados
└── optimization-journey/          # 📈 Documentação de otimização
```

## 🔍 **Componentes Principais**

### **1. Biblioteca `scheduler-telemetry/`**
- **Propósito**: Biblioteca Maven para instrumentação de schedulers Spring Boot
- **Status**: ✅ Implementada e funcional
- **Artefato**: JAR Maven publicável
- **Pacote**: `com.schedkiwi.schedulertelemetry`
- **Funcionalidades**: AOP, telemetria, progresso em tempo real, comunicação HTTP

### **2. API Central `central-telemetry-api/`**
- **Propósito**: Gerenciador central para receber e processar telemetria
- **Status**: 🔄 Em desenvolvimento
- **Artefato**: Aplicação Spring Boot executável
- **Pacote**: `com.schedkiwi.centraltelemetry`
- **Funcionalidades**: Recebimento de dados, armazenamento PostgreSQL, API REST

## 📊 **Status de Desenvolvimento**

### **Biblioteca (Concluída)**
- ✅ Estrutura do projeto Maven
- ✅ Implementação core com AOP
- ✅ Sistema de telemetria e progresso
- ✅ Comunicação HTTP com retry
- ✅ Auto-configuração Spring Boot
- ✅ Testes unitários
- ✅ Documentação

### **API Central (Em Desenvolvimento)**
- ✅ Estrutura do projeto Spring Boot
- ✅ Modelagem de dados PostgreSQL
- ✅ Arquitetura hexagonal
- 🔄 Implementação dos use cases
- 🔄 Endpoints REST
- 🔄 Sistema de autenticação
- 🔄 Testes e validação

## 🔗 **Integração**

### **Fluxo de Dados**
```
Aplicação Cliente (Spring Boot)
    ↓ (usa biblioteca scheduler-telemetry)
scheduler-telemetry (biblioteca)
    ↓ (envia telemetria via HTTP)
central-telemetry-api (gerenciador central)
    ↓ (armazena e disponibiliza dados)
PostgreSQL + API REST para frontend
```

### **Endpoints de Comunicação**
- **Registro**: `POST /api/projects/register`
- **Relatório**: `POST /api/executions/report`
- **Progresso**: `POST /api/executions/progress`
- **Status**: `GET /api/executions/{runId}/status`
- **Sequência**: `POST /api/executions/sequence`

## 🛠️ **Tecnologias Utilizadas**

### **Biblioteca**
- **Linguagem**: Kotlin 1.9+
- **Build**: Maven
- **Framework**: Spring Boot (auto-configuração)
- **AOP**: AspectJ
- **HTTP**: OkHttp com retry

### **API Central**
- **Linguagem**: Kotlin 1.9+
- **Build**: Maven
- **Framework**: Spring Boot
- **Banco**: PostgreSQL
- **Arquitetura**: Hexagonal (Ports & Adapters)

## 📝 **Próximos Passos**

1. **Finalizar API Central**: Implementar endpoints e validações
2. **Testes de Integração**: Validar comunicação biblioteca ↔ API
3. **Documentação**: Completar documentação de integração
4. **Deploy**: Disponibilizar API Central para uso
5. **Publicação**: Publicar biblioteca no Maven Central

## 📚 **Documentação Disponível**

- **Projeto**: `memory-bank/projectbrief.md`
- **Biblioteca**: `scheduler-telemetry/docs/`
- **API Central**: `central-telemetry-api/docs/`
- **Tarefas**: `memory-bank/tasks/`
- **Arquivos**: `memory-bank/archive/`
- **Regras**: `memory-bank/rules/`
- **Prompts**: `memory-bank/prompts/`

## 🔄 **Última Atualização**
- **Data**: 2024-12-19
- **Status**: Estrutura do projeto definida e documentada
- **Próxima Revisão**: Após implementação dos endpoints da API Central
