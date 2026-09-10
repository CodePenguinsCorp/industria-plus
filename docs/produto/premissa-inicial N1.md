# Premissa Inicial N1

## Premissa do desenvolvimento até a N1 (escopo para a N1)

Este documento fecha as premissas funcionais necessárias para implementar e aceitar o N1 do
Industria Plus. Ele complementa o [escopo funcional](escopo.md) sem incluir as evoluções N2/N3.

O produto é um helpdesk de manutenção industrial para uma fábrica que precisa controlar a
manutenção preventiva e corretiva do parque fabril. O problema inicial são máquinas que param
sem aviso e técnicos sem clareza sobre qual chamado tem maior prioridade.

A N1 contempla cadastros operacionais, abertura manual de chamados, definição de urgência,
consulta da fila, atribuição e reatribuição de técnicos e acompanhamento até o encerramento.

### Premissas da entrega

- O N1 não possui autenticação nem autorização. Todos os endpoints descritos neste documento são
  públicos no ambiente da entrega.
- O N1 permite cadastrar e consultar setores, equipamentos e técnicos, mas não oferece exclusão,
  atualização cadastral ou inativação desses recursos.
- Os únicos endpoints de alteração são comandos específicos para atribuir ou reatribuir um chamado
  e para avançar o seu status. Eles não representam uma atualização cadastral genérica.
- Um equipamento pertence obrigatoriamente a um único setor.
- Um chamado referencia um equipamento e um setor. O setor informado no chamado deve ser o mesmo
  setor ao qual o equipamento pertence.
- Textos obrigatórios são validados depois da remoção de espaços no início e no fim. Um texto que
  contenha apenas espaços é considerado vazio.
- Identificadores são números inteiros positivos.
- A API usa JSON em UTF-8. Datas e horários retornados pela API seguem ISO 8601.

### Fora do escopo da N1

Permanecem fora desta entrega:

- autenticação, autorização e contas de acesso;
- exclusão, atualização cadastral e inativação de setores, equipamentos e técnicos;
- remoção de atribuição sem indicar outro técnico;
- histórico de troca de peças por máquina;
- cálculo de MTBF;
- agendamento automático de manutenções preventivas.

Os itens de manutenção evolutiva dependem de novo refinamento para N2/N3.

## Requisitos Funcionais e Não Funcionais

### Requisitos Funcionais

| Código | Requisito |
| --- | --- |
| RF01 | O sistema deve permitir cadastrar setores com nome obrigatório e descrição opcional, respeitando os limites deste documento. |
| RF02 | O sistema deve permitir consultar setores com identificador, nome e descrição. |
| RF03 | O sistema deve permitir cadastrar equipamentos com patrimônio único, nome, descrição opcional e vínculo obrigatório com um setor existente. |
| RF04 | O sistema deve permitir consultar equipamentos com patrimônio, nome, descrição e setor associado. |
| RF05 | O sistema deve permitir cadastrar técnicos com nome, e-mail válido e único e especialidade opcional. |
| RF06 | O sistema deve listar técnicos e a quantidade de chamados de alta urgência não encerrados atribuídos a cada um. |
| RF07 | O sistema deve permitir abrir chamados com título, descrição, equipamento, setor, tipo de manutenção e urgência. |
| RF08 | O sistema deve permitir classificar o chamado na abertura como manutenção preventiva ou corretiva. |
| RF09 | O sistema deve permitir definir a urgência na abertura como baixa, média ou alta. |
| RF10 | O sistema deve recusar a abertura quando equipamento ou setor não existir ou quando o equipamento não pertencer ao setor informado. |
| RF11 | O sistema deve criar chamados abertos, sem técnico e com data e horário de criação definidos pelo backend. |
| RF12 | O sistema deve consultar chamados com identificação, título, descrição, equipamento, setor, tipo, urgência, status, técnico quando houver e datas de criação e atualização. |
| RF13 | O sistema deve permitir filtrar chamados por status e urgência, individualmente ou em conjunto. |
| RF14 | O sistema deve ordenar a fila por urgência alta, média e baixa e, dentro de cada urgência, do chamado mais recente para o mais antigo. |
| RF15 | O sistema deve permitir atribuir e reatribuir chamados não encerrados a técnicos existentes. |
| RF16 | O sistema deve recusar atribuições e reatribuições que ultrapassem dois chamados de alta urgência não encerrados por técnico, informando o motivo conforme a RN-001. |
| RF17 | O sistema deve permitir somente as transições de aberto para em andamento, de aberto para encerrado e de em andamento para encerrado. |
| RF18 | O sistema deve impedir reabertura, atribuição e reatribuição de chamados encerrados. |

