# Registro de bugs e evidências

## Objetivo

Este documento define o padrão para registro, acompanhamento e encerramento de bugs identificados durante o desenvolvimento, testes e aceite do Industria Plus.

O registro deve permitir reproduzir o problema, identificar seu impacto, acompanhar sua correção e manter evidências suficientes para comprovar o resultado dos testes.

O processo deve ser utilizado nas entregas N1, N2 e N3, sendo atualizado quando houver alteração nos critérios de qualidade ou no fluxo de testes.

## Registro de bugs

Todo defeito identificado deve ser registrado no backlog do projeto.

Cada registro deve conter, no mínimo:

| Campo                    | Descrição                                                                          |
| ------------------------ | ---------------------------------------------------------------------------------- |
| `Título`                 | Descrição curta e objetiva do problema                                             |
| `Descrição`              | Explicação do comportamento observado                                              |
| `Passos para reprodução` | Sequência necessária para reproduzir o problema                                    |
| `Resultado esperado`     | Comportamento definido pelos requisitos                                            |
| `Resultado obtido`       | Comportamento apresentado pelo sistema                                             |
| `Evidências`             | Prints, vídeos, respostas da API, logs controlados ou outras evidências relevantes |
| `Severidade`             | Impacto do defeito                                                                 |
| `Responsável`            | Pessoa responsável pelo acompanhamento ou correção                                 |
| `Ambiente`               | Ambiente em que o defeito foi encontrado                                           |
| `Commit`                 | Commit em que o problema foi identificado, quando aplicável                        |
| `Status`                 | Estado atual do defeito                                                            |

## Severidade

Os defeitos devem utilizar uma das seguintes classificações:

### Crítico

Interrompe o sistema, impede o uso de uma funcionalidade essencial ou viola uma regra de negócio fundamental.

Exemplos no N1:

* permitir mais de dois chamados `HIGH` não encerrados para o mesmo técnico;
* permitir alteração de um chamado `CLOSED`;
* permitir corrupção ou perda de dados;
* impedir o funcionamento completo da aplicação.

### Alto

Compromete uma funcionalidade importante, mas não impede completamente o uso do sistema.

Exemplos:

* atribuição ou reatribuição funcionando incorretamente em situações específicas;
* filtros de chamados retornando dados incorretos;
* consulta de recursos apresentando dados inconsistentes.

### Médio

Produz comportamento incorreto, porém existe um contorno que permite continuar utilizando o sistema.

Exemplos:

* erro em uma validação específica que pode ser contornado;
* inconsistência em determinada resposta ou fluxo que não comprometa os dados.

### Baixo

Possui pequeno impacto funcional ou visual.

Exemplos:

* problemas de layout;
* textos incorretos;
* pequenos problemas de apresentação que não afetam o funcionamento.

Bugs classificados como `Crítico` ou `Alto` relacionados à entrega devem impedir a conclusão da funcionalidade afetada.

## Status do bug

Os registros devem utilizar estados que permitam identificar claramente a situação do defeito.

* `Aberto`: defeito identificado e ainda não tratado.
* `Em análise`: defeito sendo investigado.
* `Em correção`: correção em desenvolvimento.
* `Corrigido`: correção implementada, aguardando validação.
* `Reaberto`: defeito voltou a ocorrer após uma tentativa de correção.
* `Fechado`: correção validada e evidência registrada.
* `Rejeitado`: comportamento identificado não constitui defeito ou não faz parte do escopo.
* `Adiado`: defeito conhecido cuja correção foi postergada para uma entrega futura.

## Evidências

Toda execução relevante de teste deve possuir evidência suficiente para comprovar o resultado.

As evidências podem incluir:

* captura de tela da interface;
* vídeo do fluxo;
* requisição e resposta HTTP;
* resposta contendo código de erro;
* consulta posterior confirmando persistência;
* resultado de testes automatizados;
* logs controlados;
* resultado de comandos de build e testes;
* informações do ambiente utilizado.

A evidência deve ser relacionada ao cenário ou bug correspondente e não deve expor credenciais, tokens, senhas ou outras informações sensíveis.

## Evidência dos cenários de aceite

Para cada cenário de aceite executado devem ser registrados:

* identificador do cenário;
* commit utilizado;
* ambiente;
* data;
* responsável;
* resultado;
* evidência.

Cenários não executados permanecem pendentes e não podem ser considerados aprovados.

Nas operações aceitas, deve ser conferida a resposta e realizada uma nova consulta para confirmar os dados persistidos.

Nas operações recusadas, deve ser confirmado que nenhuma criação ou alteração indevida ocorreu.

## Evidências obrigatórias para o N1

### Cadastro

Para cadastros aceitos, registrar a resposta de criação e uma consulta posterior confirmando os dados persistidos.

Para cadastros recusados, registrar a resposta da API e confirmar que nenhum registro inválido foi criado.

### Abertura de chamados

Registrar evidência de que:

