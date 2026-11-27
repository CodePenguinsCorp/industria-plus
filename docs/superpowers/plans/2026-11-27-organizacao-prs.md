# Organização e publicação dos pull requests

Solicitação: separar funcionalidades e correções em commits sem escopo, com títulos `feat:`, `fix:`, `docs:` ou `chore:`, e publicar PRs temáticos para revisão posterior de PO/QA.

Execução autorizada em 27/11/2026. Preservar `main`, a branch de trabalho anterior e o ZIP original. Não realizar merge, aprovar PRs, atribuir aceite a pessoas ou publicar credenciais.

1. Preservar o estado anterior em `backup/finalizacao-2026-11-27`. Criar novas branches encadeadas, sem alterar referências remotas existentes.
2. Acesso e histórico: importar o snapshot recebido com proveniência; commits separados para e-mail, bootstrap e cookie seguro. Verificar backend H2, frontend, formatação, build e Compose no topo do PR. Registrar a correção concorrente como dependência do PR seguinte.
3. Chamados: commits separados para concorrência de atribuição e última atualização. Verificar suíte H2, MySQL real e frontend.
4. MTBF: introduzir a V5 íntegra, sem modificar seu checksum na sequência; implementar cálculo e interface de MTBF; commit separado de fuso. O esquema V5 prepara também as tabelas da preventiva, ainda sem expor a funcionalidade. Verificar testes de período, horas, falhas, ausência de dados e papéis; H2/MySQL/frontend/build e upgrade.
5. Preventivas: introduzir geração, API, painel e testes; commit separado para atualização do histórico após salvar. Verificar vencimento, pausa, retomada, duplicação e concorrência. O código final deve coincidir com o snapshot anterior.
6. Documentação: consolidar parecer, critérios e evidências, mapa dos novos commits, dependências entre PRs e roteiros PO/QA. Registrar limitações e verificações ainda pendentes. Revisar por agente independente e resolver achados relevantes.
7. Publicar novas branches e PRs abertos, sem merge. Conferir bases, diffs, checks e URLs remotas. Registrar resultados do CI ou seu impedimento real.

Critérios de aceite: uma alteração semântica por commit; PRs com temas e dependências explícitos; fonte final preservada; credenciais excluídas; resultados ligados ao commit testado; nenhuma pendência manual apresentada como homologada.

Decisão de migração: V5 já foi aplicada na demonstração local. Preservar sua íntegra e introduzi-la no PR de MTBF como preparação de dados compartilhados evita editar migração aplicada ou recriar o banco de demonstração.
