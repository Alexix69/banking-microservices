# Architecture Decision Record
## Sistema Bancario — Microservicios
**ADR-001 | Versión 1.0 | Abril 2026**
**Estado: Aprobado**
**Autor: Equipo de arquitectura**

---

## Tabla de contenido

1. Contexto y problema
2. Alcance y restricciones
3. ADR-001 — Separación en dos microservicios
4. ADR-002 — Arquitectura hexagonal por servicio
5. ADR-003 — Comunicación asincrónica con RabbitMQ
6. ADR-004 — Patrón de comunicación: Choreography con Domain Events
7. ADR-005 — Estrategia de persistencia: base de datos por servicio
8. ADR-006 — Patrón Repository y separación de capas
9. ADR-007 — Principios SOLID aplicados
10. ADR-008 — Estrategia de manejo de errores
11. ADR-009 — Consistencia eventual y proyección local de clientes
12. ADR-010 — Estrategia de pruebas
13. ADR-011 — Despliegue con Docker Compose
14. Mapa de dependencias y tecnologías

---

## 1. Contexto y problema

Se requiere construir un sistema de gestión bancaria simplificada que exponga APIs REST para la administración de clientes, cuentas y movimientos. El sistema debe ser implementado siguiendo una **arquitectura de microservicios** con comunicación asincrónica entre ellos, desplegado en contenedores Docker, con pruebas automatizadas y manejo robusto de errores.

El ejercicio es evaluado a nivel **Senior**, lo que implica que no solo debe funcionar correctamente, sino que cada decisión de diseño debe ser justificable técnicamente, considerando factores de rendimiento, escalabilidad y resiliencia.

---

## 2. Alcance y restricciones

**Restricciones dadas por el enunciado:**
- Lenguaje: Java Spring Boot (elección para esta solución)
- ORM: JPA / Hibernate (explícitamente requerido)
- Base de datos: relacional
- Despliegue: Docker
- Validación de APIs: Postman

**Principios de diseño adoptados:**
- Cada microservicio es autónomo (datos, lógica, despliegue independiente)
- No hay llamadas sincrónicas entre microservicios en producción
- El dominio no depende de la infraestructura
- Todo comportamiento de negocio está cubierto por pruebas

---

## 3. ADR-001 — Separación en dos microservicios

### Decisión
El sistema se divide en exactamente **dos microservicios**:
- `customers-service`: gestiona las entidades Persona y Cliente
- `accounts-service`: gestiona las entidades Cuenta y Movimiento, y genera reportes

### Justificación

**Cohesión funcional:** Persona y Cliente comparten un aggregate natural (Cliente hereda de Persona, viven en el mismo contexto de dominio). Separarlos en distintos servicios crearía comunicación sincrónica innecesaria entre entidades que son parte del mismo bounded context.

**Separación por cambio:** Las razones por las que cambia la lógica de clientes son distintas a las razones por las que cambia la lógica de cuentas. Un nuevo requerimiento de validación de identidad afecta únicamente a `customers-service`. Una nueva regla de límites de retiro afecta únicamente a `accounts-service`. Esta separación permite desplegar cada servicio de forma independiente.

**Bounded Contexts (DDD):** Siguiendo los principios de Domain-Driven Design, se identifican dos contextos delimitados: el contexto de Identidad (quien es el cliente) y el contexto Financiero (qué tiene y hace el cliente). Cada microservicio corresponde a uno de estos contextos.

### Alternativas descartadas

**Un solo servicio monolítico:** Incumple el requerimiento explícito del enunciado. Además concentra toda la complejidad en un único punto de despliegue, eliminando la independencia de escala y cambio.

**Tres o más microservicios (Persona, Cliente, Cuenta, Movimiento por separado):** Genera fragmentación excesiva. Persona no tiene valor autónomo sin Cliente; Movimiento sin Cuenta no tiene contexto. La granularidad excesiva crea más overhead de comunicación que valor arquitectónico.

---

## 4. ADR-002 — Arquitectura hexagonal por servicio