### Requisitos Não Funcionais

| Código | Requisito |
| --- | --- |
| RNF01 | A validação do limite e a gravação da atribuição devem ser transacionais e protegidas contra concorrência, sem permitir que operações simultâneas ultrapassem a RN-001. |
| RNF02 | O backend deve validar entradas e regras de negócio independentemente do frontend, inclusive em requisições diretas à API. |
| RNF03 | Os dados devem persistir em MySQL com volume persistente e permanecer disponíveis após reinícios ou recriação de contêineres que preservem esse volume. |
| RNF04 | A API deve utilizar JSON em UTF-8 e retornar datas e horários em ISO 8601. |
| RNF05 | As falhas tratadas devem utilizar o envelope de erro documentado, com código estável, status HTTP, mensagem e indicação dos campos inválidos quando aplicável. |
| RNF06 | A interface e as mensagens destinadas ao usuário devem utilizar português brasileiro. |
| RNF07 | Erros inesperados devem retornar mensagem genérica, sem expor credenciais, rastros de execução ou detalhes internos. |
| RNF08 | Em produção, somente o serviço web do frontend deve publicar porta no host; backend e banco devem ser acessados pela rede interna dos contêineres. |
| RNF09 | O ambiente deve ser reproduzível com Docker Compose, configuração por variáveis de ambiente e alterações de esquema versionadas com Flyway. |
| RNF10 | O código deve ser organizado por domínio, com regras de negócio nos serviços do backend, persistência nos repositórios e controllers responsáveis pela entrada e delegação. |

Metas quantitativas de tempo de resposta, disponibilidade e usuários simultâneos não foram
definidas para a N1 e não constituem critérios de aceitação aprovados nesta entrega.

## Regras de Negócio

### RN-001: limite de alta urgência

Um técnico pode possuir, no máximo, dois chamados simultaneamente quando os dois critérios abaixo
forem verdadeiros:

- `urgency` igual a `HIGH`;
- `status` igual a `OPEN` ou `IN_PROGRESS`.

Chamados `CLOSED` e chamados com urgência `LOW` ou `MEDIUM` não entram na contagem. A regra é
avaliada em toda atribuição e reatribuição antes de persistir o técnico no chamado.

A validação é transacional. O backend deve obter um bloqueio pessimista sobre o técnico de destino,
contar novamente os seus chamados de alta urgência abertos dentro da mesma transação e somente
então salvar a atribuição. Isso serializa atribuições concorrentes para o mesmo técnico. A terceira
atribuição é recusada com `409 Conflict` e `code` igual a `HIGH_URGENCY_LIMIT`.

### RN-002: validação de textos obrigatórios

Textos obrigatórios devem ser validados após a remoção de espaços no início e no fim.
Um texto formado apenas por espaços deve ser considerado vazio e recusado. Os limites de
tamanho definidos neste documento devem ser aplicados ao texto resultante.
Esta é uma regra de validação de entrada.

### RN-003: técnico obrigatório na atribuição

A atribuição ou reatribuição de um chamado deve indicar um técnico existente.
Após atribuído, o chamado não pode ficar sem responsável por meio da remoção da atribuição:
na N1, a troca de responsável exige a indicação de outro técnico.
Esta regra não impede a abertura de chamados sem técnico atribuído.

### RN-004: repetição da atribuição ao mesmo técnico

Em um chamado não encerrado, atribuir novamente ao técnico que já é o responsável deve
preservar a atribuição existente, sem aumentar sua quantidade de chamados ou contabilizar
novamente o chamado no limite da RN-001.
Chamados encerrados continuam impedidos de receber atribuição ou reatribuição.

### RN-005: ordenação da fila de chamados

A fila deve apresentar primeiro os chamados de urgência alta, depois média e, por último,
baixa. Dentro de cada nível de urgência, os chamados devem ser ordenados pela data de criação
decrescente, do mais recente para o mais antigo. Quando houver empate na data de criação,
o chamado com maior identificador deve aparecer primeiro.

### Domínio e validações

#### Setor (`Sector`)

