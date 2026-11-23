# Diseño

## Context

Ver `proposal.md` (Why). Estado actual en `develop`:

- `DegreeProcedureUseCase.open` arma un `User` candidato con los siete datos y ejecuta `signUp`, busca la cuenta con `UserRepository.getByEmailForUpdate`, la empareja con `DegreeProcedure.requireMatchingStudent` y, si no existe, la inserta y avisa con `UserManagementNotifier`.
- `DegreeProcedure.open` y `requireMatchingStudent` comparan `getRole() != Role.STUDENT`, lo que la arquitectura de referencia prohíbe.
- `degree_procedure.reference` es `BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY` (`V4`); `id UUID` es solo `UNIQUE`. `initial_document.procedure_reference` apunta a `reference`; `degree_project.procedure_id` ya apunta a `id`.
- Como la referencia nace en el INSERT, `DegreeProcedureOpenedEvent` no la lleva, `insert` devuelve otra instancia y `MailDegreeProcedureNotifier` la junta con el evento en `OpeningNotice`.
- `User.uagrmCode` es obligatorio para estudiantes, de 1 a 10 dígitos, y único entre todas las cuentas.

## Goals / Non-Goals

**Objetivos.**

- La apertura solo referencia una cuenta existente; UC-002 no repite reglas ni avisos de UC-001.
- La referencia es un dato de negocio que construye el agregado; el `UUID` es la identidad y la clave primaria.
- El semestre vigente entra por un puerto que se podrá reemplazar sin tocar UC-002.

**Fuera del alcance.** Cambiar la clasificación de los PDF, el reemplazo de documentos (salvo el formato de la referencia), el registro de perfil de UC-003 (busca el trámite por estudiante) y el cierre de trámites.

## Decisions

### Flujo de apertura

`DegreeProcedureUseCase.open(requesterEmail, OpenDegreeProcedureCommand)`, flujo básico de UC-002:

1. `requireActiveRequester(requesterEmail, OPEN_DEGREE_PROCEDURE)`.
2. `users.getByUagrmCodeForUpdate(command.code())`; si no hay cuenta, rechazo en el campo `registro`: «No existe una cuenta con el registro 218001234. Créela primero con usuarios.crearCuenta.».
3. `procedures.existsOpenForStudent(student.getId())` → rechazo actual de trámite en curso (FR-056).
4. `Semester semester = calendar.currentSemester()`; si `procedures.existsByReference(DegreeProcedure.referenceOf(semester, student))` → rechazo «El estudiante ya tiene un trámite de titulación en el semestre 2026-2.».
5. Cada adjunto pasa por `requirePdf` (sin cambios).
6. `DegreeProcedure.builder().open(student, requester, semester, documents)`.
7. `procedures.insert(procedure)` y `notifier.notifyOpened(event)` por cada `DegreeProcedureOpenedEvent`.

El bloqueo de la fila de la cuenta en el paso 2 serializa dos aperturas del mismo estudiante, como hoy lo hace `getByEmailForUpdate`; el índice único de la referencia y el índice parcial de trámite en curso lo respaldan en la base de datos.

- *Alternativa descartada:* que UC-002 llame a `UserManagementUseCase` para "obtener o registrar" la cuenta. Mantenía un comando con los datos de la cuenta y no resolvía la duplicación.

### Contrato del comando

`OpenDegreeProcedureCommand(String code, List<UUID> attachmentIds)`. Se conserva el record porque, además del registro, lleva los adjuntos. `OpenDegreeProcedureCommandHandler.requiredArgs()` pasa de 7 a 1; `requiredFiles()` sigue en 4.

### Cuenta de estudiante

`User.isStudent()` dice si la cuenta tiene el rol de estudiante. `DegreeProcedure.open` comprueba `student.isStudent()` y `student.isActive()`, con mensajes en el campo `registro` («La cuenta del registro 218001234 no es de estudiante.») y `cuenta` («La cuenta del estudiante está desactivada.»). Desaparecen `requireMatchingStudent`, `incompatibleAccount` y toda comparación de roles fuera de `User`.

La comprobación hace falta porque el registro (`uagrmCode`) también guarda códigos de docentes y administrativos, y es único entre todas las cuentas.

- *Alternativa descartada:* un permiso `RolePermission.HOLD_DEGREE_PROCEDURE`. `RolePermission` autoriza lo que hace el remitente; aquí no hay acción del estudiante que autorizar, solo un dato de la cuenta referenciada.
- *Pendiente fuera de este change:* `RolePermission.BE_PROJECT_TUTOR` (UC-003) tiene la misma naturaleza: describe la cuenta elegida como tutor, no lo que hace el remitente.

### Agregado y referencia

