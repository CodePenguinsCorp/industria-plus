# Checklist de atendimento aos requisitos da N1

## Objetivo e base da análise

Esta checklist relaciona os 18 requisitos funcionais e os 10 requisitos não funcionais da
[Premissa Inicial N1](../produto/premissa-inicial%20N1.md) às evidências disponíveis no projeto.
A análise considera o código, os testes existentes e as configurações de infraestrutura.

**Tipo de verificação:** inspeção estática. As suítes de testes, o navegador e a stack Docker
não foram executados para produzir este documento. A existência de um teste não significa
que sua execução atual tenha sido aprovada.

- `[x]`: implementação ou configuração correspondente identificada e revisada.
- `[ ]`: atendimento parcial ou comprovação operacional ainda pendente, conforme a descrição.

Uma marcação de implementação não substitui o aceite do QA. O roteiro de validação ao final
permanece pendente até que sejam registradas evidências de execução.

## Requisitos Funcionais

- [x] **RF01 — Cadastrar setores.** O formulário solicita nome e descrição opcional.
  `SectorRequest` remove espaços externos e valida obrigatoriedade e tamanhos;
  `SectorService.create` persiste o cadastro.
  Evidências: [backend de setores][sectors] e [interface de setores][sector-ui].

- [x] **RF02 — Consultar setores.** O endpoint de consulta retorna identificador, nome e
  descrição; a interface carrega e apresenta os setores cadastrados.
  Evidências: [backend de setores][sectors] e [interface de setores][sector-ui].

- [x] **RF03 — Cadastrar equipamentos.** O formulário e o DTO exigem patrimônio, nome e setor.
  O serviço verifica patrimônio duplicado e existência do setor; o banco possui restrições
  de unicidade e chave estrangeira.
  Evidências: [backend de equipamentos][equipment], [interface de equipamentos][equipment-ui]
  e [migration do domínio][migration].

- [x] **RF04 — Consultar equipamentos.** A consulta retorna patrimônio, nome, descrição e
  setor associado, apresentados na listagem da interface.
  Evidências: [backend de equipamentos][equipment] e [interface de equipamentos][equipment-ui].

- [x] **RF05 — Cadastrar técnicos.** O cadastro valida nome, e-mail e especialidade opcional.
  O serviço normaliza o e-mail e recusa duplicidades; a migration também define unicidade.
  Evidências: [backend de técnicos][technicians], [interface de técnicos][technician-ui]
  e [migration do domínio][migration].

- [x] **RF06 — Consultar técnicos e carga.** O serviço calcula `highUrgencyOpenRequests`
  considerando urgência `HIGH` e status diferente de `CLOSED`. A tela apresenta a carga de
  cada técnico como quantidade de chamados abertos sobre o limite de dois.
  Evidências: [serviço de técnicos][technician-service], [repositório de chamados][request-repository]
  e [interface de técnicos][technician-ui].

- [x] **RF07 — Abrir chamados.** O formulário envia título, descrição, equipamento, setor,
  tipo e urgência. O DTO valida a entrada e o serviço persiste o chamado.
  Evidências: [domínio de chamados][requests] e [componente de chamados][request-ui].

- [x] **RF08 — Classificar manutenção.** A interface oferece preventiva e corretiva;
  `MaintenanceType` e a restrição do banco delimitam os valores aceitos.
  Evidências: [domínio de chamados][requests], [componente de chamados][request-ui]
  e [migration do domínio][migration].

- [x] **RF09 — Definir urgência.** A abertura oferece baixa, média e alta, mapeadas para
  `LOW`, `MEDIUM` e `HIGH`. O backend exige um valor de `Urgency`.
  Evidências: [domínio de chamados][requests] e [componente de chamados][request-ui].

- [x] **RF10 — Validar equipamento e setor.** A abertura consulta as referências e compara
  o setor do equipamento com o informado. Referências inexistentes geram erro de recurso não
  encontrado; divergências geram `EQUIPMENT_SECTOR_MISMATCH`.
  Evidência: [serviço de chamados][request-service].

- [x] **RF11 — Inicializar chamado.** O construtor define `OPEN`, o técnico inicia nulo e
  `@CreationTimestamp` gera a data de criação. O DTO de abertura não oferece esses campos.
  Evidências: [entidade do chamado][request-entity] e [domínio de chamados][requests].

