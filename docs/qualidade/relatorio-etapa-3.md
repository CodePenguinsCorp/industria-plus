# Indústria Plus — relatório de retomada e Etapa 3

> Registro de 24/11 preservado. A reorganização de commits, as verificações de 27/11 e a publicação dos PRs são acompanhadas na [revisão de PO e QA](revisao-po-qa.md). Afirmações abaixo sobre ausência de publicação referem-se à rodada original de 24/11.

Data: 24/11/2026. Equipe: Code Penguins. Situação: retomada e evolução implementadas localmente, com validação MySQL e navegador; documento para revisão da equipe, ainda sem homologação ou envio à professora.

## 1. Atividade e datas verificadas

Na disciplina **T2ESOFT06NA | Manutenção e Melhoria de Software | 202623**, do Teams, a atividade **Etapa 3** informa o retorno do software à equipe de origem e pede:

1. Redefinir o perfil de cada integrante.
2. Analisar a documentação produzida pela equipe anterior.
3. Analisar as manutenções e melhorias propostas pela equipe anterior.
4. Verificar se todas foram aplicadas.
5. Conferir os padrões iniciais, duplicações e possíveis regressões.
6. Atualizar os testes para os novos cenários.
7. Listar débitos técnicos e propor um plano rápido de refatoração para preparar a produção.

A entrega solicitada é um **relatório descrevendo os sete pontos**. A conta consultada mostra a Etapa 3 como **não entregue**, com prazo de **06/11/2026, às 23h59**. Isso descreve o estado observado da conta; não comprova a situação de entregas feitas por outro integrante.