### Decisión
Cada microservicio implementa **Arquitectura Hexagonal (Ports & Adapters)**, organizando el código en tres zonas:

```
┌─────────────────────────────────────────────┐
│                  DOMINIO                     │
│   Entidades, Value Objects, Domain Events    │
│   Interfaces de repositorio (puertos)        │
│   Lógica de negocio pura (sin dependencias) │
└──────────────┬──────────────────────────────┘
               │ depende de
┌──────────────▼──────────────────────────────┐
│              APLICACIÓN                      │
│   Use Cases / Services                       │
│   Orquesta el dominio                        │
│   No conoce HTTP ni JPA                     │
└──────────────┬──────────────────────────────┘
               │ implementa puertos de
┌──────────────▼──────────────────────────────┐
│           INFRAESTRUCTURA                    │
│   Controladores REST (adaptador entrada)     │
│   Repositorios JPA (adaptador salida)        │
│   Publicador de eventos RabbitMQ             │
└─────────────────────────────────────────────┘
```

### Justificación

**Testabilidad:** La lógica de dominio no tiene dependencias de Spring, JPA ni RabbitMQ. Las pruebas unitarias instancian directamente las clases de dominio sin necesidad de mocks de infraestructura. Esto hace que las pruebas sean rápidas, deterministas y fáciles de escribir.

**Principio de Inversión de Dependencias:** El dominio define interfaces (puertos) como `ClienteRepository` o `EventPublisher`. La infraestructura las implementa. El dominio nunca importa clases de JPA o Spring AMQP.

**Mantenibilidad:** Cambiar la base de datos de PostgreSQL a otra (o de RabbitMQ a Kafka) solo requiere reescribir el adaptador correspondiente. El dominio y los casos de uso no cambian.

### Alternativas descartadas

**Arquitectura en capas tradicional (Controller → Service → Repository):** Es más simple de implementar inicialmente pero acopla la lógica de negocio a detalles de infraestructura. Las anotaciones JPA en las entidades de dominio son un ejemplo de este problema: la entidad de negocio termina siendo también un artefacto de persistencia.

---

## 5. ADR-003 — Comunicación asincrónica con RabbitMQ

### Decisión
La comunicación entre `customers-service` y `accounts-service` se realiza mediante **mensajería asincrónica usando RabbitMQ** con el protocolo AMQP.

### Justificación

**Desacoplamiento temporal:** `accounts-service` no depende de que `customers-service` esté disponible para operar. Si `customers-service` cae, los eventos publicados antes de la caída persisten en la cola de RabbitMQ y son consumidos cuando el servicio se recupera.

**Resiliencia:** Bajo el modelo sincrónico (REST directo), un fallo en `customers-service` produce un fallo en cascada hacia `accounts-service`. Con el broker como intermediario, cada servicio falla de forma independiente.

**Adecuación al volumen:** El sistema gestiona eventos de creación, actualización y eliminación de clientes. Este volumen es moderado (decenas o centenas de eventos por hora en producción). RabbitMQ es suficiente y su operación es simple: un nodo en Docker Compose, sin necesidad de cluster ni gestión de particiones.

### RabbitMQ vs Kafka — análisis de la decisión

| Criterio | RabbitMQ | Kafka |
|---|---|---|
| Modelo | Cola (push al consumer) | Log distribuido (pull por offset) |
| Retención del mensaje | Hasta que el consumer hace ack | Configurable (días, semanas) |
| Replay de eventos | No soportado | Sí, rebobinando el offset |
| Múltiples consumers del mismo evento | Con fanout exchange | Nativo por consumer group |
| Throughput | Miles/seg | Millones/seg |
| Complejidad operacional | Baja (1 nodo Docker) | Alta (cluster 3+ nodos, particiones) |
| Curva de aprendizaje | Baja | Alta |

**Kafka sería la elección correcta si:** se necesitara replay de millones de eventos históricos, si múltiples sistemas distintos (analytics, operacional, auditoría) consumieran el mismo stream simultáneamente, o si el throughput superara los cientos de miles de mensajes por segundo.

