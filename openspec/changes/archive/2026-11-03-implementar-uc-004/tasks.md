# Tasks

## 1. Audit: historial de un registro

- [x] 1.1 Agregar `history(targetType, targetId)` a `ChangeLog` y el record `LoggedChange` en `audit/application`, e implementarlo en `ChangeLogService` con `findByTargetTypeAndTargetIdOrderByChangedAt` en una transacción de solo lectura (D1); verificar con pruebas de integración los escenarios de "Consulta del historial de un registro" de la spec `change-log` (orden, entradas de un mismo cambio con la misma fecha y hora, registro sin entradas)
- [x] 1.2 Documentar `history` y `LoggedChange` en `docs/model/design/subsystems/audit.md` si la firma difiere de la realización; verificar que el texto coincide con el código

## 2. GraduationProcesses: dominio y persistencia

- [x] 2.1 Escribir `V5__crear_proyectos_de_grado.sql` con `graduation_projects` según `database.md` (D3); verificar con `MigrationsTests` que se aplican las versiones 1 a 5, que el índice único impide dos proyectos en un trámite y que el check rechaza un perfil con número de versión
- [x] 2.2 Agregar `PREDEFENSE_APPROVED` a `GraduationStage` en el orden del trámite, con su comentario, y `GraduationProcess.isPredefenseApproved()` (D5); verificar con pruebas unitarias el orden de las constantes y el resultado para `OPENED`, `PREDEFENSE_APPROVED` y `FINAL_GRADE_RECORDED`
- [x] 2.3 Implementar `ProjectDocumentKind`, `ProjectStatus` (con `label()`), `GraduationProject` (con `@Version`, las constantes de los nombres de dato, `checkFile`, `createWithProfile`, `submitProfile`, `checkVersionAllowed`, `submitVersion` y `describe`) y `GraduationProjectRepository` (D3, D4); verificar con pruebas unitarias los escenarios de "Formato y tamaño del documento" (25 MB exactos, 25 MB más un byte, `.docx`, vacío), "Perfil de proyecto nuevo", "Requisitos para entregar el documento del proyecto", "Entrega de una versión del documento del proyecto" y "Versión para la defensa final", y que cada operación devuelve los siete datos del registro de cambios
- [x] 2.4 Agregar `PROFILE_NOT_ALLOWED`, `VERSION_PREREQUISITES`, `VERSION_NOT_ALLOWED` e `INVALID_FILE` a `GraduationRejectionReason` con los textos de D7; verificar con las pruebas de 2.3 que cada rechazo usa su motivo
- [x] 2.5 Implementar `ProjectHistory.from` y `ProjectHistoryItem` (D2); verificar con pruebas unitarias el armado de un perfil reemplazado, de una versión que hereda el título y de un cambio de estado posterior sin `storageKey`

## 3. Contactos de los docentes y del estudiante

- [x] 3.1 Crear en `graduationprocesses/application` el puerto `ProfessorContacts` con `ProfessorContact`, y agregar `findById` a `StudentAccounts` (D6); verificar con `./gradlew compileJava` que `graduationprocesses` no importa nada de `users`
- [x] 3.2 Agregar `findActiveWithRole` a `UserAccountRepository`, implementar `UserProfessorContacts` en `users/adapter/in` con `UserAccountRepository` y `UserStudentAccounts.findById` (D6); verificar con pruebas de integración que `thesisWorkshopProfessor` devuelve la cuenta activa con ese rol o vacío, que `findById` no devuelve una cuenta desactivada, y que el contexto de Spring arranca sin ciclo de beans

## 4. GraduationProcesses: servicio

