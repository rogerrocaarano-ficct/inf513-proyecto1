# Design

## Context

La realización de diseño de UC-004 (`docs/model/use-cases/UC-004-gestionar-proyecto-de-grado/design/realization.md`) define las clases, operaciones, la tabla y los comandos, y es la referencia de este cambio. Este diseño agrega solo lo que la realización no resuelve y que salió de compararla con el código de UC-003:

- `ChangeLogEntryRepository` ya tiene `findByTargetTypeAndTargetIdOrderByChangedAt`, pero `ChangeLog` solo expone `record`. `ChangeLogService.record` usa un solo `Instant.now(clock)` para todas las entradas de una llamada.
- `GraduationProcess` no mapea sus participantes: `ProcessParticipation` los consulta con SQL nativo sobre `graduation_process_participants`, que todavía nadie llena (UC-005).
- `StudentAccount` tiene `id`, `fullName`, `email` y `created`, pero no el registro universitario.
- `GraduationStage` tiene `OPENED` y `FINAL_GRADE_RECORDED`, y se guarda como texto (`EnumType.STRING`).
- `Pop3MailboxPoller` lee cada adjunto entero en memoria y no limita su tamaño. El sondeo usa `fixedDelay`, así que los comandos se procesan de a uno.
- Las pruebas de integración envían correos con GreenMail (`MailTestSupport`); los ejemplos manuales usan la API REST de Mailpit (`src/test/http/`). El trigger de `change_log_entries` impide `UPDATE` y `DELETE`, no `INSERT`.

## Goals / Non-Goals

**Goals:**

- Que el código siga la realización de diseño de UC-004, y que la realización se corrija donde la implementación lo exija.
- Que todos los flujos de UC-004 se puedan probar sin UC-005 ni UC-006.
- Que un rechazo no deje filas, registros de cambios, archivos ni avisos.

**Non-Goals:**

- Cambiar el estado del perfil o de una versión, designar al tutor o aprobar la predefensa: lo hacen UC-005 y UC-006.
- Descargar los documentos guardados (UC-008).
- Probar 25 MB contra `mail.tecnoweb.org.bo` o limitar el tamaño de los correos en Messaging.

## Decisions

### D1. Historial en Audit

`ChangeLog` suma `history(targetType, targetId): List<LoggedChange>`, con `LoggedChange(changedAt, actorId, field, oldValue, newValue)` en `audit/application`. `ChangeLogService` lo implementa con la consulta que ya existe, en una transacción de solo lectura que se une a la del comando.

Alternativa descartada: que GraduationProcesses use `ChangeLogEntryRepository` directamente. Saltaría la interfaz del subsistema y lo acoplaría a la entidad `ChangeLogEntry`.

### D2. Armado del historial

`ProjectHistory.from(changes)` (dominio de GraduationProcesses) agrupa las entradas por `changedAt`, en orden:

- Un grupo con el dato `storageKey` es una entrega: abre un `ProjectHistoryItem` con `documentKind`, `title`, `fileName`, `versionNumber`, `generalComment`, `status` y la fecha. Los datos que el grupo no cambia se toman del ítem anterior (p. ej., el título de una versión).
- Un grupo sin `storageKey` con el dato `status` actualiza el estado del último ítem: así llegarán los cambios de UC-006.
- La respuesta de `ConsultarProyecto` muestra el último ítem como documento vigente y los anteriores como historial.

Cada entrega registra siempre los siete datos, aunque no cambien, para que cada ítem se arme solo con su grupo. Los nombres de dato son constantes de `GraduationProject`, compartidas con `ProjectHistory`.

Alternativa descartada: agrupar por un identificador de entrega guardado en el registro de cambios. Obligaría a cambiar `change_log_entries`, y la fecha y hora común ya identifica cada llamada a `record`.

### D3. Entidad `GraduationProject` y migración `V5`

`GraduationProject` va en `graduationprocesses/domain`, con `graduationProcessId` como columna (no como `@OneToOne`), igual que `studentAccountId` en `GraduationProcess`. Así UC-003 no cambia, y el proyecto se busca con `findByGraduationProcessId`.

