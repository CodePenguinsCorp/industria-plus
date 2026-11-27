# Revisão de produto e qualidade do Indústria Plus

Organização de 27/11/2026 para revisão posterior por PO e QA. As verificações técnicas não substituem o aceite da equipe. Nenhum PR deve ser tratado como aprovado ou integrado apenas por ter sido publicado.

## Ordem e dependências

| Ordem | Tema | Branch | Base inicial | Documento de revisão |
| --- | --- | --- | --- | --- |
| 1 | Acesso e histórico recebidos, correções de acesso e configuração | `feature/acesso-e-historico` | `develop` | [Acesso](revisao-acesso.md) |
| 2 | Concorrência e última atualização dos chamados | `fix/atribuicao-e-atualizacao` | `feature/acesso-e-historico` | [Chamados](revisao-chamados.md) |
| 3 | Confiabilidade e MTBF | `feature/confiabilidade-mtbf` | `fix/atribuicao-e-atualizacao` | [MTBF](revisao-mtbf.md) |
| 4 | Planos e geração de preventivas | `feature/planos-preventivos` | `feature/confiabilidade-mtbf` | [Preventivas](revisao-preventivas.md) |
| 5 | Planejamento, evidências, parecer e aceite | `docs/revisao-e-entrega` | `feature/planos-preventivos` | Este documento e [parecer](parecer-do-projeto.md) |

Os PRs são encadeados para que cada diff mostre somente seu tema. `develop` foi criada no mesmo commit da `main` já existente, conforme o fluxo documentado do projeto. Nenhuma integração foi realizada.

Para integrar, aprovar na ordem. Preservar a ancestralidade das branches enquanto houver PRs descendentes; usar merge commit e, depois de incorporar o anterior em `develop`, alterar a base do próximo para `develop`. Squash/rebase do pai exige rebase coordenado dos descendentes para não reapresentar commits. A publicação não realiza essa integração automaticamente.

## Aderência ao planejamento

| Referência | Entrega verificável | Situação |
| --- | --- | --- |
| Escopo de evolução e roteiro de releases | Histórico de peças por máquina. | Recebido no ZIP e preservado no primeiro PR. |
| Escopo de evolução e regra confirmada em 24/11 | MTBF com horas reais e período de observação. | Implementado, com critérios e testes no PR de MTBF. |
| Escopo de evolução e regra confirmada em 24/11 | Plano periódico e abertura automática sem duplicidade. | Implementado, com critérios e testes no PR de preventivas. |
| RN-001 / CA-R1-08 / BL-N1-003 | Limite de dois HIGH sob concorrência no MySQL. | Correção e suíte real no PR de chamados. |
| RF12 / BL-N1-007 | Exibição de criação e última atualização. | Interface e teste no PR de chamados; roteiro manual de aceite incluído. |
| Retomada, análise de regressões | Cadastro normalizado e bootstrap administrativo após migração. | Correções separadas no PR de acesso. |
| RNF03 / BL-N1-004 | Persistência preservando o volume. | Evidência isolada de 24/11 mantida. Não equivale a restauração de backup. |
| Migração e preservação da instalação | Dados e vínculos da N1 preservados até V5. | Teste dedicado em MySQL executado novamente. |
| Processo de qualidade | Compilação, formatação, testes e builds. | Resultados por branch e commit nos arquivos `verificacao-*.json`; CI remoto a conferir nos PRs após a publicação. |
| Atividade da professora | Análise, testes, débitos e relatório. | Documentos preparados; papéis, origem formal, revisão e envio dependem da equipe. |

## Como revisar cada alteração

O PO confere objetivo, regra de negócio e limites descritos no roteiro do tema. O QA executa os cenários com dados controlados e registra resultado obtido, ambiente, commit e evidência. O revisor técnico confere API, permissões, migrações, falhas e testes, usando o diff incremental do PR. Os responsáveis não foram atribuídos automaticamente.

Modelo para registrar cada execução:

| Campo | Preenchimento |
| --- | --- |
| Cenário e requisito | Identificador ou descrição do roteiro. |
| Branch, commit e ambiente | Revisão exata e configuração testada. |
| Executor e data | Preencher pela equipe. |
| Esperado e obtido | Descrever comportamento e persistência. |
| Evidência | Captura, resposta de API ou log sem credenciais. |
| Resultado | Pendente até execução; depois aprovado ou reprovado. |
| Aceite do PO | Decisão registrada pelo responsável, sem inferência automática. |

## Limites e trabalho restante

- A autoria formal do ZIP e o inventário de propostas da equipe anterior não foram confirmados.
- Papéis atuais, RACI e aceite de PO/QA permanecem pendentes.
- Há uma medição e um plano atuais por equipamento. MTBF usa horas informadas e um corretivo por falha; não há telemetria.
- Faltam roteiro manual completo de todos os perfis, cobertura adicional da interface e medição atual de cobertura de requisitos/aceite.
- ESLint/angular-eslint e Spotless ou Checkstyle continuam como débitos. A formatação aprovada é Prettier, não um atestado desses linters.
- Backup/restauração, HTTPS real e falha artificial de infraestrutura permanecem sem homologação.
- PRs aguardam revisão; o código não foi integrado nem enviado como atividade no Teams.

## Preservação do trabalho

O histórico anterior foi mantido em branch local de backup. Os commits novos reagrupam o trabalho em temas sem alterar o comportamento final da aplicação. A comparação de backend, frontend, migrations, Docker, Compose e geradores contra o snapshot anterior foi executada; somente a configuração de CI e finais de linha foi acrescentada durante esta organização.

A V5 mantém o conteúdo já validado, inclusive as estruturas compartilhadas entre MTBF e preventivas. Nenhuma migração aplicada foi reescrita na sequência. O pacote recebido foi importado como snapshot identificado, não apresentado como autoria nova da equipe.
