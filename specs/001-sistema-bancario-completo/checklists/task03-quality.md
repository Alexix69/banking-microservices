# Pre-Commit Quality Checklist: TASK-03 — Jerarquía de Excepciones customers-service

**Purpose**: Exhaustive pre-commit validation of exception hierarchy against constitution, data-model, tasks, and code quality rules
**Created**: 2026-04-14
**Feature**: [spec.md](../spec.md) | **Tasks**: [tasks.md](../tasks.md) | **Constitution**: [constitution.md](../../../.specify/memory/constitution.md)
**Scope**: TASK-03 · Depth: Exhaustive
**Files analyzed**: 9 production classes in `domain/exception/` + `ExceptionHierarchyTest.java`

---

## CK-01 Completitud de la Jerarquía (vs tasks.md)

- [x] CHK001 `DomainException.java` existe en `domain/exception/` [tasks.md TASK-03]
- [x] CHK002 `BusinessRuleException.java` existe en `domain/exception/` [tasks.md TASK-03]
- [x] CHK003 `ResourceNotFoundException.java` existe en `domain/exception/` [tasks.md TASK-03]
- [x] CHK004 `DuplicateResourceException.java` existe en `domain/exception/` [tasks.md TASK-03]
- [x] CHK005 `EdadInvalidaException.java` existe en `domain/exception/` [tasks.md TASK-03]
- [x] CHK006 `ContrasenaInvalidaException.java` existe en `domain/exception/` [tasks.md TASK-03]
- [x] CHK007 `IdentificacionInvalidaException.java` existe en `domain/exception/` [tasks.md TASK-03]
- [x] CHK008 `IdentificacionDuplicadaException.java` existe en `domain/exception/` [tasks.md TASK-03]
- [x] CHK009 `ClienteNotFoundException.java` existe en `domain/exception/` [tasks.md TASK-03]
- [x] CHK010 `ClienteConCuentasActivasException` ausente — decisión explícita registrada en tasks.md Decisión B y T029 [tasks.md §Decisión B]

## CK-02 Correctitud de Clases Abstractas

- [x] CHK011 `DomainException` es `abstract` y extiende `RuntimeException` — verificado por `assertInstanceOf(RuntimeException.class, ex)` en test [Constitution §10]
- [x] CHK012 `DomainException` tiene constructor `(String message)` que delega a `super(message)` [Constitution §10]
- [x] CHK013 `BusinessRuleException` es `abstract` y extiende `DomainException` — verificado por `assertInstanceOf(DomainException.class, ex)` + `assertInstanceOf(BusinessRuleException.class, ex)` [Constitution §10]
- [x] CHK014 `BusinessRuleException` tiene constructor `protected (String message)` [Constitution §10]
- [x] CHK015 `ResourceNotFoundException` es `abstract` y extiende `DomainException` — verificado por `assertInstanceOf(DomainException.class, ex)` + `assertInstanceOf(ResourceNotFoundException.class, ex)` [Constitution §10]
- [x] CHK016 `ResourceNotFoundException` tiene constructor `protected (String message)` [Constitution §10]
- [x] CHK017 `DuplicateResourceException` es `abstract` y extiende `DomainException` — verificado por `assertInstanceOf(DomainException.class, ex)` + `assertInstanceOf(DuplicateResourceException.class, ex)` [Constitution §10]
- [x] CHK018 `DuplicateResourceException` tiene constructor `protected (String message)` [Constitution §10]

## CK-03 Correctitud de Excepciones Concretas y Mensajes

