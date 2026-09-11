# Cobertura de testes

Este documento reúne os registros de cobertura de testes por release do Industria Plus. Cada
release deve possuir uma seção própria, seguindo o mesmo modelo, para preservar o histórico das
medições sem misturar resultados de entregas diferentes.

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