| Campo         | Obrigatório | Regra                    |
| ------------- | ----------- | ------------------------ |
| `name`        | sim         | entre 2 e 100 caracteres |
| `description` | não         | no máximo 255 caracteres |

#### Equipamento (`Equipment`)

| Campo         | Obrigatório | Regra                                                    |
| ------------- | ----------- | -------------------------------------------------------- |
| `assetTag`    | sim         | identificador patrimonial único, entre 2 e 50 caracteres |
| `name`        | sim         | entre 2 e 120 caracteres                                 |
| `description` | não         | no máximo 500 caracteres                                 |
| `sectorId`    | sim         | deve identificar um setor existente                      |

#### Técnico (`Technician`)

| Campo       | Obrigatório | Regra                                  |
| ----------- | ----------- | -------------------------------------- |
| `name`      | sim         | entre 2 e 120 caracteres               |
| `email`     | sim         | formato de e-mail válido e valor único |
| `specialty` | não         | no máximo 120 caracteres               |

O técnico é um cadastro operacional do helpdesk. No N1 ele não representa uma conta autenticada.

#### Chamado de manutenção (`MaintenanceRequest`)

| Campo         | Obrigatório na abertura | Regra                                                             |
| ------------- | ----------------------- | ----------------------------------------------------------------- |
| `title`       | sim                     | entre 3 e 160 caracteres                                          |
| `description` | sim                     | entre 10 e 2.000 caracteres                                       |
| `equipmentId` | sim                     | deve identificar um equipamento existente                         |
| `sectorId`    | sim                     | deve identificar o setor existente ao qual o equipamento pertence |
| `type`        | sim                     | um dos valores de `MaintenanceType`                               |
| `urgency`     | sim                     | um dos valores de `Urgency`                                       |

O backend define `status` como `OPEN`, `technicianId` como nulo e `createdAt` no instante da
abertura. Esses campos não são aceitos como forma de contornar o fluxo de abertura.

### Valores enumerados

#### Urgência (`Urgency`)

- `LOW`: baixa
- `MEDIUM`: média
- `HIGH`: alta

#### Tipo de manutenção (`MaintenanceType`)

- `PREVENTIVE`: preventiva
- `CORRECTIVE`: corretiva

#### Status do chamado (`MaintenanceRequestStatus`)

- `OPEN`: aberto e ainda não iniciado
- `IN_PROGRESS`: atendimento em andamento
- `CLOSED`: encerrado

`OPEN` e `IN_PROGRESS` são considerados status abertos para a RN-001. `CLOSED` é terminal: um
chamado encerrado não pode voltar a outro status nem receber nova atribuição ou reatribuição.

As únicas transições permitidas são:

| Status atual  | Próximos status permitidos |
| ------------- | -------------------------- |
| `OPEN`        | `IN_PROGRESS` ou `CLOSED`  |
| `IN_PROGRESS` | `CLOSED`                   |
| `CLOSED`      | nenhum                     |

Uma transição fora dessa matriz retorna `409 Conflict`. Tentativas de alterar um chamado já
encerrado usam `MAINTENANCE_REQUEST_CLOSED`; as demais usam `INVALID_STATUS_TRANSITION`.

## Decisões e Requisitos Técnicos

### Stack e organização

- Frontend: Angular 21, TypeScript e componentes standalone; estilos em CSS puro.
- Backend: Java 17 e Spring Boot 3.5.
- Banco de dados: MySQL 8.4; persistência com Spring Data JPA e migrations Flyway.
- Infraestrutura: Docker Compose e Nginx, encaminhando `/api` ao backend.
- Configurações e credenciais recebidas por variáveis de ambiente; arquivos locais de
  credenciais não devem ser versionados.
- Código organizado por domínio, com identificadores em inglês e documentação em português brasileiro.
- Testes: Vitest no frontend; JUnit, Mockito, Spring Boot Test e MockMvc no backend.

Essas decisões seguem a [arquitetura do projeto](../arquitetura/stack.md) e substituem a
stack React, Node.js e PostgreSQL citada no acordo original.

### Contrato HTTP

A aplicação possui o contexto `/api`. Os caminhos públicos completos são, portanto,
`/api/sectors`, `/api/equipments`, `/api/technicians` e `/api/maintenance-requests`.

#### Endpoints de cadastro e consulta

