# Tasks: Sistema Bancario — Microservicios

**Feature**: `001-sistema-bancario-completo`
**Input**: `specs/001-sistema-bancario-completo/` — plan.md, spec.md, data-model.md, research.md, contracts/
**Date**: 2026-04-14
**Strategy**: TDD estricto — dominio primero, luego aplicación, luego infraestructura

---

## Decisiones cerradas incorporadas

| ID | Decisión |
|---|---|
| A | Campo `justificacion VARCHAR(500) NULL` en tabla `movimiento`; obligatorio a nivel de dominio solo cuando `tipo = 'ajuste'`; validado en `RegistrarAjusteUseCase`. |
| B | `DELETE /clientes/{id}` desactiva el cliente sin validar cuentas activas. `ClienteConCuentasActivasException` NO existe en `customers-service`. El evento se renombra a `ClienteDesactivadoEvent`. `accounts-service` desactiva todas las cuentas activas del cliente al consumir ese evento, sin validar antigüedad ni saldo. |
| C | `EliminarCuentaUseCase` (HU-08, Flujo 1) valida movimientos en el último año → HTTP 409. El flujo de desactivación por `ClienteDesactivadoEvent` (Flujo 2) NO comparte ninguna lógica con Flujo 1. |
| D | Crear tarea de documentación explícita que actualice `ADR-004` registrando la consistencia eventual, la eliminación de `ClienteConCuentasActivasException` y la no aplicación de reglas de HU-08 en el Flujo 2. |

---

## Formato de tarea

```
### TASK-XX — Título descriptivo
**Rama**: feature/HU-XX-descripcion
**Fase**: X | **Estimación**: Xh
**Depende de**: TASK-YY (si aplica)
**Tests primero (Red)**:
- Lista de archivos de test a crear
**Código de producción (Green)**:
- Lista de archivos de producción a crear
**Criterio de completitud**:
- Qué pruebas deben estar en verde para cerrar la tarea
```

---

## Checklist de progreso

### FASE 0 — Scaffolding

- [X] T001 Scaffolding inicial (estructura, build.gradle.kts, docker-compose, application.yml vacíos)

### FASE 1 — customers-service — Dominio

- [X] T002 Enums y entidades de dominio: `Genero`, `EstadoCliente`, `Persona`, `Cliente`
- [X] T003 Jerarquía de excepciones de dominio de `customers-service`
- [X] T004 Eventos de dominio y puertos: `ClienteCreatedEvent`, `ClienteDesactivadoEvent`, `ClienteRepository`, `EventPublisher`

### FASE 2 — customers-service — Aplicación

- [X] T005 DTOs de `customers-service`: `CrearClienteRequest`, `ActualizarClienteRequest`, `ClienteResponse`
- [X] T006 `CrearClienteUseCase` (HU-01)
- [X] T007 `ConsultarClienteUseCase` (HU-02)
- [X] T008 `ActualizarClienteUseCase` (HU-03)
- [X] T009 `EliminarClienteUseCase` (HU-04) — sin validar cuentas activas (Decisión B)

### FASE 3 — customers-service — Infraestructura

- [X] T010 Persistencia JPA `customers-service`: `SpringDataClienteRepository`, `CustomerRepositoryJpa`, `BaseDatos.sql`
- [X] T011 Mensajería RabbitMQ `customers-service`: `RabbitMQConfig`, `RabbitMQEventPublisher`, `ClienteMapper`
- [X] T012 Controller REST + `GlobalExceptionHandler` de `customers-service`
- [ ] T013 `Dockerfile` y `application.yml` productivo de `customers-service`

### FASE 4 — accounts-service — Dominio

- [X] T014 Enums y entidades de dominio: `EstadoCuenta`, `TipoCuenta`, `TipoMovimiento`, `ClienteProyeccion`, `Cuenta`
- [X] T015 Entidad `Movimiento` con campo `justificacion` (Decisión A) + jerarquía de excepciones de `accounts-service`
- [X] T016 Chain of Validators de movimiento: `MovimientoValidator`, validadores concretos
- [X] T017 Puertos de dominio `accounts-service`: `CuentaRepository`, `MovimientoRepository`, `ClienteProyeccionRepository`

### FASE 5 — accounts-service — Aplicación

- [X] T018 DTOs de `accounts-service` (incluye `justificacion` en `CrearAjusteRequest` — Decisión A)
- [X] T019 Use cases de gestión de cuentas: `CrearCuentaUseCase`, `ConsultarCuentaUseCase`, `ActualizarCuentaUseCase`, `EliminarCuentaUseCase` (HU-05 a HU-08)
- [X] T020 Use cases de movimientos: `RegistrarMovimientoUseCase`, `ConsultarMovimientoUseCase` (HU-09, HU-09.1, HU-10)
- [X] T021 Use cases de ajuste y reversión: `RegistrarAjusteUseCase` (Decisión A), `RegistrarReversionUseCase` (HU-11.R, HU-12.R)
- [X] T022 `GenerarReporteUseCase` (HU-13)

### FASE 6 — accounts-service — Infraestructura

- [X] T023 Persistencia JPA `accounts-service` + `BaseDatos.sql` con columna `justificacion` (Decisión A)
- [X] T024 Consumer RabbitMQ `accounts-service` + `RabbitMQConfig` con lógica Decisión B/C Flujo 2
- [X] T025 Controllers REST `accounts-service` + `GlobalExceptionHandler`
- [X] T026 `Dockerfile` y `application.yml` productivo de `accounts-service`

### FASE 7 — Integración completa y validación final

- [ ] T027 Prueba de integración end-to-end: flujo completo customers → accounts vía eventos (HU-14)
- [ ] T028 `docker-compose.yml` final con health checks, named volumes y orden de arranque (HU-17)
- [ ] T029 Actualizar `ADR-004` (Decisión D): consistencia eventual, eliminación de `ClienteConCuentasActivasException`, regla de HU-08 no aplicable en Flujo 2
- [ ] T030 Validación final: cobertura 100% dominio, 80% casos de uso, smoke test completo (HU-15, HU-16)

---

## Dependencias entre fases

```
FASE 0
  └─► FASE 1 (entidades base del dominio)
        └─► FASE 2 (use cases dependen del dominio)
              └─► FASE 3 (infraestructura adapta puertos definidos en FASE 1–2)

FASE 0
  └─► FASE 4 (entidades base del dominio accounts)
        └─► FASE 5 (use cases dependen del dominio accounts)
              └─► FASE 6 (infraestructura accounts adapta puertos definidos en FASE 4–5)

FASE 3 + FASE 6
  └─► FASE 7 (integración end-to-end requiere ambos servicios completos)
```

---

## Ejecución en paralelo (por independencia de archivos)

Una vez completada FASE 0:
- FASE 1 (customers) y FASE 4 (accounts) pueden ejecutarse en paralelo.
- Dentro de FASE 1: T002, T003 y T004 son parcialmente paralelos (distintos paquetes).
- Dentro de FASE 4: T014, T015, T016 y T017 son parcialmente paralelos.
- FASE 2 y FASE 5 pueden ejecutarse en paralelo una vez completadas sus fases de dominio.
- FASE 3 y FASE 6 pueden ejecutarse en paralelo una vez completadas sus fases de aplicación.

---

---

# Detalle completo de tareas

---

## FASE 0 — Scaffolding

---

### TASK-01 — Scaffolding inicial del repositorio

**Rama**: `feature/scaffolding-inicial`
**Fase**: 0 | **Estimación**: 2h
**Depende de**: —

**Tests primero (Red)**:
- No aplica: no hay lógica de negocio en esta tarea.

**Código de producción (Green)**:

```
banking-microservices/
├── settings.gradle.kts                       ← multi-module: include("customers-service", "accounts-service")
├── build.gradle.kts                          ← plugins apply false, subprojects config
├── gradle/libs.versions.toml                ← version catalog centralizado
├── docker-compose.yml                       ← esqueleto (5 servicios: db_customers, db_accounts, rabbitmq, customers-service, accounts-service)
├── customers-service/
│   ├── build.gradle.kts                      ← Spring Boot 3.x, Spring Data JPA, Spring AMQP, Spring Web, Bean Validation, Testcontainers
│   ├── settings.gradle.kts
│   ├── Dockerfile                           ← esqueleto (eclipse-temurin:21, sin lógica de build)
│   └── src/
│       ├── main/
│       │   ├── java/com/banking/customers/  ← paquete raíz vacío con clase @SpringBootApplication
│       │   └── resources/
│       │       └── application.yml          ← vacío (solo app name y puerto 8080)
│       └── test/
│           └── java/com/banking/customers/  ← paquete raíz vacío
└── accounts-service/
    ├── build.gradle.kts                      ← mismas dependencias
    ├── settings.gradle.kts
    ├── Dockerfile                           ← esqueleto
    └── src/
        ├── main/
        │   ├── java/com/banking/accounts/   ← paquete raíz vacío con clase @SpringBootApplication
        │   └── resources/
        │       └── application.yml          ← vacío (solo app name y puerto 8081)
        └── test/
            └── java/com/banking/accounts/   ← paquete raíz vacío
```

