# Industria Plus

Helpdesk para organizar a manutencao preventiva e corretiva de equipamentos industriais. A base
tecnica utiliza Angular no frontend, Spring Boot no backend e MySQL em Docker.

## Equipe

Grupo: `Code Penguins`

- Matheus Büsemayer: Product Owner (PO) e Engenheiro de Requisitos
- Lucas Mönich Nunes: Quality Assurance (QA) e DevOps
- André Schultz: Desenvolvedor Frontend
- José Henrique Brühmüller: Desenvolvedor Backend

## Escopo do produto

O N1 entrega o cadastro de equipamentos e setores, a abertura de chamados com nivel de urgencia
e a atribuicao dos chamados aos tecnicos.

Regra de negocio critica: um tecnico nao pode ter mais de dois chamados com urgencia `Alta`
abertos ao mesmo tempo. Essa validacao deve existir obrigatoriamente no backend.

As evolucoes N2/N3 incluem historico de troca de pecas por maquina, calculo de MTBF e
agendamento automatico de manutencoes preventivas.

## O que esta pronto

- organizacao inicial do desenvolvimento documentada
- escopo N1 e evolucoes N2/N3 delimitados
- estrutura base de `frontend/`, `backend/` e `docs/`
- `compose.yaml` para subir frontend, backend e MySQL
- pagina inicial no Angular consumindo o health check da API
- endpoint `/api/health` no backend para smoke test

## Stack definida

- Frontend: Angular 21
- Backend: Java 17 com Spring Boot 3
- Banco de dados: MySQL 8.4 em Docker
- Persistencia: Spring Data JPA e Flyway
- Padroes de codigo: `.editorconfig` na raiz e Prettier no frontend

## Estrutura principal

- `frontend/`: aplicacao Angular
- `backend/`: API Spring Boot
- `docs/`: governanca, qualidade e arquitetura inicial
- `compose.yaml`: stack local com MySQL, backend e frontend
- `run-local.ps1`: subida rapida da stack
- `stop-local.ps1`: parada da stack

## Documentacao de kickoff

- [Escopo funcional](docs/produto/escopo.md)
- [Matriz RACI](docs/governanca/raci.md)
- [Politica de debito tecnico](docs/governanca/politica-debito-tecnico.md)
- [Checklist de aprovacao de requisitos](docs/governanca/aprovacao-requisitos.md)
- [Estrategia de testes](docs/qualidade/estrategia-testes.md)
- [Stack e organizacao tecnica](docs/arquitetura/stack.md)
- [Branching, commits e linters](docs/qualidade/branching-commits-linters.md)

## Como executar com Docker

1. Crie o arquivo local de ambiente:

```powershell
Copy-Item .env.example .env
```

2. Suba a stack:

```powershell
.\run-local.ps1 -Build
```

URLs padrao:

- Frontend: `http://localhost:4202`
- Backend: `http://localhost:8082`
- API: `http://localhost:8082/api`
- Health: `http://localhost:8082/api/health`
- MySQL: `127.0.0.1:3308`

## Como executar manualmente

### Backend

```powershell
Get-Content .\.env | ForEach-Object {
  if ($_ -and -not $_.StartsWith('#')) {
    $name, $value = $_.Split('=', 2)
    Set-Item -Path "Env:$name" -Value $value
  }
}

cd backend
mvn spring-boot:run
```

### Frontend

```powershell
cd frontend
npm install
npm start
```

## Validacoes iniciais

### Backend

```powershell
cd backend
mvn test
```

### Frontend

```powershell
cd frontend
npm run build
```
