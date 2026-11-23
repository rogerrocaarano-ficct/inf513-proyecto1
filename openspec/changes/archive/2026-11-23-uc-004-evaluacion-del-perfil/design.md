# Diseño

## Contexto

El motivo está en `proposal.md`, y los flujos de negocio en `use-cases/UC-004.md`. Estado actual:

- `DegreeProject` (UC-003) tiene un solo `ProjectProfile(storedFileId, approved)`. `approved` nunca pasa a `true` fuera de las pruebas.
- `TutorAssignmentUseCase` reúne `registerInitialProfile` (presentación del perfil) y `assignTutor`, con un solo notifier, `TutorAssignmentNotifier`.
- La tabla `degree_project` (V5, V8) guarda `profile_stored_file_id`, `profile_approved` y `CHECK (tutor_id IS NULL OR profile_approved)`.
- `ReceivedEmail` ya guarda el cuerpo del correo (`getBody()`, la primera parte de texto plano), pero ningún comando lo usa.
- `StoredFileService.appendVersion` y `getHistory` versionan archivos en el almacenamiento. Solo los usa `DegreeProcedureUseCase.replaceRequirement` (UC-002), y ningún código de producción llama a `getHistory`.

## Objetivos y no objetivos

**Objetivos:**
- Un caso de uso por UC: la presentación y la evaluación del perfil salen de la clase de UC-003.
- Versiones del perfil con su evaluación, persistidas sin perder los datos actuales.

**No objetivos:**
- Mostrar el proyecto o el historial al estudiante (FR-059, UC-007).
- Renombrar `TutorAssignmentUseCase` o diseñar el tribunal: se decide al explorar UC-003.
- Cambiar el canal de correo.

## Decisiones

### Dominio (`procedures.domain`)

- **`ProjectProfile` es una versión del perfil.** Es un record con `version` (número secuencial desde 1), `storedFileId`, `title`, `status`, `observations` (nulo si no hay) y `evaluatedOn` (nulo mientras está pendiente). Es un valor del agregado, sin identidad propia, identificado por su número dentro del proyecto.
- **El versionado es del negocio, no del almacenamiento.** Cada versión guarda su propio archivo almacenado y su número; `DegreeProject` asigna el número siguiente al registrar una versión y comprueba al rehidratar que los números van de 1 en adelante sin huecos. El caso de uso no llama a `appendVersion`: el historial de archivos del almacenamiento queda en desuso (ver `AGENTS.md`, "File storage"). Alternativa descartada: encadenar los PDF en el almacenamiento además de la tabla, porque duplica el historial en dos lugares que pueden desincronizarse, y un perfil nuevo tras un rechazo es otro tema, no una corrección del archivo anterior.
- **`ProjectProfileStatus`**, un enum nuevo con `PENDING`, `APPROVED`, `OBSERVED` y `REJECTED`. Reemplaza el booleano `approved`.
- **`DegreeProject` guarda la lista ordenada de versiones.** `getProfile()` devuelve la vigente (la última) y `getProfiles()` todas. `title` sigue siendo el título vigente.
- **Operaciones de `DegreeProject`:**
  - `registerProfile(file, title, student, workshop, reference)` reemplaza a `registerInitialProfile`. Admite el perfil si no hay versiones o si la vigente está `OBSERVED` o `REJECTED` (FR-058). Agrega una versión `PENDING`, actualiza el título y levanta `DegreeProjectProfileRegisteredEvent`.
  - `approveProfile(student, director, reference)`, `observeProfile(observations, student, reference)` y `rejectProfile(observations, student, reference)` exigen la versión vigente `PENDING`. Además, observar y rechazar exigen observaciones con texto. Cada una reemplaza la versión vigente por la evaluada y levanta su evento.
  - `assignTutor` exige la versión vigente `APPROVED`.
- **Eventos nuevos:** `DegreeProjectProfileApprovedEvent` (estudiante, director, referencia, título), `DegreeProjectProfileObservedEvent` y `DegreeProjectProfileRejectedEvent` (estudiante, referencia, título, observaciones).
- Alternativa descartada: un agregado `ProjectProfile` con su propio repositorio. Las reglas de FR-058 y de la asignación del tutor cruzan el perfil y el proyecto, y bloquear un solo agregado bajo el trámite es más simple.