**Para este sistema**, el único evento relevante es `ClienteCreado` / `ClienteEliminado` con un solo consumidor. Usar Kafka introduciría complejidad operacional de un sistema diseñado para Netflix con el fin de resolver un problema que RabbitMQ resuelve con 10 líneas de configuración YAML.

### Topología de mensajería

```
customers-service
      │
      │ publica a
      ▼
[Exchange: cliente.events]  (tipo: topic)
      │
      ├── routing key: cliente.created  ──► [Queue: accounts.cliente.created]
      │                                            │
      │                                            ▼ consume
      │                                     accounts-service
      │
      └── routing key: cliente.deleted  ──► [Queue: accounts.cliente.deleted]
                                                   │
                                                   ▼ consume
                                            accounts-service
```

**Uso de Topic Exchange:** Permite enrutar mensajes por routing key, lo que facilita agregar nuevos consumidores o nuevos tipos de eventos sin modificar el productor. `audit-service` podría suscribirse a `cliente.*` sin tocar `customers-service`.

### Configuración de resiliencia

- **Dead Letter Queue (DLQ):** Si el consumer falla al procesar un mensaje después de N reintentos, el mensaje se redirige a una DLQ para inspección manual, sin bloquear la cola principal.
- **Durabilidad:** Las colas y mensajes se configuran como `durable: true` y `persistent: true` para sobrevivir reinicios del broker.
- **Acknowledgement manual:** El consumer hace `ack` solo después de persistir exitosamente el evento. Si la operación falla, el mensaje vuelve a la cola.

---

## 6. ADR-004 — Patrón de comunicación: Choreography con Domain Events

### Decisión
Se adopta el patrón **Choreography** (coreografía) para la coordinación entre microservicios, con **Domain Events** como mecanismo de emisión.

### Choreography vs Orchestration

**Choreography:** Cada servicio conoce sus propias reglas de reacción. Cuando `customers-service` publica `ClienteCreado`, `accounts-service` reacciona de forma autónoma. No existe un coordinador central. Los servicios se conocen a través de los contratos de eventos, no entre sí.

```
customers-service → [ClienteCreado] → broker → accounts-service reacciona
                                             → (futuro) notif-service reacciona
                                             → (futuro) audit-service reacciona
```

**Orchestration:** Un orquestador central dirige el flujo, enviando comandos a cada servicio y esperando respuestas. Es apropiado cuando el flujo es transaccional y requiere compensaciones (patrón Saga): si el paso 2 falla, el orquestador revierte el paso 1.

### Por qué Choreography para este sistema

El único flujo inter-servicio es la sincronización de identidad del cliente: cuando un cliente es creado o eliminado, `accounts-service` necesita actualizar su proyección local. Este flujo no requiere compensación: no hay una transacción distribuida que revertir. No hay lógica de "si accounts-service falla al sincronizar, revertir la creación del cliente".

Agregar un orquestador introduciría un punto de fallo único y un componente adicional sin valor para el caso de uso real. En un sistema donde sí existiera una transacción de alta consecuencia (por ejemplo: débito + crédito entre dos cuentas de distintos bancos), Orchestration con Saga sería la elección correcta.

### Domain Events

Un **Domain Event** es un evento que nace dentro de la lógica de dominio, como resultado de que una regla de negocio se cumplió. La diferencia con un evento de infraestructura es conceptual pero importante: garantiza que el evento refleja un hecho de negocio validado.

```java
// El aggregate Cliente genera el evento DESPUÉS de validar todas las reglas
public class Cliente extends Persona {
    public static Cliente create(CrearClienteCommand cmd) {
        validarEdad(cmd.edad());
        validarIdentificacion(cmd.identificacion());
        validarContrasena(cmd.contrasena());
        Cliente cliente = new Cliente(cmd);
        cliente.registerEvent(new ClienteCreatedEvent(cliente.getClienteId(), ...));
        return cliente;
    }
}

// La infraestructura recoge y publica el evento DESPUÉS de persistir
@Service
public class ClienteService {
    public Cliente crearCliente(CrearClienteCommand cmd) {
        Cliente cliente = Cliente.create(cmd);
        clienteRepository.save(cliente);
        eventPublisher.publish(cliente.getDomainEvents()); // publica tras persistir
        return cliente;
    }
}
```

