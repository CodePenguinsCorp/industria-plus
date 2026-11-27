# Finalização N3 — plano de trabalho

**Objetivo:** iniciar a finalização do Indústria Plus conforme a Etapa 3 do Teams e o roteiro de releases da equipe.

**Base:** `CodePenguinsCorp/industria-plus`, commit `96e5d4d`, e o arquivo `industria-plus-main 1.zip` compartilhado por Lucas no Teams. A identificação do ZIP como entrega da outra equipe é uma hipótese apoiada pela data e pelo conteúdo; a autoria e a entrega formal ainda precisam ser confirmadas.

**Arquitetura:** preservar Angular, Spring Boot, MySQL, autenticação por sessão e migrations recebidas. Incorporar o ZIP em `feature/n3-finalizacao`, mantendo a `main` e os arquivos originais de recebimento. Não importar arquivos locais de credenciais.

## Etapas

- [x] Consultar a atividade, o cronograma e os documentos da equipe.
- [x] Localizar e preservar o pacote recebido, registrar seu hash e comparar com a N1.
- [x] Executar os testes do frontend recebido: 33 aprovados.
- [x] Executar os testes do backend, o build, a formatação e a validação do Compose.
- [x] Resolver `BL-N1-007`: exibir `updatedAt` nos chamados, em `maintenance-request.component.html`, com teste em `maintenance-request.component.spec.ts` para carregamento, atribuição e mudança de status. Falha inicial e aprovação posterior verificadas.
- [x] Corrigir a normalização do e-mail antes da validação, após reproduzir a falha no teste recebido.
- [x] Corrigir o bootstrap quando existem técnicos migrados sem administrador, com dois testes de integração e ciclo de falha/aprovação.
- [x] Atualizar `.env.example` e `.env.prod.example` e alinhar cookie seguro nos geradores/Compose de produção.
- [x] Consolidar `docs/qualidade/relatorio-etapa-3.md`: sete itens da atividade, evidências, diferenças do pacote recebido, pendências, limites da validação e plano de refatoração.

## Restrições e pontos de revisão

- Papéis da N3 precisam ser definidos pela equipe; os papéis da N1 não representam aprovação automática para a N3.
- A RN-001 deve continuar protegida pelo backend. H2 não comprova concorrência em MySQL.
- Não modificar V1/V2 em bancos já instalados; verificar a migração V4 com backup e MySQL antes do aceite.
- MTBF e agendamento automático são pendências do escopo final; precisam de histórias e critérios de aceite antes da implementação.
- Publicação, envio do relatório e homologação não fazem parte desta primeira alteração local.
