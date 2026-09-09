# Escopo funcional

## Visão do produto

O Industria Plus é um helpdesk de manutenção industrial para apoiar o controle das manutenções
preventivas e corretivas do parque fabril. O sistema deve reduzir paradas sem tratamento e dar
clareza sobre a urgência dos chamados para a equipe técnica.

## Entrega N1

O primeiro nível do produto contempla:

- cadastro e consulta de equipamentos
- cadastro e consulta de setores
- abertura de chamados vinculados a um equipamento e setor
- definição do nível de urgência do chamado
- consulta de chamados por status e urgência
- atribuição de chamados a técnicos
- alteração do status do chamado para permitir seu acompanhamento e encerramento

Cadastros, telas ou regras que não contribuam diretamente para esses fluxos não fazem parte do N1.

## Regra de negócio crítica

`RN-001`: um técnico não pode ter mais de dois chamados com urgência `Alta` abertos ao mesmo
tempo.

- A validação deve ser executada no backend durante a atribuição ou reatribuição.
- Chamados encerrados não contam para o limite.
- O terceiro chamado de urgência alta deve ser recusado com uma mensagem de negócio clara.
- A validação deve considerar concorrência para impedir duas atribuições simultâneas acima do limite.
- O frontend deve orientar o usuário, mas não substitui a validação do backend.

## Cenários de aceite da RN-001

1. Um técnico sem chamados de urgência alta pode receber um chamado de urgência alta.
2. Um técnico com um chamado de urgência alta aberto pode receber o segundo.
3. Um técnico com dois chamados de urgência alta abertos não pode receber o terceiro.
4. Depois que um dos chamados de urgência alta for encerrado, o técnico pode receber outro.
5. Chamados de urgência diferente de alta não entram nessa contagem.

## Evolução N2/N3

As evoluções previstas, fora do N1, são:

- histórico de troca de peças por máquina
- cálculo de MTBF, o tempo médio entre falhas
- agendamento automático de manutenções preventivas

Esses itens devem passar novamente pelo Definition of Ready antes de entrarem em desenvolvimento.

## Premissas refinadas

Os pontos abaixo foram registrados inicialmente como pendências de refinamento e são preservados
como histórico do levantamento:

- dados obrigatórios de equipamentos, setores e técnicos
- níveis de urgência permitidos além de `Alta`
- estados do ciclo de vida de um chamado
- permissões para abertura, atribuição, reatribuição e encerramento
- critério para diferenciar manutenção preventiva de corretiva
- regras de inativação de equipamentos, setores e técnicos com histórico associado

As decisões adotadas para a implementação e o aceite do N1 estão formalizadas no
[refinamento funcional do N1](refinamento-n1.md). Esse refinamento complementa o escopo sem alterar
os requisitos originais nem antecipar funcionalidades de N2/N3.
