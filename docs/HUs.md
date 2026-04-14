De acuerdo. Basado en la información proporcionada, he consolidado todas las reglas de negocio y validaciones en un conjunto final y refinado de historias de usuario. Este conjunto está diseñado para ser claro, testeable y estar listo para ser incorporado en un sprint de desarrollo.

A continuación, se presentan las historias de usuario finales para el sistema bancario de microservicios.

---

### **ÉPICA 1 — Gestión de Personas y Clientes**
*Pertenece al microservicio: `customers-service`*

#### **HU-01 — Crear cliente**
*   **Como** consumidor de la API,
*   **quiero** registrar un nuevo cliente en el sistema con validaciones de datos específicas,
*   **para que** solo se creen perfiles válidos y completos que puedan ser asociados a productos bancarios.

**Criterios de Aceptación:**
*   **CA-01.1 — Registro exitoso**
    *   **Dado** que envío un `POST /clientes` con todos los campos obligatorios (nombre, género, edad, identificación, dirección, teléfono, contraseña, estado).
    *   **Y** la `edad` es mayor o igual a 18.
    *   **Y** la `identificación` cumple con el formato de cédula ecuatoriana.
    *   **Y** el `teléfono` incluye un prefijo de país válido.
    *   **Y** la `contraseña` tiene al menos 8 caracteres, una mayúscula y es alfanumérica.
    *   **Y** el `género` es 'MASCULINO' o 'FEMENINO'.
    *   **Cuando** el sistema procesa la solicitud.
    *   **Entonces** debe persistir el registro, devolver HTTP 201 y el recurso creado.

*   **CA-01.2 — Identificación duplicada**
    *   **Dado** que envío un `POST /clientes` con una `identificación` que ya existe.
    *   **Cuando** el sistema valida el registro.
    *   **Entonces** debe devolver HTTP 409 con un mensaje de error descriptivo.

*   **CA-01.3 — Datos inválidos o ausentes**
    *   **Dado** que envío un `POST /clientes` con datos que no cumplen alguna de las validaciones (edad, formato de identificación/teléfono, política de contraseña, campos nulos).
    *   **Cuando** el sistema valida la solicitud.
    *   **Entonces** debe devolver HTTP 400 indicando el campo y el motivo del error.

#### **HU-02 — Consultar cliente**
*   **Como** consumidor de la API,
*   **quiero** consultar los datos de un cliente por su identificador,
*   **para que** pueda verificar su información registrada.

**Criterios de Aceptación:**
*   **CA-02.1 — Cliente existente**
    *   **Dado** que realizo un `GET /clientes/{id}` con un ID válido y existente.
    *   **Cuando** el sistema procesa la solicitud.
    *   **Entonces** debe devolver HTTP 200 con los datos completos del cliente.

*   **CA-02.2 — Cliente inexistente**
    *   **Dado** que realizo un `GET /clientes/{id}` con un ID que no existe.
    *   **Cuando** el sistema busca el recurso.
    *   **Entonces** debe devolver HTTP 404.

#### **HU-03 — Actualizar cliente**
*   **Como** consumidor de la API,
*   **quiero** actualizar los datos de un cliente existente,
*   **para que** la información del sistema se mantenga vigente.

**Criterios de Aceptación:**
*   **CA-03.1 — Actualización exitosa**
    *   **Dado** que realizo un `PUT /clientes/{id}` con datos válidos sobre un cliente existente.
    *   **Cuando** el sistema procesa la solicitud.
    *   **Entonces** debe actualizar los campos enviados, persistir los cambios y devolver HTTP 200 con el recurso actualizado.

*   **CA-03.2 — Cliente inexistente**
    *   **Dado** que realizo un `PUT /clientes/{id}` con un ID que no existe.
    *   **Cuando** el sistema intenta localizar el recurso.
    *   **Entonces** debe devolver HTTP 404.

*   **CA-03.3 — Intento de modificar identificación a una duplicada**
    *   **Dado** que realizo un `PUT /clientes/{id}` intentando cambiar la `identificación` a una ya registrada por otro cliente.
    *   **Cuando** el sistema valida la unicidad.
    *   **Entonces** debe devolver HTTP 409 sin aplicar cambios.

#### **HU-04 — Eliminar cliente (lógicamente)**
*   **Como** consumidor de la API,
*   **quiero** desactivar un cliente del sistema,
*   **para que** los registros inactivos puedan ser gestionados sin perder la integridad de los datos.

