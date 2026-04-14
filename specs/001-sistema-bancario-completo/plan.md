# Implementation Plan: Sistema Bancario — Microservicios

**Branch**: `001-sistema-bancario-completo` | **Date**: 2026-04-14 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `specs/001-sistema-bancario-completo/spec.md`

## Summary

Implementación de una solución bancaria simplificada compuesta por dos microservicios autónomos (`customers-service` y `accounts-service`) con Arquitectura Hexagonal, comunicación asincrónica vía RabbitMQ, persistencia independiente en PostgreSQL y despliegue completo con Docker Compose. El sistema expone APIs REST para la gestión de clientes, cuentas, movimientos y reportes, con TDD obligatorio al 100% en dominio y 80% en servicios de aplicación.

## Technical Context

**Language/Version**: Java 21 LTS
**Primary Dependencies**: Spring Boot 3.x, Spring Data JPA + Hibernate, Spring AMQP (RabbitMQ), Spring Web MVC, Bean Validation, Testcontainers 1.19+
**Storage**: PostgreSQL 15 — instancia `db_customers` para customers-service, instancia `db_accounts` para accounts-service
**Testing**: JUnit 5 + Mockito (unitarias), Spring Boot Test + Testcontainers (integración)
**Target Platform**: Linux server, contenedor Docker (eclipse-temurin:21)
**Project Type**: REST microservices con mensajería asincrónica
**Performance Goals**: Latencia funcional aceptable en entorno local de evaluación; sin SLA de producción definido
**Constraints**: Sin autenticación/autorización; sin versionado de API `/v1/`; eliminación lógica únicamente; consistencia eventual aceptada para proyección de clientes
**Scale/Scope**: 2 microservicios, 4 bounded contexts de operación (clientes, cuentas, movimientos, reportes), 17 HUs

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| # | Gate | Status |
|---|---|---|
| G-01 | Exactamente 2 microservicios — no más, no menos | ✅ PASS |
| G-02 | Arquitectura Hexagonal con 3 capas estrictas en cada servicio | ✅ PASS |
| G-03 | Dominio sin dependencias de Spring, JPA\* ni RabbitMQ | ✅ PASS\* |
| G-04 | Dirección de dependencias: infraestructura → aplicación → dominio | ✅ PASS |
| G-05 | Comunicación inter-servicio exclusivamente vía RabbitMQ (sin REST entre servicios) | ✅ PASS |
| G-06 | Base de datos separada por microservicio (db_customers / db_accounts) | ✅ PASS |
| G-07 | TDD obligatorio — prueba primero en todo método de negocio | ✅ PASS |
| G-08 | Cero comentarios en código fuente, YAML, Dockerfile y scripts | ✅ PASS |
| G-09 | Idioma mixto: dominio en español, infraestructura en inglés | ✅ PASS |
| G-10 | `docker compose up` levanta todo el stack sin pasos previos manuales | ✅ PASS |

\*Decisión pragmática documentada en ADR-006: las entidades de dominio llevan anotaciones JPA (`@Entity`, `@Table`, `@Column`) dado que el enunciado exige JPA sobre las entidades. Las anotaciones son de mapeo, no de lógica de negocio. No se importan `JpaRepository` ni clases de Spring dentro del dominio.

**Re-check post-diseño (Phase 1)**: ✅ Todos los gates pasan. El modelo de datos, los contratos de API y la topología de mensajería son consistentes con la constitution.

## Project Structure

### Documentation (this feature)

```text
specs/001-sistema-bancario-completo/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   ├── api-customers.md
│   ├── api-accounts.md
│   └── events.md
└── tasks.md
```

### Source Code (repository root)

