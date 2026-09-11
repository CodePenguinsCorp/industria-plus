# Industria Plus

Helpdesk para organizar a manutenção preventiva e corretiva de equipamentos industriais. O sistema
usa Angular no frontend, Spring Boot no backend e MySQL, com execução local e implantação
conteinerizadas por Docker Compose.

## Equipe

Grupo: `Code Penguins`

- Matheus Büsemayer: Product Owner (PO) e Engenheiro de Requisitos
- Lucas Mönich Nunes: Quality Assurance (QA) e DevOps
- André Schultz: Desenvolvedor Frontend
- José Henrique Brühmüller: Desenvolvedor Backend

## Escopo do produto

O N1 entrega o cadastro e a consulta de setores, equipamentos e técnicos, a abertura de chamados
preventivos ou corretivos, filtros por status e urgência, atribuição e reatribuição de técnicos e o
acompanhamento do chamado até o encerramento.

Regra de negócio crítica: um técnico não pode ter mais de dois chamados com urgência `Alta`
abertos ao mesmo tempo. A validação é transacional no backend e protege atribuições concorrentes.

As evoluções N2/N3 incluem histórico de troca de peças por máquina, cálculo de MTBF e
agendamento automático de manutenções preventivas.

## Stack

- Frontend: Angular 21, servido por Nginx na imagem Docker
- Backend: Java 17 com Spring Boot 3.5
- Banco de dados: MySQL 8.4
- Persistência: Spring Data JPA e Flyway
- Infraestrutura: Docker Compose para desenvolvimento e produção
- Qualidade: testes Maven/Vitest, Prettier e `.editorconfig`

## Estrutura principal

- `frontend/`: aplicação Angular e configuração do Nginx
- `backend/`: API Spring Boot e migrations Flyway
- `docs/`: produto, governança, qualidade e arquitetura
- `compose.yaml`: stack local, construída a partir do código-fonte
- `compose.prod.yaml`: stack de produção, baseada em imagens publicadas
- `run-local.ps1` e `stop-local.ps1`: controle da stack local no Windows
- `scripts/init-prod-env.*`: criação segura do ambiente de produção
- `scripts/deploy-prod.*`: atualização e subida da stack de produção

## Pré-requisitos

Para a execução recomendada, instale Docker Desktop (Windows/macOS) ou Docker Engine com o
plugin Compose (Linux). Para desenvolvimento sem containers também são necessários Java 17,
Maven 3.9+, Node.js 22 e npm 10.

## Execução local com Docker

Na raiz do repositório, crie o arquivo de configuração local e suba a stack:

```powershell
Copy-Item .env.example .env
.\run-local.ps1 -Build
```

O primeiro build pode demorar enquanto Maven, npm e Docker baixam as dependências. Nos próximos
boots, quando não houver alteração de código ou dependências, basta executar:

```powershell
.\run-local.ps1
```

Endereços padrão:

- aplicação: `http://localhost:4202`
- backend: `http://localhost:8082`
- API: `http://localhost:8082/api`
- health check: `http://localhost:8082/api/health`
- MySQL: `127.0.0.1:3308`

Os valores podem ser alterados no `.env`. Esse arquivo é local e não deve ser commitado.

### Controle dos serviços

```powershell
# Reconstrói e sobe toda a aplicação
.\run-local.ps1 -Build

# Sobe somente o banco para desenvolvimento manual
.\run-local.ps1 -DatabaseOnly

# Sobe somente o backend (e sua dependência de banco)
.\run-local.ps1 -BackendOnly

# Sobe o frontend e as dependências exigidas pelo Compose
.\run-local.ps1 -FrontendOnly

# Para toda a stack
.\stop-local.ps1

# Para toda a stack e apaga os dados locais do MySQL
.\stop-local.ps1 -RemoveVolumes
```

`-RemoveVolumes` elimina o volume do banco e deve ser usado somente quando a perda dos dados
locais for intencional.

## Dockerização do banco

O serviço `mysql` está definido tanto no ambiente local quanto no de produção com:

- imagem oficial `mysql:8.4`;
- volume nomeado `mysql_data`, que preserva os dados entre reinícios;
- health check com `mysqladmin ping`;
- backend aguardando o banco ficar saudável antes de iniciar;
- banco, usuário e senhas recebidos por variáveis de ambiente;
- migrations Flyway executadas pelo backend ao conectar.

No ambiente local, a porta do MySQL é publicada como `3308` por padrão para permitir ferramentas
externas e a execução manual do backend. Em produção, o banco não publica porta no host: somente o
backend consegue acessá-lo pela rede interna do Compose, usando o hostname `mysql` e a porta
`3306`.

As variáveis `MYSQL_*` inicializam uma instalação nova. Se o volume `mysql_data` já existir, trocar
as senhas no arquivo de ambiente não recria automaticamente usuários nem altera suas credenciais.

Para verificar o estado e os logs do banco local:

