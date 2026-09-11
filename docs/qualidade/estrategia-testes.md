# Estratégia de testes

O QA coordena a estratégia. O foco do N1 é proteger as regras de negócio e os fluxos de cadastro, abertura, priorização, atribuição, alteração de status e encerramento de chamados.

A estratégia deve validar tanto o comportamento da aplicação pela interface quanto as regras aplicadas diretamente pela API. As validações do backend não dependem do frontend e devem ser mantidas mesmo quando uma requisição é enviada diretamente à API.

## Níveis de teste

- `Frontend`: testes de componentes, formulários, serviços e estados de interface com Vitest.
- `Backend unitário`: regras de negócio isoladas com JUnit e Mockito.
- `Backend integração`: endpoints, persistência, filtros, ordenação, transições e migrations com Spring Boot Test e MockMvc.
- `Integração da aplicação`: comunicação entre Angular, API e MySQL no ambiente Docker, incluindo persistência do volume e funcionamento do Nginx.
- `Concorrência`: testes da RN-001 utilizando MySQL para garantir que atribuições simultâneas não ultrapassem dois chamados de alta urgência por técnico.
- `Manual`: roteiro de aceite executado pelo QA antes de cada entrega oficial.

## Prioridades do N1

- cadastro e consulta de setores
- cadastro e consulta de equipamentos
- cadastro e consulta de técnicos
- abertura de chamados e definição do tipo de manutenção
- definição da urgência do chamado
- validação da relação entre equipamento e setor
- consulta, filtragem e ordenação da fila de chamados
- atribuição e reatribuição de técnicos
- contagem dos chamados de urgência alta abertos por técnico
- alteração do status e encerramento de chamados
- bloqueio de alterações em chamados encerrados
- validação de textos obrigatórios e limites dos campos
- recusas da RN-001, inclusive em atribuições concorrentes
- persistência dos dados após reinicialização ou recriação dos containers com o volume preservado
- padronização dos envelopes e códigos de erro

A fila deve ser validada com prioridade `HIGH`, `MEDIUM` e `LOW`, seguida pela ordenação por `createdAt` decrescente e, em caso de empate, pelo maior identificador.

## Regras de negócio prioritárias

### RN-001 — Limite de alta urgência

Um técnico pode possuir no máximo dois chamados simultaneamente quando o chamado possuir:

- `urgency = HIGH`;
- `status = OPEN` ou `IN_PROGRESS`.

Chamados `CLOSED`, `LOW` ou `MEDIUM` não entram na contagem. A validação deve ocorrer dentro de uma transação, com bloqueio pessimista sobre o técnico de destino.

### RN-002 — Validação de textos

Textos obrigatórios devem ser validados após a remoção de espaços no início e no fim. Textos contendo somente espaços devem ser recusados. Os limites mínimo e máximo devem ser aplicados após essa normalização.

### RN-003 — Técnico obrigatório na atribuição

Toda atribuição ou reatribuição deve indicar um técnico existente. A remoção do responsável sem indicar outro técnico não é permitida no N1.

### RN-004 — Repetição da atribuição

Atribuir novamente um chamado não encerrado ao técnico que já é seu responsável deve preservar a atribuição sem aumentar sua carga ou contabilizar novamente o chamado.

### RN-005 — Ordenação da fila

A fila deve apresentar primeiro chamados `HIGH`, depois `MEDIUM` e por último `LOW`. Dentro de cada nível, os chamados mais recentes devem aparecer primeiro; em empate de `createdAt`, o maior identificador deve aparecer primeiro.

## Roteiro manual obrigatório

1. Cadastrar um setor.
2. Cadastrar um equipamento vinculado ao setor.
3. Cadastrar um técnico com e-mail válido.
4. Abrir um chamado para o equipamento e o setor cadastrados.
5. Definir o tipo de manutenção e a urgência do chamado como `HIGH`.
6. Confirmar que o chamado foi criado como `OPEN` e sem técnico.
7. Atribuir o chamado a um técnico.
8. Abrir e atribuir um segundo chamado de urgência `HIGH` ao mesmo técnico.
9. Tentar atribuir um terceiro chamado de urgência `HIGH` ao mesmo técnico.
10. Confirmar que o sistema recusa a terceira atribuição com `409 Conflict` e `HIGH_URGENCY_LIMIT`.
11. Encerrar um dos chamados de urgência alta.
12. Confirmar que a carga do técnico foi reduzida e que ele pode receber outro chamado de urgência alta.
13. Reatribuir um chamado para outro técnico com capacidade disponível.
14. Repetir a atribuição de um chamado ao mesmo técnico responsável e confirmar que sua carga não aumenta.
15. Tentar atribuir ou alterar um chamado `CLOSED` e confirmar a recusa.
16. Abrir chamados com diferentes urgências e verificar a ordenação da fila.
17. Aplicar filtros por status, por urgência e pelos dois simultaneamente.
18. Disparar duas atribuições concorrentes para um técnico que já possui um chamado de urgência alta aberto.
19. Confirmar que somente uma atribuição é aceita.
20. Confirmar que a outra retorna `409 Conflict` com `HIGH_URGENCY_LIMIT`.
21. Confirmar que a contagem persistida permanece em dois.
22. Reiniciar ou recriar os containers preservando o volume do banco.
23. Confirmar que os registros cadastrados e os vínculos permanecem disponíveis.