- [ ] **RF12 — Consultar todos os dados do chamado. Atendimento parcial na interface.**
  A API retorna todos os campos exigidos, incluindo `createdAt` e `updatedAt`. A tela apresenta
  identificação, título, descrição, equipamento, setor, tipo, urgência, status, responsável e
  criação, mas não exibe `updatedAt`. Para considerar o atendimento completo pela interface,
  falta apresentar a data de atualização e validar sua exibição após uma alteração.
  Evidências: [resposta da API][request-response] e [template de chamados][request-template].

- [x] **RF13 — Filtrar chamados.** A interface envia filtros opcionais; a consulta do
  repositório combina status e urgência com `and`, aceitando também cada filtro isoladamente.
  Evidências: [repositório de chamados][request-repository] e [componente de chamados][request-ui].

- [x] **RF14 — Ordenar a fila.** O serviço ordena por prioridade decrescente, data de criação
  decrescente e identificador decrescente no desempate. A interface mantém a ordem recebida.
  Evidências: [serviço de chamados][request-service] e [template de chamados][request-template].

- [x] **RF15 — Atribuir e reatribuir.** O comando de atribuição consulta o chamado e o técnico,
  verifica as regras e salva o responsável. A interface permite selecionar o técnico.
  Evidências: [serviço de chamados][request-service] e [componente de chamados][request-ui].

- [x] **RF16 — Limitar alta urgência por técnico.** O serviço recusa uma nova atribuição de
  alta urgência quando o destino já possui dois chamados não encerrados, retornando
  `HIGH_URGENCY_LIMIT`. A interface apresenta a mensagem da API.
  Evidências: [serviço de chamados][request-service] e [componente de chamados][request-ui].

- [x] **RF17 — Controlar transições.** A matriz aceita apenas `OPEN → IN_PROGRESS`,
  `OPEN → CLOSED` e `IN_PROGRESS → CLOSED`; o serviço a consulta antes de salvar.
  Evidências: [status do chamado][request-status] e [serviço de chamados][request-service].

- [x] **RF18 — Preservar encerramento.** O serviço recusa atribuição, reatribuição e mudança
  de status de chamados `CLOSED`. A interface não oferece as ações nesses chamados.
  Evidências: [serviço de chamados][request-service] e [template de chamados][request-template].

## Requisitos Não Funcionais

- [x] **RNF01 — Consistência sob concorrência: mecanismo implementado.** O serviço utiliza
  `@Transactional` e bloqueios pessimistas no chamado e no técnico de destino; contagem e
  gravação pertencem à mesma transação. Existe cenário automatizado de atribuições concorrentes.
  Evidências: [serviço][request-service], [repositório de chamados][request-repository],
  [repositório de técnicos][technician-repository] e [testes de atribuição][assignment-tests].
  A comprovação com MySQL permanece no roteiro operacional: os testes existentes usam H2.

- [x] **RNF02 — Validação no backend.** Controllers aplicam `@Valid` aos corpos de entrada;
  DTOs validam os campos e serviços verificam referências, unicidade e regras dos chamados.
  As verificações não dependem de executar a interface.
  Evidências: [backend de setores][sectors], [equipamentos][equipment], [técnicos][technicians]
  e [chamados][requests].

- [ ] **RNF03 — Persistência após reinício: configuração presente, comprovação pendente.**
  O backend usa MySQL e os arquivos Compose montam `mysql_data` em `/var/lib/mysql`.
  Falta executar e registrar a recuperação dos mesmos dados após reiniciar ou recriar os
  contêineres preservando o volume.
  Evidências: [configuração do backend][application], [Compose local][compose]
  e [Compose de produção][compose-prod].

- [x] **RNF04 — Formato de comunicação: implementação identificada.** Os controllers REST
  retornam DTOs serializados em JSON pelo Spring/Jackson. Datas de domínio usam `LocalDateTime`
  e o envelope de erro usa `OffsetDateTime`, compatíveis com representação ISO 8601.
  Evidências: [resposta de chamado][request-response] e [tratamento de erros][errors].
  Validar na execução os caracteres acentuados em UTF-8 e o formato retornado. As datas de
  domínio não incluem deslocamento de fuso; ISO 8601 não implica que todos os campos usem UTC.

- [x] **RNF05 — Envelope de erros.** `RestExceptionHandler` centraliza falhas de validação,
  negócio, parâmetros, JSON e integridade, retornando `ApiError` com código e `fieldErrors`.
  Evidências: [tratamento de erros][errors] e [testes da API de chamados][request-tests].

