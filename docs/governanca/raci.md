# Matriz RACI

Esta matriz orienta o N1 do Helpdesk de Manutencao Industrial.

## Equipe e papeis

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
| Refinar o escopo e as historias do N1 | A | R | C | C | C | I |
| Aprovar requisitos para desenvolvimento | A | R | C | C | C | I |
| Definir arquitetura e stack | I | C | C | R | R | A |
| Gerenciar o debito tecnico | I | I | C | C | C | A/R |
| Planejar a estrategia de testes | I | C | A/R | C | C | C |
| Implementar o frontend | I | C | C | A/R | C | I |
| Implementar regras e APIs do backend | I | C | C | C | A/R | I |
| Modelar e versionar o banco de dados | I | C | C | I | R | A |
| Revisar pull requests | I | I | C | R | R | C |
| Executar testes e registrar defeitos | I | C | A/R | C | C | I |
| Manter pipeline e ambientes | I | I | C | C | C | A/R |
| Homologar uma entrega | A | C | R | I | I | I |

## Legenda

- `R`: executa a atividade
- `A`: responde pelo resultado e concede a aprovacao final
- `C`: participa da decisao ou revisao
- `I`: deve ser informado sobre o andamento

## Regras de uso

- Toda historia deve ter um responsavel tecnico antes de entrar em desenvolvimento.
- A aprovacao exige escopo validado pelo PO, detalhamento pelo ER e testabilidade pelo QA.
- Todo pull request precisa da aprovacao de pelo menos outro integrante da equipe.
- Impedimentos e mudancas de escopo devem ser comunicados ao PO e aos papeis afetados.