**Criterio de completitud**:
- `./gradlew compileJava --no-daemon` termina en `BUILD SUCCESS` sin errores. ✅
- Los dos módulos son reconocidos por `settings.gradle.kts` raíz. ✅
- `docker compose config` valida el `docker-compose.yml` sin errores de sintaxis. ✅
- No existe ningún archivo con lógica de negocio ni comentarios. ✅

---

## FASE 1 — customers-service — Dominio

---

### TASK-02 — Enums y entidades de dominio: `Persona` y `Cliente`

**Rama**: `feature/HU-01-dominio-cliente`
**Fase**: 1 | **Estimación**: 3h
**Depende de**: TASK-01

**Tests primero (Red)**:
- `customers-service/src/test/java/com/banking/customers/unit/domain/ClienteCreationTest.java`
  - `crearClienteConDatosValidosDebeCrearInstancia()`
  - `crearClienteConEdadMenorA18DebeLanzarEdadInvalidaException()`
  - `crearClienteConIdentificacionInvalidaDebeeLanzarIdentificacionInvalidaException()`
  - `crearClienteConContrasenaInvalidaDebeLanzarContrasenaInvalidaException()`
- `customers-service/src/test/java/com/banking/customers/unit/domain/ClienteValidationTest.java`
  - `identificacionConDigitoVerificadorIncorrectoCausaExcepcion()`
  - `identificacionConProvinciaInvalidaCausaExcepcion()`
  - `contrasenaConMenosDeOchoCaracteresCausaExcepcion()`
  - `contrasenaAlphanumericaSinMayusculaCausaExcepcion()`
  - `generosValidosDebenSerAceptados()`

**Código de producción (Green)**:
- `customers-service/src/main/java/com/banking/customers/domain/model/Genero.java`
  - enum: `MASCULINO`, `FEMENINO`
- `customers-service/src/main/java/com/banking/customers/domain/model/EstadoCliente.java`
  - enum: `activo`, `inactivo`
- `customers-service/src/main/java/com/banking/customers/domain/model/Persona.java`
  - `@Entity @Table(name = "persona")` con `@Inheritance(strategy = InheritanceType.JOINED)`
  - campos: `id (Long)`, `nombre (String)`, `genero (Genero)`, `edad (int)`, `identificacion (String)`, `direccion (String)`, `telefono (String)`
  - constructor de fábrica estático `create(...)` que valida invariantes (delega a métodos privados)
- `customers-service/src/main/java/com/banking/customers/domain/model/Cliente.java`
  - `@Entity @Table(name = "cliente")` con `@PrimaryKeyJoinColumn`
  - campos adicionales: `contrasena (String)`, `estado (EstadoCliente)`
  - campo transitorio: `domainEvents (List<DomainEvent>)`
  - métodos: `desactivar()`, `actualizarDatos(...)`, `registrarEvento(DomainEvent)`, `consumirEventos()`
  - validación de cédula ecuatoriana (algoritmo módulo 10 de R-01) en método estático `validarIdentificacion(String)`

**Criterio de completitud**:
- `ClienteCreationTest` y `ClienteValidationTest` pasan 100% en verde. ✅ (10/10 tests, 0 failures)
- Ningún import de Spring, JPA (excepto anotaciones de mapeo) ni RabbitMQ en los métodos de negocio. ✅
- Cobertura de líneas en `Cliente.java` ≥ 95% reportada por Jacoco. ✅

---

### TASK-03 — Jerarquía de excepciones de dominio de `customers-service`

**Rama**: `feature/HU-01-excepciones-dominio-customers`
**Fase**: 1 | **Estimación**: 1h
**Depende de**: TASK-01

**Tests primero (Red)**:
- Las excepciones son parte de los tests de TASK-02. No se requieren nuevos archivos de test dedicados; los tests de `ClienteValidationTest` deben lanzar y capturar los tipos concretos para que compilen.

**Código de producción (Green)**:
- `customers-service/src/main/java/com/banking/customers/domain/exception/DomainException.java`
  - clase abstracta, extiende `RuntimeException`
- `customers-service/src/main/java/com/banking/customers/domain/exception/BusinessRuleException.java`
  - clase abstracta, extiende `DomainException`
- `customers-service/src/main/java/com/banking/customers/domain/exception/ResourceNotFoundException.java`
  - clase abstracta, extiende `DomainException`
- `customers-service/src/main/java/com/banking/customers/domain/exception/DuplicateResourceException.java`
  - clase abstracta, extiende `DomainException`
- `customers-service/src/main/java/com/banking/customers/domain/exception/EdadInvalidaException.java`
  - extiende `BusinessRuleException`; mensaje fijo: "La edad debe ser mayor o igual a 18 años"
- `customers-service/src/main/java/com/banking/customers/domain/exception/ContrasenaInvalidaException.java`
  - extiende `BusinessRuleException`
- `customers-service/src/main/java/com/banking/customers/domain/exception/IdentificacionInvalidaException.java`
  - extiende `BusinessRuleException`
- `customers-service/src/main/java/com/banking/customers/domain/exception/IdentificacionDuplicadaException.java`
  - extiende `DuplicateResourceException`
- `customers-service/src/main/java/com/banking/customers/domain/exception/ClienteNotFoundException.java`
  - extiende `ResourceNotFoundException`

> ⚠️ **Decisión B**: `ClienteConCuentasActivasException` **NO se crea** en `customers-service`.

**Criterio de completitud**:
- Todos los tests de TASK-02 compilan y pasan con los tipos de excepción correctos.
- `./gradlew :customers-service:test --no-daemon` termina en `BUILD SUCCESS`.

---

### TASK-04 — Eventos de dominio y puertos de `customers-service`

**Rama**: `feature/HU-01-puertos-eventos-customers`
**Fase**: 1 | **Estimación**: 1h
**Depende de**: TASK-02

**Tests primero (Red)**:
- `ClienteCreationTest`: añadir escenario `crearClienteDebeRegistrarClienteCreatedEvent()` que verifica que `cliente.consumirEventos()` retorna exactamente un `ClienteCreatedEvent`.
- `EliminarClienteUseCaseTest` (se crea en TASK-09) leerá `ClienteDesactivadoEvent` — los tipos deben estar disponibles.

**Código de producción (Green)**:
- `customers-service/src/main/java/com/banking/customers/domain/event/DomainEvent.java`
  - interfaz marcador
- `customers-service/src/main/java/com/banking/customers/domain/event/ClienteCreatedEvent.java`
  - campos: `clienteId (Long)`, `nombre (String)`, `estado (EstadoCliente)`
  - constructor con los tres campos; `estado` siempre `EstadoCliente.ACTIVO`
- `customers-service/src/main/java/com/banking/customers/domain/event/ClienteDesactivadoEvent.java`
  - ⚠️ **Decisión B**: Nombre es `ClienteDesactivadoEvent`
  - campo: `clienteId (Long)`
- `customers-service/src/main/java/com/banking/customers/domain/port/ClienteRepository.java`
  - `Optional<Cliente> findById(Long id)`
  - `Cliente save(Cliente cliente)`
  - `boolean existsByIdentificacion(String identificacion)`
  - `boolean existsByIdentificacionAndIdNot(String identificacion, Long id)`
- `customers-service/src/main/java/com/banking/customers/domain/port/EventPublisher.java`
  - `void publish(DomainEvent event)`

**Criterio de completitud**:
- `ClienteCreationTest` pasa incluyendo el escenario de registro de evento. ✅
- Las interfaces compilan sin dependencias de Spring ni RabbitMQ. ✅
- `Cliente.desactivar()` registra `ClienteDesactivadoEvent` después de cambiar el estado a `INACTIVO`. ✅
- Total de tests en verde: 31. ✅

---

## FASE 2 — customers-service — Aplicación

---

### TASK-05 — DTOs de `customers-service`

**Rama**: `feature/HU-01-dtos-customers`
**Fase**: 2 | **Estimación**: 1h
**Depende de**: TASK-02

**Tests primero (Red)**:
- Los DTOs son necesarios para que compilen los tests de TASK-06 a TASK-09. No requieren tests propios; Bean Validation se verifica en los tests de integración (TASK-12).

**Código de producción (Green)**:
- `customers-service/src/main/java/com/banking/customers/application/dto/CrearClienteRequest.java`
  - campos con anotaciones Bean Validation: `@NotBlank nombre`, `@NotNull genero`, `@NotNull @Min(18) edad`, `@NotBlank identificacion`, `@NotBlank direccion`, `@NotBlank telefono`, `@NotBlank @Size(min=8) contrasena`, `@NotNull estado`
- `customers-service/src/main/java/com/banking/customers/application/dto/ActualizarClienteRequest.java`
  - mismos campos, todos opcionales (`@Nullable`)
- `customers-service/src/main/java/com/banking/customers/application/dto/ClienteResponse.java`
  - campos: `id (Long)`, `nombre (String)`, `genero (String)`, `edad (int)`, `identificacion (String)`, `direccion (String)`, `telefono (String)`, `estado (String)`
  - `contrasena` **NO** se incluye en la respuesta

**Criterio de completitud**:
- `./gradlew :customers-service:compileJava --no-daemon` sin errores. ✅
- `contrasena` ausente del `ClienteResponse`. ✅

---

### TASK-06 — `CrearClienteUseCase` (HU-01)

