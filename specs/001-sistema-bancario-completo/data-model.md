# Data Model — Sistema Bancario Microservicios

**Phase**: 1
**Feature**: [spec.md](spec.md) | **Research**: [research.md](research.md)
**Date**: 2026-04-14

---

## db_customers

### Tabla: `persona`

```sql
CREATE TABLE persona (
    id          BIGINT      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre      VARCHAR(100) NOT NULL,
    genero      VARCHAR(10)  NOT NULL CHECK (genero IN ('MASCULINO', 'FEMENINO')),
    edad        INTEGER      NOT NULL CHECK (edad >= 18),
    identificacion VARCHAR(10) NOT NULL UNIQUE,
    direccion   VARCHAR(200) NOT NULL,
    telefono    VARCHAR(20)  NOT NULL
);
```

### Tabla: `cliente`

```sql
CREATE TABLE cliente (
    id          BIGINT       PRIMARY KEY REFERENCES persona(id),
    contrasena  VARCHAR(255) NOT NULL,
    estado      VARCHAR(10)  NOT NULL DEFAULT 'activo' CHECK (estado IN ('activo', 'inactivo'))
);
```

**Notas de diseño**:
- `InheritanceType.JOINED`: `cliente.id` es FK hacia `persona.id`. Un `SELECT` de `Cliente` genera un `JOIN` entre ambas tablas.
- La constraint `CHECK (edad >= 18)` en BD es una segunda línea de defensa; la validación primaria ocurre en el dominio.
- `contrasena` almacena el hash; nunca el texto plano.
- La eliminación lógica se refleja en `estado = 'inactivo'`; no se usa `DELETE` físico.

### Relación entre tablas

```
persona (1) ──── (0..1) cliente
```

---

## db_accounts

### Tabla: `cliente_proyeccion`

```sql
CREATE TABLE cliente_proyeccion (
    cliente_id  BIGINT       PRIMARY KEY,
    nombre      VARCHAR(100) NOT NULL,
    estado      VARCHAR(10)  NOT NULL DEFAULT 'activo' CHECK (estado IN ('activo', 'inactivo'))
);
```

**Notas de diseño**:
- Proyección local de `customers-service`. Solo almacena lo que `accounts-service` necesita para sus validaciones (existencia y estado).
- Alimentada exclusivamente por eventos RabbitMQ. Nunca por llamadas REST.
- Operación de escritura es idempotente: `INSERT ... ON CONFLICT (cliente_id) DO UPDATE`.
- `cliente_id` es el mismo valor que `cliente.id` en `db_customers` (clave compartida de negocio; no FK real porque las BDs son independientes).

### Tabla: `cuenta`

```sql
CREATE TABLE cuenta (
    id               BIGINT          GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    numero_cuenta    VARCHAR(20)     NOT NULL UNIQUE,
    tipo             VARCHAR(10)     NOT NULL CHECK (tipo IN ('ahorro', 'corriente', 'digital')),
    saldo_inicial    NUMERIC(15, 2)  NOT NULL CHECK (saldo_inicial >= 0),
    saldo_disponible NUMERIC(15, 2)  NOT NULL,
    estado           VARCHAR(10)     NOT NULL DEFAULT 'activa' CHECK (estado IN ('activa', 'inactiva')),
    cliente_id       BIGINT          NOT NULL REFERENCES cliente_proyeccion(cliente_id)
);
```

**Notas de diseño**:
- `saldo_disponible` se actualiza con cada `Movimiento`. No es columna calculada para rendimiento de lectura.
- `saldo_inicial` es de solo lectura tras la creación.
- Para cuenta `corriente`: saldo inicial mínimo de $50.00 (validado en dominio).
- Para cuenta `ahorro` y `digital`: saldo inicial mínimo de $0.00.
- `numero_cuenta` es generado por el dominio (por ejemplo, UUID truncado o secuencia prefijada).

### Tabla: `movimiento`

```sql
CREATE TABLE movimiento (
    id                   BIGINT          GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    fecha                TIMESTAMP       NOT NULL DEFAULT NOW(),
    tipo                 VARCHAR(12)     NOT NULL CHECK (tipo IN ('deposito', 'retiro', 'ajuste', 'reversion')),
    valor                NUMERIC(15, 2)  NOT NULL CHECK (valor <> 0),
    saldo_resultante     NUMERIC(15, 2)  NOT NULL,
    cuenta_id            BIGINT          NOT NULL REFERENCES cuenta(id),
    movimiento_origen_id BIGINT          REFERENCES movimiento(id)
);
```

**Notas de diseño**:
- `valor` es positivo para depósitos/ajustes que incrementan saldo, negativo para retiros/reversiones de depósitos.
- `saldo_resultante` = saldo de la cuenta **después** de aplicar este movimiento; es de auditabilidad pura.
- `movimiento_origen_id` apunta al movimiento original cuando `tipo = 'ajuste'` o `tipo = 'reversion'`; es NULL para depósitos y retiros normales.
- La constraint `valor <> 0` es segunda línea de defensa; la validación primaria es en `ValorCeroValidator`.

### Relación entre tablas

