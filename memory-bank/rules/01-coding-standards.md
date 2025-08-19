# Coding Standards

## Kotlin/Java (Spring)
- Null-safety explícita; evitar `!!`.
- Services estendem `BaseService` (padrão do projeto).
- Validações antes de `register()` (regra de fluxo).
- Repositórios Spring Data JPA no pacote `...infrastructure.persistence`.
- DTOs ≠ Entities; use mappers.
- Logs: use `INFO` para fluxo, `DEBUG` para diagnósticos, `ERROR` com stacktrace + correlationId.

## TypeScript/Node
- `strict` habilitado; evitar `any`.
- Config via `convict`; DI via `tsyringe`; Redis via `ioredis`.
- Port/Adapter pattern (ports em `domain/ports`, adapters em `infrastructure`).
- Tratamento centralizado de erros (middleware).
- Padronize respostas e códigos HTTP.

## Frontend (React/React Native)
- Componentes puros + hooks; evitar lógica pesada no JSX.
- Design system (ShadCN ou NativeBase) e tokens de cor padronizados.
- Acessibilidade: `aria-*` e foco navegável.

## Estilo
- Use `.editorconfig` e Prettier/ESLint (TS) e ktlint/detekt (Kotlin).
