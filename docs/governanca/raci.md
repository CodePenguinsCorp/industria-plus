# Matriz RACI

Esta matriz orienta o N1 do Helpdesk de Manutenção Industrial.

## Equipe e papéis

| Sigla | Papel | Integrante |
| --- | --- | --- |
| `PO` | Product Owner | Matheus Büsemayer |
| `ER` | Engenheiro de Requisitos | Matheus Büsemayer |
| `QA` | Quality Assurance | Lucas Mönich Nunes |
| `FE` | Desenvolvedor Frontend | André Schultz |
| `BE` | Desenvolvedor Backend | José Henrique Brühmüller |
| `DO` | DevOps | Lucas Mönich Nunes |

## Responsabilidades

| Atividade | PO | ER | QA | FE | BE | DO |
| --- | --- | --- | --- | --- | --- | --- |
| Refinar o escopo e as histórias do N1 | A | R | C | C | C | I |
| Aprovar requisitos para desenvolvimento | A | R | C | C | C | I |
| Definir arquitetura e stack | I | C | C | R | R | A |
| Gerenciar o débito técnico | I | I | C | C | C | A/R |
| Planejar a estratégia de testes | I | C | A/R | C | C | C |
| Implementar o frontend | I | C | C | A/R | C | I |
| Implementar regras e APIs do backend | I | C | C | C | A/R | I |
| Modelar e versionar o banco de dados | I | C | C | I | R | A |
| Revisar pull requests | I | I | C | R | R | C |
| Executar testes e registrar defeitos | I | C | A/R | C | C | I |
| Manter pipeline e ambientes | I | I | C | C | C | A/R |
| Homologar uma entrega | A | C | R | I | I | I |

## Legenda

- `R`: executa a atividade
- `A`: responde pelo resultado e concede a aprovação final
- `C`: participa da decisão ou revisão
- `I`: deve ser informado sobre o andamento

## Regras de uso

- Toda história deve ter um responsável técnico antes de entrar em desenvolvimento.
- A aprovação exige escopo validado pelo PO, detalhamento pelo ER e testabilidade pelo QA.
- Todo pull request precisa da aprovação de pelo menos outro integrante da equipe.
- Impedimentos e mudanças de escopo devem ser comunicados ao PO e aos papéis afetados.
