# Marketplace

Aplicação de marketplace de eventos construída com Spring Boot. O projeto é um
monólito modular que separa cadastro de clientes, catálogo de eventos e
ticketing, usando eventos de domínio para sincronizar os módulos.

## Status

O projeto está em desenvolvimento. O fluxo de seleção de assentos valida a
existência do assento e cria um bloqueio temporário; as etapas de pedido e
pagamento ainda estão indicadas como próximas evoluções no código.

## Stack

- Java 25
- Spring Boot 4.0.5
- Gradle 9.4.1 via Gradle Wrapper
- Spring Web, Validation, Actuator e Spring Data REST
- JPA/Hibernate com MySQL e PostgreSQL
- MongoDB para metadados do catálogo
- Redis para cache do catálogo e bloqueio de assentos
- Lombok
- Docker Compose para os serviços de infraestrutura

## Visão geral da arquitetura

```text
src/main/java/io/miguelsouza/marketplace/
├── MarketplaceApplication.java
├── common/
│   └── infrastructure/event/       # DTOs de eventos compartilhados
├── registration/                   # Clientes e endereços
│   ├── domain/
│   └── infrastructure/             # JPA, MySQL e handlers de eventos
├── catalog/                        # Eventos e vitrine
│   ├── application/                # Casos de uso e enriquecimento
│   ├── domain/
│   └── infrastructure/             # HTTP, JPA, MongoDB e cache Redis
└── ticketing/                      # Eventos, setores, assentos e reservas
    ├── application/
    ├── domain/
    └── infrastructure/             # HTTP, PostgreSQL e locks Redis
```

### Fluxo principal

1. O módulo `registration` publica eventos quando um cliente é criado ou
   atualizado.
2. O módulo `ticketing` mantém uma projeção local de clientes e eventos.
3. O módulo `catalog` lê os eventos do seu banco relacional, enriquece os dados
   com metadados armazenados no MongoDB e usa Redis para cache da vitrine.
4. A seleção de assento usa Redis para criar um lock com TTL de 30 segundos.

## Infraestrutura local

O arquivo `compose.yml` define:

| Serviço | Tecnologia | Porta local |
|---|---|---:|
| `registration-database` | MySQL | `3307` |
| `catalog-database` | MySQL | `3308` |
| `catalog-metadata-database` | MongoDB | `27018` |
| `catalog-cache` | Redis | `6380` |
| `ticketing-database` | PostgreSQL | `5433` |
| `ticketing-locking` | Redis | `6381` |

As credenciais padrão declaradas no Compose são `app/app` para os bancos
aplicacionais. As bases são `registration`, `catalog` e `ticketing`.

Suba a infraestrutura com:

```bash
docker compose up -d
```

Para acompanhar o estado:

```bash
docker compose ps
docker compose logs -f
```

Para parar os serviços sem remover os dados:

```bash
docker compose stop
```

Para remover os serviços e os volumes locais:

```bash
docker compose down -v
```

## Pré-requisitos

- JDK 25 disponível no `PATH` ou configurado no Gradle Toolchain
- Docker com Docker Compose

## Executando a aplicação

No Windows:

```powershell
.\gradlew.bat bootRun
```

Em sistemas Unix:

```bash
./gradlew bootRun
```

O Spring Boot está configurado para iniciar o Docker Compose
automaticamente durante o desenvolvimento (`start-only`). A aplicação fica
disponível por padrão em `http://localhost:8080`.

## API disponível

### Consultar a vitrine

```http
GET /showcase
```

Retorna a lista de eventos enriquecidos pelo catálogo. O resultado é cacheado
em Redis quando não está vazio.

### Selecionar um assento

```http
POST /ticketing/events/{eventId}/seats/select
Content-Type: application/json
X-CUSTOMER-ID: {customerId}

{
  "id": "seat-id"
}
```

Retorna `201 Created` quando o assento é bloqueado com sucesso. Assentos
inexistentes ou já bloqueados geram as exceções de domínio correspondentes.

### Health check

```http
GET /actuator/health
```

Os detalhes de saúde estão habilitados pela configuração atual.

## Testes e tarefas Gradle

```bash
./gradlew test
./gradlew build
```

No Windows, use `.\gradlew.bat` no lugar de `./gradlew`.

O teste existente valida o carregamento do contexto Spring:
`src/test/java/io/miguelsouza/marketplace/MarketplaceApplicationTests.java`.

## Configuração

As configurações ficam em
`src/main/resources/application.properties`. Os três módulos usam bancos
separados e Redis com connection factories independentes. O Hibernate está
configurado com `ddl-auto=update` para o ambiente local.

Para ambientes compartilhados ou de produção, sobrescreva as propriedades por
variáveis/configuração externa e não reutilize as credenciais do Compose.

### Observações atuais

- O Compose publica o MongoDB na porta `27018`, enquanto a configuração
  Spring não declara explicitamente uma URI MongoDB; se a aplicação for
  executada fora do ambiente esperado, confirme o host e a porta efetivos.
- O projeto usa `spring-boot-docker-compose` apenas como dependência de
  desenvolvimento; em produção, a infraestrutura deve ser provisionada
  separadamente.