El evento se publica **después de la persistencia exitosa**. Si la base de datos falla, el evento no se emite y no hay inconsistencia.

### Decisión D — Desactivación de cuentas por evento (adición posterior al diseño inicial)

**Decisión**: La desactivación de un cliente via `DELETE /clientes/{id}` ocurre sin verificar si el cliente tiene cuentas activas. `customers-service` no es responsable de validar el estado de las cuentas.

**Flujo de Choreography**: `ClienteDesactivadoEvent` es consumido por `accounts-service`, que desactiva todas las cuentas activas del cliente sin aplicar las reglas de HU-08 (no se verifica antigüedad de movimientos ni saldo).

**Consistencia eventual**: Existe una ventana de tiempo entre la desactivación del cliente y la desactivación de sus cuentas, aceptada como decisión de negocio documentada.

**Eliminación de ClienteConCuentasActivasException**: Esta excepción fue eliminada de `customers-service` por diseño arquitectónico. RN-04 queda descartada en ese contexto.

**Flujos independientes**: `EliminarCuentaUseCase` (HU-08) y el procesamiento de `ClienteDesactivadoEvent` son flujos independientes que no comparten lógica de validación (Decisión C).

**Garantía ejecutable**: `deactivationShouldNeverCheckForActiveCuentas()` en `EliminarClienteUseCaseTest` verifica con `verifyNoMoreInteractions(clienteRepository)` que no existe verificación de cuentas en el use case.

---

## 7. ADR-005 — Estrategia de persistencia: base de datos por servicio

### Decisión
Cada microservicio tiene **su propia instancia de base de datos PostgreSQL**, aislada e inaccesible desde el otro servicio.

```
customers-service  ──►  PostgreSQL (db_customers)
accounts-service   ──►  PostgreSQL (db_accounts)
```

### Justificación

**Autonomía:** Si ambos servicios compartieran base de datos, un cambio en el esquema de Clientes podría romper las consultas de Cuentas. La base de datos compartida es el antipatrón más frecuente en arquitecturas de microservicios que en la práctica operan como monolitos acoplados.

**Escalabilidad independiente:** Si `accounts-service` requiere más capacidad de I/O por el volumen de movimientos, su base de datos puede escalarse sin afectar a `customers-service`.

**Libertad tecnológica:** Aunque ambos usan PostgreSQL en esta solución (por simplicidad y coherencia del stack), la separación permite que en el futuro uno migre a otra tecnología si el caso de uso lo justifica.

### Modelo de datos por servicio

**db_customers:**
```
persona (id PK, nombre, genero, edad, identificacion, direccion, telefono)
cliente (cliente_id PK, contrasena, estado, persona_id FK → persona.id)
```

**db_accounts:**
```
cliente_proyeccion (cliente_id PK, nombre, estado)  ← copia local sincronizada por eventos
cuenta (id PK, numero_cuenta UNIQUE, tipo, saldo_inicial, saldo_disponible, estado, cliente_id FK → cliente_proyeccion.cliente_id)
movimiento (id PK, fecha, tipo, valor, saldo_resultante, cuenta_id FK → cuenta.id)
```

La tabla `cliente_proyeccion` en `db_accounts` es una **proyección local** del estado del cliente, mantenida sincronizada mediante los eventos del broker. Esto permite a `accounts-service` validar la existencia y estado de un cliente sin hacer llamadas sincrónicas a `customers-service`.

### Herencia JPA: Persona → Cliente

```java
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "persona")
public class Persona { ... }

@Entity
@Table(name = "cliente")
public class Cliente extends Persona { ... }
```

`InheritanceType.JOINED` genera dos tablas con FK entre ellas, reflejando el modelo relacional de forma limpia. Es preferible a `SINGLE_TABLE` (que usa una sola tabla con columnas nulas) y a `TABLE_PER_CLASS` (que duplica las columnas de Persona en cada subclase).

---