| Método e caminho             | Entrada                                | Resposta de sucesso                                     |
| ---------------------------- | -------------------------------------- | ------------------------------------------------------- |
| `GET /sectors`               | sem corpo                              | `200 OK` com uma lista de `SectorResponse`              |
| `POST /sectors`              | `SectorRequest`                        | `201 Created` com o `SectorResponse` criado             |
| `GET /equipments`            | sem corpo                              | `200 OK` com uma lista de `EquipmentResponse`           |
| `POST /equipments`           | `EquipmentRequest`                     | `201 Created` com o `EquipmentResponse` criado          |
| `GET /technicians`           | sem corpo                              | `200 OK` com uma lista de `TechnicianResponse`          |
| `POST /technicians`          | `TechnicianRequest`                    | `201 Created` com o `TechnicianResponse` criado         |
| `GET /maintenance-requests`  | filtros opcionais `status` e `urgency` | `200 OK` com uma lista de `MaintenanceRequestResponse`  |
| `POST /maintenance-requests` | `MaintenanceRequestCreateRequest`      | `201 Created` com o `MaintenanceRequestResponse` criado |

Quando os dois filtros de chamados são enviados, ambos devem ser atendidos. Valores de enumeração
desconhecidos retornam `400 Bad Request`. A lista de chamados é ordenada primeiro por urgência
(`HIGH`, `MEDIUM`, `LOW`) e depois por `createdAt` decrescente. Não há paginação no N1.

#### Endpoints de comando do chamado

| Método e caminho                              | Corpo                         | Resposta de sucesso                             |
| --------------------------------------------- | ----------------------------- | ----------------------------------------------- |
| `PATCH /maintenance-requests/{id}/assignment` | `{ "technicianId": 42 }`      | `200 OK` com o chamado atribuído ou reatribuído |
| `PATCH /maintenance-requests/{id}/status`     | `{ "status": "IN_PROGRESS" }` | `200 OK` com o chamado após a transição         |

`technicianId` é obrigatório no comando de atribuição e deve identificar um técnico existente. O
N1 não oferece um comando para remover a atribuição sem indicar outro técnico.

#### Formato das respostas

- `SectorResponse`: `id`, `name`, `description` e `createdAt`.
- `EquipmentResponse`: `id`, `assetTag`, `name`, `description`, `sectorId`, `sectorName` e
  `createdAt`.
- `TechnicianResponse`: `id`, `name`, `email`, `specialty`, `highUrgencyOpenRequests` e
  `createdAt`. A carga indica quantos chamados de alta urgência abertos estão atribuídos ao
  técnico no instante da consulta.
- `MaintenanceRequestResponse`: `id`, `title`, `description`, `equipmentId`, `equipmentName`,
  `sectorId`, `sectorName`, `type`, `urgency`, `status`, `technicianId`, `technicianName` e
  `createdAt` e `updatedAt`.

`technicianId` e `technicianName` são nulos enquanto o chamado não estiver atribuído. Campos
opcionais sem conteúdo são retornados como nulos.

#### Envelope de erro

Toda falha tratada retorna o mesmo formato. `code` é estável e deve ser usado pelo frontend para
decisões; `message` é uma explicação em português para a pessoa usuária.

```json
{
  "timestamp": "2026-08-28T14:30:00-03:00",
  "status": 409,
  "error": "Conflict",
  "code": "HIGH_URGENCY_LIMIT",
  "message": "O técnico já possui dois chamados de alta urgência abertos.",
  "path": "/api/maintenance-requests/15/assignment",
  "fieldErrors": {}
}
```

`fieldErrors` associa o nome de cada campo inválido à respectiva mensagem e é um objeto vazio nas
falhas que não sejam de validação de campos.

Os códigos mínimos do N1 são:

