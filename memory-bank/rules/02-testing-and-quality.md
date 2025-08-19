# Testing & Quality

## Estratégia
- Unit > Integration > E2E.
- Testes de concorrência quando houver transações financeiras.
- MockK (Kotlin) e Jest (TS). Cobrir cenários de `ObjectOptimisticLockingFailureException` quando aplicável.

## Regras
- Cada bugfix precisa de teste que reproduza o erro.
- Testes devem nomear o comportamento (Given/When/Then).
- Snapshots só quando estável (sem datas/ids voláteis).

## Métricas Mínimas
- Cobertura 80% em módulos core.
- Linters sem erros em PR.
