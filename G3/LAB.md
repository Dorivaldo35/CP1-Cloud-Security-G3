# 🧪 LAB - Grupo 3: Supply chain, SBOM e Kubernetes

## 🎯 O que vamos fazer

Uma aplicação moderna **não é feita só do código da empresa**: ela carrega
centenas de peças de terceiros (bibliotecas, pacotes do sistema) e roda em
servidores configurados por arquivos (Kubernetes). Se qualquer peça ou
configuração estiver errada, a aplicação fica vulnerável. Isso é
**segurança da cadeia de suprimentos (supply chain)**.

Neste lab você vai usar 3 ferramentas para encontrar esses problemas
**automaticamente**:

| Ferramenta | Categoria | O que faz |
|---|---|---|
| **Syft** | SCA | Gera o **SBOM**: a "lista de ingredientes" de uma aplicação |
| **Grype** | SCA | Confere cada ingrediente contra bancos de vulnerabilidades (CVEs) |
| **Kubescape** | IaC | Analisa arquivos de Kubernetes e aponta configurações inseguras |

**Alvos (vulneráveis de propósito):**
- A imagem Docker do **WebGoat**, aplicação de treino da OWASP
- O arquivo `k8s/deployment-inseguro.yaml`, criado com erros de propósito

**Tempo:** ~12 minutos

> ⚠️ Use as ferramentas **somente** contra os alvos deste repositório.
> Analisar sistemas de terceiros sem autorização é crime.

---

## 📋 Pré-requisitos
- **Docker Desktop** instalado e **aberto**
- **Git** instalado
- Um terminal (no Windows: Git Bash, PowerShell ou o terminal do VS Code)

> 💡 **Para ganhar tempo**, rode antes da aula (baixa as ferramentas):
> ```
> docker pull anchore/syft
> docker pull anchore/grype
> docker pull quay.io/kubescape/kubescape-cli
> ```

---

## PARTE 1 - Rodando as ferramentas no seu computador

### Passo 1 - Baixar o repositório (1 min)
```
git clone https://github.com/Lamataa/test.git
cd test
```
📁 **O que tem aqui:**
| Arquivo / pasta | Para que serve |
|---|---|
| `docker-compose.yml` | Liga cada ferramenta com um comando, sem instalar nada |
| `k8s/` | YAML de Kubernetes **inseguro** |
| `k8s-corrigido/` | O mesmo YAML **corrigido** |
| `grype-excecoes.yaml` | Exceções documentadas do Grype (aceite de risco) |
| `.github/workflows/security.yml` | O pipeline que roda tudo sozinho no GitHub |
| `relatorios/` | Resultados das ferramentas |

### Passo 2 - Gerar o SBOM com o Syft (2 min)
```
docker compose run --rm syft
```
**O que acontece:** o Syft abre a imagem do WebGoat e lista **tudo** o que
tem dentro dela.

**Resultado esperado:** uma tabela com `NAME`, `VERSION` e `TYPE`, com cerca de
**325 pacotes**.
- `java-archive` = bibliotecas Java usadas pelo WebGoat
- `deb` = pacotes do sistema operacional (Ubuntu) da imagem

👉 **Procure a linha `xstream 1.4.5`.** Guarde esse nome.

### Passo 3 - Procurar vulnerabilidades com o Grype (3 min)
```
docker compose run --rm grype
```
**O que acontece:** o Grype pega a lista de ingredientes e compara cada um com
bancos públicos de vulnerabilidades.

**Resultado esperado:** um resumo no topo (`by severity: X critical, Y high...`)
e uma tabela.

**Como ler a tabela:**
| Coluna | Significado |
|---|---|
| `INSTALLED` | Versão que está no WebGoat |
| `FIXED IN` | Versão que corrige a falha |
| `SEVERITY` | Gravidade: Critical, High, Medium, Low |
| `EPSS` | Chance de a falha ser explorada nos próximos 30 dias |
| `KEV` | A falha já está sendo explorada por atacantes no mundo real |

👉 Olha lá o `xstream 1.4.5` no topo, com EPSS de ~98% e marcado como KEV.

### Passo 4 - Analisar o Kubernetes com o Kubescape (2 min)
Primeiro, **abra** o arquivo `k8s/deployment-inseguro.yaml` e veja:
- `privileged: true` → o container tem poder total sobre o servidor
- `runAsUser: 0` → roda como **root**
- `hostPath: path: /` → acessa o **disco inteiro** do servidor
- `DB_PASSWORD: "SenhaSuperSecreta123"` → senha escrita no arquivo

Agora rode:
```
docker compose run --rm kubescape
```
**Resultado esperado:** uma tabela com os **controles que falharam** e, no
final, o `compliance-score`.

### Passo 5 - Comparar com a versão corrigida (1 min)
```
docker compose run --rm kubescape scan framework nsa k8s-corrigido/
```
**Resultado esperado:** **nenhum controle grave falhando**. Compare os dois
YAMLs lado a lado e veja o que mudou.

---

## PARTE 2 - O pipeline: vermelho ❌ → verde ✅ (demonstração do grupo, 3 min)

**Pipeline** é uma automação: a cada `git push`, o GitHub roda o Grype e o
Kubescape sozinho. Se encontrar algo **HIGH ou CRITICAL**, o build **falha**
e o código inseguro é barrado **antes** de chegar em produção
(isso é o *shift-left*).

No topo do `.github/workflows/security.yml` existe **uma chave**:
```yaml
env:
  MODO: vulneravel
```

### 🔴 Demonstração 1 - `MODO: vulneravel`
- O Kubescape analisa o `k8s/` (inseguro)
- O Grype roda **sem exceções**
- **Resultado:** ❌ os dois jobs ficam **vermelhos**. O pipeline **bloqueou** o deploy.

### 🟢 Demonstração 2 - `MODO: corrigido`
- O Kubescape analisa o `k8s-corrigido/`, onde as configurações foram **corrigidas**
- O Grype aplica o `grype-excecoes.yaml`, com as **exceções documentadas**
- **Resultado:** ✅ os dois jobs ficam **verdes**

> ⚠️ **Atenção à diferença:**
> - O Kubescape ficou verde porque o YAML foi **CORRIGIDO**.
> - O Grype ficou verde porque o risco foi **ACEITO e DOCUMENTADO**: as bibliotecas
>   vulneráveis pertencem ao WebGoat (vulnerável de propósito), e não ao nosso código.
>   Em um projeto real, a correção seria atualizar `xstream` para ≥ 1.4.21 e
>   `tomcat-embed-core` para ≥ 11.0.25.
>
> **Suprimir não é corrigir.**

👉 Veja o histórico na aba **Actions** do repositório: os builds vermelhos e verdes.

> 💡 **Quer testar você mesmo?** Faça um *fork* do repositório, troque o `MODO`,
> faça commit e veja o resultado na aba Actions do seu fork.

---

## ❓ Perguntas de verificação
Entregue as respostas com o **print** do seu terminal:

1. No **Passo 3**, quantas vulnerabilidades **CRITICAL** o Grype encontrou?
   Cite **uma** biblioteca afetada e a versão que corrige (coluna `FIXED IN`).
2. No **Passo 4**, cite **um controle que falhou** no Kubescape e **qual linha**
   do `k8s/deployment-inseguro.yaml` causou essa falha.

---

## 🆘 Deu erro?
| Problema | Solução |
|---|---|
| `Cannot connect to the Docker daemon` | Abra o Docker Desktop e espere ficar pronto |
| `no configuration file provided` | Você não está na pasta `test`: rode `cd test` |
| Download muito lento | Acompanhe pelo **vídeo backup** (link no README) |