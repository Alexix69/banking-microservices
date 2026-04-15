<!--
SYNC IMPACT REPORT
==================
Version change: 1.0.1 → 1.0.2
Bump type: PATCH

Modified principles: sección 2.3 (nombre de evento de desactivación)

Added sections:
  1. Propósito y alcance
  2. Arquitectura (2.1 Microservicios, 2.2 Interna, 2.3 Comunicación, 2.4 Persistencia)
  3. Stack tecnológico
  4. Idioma del código
  5. Desarrollo guiado por pruebas (TDD)
  6. Gestión de ramas — Gitflow
  7. Código limpio y autodocumentado
  8. Patrones de diseño obligatorios
  9. Principios SOLID — aplicación concreta
  10. Manejo de errores
  11. Despliegue
  12. Estructura de directorios por microservicio

Removed sections: All placeholder template slots (fully replaced)

Templates requiring updates:
  ✅ .specify/templates/plan-template.md — generic; no outdated references found
  ✅ .specify/templates/spec-template.md — generic; no outdated references found
  ✅ .specify/templates/tasks-template.md — generic; no outdated references found
  ✅ .specify/templates/checklist-template.md — generic; no outdated references found
  ✅ .specify/templates/agent-file-template.md — generic; no outdated references found

Deferred TODOs: None
-->

# Constitution — Banking Microservices

## 1. Propósito y alcance

Este documento establece los principios no negociables del proyecto
banking-microservices. Todo agente, desarrollador o herramienta que
interactúe con este repositorio debe respetar estas reglas sin excepción.
Cualquier decisión que contradiga esta constitution requiere una revisión
explícita y documentada del documento antes de proceder.

El sistema implementa una solución bancaria simplificada compuesta por
dos microservicios autónomos: customers-service y accounts-service,
con comunicación asincrónica entre ellos.

---

## 2. Arquitectura

### 2.1 Microservicios

El sistema se divide en exactamente dos microservicios:

- customers-service: gestiona las entidades Persona y Cliente.
  Corresponde al bounded context de Identidad.
- accounts-service: gestiona las entidades Cuenta y Movimiento,
  genera reportes y mantiene una proyección local de clientes.
  Corresponde al bounded context Financiero.

No se crearán servicios adicionales sin una revisión explícita
de esta constitution.

### 2.2 Arquitectura interna por servicio

Cada microservicio implementa Arquitectura Hexagonal (Ports & Adapters)
con tres zonas estrictas:

- Dominio: entidades, value objects, domain events, interfaces de
  repositorio (puertos), lógica de negocio pura. Sin dependencias
  de Spring, JPA ni RabbitMQ.
- Aplicación: use cases y services. Orquesta el dominio. No conoce
  HTTP ni detalles de persistencia.
- Infraestructura: controladores REST, repositorios JPA, publicador
  y consumidor de eventos. Implementa los puertos del dominio.

La dirección de dependencias es siempre hacia adentro:
infraestructura → aplicación → dominio. Nunca al revés.

### 2.3 Comunicación entre microservicios

La comunicación entre customers-service y accounts-service es
exclusivamente asincrónica mediante RabbitMQ con el protocolo AMQP.

No existen llamadas REST sincrónicas entre microservicios en ningún
escenario de producción.

El patrón de coordinación es Choreography con Domain Events:
customers-service publica eventos de dominio (ClienteCreatedEvent,
ClienteDesactivadoEvent); accounts-service los consume y actualiza
su proyección local de forma autónoma.

### 2.4 Persistencia

Cada microservicio tiene su propia instancia de PostgreSQL,
aislada e inaccesible desde el otro servicio.

- customers-service → db_customers
- accounts-service → db_accounts

accounts-service mantiene una tabla cliente_proyeccion sincronizada
mediante eventos. La consistencia es eventual y es una decisión
de negocio aceptada.

---

## 3. Stack tecnológico

