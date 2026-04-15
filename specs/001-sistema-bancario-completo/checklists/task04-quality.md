# Pre-Commit Quality Checklist: TASK-04 — Eventos de dominio y puertos de `customers-service`

**Purpose**: Validate requirement quality, clarity, consistency, and coverage for TASK-04 before commit
**Created**: 2026-04-14
**Feature**: [spec.md](../spec.md) | **Tasks**: [tasks.md](../tasks.md) | **Constitution**: [constitution.md](../../../.specify/memory/constitution.md)
**Scope**: TASK-04 · Depth: Exhaustive
**Files analyzed**: `domain/event/` (3 classes) + `domain/port/` (2 interfaces) + `Cliente.java` (modified) + 2 new test classes

---

## CK-01 Completitud de Requisitos de Eventos

- [ ] CHK001 - Are all three event types (`DomainEvent`, `ClienteCreatedEvent`, `ClienteDesactivadoEvent`) explicitly enumerated with their full class names in tasks.md TASK-04? [Completeness, tasks.md TASK-04]
- [ ] CHK002 - Is `DomainEvent`'s contract (marker interface with zero methods vs abstract class with common fields) explicitly stated in tasks.md? [Clarity, tasks.md TASK-04]
- [ ] CHK003 - Are all four `ClienteRepository` method signatures complete in tasks.md, including return types, parameter names, and parameter types? [Completeness, tasks.md TASK-04]
- [ ] CHK004 - Is the single `EventPublisher.publish(DomainEvent event)` method specified with its exact generic parameter type (`DomainEvent`, not raw `Object`)? [Clarity, tasks.md TASK-04]
- [ ] CHK005 - Does tasks.md specify the `final` modifier requirement for event classes (`ClienteCreatedEvent`, `ClienteDesactivadoEvent`) to prevent subclassing? [Completeness, Gap]
- [ ] CHK006 - Is the null-safety contract for `ClienteDesactivadoEvent.clienteId` (may be `null` if `Cliente` was created but not yet persisted) documented in tasks.md or spec.md? [Completeness, Gap]

---

## CK-02 Claridad y Tipado Fuerte

- [ ] CHK007 - Is `ClienteCreatedEvent.estado` explicitly typed as `EstadoCliente` (domain enum) rather than `String` in tasks.md? Currently tasks.md says `estado (String)` but the TASK-02 checklist (CHK029) and implementation use `EstadoCliente`. [Conflict, tasks.md TASK-04, checklists/task02-quality.md CHK029]
- [ ] CHK008 - Is the phrase "estado siempre 'activo'" clarified as a type-safe `EstadoCliente.ACTIVO` constant, not a hard-coded `String "activo"` literal? [Clarity, Ambiguity, tasks.md TASK-04]
- [ ] CHK009 - Are the field types in `ClienteCreatedEvent` consistent with the JSON payload in `contracts/events.md` (e.g., `clienteId: Number (Long)`, `nombre: String`, `estado: String serialization` of `EstadoCliente`)? [Consistency, contracts/events.md §ClienteCreatedEvent]
- [ ] CHK010 - Are the identifier types used in `ClienteRepository` (`Long id`, `String identificacion`) consistent with the field types declared in `data-model.md` for the `cliente` and `persona` tables? [Consistency, data-model.md §Cliente, §Persona]

---

## CK-03 Arquitectura Hexagonal — Puertos

- [ ] CHK011 - Does tasks.md explicitly state that `domain/port/` must have zero imports of `org.springframework`, `jakarta.persistence`, or `org.springframework.amqp`? [Completeness, Gap — Constitution §2.2 applies globally but is not called out in TASK-04]
- [ ] CHK012 - Does tasks.md explicitly state that `domain/event/` must have zero imports of infrastructure namespaces? [Completeness, Gap]
- [ ] CHK013 - Are `ClienteRepository` and `EventPublisher` defined in requirements as secondary ports (driven/outbound) in terms of hexagonal architecture? [Clarity, Gap — tasks.md states the methods but not the architectural role]
- [ ] CHK014 - Is the dependency direction rule (infra → application → domain; never reversed) re-stated or referenced in TASK-04 for the new ports, ensuring no future violation? [Completeness, Constitution §2.2]
- [ ] CHK015 - Does tasks.md specify that `ClienteRepository` must NOT import any Spring Data `JpaRepository` or similar? [Completeness, Gap — implied by constitution but absent in task spec]

