# Revisão dos planos preventivos

## Regra acordada

Um plano por equipamento, com título, descrição, intervalo de 1 a 3650 dias, próxima data e estado ativo/pausado. Gestor/administrador configura; técnico consulta. A verificação ocorre a cada minuto enquanto o backend está ligado e ao salvar plano vencido.

Gera chamado PREVENTIVE/MEDIUM/OPEN, sem técnico, com evento de “Agendamento automático”. Uma ocorrência não pode ser duplicada por reexecução ou concorrência. Se várias datas estiverem atrasadas, gera uma ocorrência pendente e avança na cadência original até uma data futura. Pausar preserva os chamados anteriores.

## Organização dos commits

1. Geração e configuração: entidade, API, serviço, agendador, painel e testes.
2. Atualização da tela: após salvar, recarregar lista/histórico do equipamento e testar a comunicação entre componentes.

A V5 vem íntegra do PR de MTBF e não é alterada aqui. O schema, lock e restrição única `(plano, data prevista)` sustentam a proteção contra duplicação.

## Verificação técnica

Resumo em `verificacao-preventivas.json`: backend H2/MySQL, frontend, formatação, build e Compose. Testes cobrem vencimento, futuro, pausa/retomada, leitura/configuração por papel e geração concorrente de uma única ocorrência. O código final foi comparado ao snapshot validado antes da separação para evitar perda de alterações.

## Roteiro manual de QA

| Cenário | Resultado esperado |
| --- | --- |
| Salvar plano com data futura. | Nenhum chamado antecipado. |
| Salvar plano vencido ativo. | Um chamado com tipo, urgência, status, data prevista e autor corretos; próxima data futura. |
| Reexecutar a mesma ocorrência. | Não duplicar chamado nem evento de histórico. |
| Gerar simultaneamente a mesma ocorrência. | Uma única criação persistida. |
| Pausar plano vencido e depois retomar. | Pausado não gera; retomada respeita a data configurada. |
| Manter backend ligado até vencer. | Agendador cria ocorrência sem intervenção do usuário. |
| Consultar equipamento após salvar plano vencido. | Chamado e histórico aparecem sem recarregar manualmente a página. |
| Técnico tenta configurar. | Recusa pela API, sem alteração. |

Ainda pendentes: falha artificial de infraestrutura para provar recuperação, roteiro manual completo em todos os perfis e aceite formal. Não há múltiplos planos, horímetro, notificação ou atribuição automática no escopo acordado.