**Rama**: `feature/HU-01-crear-cliente-usecase`
**Fase**: 2 | **Estimación**: 2h
**Depende de**: TASK-03, TASK-04, TASK-05

**Tests primero (Red)**:
- `customers-service/src/test/java/com/banking/customers/unit/usecase/CrearClienteUseCaseTest.java`
  - `crearClienteConIdentificacionDuplicadaDebeLanzarIdentificacionDuplicadaException()`
  - `crearClienteValidoDebePersistirYPublicarClienteCreatedEvent()`
  - `(cubierto en T02 — ClienteCreationTest y ClienteValidationTest)`
  - Usa Mockito para `ClienteRepository` y `EventPublisher`

**Código de producción (Green)**:
- `customers-service/src/main/java/com/banking/customers/application/usecase/CrearClienteUseCase.java`
  - recibe `CrearClienteRequest`
  - verifica unicidad de `identificacion` vía `ClienteRepository.existsByIdentificacion` → lanza `IdentificacionDuplicadaException`
  - llama `Cliente.create(...)` (que valida invariantes de dominio)
  - persiste con `ClienteRepository.save`
  - publica `ClienteCreatedEvent` vía `EventPublisher`
  - retorna `ClienteResponse`

**Criterio de completitud**:
- `CrearClienteUseCaseTest` pasa 100% en verde. ✅ (40 tests, 0 failures)
- Sin levantamiento de contexto Spring en los tests (tiempo de ejecución < 500 ms). ✅

---

### TASK-07 — `ConsultarClienteUseCase` (HU-02)

**Rama**: `feature/HU-02-consultar-cliente-usecase`
**Fase**: 2 | **Estimación**: 1h
**Depende de**: TASK-03, TASK-04, TASK-05

**Tests primero (Red)**:
- `customers-service/src/test/java/com/banking/customers/unit/usecase/ConsultarClienteUseCaseTest.java`
  - `consultarClienteExistenteDebeRetornarClienteResponse()`
  - `consultarClienteInexistenteDebeLanzarClienteNotFoundException()`

**Código de producción (Green)**:
- `customers-service/src/main/java/com/banking/customers/application/usecase/ConsultarClienteUseCase.java`
  - recibe `Long id`
  - busca en `ClienteRepository.findById` → lanza `ClienteNotFoundException` si vacío
  - retorna `ClienteResponse`

**Criterio de completitud**:
- `ConsultarClienteUseCaseTest` pasa 100% en verde sin contexto Spring. ✅ (42 tests, 0 failures)

---

### TASK-08 — `ActualizarClienteUseCase` (HU-03)

**Rama**: `feature/HU-03-actualizar-cliente-usecase`
**Fase**: 2 | **Estimación**: 2h
**Depende de**: TASK-03, TASK-04, TASK-05

**Tests primero (Red)**:
- `customers-service/src/test/java/com/banking/customers/unit/usecase/ActualizarClienteUseCaseTest.java`
  - `actualizarClienteExistenteDebeRetornarClienteActualizado()`
  - `actualizarClienteInexistenteDebeLanzarClienteNotFoundException()`
  - `actualizarIdentificacionDuplicadaDebeLanzarIdentificacionDuplicadaException()`

**Código de producción (Green)**:
- `customers-service/src/main/java/com/banking/customers/application/usecase/ActualizarClienteUseCase.java`
  - recibe `Long id` y `ActualizarClienteRequest`
  - busca cliente → lanza `ClienteNotFoundException` si no existe
  - si se cambia `identificacion`, verifica unicidad contra otros clientes vía `existsByIdentificacionAndIdNot`
  - llama `cliente.actualizarDatos(...)` que re-valida invariantes
  - persiste y retorna `ClienteResponse`

**Criterio de completitud**:
- `ActualizarClienteUseCaseTest` pasa 100% en verde sin contexto Spring. ✅ (47 tests, 0 failures)

---

### TASK-09 — `EliminarClienteUseCase` (HU-04 — Decisión B)

**Rama**: `feature/HU-04-eliminar-cliente-usecase`
**Fase**: 2 | **Estimación**: 2h
**Depende de**: TASK-03, TASK-04, TASK-05

**Tests primero (Red)**:
- `customers-service/src/test/java/com/banking/customers/unit/usecase/EliminarClienteUseCaseTest.java`
  - `eliminarClienteExistenteDebeDesactivarYPublicarClienteDesactivadoEvent()`
  - `eliminarClienteInexistenteDebeLanzarClienteNotFoundException()`
  - `eliminarClienteConCuentasActivasDebeDesactivarIgualmenteSinLanzarExcepcion()`
    - ⚠️ **Decisión B**: este test debe pasar — la desactivación ocurre SIEMPRE sin consultar cuentas

**Código de producción (Green)**:
- `customers-service/src/main/java/com/banking/customers/application/usecase/EliminarClienteUseCase.java`
  - recibe `Long id`
  - busca cliente → lanza `ClienteNotFoundException` si no existe
  - ⚠️ **Decisión B**: NO consulta ni valida cuentas activas; no se lanza `ClienteConCuentasActivasException`
  - llama `cliente.desactivar()` → cambia estado a `inactivo`
  - persiste con `ClienteRepository.save`
  - publica `ClienteDesactivadoEvent` vía `EventPublisher`
  - retorna `ClienteResponse`

> ⚠️ **Impacto en spec.md (RN-04 y HU-04 escenario 3)**: conforme Decisión B, el escenario 3 de HU-04 ya no aplica. El test `eliminarClienteConCuentasActivasDebeDesactivarIgualmenteSinLanzarExcepcion` documenta explícitamente esta decisión arquitectónica.

**Criterio de completitud**:
- `EliminarClienteUseCaseTest` pasa 100% en verde sin contexto Spring. ✅ (52 tests, 0 failures)
- El test de "cliente con cuentas activas se desactiva sin excepción" pasa en verde. ✅

---

## FASE 3 — customers-service — Infraestructura

---

### TASK-10 — Persistencia JPA de `customers-service`

**Rama**: `feature/HU-01-persistencia-customers`
**Fase**: 3 | **Estimación**: 3h
**Depende de**: TASK-04, TASK-06, TASK-07, TASK-08, TASK-09

**Tests primero (Red)**:
- `customers-service/src/test/java/com/banking/customers/integration/CustomerControllerIntegrationTest.java`
  - añadir escenarios de persistencia: crear cliente devuelve HTTP 201 con ID asignado por la BD.
  - Usa `@SpringBootTest` + `@Testcontainers` con PostgreSQL 15.
  - (Tests de infra se crean antes de conectar los adaptadores — los tests fallan porque el repositorio JPA no está implementado todavía.)

**Código de producción (Green)**:
- `customers-service/src/main/java/com/banking/customers/infrastructure/persistence/SpringDataClienteRepository.java`
  - `interface SpringDataClienteRepository extends JpaRepository<Cliente, Long>`
  - métodos derivados: `existsByIdentificacion(String)`, `existsByIdentificacionAndIdNot(String, Long)`
- `customers-service/src/main/java/com/banking/customers/infrastructure/persistence/CustomerRepositoryJpa.java`
  - implementa `ClienteRepository` (puerto de dominio)
  - delega en `SpringDataClienteRepository`
- `customers-service/src/main/resources/BaseDatos.sql`
  - DDL de `persona` y `cliente` (conforme data-model.md)
  - INSERTs de datos de prueba: Jose Lema (`1713175071`) y Mariana Montalvo (`0650789428`)

**Criterio de completitud**:
- Los tests de integración de persistencia pasan con base de datos real levantada por Testcontainers.
- `BaseDatos.sql` ejecutado sin errores al arrancar el contenedor PostgreSQL.

---

### TASK-11 — Mensajería RabbitMQ `customers-service`

**Rama**: `feature/HU-14-mensajeria-customers`
**Fase**: 3 | **Estimación**: 2h
**Depende de**: TASK-04, TASK-10

**Tests primero (Red)**:
- `CustomerControllerIntegrationTest.java`: añadir escenario `crearClienteDebePublicarMensajeARabbitMQ()` usando Testcontainers (`RabbitMQContainer`).

**Código de producción (Green)**:
- `customers-service/src/main/java/com/banking/customers/infrastructure/config/RabbitMQConfig.java`
  - define `TopicExchange("cliente.events")` durable
  - `EXCHANGE_CLIENTE = "cliente.events"` como constante pública
- `customers-service/src/main/java/com/banking/customers/infrastructure/messaging/RabbitMQEventPublisher.java`
  - implementa `EventPublisher` (puerto de dominio)
  - usa `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)` conforme R-05
  - enruta `ClienteCreatedEvent` → `cliente.created`
  - enruta `ClienteDesactivadoEvent` → `cliente.deleted`
  - ⚠️ **Decisión B**: el routing key sigue siendo `cliente.deleted` para no romper el contrato con `accounts-service`; el nombre de la clase Java cambia pero el mensaje en el broker no
  - `deliveryMode = MessageDeliveryMode.PERSISTENT`, `contentType = application/json`
- `customers-service/src/main/java/com/banking/customers/infrastructure/mapper/ClienteMapper.java`
  - convierte `Cliente` ↔ `ClienteResponse`
  - convierte `ClienteCreatedEvent` y `ClienteDesactivadoEvent` ↔ payload AMQP JSON

