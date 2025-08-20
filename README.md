# SchedKiwi

Repositório principal contendo projetos relacionados à telemetria e agendamento de tarefas.

## 📁 Estrutura do Repositório

Este repositório contém dois projetos Maven principais:

### 🚀 scheduler-telemetry
Biblioteca Kotlin para telemetria de agendadores, fornecendo:
- Monitoramento de execução de tarefas agendadas
- Coleta de métricas de performance
- Rastreamento de progresso e sequências
- Integração com Spring Boot via auto-configuration

**Localização:** `scheduler-telemetry/`

### 🌐 central-telemetry-api
API REST centralizada para coleta e consulta de telemetria, incluindo:
- Registro de aplicações
- Relatórios de execução
- Atualizações de progresso
- Mensagens de sequência
- Consultas de status e sincronização

**Localização:** `central-telemetry-api/`

## 🛠️ Tecnologias

- **Kotlin** - Linguagem principal
- **Spring Boot** - Framework para APIs e aplicações
- **Maven** - Gerenciamento de dependências e build
- **JPA/Hibernate** - Persistência de dados
- **Flyway** - Migrações de banco de dados

## 🚀 Como Executar

### Pré-requisitos
- Java 17+
- Maven 3.8+
- Kotlin 1.8+

### scheduler-telemetry
```bash
cd scheduler-telemetry
mvn clean install
```

### central-telemetry-api
```bash
cd central-telemetry-api
mvn spring-boot:run
```

## 📚 Documentação

- **API Reference:** `central-telemetry-api/docs/api-reference.md`
- **Exemplos:** `central-telemetry-api/docs/examples.md`
- **Troubleshooting:** `central-telemetry-api/docs/troubleshooting.md`

## 🔧 Desenvolvimento

### Estrutura de Branches
- `main` - Branch principal (produção)
- `develop` - Branch de desenvolvimento
- `feature/*` - Novas funcionalidades
- `hotfix/*` - Correções urgentes

### Padrões de Commit
Seguimos [Conventional Commits](https://www.conventionalcommits.org/):
- `feat:` - Nova funcionalidade
- `fix:` - Correção de bug
- `docs:` - Documentação
- `refactor:` - Refatoração
- `test:` - Testes
- `chore:` - Tarefas de manutenção

## 📊 Status do Projeto

- ✅ **scheduler-telemetry** - Biblioteca funcional
- ✅ **central-telemetry-api** - API implementada
- 🔄 **Integração** - Em desenvolvimento

## 🤝 Contribuição

1. Fork o repositório
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo `LICENSE` para mais detalhes.

## 📞 Contato

- **GitHub:** [@dev-rodrigues](https://github.com/dev-rodrigues)
- **Repositório:** [schedkiwi](https://github.com/dev-rodrigues/schedkiwi)