- `V5__crear_proyectos_de_grado.sql` crea `graduation_projects` con las columnas, checks e índices de `database.md`.
- `@Version long version` da el bloqueo optimista. Hoy no se ejercita, porque los comandos se procesan de a uno, pero protege la versión web.
- `ProjectStatus` y `ProjectDocumentKind` se guardan como texto.
- Los avisos y las respuestas usan los textos de `label()` de cada enum.

### D4. Reglas en la entidad

`GraduationProject` decide qué se admite, con estas operaciones:

- `checkFile(file)` (estática): exige `.pdf`, tipo `application/pdf` o genérico, contenido no vacío y hasta `MAX_SIZE_BYTES = 26_214_400`. Rechaza con `INVALID_FILE`, como `InitialDocument.check`.
- `submitProfile(...)`: exige `PROJECT_PROFILE` en `PROFILE_OBSERVED` o `PROFILE_REJECTED`; si no, `PROFILE_NOT_ALLOWED`, con el estado vigente en el detalle.
- `checkVersionAllowed(predefenseApproved)`: primero `VERSION_PREREQUISITES` si el documento es un perfil sin `PROFILE_APPROVED`; después `VERSION_NOT_ALLOWED` para `TUTOR_REVIEW`, `TRIBUNAL_REVIEW`, `APPROVED` sin predefensa y `APPROVED` de `FINAL_DEFENSE_VERSION`.
- `submitVersion(...)`: vuelve a llamar a `checkVersionAllowed`, para que la entidad no dependa de que el servicio lo haya hecho.

El servicio verifica antes, y en este orden: el trámite en curso (`PROCESS_NOT_FOUND`), el proyecto (sin proyecto, `VERSION_PREREQUISITES`), las reglas de la entidad, el tutor (`VERSION_PREREQUISITES`), la cantidad de adjuntos (`ATTACHMENT_COUNT`) y el archivo. Solo entonces guarda el archivo. Las reglas van antes que el tutor porque el tutor se designa después del visto bueno: sin visto bueno ni tutor, el estudiante conoce primero lo que falta antes.

### D5. Etapa de predefensa

`GraduationStage` queda `OPENED`, `PREDEFENSE_APPROVED`, `FINAL_GRADE_RECORDED`, en ese orden, con un comentario que lo exige. `isPredefenseApproved()` compara `stage.compareTo(PREDEFENSE_APPROVED) >= 0`. Una prueba fija el orden.

Alternativa descartada: un campo booleano `predefenseApproved` en el trámite. Repetiría la etapa, que ya existe para eso.

### D6. Tutor y contactos

- `GraduationProcessRepository.findParticipantIds(processId, kind)` es una consulta nativa sobre `graduation_process_participants`, como `findInProgressByParticipant`.
- El puerto `ProfessorContacts` va en `graduationprocesses/application`, y `UserProfessorContacts` en `users/adapter/in`. Este último usa solo `UserAccountRepository` (`findActiveWithRole` en JPQL, y `findById` filtrado por cuenta activa). No usa `UserManagementService`, así que no se forma un ciclo de beans con `GraduationParticipation`.
- `StudentAccounts.findById` lo implementa `UserStudentAccounts` con `UserAccountRepository.findById`, filtrado por el rol de estudiante.
- Los avisos nombran al estudiante por su nombre completo y su dirección de correo, porque `StudentAccount` no tiene el registro universitario. La realización se corrige (D9).

### D7. Comandos y respuestas

Los tres manejadores van en `graduationprocesses/adapter/in` con `allowedRoles() = {STUDENT}` (`GraduationCommandSupport.STUDENT_ROLES`):

- `PresentarPerfil` exige 1 argumento y responde `Perfil de proyecto presentado: <título>. Se avisó al docente de Taller de Grado.`, o `No hay un docente de Taller de Grado activo a quien avisar.` en lugar de la segunda frase.
- `EntregarVersion` exige 1 argumento y responde `Versión <n> del documento del proyecto entregada.`, o `... entregada como versión para la defensa final.`
- `ConsultarProyecto` no admite argumentos (`INVALID_DATA` si llegan) y responde en texto plano: el título, el documento vigente con su tipo, número y estado, y el historial, del más reciente al más antiguo, una línea por ítem con fecha, tipo, número, comentario y estado. Sin proyecto: `Todavía no presentó un perfil de proyecto.`

