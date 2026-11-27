# Atualização: login, perfis e histórico

## Confiabilidade e preventivas — 24/11/2026

A retomada acrescentou MTBF com horas reais e período informado pelo gestor, plano preventivo por
equipamento, geração automática de chamados e migration V5. Consulte
[confiabilidade e preventivas](docs/produto/evolucao-n3.md) para critérios, limites e endpoints. As seções abaixo
descrevem o pacote recebido e as migrations V3/V4; os limites indicados para aquele pacote não
substituem o estado atual documentado nessa evolução.

Esta versão mantém o estilo original e os cadastros/chamados existentes. A migração V3 adiciona tabelas para usuários, histórico e peças. A migração V4 transforma os técnicos em usuários (perfil Técnico), remove a tabela `technicians` sem perder registros, adiciona o gestor vinculado ao técnico e a troca obrigatória da senha provisória.

### O que a migração V4 faz com os dados existentes

- Técnicos que já tinham acesso reaproveitam esse usuário; a especialidade é copiada.
- Técnicos sem acesso viram usuários com perfil Técnico **sem senha utilizável**. Para liberar o login, o administrador usa **Definir senha provisória** em Usuários.
- Se o e-mail de um técnico sem acesso já pertencer a outro usuário, ele recebe o e-mail `tecnico-<id>@migracao.invalid`, que pode ser ajustado no banco se necessário.
- Chamados e histórico passam a apontar para o usuário técnico; os nomes registrados no histórico são preservados.
- Técnicos migrados ficam sem gestor vinculado; o administrador escolhe o gestor na tela **Técnicos**.
- Usuários que já existiam mantêm a senha atual, sem troca obrigatória.

## Atualizar no Windows preservando os dados

1. Faça uma cópia de segurança do banco e do arquivo `.env` antes de atualizar.
2. Na pasta que você já está usando, pare os serviços com `docker compose down`. **Não use `-v`, `-RemoveVolumes` ou exclua volumes no Docker Desktop.**
3. Extraia este ZIP e copie o conteúdo de `industria-plus-main` para a pasta original, substituindo os arquivos do projeto. Mantenha o nome/local da pasta original e seu `.env` para continuar usando o mesmo projeto Compose e volume MySQL.
4. No `.env` original, acrescente estas linhas, escolhendo sua própria senha:

```dotenv
INITIAL_ADMIN_EMAIL=admin@industria.local
INITIAL_ADMIN_PASSWORD=SUA_SENHA_ESCOLHIDA_AQUI
SESSION_COOKIE_SECURE=false
```

A senha precisa ter pelo menos 12 caracteres e no máximo 72 bytes. Para facilitar o arquivo `.env`, use letras, números e símbolos como `!`, evitando `$`, `#`, aspas e espaços. O texto `SUA_SENHA_ESCOLHIDA_AQUI` é um marcador: substitua-o por uma senha exclusiva.

5. Com o Docker Desktop aberto, execute no terminal da pasta original:

```powershell
powershell -ExecutionPolicy Bypass -File .\run-local.ps1 -Build
```

6. Abra http://localhost:4202 e entre com o e-mail e a senha escolhidos.

O primeiro administrador é criado quando ainda não existe um usuário com perfil Administrador, mesmo que já existam técnicos migrados. Use um e-mail que não pertença a outro usuário: o bootstrap não promove contas nem altera suas senhas. Quando já há administrador, alterar essa variável não redefine senhas nem cria outro administrador. Após confirmar o primeiro acesso, você pode remover `INITIAL_ADMIN_PASSWORD` do `.env` e recriar o backend; o acesso continua armazenado no banco com senha BCrypt.

Para instalação nova, copie `.env.example` para `.env`, preencha a senha e execute o mesmo comando.

## Perfis e permissões

| Ação | Administrador | Gestor | Técnico |
|---|---|---|---|
| Consultar dashboard, setores e equipamentos | Sim | Sim | Sim |
| Tela Técnicos | Editar (gestor vinculado e exclusão) | Visualizar | Não aparece |
| Consultar chamados e histórico | Sim | Sim | Sim |
| Criar e excluir setores e equipamentos sem vínculos impeditivos | Sim | Sim | Não |
| Abrir chamado e atribuir/reatribuir técnico | Sim | Sim | Não |
| Alterar status e registrar serviço/peças | Sim | Sim | Apenas chamados atribuídos ao próprio técnico |
| Cadastrar e listar acessos em Usuários (inclusive técnicos) e definir senha provisória | Sim | Não | Não |

As permissões são validadas no backend; ocultar botões na tela é apenas uma orientação ao usuário. A consulta de chamados é compartilhada entre os perfis; as operações de atendimento do técnico são restritas ao vínculo cadastrado.

## Criar gestores e técnicos

