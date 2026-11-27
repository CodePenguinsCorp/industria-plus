# Revisão de acesso e histórico

## Escopo e origem

O pacote recebido acrescenta sessão, CSRF, perfis Administrador/Gestor/Técnico, primeiro acesso, gestor do técnico, histórico e peças. A importação é um snapshot de terceiros; autoria formal e inventário completo de propostas ainda precisam ser confirmados pela equipe. O ZIP original e seu hash estão preservados. Credenciais locais não foram importadas.

Nesta retomada foram corrigidos separadamente o e-mail normalizado, o bootstrap quando já existem técnicos migrados e o cookie seguro de produção. Outros commits ativam CI para bases encadeadas e fixam LF no checkout. `POST /technicians` deixou de existir; técnicos são criados por `POST /users`.

## Planejamento atendido

- Evolução de acesso descrita em `ATUALIZACAO.md` e histórico/peças previsto no escopo do produto.
- Revisão de regressões e configuração solicitada na retomada.
- Preservação das migrations e registros existentes.

## Testes executados em 27/11/2026

Ver resumo JSON neste diretório. Backend H2: 92 testes. Frontend: 33 testes. Formatação, build e validação dos dois arquivos Compose são gates obrigatórios desta branch. Resultados pertencem ao commit identificado no resumo; este documento acrescenta somente instruções de revisão.

A correção da concorrência MySQL pertence ao PR seguinte. Esta branch isolada não recebe atestado de concorrência MySQL. Backup/restauração e HTTPS real continuam pendentes.

## Roteiro manual de QA

Todos os cenários abaixo aguardam execução e registro da equipe.

| Cenário | Procedimento | Resultado esperado |
| --- | --- | --- |
| Primeiro administrador | Subir base de teste contendo técnicos migrados, sem administrador, com credenciais iniciais configuradas. | Administrador criado; técnico existente preservado. Repetir inicialização não duplica nem redefine conta. |
| Cadastro normalizado | Cadastrar usuário com espaços externos no e-mail e letras maiúsculas. Tentar novamente com mesmo e-mail e com formato inválido. | Primeiro cadastro aceito e normalizado; duplicidade e formato inválido recusados sem novo registro. |
| Primeiro acesso | Criar gestor e técnico com senha provisória. Entrar como técnico antes e depois de criar sua senha. | Antes da troca, operações de domínio bloqueadas; depois, acesso conforme papel. |
| Permissões | Tentar ações administrativas e de gestor como técnico, inclusive pela API com sessão e CSRF. | Respostas de recusa sem alteração dos dados. |
| Histórico e peças | Atribuir chamado, registrar serviço e duas peças, alterar status e consultar equipamento. | Eventos com autor/data, nomes dos responsáveis e peças visíveis. Chamado encerrado não recebe novas alterações. |
| Sessão e CSRF | Entrar, sair e tentar gravação sem token válido. | Logout encerra acesso; gravação sem CSRF é recusada. |

## Revisão do PO

Confirmar os papéis, as restrições por perfil e a mudança no cadastro de técnicos. Confirmar a identificação do pacote recebido. Nenhum aceite foi registrado em nome do PO ou QA.

Para cada cenário, registrar ambiente, commit, executor, resultado obtido e evidência; assinalar aprovado/reprovado somente após execução.
