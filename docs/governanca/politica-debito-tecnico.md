# Politica de gestao de debito tecnico

O DevOps e responsavel pelo acompanhamento da politica, com participacao dos desenvolvedores na
identificacao, estimativa e correcao dos debitos.

## Registro obrigatorio

Todo atalho tecnico, codigo temporario, necessidade de refatoracao, melhoria estrutural ou pendencia
deve ser registrado no backlog como `Debito Tecnico`. O registro deve conter:

- descricao do problema
- parte do sistema afetada
- motivo para a solucao definitiva nao ter sido aplicada
- impacto e risco para o projeto
- prioridade
- responsavel pela correcao
- criterio de aceite para encerramento

Comentarios no codigo e acordos verbais nao substituem o item no backlog. Um `TODO` consciente
deve referenciar o identificador do debito registrado.

## Priorizacao

- `Alta`: afeta seguranca, regra de negocio, funcionamento, dados ou bloqueia outro integrante.
- `Media`: dificulta manutencao, compreensao, testes ou evolucao do codigo.
- `Baixa`: melhoria de organizacao, legibilidade ou desempenho sem impacto imediato.

Qualquer debito que possa permitir mais de dois chamados de urgencia `Alta` abertos para o mesmo
tecnico tem prioridade maxima.

## Capacidade e pagamento

- A equipe reserva aproximadamente 15% do esforco de cada ciclo para debitos tecnicos.
- Debitos de prioridade alta devem entrar no ciclo atual ou bloquear a entrega afetada.
- Os demais debitos sao priorizados no refinamento conforme risco, impacto e custo de postergacao.
- Correcao emergencial deve gerar uma tarefa de solucao definitiva no ciclo atual ou no seguinte.

## Criterios de encerramento

- causa do debito removida
- codigo revisado
- testes relacionados atualizados e aprovados
- documentacao ajustada quando necessario
- item fechado com referencia ao pull request da correcao