**Criterios de Aceptación:**
*   **CA-04.1 — Eliminación lógica exitosa**
    *   **Dado** que realizo un `DELETE /clientes/{id}` sobre un cliente que no tiene cuentas activas.
    *   **Cuando** el sistema procesa la solicitud.
    *   **Entonces** debe cambiar el `estado` del cliente a 'inactivo' y devolver HTTP 200.

*   **CA-04.2 — Cliente inexistente**
    *   **Dado** que realizo un `DELETE /clientes/{id}` con un ID que no existe.
    *   **Cuando** el sistema intenta localizar el recurso.
    *   **Entonces** debe devolver HTTP 404.

*   **CA-04.3 — Intento de eliminar cliente con cuentas activas**
    *   **Dado** que realizo un `DELETE /clientes/{id}` sobre un cliente que tiene al menos una cuenta activa.
    *   **Cuando** el sistema valida la precondición.
    *   **Entonces** debe devolver HTTP 409 con el mensaje "El cliente posee cuentas activas que deben ser desactivadas primero".

---

### **ÉPICA 2 — Gestión de Cuentas**
*Pertenece al microservicio: `accounts-service`*

#### **HU-05 — Crear cuenta**
*   **Como** consumidor de la API,
*   **quiero** registrar una nueva cuenta bancaria asociada a un cliente,
*   **para que** el cliente pueda operar transacciones sobre ella.

**Criterios de Aceptación:**
*   **CA-05.1 — Registro exitoso**
    *   **Dado** que realizo un `POST /cuentas` con `tipo` ('ahorro', 'corriente' o 'digital'), `saldoInicial` válido, `estado` y un `clienteId` existente.
    *   **Cuando** el sistema procesa la solicitud.
    *   **Entonces** debe persistir la cuenta y devolver HTTP 201.

*   **CA-05.2 — Cliente no existe**
    *   **Dado** que realizo un `POST /cuentas` con un `clienteId` que no existe.
    *   **Cuando** el sistema valida la existencia del cliente.
    *   **Entonces** debe devolver HTTP 422 indicando que el cliente referenciado no existe.

*   **CA-05.3 — Número de cuenta duplicado**
    *   **Dado** que realizo un `POST /cuentas` con un `numero de cuenta` ya registrado.
    *   **Cuando** el sistema valida la unicidad.
    *   **Entonces** debe devolver HTTP 409.

*   **CA-05.4 — Saldo inicial negativo**
    *   **Dado** que realizo un `POST /cuentas` con un `saldoInicial` menor a cero.
    *   **Cuando** el sistema valida los datos.
    *   **Entonces** debe devolver HTTP 400.

*   **CA-05.5 — Saldo inicial insuficiente para cuenta corriente**
    *   **Dado** que realizo un `POST /cuentas` para una cuenta de tipo 'corriente' con un `saldoInicial` menor a $50.
    *   **Cuando** el sistema valida la regla de negocio.
    *   **Entonces** debe devolver HTTP 400 con un mensaje indicando el saldo mínimo requerido.

#### **HU-06, HU-07, HU-08... (y el resto de historias refinadas)**

(Se omiten las historias que no sufrieron cambios en esta última iteración para brevedad, como **HU-06, HU-07, HU-10, HU-13, HU-14, HU-15, HU-16, HU-17**, ya que su refinamiento anterior sigue siendo válido. A continuación, se incluyen las que sí tuvieron ajustes o fueron creadas).

#### **HU-08 — Eliminar cuenta (lógicamente)**
*   **Como** consumidor de la API,
*   **quiero** desactivar una cuenta bancaria,
*   **para que** las cuentas obsoletas puedan ser removidas de las operaciones activas.

**Criterios de Aceptación:**
*   **CA-08.1 — Eliminación lógica exitosa**
    *   **Dado** que realizo un `DELETE /cuentas/{id}` sobre una cuenta cuyo último movimiento fue hace más de un año.
    *   **Cuando** el sistema procesa la solicitud.
    *   **Entonces** debe cambiar el `estado` de la cuenta a 'inactiva' y devolver HTTP 200.

*   **CA-08.2 — Cuenta inexistente**
    *   **Dado** que realizo un `DELETE /cuentas/{id}` con un ID inexistente.
    *   **Cuando** el sistema busca el recurso.
    *   **Entonces** debe devolver HTTP 404.

*   **CA-08.3 — Intento de eliminar cuenta con actividad reciente**
    *   **Dado** que realizo un `DELETE /cuentas/{id}` sobre una cuenta con movimientos en el último año.
    *   **Cuando** el sistema valida la precondición.
    *   **Entonces** debe devolver HTTP 409 con un mensaje indicando que la cuenta no puede ser eliminada.