El stack es fijo y no puede cambiarse sin revisión de esta constitution.

| Componente | Tecnología | Versión mínima |
|---|---|---|
| Lenguaje | Java | 21 LTS |
| Framework | Spring Boot | 3.x |
| ORM | Spring Data JPA + Hibernate | incluido en Spring Boot 3 |
| Base de datos | PostgreSQL | 15 |
| Mensajería | RabbitMQ | 3.12 |
| Contenedores | Docker + Docker Compose | Compose v2+ |
| Pruebas unitarias | JUnit 5 + Mockito | incluido en Spring Boot 3 |
| Pruebas integración | Spring Boot Test + Testcontainers | 1.19+ |

---

## 4. Idioma del código

Se aplica el esquema híbrido Opción B de forma estricta:

### En español

- Nombres de entidades de dominio: Cliente, Persona, Cuenta, Movimiento
- Nombres de DTOs de entrada y salida: CrearClienteRequest, ClienteResponse
- Nombres de endpoints y rutas: /clientes, /cuentas, /movimientos, /reportes
- Nombres de campos de negocio en entidades y DTOs:
  numeroCuenta, saldoDisponible, tipoMovimiento
- Mensajes de error expuestos en la API: "Saldo no disponible",
  "El cliente posee cuentas activas que deben ser desactivadas primero"
- Nombres de excepciones de dominio: SaldoInsuficienteException,
  ClienteConCuentasActivasException, CuentaInactivaException
- Nombres de eventos de dominio: ClienteCreatedEvent, ClienteDeletedEvent
- Documentación, specs, ADRs, commits y nombres de rama

### En inglés

- Clases de infraestructura: CustomerController, AccountRepository,
  RabbitMQPublisher, GlobalExceptionHandler
- Clases de configuración: SecurityConfig, RabbitMQConfig, DataSourceConfig
- Clases de utilidad y helpers
- Nombres de tests y métodos de test
- Nombres de métodos de test y clases de test
- Variables internas de implementación fuera del dominio

### Regla de desempate

Si un nombre pertenece al lenguaje del negocio descrito en el enunciado
del proyecto, va en español. Si pertenece a un patrón técnico o
infraestructura, va en inglés.
Los nombres de métodos de test siguen el patrón:
subjectStateOrAction + Should + ExpectedOutcome
Ejemplos:
  clienteWithValidDataShouldCreateInstance()
  clienteWithAgeLessThan18ShouldThrowException()
  withdrawalWithInsufficientBalanceShouldReturnHttp422()
  accountWithRecentActivityShouldNotBeDeleted()
---

## 5. Desarrollo guiado por pruebas (TDD)

TDD es obligatorio en todo el proyecto sin excepción.

El ciclo es siempre: Red → Green → Refactor.

1. Se escribe la prueba primero. El código de producción no existe aún.
2. La prueba falla (Red). Esto confirma que la prueba es válida.
3. Se escribe el mínimo código necesario para que la prueba pase (Green).
4. Se refactoriza manteniendo las pruebas en verde (Refactor).

Ningún método de negocio puede existir sin una prueba que lo cubra.

### Pruebas unitarias

- Cubren lógica de dominio pura.
- Sin anotaciones de Spring (@SpringBootTest está prohibido en unitarias).
- Sin acceso a base de datos ni red.
- Se ejecutan en milisegundos.
- Usan JUnit 5 y Mockito para dependencias externas al dominio.

### Pruebas de integración

- Cubren el flujo completo: Controller → Service → Repository → BD real.
- Usan @SpringBootTest con webEnvironment = RANDOM_PORT.
- Usan Testcontainers para levantar PostgreSQL y RabbitMQ reales.
- Una prueba de integración nunca usa H2 ni mocks de infraestructura.

### Cobertura mínima

- Lógica de dominio: 100%
- Casos de uso (services): 80%
- Controladores: cubiertos por pruebas de integración

---

## 6. Gestión de ramas — Gitflow

