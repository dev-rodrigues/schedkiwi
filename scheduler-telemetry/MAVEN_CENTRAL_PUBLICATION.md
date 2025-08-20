# Publicação no Maven Central

Este documento descreve o processo para publicar a biblioteca `scheduler-telemetry` no Maven Central.

## 📋 **Pré-requisitos**

### 1. Conta Sonatype OSSRH
- Criar conta em [https://issues.sonatype.org/](https://issues.sonatype.org/)
- Solicitar acesso ao grupo `com.schedkiwi`
- Aguardar aprovação (pode levar alguns dias)

### 2. GPG Key
```bash
# Gerar nova chave GPG
gpg --gen-key

# Listar chaves
gpg --list-keys

# Exportar chave pública
gpg --export -a "carlos.henrique.rodrigues@gmail.com" > public-key.asc

# Enviar chave para servidor GPG
gpg --keyserver keyserver.ubuntu.com --send-keys <KEY_ID>
```

### 3. Variáveis de Ambiente
```bash
export OSSRH_USERNAME="seu_usuario_sonatype"
export OSSRH_PASSWORD="sua_senha_sonatype"
export GPG_PASSPHRASE="sua_senha_gpg"
```

## 🚀 **Processo de Publicação**

### 1. Build e Testes
```bash
# Executar testes
mvn clean test

# Build completo
mvn clean package
```

### 2. Validação de Release
```bash
# Validar configuração
mvn clean verify -P release

# Verificar artefatos
ls -la target/
# Deve conter: .jar, -sources.jar, -javadoc.jar, .asc (assinados)
```

### 3. Deploy para Staging
```bash
# Deploy para repositório de staging
mvn clean deploy -P release
```

### 4. Release no Sonatype
1. Acessar [https://oss.sonatype.org/](https://oss.sonatype.org/)
2. Fazer login com suas credenciais
3. Ir para "Staging Repositories"
4. Selecionar o repositório de staging
5. Clicar em "Close" e depois "Release"

## 📦 **Artefatos Gerados**

- `scheduler-telemetry-1.0.0.jar` - JAR principal
- `scheduler-telemetry-1.0.0-sources.jar` - Código fonte
- `scheduler-telemetry-1.0.0-javadoc.jar` - Documentação JavaDoc
- `scheduler-telemetry-1.0.0.pom` - POM do projeto
- Todos os arquivos `.asc` - Assinaturas GPG

## 🔧 **Configurações Importantes**

### POM.xml
- ✅ Informações do projeto (nome, descrição, URL)
- ✅ Licença MIT
- ✅ Informações do desenvolvedor
- ✅ SCM (GitHub)
- ✅ Distribution Management (Sonatype OSSRH)

### Plugins Maven
- ✅ `maven-source-plugin` - Gera JAR de fontes
- ✅ `maven-javadoc-plugin` - Gera JavaDoc
- ✅ `maven-gpg-plugin` - Assina artefatos
- ✅ `maven-release-plugin` - Gerencia releases

## 🚨 **Troubleshooting**

### Erro de Assinatura GPG
```bash
# Verificar se GPG está funcionando
gpg --version

# Verificar se a chave está disponível
gpg --list-keys
```

### Erro de Credenciais
```bash
# Verificar variáveis de ambiente
echo $OSSRH_USERNAME
echo $OSSRH_PASSWORD
echo $GPG_PASSPHRASE
```

### Erro de Build
```bash
# Limpar e tentar novamente
mvn clean
mvn clean verify -P release
```

## 📚 **Referências**

- [Sonatype OSSRH Guide](https://central.sonatype.org/publish/publish-guide/)
- [Maven Central Publishing](https://maven.apache.org/guides/mini/guide-central-repository-upload.html)
- [GPG Setup](https://central.sonatype.org/publish/requirements/gpg/)

## ⏱️ **Timeline Esperada**

1. **Setup inicial**: 1-2 dias
2. **Aprovação Sonatype**: 3-7 dias
3. **Primeira publicação**: 1 dia
4. **Disponibilidade no Maven Central**: 2-4 horas após release

## 🎯 **Status Atual**

- ✅ POM configurado para Maven Central
- ✅ Plugins de assinatura configurados
- ✅ Configuração de distribuição
- 🔄 Aguardando aprovação Sonatype
- ❌ Primeira publicação pendente
