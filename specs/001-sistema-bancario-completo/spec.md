# Feature Specification: Sistema Bancario — Microservicios

**Feature Branch**: `001-sistema-bancario-completo`
**Created**: 2026-04-14
**Status**: Draft

---

## Resumen del sistema

El sistema implementa una solución bancaria simplificada expuesta como APIs REST. Está compuesto por dos microservicios autónomos con comunicación asincrónica entre ellos:

- **customers-service** — bounded context de Identidad: gestiona Personas y Clientes.
- **accounts-service** — bounded context Financiero: gestiona Cuentas, Movimientos y Reportes; mantiene una proyección local de clientes sincronizada por eventos.

Toda la solución se despliega con un único comando desde la raíz del repositorio. No existe comunicación sincrónica entre los dos microservicios en ningún escenario de producción.

---

## User Scenarios & Testing

### Épica 1 — Gestión de Personas y Clientes (Priority: P1)

*Pertenece a: `customers-service`*

Permite registrar, consultar, actualizar y desactivar clientes bancarios. Es la base de todo el sistema: sin clientes válidos no pueden existir cuentas ni movimientos.

**Why this priority**: P1 porque todas las épicas posteriores dependen de la existencia de clientes válidos en el sistema.

**Independent Test**: Se puede probar de forma completa creando, consultando, actualizando y eliminando un cliente mediante `POST /clientes`, `GET /clientes/{id}`, `PUT /clientes/{id}` y `DELETE /clientes/{id}`.

#### HU-01 — Crear cliente

**Acceptance Scenarios**:

1. **Dado** que envío `POST /clientes` con nombre, género (MASCULINO o FEMENINO), edad ≥ 18, identificación en formato de cédula ecuatoriana válida, dirección, teléfono con prefijo de país válido, contraseña con al menos 8 caracteres alfanuméricos y una mayúscula, y estado, **Cuando** el sistema procesa la solicitud, **Entonces** persiste el registro y devuelve HTTP 201 con el recurso creado.

2. **Dado** que envío `POST /clientes` con una identificación que ya existe en el sistema, **Cuando** el sistema valida la unicidad, **Entonces** devuelve HTTP 409 con mensaje descriptivo.

3. **Dado** que envío `POST /clientes` con edad menor a 18, formato de identificación inválido, teléfono sin prefijo de país, contraseña que incumple la política, o algún campo obligatorio nulo, **Cuando** el sistema valida la solicitud, **Entonces** devuelve HTTP 400 indicando el campo y el motivo del error.

#### HU-02 — Consultar cliente

**Acceptance Scenarios**:

1. **Dado** que realizo `GET /clientes/{id}` con un ID válido y existente, **Cuando** el sistema procesa la solicitud, **Entonces** devuelve HTTP 200 con los datos completos del cliente.

2. **Dado** que realizo `GET /clientes/{id}` con un ID que no existe, **Cuando** el sistema busca el recurso, **Entonces** devuelve HTTP 404.

#### HU-03 — Actualizar cliente

**Acceptance Scenarios**:

1. **Dado** que realizo `PUT /clientes/{id}` con datos válidos sobre un cliente existente, **Cuando** el sistema procesa la solicitud, **Entonces** actualiza los campos enviados, persiste los cambios y devuelve HTTP 200 con el recurso actualizado.

2. **Dado** que realizo `PUT /clientes/{id}` con un ID que no existe, **Cuando** el sistema intenta localizar el recurso, **Entonces** devuelve HTTP 404.

3. **Dado** que realizo `PUT /clientes/{id}` intentando cambiar la identificación a una ya registrada por otro cliente, **Cuando** el sistema valida la unicidad, **Entonces** devuelve HTTP 409 sin aplicar cambios.

#### HU-04 — Eliminar cliente (lógicamente)

**Acceptance Scenarios**:

1. **Dado** que realizo `DELETE /clientes/{id}` sobre un cliente que no tiene cuentas activas, **Cuando** el sistema procesa la solicitud, **Entonces** cambia el estado del cliente a inactivo y devuelve HTTP 200.

2. **Dado** que realizo `DELETE /clientes/{id}` con un ID que no existe, **Cuando** el sistema intenta localizar el recurso, **Entonces** devuelve HTTP 404.

