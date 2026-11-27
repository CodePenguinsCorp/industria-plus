# Cobertura de testes

As novas execuções por branch, em 27/11/2026, estão nos arquivos erificacao-*.json e na [revisão de PO/QA](revisao-po-qa.md). Os registros abaixo preservam suas datas originais.

Este documento reúne os registros de cobertura de testes por release do Industria Plus. Cada
release deve possuir uma seção própria, seguindo o mesmo modelo, para preservar o histórico das
medições sem misturar resultados de entregas diferentes.

## Retomada N3 — 24/11/2026

Estado local em `feature/n3-finalizacao`, baseado em `96e5d4d` e no ZIP recebido pelo Teams.

| Verificação | Executados | Aprovados | Falhas/erros | Ignorados |
| --- | ---: | ---: | ---: | ---: |
| Backend, Maven/JUnit, H2 | 99 | 99 | 0 | 0 |
| Mesma suíte backend em MySQL 8.4 | 99 | 99 | 0 | 0 |
| Migração N1 até V5 em MySQL, execução dedicada | 1 | 1 | 0 | 0 |
| Frontend, Angular/Vitest | 38 | 38 | 0 | 0 |

Os testes recebidos de normalização de usuário detectaram uma regressão de e-mail, corrigida antes
da execução final. Foram adicionados um teste de interface para `updatedAt` e dois testes de
integração para o bootstrap do administrador. Os cenários novos foram observados falhando antes
das correções correspondentes.

Formatação e build do frontend, configurações Compose e geradores de ambiente de produção também
foram verificados. Não foram recalculados percentuais de cobertura de linhas, requisitos ou aceite.
MTBF e preventivas também foram verificados no navegador, e migração, concorrência e persistência
foram verificadas em MySQL. O aceite do PO/QA segue pendente. A suíte backend aparece duas vezes
por ter sido executada em dois bancos; não são 198 cenários distintos. Consulte o [relatório da Etapa 3](relatorio-etapa-3.md) e o
[resumo das evidências](evidencias-n3.json).

## Registro da Release N1

```text
Release: N1
Data: 11/09/2026
Responsável: Lucas Mönich Nunes

Testes executados: 24
Testes aprovados: 24
Testes reprovados: 0
Testes pendentes: 0

Cobertura de requisitos: 85,71%
Cobertura de aceite: 45,10%

Bugs críticos: 0
Bugs altos: 0
Bugs médios: 0
Bugs baixos: 0
Bugs reabertos: 0

Observações:
Foram executados testes automatizados no frontend e no backend.
No frontend, foram executados 11 testes, distribuídos em 4 arquivos de teste,
com 100% de aprovação. No backend, foram executados 13 testes, também com
100% de aprovação.

Ao todo, foram executados 24 testes, sem falhas, erros ou testes pendentes.
Os testes do frontend validaram, entre outros pontos, os campos críticos
antes da chamada à API.
```

### Memória de cálculo da Release N1

A cobertura de requisitos considera 24 dos 28 requisitos da N1 com testes automatizados
associados:

```text
(24 requisitos cobertos / 28 requisitos da N1) × 100 = 85,71%
```

Os testes cobrem `RF01` a `RF18` e `RNF01`, `RNF02`, `RNF04`, `RNF05`, `RNF06` e `RNF07`.
Os requisitos `RNF03`, `RNF08`, `RNF09` e `RNF10` dependem de validações de persistência,
infraestrutura ou revisão estrutural que não estão representadas pelos 24 testes automatizados
informados.

A cobertura de aceite considera 23 dos 51 cenários previstos para a N1 com correspondência completa
nos testes automatizados executados:

```text
(23 cenários cobertos / 51 cenários previstos) × 100 = 45,10%
```

| Grupo | Cenários cobertos | Total de cenários |
| --- | ---: | ---: |
| `CA-C` — cadastros e abertura de chamados | 5 | 9 |
| `CA-F` — consulta e ciclo de vida | 2 | 4 |
| `CA-R1` — limite de alta urgência | 6 | 8 |
| `CA-R2` — validação de textos | 2 | 5 |
| `CA-R3` — atribuição de técnico | 2 | 6 |
| `CA-R4` — repetição da atribuição | 1 | 4 |
| `CA-R5` — ordenação da fila | 3 | 5 |
| `CA-Q` — requisitos não funcionais | 2 | 10 |
| **Total** | **23** | **51** |

Os 28 cenários restantes não são testes automatizados pendentes. Eles permanecem como cenários de
aceite sem execução comprovada nesta medição e devem ser registrados quando forem validados.

As fórmulas seguem o documento de
[métricas de qualidade e cobertura de testes](metricas-qualidade.md), e os cenários estão definidos
na [premissa inicial e critérios de aceite da N1](../produto/premissa-inicial%20N1.md).
