## Specification Analysis Report — TASK-06: `CrearClienteUseCase`

### Dimensión 1 — Dirección de Dependencias

✅ **PASS**

| Import | Paquete origen | Permitido |
|--------|---------------|-----------|
| `ClienteResponse` | `application/dto/` | ✅ |
| `CrearClienteRequest` | `application/dto/` | ✅ |
| `ClienteCreatedEvent` | `domain/event/` | ✅ |
| `IdentificacionDuplicadaException` | `domain/exception/` | ✅ |
| `Cliente`, `EstadoCliente` | `domain/model/` | ✅ |
| `ClienteRepository`, `EventPublisher` | `domain/port/` | ✅ |

Ningún import de `infrastructure/`, `jakarta.*` ni `org.springframework.*`. Dirección de dependencias conforme a hexagonal architecture y constitution.md principio de que "el dominio nunca importa interfaces de Spring Data directamente".

---

### Dimensión 2 — Orden de Operaciones en `ejecutar()`

✅ **PASS**

```
1. existsByIdentificacion() → lanzar IdentificacionDuplicadaException   ✅
2. Cliente.create(...)                                                   ✅
3. clienteRepository.save(cliente) → clientePersistido                  ✅
4. new ClienteCreatedEvent(clientePersistido.getId(), ...)               ✅  ← DESPUÉS de save()
5. eventPublisher.publish(evento)                                        ✅
6. ClienteResponse.from(clientePersistido)                               ✅
```

El evento se construye con `clientePersistido.getId()` (resultado de `save()`). Hallazgo G1 resuelto: no existe path donde el evento lleve `id=null`.

---

### Dimensión 3 — Cobertura de Criterios de Aceptación HU-01

| Criterio | Test | Estado |
|----------|------|--------|
| CA-01.1 registro exitoso (persistencia + respuesta) | `clienteWithValidDataShouldPersistAndReturnResponse()` — verifica `save()` invocado, `clienteId=1L`, `nombre` correcto | ✅ |
| CA-01.2 identificación duplicada → excepción | `clienteWithDuplicateIdentificacionShouldThrowException()` — verifica excepción, `save()` nunca llamado, `publish()` nunca llamado | ✅ |
| CA-01.3 campos inválidos | Cubierto por `Cliente.create()` en T02 — no requiere test en use case | ✅ |
| Evento publicado con ID real ≠ null | `clienteCreatedEventShouldContainRealIdAfterPersistence()` — `id=42L` via `ArgumentCaptor` | ✅ |
| Respuesta no expone `contrasena` | `clienteResponseShouldNotContainContrasena()` — verifica campos por reflexión | ✅ |

⚠️ **ADVERTENCIA — A1**: tasks.md línea 341 especifica `crearClienteConEdadInvalidaDebePropagar EdadInvalidaException()` como test requerido en TASK-06. Ese test no está presente. El comportamiento está cubierto en T02 (`ClienteCreationTest`, `ClienteValidationTest`) y la instrucción de implementación lo excluye explícitamente a nivel de use case, pero queda inconsistencia documental entre tasks.md y la implementación final. Impacto: ninguno en ejecución; riesgo en trazabilidad.

---

### Dimensión 4 — Contrato del Evento (R-05)

✅ **PASS**

| Regla R-05 | Verificación |
|-----------|-------------|
| Evento publicado DESPUÉS de `save()` exitoso | Línea secuencial: `clientePersistido = save(cliente)` → `publish(new ClienteCreatedEvent(clientePersistido.getId(), ...))` ✅ |
| Si `save()` lanza excepción, evento no se publica | Sin `try-catch` en `ejecutar()` — excepción propaga antes de llegar a `publish()` ✅ |
| `clienteId` viene del retorno de `save()`, no del objeto pre-persistencia | `clientePersistido.getId()` donde `clientePersistido` es el valor de retorno de `clienteRepository.save()` ✅ |

Nota: R-05 menciona `@TransactionalEventListener(phase = AFTER_COMMIT)` para la publicación real a RabbitMQ. Esto es responsabilidad del adaptador de infraestructura (TASK-11), no del use case. El puerto `EventPublisher` actúa como frontera; el comportamiento transaccional correcto se delega a la implementación. Arquitectura conforme.

---

### Dimensión 5 — Mockito y Aislamiento del Test

✅ **PASS**

| Verificación | Resultado |
|-------------|-----------|
| `clienteRepository = mock(ClienteRepository.class)` | ✅ `@BeforeEach` |
| `eventPublisher = mock(EventPublisher.class)` | ✅ `@BeforeEach` |
| `@SpringBootTest` ausente | ✅ `NONE` |
| `@ExtendWith`, `@Autowired` ausentes | ✅ `NONE` |
| `ArgumentCaptor<DomainEvent>` usado | ✅ en `clienteCreatedEventShouldContainRealIdAfterPersistence` |
| Mock de `save()` retorna `Cliente` con `id=42L` (no null) | ✅ `Cliente.reconstitute(42L, ...)` |