## 8. ADR-006 — Patrón Repository y separación de capas

### Decisión
Se implementa el **patrón Repository** con interfaces definidas en el dominio e implementadas en la capa de infraestructura.

```java
// DOMINIO — define el contrato (puerto)
public interface ClienteRepository {
    Cliente save(Cliente cliente);
    Optional<Cliente> findById(ClienteId id);
    boolean existsByIdentificacion(String identificacion);
    void delete(ClienteId id);
}

// INFRAESTRUCTURA — implementa con JPA (adaptador)
@Repository
public class ClienteRepositoryJpa implements ClienteRepository {
    @Autowired
    private ClienteJpaRepository jpaRepo; // Spring Data JPA
    // implementación...
}
```

### Justificación

El dominio define **qué** necesita (la interfaz), sin saber **cómo** se hace (la implementación). Esto permite:

- Testear los servicios de dominio con implementaciones en memoria sin levantar base de datos
- Cambiar la tecnología de persistencia sin modificar el dominio
- Expresar las operaciones en lenguaje de negocio (`findByIdentificacion`) en lugar de lenguaje técnico (`SELECT * FROM cliente WHERE identificacion = ?`)

### Separación DTO ↔ Entidad de dominio

Las entidades JPA y las entidades de dominio son la misma clase en este proyecto (por la restricción del enunciado que exige JPA en las entidades). Para mantener la separación de contratos, se usan **DTOs** en los límites de entrada y salida:

```
HTTP Request (JSON)
    → RequestDTO (validaciones Bean Validation)
        → Mapper → Entidad de dominio
            → Lógica de negocio
        → Mapper → ResponseDTO
    → HTTP Response (JSON)
```

Las entidades de dominio nunca se exponen directamente en los endpoints.

---

## 9. ADR-007 — Principios SOLID aplicados

### Single Responsibility Principle

Cada clase tiene una única razón para cambiar:

- `MovimientoService`: solo orquesta el registro de movimientos
- `SaldoInsuficienteValidator`: solo valida que haya saldo disponible
- `LimiteDiarioValidator`: solo valida el límite de retiro diario
- `ClienteEventPublisher`: solo publica eventos al broker

La validación de movimientos no vive en el servicio; vive en objetos validadores especializados.

### Open/Closed Principle

Los validadores de movimientos implementan una interfaz común:

```java
public interface MovimientoValidator {
    void validate(Movimiento movimiento, Cuenta cuenta);
}

@Component
public class SaldoInsuficienteValidator implements MovimientoValidator { ... }

@Component
public class LimiteDiarioValidator implements MovimientoValidator { ... }

@Component
public class CuentaActivaValidator implements MovimientoValidator { ... }
```

El servicio de movimientos recibe la lista de validadores por inyección de dependencias. Agregar una nueva regla de validación requiere crear una nueva clase que implemente la interfaz, sin modificar ninguna clase existente.

### Liskov Substitution Principle

La herencia `Persona → Cliente` es honesta: `Cliente` extiende `Persona` sin romper sus contratos. Un `Cliente` puede usarse en cualquier lugar donde se espera una `Persona`. No hay métodos sobreescritos que lancen excepciones no previstas ni cambien precondiciones.

### Interface Segregation Principle

Los repositorios exponen solo lo que sus consumidores necesitan:

```java
// accounts-service solo necesita leer estado del cliente
public interface ClienteProyeccionRepository {
    boolean existsById(ClienteId id);
    boolean isActive(ClienteId id);
}

// customers-service necesita el CRUD completo
public interface ClienteRepository {
    Cliente save(Cliente cliente);
    Optional<Cliente> findById(ClienteId id);
    void delete(ClienteId id);
    // ...
}
```

### Dependency Inversion Principle

Los servicios de aplicación dependen de abstracciones (interfaces), nunca de implementaciones concretas:

```java
public class MovimientoService {
    private final MovimientoRepository movimientoRepository; // interfaz, no JPA
    private final CuentaRepository cuentaRepository;          // interfaz, no JPA
    private final List<MovimientoValidator> validators;        // interfaz, no clases concretas

    public MovimientoService(MovimientoRepository r, CuentaRepository c,
                              List<MovimientoValidator> v) {
        this.movimientoRepository = r;
        this.cuentaRepository = c;
        this.validators = v;
    }
}
```