- [x] 4.1 Agregar `findParticipantIds` a `GraduationProcessRepository` (D6) y el helper de pruebas `GraduationProjectFixtures` con el tutor, el estado del documento con su entrada en el registro de cambios y la predefensa aprobada (D8); verificar con una prueba de integración que el helper deja el tutor, el estado y la etapa leídos por los repositorios
- [x] 4.2 Implementar `GraduationProjectService.submitProfile`, con el orden de verificación de D4, el archivo en la categoría `graduation-projects`, el registro de cambios y el aviso al docente de Taller de Grado; verificar con pruebas de integración los escenarios de "Presentación del primer perfil de proyecto" (incluido sin docente de Taller de Grado activo) y "Perfil de proyecto nuevo", y que el archivo del perfil anterior sigue guardado ("Conservación de los documentos anteriores")
- [x] 4.3 Implementar `GraduationProjectService.submitVersion`, con el tutor, la predefensa, el archivo, el registro de cambios y el aviso al tutor (se omite si su cuenta está desactivada); verificar con pruebas de integración los escenarios de "Requisitos para entregar el documento del proyecto", "Entrega de una versión del documento del proyecto" y "Versión para la defensa final", con los estados fijados por `GraduationProjectFixtures`
- [x] 4.4 Implementar `GraduationProjectService.viewProject` con `ChangeLog.history` y `ProjectHistory`, y el record `ProjectView`; verificar con pruebas de integración los escenarios de "Consulta del proyecto de grado"
- [x] 4.5 Verificar con una prueba de integración el escenario "Rechazo después de guardar el archivo": un rechazo tras `FileStorage.store` no deja el proyecto, el registro de cambios ni archivos en `app.files.root`, y no envía avisos

## 5. GraduationProcesses: comandos por correo

- [x] 5.1 Agregar `STUDENT_ROLES` a `GraduationCommandSupport` e implementar `SubmitProjectProfileCommandHandler`, `SubmitDocumentVersionCommandHandler` y `ViewGraduationProjectCommandHandler` con las respuestas de D7; verificar con pruebas de punta a punta en GreenMail los escenarios de "Comandos del proyecto de grado" y "Datos del comando", un perfil presentado con el aviso al docente de Taller de Grado, una versión entregada con el aviso al tutor y una consulta con historial
- [x] 5.2 Agregar a las pruebas de punta a punta un PDF de 25 MB exactos (aceptado) y uno de 25 MB más un byte (rechazado con `INVALID_FILE`); verificar que pasan con la memoria por defecto de la tarea `test` de Gradle, y si no, fijar `-Xmx` en `build.gradle.kts` y en el `Dockerfile` y documentarlo en `README.md`
- [x] 5.3 Agregar `src/test/http/11-submit-project-profile.http`, `12-submit-document-version.http` y `13-view-graduation-project.http` con la API de Mailpit (`POST /api/v1/send` con `Attachments` en base64): el perfil, un perfil rechazado por estar pendiente, el perfil nuevo tras fijar `PROFILE_OBSERVED`, la versión tras fijar `PROFILE_APPROVED` y el tutor, la versión para la defensa final tras fijar `APPROVED` y `PREDEFENSE_APPROVED`, y la consulta, con los comandos `docker compose exec postgres psql ...` de D8; agregar el buzón del docente de Taller de Grado y del tutor a `mailbox.http` y mencionar los archivos en el `README.md`; verificar ejecutándolos contra el stack de Docker Compose y leyendo las respuestas con `mailbox.http`

## 6. Modelo

- [x] 6.1 Actualizar la realización de diseño de UC-004 con los avisos por nombre y dirección de correo, el orden de verificación, los motivos de rechazo, el armado del historial y los fixtures (D9), y `database.md` si la migración difiere; verificar con los checks de `docs/model/use-cases/AGENTS.md` y `docs/model/design/AGENTS.md` que cada clase del código está en su realización y que los derivados están al día

## 7. Integración

- [x] 7.1 Ejecutar `./gradlew test` completo y, con el stack de Docker Compose, presentar un perfil y consultar el proyecto con los ejemplos `.http`; verificar que llegan la confirmación al estudiante, el aviso al docente de Taller de Grado y el archivo en el volumen `files`

## Workflow follow-up

- Integrar `UC-004-gestionar-proyecto-de-grado` en `develop` y archivar el cambio con `/opsx:archive`.