* o chamado foi criado com `OPEN`;
* o técnico inicia como nulo;
* `createdAt` é definido pelo backend;
* tipo e urgência correspondem aos valores enviados.

### Atribuição e reatribuição

Registrar:

* técnico anterior, quando houver;
* técnico de destino;
* resposta da API;
* alteração das cargas dos técnicos envolvidos;
* consulta posterior confirmando o responsável.

### RN-001

A validação da RN-001 deve possuir evidências específicas.

No cenário concorrente:

1. registrar as duas requisições executadas;
2. registrar as duas respostas;
3. confirmar que exatamente uma recebeu `200 OK`;
4. confirmar que a outra recebeu `409 Conflict`;
5. confirmar que o código retornado foi `HIGH_URGENCY_LIMIT`;
6. realizar uma consulta final;
7. registrar que a carga persistida permaneceu igual a dois.

Esse cenário deve ser executado utilizando MySQL, pois o teste realizado apenas em H2 não substitui a evidência exigida para a concorrência da regra.

### Transições de status

Registrar o status anterior, a solicitação realizada, a resposta e o status posterior.

Para transições inválidas, registrar também o código de erro retornado.

### Persistência

Após reinicialização ou recriação dos containers com o volume preservado, registrar evidências de que os mesmos registros e vínculos continuam disponíveis.

## Evidências de erros da API

Quando o teste verificar uma falha da API, a evidência deve permitir identificar pelo menos:

* requisição enviada;
* endpoint;
* status HTTP;
* `code`;
* `message`;
* `fieldErrors`, quando aplicável;
* confirmação de que não houve alteração indevida.

Os códigos devem seguir o contrato definido para o N1, incluindo:

* `VALIDATION_ERROR`
* `INVALID_PARAMETER`
* `MALFORMED_REQUEST`
* `SECTOR_NOT_FOUND`
* `EQUIPMENT_NOT_FOUND`
* `TECHNICIAN_NOT_FOUND`
* `MAINTENANCE_REQUEST_NOT_FOUND`
* `DUPLICATE_ASSET_TAG`
* `DUPLICATE_TECHNICIAN_EMAIL`
* `EQUIPMENT_SECTOR_MISMATCH`
* `INVALID_STATUS_TRANSITION`
* `MAINTENANCE_REQUEST_CLOSED`
* `HIGH_URGENCY_LIMIT`
* `DATA_CONFLICT`
* `INTERNAL_ERROR`

## Critérios para encerramento de um bug

Um bug poderá ser marcado como `Fechado` somente quando:

1. a correção estiver implementada;
2. o cenário original for executado novamente;
3. o comportamento esperado for observado;
4. os testes relacionados não apresentarem regressão;
5. a evidência da correção estiver registrada;
6. quando aplicável, a persistência dos dados for confirmada.

Bugs críticos ou altos não devem ser encerrados somente com base em uma correção de código sem evidência de validação.

## Reabertura

Um bug deve ser reaberto quando:

* o comportamento original continuar ocorrendo;
* a correção introduzir uma regressão relacionada;
* o problema reaparecer em outra execução;
* a correção resolver apenas parcialmente o comportamento descrito.

Na reabertura, devem ser adicionadas novas evidências e descrito o comportamento observado.

## Relação entre bugs e testes

Sempre que possível, um bug deve indicar o cenário ou requisito relacionado.

Exemplos:

```text
RF16 → CA-R1-03 → BUG-XXX
RN-001 → CA-R1-08 → BUG-XXX
RN-005 → CA-R5-03 → BUG-XXX
```

Isso permite rastrear o defeito desde o requisito até a execução do teste e sua correção.

## Critério para o aceite da release

A release não deve ser considerada completamente aprovada enquanto houver:

* cenário de aceite pendente;
* cenário de aceite reprovado;
* bug crítico aberto relacionado à entrega;
* bug alto aberto relacionado à entrega;
* evidência obrigatória ausente.

A existência de bugs médios ou baixos deve ser avaliada pelo impacto e pelo escopo da entrega, sendo registrada no backlog quando sua correção não fizer parte da release atual.

## Modelo de registro

```text
ID: BUG-001
Título: Terceira atribuição HIGH aceita indevidamente

Descrição:
O sistema permite atribuir um terceiro chamado de alta urgência não encerrado
ao mesmo técnico.

Passos para reprodução:
1. Criar um técnico.
2. Criar três chamados HIGH.
3. Atribuir os dois primeiros ao técnico.
4. Atribuir o terceiro ao mesmo técnico.

Resultado esperado:
A terceira atribuição deve retornar 409 Conflict com
HIGH_URGENCY_LIMIT.

Resultado obtido:
A API retornou 200 OK e o técnico passou a possuir três chamados HIGH
não encerrados.

Severidade:
Crítico

Ambiente:
Docker + MySQL

Commit:
<commit>

Responsável:
<responsável>

Evidências:
<prints / requisições / respostas / vídeo>

Status:
Aberto
```