| HTTP  | Código                          | Uso                                                            |
| ----- | ------------------------------- | -------------------------------------------------------------- |
| `400` | `VALIDATION_ERROR`              | campo ausente, vazio, malformado ou fora dos limites           |
| `400` | `INVALID_PARAMETER`             | enumeração desconhecida ou parâmetro incompatível na URL       |
| `400` | `MALFORMED_REQUEST`             | JSON ausente, inválido ou com valor incompatível               |
| `404` | `SECTOR_NOT_FOUND`              | setor não encontrado                                           |
| `404` | `EQUIPMENT_NOT_FOUND`           | equipamento não encontrado                                     |
| `404` | `TECHNICIAN_NOT_FOUND`          | técnico não encontrado                                         |
| `404` | `MAINTENANCE_REQUEST_NOT_FOUND` | chamado não encontrado                                         |
| `409` | `DUPLICATE_ASSET_TAG`           | patrimônio de equipamento já cadastrado                        |
| `409` | `DUPLICATE_TECHNICIAN_EMAIL`    | e-mail de técnico já cadastrado                                |
| `409` | `EQUIPMENT_SECTOR_MISMATCH`     | equipamento não pertence ao setor informado                    |
| `409` | `INVALID_STATUS_TRANSITION`     | transição de status fora da matriz permitida                   |
| `409` | `MAINTENANCE_REQUEST_CLOSED`    | tentativa de atribuir, reatribuir ou alterar chamado encerrado |
| `409` | `HIGH_URGENCY_LIMIT`            | violação da RN-001                                             |
| `409` | `DATA_CONFLICT`                 | violação residual de uma restrição de integridade              |
| `500` | `INTERNAL_ERROR`                | erro inesperado sem exposição de detalhes internos             |

## Critérios de Aceitação dentro do Escopo da N1

### Como verificar e registrar o aceite

Cada cenário abaixo descreve uma **condição inicial**, uma **ação** e um **resultado esperado**.
O cenário é aprovado somente quando todos os resultados descritos forem observados.

- Executar cada cenário com dados controlados e com as demais entradas válidas, para que a
  recusa seja causada pela condição que está sendo verificada.
- Nas operações aceitas, conferir a resposta e realizar uma nova consulta para confirmar os
  dados persistidos. Nas recusadas, confirmar que não houve criação ou alteração indevida.
- Neste documento, **não encerrado** significa `OPEN` (Aberto) ou `IN_PROGRESS` (Em andamento).
  **Carga de alta urgência** é a quantidade de chamados `HIGH` não encerrados de um técnico.
- Os códigos HTTP e de erro devem ser conferidos na API. Na interface, conferir os dados
  apresentados e a mensagem compreensível ao usuário; não é necessário exibir códigos técnicos.
- Registrar o identificador do cenário, commit, ambiente, data, responsável, resultado e evidência.
  Um cenário não executado deve permanecer pendente, sem ser considerado aprovado.

### Cadastros e abertura de chamados — RF01 a RF12

| ID | Condição inicial e ação | Resultado esperado |
| --- | --- | --- |
| CA-C01 | Informar nome válido e descrição opcional de um setor e salvar. | A API retorna `201 Created`. O setor recebe identificador e aparece na consulta com nome e descrição informados, respeitando a normalização de textos. |
| CA-C02 | Com um setor existente, cadastrar equipamento com patrimônio ainda não utilizado, nome válido e descrição opcional. | A API retorna `201 Created`. A consulta apresenta patrimônio, nome, descrição e o setor associado. |
| CA-C03 | Cadastrar técnico com nome válido, e-mail válido ainda não utilizado e especialidade opcional. | A API retorna `201 Created`. O técnico aparece na consulta e inicia com carga de alta urgência igual a zero. |
| CA-C04 | Tentar cadastrar equipamento com patrimônio já utilizado ou técnico com e-mail já utilizado. Executar uma tentativa para cada caso. | A API retorna `409 Conflict`, com `DUPLICATE_ASSET_TAG` ou `DUPLICATE_TECHNICIAN_EMAIL`, respectivamente. Nenhum registro duplicado é criado. |
| CA-C05 | Abrir chamado com título e descrição válidos, equipamento existente e seu respectivo setor. Repetir para manutenção preventiva e corretiva e para urgências baixa, média e alta. | Cada abertura retorna `201 Created`, preserva tipo e urgência escolhidos e cria o chamado em `OPEN`, sem técnico e com `createdAt` definido pelo backend. |
| CA-C06 | Abrir chamado indicando um equipamento existente e um setor existente diferente daquele ao qual o equipamento pertence. | A API retorna `409 Conflict` e `EQUIPMENT_SECTOR_MISMATCH`. Nenhum chamado é criado. |
| CA-C07 | Informar setor inexistente no cadastro de equipamento; na abertura de chamado, informar equipamento ou setor inexistente, um por tentativa. | A API retorna `404 Not Found` e `SECTOR_NOT_FOUND` ou `EQUIPMENT_NOT_FOUND`, conforme a referência inválida. Nenhum registro é criado. |
| CA-C08 | Omitir um campo obrigatório ou informar valor fora das validações de domínio, um campo por tentativa. | A API retorna `400 Bad Request`, `VALIDATION_ERROR` e o campo correspondente em `fieldErrors`. Nenhum registro inválido é criado. Para textos, aplicar também os cenários da RN-002. |
| CA-C09 | Consultar um chamado criado e consultá-lo novamente após alterar sua atribuição ou status. | A consulta disponibiliza identificação, título, descrição, equipamento, setor, tipo, urgência, status, responsável quando houver e datas de criação e atualização. A criação é preservada e os dados refletem a alteração efetivada. |

