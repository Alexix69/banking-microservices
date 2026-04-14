# Pre-Commit Quality Checklist: TASK-02 — Domain Model customers-service

**Purpose**: Validate code quality and spec conformance for TASK-02 before commit
**Created**: 2026-04-14
**Feature**: [spec.md](../spec.md) | **Tasks**: [tasks.md](../tasks.md) | **Constitution**: [constitution.md](../../../.specify/memory/constitution.md)

---

## CK-01 Arquitectura Hexagonal

- [x] CHK001 `domain/model/` no contiene ningún import de `jakarta.persistence` — `find … domain/model -name "*.java" | xargs grep -l "jakarta.persistence"` → NONE [Spec §2.2]
- [x] CHK002 `domain/event/` no contiene ningún import de `jakarta.persistence` — resultado: NONE [Spec §2.2]
- [x] CHK003 `domain/model/` no contiene ningún import de `org.springframework` — resultado: NONE [Spec §2.2]
- [x] CHK004 Clases JPA (`PersonaJpaEntity`, `ClienteJpaEntity`) están exclusivamente en `infrastructure/persistence/` [Spec §2.2, §12]
- [x] CHK005 `CustomerMapper` está en `infrastructure/mapper/` [Spec §12]

## CK-02 Convención de Enums

- [x] CHK006 `EstadoCliente` tiene exactamente `ACTIVO` e `INACTIVO` en UPPER_SNAKE_CASE [Constitution §7]
- [x] CHK007 `Genero` tiene exactamente `MASCULINO` y `FEMENINO` en UPPER_SNAKE_CASE [Constitution §7]
- [x] CHK008 No existe ningún valor de enum en minúscula en `domain/` — ningún `activo`, `inactivo`, `masculino`, `femenino` presente

## CK-03 Encapsulación del Dominio

- [x] CHK009 `Persona.java` no tiene ningún método `public void setX()` — solo getters públicos y campos `protected` [Constitution §2.2]
- [x] CHK010 `Cliente.java` no tiene ningún método `public void setX()` — `contrasena` y `estado` solo accesibles via getters y métodos de negocio [Constitution §2.2]
- [x] CHK011 `PersonaJpaEntity.java` tiene setters para JPA — correcto, está en infraestructura [Constitution §2.2]
- [x] CHK012 `ClienteJpaEntity.java` tiene setters para JPA — correcto, está en infraestructura [Constitution §2.2]
- [x] CHK013 Estado de `Cliente` cambia únicamente vía `desactivar()` (→ `INACTIVO`) o `actualizarDatos()` (→ campos de negocio); `create()` establece estado inicial en constructor privado [Constitution §8]

## CK-04 Cero Comentarios

- [x] CHK014 Ningún archivo de producción (`src/main`) contiene líneas `//` — `find … -name "*.java" | xargs grep -l "//"` → NONE [Constitution §7]
- [x] CHK015 Ningún archivo de test (`src/test`) contiene líneas `//` — resultado: NONE [Constitution §7]
- [x] CHK016 Ningún archivo contiene bloques `/* */` ni Javadoc `/** */` [Constitution §7]

## CK-05 Algoritmo Cédula Ecuatoriana R-01

- [x] CHK017 `validarIdentificacion()` verifica exactamente 10 dígitos: `matches("\\d{10}")` [Research R-01, paso 1]
- [x] CHK018 Verifica provincia `d[0]*10 + d[1]` entre 1 y 24: `provincia < 1 || provincia > 24` [Research R-01, paso 2]
- [x] CHK019 Verifica tercer dígito `< 6`: `d[2] >= 6` → excepción [Research R-01, paso 3]
- [x] CHK020 Aplica multiplicadores alternos 2 y 1: `(i % 2 == 0) ? d[i] * 2 : d[i]` [Research R-01, paso 4]
- [x] CHK021 Resta 9 cuando el resultado es `>= 10`: `if (v >= 10) v -= 9` [Research R-01, paso 4]
- [x] CHK022 Calcula verificador: `(10 - suma % 10) % 10` [Research R-01, paso 6]
- [x] CHK023 Compara contra décimo dígito `d[9]` [Research R-01, paso 7]

## CK-06 Invariantes de Dominio en Cliente

- [x] CHK024 `create()` valida `edad < 18` → lanza `EdadInvalidaException` [data-model.md §invariantes]
- [x] CHK025 `create()` valida `identificacion` con módulo 10 → lanza `IdentificacionInvalidaException` [data-model.md §invariantes]
- [x] CHK026 `create()` valida `contrasena` con regex `^(?=.*[A-Z])[a-zA-Z0-9]{8,}$` → lanza `ContrasenaInvalidaException` [data-model.md §invariantes]
- [x] CHK027 `create()` NO registra ningún `ClienteCreatedEvent` — `domainEvents` está vacío tras `create()` [Constitution §8 Domain Events, hallazgo G1 resuelto]
- [x] CHK028 `actualizarDatos()` re-valida `edad` e `identificacion` antes de asignar campos [data-model.md §invariantes]

## CK-07 Domain Events

- [x] CHK029 `ClienteCreatedEvent` tiene campos `clienteId (Long)`, `nombre (String)`, `estado (EstadoCliente)` — tipado fuerte, no `String` [hallazgo G2 resuelto]
- [x] CHK030 `ClienteCreatedEvent` implementa `DomainEvent` [Constitution §8]
- [x] CHK031 `consumirEventos()` retorna copia defensiva: `new ArrayList<>(domainEvents)`, luego `domainEvents.clear()` [Constitution §8]
- [x] CHK032 `registrarEvento(DomainEvent evento)` agrega correctamente al listado interno [Constitution §8]
- [x] CHK033 `domainEvents` no tiene ninguna anotación JPA en `Cliente.java` — dominio es POJO puro [Constitution §2.2]