---

### Dimensión 6 — Manejo de Excepciones

✅ **PASS**

| Regla | Resultado |
|-------|-----------|
| `IdentificacionDuplicadaException` lanzada antes de `Cliente.create()` | ✅ — guard clause al inicio de `ejecutar()` |
| Sin `try-catch` dentro de `ejecutar()` | ✅ — `grep try\|catch` devuelve `NONE` |
| Excepciones de dominio propagan sin wrapping | ✅ — no hay traducción de excepciones en el use case |

---

## Métricas

| Métrica | Valor |
|---------|-------|
| Criterios de aceptación cubiertos | 5 / 5 |
| Tests en verde | 40 / 40 |
| Importaciones inválidas | 0 |
| `ejecutar()` líneas | 11 (límite: 15) |
| Bloqueantes | **0** |
| Advertencias | **1** |

---

## Resumen

**0 bloqueantes — 1 advertencia.**

✅ **TASK-06 está listo para commit.**

**Advertencia A1** (no bloquea): reconciliar tasks.md eliminando la entrada `crearClienteConEdadInvalidaDebePropagar EdadInvalidaException()` del listado de TASK-06 o añadiendo una nota `→ cubierto en T02` para mantener trazabilidad documental entre la especificación y la implementación real.

---

¿Deseas que aplique la corrección documental de A1 en tasks.md?