### Aplicación (`procedures.application`)

- **`ProjectProfileEvaluationUseCase`**, un caso de uso nuevo con un método público por flujo:
  - `registerProfile(requesterEmail, RegisterProfileCommand)`: flujo básico. Cubre el primer perfil y las versiones nuevas.
  - `approveProfile(requesterEmail, reference)`: A1. El único argumento identifica el trámite afectado, así que se usa la excepción de la arquitectura de referencia y no hay record.
  - `observeProfile(requesterEmail, ObserveProfileCommand)`: A2.
  - `rejectProfile(requesterEmail, RejectProfileCommand)`: A3.
- **Contratos:**
  - `RegisterProfileCommand(title, attachmentId)` reemplaza a `RegisterInitialProfileCommand`.
  - `ObserveProfileCommand(reference, observations)` y `RejectProfileCommand(reference, observations)`.
- **Versión nueva del perfil:** el caso de uso valida el PDF (`PdfDocument.require`) y lo adopta por su `UUID`; si hay proyecto, lo actualiza, y si no, lo inserta. No enlaza archivos en el almacenamiento.
- **Trámite:**
  - En la presentación, se bloquea con `getOpenByStudentForUpdate`, como hoy.
  - En la evaluación, se bloquea con `getOpenByReferenceForUpdate`, como en `assignTutor`.
- **Destinatarios:**
  - Taller es la cuenta activa con `Role.CAPSTONE_PROJECT_PROFESSOR`, como hoy. Si no hay, se rechaza la presentación.
  - El director es la primera cuenta activa con `Role.DIRECTOR`. Si no hay ninguna, la aprobación se registra igual, sin aviso al director (ver Riesgos).
- **Handlers:**
  - `RegisterProfileCommandHandler`, que reemplaza a `RegisterInitialProfileCommandHandler` y conserva `ACTION = "proyectos.registrarPerfil"`, con 1 argumento y 1 archivo.
  - `ApproveProfileCommandHandler` (`proyectos.aprobarPerfil`), `ObserveProfileCommandHandler` (`proyectos.observarPerfil`) y `RejectProfileCommandHandler` (`proyectos.rechazarPerfil`), con 1 argumento y 0 archivos. Los dos últimos leen `email.getBody()`.
- **`ProjectProfileEvaluationNotifier`**: `notifyProfileRegistered`, `notifyProfileApproved`, `notifyProfileObserved` y `notifyProfileRejected`.
- **`TutorAssignmentUseCase`** se queda solo con `assignTutor`, y pierde el `StoredFileService` y `notifyProfileRegistered` de su notifier.

### Permisos (`users.domain.RolePermission`)

- `REGISTER_INITIAL_PROFILE` pasa a llamarse `REGISTER_PROJECT_PROFILE` ("presentar el perfil de proyecto", `STUDENT`).
- Permiso nuevo `EVALUATE_PROJECT_PROFILE` ("evaluar perfiles de proyecto", `CAPSTONE_PROJECT_PROFESSOR`). No se agrega al director, porque solo Taller evalúa (FR-039).

### Infraestructura (`procedures.infrastructure`)

- **`MailProjectProfileEvaluationNotifier`**, con estos envíos:
  - A Taller: el PDF de la versión, como hoy. El asunto distingue la versión nueva.
  - Al estudiante: el resultado y las observaciones.
  - Al director: el aviso del perfil aprobado.
- **`MailTutorAssignmentNotifier`** se queda solo con el aviso del tutor.
- **Persistencia:**
  - `DegreeProjectEntity` mapea las versiones con `@ElementCollection` sobre una tabla nueva `degree_project_profile_version`, con un `@Embeddable` `DegreeProjectProfileVersion` por fila. El orden de la lista es la columna `version` (`@OrderColumn` con `@ListIndexBase(1)` de Hibernate), así que la versión 1 es la primera fila y la base guarda el mismo número que el dominio. El embeddable no mapea la columna otra vez: al leer, el número sale del orden y se comprueba en `DegreeProject.rehydrate`.
  - `JpaDegreeProjectRepository.update` actualiza el título, las versiones y el tutor.