```
cliente_proyeccion (1) ──── (0..*) cuenta (1) ──── (0..*) movimiento
                                                    movimiento (0..1) ──── (0..*) movimiento [self-ref: origen]
```

---

## Entidades de dominio

### customers-service

#### `Persona` (clase base)

| Campo Java | Tipo Java | Columna BD | Tipo BD |
|---|---|---|---|
| `id` | `Long` | `id` | `BIGINT` |
| `nombre` | `String` | `nombre` | `VARCHAR(100)` |
| `genero` | `Genero` (enum) | `genero` | `VARCHAR(10)` |
| `edad` | `int` | `edad` | `INTEGER` |
| `identificacion` | `String` | `identificacion` | `VARCHAR(10)` |
| `direccion` | `String` | `direccion` | `VARCHAR(200)` |
| `telefono` | `String` | `telefono` | `VARCHAR(20)` |

#### `Cliente` (extiende Persona)

| Campo Java | Tipo Java | Columna BD | Tipo BD |
|---|---|---|---|
| `contrasena` | `String` | `contrasena` | `VARCHAR(255)` |
| `estado` | `EstadoCliente` (enum) | `estado` | `VARCHAR(10)` |
| `domainEvents` | `List<DomainEvent>` | — | (transitorio) |

**Invariantes de dominio**:
- `edad >= 18` → `EdadInvalidaException`
- `identificacion` válida por algoritmo módulo 10 → `IdentificacionInvalidaException`
- `contrasena` ≥ 8 caracteres, alfanumérica, ≥ 1 mayúscula → `ContrasenaInvalidaException`
- `genero` = MASCULINO | FEMENINO
- `identificacion` única → `IdentificacionDuplicadaException` (verificado en use case)

### accounts-service

#### `ClienteProyeccion`

| Campo Java | Tipo Java | Columna BD | Tipo BD |
|---|---|---|---|
| `clienteId` | `Long` | `cliente_id` | `BIGINT` |
| `nombre` | `String` | `nombre` | `VARCHAR(100)` |
| `estado` | `EstadoCliente` (enum) | `estado` | `VARCHAR(10)` |

#### `Cuenta`

| Campo Java | Tipo Java | Columna BD | Tipo BD |
|---|---|---|---|
| `id` | `Long` | `id` | `BIGINT` |
| `numeroCuenta` | `String` | `numero_cuenta` | `VARCHAR(20)` |
| `tipo` | `TipoCuenta` (enum) | `tipo` | `VARCHAR(10)` |
| `saldoInicial` | `BigDecimal` | `saldo_inicial` | `NUMERIC(15,2)` |
| `saldoDisponible` | `BigDecimal` | `saldo_disponible` | `NUMERIC(15,2)` |
| `estado` | `EstadoCuenta` (enum) | `estado` | `VARCHAR(10)` |
| `clienteId` | `Long` | `cliente_id` | `BIGINT` |

**Invariantes de dominio**:
- `saldoInicial >= 0` → HTTP 400
- Para `corriente`: `saldoInicial >= 50.00` → HTTP 400
- `clienteId` debe existir y estar activo en proyección → HTTP 422

#### `Movimiento`

| Campo Java | Tipo Java | Columna BD | Tipo BD |
|---|---|---|---|
| `id` | `Long` | `id` | `BIGINT` |
| `fecha` | `LocalDateTime` | `fecha` | `TIMESTAMP` |
| `tipoMovimiento` | `TipoMovimiento` (enum) | `tipo` | `VARCHAR(12)` |
| `valor` | `BigDecimal` | `valor` | `NUMERIC(15,2)` |
| `saldoResultante` | `BigDecimal` | `saldo_resultante` | `NUMERIC(15,2)` |
| `cuentaId` | `Long` | `cuenta_id` | `BIGINT` |
| `movimientoOrigenId` | `Long` | `movimiento_origen_id` | `BIGINT` |

**Invariantes de dominio**:
- `valor != 0` → `ValorCeroValidator` → HTTP 400
- Cuenta activa → `CuentaActivaValidator` → HTTP 422
- Saldo suficiente (si retiro) → `SaldoInsuficienteValidator` → HTTP 422
- Límite diario no excedido (si retiro) → `LimiteDiarioValidator` → HTTP 422

---

## Enums de dominio

```
Genero                  EstadoCliente       EstadoCuenta        TipoCuenta          TipoMovimiento
─────────────────       ─────────────       ────────────        ──────────          ──────────────
MASCULINO               activo              activa              ahorro              deposito
FEMENINO                inactivo            inactiva            corriente           retiro
                                                                digital             ajuste
                                                                                    reversion
```

---

## Jerarquía de excepciones de dominio

### customers-service

```
DomainException (abstract)
├── BusinessRuleException (abstract)
│   ├── EdadInvalidaException                   → HTTP 400
│   ├── ContrasenaInvalidaException             → HTTP 400
│   ├── IdentificacionInvalidaException          → HTTP 400
│   └── ClienteConCuentasActivasException       → HTTP 409
├── ResourceNotFoundException
│   └── ClienteNotFoundException               → HTTP 404
└── DuplicateResourceException
    └── IdentificacionDuplicadaException       → HTTP 409
```

