# Aprovação de requisitos

Uma tarefa só pode entrar em programação quando cumprir o Definition of Ready. Ela só pode ser
considerada concluída quando cumprir o Definition of Done.

## Definition of Ready

- história no formato `Como [papel], quero [funcionalidade] para [benefício]`
- objetivo de negócio e ator principal identificados
- critérios de aceite claros e testáveis
- regras de negócio, exceções e validações documentadas
- dependências técnicas mapeadas
- impacto em frontend, backend e banco identificado
- alterações de banco definidas quando aplicável
- layout ou comportamento da interface definido quando aplicável
- contrato de API e dados principais descritos quando houver integração
- responsável técnico definido
- informações suficientes para iniciar sem novas decisões importantes

Para chamados de manutenção, o refinamento deve definir equipamento, setor, urgência, técnico,
status e o comportamento esperado da atribuição.

## Aprovação para iniciar

1. O Engenheiro de Requisitos confirma que o item cumpre o Definition of Ready.
2. O PO aprova escopo e prioridade.
3. O QA confirma que consegue derivar os cenários de teste.
4. Os desenvolvedores afetados confirmam a viabilidade técnica.

## Definition of Done

- critérios de aceite atendidos
- regras de negócio implementadas no backend quando forem críticas
- código segue os padrões da equipe e não possui erros nos gates técnicos
- testes automatizados relacionados executados com sucesso
- QA executou os testes necessários sem encontrar defeitos críticos ou altos
- pull request revisado e aprovado por pelo menos outro integrante
- código integrado na branch `develop`
- funcionalidade testada junto ao restante do sistema
- documentação atualizada quando aplicável

Para atribuição de técnicos, o Done exige teste automatizado e evidência de que o terceiro chamado
de urgência `Alta` aberto para o mesmo técnico é recusado.