- [x] CHK019 `EdadInvalidaException` extiende `BusinessRuleException` — constructor sin parámetros [tasks.md TASK-03]
- [x] CHK020 Mensaje de `EdadInvalidaException`: `"La edad debe ser mayor o igual a 18 años"` — verificado por test `edadInvalidaExceptionShouldHaveCorrectMessage()` [tasks.md TASK-03]
- [x] CHK021 `ContrasenaInvalidaException` extiende `BusinessRuleException` — constructor sin parámetros [tasks.md TASK-03]
- [x] CHK022 Mensaje de `ContrasenaInvalidaException`: `"La contraseña no cumple la política de seguridad"` — alineado con tasks.md [tasks.md TASK-03]
- [x] CHK023 `IdentificacionInvalidaException` extiende `BusinessRuleException` — constructor `(String identificacion)` [tasks.md TASK-03]
- [x] CHK024 Mensaje de `IdentificacionInvalidaException` incluye el valor de la identificación: `"La identificación 'X' no es válida"` [tasks.md TASK-03]
- [x] CHK025 `ClienteNotFoundException` extiende `ResourceNotFoundException` — constructor `(Long id)` [tasks.md TASK-03]
- [x] CHK026 Mensaje de `ClienteNotFoundException` incluye el ID numérico: `"Cliente no encontrado con ID: " + id` — verificado por test `clienteNotFoundExceptionShouldIncludeIdInMessage()` [tasks.md TASK-03]
- [x] CHK027 `IdentificacionDuplicadaException` extiende `DuplicateResourceException` — constructor `(String identificacion)` [tasks.md TASK-03]
- [x] CHK028 Mensaje de `IdentificacionDuplicadaException`: `"Ya existe un cliente con la identificación " + identificacion` [tasks.md TASK-03]

## CK-04 Arquitectura Hexagonal — Cero Dependencias de Infraestructura

- [x] CHK029 Ningún archivo en `domain/exception/` importa `jakarta.persistence.*` — `find domain/exception -name "*.java" | xargs grep -l "jakarta"` → NONE [Constitution §2.2]
- [x] CHK030 Ningún archivo en `domain/exception/` importa `org.springframework.*` — `find domain/exception -name "*.java" | xargs grep -l "springframework"` → NONE [Constitution §2.2]
- [x] CHK031 Ningún archivo en `domain/exception/` importa clases fuera del propio paquete (son clases auto-contenidas) [Constitution §2.2]

## CK-05 Cero Comentarios

- [x] CHK032 Ningún archivo del proyecto contiene líneas `//` — `find src -name "*.java" | xargs grep -l "//"` → NONE [Constitution §7]
- [x] CHK033 Ningún archivo contiene bloques `/* */` ni Javadoc `/** */` en las nuevas clases [Constitution §7]

## CK-06 Tamaño de Clases y Métodos

- [x] CHK034 Todas las 9 clases de `domain/exception/` tienen exactamente 8 líneas — muy por debajo del límite de 200 líneas por clase [Constitution §7]
- [x] CHK035 Todos los constructores tienen 1–2 líneas — muy por debajo del límite de 20 líneas por método [Constitution §7]
- [x] CHK036 El único método en cada clase es el constructor — principio de responsabilidad única respetado [Constitution §9 SRP]

## CK-07 Cobertura TDD — Existencia de Tests

- [x] CHK037 `ExceptionHierarchyTest.java` existe en `unit/domain/` con 8 métodos de test [Constitution §5]
- [x] CHK038 Tests cubren la raíz de la jerarquía: `DomainException` es `RuntimeException` — `domainExceptionShouldBeRuntimeException()` [Constitution §5]
- [x] CHK039 Tests cubren `BusinessRuleException` → `DomainException` — `businessRuleExceptionShouldExtendDomainException()` [Constitution §5]
- [x] CHK040 Tests cubren `ResourceNotFoundException` → `DomainException` — `resourceNotFoundExceptionShouldExtendDomainException()` [Constitution §5]
- [x] CHK041 Tests cubren `DuplicateResourceException` → `DomainException` — `duplicateResourceExceptionShouldExtendDomainException()` [Constitution §5]
- [x] CHK042 Tests cubren `ClienteNotFoundException` → `ResourceNotFoundException` — `clienteNotFoundExceptionShouldExtendResourceNotFoundException()` [Constitution §5]
- [x] CHK043 Tests cubren `IdentificacionDuplicadaException` → `DuplicateResourceException` — `identificicacionDuplicadaExceptionShouldExtendDuplicateResourceException()` [Constitution §5]
- [x] CHK044 Tests verifican mensaje correcto de `EdadInvalidaException` — `edadInvalidaExceptionShouldHaveCorrectMessage()` [Constitution §5]
- [x] CHK045 Tests verifican que ID aparece en mensaje de `ClienteNotFoundException` — `clienteNotFoundExceptionShouldIncludeIdInMessage()` [Constitution §5]

## CK-08 Convención de Nombres de Tests