```text
banking-microservices/
├── customers-service/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/banking/customers/
│   │   │   │   ├── domain/
│   │   │   │   │   ├── model/
│   │   │   │   │   │   ├── Persona.java
│   │   │   │   │   │   └── Cliente.java
│   │   │   │   │   ├── event/
│   │   │   │   │   │   ├── ClienteCreatedEvent.java
│   │   │   │   │   │   └── ClienteDeletedEvent.java
│   │   │   │   │   ├── exception/
│   │   │   │   │   │   ├── DomainException.java
│   │   │   │   │   │   ├── BusinessRuleException.java
│   │   │   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   │   │   ├── DuplicateResourceException.java
│   │   │   │   │   │   ├── EdadInvalidaException.java
│   │   │   │   │   │   ├── ContrasenaInvalidaException.java
│   │   │   │   │   │   ├── IdentificacionInvalidaException.java
│   │   │   │   │   │   ├── IdentificacionDuplicadaException.java
│   │   │   │   │   │   └── ClienteConCuentasActivasException.java
│   │   │   │   │   └── port/
│   │   │   │   │       ├── ClienteRepository.java
│   │   │   │   │       └── EventPublisher.java
│   │   │   │   ├── application/
│   │   │   │   │   ├── usecase/
│   │   │   │   │   │   ├── CrearClienteUseCase.java
│   │   │   │   │   │   ├── ConsultarClienteUseCase.java
│   │   │   │   │   │   ├── ActualizarClienteUseCase.java
│   │   │   │   │   │   └── EliminarClienteUseCase.java
│   │   │   │   │   └── dto/
│   │   │   │   │       ├── CrearClienteRequest.java
│   │   │   │   │       ├── ActualizarClienteRequest.java
│   │   │   │   │       └── ClienteResponse.java
│   │   │   │   └── infrastructure/
│   │   │   │       ├── controller/
│   │   │   │       │   └── CustomerController.java
│   │   │   │       ├── persistence/
│   │   │   │       │   ├── CustomerRepositoryJpa.java
│   │   │   │       │   └── SpringDataClienteRepository.java
│   │   │   │       ├── messaging/
│   │   │   │       │   └── RabbitMQEventPublisher.java
│   │   │   │       ├── mapper/
│   │   │   │       │   └── ClienteMapper.java
│   │   │   │       └── config/
│   │   │   │           └── RabbitMQConfig.java
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       └── BaseDatos.sql
│   │   └── test/
│   │       ├── unit/
│   │       │   ├── domain/
│   │       │   │   ├── ClienteCreationTest.java
│   │       │   │   └── ClienteValidationTest.java
│   │       │   └── usecase/
│   │       │       ├── CrearClienteUseCaseTest.java
│   │       │       ├── ActualizarClienteUseCaseTest.java
│   │       │       └── EliminarClienteUseCaseTest.java
│   │       └── integration/
│   │           └── CustomerControllerIntegrationTest.java
│   ├── Dockerfile
│   └── pom.xml
├── accounts-service/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/banking/accounts/
│   │   │   │   ├── domain/
│   │   │   │   │   ├── model/
│   │   │   │   │   │   ├── ClienteProyeccion.java
│   │   │   │   │   │   ├── Cuenta.java
│   │   │   │   │   │   └── Movimiento.java
│   │   │   │   │   ├── exception/
│   │   │   │   │   │   ├── DomainException.java
│   │   │   │   │   │   ├── BusinessRuleException.java
│   │   │   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   │   │   ├── DuplicateResourceException.java
│   │   │   │   │   │   ├── SaldoInsuficienteException.java
│   │   │   │   │   │   ├── LimiteDiarioExcedidoException.java
│   │   │   │   │   │   ├── CuentaInactivaException.java
│   │   │   │   │   │   ├── ClienteConCuentasActivasException.java
│   │   │   │   │   │   ├── CuentaNotFoundException.java
│   │   │   │   │   │   └── MovimientoNotFoundException.java
│   │   │   │   │   ├── port/
│   │   │   │   │   │   ├── CuentaRepository.java
│   │   │   │   │   │   ├── MovimientoRepository.java
│   │   │   │   │   │   └── ClienteProyeccionRepository.java
│   │   │   │   │   └── validator/
│   │   │   │   │       ├── MovimientoValidator.java
│   │   │   │   │       ├── SaldoInsuficienteValidator.java
│   │   │   │   │       ├── LimiteDiarioValidator.java
│   │   │   │   │       ├── CuentaActivaValidator.java
│   │   │   │   │       └── ValorCeroValidator.java
│   │   │   │   ├── application/
│   │   │   │   │   ├── usecase/
│   │   │   │   │   │   ├── CrearCuentaUseCase.java
│   │   │   │   │   │   ├── ConsultarCuentaUseCase.java
│   │   │   │   │   │   ├── ActualizarCuentaUseCase.java
│   │   │   │   │   │   ├── EliminarCuentaUseCase.java
│   │   │   │   │   │   ├── RegistrarMovimientoUseCase.java
│   │   │   │   │   │   ├── ConsultarMovimientoUseCase.java
│   │   │   │   │   │   ├── RegistrarAjusteUseCase.java
│   │   │   │   │   │   ├── RegistrarReversionUseCase.java
│   │   │   │   │   │   └── GenerarReporteUseCase.java
│   │   │   │   │   └── dto/
│   │   │   │   │       ├── CrearCuentaRequest.java
│   │   │   │   │       ├── ActualizarCuentaRequest.java
│   │   │   │   │       ├── CuentaResponse.java
│   │   │   │   │       ├── CrearMovimientoRequest.java
│   │   │   │   │       ├── CrearAjusteRequest.java
│   │   │   │   │       ├── CrearReversionRequest.java
│   │   │   │   │       ├── MovimientoResponse.java
│   │   │   │   │       └── ReporteItemResponse.java
│   │   │   │   └── infrastructure/
│   │   │   │       ├── controller/
│   │   │   │       │   ├── AccountController.java
│   │   │   │       │   ├── MovimientoController.java
│   │   │   │       │   └── ReporteController.java
│   │   │   │       ├── persistence/
│   │   │   │       │   ├── AccountRepositoryJpa.java
│   │   │   │       │   ├── MovimientoRepositoryJpa.java
│   │   │   │       │   ├── ClienteProyeccionRepositoryJpa.java
│   │   │   │       │   ├── SpringDataCuentaRepository.java
│   │   │   │       │   ├── SpringDataMovimientoRepository.java
│   │   │   │       │   └── SpringDataClienteProyeccionRepository.java
│   │   │   │       ├── messaging/
│   │   │   │       │   └── ClienteEventConsumer.java
│   │   │   │       ├── mapper/
│   │   │   │       │   ├── CuentaMapper.java
│   │   │   │       │   └── MovimientoMapper.java
│   │   │   │       └── config/
│   │   │   │           └── RabbitMQConfig.java
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       └── BaseDatos.sql
│   │   └── test/
│   │       ├── unit/
│   │       │   ├── domain/
│   │       │   │   ├── CuentaCreationTest.java
│   │       │   │   └── MovimientoValidatorTest.java
│   │       │   └── usecase/
│   │       │       ├── RegistrarMovimientoUseCaseTest.java
│   │       │       ├── EliminarCuentaUseCaseTest.java
│   │       │       └── GenerarReporteUseCaseTest.java
│   │       └── integration/
│   │           ├── AccountControllerIntegrationTest.java
│   │           ├── MovimientoControllerIntegrationTest.java
│   │           └── ClienteEventConsumerIntegrationTest.java
│   ├── Dockerfile
│   └── pom.xml
├── docker-compose.yml
└── specs/
```