---

### **ÉPICA 3 — Gestión de Movimientos**
*Pertenece al microservicio: `accounts-service`*

#### **HU-09 — Registrar movimiento**
*   **Como** consumidor de la API,
*   **quiero** registrar un movimiento (depósito o retiro) sobre una cuenta,
*   **para que** el historial transaccional y el saldo se mantengan actualizados.

**Criterios de Aceptación:**
*   **CA-09.1 — Depósito exitoso**
    *   **Dado** que realizo un `POST /movimientos` con un `valor` positivo sobre una cuenta activa.
    *   **Cuando** el sistema procesa la transacción.
    *   **Entonces** debe registrar el movimiento, incrementar el saldo y devolver HTTP 201.

*   **CA-09.2 — Retiro exitoso**
    *   **Dado** que realizo un `POST /movimientos` con un `valor` negativo sobre una cuenta activa con saldo suficiente y dentro del límite diario.
    *   **Cuando** el sistema procesa la transacción.
    *   **Entonces** debe registrar el movimiento, decrementar el saldo y devolver HTTP 201.

*   **CA-09.3 — Saldo insuficiente**
    *   **Dado** que realizo un `POST /movimientos` con un `valor` negativo que supera el saldo disponible.
    *   **Cuando** el sistema valida la operación.
    *   **Entonces** debe devolver HTTP 422 con el mensaje "Saldo no disponible".

*   **CA-09.4 — Cuenta inexistente o inactiva**
    *   **Dado** que realizo un `POST /movimientos` sobre una cuenta que no existe o está inactiva.
    *   **Cuando** el sistema valida la cuenta.
    *   **Entonces** debe devolver HTTP 422 con un mensaje descriptivo.

*   **CA-09.5 — Movimiento con valor cero**
    *   **Dado** que realizo un `POST /movimientos` con un `valor` de cero.
    *   **Cuando** el sistema valida los datos.
    *   **Entonces** debe devolver HTTP 400 con el mensaje "El valor del movimiento no puede ser cero".

#### **HU-09.1 — Aplicar límite de retiro diario (Nueva)**
*   **Como** sistema,
*   **quiero** validar que la suma de retiros de un cliente no exceda el límite diario de $500,
*   **para que** se cumplan las políticas de seguridad y riesgo del banco.

**Criterios de Aceptación:**
*   **CA-09.1.1 — Retiro excede el límite diario**
    *   **Dado** que un cliente ha acumulado $450 en retiros durante el día.
    *   **Cuando** intenta realizar un nuevo retiro de $51 o más.
    *   **Entonces** el sistema debe rechazar la transacción y devolver HTTP 422 con el mensaje "Límite de retiro diario excedido".

*   **CA-09.1.2 — Depósitos no afectan el límite**
    *   **Dado** que un cliente ha acumulado $400 en retiros.
    *   **Cuando** realiza un depósito de cualquier cantidad.
    *   **Entonces** su límite de retiro disponible para el día ($100 restantes) no se modifica.

#### **HU-11.R — Registrar movimiento de ajuste (Reemplaza HU-11)**
*   **Como** consumidor de la API,
*   **quiero** registrar un movimiento de ajuste que modifique el efecto de una transacción original,
*   **para que** los errores sean corregidos de forma auditable.

**Criterios de Aceptación:**
*   **CA-11.R.1 — Ajuste exitoso**
    *   **Dado** que realizo un `POST /ajustes` referenciando un `movimientoId` original con un valor y justificación.
    *   **Cuando** el sistema procesa la solicitud.
    *   **Entonces** debe crear un nuevo movimiento de tipo 'ajuste', actualizar el saldo y devolver HTTP 201.

#### **HU-12.R — Registrar movimiento de reversión (Reemplaza HU-12)**
*   **Como** consumidor de la API,
*   **quiero** registrar una reversión completa de una transacción,
*   **para que** una operación errónea sea anulada manteniendo la trazabilidad.

**Criterios de Aceptación:**
*   **CA-12.R.1 — Reversión exitosa**
    *   **Dado** que realizo un `POST /reversiones` con el `movimientoId` de una transacción existente.
    *   **Cuando** el sistema procesa la solicitud.
    *   **Entonces** debe crear un nuevo movimiento de tipo 'reversion' con el valor opuesto al original, anular el efecto en el saldo y devolver HTTP 201.