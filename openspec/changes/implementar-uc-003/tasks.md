# Tasks

## 1. Messaging: adjuntos

- [x] 1.1 En `Pop3MailboxPoller`, decodificar el nombre con `MimeUtility.decodeText` (D1); verificar con pruebas en `MailCommandsTests` que un adjunto con nombre `Cédula.pdf` en RFC 2047 y otro en RFC 2231 llegan al comando como `Cédula.pdf`
- [x] 1.2 Entregar al comando solo las partes con `Content-Disposition: attachment` (D2); verificar con una prueba en `MailCommandsTests` que un correo con `carta.pdf` adjunto y `logo.png` inline entrega solo `carta.pdf`, y que el escenario "Correo con adjunto" sigue pasando
- [x] 1.3 Ampliar `MailTestSupport` con el envío de varios adjuntos, de partes inline y de nombres codificados, para las pruebas de los grupos siguientes; verificar que las pruebas de 1.1 y 1.2 lo usan
- [x] 1.4 Documentar el filtrado y la decodificación en `docs/model/design/subsystems/messaging.md` (tipos `MailAttachment` y `CommandHandler`); verificar que el texto coincide con la spec `mail-commands`

## 2. Storage

- [x] 2.1 Crear `storage/application/FileStorage` y `storage/adapter/out/LocalFileStorage` con `app.files.root=${FILES_ROOT:./data/files}`, la escritura con `CREATE_NEW`, el borrado al deshacer, el borrado después de confirmar y el rechazo de claves fuera de la raíz (D4); verificar con pruebas que un archivo guardado en una transacción deshecha no queda, que uno borrado en una transacción deshecha sigue existiendo, y que `../x` se rechaza
- [x] 2.2 Crear `/app/data/files` con dueño `mailapp` en el `Dockerfile`, montar el volumen `files` con `FILES_ROOT=/app/data/files` en `docker-compose.yml` y agregar `FILES_ROOT` a `.env.example` y al `README.md`; verificar con `docker compose up --build` que `mail-app` arranca y puede escribir en el volumen

## 3. GraduationProcesses: dominio y persistencia

- [x] 3.1 Escribir `V4__crear_tramites_de_titulacion.sql` con `graduation_processes`, `graduation_process_participants` e `initial_documents` según `database.md` (D6); verificar con `MigrationsTests` que la migración se aplica y que el índice parcial impide dos trámites en curso del mismo estudiante
- [x] 3.2 Implementar `InitialDocumentType` con `fromFileName` y `classify` (D3), y `InitialDocument.check` con el formato y el tamaño de CON-005; verificar con pruebas unitarias los escenarios de "Reconocimiento de los documentos iniciales" y de "Formato y tamaño de los documentos iniciales", incluidos `cv` como palabra separada y los nombres con tildes
- [x] 3.3 Implementar `GraduationProcess`, `InitialDocument`, `GraduationProcessStatus`, `GraduationStage`, `GraduationRuleViolationException`, `GraduationRejectionReason` y `GraduationProcessRepository`, con `open`, `replaceInitialDocument`, `close` y `describe` (D6); verificar con pruebas unitarias que `open` exige los cuatro tipos, que `replaceInitialDocument` devuelve el documento quitado, que `close` exige `FINAL_GRADE_RECORDED` y que los `FieldChange` son los de la realización

## 4. Users: cuenta del estudiante

- [x] 4.1 Crear en `graduationprocesses/application` el puerto `StudentAccounts` con `StudentData` y `StudentAccount(id, fullName, email, created)` (D5, D8); verificar con `./gradlew compileJava` que `graduationprocesses` no importa nada de `users`
- [x] 4.2 Agregar `findByStudentRegistrationNumber` a `UserAccountRepository`, `INACTIVE_ACCOUNT` a `RejectionReason`, `obtainStudentAccount` y `findStudentAccount` a `UserManagementService` (extrayendo `notifyCreated` de `createAccount`), y `UserStudentAccounts` en `users/adapter/in` (D5); verificar con pruebas de integración los escenarios de la spec `user-accounts` ("Cuenta del estudiante al abrir su trámite" y "Cuenta del estudiante desactivada"), y que las pruebas de `CrearUsuario` siguen pasando sin cambios

## 5. GraduationProcesses: servicio y participación

- [x] 5.1 Implementar `GraduationProcessService` con `openProcess` (devuelve `OpenedProcess`), `replaceInitialDocument` y `closeProcess`, el registro de cambios, los avisos al estudiante y la traducción del índice parcial a `PROCESS_IN_PROGRESS` (D6, D8); verificar con pruebas de integración los escenarios de "Apertura del trámite de titulación", "Un solo trámite en curso por estudiante", "Reemplazo de un documento inicial", "Cierre del trámite de titulación" (fijando `FINAL_GRADE_RECORDED` con un `UPDATE`) y "Rechazo sin cambios", este último comprobando que no quedan archivos en `app.files.root`
- [x] 5.2 Reemplazar `NoGraduationProcessesParticipation` por `ProcessParticipation`, cambiar `GraduationProcessSummary` a `(id, studentAccountId)` y resolver el nombre en Users con `AffectedProcess` (D7); verificar con `AffectedProcessesTests` ajustado, con una prueba del escenario "Desactivación de un estudiante con trámite en curso" y con `GraduationParticipationTests`

## 6. GraduationProcesses: comandos por correo

- [x] 6.1 Implementar `GraduationCommandSupport` y los manejadores de `AbrirTramite`, `ReemplazarDocumento` y `CerrarTramite` con `allowedRoles() = {PROGRAM_SECRETARY}` y las respuestas de D8; verificar con pruebas de punta a punta en GreenMail los escenarios de "Comandos del trámite de titulación", una apertura con `Cédula.pdf` codificado y una imagen inline, y que el estudiante recibe los avisos de creación de cuenta y de apertura
- [x] 6.2 Agregar `src/test/http/08-open-graduation-process.http`, `09-replace-initial-document.http` y `10-close-graduation-process.http` con la API de Mailpit (`POST /api/v1/send` con `Attachments` en base64): creación previa del secretario(a), apertura con los cuatro adjuntos y `Cédula.pdf`, apertura con una imagen inline (`ContentID`), los rechazos de documento faltante, repetido, no reconocido y `.docx`, el reemplazo, el cierre rechazado sin calificación final, el comando `docker compose exec postgres psql ...` que fija `FINAL_GRADE_RECORDED`, y el cierre; agregar los buzones del secretario(a) y del estudiante a `mailbox.http` y mencionar los archivos en el `README.md`; verificar ejecutándolos contra el stack de Docker Compose y leyendo las respuestas con `mailbox.http`

## 7. Modelo

- [x] 7.1 Actualizar la realización de diseño de UC-003 (`OpenedProcess`, `StudentAccount.created`, `LocalFileStorage`, `ProcessParticipation`), la de UC-001 (`AffectedProcess`, `GraduationProcessSummary`) y `docs/model/design/subsystems/graduation-processes.md` (D9); verificar con los checks de `docs/model/use-cases/AGENTS.md` y `docs/model/design/AGENTS.md` que cada clase del código está en su realización y que los derivados están al día

## 8. Integración

- [x] 8.1 Ejecutar `./gradlew test` completo y, con el stack de Docker Compose, abrir un trámite con los ejemplos `.http` y verificar que llegan la confirmación al secretario(a), los dos avisos al estudiante y los cuatro archivos en el volumen `files`

## Workflow follow-up

- Integrar `UC-003-gestionar-tramite-de-titulacion` en `develop` y archivar el cambio con `/opsx:archive`.