---

## 10. ADR-008 — Estrategia de manejo de errores

### Decisión
Se implementa un **Global Exception Handler** centralizado (`@ControllerAdvice`) que mapea excepciones de dominio a respuestas HTTP estandarizadas.

### Jerarquía de excepciones

```
DomainException (base)
    ├── BusinessRuleException (reglas de negocio violadas)
    │       ├── SaldoInsuficienteException        → HTTP 422
    │       ├── LimiteDiarioExcedidoException     → HTTP 422
    │       ├── ClienteConCuentasActivasException  → HTTP 409
    │       └── CuentaInactivaException           → HTTP 422
    ├── ResourceNotFoundException                  → HTTP 404
    │       ├── ClienteNotFoundException
    │       └── CuentaNotFoundException
    └── DuplicateResourceException                → HTTP 409
            └── IdentificacionDuplicadaException
```

### Formato de respuesta de error estandarizado

```json
{
  "timestamp": "2026-04-14T10:30:00Z",
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "Saldo no disponible",
  "path": "/movimientos"
}
```

### Por qué no usar solo HTTP 400 para todo

Usar el código HTTP correcto permite a los consumidores de la API tomar decisiones programáticas sin parsear el mensaje de error:
- `400` → el request está malformado (cliente debe corregir su código)
- `404` → recurso no encontrado (cliente debe verificar el ID)
- `409` → conflicto de estado (el recurso existe pero hay un conflicto)
- `422` → el request es válido pero la regla de negocio lo rechaza

---

## 11. ADR-009 — Consistencia eventual y proyección local de clientes

### Decisión
`accounts-service` mantiene una **proyección local** de los clientes (`cliente_proyeccion`), sincronizada mediante eventos del broker, aceptando **consistencia eventual**.

### Implicaciones de la consistencia eventual

Cuando se crea un cliente en `customers-service`, existe una ventana de tiempo (milisegundos a segundos) en la que el cliente aún no está disponible en `accounts-service`. Durante esta ventana, un intento de crear una cuenta para ese cliente sería rechazado.

**Esta es una decisión de negocio aceptable** porque:
1. La ventana es muy pequeña bajo condiciones normales
2. El flujo natural del usuario (crear cliente, luego crear cuenta) tiene suficiente tiempo entre ambas operaciones
3. La alternativa (llamada sincrónica) crea acoplamiento que compromete la resiliencia del sistema

### Manejo del evento `ClienteEliminado`

Cuando `customers-service` emite `ClienteEliminado`, `accounts-service` actualiza el estado del cliente en su proyección a `inactivo`. A partir de ese momento, no se pueden crear nuevas cuentas para ese cliente ni registrar movimientos en sus cuentas existentes.

---

## 12. ADR-010 — Estrategia de pruebas

### Decisión

| Tipo | Herramienta | Qué verifica | Nivel |
|---|---|---|---|
| Unitaria | JUnit 5 + Mockito | Lógica de dominio aislada, sin Spring ni BD | HU-15 |
| Integración | Spring Boot Test + Testcontainers | Flujo completo: Controller → Service → JPA → BD real | HU-16 |

### Prueba unitaria: entidad Cliente (HU-15)

Verifica reglas de negocio puras sin infraestructura:

```java
@Test
void clienteMenorDeEdadNoPuedeCrearse() {
    CrearClienteCommand cmd = new CrearClienteCommand("Jose", 17, ...);
    assertThrows(EdadInvalidaException.class, () -> Cliente.create(cmd));
}

@Test
void contrasenaDebesTenerMinimoOchoCaracteres() {
    CrearClienteCommand cmd = new CrearClienteCommand(..., "abc123");
    assertThrows(ContrasenaInvalidaException.class, () -> Cliente.create(cmd));
}
```

No hay `@SpringBootTest`, no hay `@MockBean`, no hay base de datos. El test se ejecuta en milisegundos.

