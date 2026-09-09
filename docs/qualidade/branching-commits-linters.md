# Branching, commits, linters e CI

## Branches

- `main`: apenas código estável e pronto para entrega.
- `develop`: integração das funcionalidades da próxima versão.
- `feature/<descricao>`: nova funcionalidade criada a partir de `develop`.
- `fix/<descricao>`: correção de defeito criada a partir de `develop`.
- `refactor/<descricao>`: mudança estrutural sem alteração de comportamento.

Nenhuma alteração é feita diretamente em `main`. Features e correções entram em `develop` por pull
request; uma entrega aprovada segue de `develop` para `main` por outro pull request.

## Pull requests

- escopo único e descrição objetiva
- referência ao requisito, bug ou débito técnico
- critérios de aceite e evidências de teste preenchidos
- aprovação de pelo menos outro integrante da equipe
- build, formatação e testes obrigatórios aprovados
- preferência por alterações pequenas; pull requests acima de 400 linhas devem ser justificados

## Commits

O projeto usa Conventional Commits, com mensagens curtas em inglês:

- `feat(equipment): add equipment registration`
- `fix(request): prevent third high urgency assignment`
- `refactor(maintenance): separate assignment rule`
- `test(technician): cover high urgency request limit`
- `docs(scope): document N1 requirements`
- `chore(ci): add backend test job`
- `style(frontend): format request form`

## Linters e formatação

- `frontend`: Prettier, compilação estrita do TypeScript e build Angular.
- `backend`: compilação Maven, testes e convenções Java verificadas no pull request.
- `repositório`: `.editorconfig` obrigatório para todos os arquivos.
- `evolução do gate`: adicionar ESLint com angular-eslint e Spotless ou Checkstyle antes do primeiro
  módulo funcional ser integrado.

A documentação não deve indicar uma ferramenta como ativa antes de sua configuração existir no
repositório.

## Pipeline de CI

O pipeline deve instalar dependências, verificar formatação, executar testes, gerar os builds e
bloquear a integração quando qualquer etapa falhar. Também bloqueia pull request sem revisão ou que
viole uma regra de negócio essencial.