3. ~~CA-04.3 — anulado por Decisión D~~
   Este escenario fue eliminado por decisión arquitectónica. `DELETE /clientes/{id}` desactiva al cliente incondicionalmente si existe. La desactivación de cuentas asociadas ocurre de forma asincrónica mediante `ClienteDesactivadoEvent`. Ver ADR-004 Decisión D.

---

### Épica 2 — Gestión de Cuentas (Priority: P2)

*Pertenece a: `accounts-service`*

Permite registrar, consultar, actualizar y desactivar cuentas bancarias asociadas a clientes. La existencia de una cuenta activa permite el posterior registro de movimientos.

**Why this priority**: P2 porque sin cuentas no pueden registrarse movimientos ni generarse reportes. Depende directamente de la existencia de clientes (P1).

**Independent Test**: Se puede probar de forma completa creando, consultando, actualizando y eliminando una cuenta mediante `POST /cuentas`, `GET /cuentas/{id}`, `PUT /cuentas/{id}` y `DELETE /cuentas/{id}`.

#### HU-05 — Crear cuenta

**Acceptance Scenarios**:

1. **Dado** que realizo `POST /cuentas` con tipo (ahorro, corriente o digital), saldoInicial ≥ 0, estado y un clienteId que existe en la proyección local, **Cuando** el sistema procesa la solicitud, **Entonces** persiste la cuenta y devuelve HTTP 201.

2. **Dado** que realizo `POST /cuentas` con un clienteId que no existe en la proyección local, **Cuando** el sistema valida la existencia del cliente, **Entonces** devuelve HTTP 422 indicando que el cliente referenciado no existe.

3. **Dado** que realizo `POST /cuentas` con un número de cuenta ya registrado, **Cuando** el sistema valida la unicidad, **Entonces** devuelve HTTP 409.

4. **Dado** que realizo `POST /cuentas` con un saldoInicial menor a cero, **Cuando** el sistema valida los datos, **Entonces** devuelve HTTP 400.

5. **Dado** que realizo `POST /cuentas` para una cuenta de tipo corriente con saldoInicial menor a $50, **Cuando** el sistema valida la regla de negocio, **Entonces** devuelve HTTP 400 con mensaje indicando el saldo mínimo requerido.

#### HU-06 — Consultar cuenta

**Acceptance Scenarios**:

1. **Dado** que realizo `GET /cuentas/{id}` con un ID válido y existente, **Cuando** el sistema procesa la solicitud, **Entonces** devuelve HTTP 200 con los datos completos de la cuenta incluyendo saldo disponible.

2. **Dado** que realizo `GET /cuentas/{id}` con un ID que no existe, **Cuando** el sistema busca el recurso, **Entonces** devuelve HTTP 404.

#### HU-07 — Actualizar cuenta

**Acceptance Scenarios**:

1. **Dado** que realizo `PUT /cuentas/{id}` con datos válidos sobre una cuenta existente, **Cuando** el sistema procesa la solicitud, **Entonces** actualiza los campos enviados y devuelve HTTP 200 con el recurso actualizado.

2. **Dado** que realizo `PUT /cuentas/{id}` con un ID que no existe, **Cuando** el sistema intenta localizar el recurso, **Entonces** devuelve HTTP 404.

#### HU-08 — Eliminar cuenta (lógicamente)

**Acceptance Scenarios**:

1. **Dado** que realizo `DELETE /cuentas/{id}` sobre una cuenta cuyo último movimiento fue hace más de un año, **Cuando** el sistema procesa la solicitud, **Entonces** cambia el estado de la cuenta a inactiva y devuelve HTTP 200.

2. **Dado** que realizo `DELETE /cuentas/{id}` con un ID inexistente, **Cuando** el sistema busca el recurso, **Entonces** devuelve HTTP 404.

3. **Dado** que realizo `DELETE /cuentas/{id}` sobre una cuenta con al menos un movimiento registrado en el último año, **Cuando** el sistema valida la precondición, **Entonces** devuelve HTTP 409 con mensaje indicando que la cuenta no puede ser eliminada por tener actividad reciente.

---

### Épica 3 — Gestión de Movimientos (Priority: P2)

*Pertenece a: `accounts-service`*

Permite registrar depósitos, retiros, ajustes y reversiones sobre cuentas activas. Cada movimiento actualiza el saldo disponible de la cuenta y genera un registro de trazabilidad inmutable.