---

## CK-04 Inmutabilidad y Diseño de Eventos

- [ ] CHK016 - Is event immutability (final fields, no setters, constructor-only initialization) specified as a requirement for `ClienteDesactivadoEvent` and `ClienteCreatedEvent` in tasks.md or constitution? [Completeness, Gap — Constitution §8 references events but does not constrain mutability]
- [ ] CHK017 - Is the full contract of `Cliente.consumirEventos()` (returns defensive copy, clears internal list, second call returns empty) explicitly documented in tasks.md or constitution §8 for TASK-04? [Completeness, Constitution §8]
- [ ] CHK018 - Is the modification to `Cliente.desactivar()` (registering `ClienteDesactivadoEvent`) attributed to TASK-04 in tasks.md, rather than assumed from context? [Clarity, Gap — tasks.md TASK-04 omits the desactivar() modification explicitly]
- [ ] CHK019 - Is the requirement that `registrarEvento()` is called only from domain methods (not from outside) documented to protect encapsulation? [Completeness, Gap]

---

## CK-05 Consistencia entre Artefactos

- [ ] CHK020 - Does `constitution.md §2.3` still reference `ClienteDeletedEvent` instead of `ClienteDesactivadoEvent`, creating a documented conflict with Decisión B in tasks.md? [Conflict, constitution.md §2.3, tasks.md Decisión B]
- [ ] CHK021 - Are all occurrences of `cliente.deleted` routing key replaced by `cliente.desactivado` throughout `contracts/events.md`, including the topology diagram, routing key section, and RabbitMQConfig snippet? [Consistency, contracts/events.md]
- [ ] CHK022 - Does `contracts/events.md` use `ClienteDesactivadoEvent` consistently in both the event header, the routing section, and the Spring AMQP config snippet? [Consistency, contracts/events.md §ClienteDesactivadoEvent]
- [ ] CHK023 - Are the JSON payload field names in `contracts/events.md §ClienteCreatedEvent` (`clienteId`, `nombre`, `estado`) identical to the Java getter names (`getClienteId()`, `getNombre()`, `getEstado()`)? [Consistency, contracts/events.md §ClienteCreatedEvent]
- [ ] CHK024 - Is `tasks.md`'s listing of `ClienteCreatedEvent.estado` as type `String` a requirements artifact that needs correction to match the actual type `EstadoCliente` agreed in TASK-02? [Conflict, tasks.md TASK-04 vs checklists/task02-quality.md CHK029]

---

## CK-06 Cobertura de Escenarios y Tests

- [ ] CHK025 - Is there a specified test scenario that validates `create()` does NOT register any `ClienteDesactivadoEvent`? (distinguishes from `clienteCreationShouldLeaveEmptyDomainEvents`) [Coverage, Gap]
- [ ] CHK026 - Is there a specified test verifying that calling `desactivar()` on a `Cliente` with a null `id` (not yet persisted) does NOT throw a NullPointerException, or is this scenario explicitly excluded? [Edge Case, Gap]
- [ ] CHK027 - Is there a specified test scenario verifying that `desactivar()` called twice on the same client registers exactly two events (or alternatively, is idempotency a stated requirement)? [Edge Case, Gap]
- [ ] CHK028 - Are the 4 test methods of `ClienteEventTest` explicitly enumerated in tasks.md with the correct method names following the `subjectStateOrAction + Should + ExpectedOutcome` pattern? [Completeness, Constitution §4 v1.0.1]
- [ ] CHK029 - Are the 4 test methods of `ClienteCreationEventTest` explicitly enumerated in tasks.md? [Completeness, Gap — only `crearClienteDebeRegistrarClienteCreatedEvent()` is mentioned]
- [ ] CHK030 - Does tasks.md specify the expected total test count after TASK-04 (31 tests, 0 failures) as a completeness criterion? [Completeness, Gap]

