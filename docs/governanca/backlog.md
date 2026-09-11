# Backlog da entrega N1

## Objetivo

Este documento consolida o trabalho realizado no repositório, sua autoria e as atividades que ainda
precisam ser concluídas para o aceite da entrega N1 do Industria Plus. Funcionalidades previstas
para entregas posteriores não fazem parte deste backlog.

O levantamento foi feito por inspeção do código, da documentação e dos commits existentes em todas
as referências locais do Git em 11/09/2026. Os nomes dos integrantes foram normalizados para o
primeiro nome; os hashes permitem consultar a autoria completa registrada no Git. A autoria técnica
no histórico não substitui revisão, homologação ou aceite do trabalho.

## Critérios de status

| Status | Significado |
| --- | --- |
| `Concluído` | Alteração identificada no histórico da branch local analisada. |
| `Concluído em origin/main` | Alteração identificada no remoto, ainda ausente da branch local no momento do levantamento. |
| `Pendente` | Atividade necessária para completar ou comprovar o aceite da N1. |

## Entregas identificadas por autor

| ID | Status | Entrega | Autor no Git | Evidência |
| --- | --- | --- | --- | --- |
| ENT-001 | Concluído | Criação inicial do repositório. | José | `920de8f` |
| ENT-002 | Concluído | Estrutura base do helpdesk: Angular, Spring Boot, MySQL, Docker Compose, health check, scripts locais e documentação inicial de arquitetura, produto, governança e qualidade. | José | `3f36f67` |
| ENT-003 | Concluído | Fluxos funcionais da N1: setores, equipamentos, técnicos, chamados, filtros, atribuição, transições de status, regra de limite de alta urgência, migration e testes automatizados relacionados. | José | `995cd25` |
| ENT-004 | Concluído | Automação de implantação em produção: Compose, variáveis de ambiente, inicialização segura, scripts de deploy e instruções operacionais. | José | `7e6be99` |
| ENT-005 | Concluído | Reformulação da página inicial, métricas operacionais, navegação, estilos e busca textual nas telas de cadastro. | André | `db9871d` |
| ENT-006 | Concluído | Correção de acentuação na documentação de produto, arquitetura, governança e qualidade. | Matheus | `e98edaf` |
| ENT-007 | Concluído | Reorganização e detalhamento das premissas, requisitos, regras de negócio e critérios de aceite da N1. | Matheus | `ab70074`, `84951c1` |
| ENT-008 | Concluído | Checklist de rastreabilidade dos requisitos funcionais e não funcionais da N1 contra código, testes e infraestrutura. | Matheus | `7767059` |
| ENT-009 | Concluído | Refinamentos na política de gestão de débito técnico. | Matheus | `db40877` |
| ENT-010 | Concluído em origin/main | Ampliação da estratégia de testes, incluindo regras prioritárias, concorrência em MySQL, validações, códigos de erro e evidências. | Lucas | `5ddd525` |
| ENT-011 | Concluído em origin/main | Definição do processo de registro de bugs e evidências. | Lucas | `b0b10f6` |
| ENT-012 | Concluído em origin/main | Definição das métricas de qualidade e dos critérios para acompanhamento da N1. | Lucas | `5b5b048` |

### Síntese por integrante

| Integrante | Papel conforme a RACI | Contribuição observada |
| --- | --- | --- |
| Matheus | Product Owner e Engenheiro de Requisitos | Refinamento do escopo e dos critérios de aceite, rastreabilidade de requisitos, revisão documental e política de débito técnico. |
| Lucas | Quality Assurance e DevOps | Estratégia de testes, processo de bugs e evidências e métricas de qualidade, presentes em `origin/main`. |
| André | Desenvolvedor Frontend | Reformulação visual, dashboard operacional, métricas de interface, navegação e busca. |
| José | Desenvolvedor Backend | Estrutura do projeto, domínio e API da N1, persistência, testes, Docker e automação de produção. |

## Backlog pendente da N1

Os responsáveis abaixo são propostos a partir dos papéis definidos na [matriz RACI](raci.md). Eles
não indicam que a pessoa já iniciou ou aceitou individualmente a atividade.

