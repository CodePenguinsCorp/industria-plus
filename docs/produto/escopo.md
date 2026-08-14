# Escopo funcional

## Visao do produto

O Industria Plus e um helpdesk de manutencao industrial para apoiar o controle das manutencoes
preventivas e corretivas do parque fabril. O sistema deve reduzir paradas sem tratamento e dar
clareza sobre a urgencia dos chamados para a equipe tecnica.

## Entrega N1

O primeiro nivel do produto contempla:

- cadastro e consulta de equipamentos
- cadastro e consulta de setores
- abertura de chamados vinculados a um equipamento e setor
- definicao do nivel de urgencia do chamado
- consulta de chamados por status e urgencia
- atribuicao de chamados a tecnicos
- alteracao do status do chamado para permitir seu acompanhamento e encerramento

Cadastros, telas ou regras que nao contribuam diretamente para esses fluxos nao fazem parte do N1.

## Regra de negocio critica

`RN-001`: um tecnico nao pode ter mais de dois chamados com urgencia `Alta` abertos ao mesmo
tempo.

- A validacao deve ser executada no backend durante a atribuicao ou reatribuicao.
- Chamados encerrados nao contam para o limite.
- O terceiro chamado de urgencia alta deve ser recusado com uma mensagem de negocio clara.
- A validacao deve considerar concorrencia para impedir duas atribuicoes simultaneas acima do limite.
- O frontend deve orientar o usuario, mas nao substitui a validacao do backend.

## Cenarios de aceite da RN-001

1. Um tecnico sem chamados de urgencia alta pode receber um chamado de urgencia alta.
2. Um tecnico com um chamado de urgencia alta aberto pode receber o segundo.
3. Um tecnico com dois chamados de urgencia alta abertos nao pode receber o terceiro.
4. Depois que um dos chamados de urgencia alta for encerrado, o tecnico pode receber outro.
5. Chamados de urgencia diferente de alta nao entram nessa contagem.

## Evolucao N2/N3

As evolucoes previstas, fora do N1, sao:

- historico de troca de pecas por maquina
- calculo de MTBF, o tempo medio entre falhas
- agendamento automatico de manutencoes preventivas

Esses itens devem passar novamente pelo Definition of Ready antes de entrarem em desenvolvimento.

## Premissas a detalhar no refinamento

- dados obrigatorios de equipamentos, setores e tecnicos
- niveis de urgencia permitidos alem de `Alta`
- estados do ciclo de vida de um chamado
- permissoes para abertura, atribuicao, reatribuicao e encerramento
- criterio para diferenciar manutencao preventiva de corretiva
- regras de inativacao de equipamentos, setores e tecnicos com historico associado
