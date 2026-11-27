# Evolução N3: MTBF e preventivas

Regras de negócio confirmadas pelo usuário em 24/11/2026. Stack e padrões existentes preservados.

## MTBF

O gestor informa início e fim do período de observação e horas reais de operação. O backend conta
chamados corretivos abertos nesse mesmo período, por equipamento, e calcula horas de operação / número
de falhas. Para esta versão, cada chamado corretivo representa uma falha registrada; preventivos são
excluídos. As datas de observação são inclusivas e usam o calendário local da aplicação.

As decisões de implementação são: manter uma medição atual por equipamento; exigir período não
futuro e horas positivas que não excedam o total de horas do período; retornar MTBF nulo sem medição
ou sem falhas, distinguindo os estados na interface. Não inventar dados retroativos nem usar tempo
de calendário como se fosse operação medida. Apenas administrador e gestor alteram a medição;
todos os perfis autenticados podem consultar.

## Preventivas

O gestor define um plano por equipamento com título, descrição, periodicidade em dias e primeira
data. O backend gera um chamado PREVENTIVE quando o plano vence, com urgência MEDIUM, sem técnico,
registrando a origem automática no histórico. As regras de atendimento e atribuição permanecem.

Decisões de implementação: um plano atual por equipamento, editável e pausável; executar a
verificação a cada minuto e após salvar um plano; serializar a execução com lock no plano e chave
única (plano, data prevista). Ao recuperar períodos atrasados, gerar uma única ocorrência vencida e
avançar a próxima data pela cadência original até o futuro, sem uma enxurrada de chamados antigos.
Pausar não exclui chamados. Retomar não modifica a data sem uma alteração explícita do gestor.

## Abordagem

Usar JPA/Flyway e o agendador nativo do Spring. Não introduzir fila, serviço externo ou biblioteca
de scheduling. Reaproveitar as telas de detalhes do equipamento e os chamados/histórico existentes.
O volume de planos será pequeno, adequado ao projeto; falhas de geração são registradas e tentadas
novamente na próxima execução. Datas e cálculo terão testes determinísticos.

## Aceite

- 100 horas e duas falhas corretivas no período resultam em MTBF de 50 horas.
- Sem medição, sem falhas, intervalo invertido, data futura e horas impossíveis têm estados claros.
- Técnico não consegue alterar medições ou planos, inclusive diretamente na API.
- Plano futuro ou pausado não gera; plano vencido gera exatamente um chamado por ocorrência.
- Reexecução, concorrência e reinício não duplicam a ocorrência.
- Migração preserva os dados anteriores; comportamento validado em MySQL e no navegador.
