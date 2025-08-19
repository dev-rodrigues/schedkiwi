# Operational Exec Rules (Cursor)

## Objetivo
Permitir que o Cursor:
- Busque arquivos/conteúdos no repositório.
- Crie/edite arquivos.
- Execute suites de teste.
- Execute comandos Git seguros.
- Suba/pare a aplicação.
- Execute comandos Maven.
- Rode comandos Linux **não destrutivos**.

> **Princípio de segurança:** Sempre **planejar → pré-visualizar → executar**. Mostrar o comando exato, diretório de trabalho e efeito esperado **antes** da execução.

---

## Regras de Segurança (obrigatórias)
1. **Dry-run / Preview:** Antes de executar, apresentar uma seção **`Plan`** com:
    - `cwd` pretendido.
    - Comandos na ordem exata (um por linha).
    - Arquivos que serão criados/modificados.
2. **Escopo de diretório:** Executar somente **dentro do repositório atual**. Nunca usar `sudo`.
3. **Sem comandos destrutivos:** **PROIBIDO**:
    - `rm -r`, `rm -rf`, `sudo`, `mkfs*`, `:(){ :|:& };:`, `dd if=/dev/*`, `chmod -R 777`, `chown -R /`, `kill -9 -1`.
    - `docker system prune -a`, alterações com `iptables`/`ufw`, `shutdown`/`reboot`.
4. **Edição segura de arquivos:** Ao editar, gerar **diff** e salvar com encoding UTF-8. Para substituições em massa, usar ferramentas seguras (`sd`, `ripgrep + rewrite`, `perl -pe` com backup).
5. **Variáveis e credenciais:** Nunca imprimir segredos em logs. Preferir `.env` ou Secrets.

---

## Buscas no Repositório
Comandos permitidos (somente leitura):
- `fd <pattern> [--type f] [--hidden]` (preferível ao `find`).
- `rg "<regex>" <paths> -n --hidden --glob '!.git'`.
- `git grep -n "<regex>"`.

**Exemplo (Plan):**
cwd: .
Comandos:

fd service --type f

rg "TODO|FIXME" src -n --glob '!.git'


---

## Criação/Edição de Arquivos
- Criação de diretórios: `mkdir -p <path>`.
- Criação de arquivos: `tee <file> > /dev/null <<'EOF' ... EOF`.
- Edição leve: `ed`, `perl -0777 -pe`, `sd` (se disponível).  
  Evitar `sed -i` cross-platform; se usar, criar backup `-i.bak`.

**Exemplo (criando doc):**
mkdir -p docs/architecture
tee docs/architecture/payment-service-architecture.md > /dev/null <<'EOF'
Payment Service Architecture
...
EOF

---

## Execução de Testes
**Java/Kotlin (Maven):**
- `mvn -q -e -DskipTests=false test`.
- `mvn -q -e -Pci verify`.
- Teste único: `mvn -q -Dtest=ContaServiceTest test`.

**Node/TS (se aplicável):**
- `npm test` ou `pnpm test`.

**Relatos:**
- Sempre devolver resumo: total, pass, fail, duração e caminho de reports (ex.: `target/surefire-reports`).

---

## Git (somente operações seguras)
Permitido:
- Status/inspeção: `git status`, `git diff`, `git log --oneline --decorate -n 20`.
- Branch/checkout: `git switch -c feature/<slug>` | `git switch <branch>`.
- Stage/commit: `git add -p` | `git add <paths>` | `git commit -m "feat(scope): msg"`.
- Pull/merge rebase: `git fetch --all --prune` | `git pull --rebase`.
- Push seguro: `git push -u origin <branch>`.

**Não permitido:** `git reset --hard`, `git clean -fdx`, `git push --force` (exceto se explicitamente solicitado e justificado).

---

## Maven (build, run, utilitários)
Comandos permitidos:
- Build: `mvn -q -e clean package -DskipTests`.
- Qualidade: `mvn -q -e spotbugs:check` (se configurado).
- Execução Spring Boot: `mvn -q -e spring-boot:run -Dspring-boot.run.profiles=dev`.
- Dependências: `mvn -q -e dependency:tree -Dincludes=<groupId>:<artifactId>`.

