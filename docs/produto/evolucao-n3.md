# Evolução N3 — confiabilidade e preventivas

Regras confirmadas pelo usuário em 24/11/2026; implementação organizada nos PRs de confiabilidade e preventivas; ver [revisão de PO/QA](../qualidade/revisao-po-qa.md).
O aceite formal da equipe e do PO continua necessário.

## Histórias e critérios de aceite

### US-N3-01 — MTBF por equipamento

Como gestor, quero informar as horas reais de operação de uma máquina em um período para avaliar
seu tempo médio entre falhas.

- Informar datas inicial e final inclusivas e horas positivas com até duas casas decimais.
- O período não pode terminar no futuro nem possuir horas superiores à sua duração total.
- Contar como falha cada chamado `CORRECTIVE` aberto para o equipamento nesse período,
  independentemente do encerramento; chamados preventivos não contam.
- Calcular `MTBF = horas reais de operação / quantidade de falhas`, arredondado a duas casas.
- Sem medição: “Dados insuficientes”. Com medição e zero falhas: “Sem falhas registradas”, sem
  apresentar zero ou infinito como MTBF.
- A medição atual pode ser substituída pelo gestor/administrador. Todos os perfis autenticados
  consultam; técnico não altera nem pela API.
- O aplicativo não mede horas por sensores: o valor deve vir de registros reais da operação.

Endpoints: `GET` e `PUT /api/equipments/{id}/reliability`.
Corpo: `startDate`, `endDate`, `operatingHours`. Resposta inclui `failureCount`, `mtbfHours` e
`state` (`INSUFFICIENT_DATA`, `NO_FAILURES` ou `CALCULATED`).

### US-N3-02 — agendamento automático

Como gestor, quero configurar a periodicidade de uma preventiva para que o sistema abra chamados
automaticamente quando chegar a data prevista.

- Um plano por equipamento, com título, serviço previsto, intervalo de 1 a 3650 dias, próxima data
  e situação ativo/pausado. Apenas gestor/administrador modifica.
- Verificar a cada minuto; ao salvar um plano já vencido, verificar imediatamente.
- Gerar chamado `PREVENTIVE`, `MEDIUM`, `OPEN`, sem técnico, com autor de histórico
  “Agendamento automático” e a data originalmente prevista.
- Bloquear duplicidade da ocorrência por lock transacional e unicidade `(plano, data prevista)`.
- Se houver várias datas atrasadas, gerar uma ocorrência pendente e avançar pela cadência original
  até a primeira data futura. Não abrir uma fila retroativa inteira.
- Pausar não exclui chamados anteriores. Retomar usa a data configurada.
- Falhas transacionais não avançam a data nem mantêm um chamado parcialmente salvo; o próximo
  ciclo tenta novamente. O cenário de falha artificial de infraestrutura ainda não foi exercitado.
- Manter o backend ligado para executar o agendador. Após reiniciar, recuperar o vencimento pendente.

Endpoints: `GET` e `PUT /api/equipments/{id}/preventive-plan`. `GET` sem plano retorna `204`.
Corpo/resposta: `title`, `description`, `intervalDays`, `nextDueDate`, `enabled`; resposta inclui `id`.

## Modelo e decisões técnicas

V5 adiciona os campos de observação ao equipamento, `preventive_plans` e a identificação da
ocorrência nos chamados. V1 a V4 foram preservadas. Datas locais seguem `America/Sao_Paulo` na
JVM/container; instalações antigas precisam confirmar o fuso dos dados de origem.

A transação de atribuição usa `READ_COMMITTED` junto ao lock do técnico. Isso garante que a
contagem de chamados enxergue a atribuição confirmada enquanto uma operação concorrente aguardava
o lock. No MySQL padrão `REPEATABLE_READ`, o teste reproduziu duas aceitações acima do limite.

## Evidências de validação

- Suíte de 99 testes do backend aprovada tanto em H2 quanto em MySQL 8.4.
- Teste dedicado `MySqlUpgradeIT`: N1 com equipamento, técnico e chamado existentes migrada até
  V5 sem perder o vínculo entre os registros.
- 38 testes do frontend, formatação e build aprovados.
- Interface: 100 horas de dados fictícios / duas falhas = 50 h; plano vencido gera chamado e
  atualiza a lista/histórico na mesma tela.
- Dois geradores concorrentes criam uma única ocorrência em MySQL; períodos futuros/pausados
  não geram. Os limites inclusivos das datas de MTBF e período futuro também foram verificados.

As evidências completas e limitações estão em [relatório da Etapa 3](../qualidade/relatorio-etapa-3.md).
