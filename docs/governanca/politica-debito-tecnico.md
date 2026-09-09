# Política de gestão de débito técnico

O DevOps é responsável pelo acompanhamento da política, com participação dos desenvolvedores na
identificação, estimativa e correção dos débitos.

## Registro obrigatório

Todo atalho técnico, código temporário, necessidade de refatoração, melhoria estrutural ou pendência
deve ser registrado no backlog como `Débito Técnico`. O registro deve conter:

- descrição do problema
- parte do sistema afetada
- motivo para a solução definitiva não ter sido aplicada
- impacto e risco para o projeto
- prioridade
- responsável pela correção
- critério de aceite para encerramento

Comentários no código e acordos verbais não substituem o item no backlog. Um `TODO` consciente
deve referenciar o identificador do débito registrado.

## Priorização

- `Alta`: afeta segurança, regra de negócio, funcionamento, dados ou bloqueia outro integrante.
- `Média`: dificulta manutenção, compreensão, testes ou evolução do código.
- `Baixa`: melhoria de organização, legibilidade ou desempenho sem impacto imediato.

Qualquer débito que possa permitir mais de dois chamados de urgência `Alta` abertos para o mesmo
técnico tem prioridade máxima.

## Capacidade e pagamento

- A equipe reserva aproximadamente 15% do esforço de cada ciclo para débitos técnicos.
- Débitos de prioridade alta devem entrar no ciclo atual ou bloquear a entrega afetada.
- Os demais débitos são priorizados no refinamento conforme risco, impacto e custo de postergação.
- Correção emergencial deve gerar uma tarefa de solução definitiva no ciclo atual ou no seguinte.

## Critérios de encerramento

- causa do débito removida
- código revisado
- testes relacionados atualizados e aprovados
- documentação ajustada quando necessário
- item fechado com referência ao pull request da correção
