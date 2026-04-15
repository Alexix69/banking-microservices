# Messaging Contract — RabbitMQ Events

**Protocol**: AMQP 0-9-1
**Broker**: RabbitMQ 3.12
**Producer**: customers-service
**Consumer**: accounts-service

---

## Topología

```
customers-service
      │
      │ publish
      ▼
Exchange: cliente.events  (type: topic, durable: true, auto-delete: false)
      │
      ├─ routing key: cliente.created ──► Queue: accounts.cliente.created
      │                                       (durable: true, exclusive: false, auto-delete: false)
      │                                       x-dead-letter-exchange: cliente.events.dlx
      │                                       x-dead-letter-routing-key: dead
      │                                             │
      │                                             ▼ consume (manual ack)
      │                                       accounts-service → persiste en cliente_proyeccion
      │
      └─ routing key: cliente.desactivado ──► Queue: accounts.cliente.desactivado
                                              (durable: true, exclusive: false, auto-delete: false)
                                              x-dead-letter-exchange: cliente.events.dlx
                                              x-dead-letter-routing-key: dead
                                                    │
                                                    ▼ consume (manual ack)
                                              accounts-service → actualiza cliente_proyeccion.estado

Exchange: cliente.events.dlx  (type: direct, durable: true)
      └─ routing key: dead ──► Queue: accounts.cliente.dlq  (durable: true)
```

---

## ClienteCreatedEvent

Publicado por customers-service inmediatamente después de que la transacción de creación de cliente se confirma en `db_customers`.

### Routing key

```
cliente.created
```

### Payload (JSON serializado)

```json
{
  "clienteId": 1,
  "nombre": "Jose Lema",
  "estado": "ACTIVO"
}
```

| Campo | Tipo JSON | Descripción |
|---|---|---|
| `clienteId` | Number (Long) | ID interno del cliente en customers-service |
| `nombre` | String | Nombre completo del cliente |
| `estado` | String | Reservado. Siempre serializa como `"ACTIVO"`. El consumer ignora este campo y establece el estado como `ACTIVO` por defecto al crear la proyección local. |

### AMQP Message Properties

| Propiedad | Valor |
|---|---|
| `contentType` | `application/json` |
| `deliveryMode` | `2` (persistent) |
| `messageId` | UUID generado por el producer |
| `timestamp` | epoch seconds en el momento de publicación |

### Acción en accounts-service

```sql
INSERT INTO cliente_proyeccion (cliente_id, nombre, estado)
VALUES (:clienteId, :nombre, 'activo')
ON CONFLICT (cliente_id) DO UPDATE
    SET nombre = EXCLUDED.nombre,
        estado = EXCLUDED.estado;
```

> **Nota**: el campo `estado` del payload no se usa en el upsert. La proyección siempre se crea o actualiza como `ACTIVO` al recibir este evento. `ClienteDesactivadoEvent` es el único que modifica el estado a `INACTIVO`.

---

## ClienteDesactivadoEvent

Publicado por customers-service inmediatamente después de que la transacción de desactivación lógica del cliente se confirma en `db_customers`.

### Routing key

```
cliente.desactivado
```

### Payload (JSON serializado)

```json
{
  "clienteId": 1
}
```

| Campo | Tipo JSON | Descripción |
|---|---|---|
| `clienteId` | Number (Long) | ID interno del cliente desactivado |

### Acción en accounts-service

```sql
UPDATE cliente_proyeccion
SET estado = 'inactivo'
WHERE cliente_id = :clienteId;
```

---

## Manejo de errores en el consumer

| Escenario | Acción |
|---|---|
| Procesamiento exitoso | `channel.basicAck(deliveryTag, false)` |
| Excepción de negocio irrecuperable (constraint violation, cliente ya inactivo) | `channel.basicNack(deliveryTag, false, false)` → mensaje va a DLQ |
| Excepción temporal (BD no disponible) | Propagar excepción → Spring AMQP reencola según política configurada |

### Dead Letter Queue

Los mensajes en `accounts.cliente.dlq` requieren intervención manual: verificar el cause del error, corregir el estado si aplica, y reenviar publicando manualmente al exchange correcto con el routing key original.

---

## Configuración en Spring AMQP

### customers-service — RabbitMQConfig.java

```java
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_CLIENTE = "cliente.events";

    @Bean
    public TopicExchange clienteExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE_CLIENTE).durable(true).build();
    }
}
```

### accounts-service — RabbitMQConfig.java

```java
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_CLIENTE = "cliente.events";
    public static final String EXCHANGE_DLX = "cliente.events.dlx";
    public static final String QUEUE_CLIENTE_CREATED = "accounts.cliente.created";
    public static final String QUEUE_CLIENTE_DESACTIVADO = "accounts.cliente.desactivado";
    public static final String QUEUE_DLQ = "accounts.cliente.dlq";
    public static final String ROUTING_CREATED = "cliente.created";
    public static final String ROUTING_DESACTIVADO = "cliente.desactivado";

    @Bean
    public TopicExchange clienteExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE_CLIENTE).durable(true).build();
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return ExchangeBuilder.directExchange(EXCHANGE_DLX).durable(true).build();
    }

    @Bean
    public Queue clienteCreatedQueue() {
        return QueueBuilder.durable(QUEUE_CLIENTE_CREATED)
                .withArgument("x-dead-letter-exchange", EXCHANGE_DLX)
                .withArgument("x-dead-letter-routing-key", "dead")
                .build();
    }

    @Bean
    public Queue clienteDesactivadoQueue() {
        return QueueBuilder.durable(QUEUE_CLIENTE_DESACTIVADO)
                .withArgument("x-dead-letter-exchange", EXCHANGE_DLX)
                .withArgument("x-dead-letter-routing-key", "dead")
                .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(QUEUE_DLQ).build();
    }

    @Bean
    public Binding clienteCreatedBinding(Queue clienteCreatedQueue, TopicExchange clienteExchange) {
        return BindingBuilder.bind(clienteCreatedQueue).to(clienteExchange).with(ROUTING_CREATED);
    }

    @Bean
    public Binding clienteDesactivadoBinding(Queue clienteDesactivadoQueue, TopicExchange clienteExchange) {
        return BindingBuilder.bind(clienteDesactivadoQueue).to(clienteExchange).with(ROUTING_DESACTIVADO);
    }

    @Bean
    public Binding dlqBinding(Queue deadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with("dead");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
```
