# Research — Sistema Bancario Microservicios

**Phase**: 0 — Resolución de incógnitas previas al diseño
**Feature**: [spec.md](spec.md) | **Plan**: [plan.md](plan.md)
**Date**: 2026-04-14

---

## R-01: Algoritmo de validación de cédula ecuatoriana (módulo 10)

**Decision**: Implementar el algoritmo oficial del Registro Civil ecuatoriano, que es una variante de módulo 10.

**Algorithm**:

1. La cédula tiene exactamente 10 dígitos.
2. Los dos primeros dígitos representan la provincia (01–24). Valores > 24 son inválidos.
3. El tercer dígito debe ser < 6 (para personas naturales; el caso de RUC no aplica en este dominio).
4. Se toman los primeros 9 dígitos. A los dígitos en posición impar (1, 3, 5, 7, 9) se les multiplica por 2; si el resultado es ≥ 10, se resta 9.
5. Se suman todos los valores resultantes.
6. Se calcula el verificador: `(10 - (suma % 10)) % 10`.
7. El verificador calculado debe coincidir con el décimo dígito.

**Rationale**: Es el único algoritmo oficialmente documentado para cédulas ecuatorianas. Rechaza cadenas aleatorias de 10 dígitos con alta tasa de detección.

**Alternatives considered**:
- Solo verificar longitud y que sean dígitos: insuficiente, deja pasar identificaciones inválidas.
- Expresión regular de formato: misma limitación, no detecta dígito verificador incorrecto.

**Test cases** (cédulas válidas conocidas para pruebas unitarias):
- `1713175071` — válida
- `0102030405` — inválida (provincia 01, pero verificador incorrecto)
- `1234567890` — inválida
- `9999999999` — inválida (provincia > 24)
- `0650789428` — válida (formato correcto de provincia 06)

---

## R-02: Cálculo del límite de retiro diario de $500

**Decision**: El límite se calcula como la suma de los valores absolutos de todos los movimientos de tipo `retiro` cuya `fecha` (columna TIMESTAMP) sea del día calendario actual del servidor, agrupados por `clienteId` (es decir, sumando retiros de todas las cuentas del cliente, no por cuenta).

**Rationale**: La spec dice "acumulado por cliente". El ADR confirma que el límite es una regla de negocio del cliente, no de la cuenta. Usar `fecha::date = CURRENT_DATE` en PostgreSQL es la forma canónica de comparar por día.

**Query pattern**:

```sql
SELECT COALESCE(SUM(ABS(m.valor)), 0)
FROM movimiento m
INNER JOIN cuenta c ON m.cuenta_id = c.id
WHERE c.cliente_id = :clienteId
  AND m.tipo = 'retiro'
  AND m.fecha::date = CURRENT_DATE
FOR UPDATE
```

**Concurrency note**: El `FOR UPDATE` previene condiciones de carrera cuando dos retiros se procesan concurrentemente para el mismo cliente. Soportado por PostgreSQL 15.

**Alternatives considered**:
- Límite por cuenta: rechazado; la spec dice "por cliente".
- Calcular en memoria sumando movimientos del día: no hilo-seguro sin bloqueo de BD.

---

## R-03: Topología RabbitMQ — Topic Exchange con Dead Letter Queue

**Decision**: Topic Exchange `cliente.events` con dos queues durables y una DLQ centralizada.

**Topology**:

```
Exchange: cliente.events  (type: topic, durable: true)
  ├── Binding: cliente.created  →  Queue: accounts.cliente.created  (durable, x-dead-letter-exchange: cliente.events.dlx)
  └── Binding: cliente.deleted  →  Queue: accounts.cliente.deleted  (durable, x-dead-letter-exchange: cliente.events.dlx)

Exchange: cliente.events.dlx  (type: direct, durable: true)
  └── Binding: dead        →  Queue: accounts.cliente.dlq  (durable)
```

**Message durability**: `durable: true` en queues + `MessageDeliveryMode.PERSISTENT` en mensajes. Sobreviven reinicios de RabbitMQ.

**Acknowledgement**: Manual (`AcknowledgeMode.MANUAL`). El consumer hace `channel.basicAck` solo después de persistir exitosamente en PostgreSQL. Si la persistencia falla, `channel.basicNack(deliveryTag, false, false)` redirige a DLQ sin requeue.

**Retry policy**: Sin reintentos automáticos. Un fallo → DLQ directamente. Requeue podría causar bucles infinitos si el error es permanente (por ejemplo, constraint violation).

**Rationale**: Esta topología permite adicionar nuevos consumers (audit-service, notif-service) sin modificar el productor, y garantiza que mensajes no procesables no bloqueen la cola principal.

