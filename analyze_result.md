## Specification Analysis Report — TASK-02: Dominio customers-service

> **Artifacts analyzed**: constitution.md (v1.0.0) · data-model.md · research.md
> **Code analyzed**: 8 source files (6 production, 2 test)
> **Date**: 2026-04-14

---

### Findings Table

| ID | Dimensión | Severidad | Ubicación | Resumen | Impacto |
|---|---|---|---|---|---|
| A1 | Arquitectura Hexagonal | ❌ BLOQUEANTE | Persona.java L3–L12, L14–16 | 10 imports `jakarta.persistence.*` + `@Entity`, `@Table`, `@Inheritance` presentes en `domain/model/` | Violación directa de constitution §2.2: "Sin dependencias de Spring, JPA ni RabbitMQ" en el dominio |
| A2 | Arquitectura Hexagonal | ❌ BLOQUEANTE | Persona.java L19–39 | `@Id`, `@GeneratedValue`, `@Enumerated`, + 5 `@Column` en campos de `Persona` | Mismo principio: dominio no debe conocer detalles de persistencia |
| A3 | Arquitectura Hexagonal | ❌ BLOQUEANTE | Cliente.java L8–14, L19–21 | 6 imports `jakarta.persistence.*` + `@Entity`, `@Table`, `@PrimaryKeyJoinColumn` en `domain/model/` | Ídem A1 |
| A4 | Arquitectura Hexagonal | ❌ BLOQUEANTE | Cliente.java L24–31 | `@Column`, `@Enumerated`, `@Transient` en campos de `Cliente` | Ídem A1. `@Transient` sería correcto **solo si** se usa JPA, lo cual no debe existir en el dominio |
| B1 | Convención Enums | ❌ BLOQUEANTE | EstadoCliente.java L4 | `activo` → debe ser `ACTIVO` | Constitution §7: "Constantes: UPPER_SNAKE_CASE" |
| B2 | Convención Enums | ❌ BLOQUEANTE | EstadoCliente.java L5 | `inactivo` → debe ser `INACTIVO` | Ídem B1 |
| C1 | Encapsulación | ❌ BLOQUEANTE | Persona.java L63 | `public void setNombre(String nombre)` expone mutación directa | Constitution §2.2 + §8 (Repository Pattern): el estado cambia solo vía métodos de negocio |
| C2 | Encapsulación | ❌ BLOQUEANTE | Persona.java L71 | `public void setGenero(Genero genero)` | Ídem C1 |
| C3 | Encapsulación | ❌ BLOQUEANTE | Persona.java L79 | `public void setEdad(int edad)` | Ídem C1 |
| C4 | Encapsulación | ❌ BLOQUEANTE | Persona.java L87 | `public void setIdentificacion(String identificacion)` | Ídem C1 — además permite bypassear `validarIdentificacion()` |
| C5 | Encapsulación | ❌ BLOQUEANTE | Persona.java L95 | `public void setDireccion(String direccion)` | Ídem C1 |
| C6 | Encapsulación | ❌ BLOQUEANTE | Persona.java L103 | `public void setTelefono(String telefono)` | Ídem C1 |
| C7 | Encapsulación | ❌ BLOQUEANTE | Cliente.java L93 | `public void setContrasena(String contrasena)` — permite cambiar contraseña sin `validarContrasena()` | Ídem C1 — rompe invariante dominante |
| D1 | Cero Comentarios | ✅ CUMPLE | Todos los archivos | Sin comentarios de ningún tipo detectados | — |
| E1 | Algoritmo Cédula R-01 | ✅ CUMPLE | Cliente.java L101–118 | Implementación correcta: 10 dígitos, provincia 01–24, d[2]<6, multiplicadores alternos×2/×1, resta 9 si ≥10, verificador `(10-suma%10)%10` vs d[9] | — |
| F1 | Invariantes Dominio | ✅ CUMPLE | Cliente.java | `edad<18→EdadInvalidaException` («La edad debe ser mayor o igual a 18 años»), `identificacion→IdentificacionInvalidaException` («La identificación 'x' no es válida»), `contrasena→ContrasenaInvalidaException` descriptivo | — |
| G1 | Domain Events | ❌ BLOQUEANTE | Cliente.java L52 | `new ClienteCreatedEvent(null, nombre, "activo")` — `clienteId = null`. El evento se registra **antes** de la persistencia; JPA asignará el ID en `save()` pero el evento ya almacenado seguirá con `null`. Al publicarlo, accounts-service recibirá `clienteId = null` → violación de PK en `cliente_proyeccion` | Constitution §8 Domain Events + Research R-05 + data-model.md: `cliente_proyeccion.cliente_id` es PRIMARY KEY NOT NULL |
| G2 | Domain Events | ⚠️ ADVERTENCIA | ClienteCreatedEvent.java L7 | Campo `estado` tipado como `String` en lugar de `EstadoCliente` | Inconsistencia de tipos: permite valores arbitrarios; debería ser `EstadoCliente` enum |
| G3 | Domain Events | ✅ CUMPLE | Cliente.java L77–81 | `consumirEventos()` retorna copia defensiva (`new ArrayList<>(domainEvents)`) y limpia la lista | — |
| H1 | Tests sin Spring | ✅ CUMPLE | ClienteCreationTest.java · ClienteValidationTest.java | Solo imports `org.junit.jupiter.*` y clases del dominio. Sin `@SpringBootTest`, `@ExtendWith(SpringExtension)`, `@MockBean`, `@Autowired` | — |
| I1 | Consistencia data-model.md | ⚠️ ADVERTENCIA | data-model.md + EstadoCliente.java | data-model.md define los valores de `EstadoCliente` como `activo`/`inactivo` (minúscula) y el DDL usa `CHECK (estado IN ('activo', 'inactivo'))`. El código sigue el artefacto, pero ambos violan constitution §7. La inconsistencia está **en el artefacto upstream** y se propagó al código | Constitution §7 es autoridad; data-model.md y DDL deben corregirse junto con la implementación |
| I2 | Consistencia data-model.md | ✅ CUMPLE | Persona.java · Cliente.java | Todos los campos (nombres, tipos Java) coinciden con las tablas de entidades de data-model.md: `id(Long)`, `nombre(String)`, `genero(Genero)`, `edad(int)`, `identificacion(String)`, `direccion(String)`, `telefono(String)`, `contrasena(String)`, `estado(EstadoCliente)`, `domainEvents(List<DomainEvent>)` | — |
| J1 | Idioma del código | ⚠️ ADVERTENCIA | ClienteCreationTest.java · ClienteValidationTest.java | Nombres de métodos de test en español (`crearClienteConDatosValidosDebeCrearInstancia`, `identificacionConDigitoVerificadorIncorrectoCausaExcepcion`, etc.). Constitution §4 dice "Nombres de tests y métodos de test → en inglés". Nota: constitution §7 incluye ejemplos en español (`clienteConEdadMenorA18LanzaExcepcion()`), creando contradicción interna en el artefacto | Constitution §4 es la regla; §7 tiene ejemplos inconsistentes. Debe resolverse en constitution o normalizar a inglés |
| K1 | Cobertura TDD | ❌ BLOQUEANTE | Cliente.java L63–76 | Método `actualizarDatos()` sin ninguna prueba unitaria | Constitution §5: "Ningún método de negocio puede existir sin una prueba que lo cubra" |
| K2 | Cobertura TDD | ❌ BLOQUEANTE | Cliente.java L57 | Método `desactivar()` sin ninguna prueba unitaria | Constitution §5 — ídem K1 |