| ID | Prioridade | Status | Item | Responsável proposto | Apoio | Critério de aceite |
| --- | --- | --- | --- | --- | --- | --- |
| BL-N1-001 | Alta | Pendente | Incorporar e revisar os três commits de qualidade existentes em `origin/main`. | Matheus | Lucas | `5ddd525`, `b0b10f6` e `5b5b048` fazem parte da linha de desenvolvimento usada pela equipe, sem conflito ou perda documental, e permanecem alinhados aos requisitos da N1. |
| BL-N1-002 | Alta | Pendente | Executar as suítes e os gates mínimos no mesmo commit candidato à entrega. | José e André | Lucas | José executa `mvn test`; André executa `npm run format`, `npm test` e `npm run build`; Lucas consolida data, ambiente, commit e resultados sem falhas. |
| BL-N1-003 | Crítica | Pendente | Validar a RN-001 sob concorrência usando MySQL. | José | Lucas | Com um técnico já responsável por um chamado `HIGH`, duas atribuições simultâneas resultam em uma aceitação, uma resposta `409/HIGH_URGENCY_LIMIT` e carga final igual a dois, com evidências. |
| BL-N1-004 | Alta | Pendente | Validar a persistência do volume MySQL. | José | Lucas | Após cadastrar dados, reiniciar e recriar os contêineres sem remover o volume, os mesmos IDs e vínculos permanecem consultáveis; evidências ficam registradas. |
| BL-N1-005 | Alta | Pendente | Reproduzir a stack em ambiente limpo e validar a topologia de produção. | Lucas | José e André | Migrations são aplicadas, frontend acessa a API pelo Nginx, backend acessa o MySQL e somente o frontend publica porta no host em produção. |
| BL-N1-006 | Alta | Pendente | Executar o roteiro funcional completo da N1 e registrar evidências por cenário de aceite. | Lucas e Matheus | José e André | Lucas executa e registra as validações; Matheus confere a aderência aos requisitos; todos os cenários possuem resultado e evidência vinculados ao commit testado. |
| BL-N1-007 | Média | Pendente | Exibir a data de atualização do chamado na interface. | André | José e Lucas | A tela apresenta `updatedAt` no formato definido e o valor muda após atribuição ou transição de status; o comportamento é validado por teste e evidência visual. |
| BL-N1-008 | Média | Pendente | Validar comunicação JSON, caracteres acentuados, datas e envelopes de erro. | José e André | Lucas e Matheus | José valida o contrato da API; André valida sua apresentação na interface; Lucas registra as evidências; Matheus confere a aderência aos requisitos. |
| BL-N1-009 | Média | Pendente | Produzir e revisar a medição inicial das métricas de qualidade da release N1. | Lucas e Matheus | José e André | Lucas consolida os indicadores; Matheus avalia a prontidão da entrega; o registro contém commit, data, testes, cobertura de requisitos e aceite, cenários pendentes e bugs por severidade. |
| BL-N1-010 | Alta | Pendente | Homologar formalmente a entrega N1. | Matheus | Lucas | Todos os cenários obrigatórios foram executados, não há falha pendente nem bug crítico/alto ligado à entrega, e o aceite do PO está registrado. |

## Ordem recomendada de execução

1. Sincronizar a documentação de qualidade (`BL-N1-001`).
2. Executar os testes automatizados e corrigir qualquer regressão (`BL-N1-002`).
3. Resolver a lacuna funcional de `updatedAt` (`BL-N1-007`).
4. Executar as validações em MySQL e da infraestrutura (`BL-N1-003` a `BL-N1-005`).
5. Concluir o roteiro funcional e os testes de contrato (`BL-N1-006` e `BL-N1-008`).
6. Consolidar as métricas e realizar a homologação (`BL-N1-009` e `BL-N1-010`).

## Referências

- [Escopo funcional](../produto/escopo.md)
- [Premissa inicial e critérios de aceite da N1](../produto/premissa-inicial%20N1.md)
- [Checklist de requisitos da N1](../qualidade/checklist-requisitos-n1.md)
- [Estratégia de testes](../qualidade/estrategia-testes.md)
- [Matriz RACI](raci.md)
- [Política de débito técnico](politica-debito-tecnico.md)

## Débitos técnicos da N1

Os registros abaixo seguem os campos obrigatórios da
[política de gestão de débito técnico](politica-debito-tecnico.md). Eles descrevem pendências
estruturais identificadas no estado atual do projeto. Lacunas funcionais e atividades pontuais de
validação permanecem no backlog da N1 acima. Os débitos devem entrar no refinamento conforme
prioridade, risco, impacto e custo de postergação.

### DT-N1-002 — Linters planejados ainda não estão configurados

| Campo | Registro |
| --- | --- |
| Status | Pendente |
| Descrição do problema | A documentação prevê ESLint no frontend e Spotless ou Checkstyle no backend antes da integração do primeiro módulo funcional, mas essas ferramentas não foram identificadas na configuração atual. |
| Parte afetada | Padronização e análise estática dos códigos Angular e Java. |
| Motivo da solução definitiva não ter sido aplicada | O histórico analisado não registra uma justificativa. Os módulos funcionais foram integrados mantendo apenas Prettier, compilação TypeScript e verificações Maven já disponíveis. |
| Impacto e risco | Problemas de estilo, legibilidade e padrões de código dependem de revisão manual, aumentando o custo de manutenção e o risco de inconsistências. |
| Prioridade | Média, por afetar manutenção, revisão e evolução do código. |
| Responsável pela correção | André no frontend e José no backend, com acompanhamento de Lucas. |
| Critério de aceite | Configurar ESLint no frontend e Spotless ou Checkstyle no backend; documentar os comandos; executar as verificações sem falhas; revisar a configuração e encerrar o item com referência ao pull request. |

### DT-N1-004 — Cobertura de testes dos componentes do frontend está incompleta

| Campo | Registro |
| --- | --- |
| Status | Pendente |
| Descrição do problema | Os componentes de equipamentos, técnicos e página inicial não possuem arquivos de teste próprios. A estratégia do projeto prevê testes de componentes, formulários, serviços e estados da interface. |
| Parte afetada | `EquipmentComponent`, `TechnicianComponent`, `HomeComponent` e os fluxos de cadastro, busca, carga técnica e métricas da página inicial. |
| Motivo da solução definitiva não ter sido aplicada | O histórico analisado não registra uma justificativa. Foram identificados testes para setores, chamados e tratamento de erros, mas não para esses três componentes. |
| Impacto e risco | Validações de formulário, normalização dos dados, filtros, mensagens de erro e cálculos do dashboard podem sofrer regressões sem detecção automatizada no frontend. |
| Prioridade | Média, por afetar testes, manutenção e evolução dos fluxos da N1. |
| Responsável pela correção | André, com apoio de Lucas. |
| Critério de aceite | Adicionar testes dos comportamentos relevantes dos três componentes, incluindo validação e normalização dos cadastros, busca, carga dos técnicos, métricas, estados de carregamento e erros; executar `npm test` sem falhas; revisar os testes e encerrar o item com referência ao pull request. |
