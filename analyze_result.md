## Specification Analysis Report — FASE 4 accounts-service dominio

### Dimensión 1 — Arquitectura Hexagonal Estricta ✅ PASS

Scan resultado (verificado con grep anterior): **0 archivos** en `domain/` contienen `jakarta` o `springframework`.

| Capa | Archivos | Importaciones externas | Estado |
|------|----------|------------------------|--------|
| domain/model | 7 | Solo `java.*` y excepciones propias | ✅ |
| domain/exception | 15 | Solo package interno | ✅ |
| domain/validator | 5 | Solo domain/exception + domain/model | ✅ |
| domain/port | 3 | Solo domain/model + `java.*` | ✅ |

---

### Dimensión 2 — Enums UPPER_SNAKE_CASE ✅ PASS

| Enum | Valores declarados | Estado |
|------|--------------------|--------|
| `EstadoCliente` | `ACTIVO`, `INACTIVO` | ✅ |
| `EstadoCuenta` | `ACTIVA`, `INACTIVA` | ✅ |
| `TipoCuenta` | `AHORRO`, `CORRIENTE`, `DIGITAL` | ✅ |
| `TipoMovimiento` | `DEPOSITO`, `RETIRO`, `AJUSTE`, `REVERSION` | ✅ |

Grep de valores en minúscula: **0 ocurrencias**.

---

### Dimensión 3 — Mensajes Literales Invariantes ✅ PASS

| Regla | Excepción | Mensaje implementado | Mensaje requerido | Estado |
|-------|-----------|---------------------|-------------------|--------|
| RN-01 | `SaldoInsuficienteException` | `"Saldo no disponible"` | `"Saldo no disponible"` | ✅ exacto |
| RN-02 | `LimiteDiarioExcedidoException` | `"Límite de retiro diario excedido"` | `"Límite de retiro diario excedido"` | ✅ exacto |
| RN-03 | `ValorMovimientoInvalidoException` | `"El valor del movimiento no puede ser cero"` | `"El valor del movimiento no puede ser cero"` | ✅ exacto |
| RN-04 | N/A | (Decisión B — eliminado de accounts-service) | — | ✅ N/A |

---

### Dimensión 4 — Encapsulación del Dominio ✅ PASS

| Clase | Setters públicos | Métodos de negocio | Estado |
|-------|------------------|--------------------|--------|
| `Cuenta` | Ninguno | `aplicarMovimiento()`, `desactivar()`, `tieneMovimientosRecientes()` | ✅ |
| `Movimiento` | Ninguno | Solo factory methods | ✅ |
| `ClienteProyeccion` | Ninguno | `estaActivo()`, `create()`, `reconstitute()` | ✅ |

Constructor privado vacío presente en las tres clases. ✅

---

### Dimensión 5 — Decisión A: campo `justificacion` ✅ PASS

| Verificación | Resultado | Estado |
|---|---|---|
| `Movimiento.justificacion` tipo `String` nullable | Presente — campo sin `@NotNull` | ✅ |
| `AJUSTE` + justificacion null → `JustificacionRequeridaException` | `justificacion == null \|\| justificacion.isBlank()` | ✅ |
| `DEPOSITO` con justificacion null → válido | Test lo cubre | ✅ |
| `REVERSION` con justificacion null → válido | Test lo cubre | ✅ |
| `MovimientoJpaEntity.justificacion` con `@Column(length=500)` sin `nullable=false` | Presente y nullable por defecto JPA | ✅ |

---

### Dimensión 6 — Chain of Validators ✅ PASS

| Validator | Implementa `MovimientoValidator` | Solo actúa en retiro (valor < 0) | Constante tipo | Estado |
|-----------|----------------------------------|----------------------------------|----------------|--------|
| `ValorCeroValidator` | ✅ | N/A (valor == 0) | — | ✅ |
| `CuentaActivaValidator` | ✅ | No aplica | — | ✅ |
| `SaldoInsuficienteValidator` | ✅ | ✅ `valor.compareTo(ZERO) < 0` | — | ✅ |
| `LimiteDiarioValidator` | ✅ | ✅ `valor.compareTo(ZERO) < 0` | `new BigDecimal("500")` | ✅ |

---

### Dimensión 7 — Puertos como Interfaces Puras ✅ PASS

| Puerto | ¿Interfaz? | Imports framework | Métodos R-02/R-07 | Estado |
|--------|------------|-------------------|-------------------|--------|
| `CuentaRepository` | ✅ | Ninguno | — | ✅ |
| `MovimientoRepository` | ✅ | Ninguno | `sumRetirosDiariosByClienteId` ✅ · `existsMovimientoReciente` ✅ | ✅ |
| `ClienteProyeccionRepository` | ✅ | Ninguno | — | ✅ |