**Why this priority**: P2 porque el registro de movimientos es la operación transaccional central del sistema. Depende de cuentas activas.

**Independent Test**: Se puede probar de forma completa registrando un depósito y un retiro sobre una cuenta con saldo, verificando actualización de saldo y respuestas HTTP.

#### HU-09 — Registrar movimiento (depósito o retiro)

**Acceptance Scenarios**:

1. **Dado** que realizo `POST /movimientos` con un valor positivo sobre una cuenta activa, **Cuando** el sistema procesa la transacción, **Entonces** registra el movimiento, incrementa el saldo disponible de la cuenta y devuelve HTTP 201 con el movimiento creado.

2. **Dado** que realizo `POST /movimientos` con un valor negativo sobre una cuenta activa que tiene saldo suficiente y cuyo cliente no ha superado el límite de retiro diario de $500, **Cuando** el sistema procesa la transacción, **Entonces** registra el movimiento, decrementa el saldo disponible y devuelve HTTP 201.

3. **Dado** que realizo `POST /movimientos` con un valor negativo que supera el saldo disponible de la cuenta, **Cuando** el sistema valida la operación, **Entonces** devuelve HTTP 422 con el mensaje **"Saldo no disponible"** sin modificar el saldo.

4. **Dado** que realizo `POST /movimientos` sobre una cuenta que no existe o está inactiva, **Cuando** el sistema valida la cuenta, **Entonces** devuelve HTTP 422 con mensaje descriptivo.

5. **Dado** que realizo `POST /movimientos` con un valor de cero, **Cuando** el sistema valida los datos, **Entonces** devuelve HTTP 400 con el mensaje **"El valor del movimiento no puede ser cero"**.

#### HU-09.1 — Límite de retiro diario

**Acceptance Scenarios**:

1. **Dado** que un cliente ha acumulado $450 en retiros durante el día calendario actual, **Cuando** intenta realizar un nuevo retiro de $51 o más (total acumulado superaría $500), **Entonces** el sistema rechaza la transacción y devuelve HTTP 422 con el mensaje **"Límite de retiro diario excedido"**.

2. **Dado** que un cliente ha acumulado $400 en retiros y realiza un depósito de cualquier monto, **Cuando** el sistema procesa el depósito, **Entonces** registra el movimiento con HTTP 201 y el límite de retiro disponible para el día ($100 restantes) no se modifica.

3. **Dado** que un cliente ha acumulado $500 exactos en retiros durante el día, **Cuando** intenta realizar un retiro de $1, **Entonces** el sistema rechaza la transacción y devuelve HTTP 422 con el mensaje **"Límite de retiro diario excedido"**.

#### HU-10 — Consultar movimiento

**Acceptance Scenarios**:

1. **Dado** que realizo `GET /movimientos/{id}` con un ID válido y existente, **Cuando** el sistema procesa la solicitud, **Entonces** devuelve HTTP 200 con los datos del movimiento.

2. **Dado** que realizo `GET /movimientos/{id}` con un ID que no existe, **Cuando** el sistema busca el recurso, **Entonces** devuelve HTTP 404.

#### HU-11.R — Registrar movimiento de ajuste

**Acceptance Scenarios**:

1. **Dado** que realizo `POST /ajustes` con un movimientoId original existente, un valor de ajuste y una justificación, **Cuando** el sistema procesa la solicitud, **Entonces** crea un nuevo movimiento de tipo ajuste, actualiza el saldo disponible de la cuenta afectada y devuelve HTTP 201.

2. **Dado** que realizo `POST /ajustes` con un movimientoId que no existe, **Cuando** el sistema busca el movimiento original, **Entonces** devuelve HTTP 404.

#### HU-12.R — Registrar movimiento de reversión

**Acceptance Scenarios**:

1. **Dado** que realizo `POST /reversiones` con el movimientoId de una transacción existente, **Cuando** el sistema procesa la solicitud, **Entonces** crea un nuevo movimiento de tipo reversión con el valor opuesto al original, anula el efecto en el saldo disponible y devuelve HTTP 201.

2. **Dado** que realizo `POST /reversiones` con un movimientoId que no existe, **Cuando** el sistema busca la transacción original, **Entonces** devuelve HTTP 404.

---