### Consulta e ciclo de vida — RF13 a RF18

| ID | Condição inicial e ação | Resultado esperado |
| --- | --- | --- |
| CA-F01 | Com chamados de diferentes status e urgências, consultar por status, por urgência e pelos dois filtros juntos, em consultas separadas. | Cada resultado contém somente chamados que atendem aos filtros. Quando combinados, ambos os critérios são atendidos. A ordenação segue a RN-005. |
| CA-F02 | Com chamado em `OPEN`, solicitar `IN_PROGRESS`. Em outro chamado `OPEN`, solicitar `CLOSED`. Em chamado `IN_PROGRESS`, solicitar `CLOSED`. | Cada transição retorna `200 OK`, e a consulta posterior apresenta o status solicitado. |
| CA-F03 | Em chamado não encerrado, solicitar uma transição fora da matriz permitida, incluindo repetir o status atual ou voltar de `IN_PROGRESS` para `OPEN`. | A API retorna `409 Conflict` e `INVALID_STATUS_TRANSITION`. O status anterior é preservado. |
| CA-F04 | Em chamado `CLOSED`, tentar mudar o status, atribuir um técnico ou trocar o responsável, em tentativas separadas. | Todas as tentativas retornam `409 Conflict` e `MAINTENANCE_REQUEST_CLOSED`. Status e responsável permanecem inalterados. |

O aceite da atribuição e reatribuição é detalhado nas RN-001, RN-003 e RN-004 abaixo.

### RN-001 — Limite de dois chamados de alta urgência por técnico

| ID | Condição inicial e ação | Resultado esperado |
| --- | --- | --- |
| CA-R1-01 | Atribuir um chamado `HIGH` não encerrado a um técnico com carga zero. | A atribuição é aceita com `200 OK`; a carga passa a um. |
| CA-R1-02 | Atribuir outro chamado `HIGH` não encerrado ao mesmo técnico, agora com carga um. | A atribuição é aceita com `200 OK`; a carga passa a dois. |
| CA-R1-03 | Com carga dois, tentar atribuir ao técnico um terceiro chamado `HIGH` não encerrado. | A API retorna `409 Conflict` e `HIGH_URGENCY_LIMIT`. A carga permanece dois, e o chamado mantém seu responsável anterior ou continua sem técnico. |
| CA-R1-04 | Tentar reatribuir um chamado `HIGH` não encerrado para outro técnico que já possui carga dois. | A API retorna `409 Conflict` e `HIGH_URGENCY_LIMIT`. O técnico original permanece responsável, sem alterar as cargas. |
| CA-R1-05 | Com carga dois, encerrar um dos chamados e depois atribuir outro `HIGH` não encerrado ao mesmo técnico. | O encerramento reduz a carga para um; a nova atribuição é aceita e a carga volta a dois. |
| CA-R1-06 | Consultar a carga de um técnico com um chamado `HIGH` em `OPEN`, outro `HIGH` em `IN_PROGRESS`, além de chamados `HIGH` encerrados e chamados `LOW` ou `MEDIUM`. | A carga é exatamente dois: somente os chamados `HIGH` não encerrados são contados. |
| CA-R1-07 | Com carga de alta urgência igual a dois, atribuir ao técnico um chamado `LOW` e outro `MEDIUM` não encerrados. | Ambas as atribuições são aceitas; a carga de alta urgência permanece dois. |
| CA-R1-08 | Com carga um, enviar simultaneamente duas atribuições de chamados distintos `HIGH` não encerrados para o mesmo técnico. | Exatamente uma retorna `200 OK`; a outra retorna `409 Conflict` e `HIGH_URGENCY_LIMIT`. Após as duas respostas, a carga persistida é dois. |

### RN-002 — Validação de textos obrigatórios

Aplicar os cenários aos campos textuais obrigatórios, usando os limites de cada campo definidos
em **Domínio e validações**. Manter válidas as demais condições, como formato de e-mail e unicidade.

