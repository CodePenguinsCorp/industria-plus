# Industria Plus

Helpdesk para organizar a manutenção preventiva e corretiva de equipamentos industriais. A base
técnica utiliza Angular no frontend, Spring Boot no backend e MySQL em Docker.

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

## O que está pronto

- dashboard operacional com prioridades, indicadores e health check
- cadastro e consulta de setores, equipamentos e técnicos
- abertura de chamados preventivos e corretivos com urgência `Baixa`, `Média` ou `Alta`
- fila ordenada por prioridade e filtrável por status e urgência
- atribuição e reatribuição com a carga de chamados `Alta` visível por técnico
- ciclo de vida `Aberto` -> `Em andamento` -> `Encerrado`, com encerramento direto permitido
- RN-001 protegida por transação e bloqueio pessimista no backend
- migrations Flyway, erros padronizados e testes automatizados de frontend e backend
- `compose.yaml` e scripts PowerShell para subir frontend, backend e MySQL

## Stack definida

- Frontend: Angular 21
- Estilos: CSS puro global e encapsulado por componente
- Backend: Java 17 com Spring Boot 3
- Banco de dados: MySQL 8.4 em Docker
- Persistência: Spring Data JPA e Flyway
- Padrões de código: `.editorconfig` na raiz e Prettier no frontend

## Estrutura principal

- `frontend/`: aplicação Angular
- `backend/`: API Spring Boot
- `docs/`: produto, governança, qualidade e arquitetura
- `compose.yaml`: stack local com MySQL, backend e frontend
- `run-local.ps1`: subida rápida da stack
- `stop-local.ps1`: parada da stack

## Documentação

- [Escopo funcional](docs/produto/escopo.md)
- [Refinamento e contrato do N1](docs/produto/refinamento-n1.md)
- [Matriz RACI](docs/governanca/raci.md)
- [Política de débito técnico](docs/governanca/politica-debito-tecnico.md)
- [Checklist de aprovação de requisitos](docs/governanca/aprovacao-requisitos.md)
- [Estratégia de testes](docs/qualidade/estrategia-testes.md)
- [Stack e organização técnica](docs/arquitetura/stack.md)
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

URLs padrão:

- Frontend: `http://localhost:4202`
- Backend: `http://localhost:8082`
- API: `http://localhost:8082/api`
- Health: `http://localhost:8082/api/health`
- MySQL: `127.0.0.1:3308`

## Fluxo principal do N1

1. Cadastre um setor em `/setores`.
2. Cadastre um equipamento vinculado ao setor em `/equipamentos`.
3. Cadastre os técnicos em `/tecnicos`.
4. Abra, priorize e atribua chamados em `/chamados`.
5. Inicie ou encerre o atendimento pela fila de chamados.

A API utiliza o contexto `/api` e expõe os recursos `/sectors`, `/equipments`, `/technicians` e
`/maintenance-requests`. O contrato completo, incluindo payloads, filtros e erros, está no
[refinamento do N1](docs/produto/refinamento-n1.md).

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

## Validações

### Backend

```powershell
cd backend
mvn test
```

### Frontend

```powershell
cd frontend
npm run format
npm test
npm run build
```
