# Sistema Bancario — Microservicios

## Descripción general

Solución de evaluación técnica que implementa un sistema bancario simplificado como dos microservicios autónomos con comunicación asincrónica. El sistema expone APIs REST para la administración de clientes, cuentas y movimientos, aplica arquitectura hexagonal estricta por servicio, y se despliega completamente con un único comando Docker Compose.

---

## Arquitectura

```
customers-service (Identidad)        accounts-service (Financiero)
  Dominio: Persona, Cliente            Dominio: Cuenta, Movimiento
  Aplicación: Use Cases                Aplicación: Use Cases
  Infra: REST, JPA, RabbitMQ           Infra: REST, JPA, RabbitMQ
       │  db_customers                      │  db_accounts
       │                                    │
       └──── RabbitMQ: cliente.events ──────┘
              ClienteCreatedEvent   (routing: cliente.created)
              ClienteDesactivadoEvent (routing: cliente.desactivado)
```

- **customers-service** gestiona Persona y Cliente como aggregate raíz del contexto de Identidad.
- **accounts-service** gestiona Cuenta, Movimiento y Reportes, manteniendo una proyección local de clientes sincronizada por eventos.
- Comunicación exclusivamente asincrónica mediante **Choreography con Domain Events** sobre RabbitMQ.
- Cada servicio tiene su propia instancia de PostgreSQL; no comparten base de datos.
- La consistencia entre servicios es eventual: existe una ventana de milisegundos entre la creación/desactivación de un cliente y la actualización de la proyección local en `accounts-service`. Esto está documentado como decisión de negocio aceptada.

## Stack tecnológico

| Componente | Tecnología | Versión |
|---|---|---|
| Lenguaje | Java | 21 LTS |
| Framework | Spring Boot | 3.2.5 |
| Persistencia + Mensajería | Spring Data JPA / Spring AMQP | — |
| Base de datos / Broker | PostgreSQL / RabbitMQ | 15 / 3.12 |
| Build | Gradle multi-módulo | 8.7 |
| Pruebas integración / Mapeo | Testcontainers / MapStruct | 1.20.4 / 1.5.5 |
| Contenedores | Docker + Compose | v2 |

## Estructura del proyecto

```
banking-microservices/
├── docker-compose.yml  ·  build.gradle.kts  ·  settings.gradle.kts
├── docs/ADR-sistema-bancario-microservicios.md
├── specs/001-sistema-bancario-completo/  (spec, plan, tasks, contracts/)
├── customers-service/src/main/java/com/banking/customers/
│   ├── domain/        (model, exception, event, port)
│   ├── application/   (usecase, dto)
│   └── infrastructure/(controller, persistence, messaging, config)
└── accounts-service/src/main/java/com/banking/accounts/
    ├── domain/        (model, exception, validator, port)
    ├── application/   (usecase, dto)
    └── infrastructure/(controller, persistence, messaging, config)
```

---

## Decisiones arquitectónicas clave

- **Hexagonal estricta**: el dominio no importa Spring, JPA ni RabbitMQ. Las interfaces de repositorio viven en el dominio; la infraestructura las implementa. Pruebas unitarias sin contexto.
- **RabbitMQ sobre Kafka**: flujo inter-servicio con un solo consumidor y volumen moderado. Kafka introduciría complejidad operacional sin beneficio real.
- **Choreography sobre Orchestration**: no hay transacción distribuida que requiera compensación; cada servicio reacciona autónomamente al evento.
- **Decisión B**: `DELETE /clientes/{id}` desactiva incondicionalmente. `accounts-service` desactiva cuentas asociadas al consumir `ClienteDesactivadoEvent`, sin validar antigüedad ni saldo.
- **Decisión C**: `EliminarCuentaUseCase` (API directa) valida movimientos del último año → HTTP 409. El consumer de `ClienteDesactivadoEvent` usa lógica independiente y desactiva siempre.

## Requisitos previos

- Docker Engine 24+
- Docker Compose v2+
- Sin dependencias adicionales en el host (JDK, Maven, etc.)

---

## Levantar el stack completo

```bash
docker compose up --build
```

El stack estará listo cuando los cinco contenedores estén saludables. Los health checks de PostgreSQL y RabbitMQ bloquean el arranque de los microservicios hasta que sus dependencias estén disponibles.

| Servicio | URL |
|---|---|
| customers-service REST API | http://localhost:8080 |
| accounts-service REST API | http://localhost:8081 |
| RabbitMQ Management UI | http://localhost:15672 (guest / guest) |

## Ejecutar las pruebas

```bash
./gradlew :customers-service:test
./gradlew :accounts-service:test
./gradlew test
```

- **164 tests** en total (unitarios e integración)
- **100% de cobertura** en la capa de dominio de ambos servicios
- Las pruebas de integración levantan instancias reales de PostgreSQL y RabbitMQ mediante Testcontainers; no requieren infraestructura externa

---

## Endpoints disponibles

| Servicio | Método | Ruta | Descripción |
|---|---|---|---|
| customers (8080) | POST | /clientes | Crear cliente |
| | GET | /clientes/{id} | Consultar cliente |
| | PUT | /clientes/{id} | Actualizar cliente |
| | DELETE | /clientes/{id} | Desactivar cliente |
| accounts (8081) | POST | /cuentas | Crear cuenta |
| | GET | /cuentas/{id} | Consultar cuenta |
| | PUT | /cuentas/{id} | Actualizar cuenta |
| | DELETE | /cuentas/{id} | Desactivar cuenta |
| | POST | /movimientos | Registrar depósito o retiro |
| | GET | /movimientos/{id} | Consultar movimiento |
| | POST | /ajustes | Registrar ajuste |
| | POST | /reversiones | Registrar reversión |
| | GET | /reportes | Reporte por cliente y rango de fechas |

Para ejemplos de request/response, importar `banking-microservices-postman-collection.json` y configurar `baseUrlCustomers = http://localhost:8080` y `baseUrlAccounts = http://localhost:8081`.

---

## Reglas de negocio destacadas

- Límite de retiro diario de **$500 por cliente**, acumulado entre todas sus cuentas
- Eliminación lógica de clientes y cuentas; nunca se borran registros de la base de datos
- Saldo insuficiente retorna HTTP 422 con mensaje exacto `"Saldo no disponible"`
- Límite diario superado retorna HTTP 422 con mensaje exacto `"Límite de retiro diario excedido"`
- Movimiento con valor cero retorna HTTP 400 con mensaje exacto `"El valor del movimiento no puede ser cero"`
- Validación de cédula ecuatoriana con algoritmo módulo 10
- El ajuste de movimiento requiere campo `justificacion` obligatorio (máx. 500 caracteres)

## Documentación técnica

| Artefacto | Descripción |
|---|---|
| `specs/.../spec.md` | 17 HUs con escenarios de aceptación |
| `specs/.../plan.md` | Plan técnico y análisis de decisiones |
| `specs/.../tasks.md` | 30 tareas con criterios TDD y dependencias |
| `specs/.../contracts/` | Contratos de API REST y eventos RabbitMQ |
| `docs/ADR-*.md` | Registro de decisiones arquitectónicas (ADR-001 a ADR-011) |
