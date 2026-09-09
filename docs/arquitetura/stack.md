# Stack e organização técnica

## Decisões do projeto

- Frontend: Angular 21 com componentes standalone e TypeScript.
- Estilos: CSS puro global e por componente, sem pré-processador.
- Backend: Java 17 com Spring Boot 3.
- Banco: MySQL 8.4 em Docker.
- Persistência: Spring Data JPA e migrations com Flyway.
- Execução local: Docker Compose e scripts PowerShell.
- Idioma do código: inglês para nomes de arquivos, classes, funções, tabelas e campos.
- Idioma da interface e documentação: português brasileiro.

Estas decisões substituem a stack React, Node.js e PostgreSQL citada no acordo original, conforme a
definição posterior e explícita do projeto.

## Organização por domínio

O código funcional deve ser agrupado por domínio, evitando uma única pasta global para todas as
entidades ou serviços.

```text
frontend/src/app/
|-- core/                      # infraestrutura compartilhada
|-- layout/                    # shell e navegação
|-- pages/home/                # página de entrada
`-- features/
    |-- equipment/
    |-- sector/
    |-- technician/
    `-- maintenance-request/

backend/src/main/java/com/industriaplus/backend/
|-- config/                    # configurações transversais
|-- health/                    # verificação operacional
|-- equipment/                 # equipamentos
|-- sector/                    # setores
|-- technician/                # técnicos
`-- maintenancerequest/        # chamados e regra de atribuição
```

Cada domínio do backend pode conter controller, service, repository, entity e DTOs conforme ganhar
comportamento. Pastas e abstrações não devem ser criadas sem uso real.

## Restrições arquiteturais

- Controllers apenas validam entrada e delegam casos de uso.
- Regras de negócio ficam no backend, fora de controllers e entidades de interface.
- Acesso ao banco deve passar por repositories e migrations versionadas.
- A RN-001 deve ser transacional e protegida contra atribuições concorrentes.
- O frontend pode antecipar validações, mas deve tratar a resposta de negócio da API.
- N2/N3 não deve introduzir complexidade no modelo do N1 antes do refinamento.

## Convenções

- TypeScript: `camelCase` para variáveis e funções, `PascalCase` para tipos e componentes.
- Java: `camelCase` para métodos e atributos, `PascalCase` para classes e records.
- Banco: `snake_case` para tabelas, colunas, índices e constraints.
- Funções e classes devem ter uma responsabilidade clara e nomes orientados ao domínio.