### Épica 4 — Reportes (Priority: P3)

*Pertenece a: `accounts-service`*

Permite consultar el estado de cuenta con historial de movimientos filtrado por rango de fechas y cliente.

**Why this priority**: P3 porque es una capacidad de consulta que depende de datos preexistentes (cuentas y movimientos). No bloquea ningún flujo transaccional.

**Independent Test**: Se puede probar de forma completa consultando el reporte de un cliente con cuentas y movimientos preexistentes mediante `GET /reportes`.

#### HU-13 — Generar reporte de estado de cuenta

**Acceptance Scenarios**:

1. **Dado** que realizo `GET /reportes?clienteId={id}&fechaInicio={fecha}&fechaFin={fecha}` con parámetros válidos para un cliente con cuentas y movimientos en el rango solicitado, **Cuando** el sistema procesa la solicitud, **Entonces** devuelve HTTP 200 con un listado que incluye por cada movimiento: fecha, cliente, número de cuenta, tipo de cuenta, saldo inicial, estado, movimiento y saldo disponible.

2. **Dado** que realizo `GET /reportes?clienteId={id}&fechaInicio={fecha}&fechaFin={fecha}` con un clienteId que no existe, **Cuando** el sistema busca el cliente, **Entonces** devuelve HTTP 404.

3. **Dado** que realizo `GET /reportes` sin los parámetros requeridos (clienteId, fechaInicio, fechaFin), **Cuando** el sistema valida la solicitud, **Entonces** devuelve HTTP 400 indicando los parámetros faltantes.

4. **Dado** que realizo `GET /reportes?clienteId={id}&fechaInicio={fecha}&fechaFin={fecha}` con un cliente que no tiene movimientos en el rango de fechas indicado, **Cuando** el sistema procesa la consulta, **Entonces** devuelve HTTP 200 con lista vacía.

---

### Épica 5 — Comunicación entre microservicios (Priority: P2)

*Canal asincrónico entre `customers-service` y `accounts-service`*

Garantiza que accounts-service mantenga actualizada su proyección local de clientes mediante la recepción y procesamiento de eventos de dominio publicados por customers-service sobre RabbitMQ.

**Why this priority**: P2 porque sin la sincronización de la proyección local, accounts-service no puede validar la existencia de clientes al crear cuentas o procesar movimientos.

**Independent Test**: Se puede probar verificando que, tras crear un cliente en customers-service, posteriormente es posible crear una cuenta para ese cliente en accounts-service; y que tras eliminar el cliente lógicamente, ya no es posible crear nuevas cuentas para él.

#### HU-14 — Sincronización de proyección local de clientes

**Acceptance Scenarios**:

1. **Dado** que customers-service recibe y persiste exitosamente un nuevo cliente, **Cuando** el sistema publica el evento ClienteCreatedEvent al exchange `cliente.events` con routing key `cliente.created`, **Entonces** accounts-service consume el mensaje, persiste el cliente en su tabla de proyección local con estado activo, y la operación de creación de cuenta para ese clienteId pasa a estar disponible.

2. **Dado** que customers-service desactiva lógicamente un cliente, **Cuando** el sistema publica el evento ClienteDeletedEvent al exchange `cliente.events` con routing key `cliente.deleted`, **Entonces** accounts-service consume el mensaje y actualiza el estado del cliente en su proyección local a inactivo.

3. **Dado** que accounts-service no puede procesar un evento por un error interno, **Cuando** el número de reintentos es superado, **Entonces** el mensaje es redirigido a la Dead Letter Queue sin bloquear el procesamiento de otros mensajes.

4. **Dado** que accounts-service recibe el evento ClienteDeletedEvent para un cliente con proyección existente en estado activo, **Cuando** intenta crear una nueva cuenta para ese clienteId después del procesamiento del evento, **Entonces** devuelve HTTP 422 indicando que el cliente referenciado no existe o está inactivo.

---

### Épica 6 — Calidad y despliegue (Priority: P1)

*Aplica a ambos microservicios*

Establece los estándares de calidad de código (TDD, cobertura mínima) y el mecanismo de despliegue completo en contenedores.

**Why this priority**: P1 porque la estrategia de pruebas y el despliegue son restricciones transversales que gobiernan todo el desarrollo desde el primer ciclo.

