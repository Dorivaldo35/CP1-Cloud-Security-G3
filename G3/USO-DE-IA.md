# Declaração de uso de IA

| Ferramenta | Onde foi usada | O que o grupo fez |
|---|---|---|
| Claude (Anthropic) | Explicação dos conceitos; sugestão da estrutura do repositório; apoio na escrita do `docker-compose.yml`, do pipeline, dos manifestos Kubernetes, do código Java vulnerável, do `LAB.md` e da análise dos achados; apoio na redação do documento e dos slides | Executamos todas as ferramentas; interpretamos e conferimos os resultados; corrigimos os erros encontrados durante a execução |

## Como validamos
- Todas as ferramentas foram executadas pelo grupo, localmente (Docker) e no pipeline (GitHub Actions).
- Os números citados no documento e nos slides foram conferidos nos relatórios da pasta `relatorios/` e nos prints da pasta `docs/`.
- Os dados de identificação das ferramentas (licença, versão, mantenedor) foram conferidos nos repositórios oficiais.
- Corrigimos problemas que apareceram na prática, como o nome do arquivo de exceções do Grype e os controles do Kubescape que falhavam por falta de configuração.
