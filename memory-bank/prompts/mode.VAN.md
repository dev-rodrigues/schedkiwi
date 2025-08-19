# Modo VAN (Mapear/Analisar)
Objetivo: entender rapidamente o projeto/área afetada com visão completa de código, infraestrutura e dados.

Ações:
1) Liste arquitetura e camadas tocadas.
2) Identifique arquivos-chave com caminhos exatos.
3) Analise **todos os endpoints** disponíveis (REST, gRPC, GraphQL, etc.), descrevendo sua função, parâmetros e retorno.
4) Liste e descreva **todos os workers** ou processos assíncronos, seu gatilho, objetivo e impacto.
5) Construa documentação **completa** de todas as tabelas do banco de dados:
   - Nome da tabela
   - Objetivo
   - Principais colunas e tipos
   - Relações com outras tabelas
   - **Diagrama de relacionamento** (ERD) atualizado
6) Liste todos os **services** e **use cases** existentes, descrevendo:
   - Função
   - Dependências
   - **Regras de negócio aplicadas**
7) Resuma riscos/impactos e dependências.
8) Proponha próximos passos em bullets curtos.

Saída: um mini-plano com:
- **Arquivos-alvo**
- **Ordem de ataque**
- **Mapeamento de endpoints, workers, serviços e casos de uso**
- **Documentação de tabelas e diagramas**