**Observações:**
- Sempre informar perfil (`-Pdev`, `-Pci`) quando relevante.
- Se usar JDK toolchains, verificar `~/.m2/toolchains.xml`.

---

## Executar e Parar a Aplicação
**Java Jar direto:**
- Run: `java -jar target/app.jar --spring.profiles.active=dev`.
- Stop: localizar PID por porta e finalizar com segurança:
    - `lsof -i :8080 -t | xargs -r kill`.

**Spring Boot Maven:**
- Run: `mvn spring-boot:run -Dspring-boot.run.profiles=dev`.
- Stop: `Ctrl+C` no terminal controlado pelo Cursor.

**PM2 (se adotado):**
- Start: `pm2 start "java -jar target/app.jar --spring.profiles.active=prd" --name app`.
- Stop: `pm2 stop app`.
- Restart: `pm2 restart app`.
- Logs: `pm2 logs app --lines 200`.

> **Regra:** Ao iniciar, imprimir URL base, porta e healthcheck se disponível.

---

## Comandos Linux Permitidos (não críticos)
- Navegação/consulta: `pwd`, `ls -la`, `tree -L 2`, `du -sh <path>`, `df -h`.
- Visualização: `cat`, `head`, `tail -n 200`, `less`.
- Arquivos/diretórios: `mkdir -p`, `cp -n`, `mv`, `install -D`.
- Texto/grep: `rg`, `grep -n`, `awk`, `cut`, `sort`, `uniq`, `tr`.
- Compactação: `tar -czf <archive.tgz> <paths>` | `tar -xzf`.
- Rede leve: `curl -I <url>`, `curl -s <url>`, `nc -zv host port`.
- Processos: `ps aux | rg <pattern>`, `lsof -i :<port>`.
- Hashes/checksum: `shasum -a 256 <file>`.
- Utilitários seguros: `jq`, `yq`, `envsubst`.

**Bloqueados (exemplos):**
- `rm -r`, `rm -rf`, `sudo`, `mount`, `umount`, `fusermount`, `iptables`, `ufw`, `systemctl` (fora de dev local), `kill -9` indiscriminado, `dd`, `mkfs*`, `parted`, `diskutil` (mac) em operações de disco.

---

## Fluxo Padrão de Execução (sempre seguir)
1. **Plan:** Mostrar lista de comandos e `cwd`.
2. **Check:** Validar que nenhum comando está na **blocklist** e que caminhos estão sob o repo.
3. **Execute:** Rodar comandos **na ordem**, parando no primeiro erro e retornando o log da etapa falha.
4. **Report:** Resumo final (saída, artefatos, porta do serviço e logs).

---

## Exemplos de Playbooks

### A) Rodar testes e build rápido
Plan:
mvn -q -e test
mvn -q -e clean package -DskipTests


### B) Criar doc e linkar no README
Plan:
mkdir -p docs
tee docs/runtime-troubleshooting.md <<'EOF' ... EOF
rg -n "## Documentation" README.md || tee -a README.md <<'EOF' ... EOF


### C) Subir app em dev e validar porta
Plan:
mvn -q -e spring-boot:run -Dspring-boot.run.profiles=dev
nc -zv localhost 8080
curl -s http://localhost:8080/actuator/health || true


---

## Commits (Conventional)
- Usar `feat|fix|docs|refactor|test|chore|perf|ci`.
- Mensagem curta (≤72 chars) + corpo explicativo se necessário.

**Exemplos:**
- `docs: add runtime troubleshooting guide`.
- `test: cover optimistic locking on ContaService`.

---

## Observabilidade e Logs (quando aplicável)
- Ao subir app, imprimir último trecho do log (`tail -n 200`) e indicar:
    - Porta exposta.
    - Endpoint de health.
    - Perfis ativos.

---

## Falhas e Rollback
- Em erro de build/test, retornar:
    - Comando que falhou.
    - Código de saída.
    - Trecho relevante do log.
- Nunca executar `git reset --hard` automaticamente; sugerir correção.