---

### Dimensión 8 — Entidades JPA en Infraestructura ✅ PASS

| Entidad | Paquete | `@Entity` | Constructor protegido | `justificacion VARCHAR(500)` | Estado |
|---------|---------|-----------|----------------------|------------------------------|--------|
| `CuentaJpaEntity` | infrastructure/persistence | ✅ | ✅ | N/A | ✅ |
| `MovimientoJpaEntity` | infrastructure/persistence | ✅ | ✅ | `@Column(length=500)` ✅ | ✅ |
| `ClienteProyeccionJpaEntity` | infrastructure/persistence | ✅ | ✅ | N/A | ✅ |

---

### Dimensión 9 — Consistencia con data-model.md ✅ PASS

| Campo | Tipo Java implementado | Tipo requerido | Estado |
|-------|------------------------|----------------|--------|
| `Cuenta.saldoInicial` | `BigDecimal` | `NUMERIC(15,2)` | ✅ |
| `Cuenta.saldoDisponible` | `BigDecimal` | `NUMERIC(15,2)` | ✅ |
| `Cuenta.clienteId` | `Long` | `BIGINT` | ✅ |
| `Movimiento.valor` | `BigDecimal` | `NUMERIC(15,2)` | ✅ |
| `Movimiento.saldoResultante` | `BigDecimal` | `NUMERIC(15,2)` | ✅ |
| `Movimiento.fecha` | `LocalDateTime` | `TIMESTAMP` | ✅ |
| `Movimiento.cuentaId` | `Long` | `BIGINT NOT NULL` | ✅ |
| `Movimiento.movimientoOrigenId` | `Long` (nullable) | `BIGINT NULL` | ✅ |
| `CuentaJpaEntity` precision/scale | `precision=15, scale=2` | `NUMERIC(15,2)` | ✅ |

---

### Dimensión 10 — Tests sin Spring ✅ PASS

| Archivo de test | Anotaciones Spring | Patrón de nombres (EN) | Estado |
|-----------------|-------------------|------------------------|--------|
| `CuentaCreationTest` | Ninguna | ✅ | ✅ |
| `ExceptionHierarchyTest` | Ninguna | ✅ | ✅ |
| `MovimientoCreationTest` | Ninguna | ✅ | ✅ |
| `MovimientoValidatorTest` | Ninguna | ✅ | ✅ |
| `PortStructureTest` | Ninguna | ✅ | ✅ |

---

### Hallazgos — Tabla completa

| ID | Categoría | Severidad | Archivo | Descripción |
|----|-----------|-----------|---------|-------------|
| W1 | Test precision | ⚠️ ADVERTENCIA | CuentaCreationTest.java | `cuentaCorrienteWithBalanceLessThan50ShouldFail`, `cuentaWithNegativeBalanceShouldFail`, `cuentaForInactiveClienteShouldFail` usan `RuntimeException.class` en `assertThrows`. Deberían usar `SaldoMinimoInsuficienteException`, `SaldoInicialInvalidoException`, `ClienteInactivoException` respectivamente. Pasan porque son subtypes, pero reducen la señal de futuros regressions. |
| W2 | Test precision | ⚠️ ADVERTENCIA | MovimientoCreationTest.java | `movimientoWithZeroValueShouldThrowException` usa `RuntimeException.class` en lugar de `ValorMovimientoInvalidoException.class`. Mismo impacto que W1. |
| W3 | JPA explicitness | ⚠️ ADVERTENCIA | MovimientoJpaEntity.java | `@Column(length = 500)` en `justificacion` no declara explícitamente `nullable = true`. JPA defalt es nullable, funciona correctamente, pero la intención es más clara con `nullable = true` explícito conforme al DDL de data-model.md. |

---

### Métricas

| Métrica | Valor |
|---------|-------|
| Total archivos de producción analizados | 29 |
| Total archivos de test analizados | 5 |
| Total tests ejecutados | 28 |
| **Bloqueantes** | **0** |
| **Advertencias** | **3** |
| Cobertura de dimensiones | 10/10 ✅ |

---

### Veredicto

**0 bloqueantes — FASE 4 lista para commit.**

Las 3 advertencias son de baja prioridad (test precision + JPA explicitness) y no bloquean avanzar a FASE 5.

---

### Próximas acciones sugeridas

- **Proceder a FASE 5** (T18–T22): DTOs y use cases de accounts-service.
- **Opcional antes del commit**: corregir W1 y W2 actualizando los `assertThrows` a los tipos específicos — tomará menos de 5 minutos y mejora la calidad de los tests de regresión.

¿Quieres que corrija las advertencias W1 y W2 antes de continuar con FASE 5?