El flujo de trabajo de Git es Gitflow estricto:

```
main          ← código en producción, solo recibe merges de release/ y hotfix/
develop       ← integración continua, rama base para features
feature/      ← desarrollo de cada HU, se abre desde develop
release/      ← preparación de release, se abre desde develop
hotfix/       ← correcciones urgentes en producción, se abre desde main
```

### Convención de nombres de rama

```
feature/HU-XX-descripcion-corta
release/vX.X.X
hotfix/descripcion-corta
```

Ejemplos:

- feature/HU-01-crear-cliente
- feature/HU-09-registrar-movimiento
- release/v1.0.0

### Convención de commits (Conventional Commits)

```
tipo(alcance): descripción en español en imperativo

feat(customers): agregar validación de edad mínima en Cliente
test(customers): agregar prueba unitaria para contraseña inválida
fix(accounts): corregir cálculo de saldo tras movimiento de ajuste
refactor(accounts): extraer validadores de movimiento a clases propias
docs: actualizar ADR-003 con topología de RabbitMQ
chore: configurar Testcontainers en accounts-service
```

Tipos permitidos: feat, fix, test, refactor, docs, chore, ci, perf.

Cada commit debe corresponder a un ciclo TDD completo o a una
refactorización con pruebas en verde.

---

## 7. Código limpio y autodocumentado

### Regla absoluta: cero comentarios en cualquier archivo de cualquier tipo

Esto incluye:

