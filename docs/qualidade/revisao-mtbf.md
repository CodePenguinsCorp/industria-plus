# Revisão do MTBF por equipamento

## Regra acordada

O gestor informa horas reais de operação e datas inclusivas. Cada chamado corretivo aberto no período representa uma falha; preventivas não contam. `MTBF = horas / falhas`, com duas casas decimais. Sem medição, “Dados insuficientes”; sem falhas, mensagem própria sem MTBF zero ou infinito.

Gestor/administrador altera e perfis autenticados consultam. Há uma medição atual por equipamento. Salvar substitui a anterior. Não há telemetria nem histórico de medições nesta versão.

## Organização dos commits

1. Persistência: V5, campos do equipamento/ocorrência e teste dedicado de migração.
2. MTBF: serviço, API, permissão, painel e testes de cálculo/validação.
3. Fuso: runtime conteinerizado alinhado a `America/Sao_Paulo`.

A V5 prepara também as tabelas da preventiva, sem expor sua API neste PR. A migração já havia sido aplicada na demonstração; sua íntegra foi preservada para não alterar checksum nem exigir recriação dos dados. O PR seguinte utiliza o esquema existente sem reescrever V5.

## Rastreabilidade e testes

Requisito: MTBF do escopo e roteiro de releases; regras confirmadas pelo usuário em 24/11/2026. Testes em `EquipmentEvolutionIntegrationTest` e `equipment-maintenance.component.spec.ts`, além de `MySqlUpgradeIT`. Resumo de execução desta branch em `verificacao-mtbf.json`.

## Roteiro manual de QA

| Cenário | Resultado esperado |
| --- | --- |
| Equipamento sem medição. | Dados insuficientes. |
| Informar 100 horas em período com dois chamados corretivos. | 50 h. |
| Acrescentar preventiva no mesmo período. | Não aumenta a quantidade de falhas. |
| Chamados exatamente no início/fim do período e fora dele. | Limites inclusivos; dia seguinte excluído. |
| Medição válida em período sem falhas. | Sem falhas registradas, sem valor artificial de MTBF. |
| Horas zero, negativas, acima da duração, período invertido ou futuro. | Erro e nenhuma medição inválida persistida. |
| Técnico consulta e tenta alterar pela API. | Consulta permitida; alteração recusada. |
| Salvar outra medição e recarregar. | Medição atual substituída e persistida. |

QA registra resultado obtido, commit, ambiente, executor e evidência. PO confirma especialmente “um corretivo = uma falha” e a medição única substituível. Cobertura automática não representa aceite formal.