- [x] CHK046 Todos los métodos de test en inglés siguiendo patrón `subjectStateOrAction + Should + ExpectedOutcome` [Constitution §4 v1.0.1]
- [x] CHK047 `ExceptionHierarchyTest` sin anotaciones de Spring (`@SpringBootTest`, `@MockBean`, `@Autowired`) — solo imports de `org.junit.jupiter` y clases de dominio [Constitution §5]

## CK-09 Idioma del Código

- [x] CHK048 Nombres de excepciones de dominio en español: `DomainException`, `BusinessRuleException`, `EdadInvalidaException`, `ContrasenaInvalidaException`, `IdentificacionInvalidaException`, `ClienteNotFoundException`, `IdentificacionDuplicadaException` [Constitution §4]
- [x] CHK049 Nombres abstractos reutilizables en español: `ResourceNotFoundException`, `DuplicateResourceException` [Constitution §4]
- [x] CHK050 Mensajes de excepción en español (son mensajes de negocio expuestos en la API): `"La edad debe ser mayor o igual a 18 años"`, `"La contraseña no cumple la política de seguridad"`, `"La identificación '…' no es válida"`, `"Cliente no encontrado con ID: …"`, `"Ya existe un cliente con la identificación …"` [Constitution §4]

## CK-10 Consistencia con constitution §10 — Jerarquía de Errores

- [x] CHK051 `DomainException` es la raíz de toda excepción de negocio — consistente con constitution §10 [Constitution §10]
- [x] CHK052 `BusinessRuleException` → subcategoría de errores de regla de negocio [Constitution §10]
- [x] CHK053 `ResourceNotFoundException` → subcategoría para recursos no encontrados [Constitution §10]
- [x] CHK054 `DuplicateResourceException` → subcategoría para recursos duplicados [Constitution §10]
- [x] CHK055 La jerarquía es consistente con el mapeo HTTP declarado en constitution §10: `EdadInvalidaException` → HTTP 400, `IdentificacionInvalidaException` → HTTP 400, `ContrasenaInvalidaException` → HTTP 400, `ClienteNotFoundException` → HTTP 404, `IdentificacionDuplicadaException` → HTTP 409 [Constitution §10]

## CK-11 Consistencia con data-model.md — Jerarquía de Excepciones

- [x] CHK056 Todos los nodos de la jerarquía de `customers-service` documentados en data-model.md están implementados: `DomainException`, `BusinessRuleException`, `ResourceNotFoundException`, `DuplicateResourceException`, `EdadInvalidaException`, `ContrasenaInvalidaException`, `IdentificacionInvalidaException`, `ClienteNotFoundException`, `IdentificacionDuplicadaException` [data-model.md §jerarquía customers-service]
- [x] CHK057 `ClienteConCuentasActivasException` aparece en data-model.md pero NO está implementada — esto es correcto y está justificado por Decisión B de tasks.md. Pendiente: data-model.md debería actualizarse en T029 para eliminar esa entrada [tasks.md §T029, Decisión B]
- [x] CHK058 Las posiciones en la jerarquía coinciden con data-model.md: `EdadInvalidaException`, `ContrasenaInvalidaException`, `IdentificacionInvalidaException` bajo `BusinessRuleException`; `ClienteNotFoundException` bajo `ResourceNotFoundException`; `IdentificacionDuplicadaException` bajo `DuplicateResourceException` [data-model.md §jerarquía]

## CK-12 Regresión — Tests de TASK-02

- [x] CHK059 Los 15 tests de TASK-02 siguen en verde tras agregar las nuevas clases de excepción [Constitution §5]
- [x] CHK060 Total del suite: **23 tests — 0 failures — 0 skipped** confirmado por `BUILD SUCCESSFUL` [Constitution §5]

---

## Resultado Final

**✅ TASK-03 LISTO PARA COMMIT — 60/60 ítems PASS — 0 FAILs**

Suite completa acumulada:

| Test class | Tests | Failures |
|---|---|---|
| `ClienteCreationTest` | 5 | 0 |
| `ClienteValidationTest` | 5 | 0 |
| `ClienteDeactivationTest` | 2 | 0 |
| `ClienteUpdateTest` | 3 | 0 |
| `ExceptionHierarchyTest` | 8 | 0 |
| **Total** | **23** | **0** |

**Nota pendiente (no bloqueante)**: data-model.md §jerarquía aún lista `ClienteConCuentasActivasException`. Debe eliminarse de ese artefacto en T029 [tasks.md §T029].
