CREATE TABLE IF NOT EXISTS cliente_proyeccion (
    cliente_id BIGINT       PRIMARY KEY,
    nombre     VARCHAR(100) NOT NULL,
    estado     VARCHAR(10)  NOT NULL DEFAULT 'ACTIVO'
               CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);

CREATE TABLE IF NOT EXISTS cuenta (
    id               BIGINT         GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    numero_cuenta    VARCHAR(20)    NOT NULL UNIQUE,
    tipo             VARCHAR(10)    NOT NULL
                     CHECK (tipo IN ('AHORRO', 'CORRIENTE', 'DIGITAL')),
    saldo_inicial    NUMERIC(15, 2) NOT NULL
                     CHECK (saldo_inicial >= 0),
    saldo_disponible NUMERIC(15, 2) NOT NULL,
    estado           VARCHAR(10)    NOT NULL DEFAULT 'ACTIVA'
                     CHECK (estado IN ('ACTIVA', 'INACTIVA')),
    cliente_id       BIGINT         NOT NULL
                     REFERENCES cliente_proyeccion(cliente_id)
);

CREATE TABLE IF NOT EXISTS movimiento (
    id                   BIGINT         GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    fecha                TIMESTAMP      NOT NULL DEFAULT NOW(),
    tipo                 VARCHAR(12)    NOT NULL
                         CHECK (tipo IN ('DEPOSITO', 'RETIRO', 'AJUSTE', 'REVERSION')),
    valor                NUMERIC(15, 2) NOT NULL CHECK (valor <> 0),
    saldo_resultante     NUMERIC(15, 2) NOT NULL,
    cuenta_id            BIGINT         NOT NULL REFERENCES cuenta(id),
    movimiento_origen_id BIGINT         REFERENCES movimiento(id),
    justificacion        VARCHAR(500)   NULL
);

INSERT INTO cliente_proyeccion (cliente_id, nombre, estado)
VALUES (1, 'Jose Lema',         'ACTIVO'),
       (2, 'Mariana Montalvo',  'ACTIVO')
ON CONFLICT (cliente_id) DO NOTHING;

INSERT INTO cuenta (numero_cuenta, tipo, saldo_inicial, saldo_disponible, estado, cliente_id)
VALUES ('478758', 'AHORRO',    2000.00, 2000.00, 'ACTIVA', 1),
       ('225487', 'CORRIENTE',  100.00,  100.00, 'ACTIVA', 2),
       ('495878', 'AHORRO',       0.00,    0.00, 'ACTIVA', 2),
       ('496825', 'AHORRO',     540.00,  540.00, 'ACTIVA', 2),
       ('585545', 'CORRIENTE', 1000.00, 1000.00, 'ACTIVA', 1)
ON CONFLICT (numero_cuenta) DO NOTHING;
