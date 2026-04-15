# API Contract — customers-service

**Base URL (local)**: `http://localhost:8080`
**Content-Type**: `application/json`
**Charset**: UTF-8

---

## POST /clientes

Crea un nuevo cliente en el sistema.

### Request body

```json
{
  "nombre": "Jose Lema",
  "genero": "MASCULINO",
  "edad": 30,
  "identificacion": "1713175071",
  "direccion": "Otavalo sn y principal",
  "telefono": "+593991234567",
  "contrasena": "1234.Abc",
  "estado": "activo"
}
```

| Campo | Tipo | Obligatorio | Validación |
|---|---|---|---|
| `nombre` | String | Sí | No nulo, no vacío, max 100 chars |
| `genero` | String | Sí | MASCULINO \| FEMENINO |
| `edad` | Integer | Sí | ≥ 18 |
| `identificacion` | String | Sí | 10 dígitos, válida algoritmo módulo 10 |
| `direccion` | String | Sí | No nulo, no vacío, max 200 chars |
| `telefono` | String | Sí | Con prefijo de país (empieza con +) |
| `contrasena` | String | Sí | ≥ 8 chars, alfanumérica, ≥ 1 mayúscula |
| `estado` | String | Sí | activo \| inactivo |

### Response 201 — Created

```json
{
  "clienteId": 1,
  "nombre": "Jose Lema",
  "genero": "MASCULINO",
  "edad": 30,
  "identificacion": "1713175071",
  "direccion": "Otavalo sn y principal",
  "telefono": "+593991234567",
  "estado": "activo"
}
```

La `contrasena` no se incluye en ninguna respuesta.

### Response 400 — Bad Request

```json
{
  "timestamp": "2026-04-14T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "La edad debe ser mayor o igual a 18",
  "path": "/clientes"
}
```

### Response 409 — Conflict

```json
{
  "timestamp": "2026-04-14T10:30:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "Ya existe un cliente con la identificación 1713175071",
  "path": "/clientes"
}
```

---

## GET /clientes/{id}

Consulta los datos de un cliente por su ID interno.

### Path parameters

| Parámetro | Tipo | Descripción |
|---|---|---|
| `id` | Long | Identificador interno del cliente |

### Response 200 — OK

```json
{
  "clienteId": 1,
  "nombre": "Jose Lema",
  "genero": "MASCULINO",
  "edad": 30,
  "identificacion": "1713175071",
  "direccion": "Otavalo sn y principal",
  "telefono": "+593991234567",
  "estado": "activo"
}
```

### Response 404 — Not Found

```json
{
  "timestamp": "2026-04-14T10:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Cliente no encontrado con ID: 99",
  "path": "/clientes/99"
}
```

---

## PUT /clientes/{id}

Actualiza los datos de un cliente existente. Todos los campos son opcionales; solo se aplican los enviados.

### Request body

```json
{
  "nombre": "Jose Lema Suarez",
  "direccion": "Nueva dirección 456",
  "telefono": "+593999999999"
}
```

Los mismos campos y validaciones que `POST /clientes`, todos opcionales. `identificacion` y `genero` también son actualizables con las mismas reglas.

### Response 200 — OK

```json
{
  "clienteId": 1,
  "nombre": "Jose Lema Suarez",
  "genero": "MASCULINO",
  "edad": 30,
  "identificacion": "1713175071",
  "direccion": "Nueva dirección 456",
  "telefono": "+593999999999",
  "estado": "activo"
}
```

### Response 400 — Bad Request

Misma estructura que `POST /clientes` 400.

### Response 404 — Not Found

Misma estructura que `GET /clientes/{id}` 404.

### Response 409 — Conflict

Misma estructura que `POST /clientes` 409 (cuando la nueva identificación ya existe en otro cliente).

---

## DELETE /clientes/{id}

Desactiva lógicamente un cliente (cambia estado a inactivo).

### Path parameters

| Parámetro | Tipo | Descripción |
|---|---|---|
| `id` | Long | Identificador interno del cliente |

### Response 200 — OK

```json
{
  "clienteId": 1,
  "nombre": "Jose Lema",
  "genero": "MASCULINO",
  "edad": 30,
  "identificacion": "1713175071",
  "direccion": "Otavalo sn y principal",
  "telefono": "+593991234567",
  "estado": "inactivo"
}
```

### Response 404 — Not Found

Misma estructura que `GET /clientes/{id}` 404.

### Response 409 — Conflict

```json
{
  "timestamp": "2026-04-14T10:30:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "El cliente posee cuentas activas que deben ser desactivadas primero",
  "path": "/clientes/1"
}
```

El mensaje es literal e invariante — no puede modificarse.
