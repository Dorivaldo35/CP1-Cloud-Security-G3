# Análise de 3 achados: Grupo 3

Escolhemos um achado de cada ferramenta usada. Os três foram confirmados nos relatórios da pasta `relatorios/`.

| # | Ferramenta | O que achou | CWE | É real? |
|---|---|---|---|---|
| 1 | Grype (SCA) | Biblioteca `xstream 1.4.5` vulnerável | CWE-502 | Sim |
| 2 | Kubescape (IaC) | Container rodando em modo privilegiado | CWE-250 | Sim |
| 3 | SpotBugs + FindSecBugs (SAST) | SQL Injection no código Java | CWE-89 | Sim |

---

## 1. Grype: `xstream 1.4.5`

**Onde:** imagem do WebGoat · `relatorios/grype.json` · ID `GHSA-j9h8-phrw-h4fh` (CVE-2021-39144)

Essa foi a vulnerabilidade mais perigosa que encontramos. O Grype deu **EPSS de 98%** (chance altíssima de ser explorada) e marcou como **KEV**, ou seja, atacantes já a exploram no mundo real. O xstream converte XML em objetos Java, e nessa versão antiga um XML malicioso pode fazer o servidor **executar código do atacante**.

Também percebemos que essa única biblioteca concentra **22 das 26** vulnerabilidades HIGH/CRITICAL da imagem.

- **É real?** Sim. A versão aparece tanto no SBOM do Syft quanto no Grype. O WebGoat a inclui de propósito, para a lição de desserialização.
- **CWE:** CWE-502, desserialização de dados não confiáveis.
- **Como corrigir:** atualizar para o xstream **1.4.21** ou mais novo.
- **O que fizemos:** como a biblioteca é do WebGoat e não nossa, registramos uma **exceção documentada** (`grype-excecoes.yaml`). Mas deixamos claro: **suprimir não é corrigir**. Em um projeto real, o certo seria atualizar.

---

## 2. Kubescape: container privilegiado

**Onde:** `k8s/deployment-inseguro.yaml`, linha `privileged: true` · controle **C-0057** · `relatorios/kubescape.json`

Um container privilegiado tem praticamente o mesmo poder que o administrador do servidor. No nosso YAML isso ficou ainda pior, porque ele também roda como **root** (`runAsUser: 0`) e tem acesso ao **disco inteiro** da máquina (`hostPath: /`). Se alguém invadir a aplicação, sai do container e controla o servidor.

- **É real?** Sim. A configuração está no arquivo e é perigosa de verdade.
- **CWE:** CWE-250, execução com privilégios desnecessários.
- **Como corrigimos** (em `k8s-corrigido/`): `privileged: false`, rodar com usuário comum (`runAsNonRoot: true`), bloquear escalada de privilégio e remover o acesso ao disco do servidor.
- **Resultado:** as falhas caíram de **~30 para 0** e o pipeline ficou verde.

---

## 3. SpotBugs + FindSecBugs: SQL Injection

**Onde:** `app/src/main/java/br/edu/grupo3/Vulneravel.java`, método `buscarUsuario()` · regra **SQL_INJECTION_JDBC** · `relatorios/spotbugs.xml`

```java
st.executeQuery("SELECT * FROM usuarios WHERE nome = '" + nome + "'");
```

O nome digitado pelo usuário entra direto na consulta ao banco. Se alguém digitar `' OR '1'='1`, a consulta devolve **todos os usuários**.

- **É real?** Sim. Plantamos essa falha de propósito, e o SpotBugs a encontrou por duas regras diferentes.
- **CWE:** CWE-89, SQL Injection.
- **Como corrigir:** usar consulta parametrizada.
  ```java
  PreparedStatement ps = c.prepareStatement("SELECT * FROM usuarios WHERE nome = ?");
  ps.setString(1, nome);
  ```
- **Curiosidade:** o Wapiti (DAST) **não achou nenhuma** SQL injection no WebGoat, porque ficou preso na tela de login. O SAST encontra a falha lendo o código, sem precisar da aplicação no ar. Uma ferramenta cobre o ponto cego da outra.

---

## Falsos positivos e ruído que observamos

- **Kubescape:** mesmo com o YAML corrigido, os controles C-0211 e C-0237 continuaram falhando. A própria ferramenta avisou *"Control configurations are empty"*, ou seja, o problema era falta de configuração, não o nosso arquivo. **Falso positivo.** Resolvemos usando o framework NSA-CISA no pipeline.
- **Grype:** dezenas de alertas repetidos do `binutils`, uma ferramenta do Ubuntu que o WebGoat nem usa para rodar. Muitos vêm marcados como *"won't fix"*. É mais ruído do que risco.
- **SpotBugs:** foram 12 alertas para 5 falhas. Algumas apareceram duplicadas (SpotBugs + FindSecBugs), e 4 eram de qualidade de código, não de segurança. Mesmo assim, ele pegou **as 5 falhas que plantamos (5/5)**.
- **Wapiti:** reclamou de falta de HTTPS em tudo, o que é esperado num ambiente local de teste.