`GraduationRejectionReason` suma `PROFILE_NOT_ALLOWED` ("No se admite un perfil de proyecto nuevo"), `VERSION_PREREQUISITES` ("Todavía no puede entregar el documento del proyecto"), `VERSION_NOT_ALLOWED` ("No se admite una versión nueva") e `INVALID_FILE` ("Documento no válido").

Avisos (texto plano, después de confirmar): `Perfil de proyecto para evaluar` al docente de Taller de Grado y `Versión del documento del proyecto para revisar` al tutor.

### D8. Fijar los estados de UC-005 y UC-006

Las pruebas y los ejemplos fijan con SQL los estados que todavía no produce ningún caso de uso:

- El tutor, con un `INSERT` en `graduation_process_participants` (`kind = 'TUTOR'`).
- El estado del documento vigente, con un `UPDATE` de `graduation_projects.status`, más un `INSERT` en `change_log_entries` del dato `status` con otra fecha y hora. Así el historial muestra el estado, como lo hará UC-006.
- La predefensa aprobada, con un `UPDATE` de `graduation_processes.stage`.

En las pruebas, un helper (`GraduationProjectFixtures`) concentra estas sentencias con `JdbcTemplate`. En los ejemplos, los archivos `.http` documentan los comandos `docker compose exec postgres psql ...`, como `10-close-graduation-process.http`.

### D9. Modelo

Este cambio actualiza el modelo así:

- **Realización de diseño de UC-004:**
  - Los avisos usan el nombre y la dirección de correo del estudiante (D6).
  - El orden de verificación y los motivos de rechazo (D4, D7).
  - El armado del historial (D2) y los fixtures de prueba (D8).
- **`audit.md`:** `LoggedChange` y `history`, si la firma cambia.
- **`use-cases.md`:** sin cambios de estado (el diseño sigue en Draft).

## Risks / Trade-offs

- [Un PDF de 25 MB pesa unos 34 MB en base64, y `Pop3MailboxPoller` lo lee entero en memoria] → Una prueba de punta a punta con GreenMail envía 25 MB exactos (aceptado) y 25 MB más un byte (rechazado). Si falla por memoria, se fija `-Xmx` en el `Dockerfile` y en la tarea de pruebas de Gradle.
- [El servidor del docente puede rechazar correos de ese tamaño, y el estudiante recibiría un rebote sin que el sistema lo vea] → Fuera de este cambio; queda como pregunta abierta para la iteración en que se pruebe contra `mail.tecnoweb.org.bo`.
- [El historial depende de los nombres de los datos registrados y de la fecha y hora común de cada `record`] → Los nombres son constantes compartidas, y las pruebas cubren el armado con perfiles reemplazados, versiones y cambios de estado posteriores.
- [Los archivos de las versiones anteriores no se borran nunca] → Es lo que pide FR-010. El espacio crece con cada entrega, a lo sumo 25 MB por entrega, en el volumen `files`.
- [Los estados fijados con SQL pueden no coincidir con lo que haga UC-006] → Los nombres de estado y de dato quedan en la realización, y UC-006 los confirma al diseñarse (pregunta abierta de la realización).

## Migration Plan

1. `V5` se aplica sola al arrancar (Flyway, AD-011). Solo crea `graduation_projects`; no modifica tablas existentes.
2. `GraduationStage` suma una constante en medio. Como se guarda como texto, los trámites existentes no cambian.
3. Rollback: volver a la imagen anterior y borrar `graduation_projects` y su fila en `flyway_schema_history`. Los archivos de `graduation-projects/` en el volumen `files` se pueden borrar.

## Open Questions

- ¿El servidor `mail.tecnoweb.org.bo` acepta correos de unos 34 MB? Se responde al probar contra él, en una iteración posterior.