| ID | Condição inicial e ação | Resultado esperado |
| --- | --- | --- |
| CA-R2-01 | Enviar texto válido com espaços adicionais no início e no fim. | A operação é aceita e a consulta retorna o texto sem esses espaços externos. |
| CA-R2-02 | Enviar texto vazio e, em outra tentativa, texto formado apenas por espaços. | Cada tentativa retorna `400 Bad Request`, `VALIDATION_ERROR` e o campo em `fieldErrors`; nenhum registro é criado. |
| CA-R2-03 | Em campo com tamanho mínimo definido, enviar texto que só atinge esse mínimo quando os espaços externos são contados. | A operação é recusada com `VALIDATION_ERROR`, pois o tamanho é verificado após remover os espaços externos. |
| CA-R2-04 | Enviar texto cujo conteúdo, após remover espaços externos, tenha exatamente o mínimo permitido e, em outra tentativa, exatamente o máximo. | Ambas as operações são aceitas, desde que as demais validações do campo sejam atendidas. |
| CA-R2-05 | Enviar texto que, mesmo após remover espaços externos, exceda o máximo permitido. | A API retorna `400 Bad Request`, `VALIDATION_ERROR` e o campo em `fieldErrors`, sem criar o registro. |

### RN-003 — Técnico obrigatório na atribuição

| ID | Condição inicial e ação | Resultado esperado |
| --- | --- | --- |
| CA-R3-01 | Abrir um chamado com os dados obrigatórios válidos, sem informar técnico. | A abertura é aceita; `technicianId` e `technicianName` são nulos. A exigência de técnico aplica-se ao comando de atribuição. |
| CA-R3-02 | Em chamado não encerrado, selecionar técnico existente com capacidade segundo a RN-001 e confirmar a atribuição. | A API retorna `200 OK`; a resposta e a consulta posterior identificam o técnico selecionado. |
| CA-R3-03 | Em chamado não encerrado, enviar identificador positivo de técnico inexistente. | A API retorna `404 Not Found` e `TECHNICIAN_NOT_FOUND`. O responsável anterior é preservado ou o chamado continua sem técnico. |
| CA-R3-04 | Enviar comando de atribuição com `technicianId` ausente, nulo, zero ou negativo, em tentativas separadas. | Cada tentativa retorna `400 Bad Request`, `VALIDATION_ERROR` e erro no campo. Nenhuma atribuição é alterada. |
| CA-R3-05 | Reatribuir chamado `HIGH` não encerrado do técnico A para o técnico B, existente e com capacidade disponível. | A API retorna `200 OK`; B passa a ser o responsável, a carga de A diminui em um e a de B aumenta em um. |
| CA-R3-06 | Em chamado já atribuído, tentar remover o responsável enviando `technicianId` nulo. | A API retorna `400 Bad Request` e `VALIDATION_ERROR`. O técnico anterior continua responsável. |

### RN-004 — Repetição da atribuição ao mesmo técnico

| ID | Condição inicial e ação | Resultado esperado |
| --- | --- | --- |
| CA-R4-01 | Em chamado atribuído em `OPEN`, enviar novamente o identificador do responsável atual; repetir o cenário em `IN_PROGRESS`. | A API retorna `200 OK`, preserva o responsável e não duplica o vínculo nem aumenta sua carga. |
| CA-R4-02 | Com técnico que já possui dois chamados `HIGH` não encerrados, repetir a atribuição de um desses chamados ao próprio técnico. | A API retorna `200 OK`, sem `HIGH_URGENCY_LIMIT`; a carga permanece dois. |
| CA-R4-03 | Repetir várias vezes a atribuição de um chamado não encerrado ao responsável atual e consultar sua carga. | A contagem permanece igual à anterior às repetições. |
| CA-R4-04 | Em chamado `CLOSED`, repetir a atribuição ao responsável atual. | A API retorna `409 Conflict` e `MAINTENANCE_REQUEST_CLOSED`. O chamado permanece encerrado, sem alteração de responsável. |

### RN-005 — Ordenação da fila de chamados

