# SchedKiwi - Projeto de Telemetria de Schedulers

## Visão Geral
O SchedKiwi é um projeto que consiste em dois componentes principais:

1. **Biblioteca `scheduler-telemetry`** - Biblioteca Maven para instrumentação de schedulers Spring Boot
2. **API Central `central-telemetry-api`** - Gerenciador central para receber e processar dados de telemetria

## Estrutura do Projeto

### 📁 **Raiz do Projeto (`/`)**
```
schedkiwi/
├── scheduler-telemetry/           # Biblioteca Maven (código fonte da lib)
├── central-telemetry-api/         # API Central (código do gerenciador)
├── memory-bank/                   # Documentação e contexto do projeto
├── custom_modes/                  # Instruções de modos personalizados
└── optimization-journey/          # Documentação de otimização
```

### 📚 **Biblioteca `scheduler-telemetry/` (Raiz do Projeto)**
- **Localização**: Pasta `scheduler-telemetry/` na raiz do projeto
- **Tipo**: Módulo Maven independente
- **Propósito**: Biblioteca para instrumentação de schedulers Spring Boot
- **Artefato**: JAR Maven publicável
- **Pacote**: `com.schedkiwi.schedulertelemetry`

### 🚀 **API Central `central-telemetry-api/` (Raiz do Projeto)**
- **Localização**: Pasta `central-telemetry-api/` na raiz do projeto
- **Tipo**: Aplicação Spring Boot independente
- **Propósito**: Gerenciador central para receber e processar telemetria
- **Artefato**: Aplicação executável Spring Boot
- **Pacote**: `com.schedkiwi.centraltelemetry`

## Arquitetura

### Biblioteca → API Central
```
Aplicação Cliente (Spring Boot)
    ↓ (usa biblioteca scheduler-telemetry)
scheduler-telemetry (biblioteca)
    ↓ (envia telemetria via HTTP)
central-telemetry-api (gerenciador central)
    ↓ (armazena e disponibiliza dados)
PostgreSQL + API REST
```

## Tecnologias

### Biblioteca `scheduler-telemetry`
- **Linguagem**: Kotlin
- **Build**: Maven
- **Framework**: Spring Boot (auto-configuração)
- **AOP**: AspectJ para instrumentação
- **Comunicação**: HTTP REST

### API Central `central-telemetry-api`
- **Linguagem**: Kotlin
- **Build**: Maven
- **Framework**: Spring Boot
- **Banco**: PostgreSQL
- **Arquitetura**: Hexagonal (Ports & Adapters)

## Fluxo de Desenvolvimento

1. **Biblioteca**: Desenvolvimento e testes da biblioteca `scheduler-telemetry`
2. **API**: Desenvolvimento da API Central `central-telemetry-api`
3. **Integração**: Validação da comunicação entre biblioteca e API
4. **Publicação**: Biblioteca publicada no Maven Central
5. **Deploy**: API Central disponibilizada para uso

## Documentação

- **Biblioteca**: `scheduler-telemetry/docs/`
- **API Central**: `central-telemetry-api/docs/`
- **Projeto**: `memory-bank/` (este diretório)
- **Templates**: `memory-bank/templates/`
- **Regras**: `memory-bank/rules/`

## Histórico de Alterações

- **2024**: Criação inicial do projeto com estrutura de duas pastas na raiz
- **2024**: Definição da arquitetura biblioteca → API Central
- **2024**: Implementação da biblioteca scheduler-telemetry
- **2024**: Implementação da API Central central-telemetry-api