- **Migración `V12__degree_project_profile_version.sql`:**
  - Crea `degree_project_profile_version`, con las columnas `project_id`, `version` (CHECK `version >= 1`), `stored_file_id` (única), `title`, `status` (con un CHECK de los cuatro valores), `observations` y `evaluated_on`, y la clave primaria `(project_id, version)`. Dos CHECK más reflejan las invariantes de `ProjectProfile`: la fecha de evaluación solo en una versión evaluada, y observaciones en una observada o rechazada.
  - Copia cada proyecto como versión 1, con `APPROVED` o `PENDING` según `profile_approved`. Una aprobación anterior no tiene fecha propia y toma la de la migración.
  - Elimina `profile_stored_file_id` y `profile_approved`. El CHECK de V5 cae junto con su columna. La regla "tutor solo con perfil aprobado" queda en el dominio, porque cruza tablas.

### Documentos

- `REQUIREMENTS.md`:
  - FR-005 pasa a decir "aprobación".
  - Requisitos nuevos: el aviso al estudiante de cada evaluación de su perfil, con las observaciones, y el aviso al director del perfil aprobado.
  - NFR-003 admite texto libre en el cuerpo del correo.
  - La nota de FR-003 cita UC-003 para el tribunal.
- `GLOSSARY.md`:
  - "Visto bueno" queda solo para el documento del proyecto.
  - Términos nuevos: "Estado del perfil" (`ProjectProfileStatus`) y "Evaluación del perfil".
  - "Observación" del perfil se asocia a `ProjectProfile.observations`.
- `USER-GUIDE.md`: los tres comandos nuevos, la versión nueva con `proyectos.registrarPerfil`, y en `proyectos.asignarTutor`, la aprobación con `proyectos.aprobarPerfil`.
- `use-cases/UC-004.md`: reemplazar la línea "Pendiente de agregar" por los IDs de los requisitos nuevos.
- `openspec/specs/degree-projects/spec.md`: su propósito queda solo en la asignación del tutor.

## Riesgos y compensaciones

- [El cuerpo del correo puede traer la cita del mensaje anterior o una firma] → Se guarda tal como llega, sin espacios al inicio ni al final. La guía de usuario pide escribir solo las observaciones.
- [No hay un director activo al aprobar] → La aprobación no depende del aviso. Se registra, se avisa al estudiante y se deja constancia en el log (confirmado por el usuario el 2026-11-23).
- [La regla tutor-aprobado deja de estar en un CHECK de la base] → La garantizan `DegreeProject.assignTutor` y sus pruebas, y el trámite se bloquea antes de escribir.
- [Hay datos existentes en `degree_project`] → V12 los copia como versión 1 antes de eliminar las columnas. Se prueba con Testcontainers.

## Plan de migración

Flyway aplica V12 al arrancar. No hay vuelta atrás automática. Si hiciera falta revertir, sería con una migración posterior, porque V5 y V8 no se editan.

## Diferencias con el modelo

UC-004 sin diseño en EA; se sigue la arquitectura de referencia.

- Dominio:
  - Nuevos: `ProjectProfileStatus`, `DegreeProjectProfileApprovedEvent`, `DegreeProjectProfileObservedEvent` y `DegreeProjectProfileRejectedEvent`.
  - `ProjectProfile` pasa a ser una versión con estado.
  - `DegreeProject.registerProfile`, `approveProfile`, `observeProfile`, `rejectProfile` y `getProfiles`.
- Aplicación: `ProjectProfileEvaluationUseCase`, `ProjectProfileEvaluationNotifier`, `RegisterProfileCommand`, `ObserveProfileCommand`, `RejectProfileCommand` y los cuatro handlers.
- Permisos: `REGISTER_PROJECT_PROFILE` y `EVALUATE_PROJECT_PROFILE`.
- Infraestructura: `MailProjectProfileEvaluationNotifier`, `DegreeProjectProfileVersion` y la tabla `degree_project_profile_version`.
- Almacenamiento: el perfil no usa el historial de archivos (`appendVersion`, `getHistory`), que queda en desuso; UC-002 lo sigue usando hasta que se migre.