**Alternatives considered**:
- Fanout exchange: no permite routing por tipo de evento.
- Direct exchange: sí permite routing pero no soporte para wildcard `cliente.*` en futuros consumers.
- Reintentos con backoff: añade complejidad no justificada para el scope actual.

---

## R-04: Estrategia InheritanceType.JOINED para Persona → Cliente

**Decision**: `@Inheritance(strategy = InheritanceType.JOINED)` en `Persona`. Cliente hereda con su propia tabla con FK a `persona.id`.

**Schema resultante**:
```sql
persona (id PK, nombre, genero, edad, identificacion, direccion, telefono)
cliente (id PK FK→persona.id, contrasena, estado)
```

**Rationale**: Normalización correcta, sin columnas nulas, refleja el modelo relacional limpiamente. Permite consultas polimórficas vía JOIN. Consistente con el principio de Liskov: `Cliente` puede usarse donde se espera `Persona`.

**Alternatives considered**:
- `SINGLE_TABLE`: una sola tabla con columnas nulas para atributos específicos de Cliente. Rompe las constraints `NOT NULL` del dominio.
- `TABLE_PER_CLASS`: duplica las columnas de Persona en `cliente`. Inconsistencia si Persona cambia.

---

## R-05: Publicación de eventos post-transacción

**Decision**: Usar `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)` para publicar eventos de dominio a RabbitMQ exclusivamente después de que la transacción de BD se confirme.

**Flow**:

```
ClienteService.crearCliente()
  └─ @Transactional
       ├─ Cliente.create()                      → registra ClienteCreatedEvent
       ├─ clienteRepository.save(cliente)        → INSERT en PostgreSQL
       └─ applicationEventPublisher.publishEvent(event)
            └─ @TransactionalEventListener AFTER_COMMIT
                 └─ rabbitMQEventPublisher.publish(event)  → RabbitMQ
```

**Rationale**: Si el `save` falla y se hace rollback, el evento NO se publica a RabbitMQ. Si el `save` succeeds pero RabbitMQ está caído, la transacción ya se confirmó y accounts-service recibirá el evento cuando RabbitMQ se recupere (RabbitMQ persistirá si el exchange está durable). Si RabbitMQ está completamente inaccesible, el evento se pierde — esta es la limitación conocida del enfoque sin Outbox Pattern. Para el scope actual, es aceptable.

**Alternatives considered**:
- Transactional Outbox Pattern: escribe el evento en una tabla pendiente dentro de la misma transacción; un proceso separado lo publica. Mayor resiliencia, pero añade infraestructura (polling / CDC con Debezium). Fuera del scope actual.

---

## R-06: Idempotencia del consumer en accounts-service

**Decision**: El consumer usa `INSERT ... ON CONFLICT (cliente_id) DO UPDATE SET nombre = EXCLUDED.nombre, estado = EXCLUDED.estado` para `ClienteCreatedEvent`, y `UPDATE cliente_proyeccion SET estado = 'inactivo' WHERE cliente_id = :id` para `ClienteDeletedEvent`.

**Rationale**: RabbitMQ garantiza at-least-once delivery. Un mismo evento puede llegar dos veces (por redelivery tras un nack o por reconexión). La operación de upsert hace que el resultado sea el mismo independientemente de cuántas veces se aplique.

**Alternatives considered**:
- Verificar existencia antes de insertar (SELECT + INSERT): introduce condición de carrera en entornos concurrentes.
- Tabla de eventos procesados: overhead innecesario para el scope actual.

---

## R-07: Eliminación de cuenta con actividad en el último año

**Decision**: "Último año" = 365 días anteriores a la fecha actual del servidor. Query: `EXISTS (SELECT 1 FROM movimiento WHERE cuenta_id = :id AND fecha >= NOW() - INTERVAL '1 year')`.

**Rationale**: La spec dice "movimientos en el último año". 365 días es la interpretación estándar de "un año" en contextos financieros donde no se especifica año calendario.

**Alternatives considered**:
- Año calendario (1 enero – 31 diciembre): ambiguo entre cambios de año.
- Períodos variables por producto bancario: fuera del scope.

---

## R-08: Reversión de movimiento

**Decision**: La reversión crea un nuevo `Movimiento` con:
- `tipo = 'reversion'`
- `valor = -1 * movimientoOriginal.valor`
- `movimientoOrigenId = movimientoOriginal.id`
- El `saldoResultante` se calcula como `cuentaActual.saldoDisponible + (-1 * movimientoOriginal.valor)`.

El movimiento original no se modifica. La cuenta actualiza su `saldoDisponible`.

**Rationale**: Trazabilidad completa del historial. El reporte muestra ambos movimientos. Consistente con la auditabilidad requerida.

**Edge case**: Si el movimiento original fue un retiro de $100 y el saldo actual es $50, la reversión (que equivale a un depósito de $100) eleva el saldo a $150. Esto es correcto y esperado.