### accounts-service

```
DomainException (abstract)
├── BusinessRuleException (abstract)
│   ├── SaldoInsuficienteException             → HTTP 422
│   ├── LimiteDiarioExcedidoException          → HTTP 422
│   ├── CuentaInactivaException                → HTTP 422
│   └── ClienteConCuentasActivasException      → HTTP 409
├── ResourceNotFoundException
│   ├── CuentaNotFoundException               → HTTP 404
│   └── MovimientoNotFoundException           → HTTP 404
└── DuplicateResourceException
    └── NumeroCuentaDuplicadoException        → HTTP 409
```

---

## Scripts de inicialización — BaseDatos.sql

### customers-service/src/main/resources/BaseDatos.sql

```sql
CREATE TABLE IF NOT EXISTS persona (
    id              BIGINT      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre          VARCHAR(100) NOT NULL,
    genero          VARCHAR(10)  NOT NULL CHECK (genero IN ('MASCULINO', 'FEMENINO')),
    edad            INTEGER      NOT NULL CHECK (edad >= 18),
    identificacion  VARCHAR(10)  NOT NULL UNIQUE,
    direccion       VARCHAR(200) NOT NULL,
    telefono        VARCHAR(20)  NOT NULL
);

CREATE TABLE IF NOT EXISTS cliente (
    id          BIGINT       PRIMARY KEY REFERENCES persona(id),
    contrasena  VARCHAR(255) NOT NULL,
    estado      VARCHAR(10)  NOT NULL DEFAULT 'activo' CHECK (estado IN ('activo', 'inactivo'))
);

INSERT INTO persona (nombre, genero, edad, identificacion, direccion, telefono)
VALUES ('Jose Lema', 'MASCULINO', 30, '1713175071', 'Otavalo sn y principal', '+593991234567');

INSERT INTO cliente (id, contrasena, estado)
VALUES (currval('persona_id_seq'), '$2a$10$abcdefghijklmnopqrstuuVwxyzABCDEFGHIJ1234567890ABCDE', 'activo');

INSERT INTO persona (nombre, genero, edad, identificacion, direccion, telefono)
VALUES ('Mariana Montalvo', 'FEMENINO', 25, '0650789428', 'Amazonas y NNUU', '+593997654321');

INSERT INTO cliente (id, contrasena, estado)
VALUES (currval('persona_id_seq'), '$2a$10$abcdefghijklmnopqrstuuVwxyzABCDEFGHIJ1234567890ABCDE', 'activo');
```

### accounts-service/src/main/resources/BaseDatos.sql

```sql
CREATE TABLE IF NOT EXISTS cliente_proyeccion (
    cliente_id  BIGINT       PRIMARY KEY,
    nombre      VARCHAR(100) NOT NULL,
    estado      VARCHAR(10)  NOT NULL DEFAULT 'activo' CHECK (estado IN ('activo', 'inactivo'))
);

CREATE TABLE IF NOT EXISTS cuenta (
    id               BIGINT          GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    numero_cuenta    VARCHAR(20)     NOT NULL UNIQUE,
    tipo             VARCHAR(10)     NOT NULL CHECK (tipo IN ('ahorro', 'corriente', 'digital')),
    saldo_inicial    NUMERIC(15, 2)  NOT NULL CHECK (saldo_inicial >= 0),
    saldo_disponible NUMERIC(15, 2)  NOT NULL,
    estado           VARCHAR(10)     NOT NULL DEFAULT 'activa' CHECK (estado IN ('activa', 'inactiva')),
    cliente_id       BIGINT          NOT NULL REFERENCES cliente_proyeccion(cliente_id)
);

CREATE TABLE IF NOT EXISTS movimiento (
    id                   BIGINT          GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    fecha                TIMESTAMP       NOT NULL DEFAULT NOW(),
    tipo                 VARCHAR(12)     NOT NULL CHECK (tipo IN ('deposito', 'retiro', 'ajuste', 'reversion')),
    valor                NUMERIC(15, 2)  NOT NULL CHECK (valor <> 0),
    saldo_resultante     NUMERIC(15, 2)  NOT NULL,
    cuenta_id            BIGINT          NOT NULL REFERENCES cuenta(id),
    movimiento_origen_id BIGINT          REFERENCES movimiento(id)
);

INSERT INTO cliente_proyeccion (cliente_id, nombre, estado)
VALUES (1, 'Jose Lema', 'activo'),
       (2, 'Mariana Montalvo', 'activo');

INSERT INTO cuenta (numero_cuenta, tipo, saldo_inicial, saldo_disponible, estado, cliente_id)
VALUES ('478758', 'ahorro', 2000.00, 2000.00, 'activa', 1),
       ('225487', 'corriente', 100.00, 100.00, 'activa', 2),
       ('495878', 'ahorro', 0.00, 0.00, 'activa', 2),
       ('496825', 'ahorro', 540.00, 540.00, 'activa', 2),
       ('585545', 'corriente', 1000.00, 1000.00, 'activa', 1);
```
