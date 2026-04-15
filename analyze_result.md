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
