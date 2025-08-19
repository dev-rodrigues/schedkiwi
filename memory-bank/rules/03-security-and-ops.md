# Security & Ops

- Jamais commitar segredos (use Secrets/ConfigMap ou env).
- Validar inputs em bordas (controllers/adapters).
- Logs nunca devem conter dados sensíveis.
- Healthchecks e readiness (quando em K8s).
- Observabilidade: logs estruturados + (se disponível) métricas/trace.