- [x] **RNF06 — Português brasileiro.** Formulários, rótulos de urgência/status e mensagens
  de negócio revisados estão em português; o frontend traduz os valores enumerados da API.
  Evidências: [interfaces funcionais][features], [componente de chamados][request-ui]
  e [tratamento de erros][errors]. A revisão visual completa permanece no roteiro de aceite.

- [x] **RNF07 — Ocultação de detalhes internos.** O tratamento de exceções inesperadas
  registra detalhes no log do servidor e devolve `INTERNAL_ERROR` com mensagem genérica.
  Existe teste específico que verifica a resposta sem a mensagem interna da exceção.
  Evidências: [tratamento de erros][errors] e [teste do tratador de erros][error-tests].

- [x] **RNF08 — Isolamento em produção: configuração identificada.** Somente o frontend
  possui `ports` no Compose de produção; backend e MySQL usam a rede interna. Nginx encaminha
  `/api/` para o backend. O Compose local publica outras portas para desenvolvimento.
  Evidências: [Compose de produção][compose-prod] e [Nginx][nginx].

- [ ] **RNF09 — Reprodutibilidade: estrutura presente, execução pendente.** Há builds no
  Compose local, imagens configuráveis em produção, variáveis de ambiente e migrations Flyway.
  Falta registrar uma subida em ambiente limpo e confirmar aplicação das migrations e
  comunicação entre os três serviços.
  Evidências: [Compose local][compose], [Compose de produção][compose-prod],
  [configuração do backend][application] e [migration do domínio][migration].

- [x] **RNF10 — Organização por domínio.** Cadastros e chamados possuem controllers, serviços,
  repositórios e DTOs próprios. Controllers delegam casos de uso; os serviços concentram as
  verificações de negócio e acessam os repositórios. O frontend agrupa telas por funcionalidade.
  Evidências: [backend][backend] e [funcionalidades do frontend][features].

## Testes existentes relacionados

Os arquivos abaixo foram localizados durante a inspeção. Sua presença indica cobertura
implementada para determinados cenários, não execução aprovada nem cobertura integral do requisito.

| Arquivo | Cenários relacionados |
| --- | --- |
| [CatalogApiIntegrationTest][catalog-tests] | Criação e consulta dos cadastros, normalização, duplicidade de patrimônio/e-mail e limites mínimos após remoção de espaços. |
| [MaintenanceRequestApiIntegrationTest][request-tests] | Abertura, consulta, filtros, prioridade, setor incompatível, textos, transições, encerramento e erros de entrada. |
| [MaintenanceRequestAssignmentIntegrationTest][assignment-tests] | Limite de alta urgência, reatribuição recusada, repetição ao mesmo técnico e atribuições concorrentes. |
| [RestExceptionHandlerTest][error-tests] | Ocultação de detalhes de exceções inesperadas. |
| [sector.component.spec.ts][sector-tests] | Formulário inválido, normalização e inclusão do setor na lista. |
| [maintenance-request.component.spec.ts][request-ui-tests] | Validação de abertura, envio normalizado e apresentação da recusa da RN-001. |
| [api-error.service.spec.ts][api-error-tests] | Mensagens de campos, conflitos de negócio e indisponibilidade do backend. |

O [perfil de testes do backend][test-profile] utiliza H2 em modo de compatibilidade MySQL.
Isso não comprova o comportamento de bloqueios, persistência ou migrations em um MySQL real.

## Checklist de validação para o aceite

- [ ] Executar `mvn test` em `backend/` e registrar o resultado e a revisão do código testada.
- [ ] Executar `npm test -- --watch=false` e `npm run build` em `frontend/` e registrar os resultados.
- [ ] Validar os arquivos Compose com `docker compose config --quiet` e
  `docker compose --env-file .env.prod.example -f compose.prod.yaml config --quiet`, na raiz.
- [ ] Em ambiente de teste com volume novo, subir a stack e confirmar migrations, API e interface.
- [ ] Cadastrar e consultar setor, equipamento e técnico; conferir campos obrigatórios,
  limites de tamanho, duplicidades e referências inexistentes (RF01–RF06 e RNF02).
- [ ] Abrir chamados preventivos e corretivos nas três urgências, conferindo os dados iniciais
  e a rejeição de equipamento/setor incompatíveis (RF07–RF11).