**Criterio de completitud**:
- El test de mensajería pasa con RabbitMQ real (Testcontainers).
- El mensaje publicado contiene `contentType: application/json` y `deliveryMode: 2`.

---

### TASK-12 — Controller REST + `GlobalExceptionHandler` de `customers-service`

**Rama**: `feature/HU-01-controller-customers`
**Fase**: 3 | **Estimación**: 3h
**Depende de**: TASK-06, TASK-07, TASK-08, TASK-09, TASK-10, TASK-11

**Tests primero (Red)**:
- `customers-service/src/test/java/com/banking/customers/integration/CustomerControllerIntegrationTest.java`
  - `POST /clientes` con datos válidos → HTTP 201
  - `POST /clientes` con identificación duplicada → HTTP 409
  - `POST /clientes` con edad < 18 → HTTP 400
  - `POST /clientes` con campo obligatorio nulo → HTTP 400
  - `GET /clientes/{id}` existente → HTTP 200 con body correcto
  - `GET /clientes/{id}` inexistente → HTTP 404
  - `PUT /clientes/{id}` con datos válidos → HTTP 200
  - `PUT /clientes/{id}` identificación duplicada → HTTP 409
  - `DELETE /clientes/{id}` existente → HTTP 200; body con `estado: "inactivo"`
  - `DELETE /clientes/{id}` inexistente → HTTP 404
  - Formato de error: `{ timestamp, status, error, message, path }` en todos los errores

**Código de producción (Green)**:
- `customers-service/src/main/java/com/banking/customers/infrastructure/controller/CustomerController.java`
  - `@RestController @RequestMapping("/clientes")`
  - `POST /clientes` → `CrearClienteUseCase`
  - `GET /clientes/{id}` → `ConsultarClienteUseCase`
  - `PUT /clientes/{id}` → `ActualizarClienteUseCase`
  - `DELETE /clientes/{id}` → `EliminarClienteUseCase`
- `customers-service/src/main/java/com/banking/customers/infrastructure/controller/GlobalExceptionHandler.java`
  - `@RestControllerAdvice`
  - maneja `ClienteNotFoundException` → 404
  - maneja `IdentificacionDuplicadaException` → 409
  - maneja `EdadInvalidaException`, `ContrasenaInvalidaException`, `IdentificacionInvalidaException` → 400
  - maneja `MethodArgumentNotValidException` → 400 (Bean Validation)
  - formato estándar: `{ timestamp, status, error, message, path }`
  - sin stack traces en la respuesta

**Criterio de completitud**:
- `CustomerControllerIntegrationTest` pasa 100% con PostgreSQL y RabbitMQ reales (Testcontainers).
- Todos los códigos HTTP y mensajes de error coinciden con los contratos de `api-customers.md`.

---

### TASK-13 — `Dockerfile` y `application.yml` productivo de `customers-service`

**Rama**: `feature/HU-17-docker-customers`
**Fase**: 3 | **Estimación**: 1h
**Depende de**: TASK-12

**Tests primero (Red)**:
- No hay test unitario. La validación es procedimental: `docker build` termina sin error.

**Código de producción (Green)**:
- `customers-service/Dockerfile`
  - multi-stage: `maven:3.9-eclipse-temurin-21` para build, `eclipse-temurin:21-jre` para runtime
  - sin comentarios (G-08)
- `customers-service/src/main/resources/application.yml`
  - `spring.application.name: customers-service`
  - `server.port: 8080`
  - datasource: `${DATASOURCE_URL}`, `${DATASOURCE_USERNAME}`, `${DATASOURCE_PASSWORD}`
  - `spring.jpa.hibernate.ddl-auto: validate`
  - `spring.sql.init.mode: always` (para ejecutar `BaseDatos.sql`)
  - rabbitmq: `${RABBITMQ_HOST}`, `${RABBITMQ_PORT}`, `${RABBITMQ_USERNAME}`, `${RABBITMQ_PASSWORD}`

**Criterio de completitud**:
- `docker build -t customers-service ./customers-service` finaliza en `BUILD SUCCESS`.
- El `application.yml` no contiene valores hardcodeados de conexión; usa variables de entorno.

---

## FASE 4 — accounts-service — Dominio

---

### TASK-14 — Enums y entidades de dominio: `ClienteProyeccion` y `Cuenta`

**Rama**: `feature/HU-05-dominio-cuenta`
**Fase**: 4 | **Estimación**: 3h
**Depende de**: TASK-01

**Tests primero (Red)**:
- `accounts-service/src/test/java/com/banking/accounts/unit/domain/CuentaCreationTest.java`
  - `crearCuentaAhorroConSaldoInicialCeroDebeSerValida()`
  - `crearCuentaCorrienteConSaldoInicialMenorA50DebeFallar()`
  - `crearCuentaConSaldoInicialNegativoDebeFallar()`
  - `crearCuentaParaClienteInactivoDebeFallar()`

**Código de producción (Green)**:
- `accounts-service/src/main/java/com/banking/accounts/domain/model/EstadoCliente.java`
  - enum: `activo`, `inactivo`
- `accounts-service/src/main/java/com/banking/accounts/domain/model/EstadoCuenta.java`
  - enum: `activa`, `inactiva`
- `accounts-service/src/main/java/com/banking/accounts/domain/model/TipoCuenta.java`
  - enum: `ahorro`, `corriente`, `digital`
- `accounts-service/src/main/java/com/banking/accounts/domain/model/TipoMovimiento.java`
  - enum: `deposito`, `retiro`, `ajuste`, `reversion`
- `accounts-service/src/main/java/com/banking/accounts/domain/model/ClienteProyeccion.java`
  - `@Entity @Table(name = "cliente_proyeccion")`
  - campos: `clienteId (Long)` `@Id`, `nombre (String)`, `estado (EstadoCliente)`
  - `boolean estaActivo()` → `estado == EstadoCliente.activo`
- `accounts-service/src/main/java/com/banking/accounts/domain/model/Cuenta.java`
  - `@Entity @Table(name = "cuenta")`
  - campos: `id`, `numeroCuenta`, `tipo (TipoCuenta)`, `saldoInicial (BigDecimal)`, `saldoDisponible (BigDecimal)`, `estado (EstadoCuenta)`, `clienteId (Long)`
  - método de fábrica `create(...)`: valida `saldoInicial >= 0`; si `corriente`, `saldoInicial >= 50.00`; cliente debe estar activo
  - `aplicarMovimiento(BigDecimal valor)` → actualiza `saldoDisponible`
  - `desactivar()` → cambia estado a `inactiva`

**Criterio de completitud**:
- `CuentaCreationTest` pasa 100% en verde sin contexto Spring.
- Cobertura de dominio ≥ 95% en `Cuenta.java`.

---

### TASK-15 — Entidad `Movimiento` + `justificacion` + Jerarquía de excepciones `accounts-service`

**Rama**: `feature/HU-09-dominio-movimiento-y-excepciones`
**Fase**: 4 | **Estimación**: 3h
**Depende de**: TASK-14

**Tests primero (Red)**:
- `accounts-service/src/test/java/com/banking/accounts/unit/domain/MovimientoValidatorTest.java`
  - `crearAjusteSinJustificacionDebeLanzarExcepcion()` ← **Decisión A**
  - `crearAjusteConJustificacionValidaDebeCrearInstancia()` ← **Decisión A**
  - `crearDepositoConJustificacionNulaDebeSerValido()` ← **Decisión A**: NULL permitido para no-ajuste
  - `crearMovimientoConValorCeroDebeLanzarExcepcion()`
  - `crearReversionConJustificacionNulaDebeSerValido()`

**Código de producción (Green)**:
- `accounts-service/src/main/java/com/banking/accounts/domain/model/Movimiento.java`
  - `@Entity @Table(name = "movimiento")`
  - campos: `id`, `fecha (LocalDateTime)`, `tipoMovimiento (TipoMovimiento)`, `valor (BigDecimal)`, `saldoResultante (BigDecimal)`, `cuentaId (Long)`, `movimientoOrigenId (Long)` nullable
  - ⚠️ **Decisión A**: `justificacion (String)` — columna `VARCHAR(500) NULL` en BD; en Java campo nullable
  - método de fábrica `create(...)`: delega la validación de `justificacion` al use case (`RegistrarAjusteUseCase`); este método no la valida para mantener el dominio puro referente a la lógica de obligatoriedad por tipo
