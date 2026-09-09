# Estratégia de testes

O QA coordena a estratégia. O foco do N1 é proteger as regras de negócio e os fluxos de cadastro,
abertura, priorização, atribuição e encerramento de chamados.

## Níveis de teste

- `Frontend`: testes de componentes, formulários, serviços e estados de interface com Vitest.
- `Backend unitário`: regras de negócio isoladas com JUnit e Mockito.
- `Backend integração`: endpoints, persistência e migrations com Spring Boot Test e MockMvc.
- `Integração da aplicação`: comunicação entre Angular, API e MySQL no ambiente Docker.
- `Manual`: roteiro de aceite executado pelo QA antes de cada entrega oficial.

## Prioridades do N1

- cadastro e consulta de equipamentos
- cadastro e consulta de setores
- abertura de chamados e definição da urgência
- atribuição e reatribuição de técnicos
- alteração do status e encerramento de chamados
- contagem dos chamados de urgência alta abertos por técnico
- recusas da RN-001, inclusive em atribuições concorrentes

## Roteiro manual obrigatório

1. Cadastrar um setor.
2. Cadastrar um equipamento vinculado ao setor.
3. Abrir um chamado para o equipamento e o setor cadastrados.
4. Definir a urgência do chamado como `Alta`.
5. Atribuir o chamado a um técnico.
6. Atribuir um segundo chamado de urgência alta ao mesmo técnico.
7. Tentar atribuir um terceiro chamado de urgência alta ao mesmo técnico.
8. Confirmar que o sistema recusa a terceira atribuição com uma mensagem clara.
9. Encerrar um dos chamados de urgência alta.
10. Confirmar que o técnico pode receber outro chamado de urgência alta.
11. Disparar duas atribuições concorrentes para um técnico que já possui um chamado de urgência
    alta aberto.
12. Confirmar que somente uma atribuição é aceita, a outra retorna `409` com
    `HIGH_URGENCY_LIMIT` e a contagem persistida permanece em dois.

## Gate mínimo por pull request

1. `npm run format`, `npm test` e `npm run build` no frontend.
2. `mvn test` no backend.
3. Testes novos ou atualizados para toda mudança de comportamento.
4. Evidência do fluxo principal validado quando houver alteração visual.
5. Nenhum defeito crítico ou alto aberto relacionado à entrega.

## Registro de defeitos

Todo bug deve ser registrado no backlog com título, descrição, passos para reprodução, resultado
esperado, resultado obtido, evidências, severidade e responsável.

- `Crítico`: interrompe o sistema ou viola uma regra essencial, incluindo a RN-001.
- `Alto`: compromete uma funcionalidade importante sem impedir todo o uso.
- `Médio`: causa comportamento incorreto, mas possui contorno.
- `Baixo`: problema visual, textual ou de pequeno impacto.

Bugs críticos ou altos bloqueiam a conclusão da funcionalidade afetada.