### Prueba de integración (HU-16)

Verifica el flujo completo con contenedores reales:

```java
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
class CrearMovimientoIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    @Test
    void retiroConSaldoInsuficienteRetorna422() {
        // Crea cuenta con saldo 100
        // Intenta retiro de 200
        // Verifica HTTP 422 y mensaje "Saldo no disponible"
        // Verifica que el saldo de la cuenta no cambió
    }
}
```

Testcontainers levanta una instancia real de PostgreSQL en Docker durante la prueba, sin necesidad de una base de datos externa ni mocks.

---

## 13. ADR-011 — Despliegue con Docker Compose

### Decisión
Toda la solución se levanta con un único `docker-compose up` desde la raíz del proyecto.

### Servicios definidos en docker-compose.yml

```yaml
services:
  db-customers:        # PostgreSQL instancia 1
  db-accounts:         # PostgreSQL instancia 2
  rabbitmq:            # RabbitMQ con management UI
  customers-service:   # Spring Boot, depende de db-customers y rabbitmq
  accounts-service:    # Spring Boot, depende de db-accounts y rabbitmq
```

### Orden de arranque y health checks

Los microservicios no arrancan hasta que sus dependencias (BD y broker) estén saludables. Se usan `healthcheck` en los contenedores de PostgreSQL y RabbitMQ, y `depends_on: condition: service_healthy` en los microservicios.

### Inicialización de base de datos

El script `BaseDatos.sql` se monta como volumen en el directorio de inicialización de PostgreSQL (`/docker-entrypoint-initdb.d/`), asegurando que el esquema y los datos de prueba se apliquen automáticamente en el primer arranque.

### Persistencia de datos

Los volúmenes de PostgreSQL se declaran como named volumes, garantizando que los datos sobrevivan a `docker-compose down` y estén disponibles en el siguiente `docker-compose up`.

---

## 14. Mapa de dependencias y tecnologías

### customers-service

| Capa | Tecnología | Rol |
|---|---|---|
| Framework | Spring Boot 3.x | Runtime, DI, auto-configuration |
| API REST | Spring Web MVC | Controllers, @ControllerAdvice |
| Persistencia | Spring Data JPA + Hibernate | Repositorios, mapeo ORM |
| Base de datos | PostgreSQL 15 | Almacenamiento relacional |
| Mensajería | Spring AMQP (RabbitMQ) | Publicación de Domain Events |
| Validaciones | Bean Validation (jakarta) | @Valid, @NotNull, @Size |
| Pruebas unitarias | JUnit 5 + Mockito | Lógica de dominio aislada |
| Pruebas integración | Spring Boot Test + Testcontainers | Flujo completo con BD real |
| Contenedor | Docker (imagen eclipse-temurin:21) | Despliegue |

### accounts-service

| Capa | Tecnología | Rol |
|---|---|---|
| Framework | Spring Boot 3.x | Runtime, DI, auto-configuration |
| API REST | Spring Web MVC | Controllers, @ControllerAdvice |
| Persistencia | Spring Data JPA + Hibernate | Repositorios, mapeo ORM |
| Base de datos | PostgreSQL 15 | Almacenamiento relacional |
| Mensajería | Spring AMQP (RabbitMQ) | Consumo de Domain Events |
| Validaciones | Bean Validation (jakarta) | @Valid en DTOs |
| Pruebas unitarias | JUnit 5 + Mockito | Validadores de movimientos |
| Pruebas integración | Spring Boot Test + Testcontainers | Flujo completo con BD y broker |
| Contenedor | Docker (imagen eclipse-temurin:21) | Despliegue |

### Infraestructura compartida

| Componente | Tecnología | Versión |
|---|---|---|
| Broker de mensajería | RabbitMQ | 3.12-management |
| Base de datos | PostgreSQL | 15 |
| Orquestación local | Docker Compose | v2 |

---

*Documento generado como parte del proceso de diseño previo a la implementación. Cualquier desviación durante la implementación debe ser documentada como una nueva entrada ADR o como una revisión de esta.*
