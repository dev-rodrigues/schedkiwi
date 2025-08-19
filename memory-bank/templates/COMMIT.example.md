# Commit Template (Conventional Commit)

## Tipo do Commit
(qual tipo descreve melhor sua mudança?)
- feat: nova funcionalidade
- fix: correção de bug
- docs: apenas documentação
- style: formatação, sem impacto em código
- refactor: refatoração sem alterar comportamento externo
- test: inclusão/alteração de testes
- chore: manutenção, build, configs, dependências
- perf: melhoria de performance
- ci: alterações de pipeline

> Exemplo: **feat**

---

## Escopo
(parte do projeto afetada, opcional mas recomendado)
- Ex.: service, api, auth, infra, ci, ui

> Exemplo: **service**

---

## Mensagem Curta
(frase objetiva, ≤72 caracteres, presente do indicativo)
- Descreva **o que** mudou, não o porquê.
- Não use ponto final.

> Exemplo: **adiciona validação antes do register**

---

## Corpo (opcional)
- Explique **o porquê** da mudança, contexto ou detalhes técnicos.
- Liste impactos, riscos ou decisões importantes.
- Pode usar bullet points.

---

## Footer (opcional)
- Referências a issues, tickets, breaking changes.
- Exemplo:
  - `BREAKING CHANGE: altera formato de resposta da API`
  - `Closes #123`