---

### Coverage Summary

| Método de Negocio | Tiene Test | Test IDs |
|---|---|---|
| `Cliente.create()` — datos válidos | ✅ | `ClienteCreationTest::crearClienteConDatosValidosDebeCrearInstancia` |
| `Cliente.create()` — edad < 18 | ✅ | `ClienteCreationTest::crearClienteConEdadMenorA18DebeLanzarEdadInvalidaException` |
| `Cliente.create()` — identificación inválida | ✅ | `ClienteCreationTest`, `ClienteValidationTest` (múltiples casos) |
| `Cliente.create()` — contraseña inválida | ✅ | `ClienteCreationTest`, `ClienteValidationTest` |
| `Cliente.create()` — registra event | ✅ | `ClienteCreationTest::crearClienteDebeRegistrarClienteCreatedEvent` |
| `Cliente.desactivar()` | ❌ | — |
| `Cliente.actualizarDatos()` | ❌ | — |
| `Cliente.consumirEventos()` | ✅ (parcial) | Cubierto dentro del test de eventos |

---

### Constitution Alignment Issues

| Principio | §Ref | Violación |
|---|---|---|
| Dominio sin dependencias de infraestructura | §2.2 | Persona.java y Cliente.java importan y usan `jakarta.persistence.*` — **12 violaciones concretas** |
| UPPER_SNAKE_CASE para constantes | §7 | `EstadoCliente.activo`, `EstadoCliente.inactivo` en minúscula |
| Estado cambia solo vía métodos de negocio | §2.2 + §8 | 7 setters públicos (`setNombre`, `setGenero`, `setEdad`, `setIdentificacion`, `setDireccion`, `setTelefono`, `setContrasena`) |
| TDD: ningún método sin prueba | §5 | `actualizarDatos()`, `desactivar()` sin tests |
| Domain Events after persistence | §8 + R-05 | `clienteId = null` en evento — el consumidor recibirá un ID nulo que viola PK de `cliente_proyeccion` |