| ID | Condição inicial e ação | Resultado esperado |
| --- | --- | --- |
| CA-R5-01 | Consultar uma fila contendo chamados `HIGH`, `MEDIUM` e `LOW`. | Todos os `HIGH` aparecem antes dos `MEDIUM`, que aparecem antes dos `LOW`. |
| CA-R5-02 | Consultar um chamado `HIGH` antigo e um chamado de menor urgência criado depois dele. | O chamado `HIGH` aparece primeiro; a urgência tem precedência sobre a data. |
| CA-R5-03 | Consultar chamados de mesma urgência com datas e horários de criação diferentes. | Os chamados aparecem do mais recente para o mais antigo. |
| CA-R5-04 | Com dados controlados de mesma urgência e mesmo valor de `createdAt`, consultar a fila. | O chamado com maior identificador aparece primeiro. |
| CA-R5-05 | Aplicar filtro de status, de urgência e ambos juntos à fila, em consultas separadas. | Somente os chamados compatíveis são retornados, mantendo a ordem por urgência, criação e identificador. |

### Qualidade e operação — Requisitos Não Funcionais

| ID | Requisito e verificação | Resultado esperado |
| --- | --- | --- |
| CA-Q01 | **RNF01:** executar CA-R1-08 em ambiente de teste com MySQL. | A concorrência não ultrapassa a carga dois. Registrar as duas respostas e a consulta final; teste apenas em H2 não substitui essa evidência. |
| CA-Q02 | **RNF02:** executar os cenários de entradas inválidas e recusas de negócio diretamente na API, sem usar a interface. | As mesmas validações são aplicadas e nenhum dado inválido é persistido. |
| CA-Q03 | **RNF03:** cadastrar dados, registrar IDs e vínculos, reiniciar e depois recriar os contêineres em ambiente de teste preservando o volume do banco. | Após cada operação, os mesmos registros e vínculos continuam consultáveis. Não remover o volume durante a verificação. |
| CA-Q04 | **RNF04:** enviar e consultar textos com acentos e inspecionar respostas com datas. | A comunicação utiliza JSON em UTF-8, preserva os caracteres e apresenta datas e horários em ISO 8601. |
| CA-Q05 | **RNF05:** provocar falha de validação, recurso inexistente e conflito de negócio, em tentativas separadas. | Cada resposta contém o envelope documentado, status e código correspondentes; `fieldErrors` identifica campos inválidos ou é vazio quando não se aplica. |
| CA-Q06 | **RNF06:** percorrer cadastros e chamados e provocar mensagens de validação e negócio. | Rótulos, status, urgências e mensagens destinados ao usuário são apresentados em português brasileiro. |
| CA-Q07 | **RNF07:** simular uma exceção inesperada em teste controlado e inspecionar a resposta. | A API retorna `500 Internal Server Error`, `INTERNAL_ERROR` e mensagem genérica, sem credenciais, rastros de execução ou detalhes internos. |
| CA-Q08 | **RNF08:** inspecionar as portas publicadas pela stack de produção em ambiente de teste e acessar a API pelo frontend. | Somente o frontend publica porta no host; Nginx encaminha a requisição ao backend pela rede interna, e o banco não publica porta no host. |
| CA-Q09 | **RNF09:** subir a stack em ambiente de teste limpo, com variáveis configuradas e volume novo. | Frontend, backend e MySQL iniciam; Flyway aplica as migrations e o fluxo de cadastro e consulta funciona. Registrar a configuração utilizada sem expor credenciais. |
| CA-Q10 | **RNF10:** revisar a organização do código dos domínios da N1. | Controllers validam entrada e delegam; serviços executam regras de negócio; repositórios concentram o acesso a dados; funcionalidades permanecem organizadas por domínio. |

### Condições para concluir o aceite da N1

- Todos os cenários acima devem ter resultado e evidência registrados; cenários pendentes ou
  reprovados devem ser resolvidos antes de declarar o aceite completo.
- Executar `npm run format`, `npm test` e `npm run build` no frontend e `mvn test` no backend,
  conforme a [estratégia de testes](../qualidade/estrategia-testes.md), sem falhas pendentes.
- Validar o fluxo completo pela interface: cadastrar setor, equipamento e técnico; abrir
  chamados; atribuir dois de alta urgência; conferir a recusa do terceiro; encerrar um dos
  anteriores; e confirmar que uma nova atribuição passa a ser aceita.
- Não deve haver defeito crítico ou alto aberto relacionado à entrega.

Estes são critérios exigidos para o aceite, e não uma declaração de testes já executados ou
funcionalidades já aprovadas. Autenticação e evoluções N2/N3 permanecem fora deste aceite.
