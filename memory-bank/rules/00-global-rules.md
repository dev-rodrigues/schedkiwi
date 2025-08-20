# Global Rules (Cursor)

## Objetivo
Manter consistência de arquitetura, código, testes e operações em todos os serviços e apps.

## Como o Cursor deve agir
- Sempre cite caminhos de arquivos exatos quando sugerir alterações.
- Antes de propor refactor, liste impacto e riscos.
- Prefira código explícito e log detalhado (o usuário **prefere logging detalhado**).
- Sugira diffs minimalistas e commit por escopo.
- Quando houver dúvida de contexto, infira a melhor prática e siga os padrões deste diretório.

## Padrões de Projeto
- Arquitetura Hexagonal (quando aplicável).
- Separar domínio de infraestrutura: **domain** ≠ **persistence/http/ui**.
- Camadas: `domain` → `application` → `infrastructure`.
- Evitar acoplamento de entidades de domínio com mapeamentos de banco.

## Estrutura do Projeto
- **Biblioteca**: `scheduler-telemetry/` (pasta na raiz) - Código fonte da biblioteca Maven
- **API Central**: `central-telemetry-api/` (pasta na raiz) - Código do gerenciador central
- **Documentação**: `memory-bank/` - Contexto, regras e documentação do projeto
- **Raiz**: Projeto com duas pastas principais para código fonte

## Qualidade
- Lint e format automáticos no pre-commit.
- Cobertura mínima: 80% em módulos críticos.
- PRs exigem checklist e link de issue/tarefa.