**Independent Test**: Se puede probar ejecutando el suite de pruebas unitarias de dominio de cualquier microservicio sin dependencias externas, y levantando el sistema completo con `docker compose up`.

#### HU-15 — Pruebas unitarias de dominio

**Acceptance Scenarios**:

1. **Dado** que existe la lógica de negocio del dominio (validaciones de Cliente, Chain of Validators de movimientos), **Cuando** se ejecutan las pruebas unitarias, **Entonces** se ejecutan sin levantar el contexto de Spring, sin acceso a base de datos ni red, en menos de 1 segundo por clase, y todas pasan en verde.

2. **Dado** que una regla de dominio existe sin prueba unitaria que la cubra, **Cuando** se ejecuta el reporte de cobertura, **Entonces** la cobertura de la capa de dominio es menor al 100%, lo cual constituye un criterio de falla del build.

#### HU-16 — Pruebas de integración con Testcontainers

**Acceptance Scenarios**:

1. **Dado** que existen pruebas de integración configuradas con Testcontainers, **Cuando** se ejecutan, **Entonces** levantan instancias reales de PostgreSQL y RabbitMQ en Docker, ejecutan el flujo completo Controller → Service → Repository → BD real y validan tanto el código HTTP como la persistencia resultante.

2. **Dado** que se ejecuta la prueba de integración para el escenario de retiro con saldo insuficiente, **Cuando** el sistema procesa el retiro, **Entonces** la prueba verifica HTTP 422, el mensaje "Saldo no disponible" y que el saldo de la cuenta no cambió.

#### HU-17 — Despliegue con Docker Compose

**Acceptance Scenarios**:

1. **Dado** que se ejecuta `docker compose up` desde la raíz del repositorio en un entorno limpio sin dependencias previas, **Cuando** el proceso completa el arranque, **Entonces** los cinco contenedores (db_customers, db_accounts, rabbitmq, customers-service, accounts-service) están corriendo y saludables.

2. **Dado** que los servicios de base de datos y broker aún no están listos, **Cuando** los microservicios intentan arrancar, **Entonces** no inician hasta que sus dependencias superen los health checks definidos.

3. **Dado** que el sistema es detenido con `docker compose down` y reiniciado con `docker compose up`, **Cuando** el stack está completamente levantado, **Entonces** los datos persistidos (clientes, cuentas, movimientos) siguen disponibles gracias a los named volumes.

---

### Edge Cases

- ¿Qué ocurre si se intenta crear un movimiento en una cuenta cuyo cliente fue desactivado?
- ¿Cómo se calcula el límite diario cuando un cliente tiene múltiples cuentas? (Se acumula por cliente, sumando retiros de todas sus cuentas en el día).
- ¿Qué valor toma el saldo disponible tras una reversión si ya hubo movimientos posteriores al original? (La reversión crea un movimiento con valor opuesto; el saldo se recalcula de forma incremental).
- ¿Qué ocurre si el evento de cliente llega duplicado al broker? (La proyección local debe ser idempotente: un upsert no genera duplicados).
- ¿Cuenta nueva sin movimientos puede eliminarse? (Sí, porque no tiene actividad en el último año).

---

## Requirements

### Functional Requirements

- **FR-001**: El sistema DEBE permitir crear, consultar, actualizar y desactivar clientes con las validaciones descritas en HU-01 a HU-04.
- **FR-002**: El sistema DEBE permitir crear, consultar, actualizar y desactivar cuentas con las validaciones descritas en HU-05 a HU-08.
- **FR-003**: El sistema DEBE registrar movimientos de depósito, retiro, ajuste y reversión aplicando todas las reglas de validación descritas en HU-09 a HU-12.R.
- **FR-004**: El sistema DEBE generar reportes de estado de cuenta filtrados por cliente y rango de fechas conforme HU-13.
- **FR-005**: customers-service DEBE publicar eventos `ClienteCreatedEvent` y `ClienteDeletedEvent` al broker tras cada persistencia exitosa.
- **FR-006**: accounts-service DEBE consumir los eventos del broker y mantener actualizada su proyección local de clientes de forma autónoma.
- **FR-007**: El sistema DEBE rechazar todo retiro que supere el saldo disponible con el mensaje exacto **"Saldo no disponible"**.
- **FR-008**: El sistema DEBE acumular los retiros de un cliente durante el día y rechazar cualquier retiro que supere $500 acumulado con el mensaje exacto **"Límite de retiro diario excedido"**.
- **FR-009**: La eliminación de clientes y cuentas DEBE ser lógica (cambio de estado), nunca física.
- **FR-010**: El sistema DEBE impedir la eliminación de un cliente con cuentas activas con el mensaje exacto **"El cliente posee cuentas activas que deben ser desactivadas primero"**.
- **FR-011**: El sistema DEBE impedir la eliminación de una cuenta con movimientos registrados en el último año.
- **FR-012**: El sistema DEBE rechazar movimientos con valor igual a cero con el mensaje exacto **"El valor del movimiento no puede ser cero"**.
- **FR-013**: Toda respuesta de error DEBE incluir timestamp, status, error, message y path; sin exponer stack traces ni detalles de infraestructura.
- **FR-014**: El sistema completo DEBE levantarse con un único comando `docker compose up` desde la raíz del repositorio.

