# API Contract — accounts-service

**Base URL (local)**: `http://localhost:8081`
**Content-Type**: `application/json`
**Charset**: UTF-8

---

## POST /cuentas

Crea una nueva cuenta bancaria asociada a un cliente.

### Request body

```json
{
  "numeroCuenta": "478758",
  "tipo": "ahorro",
  "saldoInicial": 2000.00,
  "estado": "activa",
  "clienteId": 1
}
```

| Campo | Tipo | Obligatorio | Validación |
|---|---|---|---|
| `numeroCuenta` | String | Sí | No nulo, max 20 chars |
| `tipo` | String | Sí | ahorro \| corriente \| digital |
| `saldoInicial` | BigDecimal | Sí | ≥ 0; ≥ 50 si tipo = corriente |
| `estado` | String | Sí | activa \| inactiva |
| `clienteId` | Long | Sí | Debe existir y estar activo en proyección |

### Response 201 — Created

```json
{
  "id": 1,
  "numeroCuenta": "478758",
  "tipo": "ahorro",
  "saldoInicial": 2000.00,
  "saldoDisponible": 2000.00,
  "estado": "activa",
  "clienteId": 1
}
```

### Response 400 — Bad Request

```json
{
  "timestamp": "2026-04-14T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "El saldo inicial debe ser mayor o igual a cero",
  "path": "/cuentas"
}
```

### Response 409 — Conflict

```json
{
  "timestamp": "2026-04-14T10:30:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "Ya existe una cuenta con el número 478758",
  "path": "/cuentas"
}
```

### Response 422 — Unprocessable Entity

```json
{
  "timestamp": "2026-04-14T10:30:00Z",
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "El cliente referenciado no existe o está inactivo",
  "path": "/cuentas"
}
```

---

## GET /cuentas/{id}

Consulta los datos de una cuenta por su ID.

### Response 200 — OK

```json
{
  "id": 1,
  "numeroCuenta": "478758",
  "tipo": "ahorro",
  "saldoInicial": 2000.00,
  "saldoDisponible": 1500.00,
  "estado": "activa",
  "clienteId": 1
}
```

### Response 404 — Not Found

```json
{
  "timestamp": "2026-04-14T10:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Cuenta no encontrada con ID: 99",
  "path": "/cuentas/99"
}
```

---

## PUT /cuentas/{id}

Actualiza los datos de una cuenta existente. Solo `estado`, `numeroCuenta` y `tipo` son actualizables.

### Request body

```json
{
  "estado": "inactiva"
}
```

### Response 200 — OK

```json
{
  "id": 1,
  "numeroCuenta": "478758",
  "tipo": "ahorro",
  "saldoInicial": 2000.00,
  "saldoDisponible": 1500.00,
  "estado": "inactiva",
  "clienteId": 1
}
```

### Response 404 — Not Found

Misma estructura que `GET /cuentas/{id}` 404.

---

## DELETE /cuentas/{id}

Desactiva lógicamente una cuenta (cambia estado a inactiva) si no tiene movimientos en el último año.

### Response 200 — OK

```json
{
  "id": 1,
  "numeroCuenta": "478758",
  "tipo": "ahorro",
  "saldoInicial": 2000.00,
  "saldoDisponible": 2000.00,
  "estado": "inactiva",
  "clienteId": 1
}
```

### Response 404 — Not Found

Misma estructura que `GET /cuentas/{id}` 404.

### Response 409 — Conflict

```json
{
  "timestamp": "2026-04-14T10:30:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "La cuenta no puede eliminarse porque tiene actividad en el último año",
  "path": "/cuentas/1"
}
```

---

## POST /movimientos

Registra un depósito (valor positivo) o retiro (valor negativo) sobre una cuenta activa.

### Request body

```json
{
  "cuentaId": 1,
  "valor": -575.00
}
```

| Campo | Tipo | Obligatorio | Validación |
|---|---|---|---|
| `cuentaId` | Long | Sí | Debe existir y estar activa |
| `valor` | BigDecimal | Sí | != 0; negativo = retiro; positivo = depósito |

### Response 201 — Created

```json
{
  "id": 1,
  "fecha": "2026-04-14T10:30:00",
  "tipoMovimiento": "retiro",
  "valor": -575.00,
  "saldoResultante": 1425.00,
  "cuentaId": 1
}
```

### Response 400 — Bad Request

```json
{
  "timestamp": "2026-04-14T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "El valor del movimiento no puede ser cero",
  "path": "/movimientos"
}
```

### Response 422 — Unprocessable Entity (saldo insuficiente)

```json
{
  "timestamp": "2026-04-14T10:30:00Z",
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "Saldo no disponible",
  "path": "/movimientos"
}
```

Los mensajes `"Saldo no disponible"` y `"Límite de retiro diario excedido"` son literales invariantes.

### Response 422 — Unprocessable Entity (límite diario)

```json
{
  "timestamp": "2026-04-14T10:30:00Z",
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "Límite de retiro diario excedido",
  "path": "/movimientos"
}
```

