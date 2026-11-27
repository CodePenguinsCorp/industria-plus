# Revisão de atribuição e atualização dos chamados

## Escopo e planejamento

Dois commits independentes: corrigir a RN-001 sob concorrência real no MySQL e completar a exibição de `updatedAt` (RF12 / BL-N1-007). O limite continua sendo dois chamados de alta urgência não encerrados por técnico.

A atribuição usa `READ_COMMITTED` junto ao lock pessimista do técnico. Isso permite contar uma atribuição confirmada por outra transação enquanto a atual aguardava o lock. O defeito foi reproduzido anteriormente no MySQL com duas respostas aceitas; H2 sozinho não o revelou.

## Verificação técnica

Suíte completa de backend em H2 e em schema novo MySQL, mais frontend, formatação, build e Compose. As contagens e o commit testado estão em `verificacao-chamados.json`. Não são suítes de 184 cenários distintos: são 92 casos executados em dois bancos.

## Roteiro manual de QA

| Cenário | Resultado esperado |
| --- | --- |
| Técnico com um HIGH recebe duas atribuições HIGH simultâneas. | Uma resposta 200; outra 409 com `HIGH_URGENCY_LIMIT`; carga final dois. |
| Reatribuir para técnico já com dois HIGH. | Recusa e preservação do responsável anterior e das cargas. |
| Encerrar um HIGH e atribuir outro. | Vaga liberada e nova atribuição aceita. |
| Atribuir LOW/MEDIUM quando carga HIGH já é dois. | Operação aceita sem aumentar a contagem HIGH. |
| Consultar chamado, atribuir técnico e alterar status. | Data de criação preservada e “Atualizado em” coerente em cada consulta da interface. |
| Tentar atribuição ou status em chamado encerrado. | Recusa sem alteração persistida. |

Os cenários têm cobertura automatizada conforme o resumo. O roteiro manual integrado e o aceite do PO/QA continuam pendentes; registrar commit, ambiente, executor e evidência.
