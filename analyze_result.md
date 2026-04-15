## Análisis FASE 5 — accounts-service Aplicación (T18–T22)

---

### Dimensión 1 — Dirección de dependencias en use cases

| Archivo | Imports detectados | Resultado |
|---|---|---|
| Todos los `*UseCase.java` | Solo `application/dto/`, `domain/model/`, `domain/port/`, `domain/exception/`, `domain/validator/` | ✅ PASS |
| DTOs | `jakarta.validation.*` (permitido en capa DTO) | ✅ PASS |

No hay ninguna referencia a `infrastructure`, `springframework` ni contexto Spring en ningún use case.

---

### Dimensión 2 — Decisión A: justificacion en ajuste

| Check | Evidencia | Resultado |
|---|---|---|
| `RegistrarAjusteUseCase` valida `justificacion != null && !isBlank()` | `validarJustificacion()` → lanza `JustificacionRequeridaException` | ✅ PASS |
| `RegistrarReversionUseCase` pasa `null` a `Movimiento.create()` | `Movimiento.create(REVERSION, valorReversion, saldoResultante, ..., null)` (último arg) | ✅ PASS |
| `RegistrarMovimientoUseCase` no maneja `justificacion` | `Movimiento.create(..., null, null)` — sin justificacion | ✅ PASS |

---

### Dimensión 3 — Decisión C: dos flujos de eliminación

| Check | Evidencia | Resultado |
|---|---|---|
| Usa `existsMovimientoReciente(id)` | Presente en `EliminarCuentaUseCase.ejecutar()` | ✅ PASS |
| Lanza excepción si hay actividad | `BusinessRuleViolationException("La cuenta no puede eliminarse...")` | ✅ PASS |
| No desactiva cuentas de forma masiva | No hay referencia a `desactivarTodasPorClienteId` | ✅ PASS |
| No referencia a `ClienteDesactivadoEvent` | Ningún import o mención en FASE 5 | ✅ PASS |

---

### Dimensión 4 — Límite diario por cliente (R-02)

| Check | Evidencia | Resultado |
|---|---|---|
| Llama `sumRetirosDiariosByClienteId(cuenta.getClienteId())` | `movimientoRepository.sumRetirosDiariosByClienteId(cuenta.getClienteId())` | ✅ PASS |

Acumulado calculado a nivel de `clienteId`, no de `cuentaId`.

---

### Dimensión 5 — Semántica de reversión (R-08)

| Check | Evidencia | Resultado |
|---|---|---|
| `movimientoOrigen.getValor().negate()` | Línea exacta en `RegistrarReversionUseCase` | ✅ PASS |
| No modifica el movimiento original | Solo lee `movimientoOrigen.getValor()` y `getCuentaId()` | ✅ PASS |
| Crea nuevo `Movimiento` con tipo `REVERSION` | `Movimiento.create(TipoMovimiento.REVERSION, ...)` | ✅ PASS |
| `movimientoOrigenId` apunta al original | `request.getMovimientoOrigenId()` pasado al `create()` | ✅ PASS |

---

### Dimensión 6 — Cobertura de HU-13 en `GenerarReporteUseCase`

| Check | Evidencia | Resultado |
|---|---|---|
| Valida `fechaInicio` no posterior a `fechaFin` | `validarFechas()` → `FechaRangoInvalidoException` | ✅ PASS |
| Lanza excepción si cliente no existe | `ClienteNotFoundException(clienteId)` | ✅ PASS |
| Retorna lista vacía si no hay movimientos | `if (movimientos.isEmpty()) return emptyList()` | ✅ PASS |
| Todos los campos de `ReporteItemResponse` presentes | `fecha`, `cliente`, `numeroCuenta`, `tipoCuenta`, `saldoInicial`, `estado`, `movimiento`, `saldoDisponible` | ✅ PASS |
| `saldoDisponible` = saldo después del movimiento | Mapeado a `m.getSaldoResultante()` | ✅ PASS |

---

### Dimensión 7 — Chain of validators en movimientos

| Check | Evidencia | Resultado |
|---|---|---|
| `List<MovimientoValidator>` inyectado por constructor | Tercer parámetro del constructor | ✅ PASS |
| Validators ejecutados ANTES de `aplicarMovimiento()` | Orden: `validators.forEach(...)` → `saldoResultante =` → `Movimiento.create()` → `cuenta.aplicarMovimiento(valor)` | ✅ PASS |

---

### Dimensión 8 — TipoMovimiento derivado del signo del valor

| Check | Evidencia | Resultado |
|---|---|---|
| `valor > 0 → DEPOSITO`, `valor < 0 → RETIRO` | `resolverTipo()`: `compareTo(ZERO) > 0 ? DEPOSITO : RETIRO` | ✅ PASS |
| `CrearMovimientoRequest` no tiene campo `tipo` | Solo `cuentaId` y `valor` | ✅ PASS |

---

### Dimensión 9 — Tests: aislamiento y cobertura

| Check | Evidencia | Resultado |
|---|---|---|
| Todos usan `mock()` puro de Mockito | `@BeforeEach` con `mock(...)`, sin `@SpringBootTest` en ningún archivo | ✅ PASS |
| `depositoShouldNotAffectDailyLimit()` existe | Presente en `RegistrarMovimientoUseCaseTest` — `acumuladoDiario=490.00` con depósito de 100 pasa igual | ✅ PASS |
| `EliminarCuentaUseCaseTest` cubre 3 escenarios Flujo 1 | `cuentaWithoutRecentActivity`, `cuentaWithRecentActivity`, `nonExistentCuenta` | ✅ PASS |

---

### Dimensión 10 — DTOs: contratos de API

| Check | Evidencia | Resultado |
|---|---|---|
| `CrearAjusteRequest.justificacion` con `@NotBlank @Size(max=500)` | Ambas anotaciones presentes | ✅ PASS |
| `CuentaResponse` incluye `saldoDisponible` | Campo `saldoDisponible` + getter + mapeo en `from()` | ✅ PASS |
| `MovimientoResponse` incluye `justificacion` (nullable) | Campo `justificacion` + getter + mapeo en `from()` | ✅ PASS |
| `ReporteItemResponse` con los 8 campos requeridos | `fecha`, `cliente`, `numeroCuenta`, `tipoCuenta`, `saldoInicial`, `estado`, `movimiento`, `saldoDisponible` | ✅ PASS |

---

## Resumen

| Métrica | Valor |
|---|---|
| Dimensiones analizadas | 10 |
| ❌ Bloqueantes | **0** |
| ⚠️ Advertencias | **0** |
| ✅ Dimensiones en PASS | **10/10** |
| Tests totales | 62 — 0 failures |

**FASE 5 lista para commit.** Todos los contratos arquitectónicos, decisiones de diseño (A, C), invariantes de investigación (R-02, R-08) y reglas de la constitución se respetan íntegramente.
