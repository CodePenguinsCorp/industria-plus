# Stack e organizacao tecnica

## Decisoes do projeto

- Frontend: Angular 21 com componentes standalone e TypeScript.
- Estilos: CSS puro global e por componente, sem pre-processador.
- Backend: Java 17 com Spring Boot 3.
- Banco: MySQL 8.4 em Docker.
- Persistencia: Spring Data JPA e migrations com Flyway.
- Execucao local: Docker Compose e scripts PowerShell.
- Idioma do codigo: ingles para nomes de arquivos, classes, funcoes, tabelas e campos.
- Idioma da interface e documentacao: portugues brasileiro.

Estas decisoes substituem a stack React, Node.js e PostgreSQL citada no acordo original, conforme a
definicao posterior e explicita do projeto.

## Organizacao por dominio

O codigo funcional deve ser agrupado por dominio, evitando uma unica pasta global para todas as
entidades ou servicos.

```text
frontend/src/app/
|-- core/                      # infraestrutura compartilhada
|-- layout/                    # shell e navegacao
|-- pages/home/                # pagina de entrada
`-- features/
    |-- equipment/
    |-- sector/
    |-- technician/
    `-- maintenance-request/

backend/src/main/java/com/industriaplus/backend/
|-- config/                    # configuracoes transversais
|-- health/                    # verificacao operacional
|-- equipment/                 # equipamentos
|-- sector/                    # setores
|-- technician/                # tecnicos
`-- maintenancerequest/        # chamados e regra de atribuicao
```

Cada dominio do backend pode conter controller, service, repository, entity e DTOs conforme ganhar
comportamento. Pastas e abstracoes nao devem ser criadas sem uso real.

## Restricoes arquiteturais

- Controllers apenas validam entrada e delegam casos de uso.
- Regras de negocio ficam no backend, fora de controllers e entidades de interface.
- Acesso ao banco deve passar por repositories e migrations versionadas.
- A RN-001 deve ser transacional e protegida contra atribuicoes concorrentes.
- O frontend pode antecipar validacoes, mas deve tratar a resposta de negocio da API.
- N2/N3 nao deve introduzir complexidade no modelo do N1 antes do refinamento.

## Convencoes

- TypeScript: `camelCase` para variaveis e funcoes, `PascalCase` para tipos e componentes.
- Java: `camelCase` para metodos e atributos, `PascalCase` para classes e records.
- Banco: `snake_case` para tabelas, colunas, indices e constraints.
- Funcoes e classes devem ter uma responsabilidade clara e nomes orientados ao dominio.
