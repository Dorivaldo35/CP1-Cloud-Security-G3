# DevSecOps - Grupo 3: Supply chain, SBOM e Kubernetes

Check Point 02 · DevSecOps

## Integrantes
| Nome | RM | Papel |
|---|---|---|
| Dorivaldo | ______ | Pipeline (GitHub Actions) |
| Gabriel Lamata | ______ | Líder técnico (repositório e Docker) |
| Luiz Parpinelli | ______ | Documentação do repositório |
| Nickolas | ______ | Relatórios e evidências |

## Ferramentas
| Categoria | Ferramenta | Onde está |
|---|---|---|
| SAST | SpotBugs + FindSecBugs | `app/` e `relatorios/spotbugs.xml` |
| SCA | Syft + Grype | **LAB + pipeline**: `relatorios/sbom.json` e `relatorios/grype.json` |
| IaC | Kubescape | **LAB + pipeline**: `relatorios/kubescape.json` |
| DAST | Wapiti | `relatorios/wapiti/` |

**Alvos:** imagem Docker do OWASP WebGoat, manifesto Kubernetes propositalmente inseguro (`k8s/`) e código Java com falhas conhecidas (`app/`).

## 🧪 Laboratório
Siga o **[LAB.md](LAB.md)**.

Vídeo backup: ______

## Estrutura
```
LAB.md                  passo a passo para a turma
ANALISE-ACHADOS.md      3 achados (real/falso positivo, CWE, correção)
docker-compose.yml      roda todas as ferramentas sem instalar nada
grype-excecoes.yaml     exceções documentadas do Grype (aceite de risco)
.github/workflows/      pipeline que falha em HIGH/CRITICAL
k8s/                    manifesto Kubernetes inseguro
k8s-corrigido/          manifesto corrigido
app/                    código Java vulnerável (alvo do SpotBugs)
relatorios/             saídas das 4 ferramentas
docs/                   prints das execuções
USO-DE-IA.md            declaração de uso de IA
```

## Como rodar cada ferramenta
```
docker compose run --rm syft        # gera o SBOM
docker compose run --rm grype       # vulnerabilidades (lê o SBOM do Syft)
docker compose run --rm kubescape   # manifesto Kubernetes
docker compose run --rm spotbugs    # código Java
docker compose up -d webgoat        # liga o alvo (espere ~1 min)
docker compose run --rm wapiti      # DAST contra o WebGoat LOCAL
docker compose down                 # desliga tudo
```

## Pipeline
Em `.github/workflows/security.yml`, a variável `MODO` controla a demonstração:
- `vulneravel`: analisa `k8s/` sem exceções, e os jobs ficam ❌ vermelhos
- `corrigido`: analisa `k8s-corrigido/` com as exceções documentadas, e os jobs ficam ✅ verdes

> ⚠️ Todas as ferramentas foram executadas **somente** contra alvos locais e autorizados.