- Entre como administrador e abra **Usuários** no menu.
- Informe nome, e-mail, senha provisória e perfil.
- Cadastre primeiro os gestores. Para o perfil Técnico, selecione o **Gestor vinculado** (obrigatório) e, se quiser, a especialidade. Não existe mais cadastro separado na tela Técnicos.
- A tabela **Acessos cadastrados** lista todos os usuários, com o gestor vinculado dos técnicos e a situação da senha (provisória ou definida).
- **Definir senha provisória** gera uma nova senha provisória para qualquer usuário; ela deverá ser trocada no próximo login.
- Use **Sair** para encerrar a sessão e testar outro perfil.

## Primeiro acesso

- Todo acesso criado pelo administrador começa com senha provisória.
- Ao entrar, o usuário é levado à tela **Crie sua senha**: informa a senha provisória, a nova senha (12 caracteres a 72 bytes, diferente da provisória) e a confirmação.
- Até a troca, o backend recusa as demais operações com `403 PASSWORD_CHANGE_REQUIRED`; apenas `/auth/me`, `/auth/password` e `/auth/logout` ficam liberados.
- O primeiro administrador, configurado pelo `.env`, não precisa trocar a senha.

## Equipamentos e atendimento

- Em **Equipamentos**, clique em **Detalhes** para consultar os chamados, intervenções, peças trocadas e histórico completo da máquina.
- Em **Chamados**, clique em **Histórico e serviço realizado**.
- Descreva o serviço; se houver peças trocadas, informe o nome e a quantidade e clique em **Adicionar peça**. Podem ser incluídas várias peças, ou nenhuma.
- Clique em **Salvar atendimento** antes de encerrar o chamado na fila.
- Cada abertura, alteração de status, atribuição/reatribuição e intervenção registra autor, data e informações da operação. Mudanças de responsável preservam os nomes anteriores.
- Chamados encerrados continuam consultáveis, mas não recebem novas intervenções nem alterações.
- O histórico começa a ser registrado nesta atualização. Atendimentos e mudanças anteriores não são reconstruídos, pois esses dados não eram armazenados.

## API adicionada

- `GET /api/auth/csrf`: inicializa o token CSRF.
- `POST /api/auth/login`: formulário URL-encoded com `email` e `password`; cria sessão.
- `GET /api/auth/me`: usuário da sessão atual.
- `POST /api/auth/logout`: encerra a sessão.
- `POST /api/auth/password`: troca da própria senha (`currentPassword`, `newPassword`).
- `GET/POST /api/users`: consulta/criação de usuários, exclusiva do administrador. Para técnicos, envie `managerId` e, opcionalmente, `specialty`.
- `POST /api/users/{id}/password-reset`: define nova senha provisória (`password`), exclusiva do administrador.
- `GET /api/technicians`: equipe com carga de urgência Alta e gestor vinculado.
- `GET /api/technicians/managers`: gestores disponíveis (administrador e gestor).
- `PATCH /api/technicians/{id}/manager`: altera o gestor vinculado (`managerId`), exclusivo do administrador.
- `DELETE /api/technicians/{id}`: exclui o técnico sem chamados, exclusivo do administrador. `POST /api/technicians` deixou de existir.
- `GET /api/equipments/{id}`: equipamento, chamados e histórico.
- `GET /api/maintenance-requests/{id}`: dados do chamado.
- `GET /api/maintenance-requests/{id}/history`: histórico do atendimento.
- `POST /api/maintenance-requests/{id}/interventions`: serviço realizado e peças.

Requisições de escrita precisam do cookie de sessão e do cabeçalho `X-XSRF-TOKEN`. O Angular usa o cookie `XSRF-TOKEN` automaticamente. No Postman, obtenha primeiro `/auth/csrf`, mantenha os cookies e copie o token para esse cabeçalho; renove-o após o login.

Exemplo de intervenção:

```json
{
  "description": "Substituído o rolamento e testada a máquina.",
  "parts": [{ "name": "Rolamento", "quantity": 2 }]
}
```

## Validação e limites

Testes automatizados: o backend tem testes unitários (JUnit 5 + Mockito) das regras de negócio isoladas — limite de urgência Alta (RN-001), transições de status, repetição de atribuição (RN-004), ordenação da fila (RN-005), cadastro de usuários e vínculo técnico-gestor, senha provisória e troca obrigatória, permissões por perfil, histórico/peças e bloqueio do primeiro acesso — além dos testes de integração com MockMvc e H2 (login/sessão, CSRF, papéis, primeiro acesso, redefinição de senha, gestor vinculado e migração V4). O frontend tem 33 testes (Vitest). A migração V4 também foi conferida em MySQL 8 com dados de exemplo.

Não foram adicionados recuperação de senha por e-mail (o administrador define uma senha provisória), edição/desativação de usuários, agendamento automático ou cálculo de MTBF. A sessão expira após 30 minutos de inatividade e é perdida ao reiniciar o backend; basta entrar novamente.

Em produção, use HTTPS e `SESSION_COOKIE_SECURE=true`. A validação local pode usar HTTP com `false`.
