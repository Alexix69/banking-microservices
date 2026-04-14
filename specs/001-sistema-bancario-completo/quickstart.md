# Quickstart — Sistema Bancario Microservicios

**Branch**: `001-sistema-bancario-completo`
**Date**: 2026-04-14

---

## Prerrequisitos

- Docker Engine 24+ y Docker Compose v2+ instalados en el host.
- Puertos 5432, 5433, 5672, 15672, 8080, 8081 disponibles.
- Sin dependencias adicionales (JDK, Maven, etc.) en el host.

---

## Levantar el stack completo

```bash
docker compose up
```

Ejecutar desde la raíz del repositorio. El comando construye las imágenes, levanta los cinco contenedores en el orden correcto y aplica los esquemas iniciales de base de datos.

El stack estará listo cuando todos los health checks pasen. Los logs muestran:

```
customers-service  | Started CustomersServiceApplication in X.XXX seconds
accounts-service   | Started AccountsServiceApplication in X.XXX seconds
```

---

## URLs de los servicios

| Servicio | URL |
|---|---|
| customers-service REST API | http://localhost:8080 |
| accounts-service REST API | http://localhost:8081 |
| RabbitMQ Management UI | http://localhost:15672 (guest / guest) |

---

## Flujo de prueba rápida con curl

### 1. Crear un cliente

```bash
curl -s -X POST http://localhost:8080/clientes \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Jose Lema",
    "genero": "MASCULINO",
    "edad": 30,
    "identificacion": "1713175071",
    "direccion": "Otavalo sn y principal",
    "telefono": "+593991234567",
    "contrasena": "1234.Abc",
    "estado": "activo"
  }'
```

Respuesta esperada: HTTP 201 con `clienteId` asignado.

### 2. Esperar la sincronización (consistencia eventual)

Esperar 1–2 segundos antes del siguiente paso. En ese tiempo, customers-service habrá publicado el evento `ClienteCreatedEvent` y accounts-service habrá actualizado su proyección local.

### 3. Crear una cuenta

```bash
curl -s -X POST http://localhost:8081/cuentas \
  -H "Content-Type: application/json" \
  -d '{
    "numeroCuenta": "478758",
    "tipo": "ahorro",
    "saldoInicial": 2000.00,
    "estado": "activa",
    "clienteId": 1
  }'
```

Respuesta esperada: HTTP 201 con `saldoDisponible: 2000.00`.

### 4. Registrar un retiro

```bash
curl -s -X POST http://localhost:8081/movimientos \
  -H "Content-Type: application/json" \
  -d '{
    "cuentaId": 1,
    "valor": -575.00
  }'
```

Respuesta esperada: HTTP 201 con `saldoResultante: 1425.00`.

### 5. Registrar un depósito

```bash
curl -s -X POST http://localhost:8081/movimientos \
  -H "Content-Type: application/json" \
  -d '{
    "cuentaId": 1,
    "valor": 500.00
  }'
```

Respuesta esperada: HTTP 201 con `saldoResultante: 1925.00`.

### 6. Consultar el reporte de estado de cuenta

```bash
curl -s "http://localhost:8081/reportes?clienteId=1&fechaInicio=2026-01-01&fechaFin=2026-12-31"
```

Respuesta esperada: HTTP 200 con array de movimientos para Jose Lema.

### 7. Intentar retiro con saldo insuficiente

```bash
curl -s -X POST http://localhost:8081/movimientos \
  -H "Content-Type: application/json" \
  -d '{
    "cuentaId": 1,
    "valor": -9999.00
  }'
```

Respuesta esperada: HTTP 422 con `"message": "Saldo no disponible"`.

### 8. Intentar eliminar cliente con cuenta activa

```bash
curl -s -X DELETE http://localhost:8080/clientes/1
```

Respuesta esperada: HTTP 409 con `"message": "El cliente posee cuentas activas que deben ser desactivadas primero"`.

---

## Detener el stack

```bash
docker compose down
```

Los datos persisten en los named volumes. Al ejecutar `docker compose up` nuevamente, los datos estarán disponibles.

Para destruir también los volúmenes (reset completo):

```bash
docker compose down -v
```

---

## Ejecutar las pruebas

### Pruebas unitarias (sin Docker, sin dependencias externas)

Desde el directorio de cada microservicio:

```bash
cd customers-service && ./mvnw test -Dtest="**/*Test" -DfailIfNoTests=false
cd accounts-service  && ./mvnw test -Dtest="**/*Test" -DfailIfNoTests=false
```

Las pruebas unitarias se ejecutan en milisegundos y no requieren que el stack esté levantado.

### Pruebas de integración (requieren Docker para Testcontainers)

```bash
cd customers-service && ./mvnw verify -Dtest="**/*IntegrationTest" -DfailIfNoTests=false
cd accounts-service  && ./mvnw verify -Dtest="**/*IntegrationTest" -DfailIfNoTests=false
```

Testcontainers levanta contenedores PostgreSQL y RabbitMQ automáticamente durante la ejecución y los destruye al terminar.

### Suite completa

```bash
cd customers-service && ./mvnw verify
cd accounts-service  && ./mvnw verify
```

---

## Notas sobre consistencia eventual

La creación de un cliente en customers-service propaga su existencia a accounts-service de forma **asincrónica**. Existe una ventana de tiempo (habitualmente < 1 segundo en condiciones normales) durante la cual el cliente no está disponible en la proyección local de accounts-service.

Flujo recomendado al trabajar con Postman o scripts de prueba:
1. Crear cliente → esperar HTTP 201.
2. Esperar 1 segundo.
3. Crear cuenta → la proyección ya estará sincronizada.

Si se recibe HTTP 422 con "El cliente referenciado no existe" inmediatamente después de crear un cliente, esperar un segundo y reintentar. Este es el comportamiento esperado bajo consistencia eventual.

---

## Variables de entorno relevantes

| Variable | Servicio | Descripción |
|---|---|---|
| `SPRING_DATASOURCE_URL` | ambos | URL JDBC de PostgreSQL |
| `SPRING_DATASOURCE_USERNAME` | ambos | Usuario de BD |
| `SPRING_DATASOURCE_PASSWORD` | ambos | Contraseña de BD |
| `SPRING_RABBITMQ_HOST` | ambos | Host del broker |
| `SPRING_RABBITMQ_PORT` | ambos | Puerto AMQP (5672) |
| `SPRING_RABBITMQ_USERNAME` | ambos | Usuario RabbitMQ |
| `SPRING_RABBITMQ_PASSWORD` | ambos | Contraseña RabbitMQ |

Todos los valores de desarrollo están definidos en `docker-compose.yml`. Para otros entornos, sobreescribir con variables de entorno del host.