- `DegreeProcedure.builder().open(student, requester, semester, attachments)`: el estudiante entra una sola vez; se elimina `withStudent`.
- `reference` pasa de `Long` a `String` y se asigna en `open` con `referenceOf(semester, student)` = `semester + "-" + student.getUagrmCode()`.
- `parseReference(String)` valida `\d{4}-[12]-\d{1,10}` y devuelve la referencia; el error muestra el ejemplo `2026-2-218001234`. Se elimina `referenceLabel`.
- `DegreeProcedureOpenedEvent` gana `reference`. `DegreeProcedureNotifier.notifyOpened(event)` pierde el parámetro aparte y se elimina `OpeningNotice`.
- `rehydrate` recibe la referencia como `String`.

### Semestre vigente

- `procedures.domain.Semester`: record `(int year, int period)`, periodo 1 o 2, con `parse("2026-2")` y `toString()` → `2026-2`. Error de dominio si el texto es inválido.
- `procedures.application.AcademicCalendar`: puerto con `Semester currentSemester()`.
- `procedures.infrastructure.ConfiguredAcademicCalendar`: lee `cpms.academic.current-semester` y lo valida con `Semester.parse` al crearse, para que la aplicación no inicie con un valor inválido. En `application.properties`: `cpms.academic.current-semester=${CPMS_CURRENT_SEMESTER:2026-2}`.
- *Alternativa descartada:* deducir el semestre de la fecha. Exige fijar el calendario de la UAGRM, que pertenece a otro caso de uso.
- *Alternativa descartada:* una constante en el dominio. Obliga a cambiar código cada semestre.

### Repositorios

- `UserRepository.getByUagrmCodeForUpdate(String code)` reemplaza a `getByEmailForUpdate`, cuyo único uso era UC-002. Spring Data: `findForUpdateByUagrmCode` con bloqueo pesimista, como el método actual.
- `DegreeProcedureRepository`: `insert` devuelve `void`; `getOpenByReferenceForUpdate(String)`; nuevo `existsByReference(String)`; `existsOpenForStudent` y `getOpenByStudentForUpdate` sin cambios.
- `DegreeProcedureEntity`: `@Id UUID id`; `reference` es una columna `String` única. `InitialDocumentEntity` se une por `procedure_id`. `update` busca por `id`.

### Migración `V10__degree_procedure_uuid_key.sql`

1. Agrega `degree_procedure.code VARCHAR(20)` y la llena con `<año de opened_at>-<1 si el mes ≤ 6, si no 2>-<user_account.uagrm_code del estudiante>`.
2. Agrega `initial_document.procedure_id UUID` y la llena desde `degree_procedure.id`; quita `procedure_reference`, y la restricción única pasa a `(procedure_id, document_type)`.
3. Cambia la clave primaria de `degree_procedure` a `id`, elimina la columna `reference` y renombra `code` a `reference`, `NOT NULL UNIQUE`.

Se conserva el índice parcial de trámite en curso por estudiante. No se edita `V4`.

### UC-003

`TutorAssignmentUseCase` usa `procedure.getReference()` directamente; `AssignTutorCommand` y su handler no cambian. Sus eventos ya llevan la referencia como texto.

## Risks / Trade-offs

- [El semestre configurado queda desactualizado al cambiar de semestre] → La propiedad sale de una variable de entorno, `README.md` indica cuándo cambiarla, y el caso de uso de calendario académico la reemplazará.
- [La migración encuentra dos trámites del mismo estudiante en el mismo semestre deducido y falla por la restricción única] → Solo hay datos de desarrollo (`V8` ya los borró una vez); si ocurre, se limpia la base local.
- [Se pierde la atomicidad entre crear la cuenta y abrir el trámite] → Aceptado: si la apertura falla, la cuenta queda y Secretaría reenvía solo la apertura.
- [Referencias más largas de escribir] → Se copian de la confirmación; el error de formato muestra un ejemplo.

## Diferencias con el modelo

UC-002 sin diseño en EA; se sigue la arquitectura de referencia. El modelo se reconstruirá desde el código; este change crea, cambia y elimina:

- `procedures.domain`: nuevo `Semester`; `DegreeProcedure` (`reference: String`, `open(student, requester, semester, attachments)`, `referenceOf`, `parseReference(String)`; se eliminan `withStudent`, `referenceLabel`, `requireMatchingStudent`, `incompatibleAccount`); `DegreeProcedureOpenedEvent` con `reference`; `DegreeProcedureRepository` (`existsByReference`, `getOpenByReferenceForUpdate(String)`, `insert` sin retorno).
- `procedures.application`: nuevo puerto `AcademicCalendar`; `OpenDegreeProcedureCommand(code, attachmentIds)`; `DegreeProcedureUseCase` sin `UserManagementNotifier` y con `AcademicCalendar`; `DegreeProcedureNotifier.notifyOpened(event)`.
- `users.domain`: `User.isStudent()`; `UserRepository.getByUagrmCodeForUpdate` reemplaza a `getByEmailForUpdate`.
- Infraestructura: nuevo `ConfiguredAcademicCalendar`; `MailDegreeProcedureNotifier` sin `OpeningNotice`; entidades y repositorios JPA de trámites con clave `UUID`.