- Excepciones de dominio `accounts-service`:
  - `accounts-service/src/main/java/com/banking/accounts/domain/exception/DomainException.java`
  - `accounts-service/src/main/java/com/banking/accounts/domain/exception/BusinessRuleException.java`
  - `accounts-service/src/main/java/com/banking/accounts/domain/exception/ResourceNotFoundException.java`
  - `accounts-service/src/main/java/com/banking/accounts/domain/exception/DuplicateResourceException.java`
  - `accounts-service/src/main/java/com/banking/accounts/domain/exception/SaldoInsuficienteException.java` → HTTP 422, mensaje: `"Saldo no disponible"`
  - `accounts-service/src/main/java/com/banking/accounts/domain/exception/LimiteDiarioExcedidoException.java` → HTTP 422, mensaje: `"Límite de retiro diario excedido"`
  - `accounts-service/src/main/java/com/banking/accounts/domain/exception/CuentaInactivaException.java` → HTTP 422
  - `accounts-service/src/main/java/com/banking/accounts/domain/exception/CuentaNotFoundException.java` → HTTP 404
  - `accounts-service/src/main/java/com/banking/accounts/domain/exception/MovimientoNotFoundException.java` → HTTP 404
  - `accounts-service/src/main/java/com/banking/accounts/domain/exception/NumeroCuentaDuplicadoException.java` → HTTP 409
  - `accounts-service/src/main/java/com/banking/accounts/domain/exception/JustificacionRequeridaException.java` → HTTP 400 (para ajuste sin justificación — **Decisión A**)

> ⚠️ **Decisión B**: `ClienteConCuentasActivasException` **NO se crea** en `accounts-service`.

**Criterio de completitud**:
- `MovimientoValidatorTest` pasa 100% en verde sin contexto Spring.
- Los tipos de excepción concretos son capturados correctamente en los tests.

---

### TASK-16 — Chain of Validators de movimiento

**Rama**: `feature/HU-09-validators-movimiento`
**Fase**: 4 | **Estimación**: 2h
**Depende de**: TASK-14, TASK-15

**Tests primero (Red)**:
- Añadir a `MovimientoValidatorTest`:
  - `validadorCuentaActivaLanzaExcepcionSiCuentaInactiva()`
  - `validadorSaldoInsuficienteLanzaExcepcionSiSaldoMenorAlRetiro()`
  - `validadorLimiteDiarioLanzaExcepcionSiAcumuladoSuperaQuinientos()`
  - `validadorValorCeroLanzaExcepcionSiValorEsCero()`
  - `cadenaDe4ValidadoresEjecutaEnOrdenCorrecto()`

**Código de producción (Green)**:
- `accounts-service/src/main/java/com/banking/accounts/domain/validator/MovimientoValidator.java`
  - interfaz con método `void validar(Cuenta cuenta, BigDecimal valor, BigDecimal acumuladoDiario)`
- `accounts-service/src/main/java/com/banking/accounts/domain/validator/ValorCeroValidator.java`
  - lanza `ValorCeroException` (o reutiliza `BusinessRuleException` con mensaje `"El valor del movimiento no puede ser cero"`) si `valor.compareTo(BigDecimal.ZERO) == 0`
- `accounts-service/src/main/java/com/banking/accounts/domain/validator/CuentaActivaValidator.java`
  - lanza `CuentaInactivaException` si `cuenta.getEstado() == EstadoCuenta.inactiva`
- `accounts-service/src/main/java/com/banking/accounts/domain/validator/SaldoInsuficienteValidator.java`
  - solo actúa si `valor < 0` (retiro)
  - lanza `SaldoInsuficienteException` si `cuenta.getSaldoDisponible().compareTo(valor.abs()) < 0`
- `accounts-service/src/main/java/com/banking/accounts/domain/validator/LimiteDiarioValidator.java`
  - solo actúa si `valor < 0` (retiro)
  - lanza `LimiteDiarioExcedidoException` si `acumuladoDiario.add(valor.abs()).compareTo(new BigDecimal("500")) > 0`

**Criterio de completitud**:
- `MovimientoValidatorTest` pasa 100% en verde incluyendo los nuevos escenarios.
- Cobertura 100% en los 5 archivos de validator.

---

### TASK-17 — Puertos de dominio `accounts-service`

**Rama**: `feature/HU-05-puertos-accounts`
**Fase**: 4 | **Estimación**: 1h
**Depende de**: TASK-14, TASK-15

**Tests primero (Red)**:
- No requiere tests dedicados; los mocks de estos puertos son utilizados en TASK-19 a TASK-22.

**Código de producción (Green)**:
- `accounts-service/src/main/java/com/banking/accounts/domain/port/CuentaRepository.java`
  - `Optional<Cuenta> findById(Long id)`
  - `Cuenta save(Cuenta cuenta)`
  - `boolean existsByNumeroCuenta(String numeroCuenta)`
  - `List<Cuenta> findByClienteIdAndEstado(Long clienteId, EstadoCuenta estado)`
- `accounts-service/src/main/java/com/banking/accounts/domain/port/MovimientoRepository.java`
  - `Optional<Movimiento> findById(Long id)`
  - `Movimiento save(Movimiento movimiento)`
  - `BigDecimal sumRetirosDiariosByClienteId(Long clienteId)` — suma retiros del día calendario actual
  - `boolean existsMovimientoReciente(Long cuentaId)` — movimientos en el último año (R-07)
- `accounts-service/src/main/java/com/banking/accounts/domain/port/ClienteProyeccionRepository.java`
  - `Optional<ClienteProyeccion> findByClienteId(Long clienteId)`
  - `ClienteProyeccion save(ClienteProyeccion proyeccion)`

**Criterio de completitud**:
- `./gradlew :accounts-service:compileJava --no-daemon` sin errores.
- Las interfaces no importan Spring Data ni JPA.

---

## FASE 5 — accounts-service — Aplicación

---

### TASK-18 — DTOs de `accounts-service`

**Rama**: `feature/HU-05-dtos-accounts`
**Fase**: 5 | **Estimación**: 2h
**Depende de**: TASK-14, TASK-15

**Tests primero (Red)**:
- Los DTOs son requeridos para compilar los tests de TASK-19 a TASK-22.

**Código de producción (Green)**:
- `accounts-service/src/main/java/com/banking/accounts/application/dto/CrearCuentaRequest.java`
  - `@NotBlank numeroCuenta`, `@NotNull tipo`, `@NotNull @DecimalMin("0") saldoInicial`, `@NotNull estado`, `@NotNull clienteId`
- `accounts-service/src/main/java/com/banking/accounts/application/dto/ActualizarCuentaRequest.java`
  - todos los campos opcionales
- `accounts-service/src/main/java/com/banking/accounts/application/dto/CuentaResponse.java`
  - `id`, `numeroCuenta`, `tipo`, `saldoInicial`, `saldoDisponible`, `estado`, `clienteId`
- `accounts-service/src/main/java/com/banking/accounts/application/dto/CrearMovimientoRequest.java`
  - `@NotNull cuentaId`, `@NotNull @DecimalMin(value="0", inclusive=false) valor` (se permite negativo: validar con `@NotNull` + lógica de dominio)
  - ajuste: usar valor positivo o negativo según el tipo
- `accounts-service/src/main/java/com/banking/accounts/application/dto/CrearAjusteRequest.java`
  - ⚠️ **Decisión A**: `@NotNull movimientoOrigenId`, `@NotNull valor`, `@NotBlank justificacion` con `@Size(max=500)`
- `accounts-service/src/main/java/com/banking/accounts/application/dto/CrearReversionRequest.java`
  - `@NotNull movimientoOrigenId`
- `accounts-service/src/main/java/com/banking/accounts/application/dto/MovimientoResponse.java`
  - `id`, `fecha`, `tipoMovimiento`, `valor`, `saldoResultante`, `cuentaId`, `movimientoOrigenId`, `justificacion`
- `accounts-service/src/main/java/com/banking/accounts/application/dto/ReporteItemResponse.java`
  - `fecha`, `cliente (String)`, `numeroCuenta`, `tipoCuenta`, `saldoInicial`, `estado`, `movimiento (BigDecimal)`, `saldoDisponible`

**Criterio de completitud**:
- `./gradlew :accounts-service:compileJava --no-daemon` sin errores.
- `CrearAjusteRequest` tiene el campo `justificacion` como `@NotBlank`.

---

### TASK-19 — Use cases de gestión de cuentas (HU-05 a HU-08)

**Rama**: `feature/HU-05-usecase-cuentas`
**Fase**: 5 | **Estimación**: 4h
**Depende de**: TASK-17, TASK-18

**Tests primero (Red)**:
- `accounts-service/src/test/java/com/banking/accounts/unit/usecase/EliminarCuentaUseCaseTest.java`
  - `eliminarCuentaSinMovimientosRecientesDebeDesactivar()` (HU-08 Flujo 1 — Decisión C)
  - `eliminarCuentaConMovimientosEnUltimoAnioDebeRetornar409()` (HU-08 Flujo 1 — Decisión C)
  - `eliminarCuentaInexistenteDebeLanzarCuentaNotFoundException()`
- `accounts-service/src/test/java/com/banking/accounts/unit/usecase/CrearCuentaUseCaseTest.java`
  - `crearCuentaConClienteActivoDebeRetornarCuentaCreada()`
  - `crearCuentaConClienteInexistenteDebeRetornar422()`
  - `crearCuentaConNumeroDuplicadoDebeRetornar409()`
  - `crearCuentaCorrienteConSaldoMenorA50DebeRetornar400()`

**Código de producción (Green)**:
- `accounts-service/src/main/java/com/banking/accounts/application/usecase/CrearCuentaUseCase.java`
  - valida cliente existe y está activo en proyección → 422
  - valida `numeroCuenta` único → 409
  - valida `saldoInicial` por tipo → 400
  - persiste y retorna `CuentaResponse`