User: Follow instructions in [speckit.implement.prompt.md](file:///home/alexis-yepez/Desktop/challenges/backend/banking-microservices/.github/prompts/speckit.implement.prompt.md).
Implementar TASK-07 — ConsultarClienteUseCase (HU-02).

Contexto:

- specs/001-sistema-bancario-completo/tasks.md (TASK-07)
- specs/001-sistema-bancario-completo/spec.md (HU-02)
- constitution.md (v1.0.2)

PASO 1 — RED: crear el test primero.

Crear:
customers-service/src/test/java/com/banking/customers/
unit/usecase/ConsultarClienteUseCaseTest.java

Con los siguientes métodos:
  existingClienteShouldReturnClienteResponse()
  nonExistingClienteShouldThrowClienteNotFoundException()

Setup con Mockito:
  clienteRepository = mock(ClienteRepository.class)
  useCase = new ConsultarClienteUseCase(clienteRepository)

PASO 2 — GREEN: crear el use case.

Crear:
customers-service/src/main/java/com/banking/customers/
application/usecase/ConsultarClienteUseCase.java

- Constructor con ClienteRepository
- Método: public ClienteResponse ejecutar(Long id)
- Flujo:
  1. clienteRepository.findById(id)
  2. Si empty → lanzar ClienteNotFoundException(id)
  3. Retornar ClienteResponse.from(cliente)
- Sin @Service ni anotaciones de Spring
- Sin EventPublisher (consulta no publica eventos)

RESTRICCIONES ABSOLUTAS:

- Cero comentarios
- Sin imports de jakarta ni springframework
- Test sin anotaciones de Spring

CRITERIO DE COMPLETITUD:

- ./gradlew :customers-service:test → 42 tests — 0 failures
- find application/usecase -name "*.java" |
  xargs grep -l "springframework\|jakarta" → 0 resultados
- find src -name "*.java" | xargs grep -l "//" → 0 resultados

GitHub Copilot: Ran terminal command:  grep -n "TASK-07\|ClienteNotFoundException\|findById" /home/alexis-yepez/Desktop/challenges/backend/banking-microservices/specs/001-sistema-bancario-completo/tasks.md | head -20 && echo "---" && cat /home/alexis-yepez/Desktop/challenges/backend/banking-microservices/customers-service/src/main/java/com/banking/customers/domain/exception/ClienteNotFoundException.java

**PASO 1 — RED**

Created [](file:///home/alexis-yepez/Desktop/challenges/backend/banking-microservices/customers-service/src/test/java/com/banking/customers/unit/usecase/ConsultarClienteUseCaseTest.java)

Ran terminal command:  ./gradlew :customers-service:compileTestJava --no-daemon 2>&1 | grep -E "error:|ConsultarClienteUseCase|FAILED|BUILD" | head -10

Red confirmado. **PASO 2 — GREEN**  

Created [](file:///home/alexis-yepez/Desktop/challenges/backend/banking-microservices/customers-service/src/main/java/com/banking/customers/application/usecase/ConsultarClienteUseCase.java)

Ran terminal command:  ./gradlew :customers-service:test --no-daemon 2>&1 | grep -E "BUILD|FAILED|tests" | tail -10

Ran terminal command:  find customers-service/build/test-results -name "*.xml" | xargs grep -h 'tests=' | sed 's/.*tests="\([0-9]*\)".*/\1/' | awk '{s+=$1} END {print "Total:", s}' && find customers-service/build/test-results -name "*.xml" | xargs grep -h 'failures=' | sed 's/.*failures="\([0-9]*\)".*/\1/' | awk '{s+=$1} END {print "Failures:", s}' && echo "=== Spring/Jakarta in usecase ===" && find customers-service/src/main/java/com/banking/customers/application/usecase -name "*.java" | xargs grep -l "springframework\|jakarta" 2>/dev/null || echo "NONE (OK)" && echo "=== Comments ===" && find customers-service/src -name "*.java" | xargs grep -l "//" 2>/dev/null || echo "NONE (OK)"

42 tests — 0 fallos. Marco TASK-07 en tasks.md:

Read [](file:///home/alexis-yepez/Desktop/challenges/backend/banking-microservices/specs/001-sistema-bancario-completo/tasks.md#53-53), lines 53 to 60

Read [](file:///home/alexis-yepez/Desktop/challenges/backend/banking-microservices/specs/001-sistema-bancario-completo/tasks.md#359-359), lines 359 to 385

Replacing 4 lines with 4 lines in [](file:///home/alexis-yepez/Desktop/challenges/backend/banking-microservices/specs/001-sistema-bancario-completo/tasks.md)

```

```

TASK-07 completada.

**Archivos creados:**

- ConsultarClienteUseCaseTest.java — 2 tests
- ConsultarClienteUseCase.java — `ejecutar()` en 3 líneas via `Optional.map().orElseThrow()`

| Criterio | Estado |
|---|---|
| Tests totales | 42 — 0 fallos |
| Spring/Jakarta en `application/usecase/` | 0 archivos |
| Comentarios `//` | 0 archivos |

Made changes.

User: Follow instructions in [speckit.implement.prompt.md](file:///home/alexis-yepez/Desktop/challenges/backend/banking-microservices/.github/prompts/speckit.implement.prompt.md).
Implementar TASK-08 — ActualizarClienteUseCase (HU-03).

Contexto:

- specs/001-sistema-bancario-completo/tasks.md (TASK-08)
- specs/001-sistema-bancario-completo/spec.md (HU-03, CA-03.1 a CA-03.3)
- specs/001-sistema-bancario-completo/contracts/api-customers.md
  (PUT /clientes/{id})
- constitution.md (v1.0.2)

PASO 1 — RED: crear el test primero.

Crear:
customers-service/src/test/java/com/banking/customers/
unit/usecase/ActualizarClienteUseCaseTest.java

Con los siguientes métodos:
  existingClienteWithValidDataShouldUpdateAndReturnResponse()
  nonExistingClienteShouldThrowClienteNotFoundException()
  updateWithDuplicateIdentificacionShouldThrowException()
  updateShouldOnlyModifyProvidedFields()
  updateShouldRevalidateEdadAndIdentificacion()

Setup con Mockito:
  clienteRepository = mock(ClienteRepository.class)
  useCase = new ActualizarClienteUseCase(clienteRepository)

Para updateShouldOnlyModifyProvidedFields():

- ActualizarClienteRequest con solo nombre y direccion
    provistos (resto null)
- Verificar que solo esos campos cambian en el cliente
- Los campos null del request no sobreescriben los
    valores existentes del cliente

Para updateWithDuplicateIdentificacionShouldThrowException():

- existsByIdentificacionAndIdNot() retorna true
- Verificar que save() nunca se llama

PASO 2 — GREEN: crear el use case.

Crear:
customers-service/src/main/java/com/banking/customers/
application/usecase/ActualizarClienteUseCase.java

- Constructor con ClienteRepository
- Método: public ClienteResponse ejecutar(Long id,
  ActualizarClienteRequest request)
- Flujo:
  1. clienteRepository.findById(id)
     → ClienteNotFoundException si no existe
  2. Si request.getIdentificacion() != null:
     existsByIdentificacionAndIdNot(identificacion, id)
     → IdentificacionDuplicadaException si duplicada
  3. Construir los valores finales de cada campo:
     usar el valor del request si no es null,
     mantener el valor actual del cliente si es null
  4. cliente.actualizarDatos(nombre, genero, edad,
     identificacion, direccion, telefono)
  5. clienteRepository.save(cliente)
  6. Retornar ClienteResponse.from(clienteActualizado)

⚠️ actualizarDatos() en Cliente requiere todos los campos.
   El use case es responsable de resolver los valores
   finales antes de llamar al método de dominio.
   Si el campo en el request es null, usar el valor
   actual del cliente como fallback.

⚠️ ActualizarClienteUseCase NO publica eventos.
   La actualización de datos de cliente no genera
   ningún evento de dominio en esta versión.

- Sin @Service ni anotaciones de Spring

RESTRICCIONES ABSOLUTAS:

- Cero comentarios
- Sin imports de jakarta ni springframework
- Test sin anotaciones de Spring
- ejecutar() no supera 15 líneas

CRITERIO DE COMPLETITUD:

- ./gradlew :customers-service:test → 47 tests — 0 failures
- updateShouldOnlyModifyProvidedFields() pasa en verde
- find application/usecase -name "*.java" |
  xargs grep -l "springframework\|jakarta" → 0 resultados
- find src -name "*.java" |
  xargs grep -l "//" → 0 resultados

GitHub Copilot: Ran terminal command:  cat /home/alexis-yepez/Desktop/challenges/backend/banking-microservices/customers-service/src/main/java/com/banking/customers/application/dto/ActualizarClienteRequest.java && echo "---" && grep -n "actualizarDatos" /home/alexis-yepez/Desktop/challenges/backend/banking-microservices/customers-service/src/main/java/com/banking/customers/domain/model/Cliente.java

Now I have everything needed. **PASO 1 — RED**

Created [](file:///home/alexis-yepez/Desktop/challenges/backend/banking-microservices/customers-service/src/test/java/com/banking/customers/unit/usecase/ActualizarClienteUseCaseTest.java)

Ran terminal command:  ./gradlew :customers-service:compileTestJava --no-daemon 2>&1 | grep -E "error:|ActualizarClienteUseCase|FAILED|BUILD" | head -6

Red confirmado. **PASO 2 — GREEN**  

Created [](file:///home/alexis-yepez/Desktop/challenges/backend/banking-microservices/customers-service/src/main/java/com/banking/customers/application/usecase/ActualizarClienteUseCase.java)

Ran terminal command:  ./gradlew :customers-service:test --no-daemon 2>&1 | grep -E "BUILD|FAILED" | tail -5

Ran terminal command:  find customers-service/build/test-results -name "*.xml" | xargs grep -h 'tests=' | sed 's/.*tests="\([0-9]*\)".*/\1/' | awk '{s+=$1} END {print "Total:", s}' && find customers-service/build/test-results -name "*.xml" | xargs grep -h 'failures=' | sed 's/.*failures="\([0-9]*\)".*/\1/' | awk '{s+=$1} END {print "Failures:", s}' && echo "=== Spring/Jakarta ===" && find customers-service/src/main/java/com/banking/customers/application/usecase -name "*.java" | xargs grep -l "springframework\|jakarta" 2>/dev/null || echo "NONE (OK)" && echo "=== Comments ===" && find customers-service/src -name "*.java" | xargs grep -l "//" 2>/dev/null || echo "NONE (OK)" && echo "=== ejecutar() lines ===" && awk '/public ClienteResponse ejecutar\(Long id/{found=1; count=0} found{count++} /^    \}$/ && found && count>1{print count-1 " lines"; found=0}' customers-service/src/main/java/com/banking/customers/application/usecase/ActualizarClienteUseCase.java

47 tests — 0 fallos — `ejecutar()` en 14 líneas. Marco TASK-08 en tasks.md:  

Read [](file:///home/alexis-yepez/Desktop/challenges/backend/banking-microservices/specs/001-sistema-bancario-completo/tasks.md#380-380), lines 380 to 410

Replacing 4 lines with 4 lines in [](file:///home/alexis-yepez/Desktop/challenges/backend/banking-microservices/specs/001-sistema-bancario-completo/tasks.md)

```

```

TASK-08 completada.

**Archivos creados:**

- ActualizarClienteUseCaseTest.java — 5 tests
- ActualizarClienteUseCase.java — `ejecutar()` en 14 líneas

| Criterio | Estado |
|---|---|
| Tests totales | 47 — 0 fallos |
| `updateShouldOnlyModifyProvidedFields()` | ✅ |
| Spring/Jakarta en `application/usecase/` | 0 archivos |
| Comentarios `//` | 0 archivos |
| `ejecutar()` ≤ 15 líneas | 14 líneas |

Made changes.

User: Follow instructions in [speckit.implement.prompt.md](file:///home/alexis-yepez/Desktop/challenges/backend/banking-microservices/.github/prompts/speckit.implement.prompt.md).
Implementar TASK-09 — EliminarClienteUseCase (HU-04).

Contexto:

- specs/001-sistema-bancario-completo/tasks.md (TASK-09)
- specs/001-sistema-bancario-completo/spec.md (HU-04, CA-04.1 a CA-04.2)
- specs/001-sistema-bancario-completo/contracts/api-customers.md
  (DELETE /clientes/{id})
- specs/001-sistema-bancario-completo/contracts/events.md
  (ClienteDesactivadoEvent)
- constitution.md (v1.0.2)

DECISIÓN B — LEER ANTES DE IMPLEMENTAR:
EliminarClienteUseCase NUNCA valida si el cliente tiene
cuentas activas. ClienteConCuentasActivasException NO existe
en customers-service. El use case desactiva al cliente
incondicionalmente si existe. La desactivación de cuentas
es responsabilidad exclusiva de accounts-service al consumir
ClienteDesactivadoEvent.

PASO 1 — RED: crear el test primero.

Crear:
customers-service/src/test/java/com/banking/customers/
unit/usecase/EliminarClienteUseCaseTest.java

Con los siguientes métodos:
  existingClienteShouldDeactivateAndPublishEvent()
  nonExistingClienteShouldThrowClienteNotFoundException()
  deactivatedClienteShouldPublishClienteDesactivadoEvent()
  deactivationShouldNeverCheckForActiveCuentas()
  publishedEventShouldContainCorrectClienteId()

Setup con Mockito:
  clienteRepository = mock(ClienteRepository.class)
  eventPublisher = mock(EventPublisher.class)
  useCase = new EliminarClienteUseCase(clienteRepository,
                                        eventPublisher)

Para deactivationShouldNeverCheckForActiveCuentas():

- Ejecutar useCase.ejecutar() con un cliente existente
- Verificar que clienteRepository solo recibe llamadas a
    findById() y save() — ninguna otra
- Verificar que NO se llama ningún método que involucre
    verificación de cuentas
- Este test es la garantía ejecutable de Decisión B

Para publishedEventShouldContainCorrectClienteId():

- Capturar el evento con ArgumentCaptor<DomainEvent>
- Verificar que es instancia de ClienteDesactivadoEvent
- Verificar que clienteId coincide con el del cliente
    desactivado

PASO 2 — GREEN: crear el use case.

Crear:
customers-service/src/main/java/com/banking/customers/
application/usecase/EliminarClienteUseCase.java

- Constructor con ClienteRepository y EventPublisher
- Método: public ClienteResponse ejecutar(Long id)
- Flujo:
  1. clienteRepository.findById(id)
     → ClienteNotFoundException si no existe
  2. cliente.desactivar()
     (cambia estado a INACTIVO y registra
     ClienteDesactivadoEvent internamente en el aggregate)
  3. clienteRepository.save(cliente)
  4. List<DomainEvent> eventos = cliente.consumirEventos()
  5. eventos.forEach(eventPublisher::publish)
  6. Retornar ClienteResponse.from(clienteDesactivado)

⚠️ El evento lo registra cliente.desactivar() internamente.
   El use case solo lo extrae con consumirEventos() y
   lo publica. Esto mantiene la lógica de qué evento
   se genera dentro del dominio.

⚠️ NO existe ninguna verificación de cuentas activas
   en ningún punto de este flujo. Ninguna llamada a
   métodos relacionados con cuentas.

- Sin @Service ni anotaciones de Spring

PASO 3 — REFACTOR:
Verificar que:

- ejecutar() no supera 10 líneas
- El único motivo por el que este use case puede fallar
  es ClienteNotFoundException
- No hay ninguna referencia a cuentas en ninguna parte

RESTRICCIONES ABSOLUTAS:

- Cero comentarios
- Sin imports de jakarta ni springframework
- ClienteConCuentasActivasException NO se importa
  ni se referencia en ningún lugar
- Test sin anotaciones de Spring

CRITERIO DE COMPLETITUD:

- ./gradlew :customers-service:test → 52 tests — 0 failures
- deactivationShouldNeverCheckForActiveCuentas() pasa
  en verde — garantía ejecutable de Decisión B
- publishedEventShouldContainCorrectClienteId() pasa
  con ArgumentCaptor verificando ClienteDesactivadoEvent
- find application/usecase -name "EliminarClienteUseCase.java"
  | xargs grep -i "cuenta" → 0 resultados
- find src -name "*.java" | xargs grep -l "//" → 0 resultados

GitHub Copilot: Ran terminal command:  cat /home/alexis-yepez/Desktop/challenges/backend/banking-microservices/customers-service/src/main/java/com/banking/customers/domain/event/ClienteDesactivadoEvent.java

**PASO 1 — RED**

Created [](file:///home/alexis-yepez/Desktop/challenges/backend/banking-microservices/customers-service/src/test/java/com/banking/customers/unit/usecase/EliminarClienteUseCaseTest.java)

Ran terminal command:  ./gradlew :customers-service:compileTestJava --no-daemon 2>&1 | grep -E "error:|EliminarClienteUseCase|FAILED|BUILD" | head -5

Red confirmado. **PASO 2 — GREEN**  

Created [](file:///home/alexis-yepez/Desktop/challenges/backend/banking-microservices/customers-service/src/main/java/com/banking/customers/application/usecase/EliminarClienteUseCase.java)

Ran terminal command:  ./gradlew :customers-service:test --no-daemon 2>&1 | grep -E "BUILD|FAILED" | tail -3

Ran terminal command:  find customers-service/build/test-results -name "*.xml" | xargs grep -h 'tests=' | sed 's/.*tests="\([0-9]*\)".*/\1/' | awk '{s+=$1} END {print "Total:", s}' && find customers-service/build/test-results -name "*.xml" | xargs grep -h 'failures=' | sed 's/.*failures="\([0-9]*\)".*/\1/' | awk '{s+=$1} END {print "Failures:", s}' && echo "=== Spring/Jakarta ===" && find customers-service/src/main/java/com/banking/customers/application/usecase -name "*.java" | xargs grep -l "springframework\|jakarta" 2>/dev/null || echo "NONE (OK)" && echo "=== Comments ===" && find customers-service/src -name "*.java" | xargs grep -l "//" 2>/dev/null || echo "NONE (OK)" && echo "=== Cuentas ref ===" && grep -i "cuenta" customers-service/src/main/java/com/banking/customers/application/usecase/EliminarClienteUseCase.java || echo "NONE (OK)" && echo "=== ejecutar() lines ===" && awk '/public ClienteResponse ejecutar\(Long id\)/{found=1; count=0} found{count++} /^    \}$/ && found && count>1{print count-1 " lines"; found=0}' customers-service/src/main/java/com/banking/customers/application/usecase/EliminarClienteUseCase.java

52 tests — 0 fallos — `ejecutar()` en 7 líneas. Marco TASK-09 en tasks.md:

Read [](file:///home/alexis-yepez/Desktop/challenges/backend/banking-microservices/specs/001-sistema-bancario-completo/tasks.md#409-409), lines 409 to 445

Replacing 5 lines with 5 lines in [](file:///home/alexis-yepez/Desktop/challenges/backend/banking-microservices/specs/001-sistema-bancario-completo/tasks.md)

```

```

TASK-09 completada.

**Archivos creados:**

- EliminarClienteUseCaseTest.java — 5 tests
- EliminarClienteUseCase.java — `ejecutar()` en 7 líneas

| Criterio | Estado |
|---|---|
| Tests totales | 52 — 0 fallos |
| `deactivationShouldNeverCheckForActiveCuentas()` | ✅ — garantía ejecutable de Decisión B |
| `publishedEventShouldContainCorrectClienteId()` | ✅ — `ArgumentCaptor` verifica `ClienteDesactivadoEvent` con `clienteId=1L` |
| Referencia a "cuenta" en `EliminarClienteUseCase` | 0 ocurrencias |
| Spring/Jakarta en `application/usecase/` | 0 archivos |
| Comentarios `//` | 0 archivos |
| `ejecutar()` ≤ 10 líneas | 7 líneas |

Made changes.

User: Follow instructions in [speckit.analyze.prompt.md](file:///home/alexis-yepez/Desktop/challenges/backend/banking-microservices/.github/prompts/speckit.analyze.prompt.md).
Analizar TASK-09 — EliminarClienteUseCase.

ARCHIVOS A ANALIZAR:

- customers-service/src/main/java/com/banking/customers/
  application/usecase/EliminarClienteUseCase.java
- customers-service/src/test/java/com/banking/customers/
  unit/usecase/EliminarClienteUseCaseTest.java

ARTEFACTOS DE REFERENCIA:

- constitution.md (v1.0.2)
- specs/001-sistema-bancario-completo/spec.md (HU-04)
- specs/001-sistema-bancario-completo/tasks.md (Decisión B)
- specs/001-sistema-bancario-completo/contracts/events.md
  (ClienteDesactivadoEvent)

DIMENSIONES DE ANÁLISIS:

1. DECISIÓN B — AUSENCIA DE VERIFICACIÓN DE CUENTAS
   Verificar que EliminarClienteUseCase.java:
   - No importa ninguna clase relacionada con cuentas
   - No tiene ninguna referencia al string "cuenta"
     en ninguna forma (variable, método, comentario)
   - No lanza ni captura ClienteConCuentasActivasException
   - No tiene ningún puerto adicional más allá de
     ClienteRepository y EventPublisher
   Cualquier referencia a cuentas es BLOQUEANTE.

2. FLUJO DE EVENTO — DOMINIO REGISTRA, USE CASE PUBLICA
   Verificar que el flujo sigue exactamente:
   1. cliente.desactivar() registra ClienteDesactivadoEvent
      internamente en el aggregate
   2. clienteRepository.save() persiste el estado
   3. cliente.consumirEventos() extrae los eventos
   4. eventPublisher.publish() los publica
   Verificar que el use case NO construye el evento
   directamente con new ClienteDesactivadoEvent().
   El evento debe originarse en el dominio, no en
   el use case.
   Si el use case construye el evento directamente
   reportar como ADVERTENCIA — no es bloqueante pero
   rompe la separación de responsabilidades entre
   dominio y aplicación.

3. COBERTURA DE CRITERIOS DE ACEPTACIÓN HU-04
   Verificar cobertura de:
   CA-04.1: cliente sin cuentas activas se desactiva
     → en este modelo: cliente siempre se desactiva
       si existe, independiente de cuentas
   CA-04.2: cliente inexistente → ClienteNotFoundException
   Adicionalmente:
   - Evento publicado con clienteId correcto
   - Respuesta refleja estado INACTIVO
   Reportar criterio sin cobertura como BLOQUEANTE.

4. DIRECCIÓN DE DEPENDENCIAS
   EliminarClienteUseCase solo debe importar:
   - application/dto/ (ClienteResponse)
   - domain/model/ (Cliente, EstadoCliente si aplica)
   - domain/port/ (ClienteRepository, EventPublisher)
   - domain/event/ (DomainEvent para el forEach)
   - domain/exception/ (ClienteNotFoundException)
   NO debe importar infrastructure/ ni jakarta ni springframework.
   Reportar violación como BLOQUEANTE.

5. CONSISTENCIA CON EL AGGREGATE PATTERN
   Verificar que:
   - La lógica de qué evento se genera vive en
     Cliente.desactivar(), no en el use case
   - consumirEventos() se llama después de save()
     para garantizar que el evento se publica solo
     tras persistencia exitosa
   - Si save() lanzara excepción, consumirEventos()
     no se llamaría y el evento no se publicaría
   Reportar orden incorrecto como BLOQUEANTE.

6. GARANTÍA EJECUTABLE DE DECISIÓN B
   Verificar que el test
   deactivationShouldNeverCheckForActiveCuentas()
   verifica con Mockito que clienteRepository solo
   recibe llamadas a findById() y save(), sin ninguna
   otra interacción.
   La forma correcta es:
     verify(clienteRepository).findById(anyLong())
     verify(clienteRepository).save(any(Cliente.class))
     verifyNoMoreInteractions(clienteRepository)
   Si verifyNoMoreInteractions() no está presente,
   el test no garantiza la ausencia de verificación
   de cuentas. Reportar como ADVERTENCIA.

FORMATO DE REPORTE:

- Por dimensión: ✅ PASS o ❌ BLOQUEANTE o ⚠️ ADVERTENCIA
- Resumen: X bloqueantes, Y advertencias
- Si 0 bloqueantes: confirmar T09 listo para commit

GitHub Copilot: Ran terminal command:  cat /home/alexis-yepez/Desktop/challenges/backend/banking-microservices/customers-service/src/main/java/com/banking/customers/application/usecase/EliminarClienteUseCase.java && echo "---TEST---" && cat /home/alexis-yepez/Desktop/challenges/backend/banking-microservices/customers-service/src/test/java/com/banking/customers/unit/usecase/EliminarClienteUseCaseTest.java

Ran terminal command:  grep -n "desactivar\|consumirEventos\|registrarEvento\|ClienteDesactivadoEvent" /home/alexis-yepez/Desktop/challenges/backend/banking-microservices/customers-service/src/main/java/com/banking/customers/domain/model/Cliente.java && echo "=== cuenta refs in usecase ===" && grep -i "cuenta" /home/alexis-yepez/Desktop/challenges/backend/banking-microservices/customers-service/src/main/java/com/banking/customers/application/usecase/EliminarClienteUseCase.java || echo "NONE" && echo "=== new ClienteDesactivadoEvent in usecase ===" && grep "new ClienteDesactivadoEvent" /home/alexis-yepez/Desktop/challenges/backend/banking-microservices/customers-service/src/main/java/com/banking/customers/application/usecase/EliminarClienteUseCase.java || echo "NONE" && echo "=== imports ===" && head -12 /home/alexis-yepez/Desktop/challenges/backend/banking-microservices/customers-service/src/main/java/com/banking/customers/application/usecase/EliminarClienteUseCase.java

---

## Specification Analysis Report — TASK-09: `EliminarClienteUseCase`

### Dimensión 1 — Decisión B: Ausencia de Verificación de Cuentas

✅ **PASS**

| Verificación | Resultado |
|---|---|
| Imports relacionados con cuentas | 0 |
| Referencia al string "cuenta" | 0 (grep devuelve `NONE`) |
| `ClienteConCuentasActivasException` importada o lanzada | Ausente |
| Puertos adicionales más allá de `ClienteRepository` y `EventPublisher` | Ninguno |

---

### Dimensión 2 — Flujo de Evento: Dominio Registra, Use Case Publica

✅ **PASS**

El evento se origina exclusivamente en Cliente.java:

```java
public void desactivar() {
    this.estado = EstadoCliente.INACTIVO;
    registrarEvento(new ClienteDesactivadoEvent(this.id));  // ← dominio construye el evento
}
```

El use case **nunca** instancia `new ClienteDesactivadoEvent()` directamente. Flujo verificado:

```
cliente.desactivar()            → evento registrado en el aggregate  ✅
clienteRepository.save(cliente) → persistencia                       ✅
cliente.consumirEventos()       → extracción (objeto original)       ✅
eventPublisher.publish(...)     → publicación                        ✅
```

`consumirEventos()` se llama sobre `cliente` (el objeto donde `desactivar()` registró el evento), no sobre `clienteDesactivado` — correcto, ya que JPA en producción puede retornar una nueva instancia sin el estado de eventos en memoria.

---

### Dimensión 3 — Cobertura de Criterios de Aceptación HU-04

✅ **PASS**

| Criterio | Test | Estado |
|---|---|---|
| CA-04.1 cliente se desactiva (Decisión B: siempre, sin condición de cuentas) | `existingClienteShouldDeactivateAndPublishEvent()` — `response.getEstado() == "INACTIVO"` | ✅ |
| CA-04.2 cliente inexistente → `ClienteNotFoundException` | `nonExistingClienteShouldThrowClienteNotFoundException()` — `save()` y `publish()` nunca llamados | ✅ |
| Evento publicado con `clienteId` correcto | `publishedEventShouldContainCorrectClienteId()` — `ArgumentCaptor` verifica `clienteId == 1L` | ✅ |
| Respuesta refleja estado `INACTIVO` | `existingClienteShouldDeactivateAndPublishEvent()` | ✅ |

---

### Dimensión 4 — Dirección de Dependencias

✅ **PASS**

| Import | Paquete | Permitido |
|---|---|---|
| `ClienteResponse` | `application/dto/` | ✅ |
| `DomainEvent` | `domain/event/` | ✅ |
| `ClienteNotFoundException` | `domain/exception/` | ✅ |
| `Cliente` | `domain/model/` | ✅ |
| `ClienteRepository`, `EventPublisher` | `domain/port/` | ✅ |
| `java.util.List` | JDK | ✅ |

Cero imports de `infrastructure/`, `jakarta.*`, `org.springframework.*`.

---

### Dimensión 5 — Consistencia con el Aggregate Pattern

✅ **PASS**

| Regla | Verificación |
|---|---|
| Lógica de qué evento se genera vive en `Cliente.desactivar()` | ✅ — `registrarEvento(new ClienteDesactivadoEvent(this.id))` en el dominio |
| `consumirEventos()` llamado después de `save()` | ✅ — orden garantiza que si `save()` lanza, el evento no se publica |
| Use case no construye el evento directamente | ✅ — sin `new ClienteDesactivadoEvent()` en el use case |

---

### Dimensión 6 — Garantía Ejecutable de Decisión B

✅ **PASS**

`deactivationShouldNeverCheckForActiveCuentas()` contiene exactamente las tres verificaciones requeridas:

```java
verify(clienteRepository).findById(ID);
verify(clienteRepository).save(any(Cliente.class));
verifyNoMoreInteractions(clienteRepository);    // ← presente ✅
```

`verifyNoMoreInteractions` garantiza que ninguna otra llamada ocurre sobre el repositorio — es la prueba ejecutable de que no existe verificación de cuentas ni ahora ni si alguien modifica el use case en el futuro.

---

## Métricas

| Métrica | Valor |
|---|---|
| Criterios de aceptación cubiertos | 4 / 4 |
| Tests en verde | 52 / 52 |
| Importaciones inválidas | 0 |
| Referencias a "cuenta" | 0 |
| `ejecutar()` líneas | 7 (límite: 10) |
| Bloqueantes | **0** |
| Advertencias | **0** |

---

**0 bloqueantes — 0 advertencias.**

✅ **TASK-09 está listo para commit.**
