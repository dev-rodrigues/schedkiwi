# Documentação

## Localização
- Toda documentação deve estar no diretório `/docs` na raiz do repositório.
- Subpastas são permitidas para organização por assunto, mas **não** devem conter arquivos fora de `/docs`.

## Formato de Arquivos
- Preferencialmente no formato **Markdown (.md)**.
- Nome de arquivo em inglês, lowercase e com `-` como separador (ex.: `payment-service-architecture.md`).
- O título do documento (linha 1) deve ser um `# H1` claro e coerente com o nome do arquivo.
- Estrutura sugerida:
    1. **Título** (`#`)
    2. **Resumo/Objetivo** (explicação breve do propósito do documento)
    3. **Contexto** (cenário ou motivação)
    4. **Detalhamento** (arquitetura, fluxos, APIs, diagramas, etc.)
    5. **Exemplos** (quando aplicável, código, comandos, payloads)
    6. **Referências** (links internos ou externos)

## Boas Práticas
- Sempre iniciar com um resumo de **o que é** e **para que serve** o documento.
- Diagramas devem ser inseridos usando [Mermaid](https://mermaid.js.org/) ou imagens salvas no próprio repositório (em `/docs/assets`).
- Se for documentação de API, seguir o padrão OpenAPI/Swagger ou tabelas Markdown para endpoints.
- Ao atualizar um documento, incluir uma seção `## Histórico de Alterações` no final, com data, autor e descrição da modificação.
- Não duplicar conteúdo: se houver um documento relacionado, referenciar usando links relativos (`[ver outro doc](../outro-doc.md)`).

## Estilo de Escrita
- Linguagem clara, objetiva e técnica.
- Evitar termos ambíguos ou subjetivos.
- Quando possível, incluir exemplos de uso, fluxos ou pseudo-código.
- Usar listas, tabelas e subtítulos para facilitar a leitura.

## Validação
- Documentos devem ser revisados em PR como qualquer outro código.
- Revisão deve garantir:
    - Local correto (`/docs`).
    - Estrutura mínima respeitada.
    - Ortografia e gramática corretas.
    - Links funcionando.