### Reglas de negocio no negociables

| ID | Regla | Código HTTP | Mensaje exacto |
|---|---|---|---|
| RN-01 | Retiro que supera el saldo disponible | 422 | "Saldo no disponible" |
| RN-02 | Retiros acumulados del día superan $500 | 422 | "Límite de retiro diario excedido" |
| RN-03 | Movimiento con valor igual a cero | 400 | "El valor del movimiento no puede ser cero" |
| RN-04 | ~~Eliminado por Decisión D~~ | Ver ADR-004 | Comportamiento reemplazado por desactivación asincrónica vía evento | — |
| RN-05 | Eliminar cuenta con actividad en el último año | 409 | Mensaje descriptivo de actividad reciente |
| RN-06 | Crear cuenta para cliente inexistente o inactivo | 422 | Mensaje descriptivo |
| RN-07 | Edad del cliente menor a 18 | 400 | Mensaje descriptivo |
| RN-08 | Saldo inicial negativo en cualquier cuenta | 400 | Mensaje descriptivo |
| RN-09 | Saldo inicial en cuenta corriente menor a $50 | 400 | Mensaje indicando saldo mínimo requerido |
| RN-10 | Identificación duplicada al crear o actualizar cliente | 409 | Mensaje descriptivo |

### Contratos de API

#### customers-service → `/clientes`

| Verbo | Ruta | Descripción | Código éxito | Códigos error |
|---|---|---|---|---|
| POST | /clientes | Crear cliente | 201 | 400, 409 |
| GET | /clientes/{id} | Consultar cliente | 200 | 404 |
| PUT | /clientes/{id} | Actualizar cliente | 200 | 400, 404, 409 |
| DELETE | /clientes/{id} | Desactivar cliente | 200 | 404, 409 |

#### accounts-service → `/cuentas`

| Verbo | Ruta | Descripción | Código éxito | Códigos error |
|---|---|---|---|---|
| POST | /cuentas | Crear cuenta | 201 | 400, 409, 422 |
| GET | /cuentas/{id} | Consultar cuenta | 200 | 404 |
| PUT | /cuentas/{id} | Actualizar cuenta | 200 | 400, 404 |
| DELETE | /cuentas/{id} | Desactivar cuenta | 200 | 404, 409 |

#### accounts-service → `/movimientos`

| Verbo | Ruta | Descripción | Código éxito | Códigos error |
|---|---|---|---|---|
| POST | /movimientos | Registrar depósito o retiro | 201 | 400, 422 |
| GET | /movimientos/{id} | Consultar movimiento | 200 | 404 |
| POST | /ajustes | Registrar ajuste | 201 | 400, 404 |
| POST | /reversiones | Registrar reversión | 201 | 404 |

#### accounts-service → `/reportes`

| Verbo | Ruta | Descripción | Código éxito | Códigos error |
|---|---|---|---|---|
| GET | /reportes | Reporte de estado de cuenta | 200 | 400, 404 |

**Parámetros requeridos en `/reportes`**: `clienteId`, `fechaInicio` (ISO-8601), `fechaFin` (ISO-8601).

**Campos del reporte por fila**: fecha, cliente, número de cuenta, tipo de cuenta, saldo inicial, estado, movimiento (valor del movimiento), saldo disponible.

#### Formato de respuesta de error (todos los endpoints)

