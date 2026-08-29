# Refinamento funcional do N1

## Objetivo

Este documento fecha as premissas funcionais necessárias para implementar e aceitar o N1 do
Industria Plus. Ele complementa o [escopo funcional](escopo.md) sem incluir as evoluções N2/N3.

## Premissas da entrega

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

## Domínio e validações

### Setor (`Sector`)

| Campo         | Obrigatório | Regra                    |
| ------------- | ----------- | ------------------------ |
| `name`        | sim         | entre 2 e 100 caracteres |
| `description` | não         | no máximo 255 caracteres |

### Equipamento (`Equipment`)

| Campo         | Obrigatório | Regra                                                    |
| ------------- | ----------- | -------------------------------------------------------- |
| `assetTag`    | sim         | identificador patrimonial único, entre 2 e 50 caracteres |
| `name`        | sim         | entre 2 e 120 caracteres                                 |
| `description` | não         | no máximo 500 caracteres                                 |
| `sectorId`    | sim         | deve identificar um setor existente                      |

### Técnico (`Technician`)

| Campo       | Obrigatório | Regra                                  |
| ----------- | ----------- | -------------------------------------- |
| `name`      | sim         | entre 2 e 120 caracteres               |
| `email`     | sim         | formato de e-mail válido e valor único |
| `specialty` | não         | no máximo 120 caracteres               |

O técnico é um cadastro operacional do helpdesk. No N1 ele não representa uma conta autenticada.

### Chamado de manutenção (`MaintenanceRequest`)

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

## Valores enumerados

### Urgência (`Urgency`)

- `LOW`: baixa
- `MEDIUM`: média
- `HIGH`: alta

### Tipo de manutenção (`MaintenanceType`)

- `PREVENTIVE`: preventiva
- `CORRECTIVE`: corretiva

### Status do chamado (`MaintenanceRequestStatus`)

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

## Contrato HTTP

A aplicação possui o contexto `/api`. Os caminhos públicos completos são, portanto,
`/api/sectors`, `/api/equipments`, `/api/technicians` e `/api/maintenance-requests`.

### Endpoints de cadastro e consulta

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

### Endpoints de comando do chamado

| Método e caminho                              | Corpo                         | Resposta de sucesso                             |
| --------------------------------------------- | ----------------------------- | ----------------------------------------------- |
| `PATCH /maintenance-requests/{id}/assignment` | `{ "technicianId": 42 }`      | `200 OK` com o chamado atribuído ou reatribuído |
| `PATCH /maintenance-requests/{id}/status`     | `{ "status": "IN_PROGRESS" }` | `200 OK` com o chamado após a transição         |

`technicianId` é obrigatório no comando de atribuição e deve identificar um técnico existente. O
N1 não oferece um comando para remover a atribuição sem indicar outro técnico.

### Formato das respostas

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

### Envelope de erro

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

## RN-001: limite de alta urgência

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

## Critérios de aceite do N1

### Cadastros e abertura

1. É possível cadastrar e listar um setor válido.
2. É possível cadastrar e listar um equipamento com patrimônio único e setor existente.
3. É possível cadastrar e listar um técnico com e-mail válido e único.
4. É possível abrir um chamado válido, que nasce como `OPEN`, sem técnico e com `createdAt` definido
   pelo backend.
5. A abertura é recusada quando o equipamento não pertence ao setor informado.
6. Entradas fora dos limites retornam o envelope de erro com `VALIDATION_ERROR` e os campos
   inválidos.

### Consulta e ciclo de vida

1. É possível filtrar chamados por status, por urgência ou pelos dois critérios em conjunto.
2. A consulta apresenta chamados em `HIGH`, `MEDIUM`, `LOW` e, dentro de cada urgência, do mais novo
   para o mais antigo.
3. É possível atribuir um chamado aberto a um técnico e reatribuí-lo a outro técnico.
4. As transições `OPEN` para `IN_PROGRESS`, `OPEN` para `CLOSED` e `IN_PROGRESS` para `CLOSED` são
   aceitas.
5. Qualquer outra transição é recusada, e `CLOSED` permanece terminal.

### RN-001

1. Um técnico sem chamados de urgência alta pode receber um chamado de urgência alta.
2. Um técnico com um chamado de urgência alta aberto pode receber o segundo.
3. Um técnico com dois chamados de urgência alta abertos não pode receber o terceiro; a API retorna
   `409 Conflict` e `HIGH_URGENCY_LIMIT`.
4. Depois que um dos chamados de urgência alta é encerrado, o técnico pode receber outro.
5. Chamados de urgência diferente de alta não entram nessa contagem.
6. Quando duas atribuições concorrentes tentam ultrapassar o limite para o mesmo técnico, somente a
   atribuição que ainda respeita o limite é confirmada; a outra recebe `HIGH_URGENCY_LIMIT`, e o
   estado persistido nunca supera dois chamados de alta urgência abertos.

## Fora do N1

Permanecem fora desta entrega:

- autenticação, autorização e contas de acesso;
- exclusão, atualização cadastral e inativação de setores, equipamentos e técnicos;
- remoção de atribuição sem indicar outro técnico;
- histórico de troca de peças por máquina;
- cálculo de MTBF;
- agendamento automático de manutenções preventivas.

Os itens de manutenção evolutiva dependem de novo refinamento para N2/N3.