- Comentarios de línea (//)
- Comentarios de bloque (/**/)
- Javadoc (/** */)
- Comentarios TODO o FIXME
- Comentarios en archivos de configuración YAML/properties
- Comentarios en scripts bash o Dockerfile

El código expresa su intención a través de nombres. Si se siente la
necesidad de escribir un comentario, el nombre del método o variable
debe ser reescrito hasta que el comentario sea innecesario.

### Reglas de nomenclatura

- Métodos: verbos en infinitivo que describen la acción completa.
  Ejemplos: crearCliente(), validarSaldoSuficiente(), registrarMovimiento()
- Clases: sustantivos que describen exactamente su responsabilidad.
  Una clase no puede tener más de una responsabilidad (SRP).
- Variables: nombres completos, sin abreviaciones.
  saldoDisponible en lugar de sd. numeroCuenta en lugar de nroCta.
- Constantes: UPPER_SNAKE_CASE. LIMITE_RETIRO_DIARIO = 500.
- Tests: el nombre describe el escenario completo en inglés.
  clienteWithAgeLessThan18ShouldThrowException()
  withdrawalWithInsufficientBalanceShouldReturnHttp422()

### Tamaño de métodos y clases

- Un método hace exactamente una cosa.
- Máximo 20 líneas por método. Si supera este límite, debe extraerse.
- Máximo 200 líneas por clase. Si supera este límite, debe dividirse.

---

## 8. Patrones de diseño obligatorios

### Repository Pattern

Cada entidad tiene su interfaz de repositorio definida en el dominio.
La implementación JPA vive en infraestructura.
El dominio nunca importa interfaces de Spring Data directamente.

### DTO Pattern

Las entidades de dominio nunca se exponen en los endpoints.
Cada operación tiene su propio DTO de request y de response.
La conversión entre DTO y entidad es responsabilidad de un Mapper.

### Domain Events

Los eventos de dominio nacen dentro de los aggregates, después de
que todas las reglas de negocio se han validado y la entidad se ha
persistido exitosamente.
La infraestructura recoge y publica los eventos al broker.

### Chain of Validators

Las reglas de validación de movimientos se implementan como objetos
que implementan la interfaz MovimientoValidator.
El servicio ejecuta la cadena completa antes de persistir.
Agregar una nueva regla de validación no modifica clases existentes.

### Global Exception Handler

Un único @ControllerAdvice por microservicio centraliza el mapeo
de excepciones de dominio a respuestas HTTP.
Las excepciones de dominio nunca exponen detalles de infraestructura.

---

## 9. Principios SOLID — aplicación concreta

### Single Responsibility

Cada clase tiene exactamente una razón para cambiar.
Los validadores, mappers, publishers y handlers son clases separadas.

### Open/Closed

El sistema es abierto a extensión y cerrado a modificación.
Nuevas reglas de validación = nueva clase, no modificar existentes.

### Liskov Substitution

La herencia Persona → Cliente no rompe contratos.
Cliente puede usarse en cualquier contexto donde se espera Persona.
Estrategia JPA: InheritanceType.JOINED.

### Interface Segregation

Los repositorios exponen solo las operaciones que sus consumidores necesitan.
La proyección de cliente en accounts-service tiene su propia interfaz mínima,
separada del repositorio completo de customers-service.

### Dependency Inversion

Los services dependen de interfaces, nunca de implementaciones concretas.
Spring inyecta las implementaciones; el dominio no conoce Spring.

---

## 10. Manejo de errores

### Jerarquía de excepciones de dominio

```
DomainException
├── BusinessRuleException
│   ├── SaldoInsuficienteException        → HTTP 422
│   ├── LimiteDiarioExcedidoException     → HTTP 422
│   ├── CuentaInactivaException           → HTTP 422
│   └── ClienteConCuentasActivasException → HTTP 409
├── ResourceNotFoundException             → HTTP 404
│   ├── ClienteNotFoundException
│   ├── CuentaNotFoundException
│   └── MovimientoNotFoundException
└── DuplicateResourceException            → HTTP 409
    └── IdentificacionDuplicadaException
```

### Formato de respuesta de error

```json
{
  "timestamp": "ISO-8601",
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "Saldo no disponible",
  "path": "/movimientos"
}
```

Ninguna respuesta de error expone stack traces, nombres de clases
internas ni detalles de infraestructura.

---

## 11. Despliegue

Toda la solución se levanta con un único comando desde la raíz:

```bash
docker compose up
```

Esto inicia: db_customers, db_accounts, rabbitmq,
customers-service y accounts-service.

Los microservicios no arrancan hasta que sus dependencias
superen los health checks definidos.

El script BaseDatos.sql se aplica automáticamente en el primer arranque
mediante el mecanismo docker-entrypoint-initdb.d de PostgreSQL.

Los datos persisten entre reinicios mediante named volumes.

---

## 12. Estructura de directorios por microservicio

```
{service}-service/
├── src/
│   ├── main/
│   │   ├── java/com/banking/{service}/
│   │   │   ├── domain/
│   │   │   │   ├── model/          ← entidades y value objects
│   │   │   │   ├── event/          ← domain events
│   │   │   │   ├── exception/      ← excepciones de dominio
│   │   │   │   ├── port/           ← interfaces de repositorio y servicios
│   │   │   │   └── service/        ← lógica de dominio pura
│   │   │   ├── application/
│   │   │   │   ├── usecase/        ← casos de uso
│   │   │   │   └── dto/            ← request y response DTOs
│   │   │   └── infrastructure/
│   │   │       ├── controller/     ← REST controllers
│   │   │       ├── persistence/    ← implementaciones JPA
│   │   │       ├── messaging/      ← RabbitMQ publisher/consumer
│   │   │       ├── mapper/         ← conversión DTO ↔ entidad
│   │   │       └── config/         ← Spring configuration
│   │   └── resources/
│   │       └── application.yml
│   └── test/
│       ├── unit/                   ← pruebas unitarias de dominio
│       └── integration/            ← pruebas de integración con Testcontainers
├── Dockerfile
└── pom.xml
```

---

*Este documento es la fuente de verdad del proyecto. Toda spec, plan
y tarea generada por cualquier agente debe ser consistente con él.*

**Version**: 1.0.2 | **Ratified**: 2026-04-14 | **Last Amended**: 2026-04-14
