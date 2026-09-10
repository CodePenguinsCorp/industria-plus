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

### RN-002

1. Um texto obrigatório com espaços no início e no fim é aceito quando o conteúdo resultante
   atende às validações do campo, sendo armazenado sem esses espaços.
2. Um texto obrigatório vazio ou formado apenas por espaços é recusado com `400 Bad Request`,
   `VALIDATION_ERROR` e identificação do campo em `fieldErrors`.
3. Um texto cujo tamanho fica abaixo do mínimo após remover os espaços externos é recusado,
   mesmo que o tamanho original atinja o mínimo exigido.
4. Textos com tamanho exatamente igual ao mínimo ou ao máximo após a remoção dos espaços externos
   são aceitos, desde que as demais validações sejam atendidas.
5. Um texto que permanece acima do máximo permitido após a remoção dos espaços externos é
   recusado com `VALIDATION_ERROR`, sem persistir o cadastro ou chamado.

### RN-003

1. Um chamado válido pode ser aberto sem técnico, retornando `technicianId` e `technicianName`
   nulos.
2. Um chamado não encerrado pode ser atribuído a um técnico existente, desde que a RN-001 seja
   respeitada; a resposta e a consulta posterior apresentam o responsável indicado.
3. Uma atribuição com identificador positivo de técnico inexistente é recusada com
   `404 Not Found` e `TECHNICIAN_NOT_FOUND`, preservando a atribuição anterior, quando houver.
4. Um comando de atribuição com `technicianId` ausente, nulo, zero ou negativo é recusado com
   `400 Bad Request` e `VALIDATION_ERROR`, sem alterar o responsável.
5. A reatribuição para outro técnico existente substitui o responsável, respeitando a RN-001
   e atualizando as contagens dos técnicos envolvidos quando o chamado for de alta urgência.
6. Uma tentativa de deixar um chamado atribuído sem responsável por envio de `technicianId`
   nulo é recusada; o técnico anterior permanece vinculado ao chamado.

### RN-004

1. Repetir a atribuição ao responsável atual de um chamado em `OPEN` ou `IN_PROGRESS` retorna
   `200 OK` e preserva o mesmo técnico, sem duplicar o vínculo ou aumentar sua carga.
2. Quando o técnico já possui dois chamados de alta urgência não encerrados, repetir a atribuição
   de um desses chamados ao mesmo técnico é aceito, sem retornar `HIGH_URGENCY_LIMIT`.
3. Após várias repetições da mesma atribuição, a consulta do técnico mantém a contagem original
   de chamados de alta urgência não encerrados.
4. Repetir a atribuição ao mesmo técnico em um chamado `CLOSED` é recusado com `409 Conflict`
   e `MAINTENANCE_REQUEST_CLOSED`, preservando o chamado encerrado.

### RN-005

1. Uma consulta com chamados de urgências diferentes apresenta todos os de `HIGH` antes dos de
   `MEDIUM`, e todos os de `MEDIUM` antes dos de `LOW`.
2. Um chamado de alta urgência aparece antes de outro de menor urgência, mesmo que este tenha
   sido criado mais recentemente.
3. Dentro de uma mesma urgência, chamados com datas de criação diferentes aparecem do mais
   recente para o mais antigo.
4. Quando dois chamados possuem a mesma urgência e a mesma data e horário de criação, o de
   maior identificador aparece primeiro.
5. A ordenação é preservada ao filtrar por status, por urgência ou pelos dois critérios juntos,
   considerando somente os chamados que atendem aos filtros.

### Qualidade e operação

1. A consulta de técnicos apresenta a contagem de chamados de alta urgência em `OPEN` ou
   `IN_PROGRESS`, sem incluir os encerrados.
2. Patrimônio e e-mail duplicados são recusados com os respectivos códigos de conflito.
3. Requisições diretas à API continuam sujeitas às mesmas validações e regras de negócio.
4. As respostas respeitam os formatos JSON, datas e envelope de erro definidos neste documento.
5. A interface apresenta status, urgências e mensagens ao usuário em português brasileiro.
6. Os dados permanecem disponíveis após reiniciar a stack preservando o volume do banco.
7. A configuração de produção publica somente a porta do frontend no host.
8. As verificações de frontend (`npm run format`, `npm test` e `npm run build`) e backend
   (`mvn test`) devem passar, conforme a [estratégia de testes](../qualidade/estrategia-testes.md).
9. O fluxo manual de cadastro, abertura, atribuição, recusa do terceiro chamado de alta urgência
   e encerramento deve ser validado antes da entrega oficial.
10. Não deve haver defeito crítico ou alto aberto relacionado à entrega.

Os critérios descrevem as verificações exigidas para o aceite; este documento não representa
evidência de que essas verificações já tenham sido executadas.
