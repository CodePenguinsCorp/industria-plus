# Métricas de qualidade e cobertura de testes

## Objetivo

Este documento define as métricas utilizadas para acompanhar a qualidade do Industria Plus e a cobertura dos testes realizados durante as entregas N1, N2 e N3.

As métricas devem apoiar a avaliação dos requisitos, regras de negócio, testes automatizados, cenários de aceite e defeitos identificados.

As métricas devem ser atualizadas a cada release e utilizadas como evidência complementar ao processo de aceite.

## Cobertura de requisitos

A cobertura de requisitos mede quantos requisitos funcionais e não funcionais possuem testes ou cenários de validação associados.

### Fórmula

```text
Cobertura de requisitos (%) =
(requisitos com pelo menos um teste associado / total de requisitos da release) × 100
```

Para a N1, devem ser considerados os requisitos `RF01` a `RF18` e `RNF01` a `RNF10`.

Um requisito é considerado coberto quando existir pelo menos um teste automatizado ou cenário de aceite que valide diretamente seu comportamento.

## Cobertura das regras de negócio

As regras de negócio devem possuir testes específicos, independentemente da cobertura geral dos requisitos.

Para o N1, devem ser acompanhadas principalmente:

* `RN-001`: limite de dois chamados de alta urgência por técnico;
* `RN-002`: validação e normalização de textos;
* `RN-003`: técnico obrigatório na atribuição;
* `RN-004`: repetição da atribuição ao mesmo técnico;
* `RN-005`: ordenação da fila de chamados.

A RN-001 deve possuir cobertura dos cenários normais, recusas e concorrência.

## Cobertura dos critérios de aceitação

Os cenários de aceite devem ser acompanhados individualmente.

### Fórmula

```text
Cobertura de aceite (%) =
(cenários executados / total de cenários previstos) × 100
```

Um cenário executado deve possuir resultado e evidência registrados.

Cenários pendentes não devem ser considerados aprovados.

Para a N1, os cenários definidos no refinamento incluem os grupos:

* `CA-C`: cadastros e abertura de chamados;
* `CA-F`: consulta e ciclo de vida;
* `CA-R1`: RN-001;
* `CA-R2`: RN-002;
* `CA-R3`: RN-003;
* `CA-R4`: RN-004;
* `CA-R5`: RN-005;
* `CA-Q`: requisitos não funcionais.

## Cobertura de testes automatizados

A cobertura automatizada deve ser acompanhada separadamente por camada.

### Frontend

Acompanhar:

* componentes;
* formulários;
* serviços;
* estados de interface;
* fluxos relevantes do N1.

Os testes do frontend utilizam Vitest.

### Backend

Acompanhar:

* regras de negócio;
* serviços;
* controllers;
* validações;
* tratamento de erros;
* persistência e consultas;
* transições de status;
* atribuição e reatribuição.

Os testes do backend utilizam JUnit, Mockito, Spring Boot Test e MockMvc.

### Infraestrutura e integração

Os fluxos de integração devem validar:

* comunicação entre frontend e backend;
* comunicação entre backend e MySQL;
* execução das migrations;
* persistência do volume;
* acesso da aplicação através do Nginx.

## Cobertura de código

A cobertura de código pode ser utilizada como métrica complementar para identificar partes do sistema pouco exercitadas pelos testes.

Devem ser acompanhados, quando disponíveis na ferramenta de cobertura:

* cobertura de linhas;
* cobertura de branches;
* cobertura de métodos;
* cobertura de classes.

A cobertura de código não substitui os testes de regras de negócio e critérios de aceite.

Uma porcentagem elevada de cobertura não deve ser considerada suficiente quando regras críticas, como a RN-001, não estiverem adequadamente validadas.

## Cobertura da RN-001

A RN-001 possui prioridade especial por envolver concorrência e integridade dos dados.

Devem estar cobertos, no mínimo:

* atribuição com carga zero;
* atribuição com carga um;
* rejeição da terceira atribuição;
* reatribuição para técnico sem capacidade;
* liberação de capacidade após encerramento;
* contagem somente de `HIGH` não encerrados;
* atribuições `LOW` e `MEDIUM` sem impacto na carga;
* atribuições concorrentes para o mesmo técnico;
* repetição da atribuição ao próprio responsável.

O cenário concorrente deve ser validado com MySQL e sua evidência deve registrar as duas respostas e a consulta final da carga persistida.

## Métricas de execução

Devem ser acompanhados, por release:

| Métrica                         | Descrição                                                   |
| ------------------------------- | ----------------------------------------------------------- |
| `Testes executados`             | Quantidade total de testes executados                       |
| `Testes aprovados`              | Quantidade de testes executados sem falha                   |
| `Testes reprovados`             | Quantidade de testes executados com falha                   |
| `Testes pendentes`              | Quantidade de testes previstos ainda não executados         |
| `Cenários de aceite aprovados`  | Cenários de aceite concluídos com resultado esperado        |
| `Cenários de aceite reprovados` | Cenários de aceite que apresentaram comportamento incorreto |
| `Cenários de aceite pendentes`  | Cenários ainda não executados                               |
| `Bugs abertos`                  | Quantidade de defeitos ainda não encerrados                 |
| `Bugs críticos`                 | Quantidade de defeitos com severidade crítica               |
| `Bugs altos`                    | Quantidade de defeitos com severidade alta                  |
| `Bugs reabertos`                | Quantidade de defeitos que voltaram a ocorrer               |
| `Cobertura de requisitos`       | Percentual de requisitos com testes associados              |
| `Cobertura de aceite`           | Percentual de cenários de aceite executados                 |

## Taxa de aprovação dos testes

### Fórmula

```text
Taxa de aprovação (%) =
(testes aprovados / testes executados) × 100
```

Testes pendentes não devem entrar no cálculo da taxa de aprovação.

O resultado deve ser acompanhado juntamente com a quantidade de testes reprovados para evitar que uma taxa percentual isolada esconda uma quantidade relevante de falhas.

## Taxa de aprovação do aceite

### Fórmula

```text
Taxa de aprovação do aceite (%) =
(cenários aprovados / cenários executados) × 100
```

Um cenário somente pode ser considerado aprovado quando todos os resultados esperados forem observados e a evidência correspondente estiver registrada.

## Métricas de defeitos

Devem ser acompanhadas, no mínimo:

* quantidade total de bugs identificados;
* quantidade de bugs por severidade;
* quantidade de bugs abertos;
* quantidade de bugs corrigidos;
* quantidade de bugs fechados;
* quantidade de bugs reabertos;
* quantidade de bugs críticos ou altos relacionados à release.

Bugs críticos ou altos relacionados à entrega devem ser considerados bloqueadores do aceite da funcionalidade afetada.

## Métrica de regressão

Sempre que uma correção de bug ou alteração de comportamento for realizada, os testes relacionados devem ser executados novamente.

Deve ser acompanhado:

```text
Quantidade de regressões identificadas após alterações
```

As regressões devem ser registradas como bugs e relacionadas ao teste ou requisito que apresentou a falha.

## Métricas de execução da pipeline

Devem ser acompanhados os resultados mínimos exigidos pelo projeto:

### Frontend

```text
npm run format
npm test
npm run build
```

### Backend

```text
mvn test
```

Uma release não deve ser considerada pronta quando esses comandos apresentarem falhas pendentes relacionadas à entrega.

## Metas da N1

O refinamento da N1 não estabelece metas quantitativas para tempo de resposta, disponibilidade ou quantidade de usuários simultâneos.

Portanto, essas métricas não constituem critérios de aceitação da N1.

Para a N1, o foco das métricas deve permanecer em:

* cobertura dos requisitos;
* cobertura dos cenários de aceite;
* cobertura das regras de negócio;
* execução dos testes automatizados;
* aprovação dos testes;
* quantidade e severidade dos bugs;
* execução dos requisitos não funcionais previstos;
* evidências dos cenários críticos.

## Registro das métricas

As métricas devem ser registradas por release, contendo:

* release avaliada;
* data da medição;
* responsável;
* quantidade total de testes;
* quantidade de testes aprovados;
* quantidade de testes reprovados;
* quantidade de cenários pendentes;
* cobertura de requisitos;
* cobertura de aceite;
* quantidade de bugs por severidade;
* observações relevantes.

Exemplo:

```text
Release: N1
Data: <data>
Responsável: <responsável>

Testes executados: <quantidade>
Testes aprovados: <quantidade>
Testes reprovados: <quantidade>
Testes pendentes: <quantidade>

Cobertura de requisitos: <percentual>%
Cobertura de aceite: <percentual>%

Bugs críticos: <quantidade>
Bugs altos: <quantidade>
Bugs médios: <quantidade>
Bugs baixos: <quantidade>
Bugs reabertos: <quantidade>

Observações:
<observações>
```

## Critérios de qualidade para o aceite

As métricas não substituem os critérios de aceite definidos para a release.

Para a N1, o aceite deve considerar:

* todos os cenários de aceite executados;
* nenhum cenário de aceite pendente ou reprovado;
* testes automatizados sem falhas pendentes;
* bugs críticos ou altos relacionados à entrega resolvidos;
* evidências obrigatórias registradas;
* fluxo completo da aplicação validado.

As métricas devem ser utilizadas como apoio à decisão e para acompanhamento da evolução da qualidade entre as releases.