Em 24/11, a aba de trabalhos futuros desta disciplina informava que não havia tarefas futuras. Não foi localizada uma atribuição com prazo em **27/11**. O [cronograma da equipe no Drive](https://docs.google.com/document/d/131rLqLVaBYDDsRDc9fPk6Uw1npni6MzX_sljbf3-0lk/edit) indica **avaliação da maturidade do processo em 27/11** e **pitch final N3 em 04/12**. Confirmar em aula eventuais alterações de prazo ou instruções ainda não publicadas.

## 2. Papéis da equipe

O documento de temas e funções tem os campos da N3 em branco. A matriz `docs/governanca/raci.md` ainda corresponde à N1. Portanto, a redefinição de papéis exigida pela professora está **pendente de decisão da equipe**. Não foi atribuída uma responsabilidade nova em nome de nenhum integrante.

Para a N3, registrar os responsáveis por PO, requisitos, QA, frontend, backend e DevOps e atualizar a matriz RACI. Definir também quem consolida o relatório e quem realiza a apresentação e o envio.

## 3. Material recebido e rastreabilidade

- Base original: [CodePenguinsCorp/industria-plus](https://github.com/CodePenguinsCorp/industria-plus), commit `96e5d4d4066f7cf18903ed523af5b0600b64e33d`, de 11/09/2026.
- O repositório original consultado tinha apenas a branch remota `main`, nenhuma pull request e nenhum fork vinculado. As buscas por nome, código e repositórios acessíveis não localizaram outro fork correspondente.
- A busca pelo nome exato no Teams encontrou **`industria-plus-main 1.zip`**, compartilhado por **Lucas Mönich Nunes**, com arquivos datados de **23/10/2026**.
- SHA-256 do ZIP: `8A6AD5E3AB574C7C20BC6D2C1DC661EB1BE46782E2C5691161DF4AE923329EED`.
- O pacote inclui `ATUALIZACAO.md`, código, migrations V3/V4 e novos testes. É a versão de trabalho usada nesta retomada. A confirmação de que é a entrega formal da equipe anterior, sua autoria e o roteiro original de manutenção continuam pendentes.

O código foi incorporado na branch local `feature/n3-finalizacao`. A `main` permanece na base original. Foram respeitadas as remoções das três classes antigas de técnico, substituídas por usuários com perfil técnico. Os arquivos de credenciais `.env` e `.env.prod` do ZIP não foram incorporados ao repositório. Os modelos `.env.example` e `.env.prod.example` foram preservados e atualizados com as novas variáveis de autenticação.

## 4. Análise da documentação e das melhorias

O documento `ATUALIZACAO.md` descreve instalação, migração de técnicos, permissões por perfil, primeiro acesso, endpoints, histórico e limites conhecidos. Ele é a principal descrição funcional encontrada no pacote recebido.

| Item | Situação observada no código recebido | Evidência principal |
| --- | --- | --- |
| Login por sessão e perfis Administrador/Gestor/Técnico | Implementado; testes de autenticação e autorização disponíveis | `auth/SecurityConfig.java`, `AccessService.java`, `SecurityAndHistoryIntegrationTest.java` |
| Senha provisória e troca obrigatória no primeiro acesso | Implementado | `UserService.java`, `PasswordChangeRequiredFilter.java`, testes correspondentes |
| Técnicos como usuários e vínculo com gestor | Implementado, com migração V4 e adaptação da interface | `V4__technicians_as_users.sql`, `TechnicianService.java`, tela Usuários |
| Histórico de abertura, atribuição, status e serviços | Implementado | `history/HistoryService.java`, `HistoryListComponent`, `RequestDetailComponent` |
| Peças trocadas por equipamento | Implementado por intervenções vinculadas aos chamados e consulta de detalhes do equipamento | `ReplacedPart.java`, `EquipmentDetailController.java`, testes de histórico |
| Cálculo de MTBF | Ausente no pacote; pendente do roteiro final | Limite declarado em `ATUALIZACAO.md` |
| Agendamento automático de preventivas | Ausente no pacote; pendente do roteiro final | Limite declarado em `ATUALIZACAO.md` |

Os dois últimos itens estão previstos no [roteiro de releases do Drive](https://docs.google.com/document/d/16kBKTleZZuMLS5tOuIHPJsKBjVIP9S0Udj-7wOaKLHY/edit), que pede concluir as implementações do segundo release, polir o sistema, tratar débitos e preparar o pitch final em 04/12. Não há evidência suficiente para afirmar que todas as melhorias propostas pela outra equipe foram entregues, pois não foi localizado um inventário formal completo das propostas dessa equipe.

A documentação de produto e o backlog versionados ainda concentram os critérios da N1. Devem ser atualizados para refletir autenticação, permissões, histórico, novas regras de técnicos e requisitos da N3. O acordo inicial no Drive também menciona uma stack anterior; para a implementação atual, a referência técnica é Angular/Spring Boot/MySQL do repositório.

## 5. Padrões, regressões e correções realizadas

A estrutura Angular/Spring Boot, o banco MySQL e o versionamento Flyway foram mantidos. O pacote reutiliza serviços e componentes de histórico. Há, porém, diferenças de estilo entre Java novo e antigo, uso de imports com curinga e rótulos de status repetidos em componentes. A existência de uma ferramenta de formatação não comprova a execução de ESLint ou Checkstyle/Spotless, que continuam ausentes da configuração inspecionada.

### BUG-N3-001 — e-mail válido rejeitado na criação de usuário

- Reprodução: administrador envia `POST /api/users` com nome válido, e-mail `  ANA@EXAMPLE.COM  `, senha provisória válida, perfil técnico e gestor válido.
- Esperado: `201`, e-mail normalizado `ana@example.com`, técnico vinculado ao gestor e senha provisória registrada.
- Obtido no pacote recebido: `400`. O teste existente `CatalogApiIntegrationTest.shouldCreateAndListNormalizedCatalogResources` falhou.
- Causa: `@Email` era executado antes do `trim` de `UserService`, impedindo que a normalização fosse alcançada.
- Correção: normalizar os espaços externos no construtor de `UserCreateRequest`, preservando as validações de e-mail obrigatório e formato.
- Resultado: a suíte de 90 testes do backend passou após a correção, incluindo o cenário original e a rejeição de e-mail inválido. Situação: corrigido e verificado em testes automatizados H2; homologação da equipe pendente.

### BL-N1-007 — última atualização não exibida nos chamados

- O modelo e a API já expunham `updatedAt`, mas a fila mostrava apenas `createdAt`.
- Foi acrescentado o campo **Atualizado em**, no mesmo formato de data usado pela tela.
- O novo teste primeiro falhou porque a data não era renderizada. Após a correção, verifica a data no carregamento e sua atualização após atribuição e mudança de status.
- Situação: implementação e teste concluídos; evidência visual integrada e aceite da equipe pendentes.

### Mudança de contrato documentada

O cadastro de técnicos passou de `POST /technicians` para `POST /users`, com perfil técnico e gestor vinculado. É uma alteração funcional documentada no pacote, que exige atualizar a premissa da N1, clientes externos e roteiros manuais. Os testes antigos foram adaptados no pacote recebido. A simples aprovação dos testes novos não demonstra compatibilidade com clientes antigos.

### BUG-N3-002 — migração pode impedir o primeiro acesso administrativo

- Reprodução: atualizar uma N1 que já tenha técnicos. V4 os transforma em usuários técnicos com senha inutilizável, sem criar um administrador.
- Causa: o bootstrap retornava quando encontrava qualquer usuário, incluindo os técnicos migrados. Assim, `INITIAL_ADMIN_PASSWORD` não criava a conta capaz de liberar os acessos.
- Correção: criar o primeiro administrador configurado quando não existe perfil `ADMIN`. O lock já existente no registro de bootstrap foi preservado. A rotina não promove técnicos nem redefine contas existentes; o e-mail inicial precisa ser exclusivo.
- Evidência: o novo `InitialAdminIntegrationTest.shouldCreateFirstAdminWhenMigratedTechniciansAlreadyExist` falhou antes da correção, com zero administradores, e passou depois. Verifica senha BCrypt válida, preservação do técnico migrado e execução repetida sem duplicação. Outro teste verifica a preservação de administrador existente.
- Resultado: **92 testes do backend aprovados** após esta correção. Esse resultado foi complementado posteriormente pela validação real em MySQL descrita na seção 6.

### Configuração de produção

Os modelos de ambiente foram atualizados para o primeiro login. Os dois geradores `scripts/init-prod-env.*` e o padrão de `compose.prod.yaml` agora usam `SESSION_COOKIE_SECURE=true`, de acordo com a exigência documentada de HTTPS. A configuração local mantém `false`. Os geradores foram verificados em arquivos temporários, sem alterar credenciais ou serviços existentes.

### BUG-N3-003 — concorrência da RN-001 no MySQL

Na primeira execução da suíte completa em MySQL, o teste concorrente retornou duas aceitações
(`200, 200`), em vez de uma aceitação e uma recusa (`200, 409`). A contagem usava um snapshot antigo
da transação `REPEATABLE_READ`, aberto antes de aguardar o lock do técnico. A atribuição agora usa
`READ_COMMITTED` e preserva o bloqueio do técnico até o commit. A suíte MySQL, incluindo a contagem
final igual a dois, passou após a correção. Esse defeito não havia aparecido em H2.

### Funcionalidades concluídas na sequência

O usuário confirmou as regras de MTBF com horas reais e preventivas periódicas. Foram adicionados
os [requisitos e contratos da N3](../produto/evolucao-n3.md), a migration V5, endpoints protegidos,
agendador nativo e formulários nos detalhes do equipamento. A interface foi conferida com dados
fictícios: 100 horas / duas falhas = 50 h; uma preventiva vencida gerou chamado e atualizou a lista
e o histórico sem recarregar a página. O agendador também gerou uma ocorrência sozinho, após
simulação controlada de vencimento no banco isolado. A próxima data avançou sem duplicar a ocorrência.

O fuso da JVM/container foi alinhado a `America/Sao_Paulo`, evitando diferenças entre as datas de
abertura dos chamados e os limites do período. Históricos antigos não foram reescritos.

## 6. Testes e limites da verificação

Ambiente: Windows 11, Node.js 22.14.0, npm 11.1.0, Maven 3.9.6 e JDK 22.0.2 compilando com `release 17`. O runtime Java 17 do CI não foi executado localmente nesta rodada.

| Verificação | Resultado de 24/11/2026 |
| --- | --- |
| Frontend original N1 | 12 testes aprovados em 4 arquivos |
| Frontend recebido, antes das correções | 33 testes aprovados em 10 arquivos |
| Teste novo de `updatedAt`, antes da correção | Falhou como esperado: data ausente |
| Frontend após a correção | **34 testes aprovados em 10 arquivos** |
| `npm run format` | Aprovado |
| `npm run build` | Aprovado |
| Backend recebido | 90 testes, 1 falha no cadastro normalizado de usuário |
| Teste novo de bootstrap, antes da correção | 2 testes executados; o cenário de técnicos migrados falhou como esperado, sem administrador |
| Backend final em H2, `mvn test` | **99 testes aprovados, 0 falhas, 0 erros e 0 ignorados** |
| Backend final em MySQL 8.4, `mvn test` | **99 testes aprovados, incluindo concorrência de atribuição e preventivas** |
| Migração com dados N1, `MySqlUpgradeIT` | **1 teste aprovado em MySQL; equipamento, técnico e vínculo do chamado preservados até V5** |
| Frontend final após evolução | **38 testes aprovados em 12 arquivos** |
| Compose local com `.env.example` | Configuração válida, código de saída 0 |
| Compose de produção com `.env.prod.example` | Configuração válida, código de saída 0 |
| Geradores de configuração PowerShell e shell | Cookie seguro habilitado, senha inicial gerada e configuração Compose válidos |

Os testes do backend incluem regras de alta urgência, transições, autorizações, senha provisória, histórico, peças e compatibilidade de migrations com H2. O teste de limite concorrente em H2 não substitui a validação em MySQL exigida no roteiro original. A quantidade de testes não é percentual de cobertura de linhas ou de aceite; esses percentuais não foram medidos novamente.

O Docker foi iniciado e a validação usou um projeto separado, `industria-plus-n3-validation`,
com MySQL em 3309, API em 8083, frontend em 4203 e volume próprio. A aplicação conteinerizada usa
Java 17. Foram verificadas a migração de uma base N1 de teste, concorrência no MySQL, login
administrativo, MTBF e geração de preventivas no navegador, execução automática do agendador e
persistência após recriar o container MySQL sem remover o volume. A assinatura antes e depois foi
`4:100.00:2026-12-01:QA-N3-01` (quatro chamados, horas, próxima data e patrimônio).

Permanecem sem execução nesta rodada: recuperação de backup, produção HTTPS real, navegação manual
completa em todos os perfis e injeção de falha artificial de infraestrutura. As permissões por
perfil foram verificadas na suíte MySQL. Captura da interface: [MTBF e preventivas](evidencias/mtbf-preventivas.png).

## 7. Pendências e plano rápido para a N3

| ID | Prioridade | Pendência e impacto | Próximo passo e aceite |
| --- | --- | --- | --- |
| N3-001 | Alta | Papéis N3 e aceite formal pendentes | Equipe registra a rotação de papéis, atualiza RACI e revisa os requisitos N3 já documentados. |
| N3-002 | Implementado | MTBF com horas reais e período correspondente | Testes de limites e permissão e demonstração de 50 h aprovados; falta aceite do PO/QA. |
| N3-003 | Implementado | Plano periódico e geração automática | Geração, pausa, retomada, concorrência e atualização visual verificadas; falta aceite do PO/QA. |
| N3-004 | Validado em teste | Migração, persistência e RN-001 em MySQL | Evidências atuais registradas em ambiente isolado. Antes de produção, validar backup/restauração e plano de rollback com os dados reais. |
| N3-005 | Média | Cobertura de interface incompleta | Testar detalhes de equipamento e atendimento, registro de peças, falhas de carregamento e bloqueios por perfil; executar roteiro manual com evidências. |
| N3-006 | Média | Histórico consulta as peças separadamente por registro | Medir consultas para um histórico representativo; se houver custo relevante, buscar peças em lote, mantendo ordenação e registros sem peças. |
| N3-007 | Média | Linters previstos e ausentes; estilo Java desigual | Configurar as ferramentas prometidas no acordo, revisar apenas os módulos necessários e executar os gates em CI. |
| N3-008 | Média | Configuração corrigida; operação real em HTTPS pendente | Geradores e Compose agora usam cookie seguro. Antes de produção, validar login/logout e envio do cookie no endpoint HTTPS real. |
| N3-009 | Alta | Relatório, maturidade e pitch ainda sem aceite final | Revisar este relatório com a equipe, completar evidências e métricas, confirmar orientações de 27/11 e preparar demonstração real para 04/12. |

Próximos passos: confirmar origem formal do ZIP e papéis N3; revisar os commits e o relatório;
homologar com PO/QA; completar cenários manuais e operação de backup/HTTPS; ensaiar o
[roteiro do pitch](../apresentacao/roteiro-pitch-final.md). Código, correções e evidências estão
organizados em commits locais; a publicação ainda não foi realizada.

Para a discussão de maturidade, reunir evidências do fluxo de branches/PRs, resultados do CI, rastreabilidade entre requisitos e testes, registro de defeitos, critérios de aceite e processo de release. Esses tópicos são uma preparação baseada no projeto; não constituem uma rubrica adicional publicada pela professora.

## Encaminhamento

Esta retomada deixa o pacote recebido incorporado, regressões corrigidas e MTBF/preventivas
implementados e testados. A revisão técnica adicional encontrou e ajudou a corrigir o fuso e a
atualização visual após gerar uma preventiva. A conclusão acadêmica depende da revisão por outro
integrante, dos papéis N3 e do aceite PO/QA. Nenhuma entrega foi enviada ao Teams e nenhuma
alteração foi publicada no GitHub nesta rodada.