```powershell
docker compose ps mysql
docker compose logs -f mysql
docker compose exec mysql mysqladmin ping -h 127.0.0.1 -uroot -p
```

## Implantação em produção

A topologia de produção expõe apenas o Nginx do frontend. Requisições para `/api` são encaminhadas
internamente ao backend, e o backend acessa o MySQL pela rede privada do Compose:

```text
Navegador -> frontend/Nginx :80 -> backend :8080 -> mysql :3306 -> volume mysql_data
```

### 1. Publique as imagens

Construa e publique as imagens do backend e do frontend em um registry acessível pelo servidor,
como GHCR, Docker Hub ou um registry privado:

```powershell
docker build -t ghcr.io/organizacao/industria-plus/backend:1.0.0 .\backend
docker build -t ghcr.io/organizacao/industria-plus/frontend:1.0.0 .\frontend
docker push ghcr.io/organizacao/industria-plus/backend:1.0.0
docker push ghcr.io/organizacao/industria-plus/frontend:1.0.0
```

Se o registry for privado, execute `docker login` no servidor antes do deploy.

### 2. Gere o ambiente de produção

No servidor, após clonar o repositório, gere `.env.prod` com senhas aleatórias e informe as imagens
publicadas:

```powershell
.\scripts\init-prod-env.ps1 `
  -BackendImage "ghcr.io/organizacao/industria-plus/backend:1.0.0" `
  -FrontendImage "ghcr.io/organizacao/industria-plus/frontend:1.0.0"
```

No Linux:

```sh
BACKEND_IMAGE=ghcr.io/organizacao/industria-plus/backend:1.0.0 \
FRONTEND_IMAGE=ghcr.io/organizacao/industria-plus/frontend:1.0.0 \
sh ./scripts/init-prod-env.sh
```

O arquivo `.env.prod` é ignorado pelo Git. Guarde uma cópia segura das credenciais. Para revisar o
formato sem valores reais, consulte `.env.prod.example`.

### 3. Faça o deploy

```powershell
.\scripts\deploy-prod.ps1
```

Ou, no Linux:

```sh
sh ./scripts/deploy-prod.sh
```

Os scripts validam os arquivos, baixam as imagens configuradas, atualizam os containers em segundo
plano e exibem o estado da stack. Para uma nova versão, altere `BACKEND_IMAGE` e `FRONTEND_IMAGE`
no `.env.prod` e execute novamente o mesmo comando.

Comandos operacionais úteis:

```powershell
docker compose --env-file .env.prod -f compose.prod.yaml ps
docker compose --env-file .env.prod -f compose.prod.yaml logs -f
docker compose --env-file .env.prod -f compose.prod.yaml down
```

Não use `down -v` em produção, pois esse parâmetro remove o volume persistente do MySQL.

## Execução manual para desenvolvimento

Suba somente o banco:

```powershell
Copy-Item .env.example .env
.\run-local.ps1 -DatabaseOnly
```

Em outro terminal, carregue as variáveis e execute o backend:

```powershell
Get-Content .\.env | ForEach-Object {
  if ($_ -and -not $_.StartsWith('#')) {
    $name, $value = $_.Split('=', 2)
    Set-Item -Path "Env:$name" -Value $value
  }
}

Set-Location backend
mvn spring-boot:run
```

Em um terceiro terminal, execute o frontend:

```powershell
Set-Location frontend
npm ci
npm start
```

## Validações

```powershell
# Backend
Set-Location backend
mvn test

# Frontend (execute a partir de frontend/)
npm run format
npm test
npm run build

# Validação estrutural dos arquivos Compose (a partir da raiz)
docker compose config --quiet
docker compose --env-file .env.prod.example -f compose.prod.yaml config --quiet
```

## Fluxo principal do N1

1. Cadastre um setor em `/setores`.
2. Cadastre um equipamento vinculado ao setor em `/equipamentos`.
3. Cadastre os técnicos em `/tecnicos`.
4. Abra, priorize e atribua chamados em `/chamados`.
5. Inicie ou encerre o atendimento pela fila de chamados.

A API usa o contexto `/api` e expõe `/sectors`, `/equipments`, `/technicians` e
`/maintenance-requests`. O contrato completo está no
[premissa inicial e critérios de aceite da N1](docs/produto/premissa-inicial%20N1.md).

## Documentação

- [Escopo funcional](docs/produto/escopo.md)
- [Premissa inicial e critérios de aceite da N1](docs/produto/premissa-inicial%20N1.md)
- [Matriz RACI](docs/governanca/raci.md)
- [Política de débito técnico](docs/governanca/politica-debito-tecnico.md)
- [Checklist de aprovação de requisitos](docs/governanca/aprovacao-requisitos.md)
- [Estratégia de testes](docs/qualidade/estrategia-testes.md)
- [Stack e organização técnica](docs/arquitetura/stack.md)
- [Branching, commits e linters](docs/qualidade/branching-commits-linters.md)