- [ ] Resolver a ausência de `updatedAt` na tela e conferir todos os dados consultáveis (RF12).
- [ ] Conferir filtros isolados e combinados, prioridade, ordem cronológica e desempate por ID
  com dados controlados (RF13–RF14).
- [ ] Atribuir, reatribuir, repetir a mesma atribuição e executar todas as transições permitidas
  e recusadas, incluindo chamados encerrados (RF15–RF18).
- [ ] No MySQL, tentar duas atribuições simultâneas de alta urgência para um técnico que já
  possui uma: conferir uma aceitação, uma recusa e contagem final igual a dois (RNF01).
- [ ] Reiniciar e recriar os contêineres de teste preservando o volume; conferir os mesmos IDs
  e vínculos cadastrados anteriormente, sem usar remoção de volumes (RNF03).
- [ ] Conferir JSON com acentos, datas ISO 8601, códigos e campos do envelope de erros;
  revisar idioma das telas e ausência de detalhes internos nas respostas (RNF04–RNF07).
- [ ] Conferir no ambiente de produção de teste que somente o frontend publica porta no host
  e que a interface acessa a API pelo Nginx (RNF08).
- [ ] Registrar a reprodução do ambiente e revisar a separação das responsabilidades nas
  alterações futuras (RNF09–RNF10).

Para cada validação executada, registrar data, responsável, commit, ambiente, resultado e
evidência. Falhas devem ser registradas conforme a [estratégia de testes](estrategia-testes.md).
Esta checklist mantém o escopo da N1; autenticação e evoluções N2/N3 não são exigidas aqui.

[backend]: ../../backend/src/main/java/com/industriaplus/backend/
[sectors]: ../../backend/src/main/java/com/industriaplus/backend/sector/
[equipment]: ../../backend/src/main/java/com/industriaplus/backend/equipment/
[technicians]: ../../backend/src/main/java/com/industriaplus/backend/technician/
[requests]: ../../backend/src/main/java/com/industriaplus/backend/maintenancerequest/
[request-service]: ../../backend/src/main/java/com/industriaplus/backend/maintenancerequest/MaintenanceRequestService.java
[request-repository]: ../../backend/src/main/java/com/industriaplus/backend/maintenancerequest/MaintenanceRequestRepository.java
[request-entity]: ../../backend/src/main/java/com/industriaplus/backend/maintenancerequest/MaintenanceRequest.java
[request-response]: ../../backend/src/main/java/com/industriaplus/backend/maintenancerequest/MaintenanceRequestResponse.java
[request-status]: ../../backend/src/main/java/com/industriaplus/backend/maintenancerequest/MaintenanceRequestStatus.java
[technician-service]: ../../backend/src/main/java/com/industriaplus/backend/technician/TechnicianService.java
[technician-repository]: ../../backend/src/main/java/com/industriaplus/backend/technician/TechnicianRepository.java
[errors]: ../../backend/src/main/java/com/industriaplus/backend/error/RestExceptionHandler.java
[features]: ../../frontend/src/app/features/
[sector-ui]: ../../frontend/src/app/features/sector/
[equipment-ui]: ../../frontend/src/app/features/equipment/
[technician-ui]: ../../frontend/src/app/features/technician/
[request-ui]: ../../frontend/src/app/features/maintenance-request/maintenance-request.component.ts
[request-template]: ../../frontend/src/app/features/maintenance-request/maintenance-request.component.html
[migration]: ../../backend/src/main/resources/db/migration/V2__create_n1_domain.sql
[application]: ../../backend/src/main/resources/application.yml
[test-profile]: ../../backend/src/test/resources/application.yml
[compose]: ../../compose.yaml
[compose-prod]: ../../compose.prod.yaml
[nginx]: ../../frontend/nginx.conf
[catalog-tests]: ../../backend/src/test/java/com/industriaplus/backend/CatalogApiIntegrationTest.java
[request-tests]: ../../backend/src/test/java/com/industriaplus/backend/MaintenanceRequestApiIntegrationTest.java
[assignment-tests]: ../../backend/src/test/java/com/industriaplus/backend/MaintenanceRequestAssignmentIntegrationTest.java
[error-tests]: ../../backend/src/test/java/com/industriaplus/backend/RestExceptionHandlerTest.java
[sector-tests]: ../../frontend/src/app/features/sector/sector.component.spec.ts
[request-ui-tests]: ../../frontend/src/app/features/maintenance-request/maintenance-request.component.spec.ts
[api-error-tests]: ../../frontend/src/app/core/services/api-error.service.spec.ts
