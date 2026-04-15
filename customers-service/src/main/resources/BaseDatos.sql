CREATE TABLE IF NOT EXISTS persona (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre         VARCHAR(100) NOT NULL,
    genero         VARCHAR(10)  NOT NULL CHECK (genero IN ('MASCULINO', 'FEMENINO')),
    edad           INTEGER      NOT NULL CHECK (edad >= 18),
    identificacion VARCHAR(10)  NOT NULL UNIQUE,
    direccion      VARCHAR(200) NOT NULL,
    telefono       VARCHAR(20)  NOT NULL
);

CREATE TABLE IF NOT EXISTS cliente (
    id         BIGINT       PRIMARY KEY REFERENCES persona(id),
    contrasena VARCHAR(255) NOT NULL,
    estado     VARCHAR(10)  NOT NULL DEFAULT 'ACTIVO' CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);

INSERT INTO persona (id, nombre, genero, edad, identificacion, direccion, telefono)
OVERRIDING SYSTEM VALUE
VALUES
    (1, 'Jose Lema',          'MASCULINO', 35, '1713175071', 'Guayaquil',  '098254678'),
    (2, 'Marianela Montalvo', 'FEMENINO',  33, '0650789428', 'Latacunga',  '097548965'),
    (3, 'Juan Osorio',        'MASCULINO', 28, '1700000001', 'Ambato',     '098745678');

INSERT INTO cliente (id, contrasena, estado)
VALUES
    (1, 'Joselem1',    'ACTIVO'),
    (2, 'Montalvo1',   'ACTIVO'),
    (3, 'Juanosori1',  'ACTIVO');