- `accounts-service/src/main/java/com/banking/accounts/application/usecase/ConsultarCuentaUseCase.java`
  - busca por `id` → 404 si no existe
- `accounts-service/src/main/java/com/banking/accounts/application/usecase/ActualizarCuentaUseCase.java`
  - busca por `id` → 404; aplica cambios; persiste
- `accounts-service/src/main/java/com/banking/accounts/application/usecase/EliminarCuentaUseCase.java`
  - ⚠️ **Decisión C Flujo 1**: valida `existsMovimientoReciente(cuentaId)` → lanza excepción → HTTP 409
  - si no tiene actividad reciente: `cuenta.desactivar()` → persiste
  - **NO** se usa en el procesamiento de `ClienteDesactivadoEvent` — ese flujo tiene su propia lógica

**Criterio de completitud**:
- `EliminarCuentaUseCaseTest` y `CrearCuentaUseCaseTest` pasan 100% en verde sin contexto Spring.

---

### TASK-20 — Use cases de movimientos: depósito, retiro, consulta (HU-09, HU-09.1, HU-10)

**Rama**: `feature/HU-09-usecase-movimientos`
**Fase**: 5 | **Estimación**: 3h
**Depende de**: TASK-16, TASK-17, TASK-18

**Tests primero (Red)**:
- `accounts-service/src/test/java/com/banking/accounts/unit/usecase/RegistrarMovimientoUseCaseTest.java`
  - `depositoSobreCuentaActivaDebeIncrementarSaldo()`
  - `retiroConSaldoSuficienteDebeDecrementarSaldo()`
  - `retiroConSaldoInsuficienteDebeLanzarSaldoInsuficienteException()`
  - `retiroQueSuperaLimiteDiarioDebeLanzarLimiteDiarioExcedidoException()`
  - `movimientoConValorCeroDebeLanzarExcepcion()`
  - `movimientoSobreCuentaInactivaDebeLanzarCuentaInactivaException()`
  - `depositoNoAfectaLimiteDiarioDeRetiro()`

**Código de producción (Green)**:
- `accounts-service/src/main/java/com/banking/accounts/application/usecase/RegistrarMovimientoUseCase.java`
  - busca `Cuenta` → 422 si no existe
  - obtiene `acumuladoDiario` via `MovimientoRepository.sumRetirosDiariosByClienteId` (R-02)
  - ejecuta chain of validators
  - crea `Movimiento` (sin `justificacion`, sin `movimientoOrigenId`)
  - aplica `cuenta.aplicarMovimiento(valor)` → actualiza `saldoDisponible`
  - persiste movimiento y cuenta
  - retorna `MovimientoResponse`
- `accounts-service/src/main/java/com/banking/accounts/application/usecase/ConsultarMovimientoUseCase.java`
  - busca por `id` → `MovimientoNotFoundException` si no existe

**Criterio de completitud**:
- `RegistrarMovimientoUseCaseTest` pasa 100% en verde.
- El acumulado diario se calcula a nivel de `clienteId`, no de `cuentaId` (conforme R-02).

---

### TASK-21 — Use cases de ajuste y reversión: `RegistrarAjusteUseCase`, `RegistrarReversionUseCase` (HU-11.R, HU-12.R)

**Rama**: `feature/HU-11-usecase-ajuste-reversion`
**Fase**: 5 | **Estimación**: 3h
**Depende de**: TASK-16, TASK-17, TASK-18

**Tests primero (Red)**:
- `accounts-service/src/test/java/com/banking/accounts/unit/usecase/RegistrarAjusteUseCaseTest.java`
  - `ajusteConJustificacionValidaDebeCrearMovimiento()` — **Decisión A**
  - `ajusteSinJustificacionDebeLanzarJustificacionRequeridaException()` — **Decisión A**
  - `ajusteConMovimientoOrigenInexistenteDebeLanzarMovimientoNotFoundException()`
- `accounts-service/src/test/java/com/banking/accounts/unit/usecase/RegistrarReversionUseCaseTest.java`
  - `reversionDeDepositoDebeCrearMovimientoConValorOpuesto()` (R-08)
  - `reversionDeRetiroDebeActualizarSaldoCorrectamente()`
  - `reversionConMovimientoOrigenInexistenteDebeLanzarMovimientoNotFoundException()`

**Código de producción (Green)**:
- `accounts-service/src/main/java/com/banking/accounts/application/usecase/RegistrarAjusteUseCase.java`
  - recibe `CrearAjusteRequest` con `justificacion`
  - ⚠️ **Decisión A**: valida `justificacion != null && !justificacion.isBlank()` → lanza `JustificacionRequeridaException` si vacía
  - busca `movimientoOrigen` por `movimientoOrigenId` → 404 si no existe
  - busca `Cuenta` del movimiento origen
  - crea `Movimiento` con `tipo = ajuste`, `valor`, `justificacion`, `movimientoOrigenId`
  - aplica `cuenta.aplicarMovimiento(valor)`, persiste ambos
  - retorna `MovimientoResponse`
- `accounts-service/src/main/java/com/banking/accounts/application/usecase/RegistrarReversionUseCase.java`
  - busca `movimientoOrigen` → 404 si no existe
  - crea `Movimiento` con `tipo = reversion`, `valor = -1 * movimientoOrigen.getValor()`, `movimientoOrigenId`
  - `justificacion = null` (conforme Decisión A: solo ajuste requiere justificación)
  - aplica `cuenta.aplicarMovimiento(valor)` (R-08), persiste

**Criterio de completitud**:
- `RegistrarAjusteUseCaseTest` y `RegistrarReversionUseCaseTest` pasan 100% en verde.
- Test `ajusteSinJustificacionDebeLanzarJustificacionRequeridaException` pasa en verde.

---

### TASK-22 — `GenerarReporteUseCase` (HU-13)

**Rama**: `feature/HU-13-usecase-reporte`
**Fase**: 5 | **Estimación**: 2h
**Depende de**: TASK-17, TASK-18

**Tests primero (Red)**:
- `accounts-service/src/test/java/com/banking/accounts/unit/usecase/GenerarReporteUseCaseTest.java`
  - `reporteConClienteYMovimientosEnRangoDebeRetornarListaNoVacia()`
  - `reporteConClienteInexistenteDebeLanzarClienteNotFoundException()`
  - `reporteSinParametrosRequeridosDebeLanzarExcepcion()`
  - `reporteConRangoSinMovimientosDebeRetornarListaVacia()`

**Código de producción (Green)**:
- `accounts-service/src/main/java/com/banking/accounts/application/usecase/GenerarReporteUseCase.java`
  - recibe `Long clienteId`, `LocalDate fechaInicio`, `LocalDate fechaFin`
  - verifica cliente en proyección → 404 si no existe
  - query de movimientos en rango de fechas por las cuentas del cliente
  - mapea a `List<ReporteItemResponse>` con los campos: fecha, cliente, numeroCuenta, tipoCuenta, saldoInicial, estado, movimiento (valor), saldoDisponible

**Criterio de completitud**:
- `GenerarReporteUseCaseTest` pasa 100% en verde sin contexto Spring.

---

## FASE 6 — accounts-service — Infraestructura

---

### TASK-23 — Persistencia JPA de `accounts-service` + `BaseDatos.sql` con `justificacion`

**Rama**: `feature/HU-05-persistencia-accounts`
**Fase**: 6 | **Estimación**: 4h
**Depende de**: TASK-17, TASK-19, TASK-20, TASK-21, TASK-22

**Tests primero (Red)**:
- `accounts-service/src/test/java/com/banking/accounts/integration/AccountControllerIntegrationTest.java`
  - tests de persistencia: crear cuenta retorna HTTP 201 con ID de BD.
  - Usa `@SpringBootTest` + `@Testcontainers` con PostgreSQL 15.

**Código de producción (Green)**:
- `accounts-service/src/main/java/com/banking/accounts/infrastructure/persistence/SpringDataCuentaRepository.java`
  - `JpaRepository<Cuenta, Long>` + `existsByNumeroCuenta`, `findByClienteIdAndEstado`
- `accounts-service/src/main/java/com/banking/accounts/infrastructure/persistence/SpringDataMovimientoRepository.java`
  - query `@Query`: `sumRetirosDiariosByClienteId` (conforme R-02 con `JOIN cuenta`, filtro por `tipo = retiro` y `fecha::date = CURRENT_DATE`)
  - query `existsMovimientoReciente`: `EXISTS` con `fecha >= NOW() - INTERVAL '1 year'` (R-07)
- `accounts-service/src/main/java/com/banking/accounts/infrastructure/persistence/SpringDataClienteProyeccionRepository.java`
  - `JpaRepository<ClienteProyeccion, Long>` + `findByClienteId`
- `accounts-service/src/main/java/com/banking/accounts/infrastructure/persistence/AccountRepositoryJpa.java`
  - implementa `CuentaRepository`
- `accounts-service/src/main/java/com/banking/accounts/infrastructure/persistence/MovimientoRepositoryJpa.java`
  - implementa `MovimientoRepository`
- `accounts-service/src/main/java/com/banking/accounts/infrastructure/persistence/ClienteProyeccionRepositoryJpa.java`
  - implementa `ClienteProyeccionRepository`