Os testes de concorrência devem ser realizados com MySQL; um teste equivalente executado somente em H2 não substitui a evidência exigida para esse cenário.

## Validações e recusas obrigatórias

Devem ser testados, no mínimo:

- campos obrigatórios ausentes;
- campos obrigatórios vazios;
- textos contendo somente espaços;
- textos abaixo do limite mínimo;
- textos acima do limite máximo;
- patrimônio de equipamento duplicado;
- e-mail de técnico duplicado;
- setor inexistente;
- equipamento inexistente;
- técnico inexistente;
- equipamento pertencente a setor diferente do informado no chamado;
- atribuição com `technicianId` ausente, nulo, zero ou negativo;
- tentativa de remover a atribuição sem indicar outro técnico;
- transição de status inválida;
- tentativa de alterar chamado encerrado;
- tentativa de ultrapassar o limite de alta urgência.

Os critérios de validação e os comportamentos esperados estão definidos nos cenários de aceite da N1.

## Códigos de erro

Os testes da API devem verificar não somente o status HTTP, mas também o `code` retornado pelo backend. O código é estável e deve ser utilizado pelo frontend para decisões de comportamento.

### `400 Bad Request`

- `VALIDATION_ERROR`: campo ausente, vazio, malformado ou fora dos limites.
- `INVALID_PARAMETER`: enumeração desconhecida ou parâmetro incompatível.
- `MALFORMED_REQUEST`: JSON ausente, inválido ou com valor incompatível.

### `404 Not Found`

- `SECTOR_NOT_FOUND`
- `EQUIPMENT_NOT_FOUND`
- `TECHNICIAN_NOT_FOUND`
- `MAINTENANCE_REQUEST_NOT_FOUND`

### `409 Conflict`

- `DUPLICATE_ASSET_TAG`
- `DUPLICATE_TECHNICIAN_EMAIL`
- `EQUIPMENT_SECTOR_MISMATCH`
- `INVALID_STATUS_TRANSITION`
- `MAINTENANCE_REQUEST_CLOSED`
- `HIGH_URGENCY_LIMIT`
- `DATA_CONFLICT`

### `500 Internal Server Error`

- `INTERNAL_ERROR`

Os códigos e suas respectivas utilizações devem permanecer consistentes com o contrato definido para a API do N1.

## Envelope de erro

As falhas tratadas devem retornar o envelope padronizado contendo:

- `timestamp`
- `status`
- `error`
- `code`
- `message`
- `path`
- `fieldErrors`

`fieldErrors` deve identificar os campos inválidos em erros de validação e permanecer vazio quando o erro não estiver relacionado a campos específicos.

## Gate mínimo por pull request

1. `npm run format`, `npm test` e `npm run build` no frontend.
2. `mvn test` no backend.
3. Testes novos ou atualizados para toda mudança de comportamento.
4. Testes específicos das regras de negócio afetadas pela alteração.
5. Evidência do fluxo principal validado quando houver alteração visual.
6. Quando aplicável, evidência de testes diretamente na API, sem depender do frontend.
7. Nenhum defeito crítico ou alto aberto relacionado à entrega.

O aceite da N1 exige que os comandos de frontend e backend sejam executados sem falhas pendentes.

## Registro de defeitos

Todo bug deve ser registrado no backlog com título, descrição, passos para reprodução, resultado esperado, resultado obtido, evidências, severidade e responsável.

- `Crítico`: interrompe o sistema ou viola uma regra essencial, incluindo a RN-001, integridade dos dados ou bloqueios que permitam operações proibidas.
- `Alto`: compromete uma funcionalidade importante sem impedir todo o uso.
- `Médio`: causa comportamento incorreto, mas possui contorno.
- `Baixo`: problema visual, textual ou de pequeno impacto.

Bugs críticos ou altos bloqueiam a conclusão da funcionalidade afetada.

## Evidências de teste

Para cada cenário de aceite executado devem ser registrados:

- identificador do cenário;
- commit utilizado;
- ambiente;
- data;
- responsável;
- resultado;
- evidência correspondente.

Cenários não executados devem permanecer pendentes e não podem ser considerados aprovados. Nas operações aceitas, deve ser conferida a resposta e realizada nova consulta para confirmar a persistência. Nas operações recusadas, deve ser confirmado que nenhuma criação ou alteração indevida ocorreu.