## CK-08 CustomerMapper

- [x] CHK034 `toCliente()` mapea todos los campos: `id, nombre, genero, edad, identificacion, direccion, telefono, contrasena, estado` vía `Cliente.reconstitute()` — sin pérdida de datos [data-model.md §Persona, §Cliente]
- [x] CHK035 `toJpaEntity()` mapea todos los campos en sentido inverso usando setters de `ClienteJpaEntity` — sin pérdida de datos [data-model.md §DDL]
- [x] CHK036 `CustomerMapper` no tiene ninguna anotación de Spring — mapper puro [Constitution §2.2]
- [x] CHK037 Campos mapeados coinciden exactamente con las tablas `persona` y `cliente` de `data-model.md` (columnas UPPER_SNAKE_CASE mapeadas a campos camelCase) [data-model.md]

## CK-09 Tests

- [x] CHK038 Los 4 archivos de test no tienen ninguna anotación de Spring (`@SpringBootTest`, `@MockBean`, `@Autowired`, etc.) [Constitution §5]
- [x] CHK039 Todos los métodos de test siguen el patrón `subjectStateOrAction + Should + ExpectedOutcome` en inglés [Constitution §4]
- [x] CHK040 `ClienteCreationTest` (5 tests): `clienteWithValidDataShouldCreateInstance`, `clienteWithAgeLessThan18ShouldThrowEdadInvalidaException`, `clienteWithInvalidIdentificacionShouldThrowException`, `clienteWithInvalidContrasenaShouldThrowException`, `clienteCreationShouldNotRegisterEventDirectly` [Constitution §5]
- [x] CHK041 `ClienteValidationTest` (5 tests): dígito verificador, provincia inválida, contraseña corta, sin mayúscula, géneros válidos [Constitution §5]
- [x] CHK042 `ClienteDeactivationTest` (2 tests): `desactivatedClienteShouldHaveInactiveState` vía `create()` y vía `reconstitute()` [Constitution §5, hallazgo K2 resuelto]
- [x] CHK043 `ClienteUpdateTest` (3 tests): actualización válida, edad inválida, identificacion inválida [Constitution §5, hallazgo K1 resuelto]
- [x] CHK044 Total: **15 tests — 0 failures — 0 skipped** — `BUILD SUCCESSFUL` confirmado [Constitution §5]

## CK-10 Tamaño de Métodos y Clases

- [x] CHK045 `validarIdentificacion()` — 19 líneas, dentro del límite de 20 [Constitution §7]
- [x] CHK046 `actualizarDatos()` — 8 líneas [Constitution §7]
- [x] CHK047 `create()` — 5 líneas [Constitution §7]
- [x] CHK048 `Cliente.java` — ~115 líneas, dentro del límite de 200 [Constitution §7]
- [x] CHK049 `PersonaJpaEntity.java` — ~100 líneas, dentro del límite de 200 [Constitution §7]
- [x] CHK050 `CustomerMapper.java` — ~35 líneas, dentro del límite de 200 [Constitution §7]

## CK-11 Idioma del Código

- [x] CHK051 Clases de dominio en español: `Persona`, `Cliente`, `EstadoCliente`, `Genero` [Constitution §4]
- [x] CHK052 Clases de infraestructura en inglés: `PersonaJpaEntity`, `ClienteJpaEntity`, `CustomerMapper` [Constitution §4]
- [x] CHK053 Métodos de negocio en español: `create`, `desactivar`, `actualizarDatos`, `consumirEventos`, `registrarEvento`, `reconstitute`, `validarEdad`, `validarIdentificacion`, `validarContrasena` [Constitution §4]
- [x] CHK054 Métodos de test en inglés siguiendo patrón `should + outcome` [Constitution §4 v1.0.1]

## CK-12 Consistencia con data-model.md

- [x] CHK055 Campos de `Persona` coinciden con tabla `persona`: `id(Long)`, `nombre(String)`, `genero(Genero)`, `edad(int)`, `identificacion(String)`, `direccion(String)`, `telefono(String)` [data-model.md §Persona]
- [x] CHK056 Campos de `Cliente` coinciden con tabla `cliente`: `contrasena(String)`, `estado(EstadoCliente)`, `domainEvents` transitorio [data-model.md §Cliente]
- [x] CHK057 `PersonaJpaEntity` mapea exactamente las columnas del DDL con anotaciones `@Column` de longitud y nullability correctas [data-model.md §DDL persona]
- [x] CHK058 `ClienteJpaEntity` mapea exactamente las columnas del DDL con `@Enumerated(EnumType.STRING)` para almacenar `ACTIVO`/`INACTIVO` [data-model.md §DDL cliente]

---

## Resultado Final

**✅ TASK-02 LISTO PARA COMMIT — 58/58 ítems PASS — 0 FAILs**

Todos los hallazgos bloqueantes del análisis han sido resueltos:
- A1–A4: JPA eliminado del dominio → clases JPA en `infrastructure/persistence/`
- B1–B2: `EstadoCliente.ACTIVO` / `INACTIVO` en UPPER_SNAKE_CASE
- C1–C7: Setters eliminados de `Persona` y `Cliente`
- G1: `create()` no registra evento — responsabilidad del use case (TASK-06)
- G2: `ClienteCreatedEvent.estado` tipado como `EstadoCliente`
- K1–K2: 5 tests nuevos cubren `desactivar()` y `actualizarDatos()`
- J1: Métodos de test renombrados a inglés