- `accounts-service/src/main/resources/BaseDatos.sql`
  - ⚠️ **Decisión A**: tabla `movimiento` debe incluir `justificacion VARCHAR(500) NULL`
  - DDL completo: `cliente_proyeccion`, `cuenta`, `movimiento`
  - INSERTs de datos de prueba conforme data-model.md

**Criterio de completitud**:
- Tests de integración de persistencia pasan con PostgreSQL real.
- `BaseDatos.sql` incluye `justificacion VARCHAR(500) NULL` en la tabla `movimiento`.
- Las queries de límite diario y movimiento reciente funcionan correctamente.

---

### TASK-24 — Consumer RabbitMQ `accounts-service` + `RabbitMQConfig` (Decisión B + C Flujo 2)

**Rama**: `feature/HU-14-consumer-accounts`
**Fase**: 6 | **Estimación**: 4h
**Depende de**: TASK-17, TASK-23

**Tests primero (Red)**:
- `accounts-service/src/test/java/com/banking/accounts/integration/ClienteEventConsumerIntegrationTest.java`
  - `consumirClienteCreatedEventDebeInsertarEnProyeccion()` (idempotente: segunda ejecución no duplica — R-06)
  - `consumirClienteDesactivadoEventDebeActualizarEstadoEnProyeccion()` — **Decisión B**
  - `consumirClienteDesactivadoEventDebeDesactivarTodasLasCuentasActivasSinValidarAntiguedad()` — **Decisión B + C Flujo 2**
  - `consumirClienteDesactivadoEventCuentaConMovimientosRecientesDebeDesactivarseIgualmente()` — **Decisión C**: Flujo 2 ignora la regla de HU-08
  - `mensajeNoProcesoDebeIrADLQSinBloquearCola()` (DLQ)
  - Usa `@SpringBootTest` + `RabbitMQContainer` + `PostgreSQLContainer` (Testcontainers)

**Código de producción (Green)**:
- `accounts-service/src/main/java/com/banking/accounts/infrastructure/config/RabbitMQConfig.java`
  - `TopicExchange("cliente.events")` durable
  - `DirectExchange("cliente.events.dlx")` durable
  - `Queue("accounts.cliente.created")` con DLX
  - `Queue("accounts.cliente.deleted")` con DLX
  - `Queue("accounts.cliente.dlq")`
  - Bindings conforme contracts/events.md
- `accounts-service/src/main/java/com/banking/accounts/infrastructure/messaging/ClienteEventConsumer.java`
  - `@RabbitListener` con `AcknowledgeMode.MANUAL`
  - Para `ClienteCreatedEvent`: upsert en `ClienteProyeccionRepository` (R-06)
  - Para `ClienteDesactivadoEvent`:
    1. `UPDATE cliente_proyeccion SET estado = 'inactivo'`
    2. ⚠️ **Decisión B**: `UPDATE cuenta SET estado = 'inactiva' WHERE cliente_id = :clienteId AND estado = 'activa'` — sin verificar antigüedad de movimientos ni saldo
    3. ⚠️ **Decisión C Flujo 2**: **NO llama** a `EliminarCuentaUseCase`; lógica independiente directa
  - `channel.basicAck` en éxito; `channel.basicNack(tag, false, false)` en error irrecuperable → DLQ
- `accounts-service/src/main/java/com/banking/accounts/infrastructure/mapper/ClienteMapper.java`
  - desserializa JSON de RabbitMQ → `ClienteCreatedEvent` / `ClienteDesactivadoEvent`

**Criterio de completitud**:
- `ClienteEventConsumerIntegrationTest` pasa 100% con RabbitMQ y PostgreSQL reales (Testcontainers).
- Test `consumirClienteDesactivadoEventCuentaConMovimientosRecientesDebeDesactivarseIgualmente` pasa en verde (confirma Decisión C).
- Upsert de `ClienteCreatedEvent` es idempotente.

---

### TASK-25 — Controllers REST `accounts-service` + `GlobalExceptionHandler`

**Rama**: `feature/HU-05-controllers-accounts`
**Fase**: 6 | **Estimación**: 4h
**Depende de**: TASK-19, TASK-20, TASK-21, TASK-22, TASK-23

**Tests primero (Red)**:
- `accounts-service/src/test/java/com/banking/accounts/integration/AccountControllerIntegrationTest.java`
  - `POST /cuentas` con cliente activo → HTTP 201
  - `POST /cuentas` con cliente inexistente → HTTP 422
  - `POST /cuentas` número duplicado → HTTP 409
  - `POST /cuentas` saldo corriente < $50 → HTTP 400
  - `GET /cuentas/{id}` → HTTP 200 / 404
  - `PUT /cuentas/{id}` → HTTP 200 / 404
  - `DELETE /cuentas/{id}` sin actividad reciente → HTTP 200 (Decisión C Flujo 1)
  - `DELETE /cuentas/{id}` con actividad en último año → HTTP 409 (Decisión C Flujo 1)
- `accounts-service/src/test/java/com/banking/accounts/integration/MovimientoControllerIntegrationTest.java`
  - `POST /movimientos` depósito → HTTP 201
  - `POST /movimientos` retiro saldo insuficiente → HTTP 422, mensaje exacto `"Saldo no disponible"`
  - `POST /movimientos` límite diario excedido → HTTP 422, mensaje exacto `"Límite de retiro diario excedido"`
  - `POST /movimientos` valor cero → HTTP 400, mensaje exacto `"El valor del movimiento no puede ser cero"`
  - `POST /ajustes` sin `justificacion` → HTTP 400 (Decisión A)
  - `POST /ajustes` con `justificacion` válida → HTTP 201
  - `POST /reversiones` → HTTP 201
  - `GET /movimientos/{id}` → HTTP 200 / 404
  - `GET /reportes` con parámetros válidos → HTTP 200
  - `GET /reportes` sin parámetros → HTTP 400
  - `GET /reportes` cliente inexistente → HTTP 404

**Código de producción (Green)**:
- `accounts-service/src/main/java/com/banking/accounts/infrastructure/controller/AccountController.java`
  - `@RestController @RequestMapping("/cuentas")`
  - CRUD completo delegando en use cases correspondientes
- `accounts-service/src/main/java/com/banking/accounts/infrastructure/controller/MovimientoController.java`
  - `@RestController`
  - `POST /movimientos` → `RegistrarMovimientoUseCase`
  - `GET /movimientos/{id}` → `ConsultarMovimientoUseCase`
  - `POST /ajustes` → `RegistrarAjusteUseCase`
  - `POST /reversiones` → `RegistrarReversionUseCase`
- `accounts-service/src/main/java/com/banking/accounts/infrastructure/controller/ReporteController.java`
  - `GET /reportes?clienteId={id}&fechaInicio={fecha}&fechaFin={fecha}` → `GenerarReporteUseCase`
  - parámetros requeridos: `@RequestParam(required = true)`
- `accounts-service/src/main/java/com/banking/accounts/infrastructure/controller/GlobalExceptionHandler.java`
  - `@RestControllerAdvice`
  - `SaldoInsuficienteException` → 422 + `"Saldo no disponible"`
  - `LimiteDiarioExcedidoException` → 422 + `"Límite de retiro diario excedido"`
  - `CuentaInactivaException` → 422
  - `CuentaNotFoundException`, `MovimientoNotFoundException` → 404
  - `NumeroCuentaDuplicadoException` → 409
  - `JustificacionRequeridaException` → 400
  - `MethodArgumentNotValidException` → 400 (Bean Validation)
  - `MissingServletRequestParameterException` → 400 (parámetros faltantes en /reportes)
  - formato estándar: `{ timestamp, status, error, message, path }`

**Criterio de completitud**:
- `AccountControllerIntegrationTest` y `MovimientoControllerIntegrationTest` pasan 100% con PostgreSQL real.
- Mensajes exactos de RN-01, RN-02, RN-03 verificados en tests de integración.

---

### TASK-26 — `Dockerfile` y `application.yml` productivo de `accounts-service`

**Rama**: `feature/HU-17-docker-accounts`
**Fase**: 6 | **Estimación**: 1h
**Depende de**: TASK-25

**Tests primero (Red)**:
- No hay test unitario. Validación: `docker build` termina sin error.

**Código de producción (Green)**:
- `accounts-service/Dockerfile`
  - multi-stage: `maven:3.9-eclipse-temurin-21` para build, `eclipse-temurin:21-jre` para runtime
  - sin comentarios (G-08)
- `accounts-service/src/main/resources/application.yml`
  - `spring.application.name: accounts-service`
  - `server.port: 8081`
  - datasource: `${DATASOURCE_URL}`, `${DATASOURCE_USERNAME}`, `${DATASOURCE_PASSWORD}`
  - `spring.jpa.hibernate.ddl-auto: validate`
  - `spring.sql.init.mode: always`
  - rabbitmq: `${RABBITMQ_HOST}`, `${RABBITMQ_PORT}`, `${RABBITMQ_USERNAME}`, `${RABBITMQ_PASSWORD}`

**Criterio de completitud**:
- `docker build -t accounts-service ./accounts-service` finaliza sin errores.
- `application.yml` no contiene valores hardcodeados; usa variables de entorno.

---

## FASE 7 — Integración completa y validación final

