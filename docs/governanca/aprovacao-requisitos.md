# Aprovacao de requisitos

Uma tarefa so pode entrar em programacao quando cumprir o Definition of Ready. Ela so pode ser
considerada concluida quando cumprir o Definition of Done.

## Definition of Ready

- historia no formato `Como [papel], quero [funcionalidade] para [beneficio]`
- objetivo de negocio e ator principal identificados
- criterios de aceite claros e testaveis
- regras de negocio, excecoes e validacoes documentadas
- dependencias tecnicas mapeadas
- impacto em frontend, backend e banco identificado
- alteracoes de banco definidas quando aplicavel
- layout ou comportamento da interface definido quando aplicavel
- contrato de API e dados principais descritos quando houver integracao
- responsavel tecnico definido
- informacoes suficientes para iniciar sem novas decisoes importantes

Para chamados de manutencao, o refinamento deve definir equipamento, setor, urgencia, tecnico,
status e o comportamento esperado da atribuicao.

## Aprovacao para iniciar

1. O Engenheiro de Requisitos confirma que o item cumpre o Definition of Ready.
2. O PO aprova escopo e prioridade.
3. O QA confirma que consegue derivar os cenarios de teste.
4. Os desenvolvedores afetados confirmam a viabilidade tecnica.

## Definition of Done

- criterios de aceite atendidos
- regras de negocio implementadas no backend quando forem criticas
- codigo segue os padroes da equipe e nao possui erros nos gates tecnicos
- testes automatizados relacionados executados com sucesso
- QA executou os testes necessarios sem encontrar defeitos criticos ou altos
- pull request revisado e aprovado por pelo menos outro integrante
- codigo integrado na branch `develop`
- funcionalidade testada junto ao restante do sistema
- documentacao atualizada quando aplicavel

Para atribuicao de tecnicos, o Done exige teste automatizado e evidencia de que o terceiro chamado
de urgencia `Alta` aberto para o mesmo tecnico e recusado.