### Response 422 — Unprocessable Entity (cuenta inactiva)

```json
{
  "timestamp": "2026-04-14T10:30:00Z",
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "La cuenta está inactiva y no puede recibir movimientos",
  "path": "/movimientos"
}
```

---

## GET /movimientos/{id}

Consulta un movimiento por su ID.

### Response 200 — OK

```json
{
  "id": 1,
  "fecha": "2026-04-14T10:30:00",
  "tipoMovimiento": "retiro",
  "valor": -575.00,
  "saldoResultante": 1425.00,
  "cuentaId": 1,
  "movimientoOrigenId": null
}
```

### Response 404 — Not Found

```json
{
  "timestamp": "2026-04-14T10:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Movimiento no encontrado con ID: 99",
  "path": "/movimientos/99"
}
```

---

## POST /ajustes

Registra un movimiento de ajuste sobre una transacción existente.

### Request body

```json
{
  "movimientoOrigenId": 1,
  "valor": 50.00,
  "justificacion": "Corrección de monto por error de digitación"
}
```

| Campo | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `movimientoOrigenId` | Long | Sí | ID del movimiento a ajustar |
| `valor` | BigDecimal | Sí | Monto del ajuste (positivo o negativo, != 0) |
| `justificacion` | String | Sí | Motivo del ajuste |

### Response 201 — Created

```json
{
  "id": 5,
  "fecha": "2026-04-14T11:00:00",
  "tipoMovimiento": "ajuste",
  "valor": 50.00,
  "saldoResultante": 1475.00,
  "cuentaId": 1,
  "movimientoOrigenId": 1
}
```

### Response 404 — Not Found

```json
{
  "timestamp": "2026-04-14T11:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Movimiento no encontrado con ID: 99",
  "path": "/ajustes"
}
```

---

## POST /reversiones

Registra la reversión completa de una transacción existente.

### Request body

```json
{
  "movimientoOrigenId": 1
}
```

| Campo | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `movimientoOrigenId` | Long | Sí | ID del movimiento a revertir |

### Response 201 — Created

```json
{
  "id": 6,
  "fecha": "2026-04-14T11:05:00",
  "tipoMovimiento": "reversion",
  "valor": 575.00,
  "saldoResultante": 2000.00,
  "cuentaId": 1,
  "movimientoOrigenId": 1
}
```

El `valor` de la reversión es el opuesto del movimiento original. Si el original fue `-575.00` (retiro), la reversión es `+575.00`.

### Response 404 — Not Found

Misma estructura que `POST /ajustes` 404.

---

## GET /reportes

Genera el reporte de estado de cuenta con historial de movimientos filtrado por cliente y rango de fechas.

### Query parameters

| Parámetro | Tipo | Obligatorio | Formato |
|---|---|---|---|
| `clienteId` | Long | Sí | Entero positivo |
| `fechaInicio` | String | Sí | ISO-8601 fecha: `2026-01-01` |
| `fechaFin` | String | Sí | ISO-8601 fecha: `2026-04-14` |

### Ejemplo de URL

```
GET /reportes?clienteId=1&fechaInicio=2026-01-01&fechaFin=2026-04-14
```

### Response 200 — OK

Array de objetos. Una fila por movimiento. Si no hay movimientos en el rango, devuelve array vacío `[]`.

```json
[
  {
    "fecha": "2026-04-14T10:30:00",
    "cliente": "Jose Lema",
    "numeroCuenta": "478758",
    "tipo": "ahorro",
    "saldoInicial": 2000.00,
    "estado": "activa",
    "movimiento": -575.00,
    "saldoDisponible": 1425.00
  },
  {
    "fecha": "2026-04-14T09:00:00",
    "cliente": "Jose Lema",
    "numeroCuenta": "585545",
    "tipo": "corriente",
    "saldoInicial": 1000.00,
    "estado": "activa",
    "movimiento": 500.00,
    "saldoDisponible": 1500.00
  }
]
```

| Campo | Descripción |
|---|---|
| `fecha` | Fecha y hora del movimiento |
| `cliente` | Nombre del cliente |
| `numeroCuenta` | Número de cuenta |
| `tipo` | Tipo de cuenta (ahorro / corriente / digital) |
| `saldoInicial` | Saldo inicial de la cuenta al momento de creación |
| `estado` | Estado actual de la cuenta |
| `movimiento` | Valor del movimiento (positivo = ingreso, negativo = egreso) |
| `saldoDisponible` | Saldo disponible de la cuenta DESPUÉS del movimiento |

### Response 400 — Bad Request

```json
{
  "timestamp": "2026-04-14T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Los parámetros clienteId, fechaInicio y fechaFin son obligatorios",
  "path": "/reportes"
}
```

### Response 404 — Not Found

```json
{
  "timestamp": "2026-04-14T10:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Cliente no encontrado con ID: 99",
  "path": "/reportes"
}
```