---

### TASK-27 — Prueba de integración end-to-end: flujo events entre microservicios (HU-14)

**Rama**: `feature/HU-14-integracion-end-to-end`
**Fase**: 7 | **Estimación**: 4h
**Depende de**: TASK-13, TASK-26

**Tests primero (Red)**:
- `accounts-service/src/test/java/com/banking/accounts/integration/ClienteEventConsumerIntegrationTest.java` (completar con escenario end-to-end):
  - `flujoCompletoCrearClientePublicarEventoYCrearCuenta()`:
    1. Simular recepción de `ClienteCreatedEvent` en accounts-service
    2. Verificar que el `clienteId` está en proyección local con estado activo
    3. Crear cuenta para ese `clienteId` → HTTP 201 esperado
  - `flujoCompletoDesactivarClientePublicarEventoYVerificarCuentasInactivas()`:
    1. Insertar cliente y cuentas activas en BD
    2. Simular recepción de `ClienteDesactivadoEvent`
    3. Verificar que `cliente_proyeccion.estado = 'inactivo'`
    4. Verificar que todas las cuentas activas del cliente pasan a `inactiva`
    5. ⚠️ **Decisión B**: verificar que cuentas con movimientos recientes también se desactivan (Flujo 2)
    6. Intentar crear nueva cuenta para ese cliente → HTTP 422

**Código de producción (Green)**:
- No se crean archivos nuevos; los adaptadores ya están implementados.
- Si hay bugs detectados aquí, se corrigen en los archivos existentes de FASE 6.

**Criterio de completitud**:
- Todos los tests end-to-end pasan con PostgreSQL y RabbitMQ reales (Testcontainers).
- El comportamiento de Decisión B (desactivación forzada sin validar antigüedad) está cubierto por al menos un test verde.

---

### TASK-28 — `docker-compose.yml` final con health checks, named volumes y orden de arranque (HU-17)

**Rama**: `feature/HU-17-docker-compose-final`
**Fase**: 7 | **Estimación**: 2h
**Depende de**: TASK-13, TASK-26

**Tests primero (Red)**:
- No hay test unitario. Validación: `docker compose up` levanta los 5 contenedores sanos.

**Código de producción (Green)**:
- `docker-compose.yml` (en la raíz del repositorio) con los 5 servicios:

```yaml
services:
  db_customers:
    image: postgres:15
    environment: { POSTGRES_DB, POSTGRES_USER, POSTGRES_PASSWORD }
    volumes: [customers-data:/var/lib/postgresql/data]
    healthcheck: { test: pg_isready, interval: 10s, retries: 5 }

  db_accounts:
    image: postgres:15
    environment: { POSTGRES_DB, POSTGRES_USER, POSTGRES_PASSWORD }
    ports: ["5433:5432"]
    volumes: [accounts-data:/var/lib/postgresql/data]
    healthcheck: { test: pg_isready, interval: 10s, retries: 5 }

  rabbitmq:
    image: rabbitmq:3.12-management
    ports: ["5672:5672", "15672:15672"]
    healthcheck: { test: rabbitmq-diagnostics check_port_connectivity, interval: 10s, retries: 5 }

  customers-service:
    build: ./customers-service
    ports: ["8080:8080"]
    depends_on:
      db_customers: { condition: service_healthy }
      rabbitmq: { condition: service_healthy }
    environment: { DATASOURCE_URL, DATASOURCE_USERNAME, DATASOURCE_PASSWORD, RABBITMQ_HOST, ... }

  accounts-service:
    build: ./accounts-service
    ports: ["8081:8081"]
    depends_on:
      db_accounts: { condition: service_healthy }
      rabbitmq: { condition: service_healthy }
    environment: { DATASOURCE_URL, DATASOURCE_USERNAME, DATASOURCE_PASSWORD, RABBITMQ_HOST, ... }

volumes:
  customers-data:
  accounts-data:
```

**Criterio de completitud**:
- `docker compose up` desde raíz del repositorio en entorno limpio levanta los 5 contenedores.
- `docker compose down && docker compose up` retiene los datos en volúmenes nombrados.
- Health checks de BD y RabbitMQ bloquean el arranque de los microservicios hasta estar listos.
- Los microservicios responden correctamente después del reinicio.

---

### TASK-29 — Actualizar `ADR-004` (Decisión D)

**Rama**: `feature/docs-adr-004-desactivacion-cuentas`
**Fase**: 7 | **Estimación**: 1h
**Depende de**: TASK-09, TASK-24

**Tests primero (Red)**:
- No aplica: tarea de documentación.

**Código de producción (Green)**:
- `docs/ADR-sistema-bancario-microservicios.md` — actualizar ADR-004 registrando las siguientes secciones:

  **Título**: ADR-004 — Consistencia eventual en desactivación de cliente y sus cuentas

  **Contenido nuevo a agregar**:
  1. **Decisión**: La desactivación de un cliente via `DELETE /clientes/{id}` ocurre sin verificar si el cliente tiene cuentas activas. `customers-service` no es responsable de validar el estado de las cuentas.
  2. **Flujo de Choreography**: `ClienteDesactivadoEvent` es consumido por `accounts-service`, que desactiva todas las cuentas activas del cliente sin aplicar las reglas de HU-08 (no se verifica antigüedad de movimientos ni saldo).
  3. **Consistencia eventual**: Existe una ventana de tiempo entre la desactivación del cliente y la desactivación de sus cuentas, aceptada como decisión de negocio documentada.
  4. **Eliminación de `ClienteConCuentasActivasException`**: Esta excepción fue eliminada de `customers-service` por diseño arquitectónico. `RN-04` queda descartada conforme a esta ADR.
  5. **Flujos independientes**: `EliminarCuentaUseCase` (HU-08) y el procesamiento de `ClienteDesactivadoEvent` son flujos independientes que no comparten lógica de validación (Decisión C).

**Criterio de completitud**:
- El ADR actualizado documenta explícitamente los 5 puntos listados.
- No hay contradicción entre el ADR y el código implementado.

---

### TASK-30 — Validación final: cobertura, smoke test y revisión de calidad (HU-15, HU-16)

**Rama**: `feature/HU-15-HU-16-validacion-final`
**Fase**: 7 | **Estimación**: 3h
**Depende de**: T027, T028, T029

**Tests primero (Red)**:
- Ejecutar el suite completo y verificar que no hay tests en rojo.

**Código de producción (Green)**:
- Corregir cualquier test fallido detectado en la validación.
- Ajustar configuración de Jacoco en `build.gradle.kts` de ambos módulos:
  - `customers-service/build.gradle.kts`: regla de cobertura `LINE` ≥ 100% en paquetes `domain.*`
  - `customers-service/build.gradle.kts`: regla de cobertura `LINE` ≥ 80% en paquetes `application.*`
  - `accounts-service/build.gradle.kts`: mismas reglas
- Verificar ausencia de comentarios en todos los archivos (G-08):
  - `find . -name "*.java" | xargs grep -l "//"` debe devolver cero resultados
  - `find . -name "*.yml" | xargs grep -l "^#"` debe devolver cero resultados (excepto la línea `#` obligatoria de inicio si la hay)

**Criterio de completitud**:

| Criterio | Umbral |
|---|---|
| Cobertura de dominio `customers-service` | 100% |
| Cobertura de dominio `accounts-service` | 100% |
| Cobertura de `application.*` en ambos servicios | ≥ 80% |
| Tests en rojo | 0 |
| Comentarios en código fuente | 0 |
| `docker compose up` desde repositorio limpio | ✅ |
| Endpoints responden conforme contratos | ✅ (verificar con `curl` del quickstart.md) |
| Mensajes exactos RN-01, RN-02, RN-03 | ✅ |

---

## Resumen estadístico

| Métrica | Valor |
|---|---|
| Total de tareas | 30 |
| FASE 0 (Scaffolding) | 1 tarea |
| FASE 1 (customers dominio) | 3 tareas |
| FASE 2 (customers aplicación) | 4 tareas |
| FASE 3 (customers infraestructura) | 4 tareas |
| FASE 4 (accounts dominio) | 4 tareas |
| FASE 5 (accounts aplicación) | 5 tareas |
| FASE 6 (accounts infraestructura) | 4 tareas |
| FASE 7 (integración final) | 4 tareas |
| Estimación total | **~60 horas** |
| Estimación paralela máxima (FASE 1+4 simultáneas) | **~42 horas calendario** |

### Decisiones arquitectónicas aplicadas por tarea

| Decisión | Tareas afectadas |
|---|---|
| A — `justificacion` en ajuste | T015, T018, T021, T23, T25 |
| B — Choreography puro / sin ClienteConCuentasActivasException | T03, T09, T15, T24, T27, T29 |
| C — Dos flujos de desactivación independientes | T09, T19, T24, T25, T27 |
| D — ADR-004 a actualizar | T29 |

### Alcance MVP sugerido

Para una validación inicial funcional, implementar en orden:
`T01 → T02 → T03 → T04 → T05 → T06 → T07 → T08 → T09 → T10 → T11 → T12`

Esto entrega **customers-service completo** y funcional (Épica 1 — HU-01 a HU-04) verificable con `curl` antes de comenzar `accounts-service`.