```json
{
  "timestamp": "2026-04-14T10:30:00Z",
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "Saldo no disponible",
  "path": "/movimientos"
}
```

### Key Entities

- **Persona**: nombre, género (MASCULINO | FEMENINO), edad, identificación (cédula ecuatoriana), dirección, teléfono (con prefijo de país). Clase base en customers-service.
- **Cliente**: extiende Persona. Atributos adicionales: contraseña (≥ 8 caracteres, alfanumérica, ≥ 1 mayúscula), estado (activo | inactivo). Pertenece al bounded context de Identidad.
- **ClienteProyeccion**: copia local en accounts-service. Atributos: clienteId, nombre, estado. Sincronizada por eventos; nunca modificada directamente por la API.
- **Cuenta**: número de cuenta (único), tipo (ahorro | corriente | digital), saldo inicial, saldo disponible, estado (activa | inactiva), referencia a clienteId. Pertenece al bounded context Financiero.
- **Movimiento**: fecha, tipo (depósito | retiro | ajuste | reversión), valor, saldo resultante, referencia a cuenta. Registro inmutable de auditoría.
- **ClienteCreatedEvent**: evento de dominio publicado tras la creación exitosa de un cliente. Contiene clienteId y nombre.
- **ClienteDeletedEvent**: evento de dominio publicado tras la desactivación lógica de un cliente. Contiene clienteId.

---

## Success Criteria

### Measurable Outcomes

- **SC-001**: El 100% de los criterios de aceptación de las 17 HUs (HU-01 a HU-17) son verificables mediante pruebas automatizadas en verde.
- **SC-002**: La cobertura de pruebas de la capa de dominio es del 100% en ambos microservicios.
- **SC-003**: La cobertura de pruebas de los casos de uso (services) es del 80% o más en ambos microservicios.
- **SC-004**: Todos los controladores quedan cubiertos por al menos una prueba de integración con contenedores reales.
- **SC-005**: El sistema completo levanta correctamente desde un repositorio limpio con el único comando `docker compose up`, sin ninguna dependencia previa instalada en el host más allá de Docker.
- **SC-006**: Todos los endpoints de la API responden de acuerdo a los contratos definidos (rutas, verbos, códigos HTTP, formato de error) verificables con Postman.
- **SC-007**: Los mensajes de error exactos para RN-01, RN-02 y RN-03 se retornan con el texto literal especificado, sin variaciones. RN-04 fue eliminado por Decisión D (ver ADR-004).
- **SC-008**: La latencia de respuesta de cualquier endpoint es aceptable bajo carga normal de prueba funcional (operación completada sin timeouts en el entorno local).
- **SC-009**: Ningún archivo de código fuente (Java, YAML, properties, Dockerfile, scripts) contiene comentarios de ningún tipo.
- **SC-010**: La comunicación entre microservicios ocurre exclusivamente mediante RabbitMQ; no existen llamadas REST directas entre los dos microservicios.

---

## Assumptions

- El sistema está orientado a un entorno de evaluación técnica senior; no se requiere autenticación de usuarios ni autorización por roles.
- El cálculo del límite de retiro diario de $500 se acumula por clienteId, sumando los valores absolutos de todos los retiros de todas sus cuentas en el día calendario (00:00:00 a 23:59:59 en la zona horaria del servidor).
- La identificación de cédula ecuatoriana válida cumple el algoritmo estándar de validación de módulo 10.
- Los tipos de cuenta válidos son exclusivamente: ahorro, corriente y digital.
- El género válido es exclusivamente: MASCULINO o FEMENINO.
- El reporte de estado de cuenta se obtiene vía query con parámetros; no incluye paginación en esta versión.
- La consistencia entre microservicios es eventual; existe una ventana de tiempo (milisegundos) entre la creación de un cliente y su disponibilidad en la proyección local de accounts-service. Esto es aceptado como decisión de negocio.
- La reversión de un movimiento crea un nuevo movimiento con valor opuesto; no borra ni modifica el movimiento original, preservando la trazabilidad.
- Los datos de prueba necesarios para las pruebas de integración se insertan mediante el mecanismo de inicialización de la base de datos en el contenedor (docker-entrypoint-initdb.d).
- No se requiere un esquema de versionado de API (sin `/v1/` en las rutas) en esta versión.
