# Memory Bank - SchedKiwi

## 📖 **Visão Geral**
Este diretório contém toda a documentação, contexto e controle do projeto SchedKiwi, um sistema de telemetria para schedulers Spring Boot.

## 🏗️ **Estrutura do Projeto SchedKiwi**

### **Raiz do Projeto (`/`)**
```
schedkiwi/
├── scheduler-telemetry/           # 📚 Biblioteca Maven (código fonte da lib)
├── central-telemetry-api/         # 🚀 API Central (código do gerenciador)
├── memory-bank/                   # 📖 Este diretório (documentação e contexto)
├── custom_modes/                  # 🔧 Instruções de modos personalizados
└── optimization-journey/          # 📈 Documentação de otimização
```

## 📁 **Estrutura do Memory Bank**

### **Arquivos Principais**
- **`projectbrief.md`** - Visão geral e contexto do projeto
- **`activeContext.md`** - Contexto ativo e status atual
- **`progress.md`** - Acompanhamento do progresso do projeto

### **Diretórios**
- **`tasks/`** - Tarefas ativas e planejamento
- **`archive/`** - Arquivos de tarefas concluídas
- **`rules/`** - Regras e padrões do projeto
- **`prompts/`** - Instruções para modos de operação
- **`templates/`** - Modelos para commits, PRs e tarefas

## 🔍 **Componentes do Projeto**

### **1. Biblioteca `scheduler-telemetry/`**
- **Localização**: Pasta na raiz do projeto
- **Propósito**: Biblioteca Maven para instrumentação de schedulers
- **Status**: ✅ 100% implementada
- **Artefato**: JAR Maven publicável

### **2. API Central `central-telemetry-api/`**
- **Localização**: Pasta na raiz do projeto
- **Propósito**: Gerenciador central para telemetria
- **Status**: 🔄 30% implementada
- **Artefato**: Aplicação Spring Boot

## 📊 **Status Atual**

### **Progresso Geral**: 65% concluído
- **Biblioteca**: ✅ Completa
- **API Central**: 🔄 Em desenvolvimento
- **Integração**: 📋 Pendente

### **Próximos Passos**
1. Finalizar implementação dos use cases da API Central
2. Implementar endpoints REST
3. Sistema de autenticação
4. Testes e validação
5. Integração biblioteca ↔ API

## 📚 **Documentação Disponível**

### **Projeto**
- **`projectbrief.md`** - Visão geral completa
- **`activeContext.md`** - Contexto atual e estrutura
- **`progress.md`** - Acompanhamento detalhado

### **Tarefas**
- **`tasks/central-telemetry-api-task.md`** - Tarefa da API Central
- **`tasks/scheduler-telemetry-task.md`** - Tarefa da biblioteca
- **`tasks/scheduler-telemetry-plan.md`** - Plano de implementação

### **Arquivos**
- **`archive/archive-scheduler-telemetry-implementation.md`** - Implementação da biblioteca

### **Regras e Padrões**
- **`rules/`** - Padrões de código, testes e operações
- **`prompts/`** - Instruções para modos de trabalho
- **`templates/`** - Modelos para documentação

## 🎯 **Como Usar**

### **Para Desenvolvedores**
1. **Entender o projeto**: Leia `projectbrief.md`
2. **Ver status atual**: Consulte `activeContext.md` e `progress.md`
3. **Ver tarefas**: Acesse `tasks/`
4. **Seguir padrões**: Consulte `rules/`

### **Para Contribuição**
1. **Criar tarefa**: Use template em `templates/TASK.example.md`
2. **Seguir regras**: Aplique padrões em `rules/`
3. **Documentar**: Atualize arquivos relevantes
4. **Arquivar**: Mova tarefas concluídas para `archive/`

## 🔄 **Atualizações**

### **Última Atualização**: 2024-12-19
- **Responsável**: Sistema de Memory Bank
- **Mudança**: Estrutura do projeto documentada e localizações especificadas
- **Próxima Revisão**: Após implementação dos endpoints REST

### **Histórico de Mudanças**
- **2024-12-19**: Estrutura do projeto documentada
- **2024-12-19**: Localizações de código especificadas
- **2024-12-19**: Memory Bank organizado e atualizado

## 📞 **Suporte**

Para dúvidas sobre:
- **Estrutura do projeto**: Consulte `projectbrief.md`
- **Status atual**: Verifique `activeContext.md` e `progress.md`
- **Padrões**: Acesse `rules/`
- **Tarefas**: Consulte `tasks/`

---

**Nota**: Este memory-bank é atualizado automaticamente conforme o projeto evolui. Mantenha a documentação sempre atualizada para facilitar o desenvolvimento e manutenção.