**Structure Decision**: Estructura de dos proyectos Maven independientes bajo el mismo repositorio, cada uno con Arquitectura Hexagonal estricta. No hay módulo padre Maven compartido para mantener el aislamiento de classpath y dependencias.

---

## Maven Dependencies

### customers-service — pom.xml

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                             https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.5</version>
        <relativePath/>
    </parent>
    <groupId>com.banking</groupId>
    <artifactId>customers-service</artifactId>
    <version>1.0.0</version>

    <properties>
        <java.version>21</java.version>
        <testcontainers.version>1.19.8</testcontainers.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-amqp</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>${testcontainers.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>postgresql</artifactId>
            <version>${testcontainers.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>rabbitmq</artifactId>
            <version>${testcontainers.version}</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

### accounts-service — pom.xml

Idéntico a customers-service salvo `<artifactId>accounts-service</artifactId>`. No existen dependencias adicionales.

---

## Docker Compose

```yaml
services:
  db-customers:
    image: postgres:15
    environment:
      POSTGRES_DB: db_customers
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    volumes:
      - db_customers_data:/var/lib/postgresql/data
      - ./customers-service/src/main/resources/BaseDatos.sql:/docker-entrypoint-initdb.d/init.sql
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres -d db_customers"]
      interval: 10s
      timeout: 5s
      retries: 5

  db-accounts:
    image: postgres:15
    environment:
      POSTGRES_DB: db_accounts
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    volumes:
      - db_accounts_data:/var/lib/postgresql/data
      - ./accounts-service/src/main/resources/BaseDatos.sql:/docker-entrypoint-initdb.d/init.sql
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres -d db_accounts"]
      interval: 10s
      timeout: 5s
      retries: 5

  rabbitmq:
    image: rabbitmq:3.12-management
    environment:
      RABBITMQ_DEFAULT_USER: guest
      RABBITMQ_DEFAULT_PASS: guest
    volumes:
      - rabbitmq_data:/var/lib/rabbitmq
    healthcheck:
      test: ["CMD", "rabbitmq-diagnostics", "check_running"]
      interval: 10s
      timeout: 5s
      retries: 5

  customers-service:
    build: ./customers-service
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://db-customers:5432/db_customers
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: postgres
      SPRING_RABBITMQ_HOST: rabbitmq
      SPRING_RABBITMQ_PORT: 5672
      SPRING_RABBITMQ_USERNAME: guest
      SPRING_RABBITMQ_PASSWORD: guest
    ports:
      - "8080:8080"
    depends_on:
      db-customers:
        condition: service_healthy
      rabbitmq:
        condition: service_healthy

  accounts-service:
    build: ./accounts-service
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://db-accounts:5432/db_accounts
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: postgres
      SPRING_RABBITMQ_HOST: rabbitmq
      SPRING_RABBITMQ_PORT: 5672
      SPRING_RABBITMQ_USERNAME: guest
      SPRING_RABBITMQ_PASSWORD: guest
    ports:
      - "8081:8080"
    depends_on:
      db-accounts:
        condition: service_healthy
      rabbitmq:
        condition: service_healthy

volumes:
  db_customers_data:
  db_accounts_data:
  rabbitmq_data:
```

---

## TDD Strategy — Orden de Implementación por Capa

El ciclo Red → Green → Refactor se aplica estrictamente en este orden.

### customers-service

| Fase | Capa | Clases de prueba | Clases de producción |
|---|---|---|---|
| 1 | Dominio — model | ClienteCreationTest, ClienteValidationTest | Persona, Cliente con todas las validaciones de negocio |
| 2 | Dominio — events | ClienteCreationTest (verifica eventos registrados) | ClienteCreatedEvent, ClienteDeletedEvent |
| 3 | Dominio — exceptions | Cubierto en fases 1–2 | Jerarquía completa DomainException |
| 4 | Aplicación — use cases | CrearClienteUseCaseTest, ActualizarClienteUseCaseTest, EliminarClienteUseCaseTest | Use cases con ClienteRepository mock |
| 5 | Infraestructura — integración | CustomerControllerIntegrationTest + Testcontainers | CustomerController, CustomerRepositoryJpa, RabbitMQEventPublisher, RabbitMQConfig |

### accounts-service

| Fase | Capa | Clases de prueba | Clases de producción |
|---|---|---|---|
| 1 | Dominio — validators | MovimientoValidatorTest | ValorCeroValidator, CuentaActivaValidator, SaldoInsuficienteValidator, LimiteDiarioValidator |
| 2 | Dominio — model | CuentaCreationTest | Cuenta, Movimiento, ClienteProyeccion |
| 3 | Aplicación — movimientos | RegistrarMovimientoUseCaseTest | RegistrarMovimientoUseCase con chain de validators |
| 4 | Aplicación — cuentas/reportes | EliminarCuentaUseCaseTest, GenerarReporteUseCaseTest | EliminarCuentaUseCase, GenerarReporteUseCase |
| 5 | Aplicación — ajustes/reversiones | RegistrarAjusteUseCaseTest, RegistrarReversionUseCaseTest | RegistrarAjusteUseCase, RegistrarReversionUseCase |
| 6 | Infraestructura — API | AccountControllerIntegrationTest, MovimientoControllerIntegrationTest | AccountController, MovimientoController, ReporteController, repositorios JPA |
| 7 | Infraestructura — messaging | ClienteEventConsumerIntegrationTest + Testcontainers RabbitMQ | ClienteEventConsumer, RabbitMQConfig con DLQ |

### Reglas TDD no negociables

- Las pruebas de las fases 1–5 (customers) y 1–5 (accounts) no llevan ninguna anotación de Spring.
- Las pruebas de integración usan `@SpringBootTest(webEnvironment = RANDOM_PORT)` y `@Testcontainers`.
- Ninguna prueba de integración usa H2 ni `@MockBean` para infraestructura.
- La cobertura de dominio debe alcanzar 100% antes de avanzar a la capa de aplicación.

---

## Risk Register

| # | Riesgo | Prob | Impacto | Mitigación |
|---|---|---|---|---|
| R-01 | Evento de cliente no llegó a accounts-service cuando se intenta crear cuenta (ventana de consistencia eventual) | Media | Alto | En pruebas de integración: usar `Awaitility` para esperar la proyección antes de crear cuenta. En producción: documentado como comportamiento esperado en quickstart. |
| R-02 | Doble procesamiento del mismo evento RabbitMQ (at-least-once delivery) | Media | Medio | `cliente_proyeccion` con `INSERT ... ON CONFLICT (cliente_id) DO UPDATE` — operación idempotente por diseño. |
| R-03 | Evento publicado a RabbitMQ antes de que la transacción se confirme en BD (rollback post-publicación) | Baja | Alto | Publicar el evento exclusivamente dentro de `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)`. |
| R-04 | Carrera de datos en cálculo del límite de retiro diario si hay peticiones concurrentes | Baja | Alto | `SELECT SUM(valor) FROM movimiento WHERE cuenta_id IN (...) AND fecha::date = TODAY FOR UPDATE` dentro de la transacción del use case. |
| R-05 | Algoritmo módulo 10 para validación de cédula ecuatoriana implementado incorrectamente | Baja | Alto | 100% cobertura unitaria en `CedulaEcuatorianaValidator` con mínimo 10 cédulas de muestra válidas e inválidas conocidas. |
| R-06 | Microservicio arranca antes que RabbitMQ esté listo para aceptar conexiones | Alta | Bajo | `depends_on: condition: service_healthy` en docker-compose.yml + `spring.rabbitmq.connection-timeout: 30000` en application.yml. |
| R-07 | N+1 queries con JOINED inheritance al consultar lista de clientes | Baja | Bajo | Verificar en pruebas de integración con Hibernate statistics. Usar `JOIN FETCH` si se detecta. |
