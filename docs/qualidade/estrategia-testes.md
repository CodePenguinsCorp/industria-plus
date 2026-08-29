# Estrategia de testes

O QA coordena a estrategia. O foco do N1 e proteger as regras de negocio e os fluxos de cadastro,
abertura, priorizacao, atribuicao e encerramento de chamados.

## Niveis de teste

- `Frontend`: testes de componentes, formularios, servicos e estados de interface com Vitest.
- `Backend unitario`: regras de negocio isoladas com JUnit e Mockito.
- `Backend integracao`: endpoints, persistencia e migrations com Spring Boot Test e MockMvc.
- `Integracao da aplicacao`: comunicacao entre Angular, API e MySQL no ambiente Docker.
- `Manual`: roteiro de aceite executado pelo QA antes de cada entrega oficial.

## Prioridades do N1

- cadastro e consulta de equipamentos
- cadastro e consulta de setores
- abertura de chamados e definicao da urgencia
- atribuicao e reatribuicao de tecnicos
- alteracao do status e encerramento de chamados
- contagem dos chamados de urgencia alta abertos por tecnico
- recusas da RN-001, inclusive em atribuicoes concorrentes

## Roteiro manual obrigatorio

1. Cadastrar um setor.
2. Cadastrar um equipamento vinculado ao setor.
3. Abrir um chamado para o equipamento e o setor cadastrados.
4. Definir a urgencia do chamado como `Alta`.
5. Atribuir o chamado a um tecnico.
6. Atribuir um segundo chamado de urgencia alta ao mesmo tecnico.
7. Tentar atribuir um terceiro chamado de urgencia alta ao mesmo tecnico.
8. Confirmar que o sistema recusa a terceira atribuicao com uma mensagem clara.
9. Encerrar um dos chamados de urgencia alta.
10. Confirmar que o tecnico pode receber outro chamado de urgencia alta.
11. Disparar duas atribuicoes concorrentes para um tecnico que ja possui um chamado de urgencia
    alta aberto.
12. Confirmar que somente uma atribuicao e aceita, a outra retorna `409` com
    `HIGH_URGENCY_LIMIT` e a contagem persistida permanece em dois.

## Gate minimo por pull request

1. `npm run format`, `npm test` e `npm run build` no frontend.
2. `mvn test` no backend.
3. Testes novos ou atualizados para toda mudanca de comportamento.
4. Evidencia do fluxo principal validado quando houver alteracao visual.
5. Nenhum defeito critico ou alto aberto relacionado a entrega.

## Registro de defeitos

Todo bug deve ser registrado no backlog com titulo, descricao, passos para reproducao, resultado
esperado, resultado obtido, evidencias, severidade e responsavel.

- `Critico`: interrompe o sistema ou viola uma regra essencial, incluindo a RN-001.
- `Alto`: compromete uma funcionalidade importante sem impedir todo o uso.
- `Medio`: causa comportamento incorreto, mas possui contorno.
- `Baixo`: problema visual, textual ou de pequeno impacto.

Bugs criticos ou altos bloqueiam a conclusao da funcionalidade afetada.