---

## CK-07 Convención de Nombres y Código Limpio

- [ ] CHK031 - Are the two new test class names (`ClienteEventTest`, `ClienteCreationEventTest`) consistent with the naming pattern used for existing test classes (`ClienteCreationTest`, `ExceptionHierarchyTest`)? [Consistency, Constitution §4]
- [ ] CHK032 - Does tasks.md call out the zero-comments constraint for newly created production files (`ClienteDesactivadoEvent`, `ClienteRepository`, `EventPublisher`) and test files? [Completeness, Gap — Constitution §7 is global but not referenced in TASK-04]
- [ ] CHK033 - Are the Spanish-language requirements for port names (`ClienteRepository` in Spanish) and event names consistent with the constitution §4 hybrid language scheme? [Consistency, Constitution §4]
- [ ] CHK034 - Does tasks.md specify the language of domain port interface names (`ClienteRepository` in Spanish vs `EventPublisher` in English), considering the constitution §4 hybrid scheme? [Clarity, Ambiguity, Constitution §4]

---

## CK-08 Regresión y Criterio de Completitud

- [ ] CHK035 - Does tasks.md explicitly state that all 23 tests from TASK-02 and TASK-03 must remain green after TASK-04 changes? [Completeness, Gap]
- [ ] CHK036 - Is it documented whether adding the `final` modifier to `ClienteCreatedEvent` is a breaking change for any downstream test or infrastructure class? [Risk, Completeness, Gap]
- [ ] CHK037 - Does the TASK-04 completeness criterion cover the `desactivar()` modification to `Cliente.java`, or is that change missing from the explicit acceptance criteria? [Completeness, Gap — tasks.md TASK-04 criteria do not mention desactivar() update]
- [ ] CHK038 - Are the zero-infra-import constraints verifiable with a documented `find … | xargs grep` command in the completeness criteria, as was done for TASK-02/TASK-03? [Measurability, Gap]
- [ ] CHK039 - Is the scope of "existing tests remain green" restricted to `customers-service` only, or must `accounts-service` tests also be unaffected? [Clarity, Spec §Regresión]

---

## Resultado Final

| Category | Items | Issues Found |
|---|---|---|
| CK-01 Completitud de Eventos | 6 | — |
| CK-02 Claridad y Tipado Fuerte | 4 | — |
| CK-03 Arquitectura Hexagonal — Puertos | 5 | — |
| CK-04 Inmutabilidad y Diseño de Eventos | 4 | — |
| CK-05 Consistencia entre Artefactos | 5 | — |
| CK-06 Cobertura de Escenarios y Tests | 6 | — |
| CK-07 Convención de Nombres y Código Limpio | 4 | — |
| CK-08 Regresión y Criterio de Completitud | 5 | — |
| **Total** | **39** | **—** |

**Hallazgos pre-marcados para revisión**:
- **CHK007 / CHK024**: `tasks.md` TASK-04 declara `ClienteCreatedEvent.estado` como `String`, pero TASK-02 (CHK029) y la implementación usan `EstadoCliente`. **Conflicto de artefacto** — tasks.md debe corregirse.
- **CHK018**: La modificación de `Cliente.desactivar()` (agregar `registrarEvento(...)`) no está listada explícitamente en los artefactos de TASK-04. **Gap de trazabilidad**.
- **CHK020**: `constitution.md §2.3` referencia `ClienteDeletedEvent`; esto entra en conflicto con **Decisión B**. Requiere actualización de la constitution (candidato para T029).
- **CHK037**: Los criterios de completitud de TASK-04 en tasks.md no mencionan la actualización de `desactivar()`. **Gap en criterio de aceptación**.
