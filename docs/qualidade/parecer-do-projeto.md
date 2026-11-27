# Parecer do Indústria Plus

Atualizado em 27/11/2026. Comparação entre o planejamento da equipe, o ZIP recebido e a implementação local atual. O comparativo inicial usa as verificações de 24/11. Em 27/11 as suítes foram executadas novamente por conjunto de alterações; resultados atuais e ordem dos PRs estão na [revisão de PO/QA](revisao-po-qa.md). Os nomes da demonstração foram atualizados sem mudar a lógica.

O produto possui as três evoluções funcionais previstas além da primeira entrega: histórico de peças, MTBF e preventivas automáticas. O histórico já estava no ZIP; MTBF e preventivas foram implementados nesta retomada. Também foram corrigidos defeitos de cadastro, bootstrap administrativo e concorrência. A implementação está disponível localmente, mas a homologação da equipe, a integração no GitHub e a preparação operacional para produção continuam pendentes.

## Base da comparação

- Planejamento funcional: [escopo](../produto/escopo.md), [premissa inicial](../produto/premissa-inicial%20N1.md), [backlog](../governanca/backlog.md) e [roteiro de releases no Drive](https://docs.google.com/document/d/16kBKTleZZuMLS5tOuIHPJsKBjVIP9S0Udj-7wOaKLHY/edit).
- Recebimento: `industria-plus-main 1.zip`, compartilhado por Lucas no Teams, com arquivos datados de 23/10/2026. SHA-256: `8A6AD5E3AB574C7C20BC6D2C1DC661EB1BE46782E2C5691161DF4AE923329EED`. O conteúdo foi preservado separadamente.
- Base Git anterior: `96e5d4d`. O histórico original foi preservado localmente e reorganizado em commits por funcionalidade. Importação sem credenciais: `ed6f68c`; e-mail: `23113a1`; bootstrap: `821ad0b`; cookie: `60a260b`; concorrência: `76beeb4`; última atualização: `e097820`; persistência: `e13bb8a`; MTBF: `592f2ab`; fuso: `502ac71`; preventivas: `a7c8f96`; atualização do histórico: `4e6bd36`.
- Trabalho distribuído em branches temáticas encadeadas, preservando a branch anterior de trabalho e uma cópia de segurança local. A autoria formal da manutenção recebida e seu inventário completo de propostas ainda precisam de confirmação da equipe. Não é possível afirmar que todas as propostas da outra equipe foram atendidas apenas pelo ZIP localizado.

“Implementado” significa código disponível e verificação indicada; não equivale a aceite do PO/QA. “Já recebido” distingue o trabalho existente no pacote das alterações desta retomada.

## Comparativo funcional

| O que estava previsto ou foi incorporado ao produto | Quando o ZIP foi recebido | O que foi feito nesta retomada | Situação atual |
| --- | --- | --- | --- |
| Cadastro e consulta de setores | Já recebido. | Preservado; integra as suítes executadas. | Implementado. |
| Cadastro e consulta de equipamentos, patrimônio único e vínculo com setor | Já recebido. | Preservado; acrescentados os painéis de confiabilidade e plano preventivo. | Implementado; edição cadastral não foi acrescentada. |
| Cadastro e consulta de técnicos e sua carga | Já recebido, com técnicos convertidos em usuários e vínculo com gestor. | Preservados migração e novo contrato de cadastro. | Implementado; `POST /technicians` foi substituído por criação de usuário técnico. |
| Abertura de chamados preventivos e corretivos | Já recebido. | Preservado; incluída abertura automática das preventivas programadas. | Implementado. |
| Urgência, filtros, ordenação, atribuição e reatribuição | Já recebido. | Corrigida a concorrência real da regra de alta urgência no MySQL. | Implementado e validado por testes. |
| Ciclo de vida e bloqueio de alterações após encerramento | Já recebido. | Preservado; cenários incluídos na suíte executada. | Implementado. |
| Mostrar criação e última atualização do chamado | API/modelo já tinham as duas datas; a fila só exibia a criação. | Acrescentado “Atualizado em”, com teste de carregamento, atribuição e mudança de status. | Implementado; aceite visual completo do fluxo pendente. |
| Histórico de peças por máquina, previsto no escopo de evolução | Já recebido, por intervenções e peças associadas aos chamados. | Preservado e verificado na suíte; não foi criado nesta retomada. | Implementado; demonstração atual ainda não tem intervenção ou peça cadastrada. |
| Histórico de abertura, responsáveis, status e serviços | Já recebido. | Acrescentado histórico de abertura automática de preventivas e atualização imediata da tela. | Implementado. |
| Login, perfis e permissões | Já recebido. | Corrigida a criação do primeiro administrador quando já existem técnicos migrados. | Implementado; permissões verificadas automaticamente. |
| Senha provisória, primeiro acesso e redefinição pelo administrador | Já recebido. | Preservado; corrigida a validação de e-mail na criação de usuário. | Implementado. |
| MTBF por equipamento, previsto no roteiro | Não implementado; o próprio ZIP declarava essa ausência. | Criados período de observação, horas reais informadas, contagem de falhas, cálculo, API, permissões, interface e testes. | Implementado e demonstrado: 100 horas / 2 falhas = 50 h. |
| Agendamento automático de preventivas, previsto no roteiro | Não implementado; abrir manualmente um chamado preventivo não era agendamento. | Criados plano por equipamento, intervalo em dias, data, pausa/retomada, agendador, controle de duplicidade e interface. | Implementado; geração imediata e em segundo plano verificadas. |
| Evolução de banco preservando os registros existentes | ZIP incluía V3/V4 e declaração de teste próprio da V4 em MySQL. | Preservadas V1–V4, criada V5 e executada migração N1→V5 em MySQL com registros e vínculos existentes. | Validado em base de teste; migração dos dados reais exige backup e procedimento operacional. |
| Polimento, correção de regressões e tratamento de débitos | Havia lacunas funcionais, defeitos e débitos documentados. | Corrigidos os defeitos descritos abaixo; ampliados testes e documentação. | Parcial: ainda existem débitos e validações pendentes. |
| Relatório de retomada, evidências e pitch final | Não havia consolidação do trabalho desta retomada. | Produzidos relatório dos sete itens da atividade, evidências, critérios das novas funções e roteiro do pitch. | Documentos preparados; revisão, apresentação e envio pendentes. |

## Correções além das duas funcionalidades novas

| Problema encontrado | Efeito no pacote recebido ou na validação | Correção e comprovação |
| --- | --- | --- |
| E-mail validado antes de remover espaços externos | Um e-mail válido como `  ANA@EXAMPLE.COM  ` retornava `400`; um teste do próprio pacote falhou. | Remoção dos espaços antes da validação. Cadastro normalizado passa, mantendo rejeição de formato inválido. |
| Bootstrap retornava ao encontrar qualquer usuário | Técnicos migrados podiam impedir a criação do primeiro administrador e bloquear a liberação dos acessos. | Verificar existência de perfil administrador. Dois testes cobrem migração, senha válida, repetição e preservação de conta existente. |
| Snapshot antigo na atribuição concorrente do MySQL | Duas atribuições simultâneas eram aceitas e podiam ultrapassar o limite de dois chamados de alta urgência. H2 não revelou o defeito. | `READ_COMMITTED` na atribuição, mantendo lock do técnico. MySQL passou a retornar uma aceitação e uma recusa, com carga final dois. |
| Última atualização ausente da fila | Requisito previsto no backlog permanecia incompleto na apresentação. | Campo exibido e teste de componente correspondente. |
| Cookie de produção sem padrão seguro nos geradores | A configuração gerada não refletia a exigência de HTTPS documentada. | Geradores e Compose de produção usam `SESSION_COOKIE_SECURE=true`; modelos incluem variáveis do administrador inicial. HTTPS real ainda precisa de validação. |
| Fuso e atualização visual na evolução nova | Datas do período e geração de preventiva precisavam ser coerentes; a lista precisava refletir o chamado recém-gerado. | Fuso `America/Sao_Paulo` no runtime conteinerizado e atualização dos detalhes após salvar o plano. Verificação automatizada e no navegador. |

Não foram encontradas falhas pendentes nas suítes executadas. Isso não demonstra ausência de defeitos em cenários ainda não testados.

## Regras implementadas e seus limites

**MTBF.** Gestor ou administrador informa datas inclusivas e horas reais de operação. O sistema conta os chamados corretivos abertos nesse período e divide as horas pela quantidade de falhas. Preventivas não entram na conta. Sem medição, mostra dados insuficientes; com zero falhas, informa que não há falhas registradas sem inventar um MTBF zero ou infinito. Há validações de período, horas, futuro e permissões.

A versão mantém uma medição atual por equipamento, substituída ao salvar outra. Não coleta telemetria, não mantém uma série histórica de medições e considera cada chamado corretivo uma falha. Essas são as regras confirmadas com o usuário, não uma implementação de sensores ou análise de confiabilidade mais ampla.

**Preventivas.** Há um plano por equipamento, com intervalo de 1 a 3650 dias, próxima data e estado ativo/pausado. O backend verifica vencimentos a cada minuto e também ao salvar um plano vencido. Gera chamado preventivo de urgência média, aberto e sem técnico, com registro de agendamento automático. Lock e restrição única protegem a ocorrência contra duplicação.

Se várias datas estiverem atrasadas, gera uma ocorrência pendente e avança a cadência até uma data futura. Não abre todos os chamados retroativos. Pausar preserva o histórico. O backend precisa estar ligado para executar o agendador; a versão não possui múltiplos planos por máquina, periodicidade por horímetro, notificações ou atribuição automática. Esses itens não foram exigidos pela regra acordada.

## Validação realizada

| Verificação | Resultado comprovado em 24/11 |
| --- | --- |
| Backend recebido | 90 testes, com uma falha de normalização de e-mail. |
| Backend após correções e evolução, H2 | 99 aprovados; zero falhas, erros ou ignorados. |
| Mesma suíte em MySQL 8.4 | 99 aprovados, incluindo concorrência de atribuição e preventivas. |
| Migração dedicada em MySQL | Um teste aprovado, de N1/V2 com dados existentes até V5. |
| Frontend recebido | 33 testes aprovados. |
| Frontend atual | 38 testes aprovados em 12 arquivos. |
| Formatação e builds | Formatação e build do frontend, imagens Docker e configurações Compose aprovados. |
| Interface integrada | Login administrativo, MTBF, preventiva vencida e atualização da lista/histórico verificados. |
| Agendador real | Criou uma ocorrência em segundo plano no banco isolado. |
| Persistência | Recriação do container MySQL preservando o volume manteve IDs, chamados, horas e próxima data. |

São os mesmos 99 testes de backend executados em dois bancos, não 198 testes distintos. A diferença em relação ao ZIP é de nove testes de backend e cinco de frontend na suíte regular, além do teste dedicado de migração. Os percentuais de linhas, requisitos e aceite não foram recalculados; os 85,71% e 45,10% do registro antigo são medições históricas e não podem ser apresentados como resultado desta versão.

O runtime da aplicação Docker é Java 17. Os testes locais Maven usaram JDK 22 compilando para Java 17. A execução do pipeline remoto é registrada na revisão de PO/QA após a publicação de cada PR. Resultados detalhados: [evidências](evidencias-n3.json) e [relatório da retomada](relatorio-etapa-3.md).

## Pendências antigas e situação atual

O backlog original conserva referências à primeira entrega. Seu texto antigo não deve ser lido como uma lista atual de tudo que falta.

| Item do backlog | Estado atual |
| --- | --- |
| BL-N1-001 — incorporar os três commits de qualidade | Os documentos correspondentes fazem parte da base de trabalho; revisão final pela equipe continua necessária. |
| BL-N1-002 — executar testes e gates | Executado localmente para a implementação validada; verificação de CI remoto acompanhada nos PRs; revisão e aceite ainda pendentes. |
| BL-N1-003 — concorrência da regra crítica em MySQL | Validado, após correção do defeito reproduzido no banco real. |
| BL-N1-004 — persistência do volume MySQL | Validado no ambiente isolado, com recriação do container e preservação dos registros. |
| BL-N1-005 — ambiente limpo e topologia de produção | Stack isolada, migrations e comunicação Nginx/API/MySQL verificadas; configuração de produção validada. Implantação real com HTTPS continua pendente. |
| BL-N1-006 — roteiro funcional completo com evidências | Parcial: há testes automatizados e demonstração administrativa, mas falta o roteiro manual completo com todos os perfis e aceite. |
| BL-N1-007 — data de atualização na interface | Implementado e testado; falta aceite visual integrado do fluxo completo. |
| BL-N1-008 — JSON, acentos, datas e erros | Parte coberta em testes e interface; falta consolidar o roteiro completo de contrato e apresentação. |
| BL-N1-009 — métricas de qualidade | Contagens, ambiente, commits e resultados registrados; percentuais atuais de cobertura e aceite não recalculados. |
| BL-N1-010 — homologação formal | Pendente de PO/QA e revisão de outro integrante. |

## O que ainda falta fazer

| Prioridade | Pendência | O que encerra o item |
| --- | --- | --- |
| Alta | Papéis atuais da equipe e RACI | Equipe define responsáveis por produto, requisitos, QA, frontend, backend, operação, relatório e apresentação. Os papéis antigos não são uma redefinição automática. |
| Alta | Confirmar origem formal do ZIP e propostas da equipe anterior | Equipe confirma entrega/autoria e disponibiliza o inventário de propostas para conferir aplicação integral. |
| Alta | Homologar o fluxo completo | Executar e registrar administrador, gestor e técnico: criação e primeiro acesso, vínculo, atribuição, serviço, peças, encerramento, restrições e novas funções. |
| Alta | Revisar e integrar o trabalho | PRs temáticos preparados para revisão e publicação, com roteiros e evidências por commit. A situação remota e os links são registrados na revisão de PO/QA. Aprovação e integração dependem da equipe. |
| Alta | Fechar relatório, maturidade e pitch | Revisar os sete itens da atividade, completar papéis/evidências e preparar apresentação com demonstração e responsáveis. O roteiro é um rascunho, não os slides prontos. |
| Alta antes de produção | Backup, restauração e rollback | Restaurar uma cópia em ambiente separado, conferir integridade e documentar a recuperação. Persistir um volume não equivale a testar restauração de backup. |
| Alta antes de produção | HTTPS e cookies na implantação real | Confirmar login, logout, sessão, CSRF e cookie seguro no endereço HTTPS real. Validar Compose não equivale a realizar essa implantação. |
| Média | Cobertura de interface | Adicionar cenários relevantes de página inicial, cadastro/lista de equipamentos, atendimento/peças, falhas de carregamento e permissões visuais. Os testes novos dos detalhes não cobrem toda a interface. |
| Média | Linters previstos no acordo | Configurar ESLint/angular-eslint e Spotless ou Checkstyle, documentar comandos e executar no CI. Prettier, build e compilação não equivalem a esses linters. |
| Média | Falha artificial do agendador/infraestrutura | Provocar falha controlada e comprovar rollback, ausência de ocorrência parcial e recuperação no ciclo seguinte. |
| Média | Métricas atuais e documentos antigos | Atualizar rastreabilidade e calcular cobertura com denominador e cenários explícitos, sem reutilizar percentuais antigos. |
| Após medir | Consultas de peças no histórico | Medir um histórico representativo. O serviço busca peças por registro; se o custo justificar, substituir por busca em lote sem alterar a ordem nem omitir eventos sem peças. |

## Exigências da professora

| Ponto da Etapa 3 | Situação |
| --- | --- |
| Redefinir os perfis dos integrantes | Pendente de decisão da equipe. |
| Analisar a documentação anterior | Análise produzida no relatório da retomada. |
| Analisar manutenções e melhorias propostas | Comparação produzida para os materiais localizados; inventário formal da outra equipe não confirmado. |
| Verificar se todas foram aplicadas | As três evoluções do roteiro do produto estão implementadas; aplicação de todas as propostas externas não pode ser atestada sem o inventário completo. |
| Conferir padrões, duplicações e regressões | Estrutura mantida, três defeitos corrigidos e débitos de padronização/consulta registrados. |
| Atualizar testes | Executado, com resultados acima; cobertura manual e de interface ainda parcial. |
| Listar débitos e plano rápido de refatoração | Registrado no relatório e neste parecer; execução dos débitos restantes segue pendente. |

Na consulta de 24/11, a atividade do Teams aparecia como não entregue na conta consultada, com prazo de 06/11 às 23h59. Isso não confirma entregas feitas por outro integrante nem o estado atual do Teams em 27/11. O [cronograma do Drive](https://docs.google.com/document/d/131rLqLVaBYDDsRDc9fPk6Uw1npni6MzX_sljbf3-0lk/edit) consultado previa maturidade do processo em 27/11 e pitch em 04/12. Este parecer não representa nova consulta das orientações publicadas depois de 24/11.

## Próximo trabalho que pode ser feito

A prioridade prática é preparar uma demonstração completa com gestor e técnico, intervenção, peças e encerramento, e executar o roteiro manual por perfil. Isso melhora tanto a validação do produto quanto a apresentação. Em seguida, atualizar a matriz de requisitos e métricas, fechar o relatório com a equipe e transformar o roteiro em slides com evidências reais.

No desenvolvimento, é possível ampliar testes dos fluxos ainda descobertos, configurar os linters e ensaiar restauração/recuperação. Edição/desativação de usuários, recuperação de senha por e-mail, edição cadastral, múltiplos planos e gráficos históricos podem ser evoluções futuras; não devem ser apresentados como obrigações originais não cumpridas sem decisão de escopo.

## Nomes da demonstração

Em 27/11 os dados fictícios passaram a usar nomes comuns: **Prensa hidráulica**, patrimônio **PRE-001**, setor **Estamparia**, chamados **Vazamento no circuito hidráulico** e **Queda de pressão durante a prensagem**, preventiva **Lubrificação dos componentes da prensa** e conta `admin@industriaplus.local`. IDs, vínculos, datas, quatro chamados, medição de 100 horas e resultado de 50 h foram preservados. O gerador de dados foi alinhado aos novos nomes.

Os nomes comuns melhoram a apresentação; os registros continuam sendo dados de demonstração, sem comprovação de operação industrial real. Referências históricas da disciplina nos documentos e evidências anteriores foram preservadas.

Captura atual: [demonstração do equipamento](evidencias/demonstracao.png).