---

### Inconsistencias en Artefactos Upstream

data-model.md define valores de `EstadoCliente` en minúscula (`activo`, `inactivo`) y el DDL SQL tiene `CHECK (estado IN ('activo', 'inactivo'))`. Esto contradice constitution §7. Ambos artefactos deben corregirse **antes o junto con** la corrección del código. El DDL también necesita actualizar la constraint a `CHECK (estado IN ('ACTIVO', 'INACTIVO'))`.

---

### Métricas

| Métrica | Valor |
|---|---|
| Archivos analizados | 8 |
| Hallazgos totales | 22 |
| ❌ BLOQUEANTES | **14** |
| ⚠️ ADVERTENCIAS | **4** |
| ✅ CUMPLE | **8 dimensiones/sub-ítems** |
| Métodos de negocio sin test | 2 (`desactivar`, `actualizarDatos`) |
| Violaciones JPA en dominio | 12 (imports + anotaciones) |
| Setters públicos ilegales | 7 |

---

### Correcciones ordenadas — bloqueantes antes de TASK-03

**Orden de ejecución recomendado** (secuencial, cada paso depende del anterior):

1. **Eliminar JPA del dominio** — Persona.java y Cliente.java: eliminar todos los `import jakarta.persistence.*` y sus anotaciones. El dominio queda como POJO puro. Las anotaciones JPA se mueven a clases `JpaPersona` / `JpaCliente` en `infrastructure/persistence/`.

2. **Renombrar enums a UPPER_SNAKE_CASE** — EstadoCliente.java: `activo → ACTIVO`, `inactivo → INACTIVO`. Actualizar también data-model.md, el DDL (`CHECK (estado IN ('ACTIVO','INACTIVO'))`), y todos los usos en Cliente.java, tests.

3. **Eliminar setters públicos en Persona y Cliente** — Hacer `package-private` o eliminarlos; el acceso a mutación debe ser solo a través de `actualizarDatos()` en `Cliente`. Si JPA (en infraestructura) los necesita, esa es responsabilidad de la clase de entidad JPA en infrastructure, no del dominio.

4. **Corregir `clienteId = null` en `ClienteCreatedEvent`** — La solución es mover el `registrarEvento()` al **servicio de aplicación** `ClienteService`, después de `save()`, cuando el ID ya está asignado: `cliente.registrarEvento(new ClienteCreatedEvent(cliente.getId(), cliente.getNombre(), ACTIVO))`. Alternativamente, hacer que `registrarEvento` en el dominio sea solo un marcador y que el servicio construya el evento real.

5. **Escribir tests faltantes** — Añadir `ClienteUpdateTest` / `ClienteDeactivationTest` cubriendo `actualizarDatos()` (casos: actualización parcial, validación de edad/identificación, estado cambiante) y `desactivar()` (estado queda en `INACTIVO`).

6. **Tipificar `estado` en `ClienteCreatedEvent`** — Cambiar el campo `String estado` a `EstadoCliente estado` en ClienteCreatedEvent.java.

7. *(Menor — resolver antes de TASK-10+)* **Normalizar nombres de test a inglés** — Renombrar métodos de test según constitution §4: `crearClienteConDatosValidosDebeCrearInstancia → clienteWithValidDataShouldCreateInstance`, etc. Abrir también issue sobre ambigüedad en constitution §7.

---

### Próximas acciones

- **Hay 14 BLOQUEANTES**: No continuar con TASK-03 hasta resolver los puntos 1–5 anteriores.
- Los puntos 1 y 3 son interdependientes: al eliminar JPA del dominio, los setters dejan de ser necesarios para Hibernate y pueden eliminarse con seguridad.
- Sugerencia de comando: corregir el dominio, re-ejecutar `./gradlew :customers-service:test --tests "com.banking.customers.unit.domain.*"` para verificar que los tests existentes siguen en verde, **luego** añadir los tests faltantes (TDD: Red primero).

---
