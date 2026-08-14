# Branching, commits, linters e CI

## Branches

- `main`: apenas codigo estavel e pronto para entrega.
- `develop`: integracao das funcionalidades da proxima versao.
- `feature/<descricao>`: nova funcionalidade criada a partir de `develop`.
- `fix/<descricao>`: correcao de defeito criada a partir de `develop`.
- `refactor/<descricao>`: mudanca estrutural sem alteracao de comportamento.

Nenhuma alteracao e feita diretamente em `main`. Features e correcoes entram em `develop` por pull
request; uma entrega aprovada segue de `develop` para `main` por outro pull request.

## Pull requests

- escopo unico e descricao objetiva
- referencia ao requisito, bug ou debito tecnico
- criterios de aceite e evidencias de teste preenchidos
- aprovacao de pelo menos outro integrante da equipe
- build, formatacao e testes obrigatorios aprovados
- preferencia por alteracoes pequenas; pull requests acima de 400 linhas devem ser justificados

## Commits

O projeto usa Conventional Commits, com mensagens curtas em ingles:

- `feat(equipment): add equipment registration`
- `fix(request): prevent third high urgency assignment`
- `refactor(maintenance): separate assignment rule`
- `test(technician): cover high urgency request limit`
- `docs(scope): document N1 requirements`
- `chore(ci): add backend test job`
- `style(frontend): format request form`

## Linters e formatacao

- `frontend`: Prettier, compilacao estrita do TypeScript e build Angular.
- `backend`: compilacao Maven, testes e convencoes Java verificadas no pull request.
- `repositorio`: `.editorconfig` obrigatorio para todos os arquivos.
- `evolucao do gate`: adicionar ESLint com angular-eslint e Spotless ou Checkstyle antes do primeiro
  modulo funcional ser integrado.

A documentacao nao deve indicar uma ferramenta como ativa antes de sua configuracao existir no
repositorio.

## Pipeline de CI

O pipeline deve instalar dependencias, verificar formatacao, executar testes, gerar os builds e
bloquear a integracao quando qualquer etapa falhar. Tambem bloqueia pull request sem revisao ou que
viole uma regra de negocio essencial.
