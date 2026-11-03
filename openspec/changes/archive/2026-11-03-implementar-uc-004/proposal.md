# Proposal

## Why

UC-004 es arquitectónicamente significativo: recibe por correo los archivos del proyecto de grado, de hasta 25 MB (CON-004), y conserva el historial de sus versiones. Su modelo ya está detallado, analizado y diseñado sobre el trámite de titulación de UC-003, que ya está implementado, así que es el siguiente caso de uso que se implementa.

## What Changes

- Comandos del proyecto de grado por correo, permitidos al estudiante sobre su propio trámite en curso:
  - `PresentarPerfil(titulo)` con el perfil de proyecto en PDF como único adjunto (FR-004, FR-023, FR-058).
  - `EntregarVersion(comentario)` con el documento del proyecto en PDF como único adjunto (FR-008, FR-010, FR-025, FR-040).
  - `ConsultarProyecto()`: el título, el documento vigente con su estado y el historial de perfiles y versiones (FR-059).
- Reglas del proyecto de grado:
  - El primer perfil crea el proyecto de grado; un perfil nuevo solo se admite si el anterior fue rechazado u observado.
  - Una versión solo se admite con el perfil con visto bueno y un tutor asignado, y según el estado de revisión de la versión vigente; después de aprobar la predefensa, la versión es la versión para la defensa final.
  - Los archivos son PDF de hasta 25 MB.
- Avisos por correo al docente de Taller de Grado de cada perfil (FR-057) y al tutor de cada versión (FR-041).
- Historial: no hay tabla de versiones. Cada entrega queda en el registro de cambios, que suma la lectura del historial de un registro, y los archivos anteriores no se borran.
- Ejemplos `.http` de los tres comandos con la API de Mailpit, con los estados que todavía no producen UC-005 y UC-006 fijados con `UPDATE` en la base de datos.

Fuera de esta propuesta:

- Evaluar el perfil y revisar las versiones (UC-006) y designar al tutor (UC-005). Hasta entonces, los estados `PROFILE_OBSERVED`, `PROFILE_REJECTED`, `PROFILE_APPROVED`, `TUTOR_OBSERVED`, `TRIBUNAL_OBSERVED` y `APPROVED`, la etapa `PREDEFENSE_APPROVED` y el tutor del trámite solo se alcanzan con un cambio directo en la base de datos, en las pruebas y en los ejemplos `.http`.
- Probar los adjuntos de 25 MB contra el servidor de correo del docente (`mail.tecnoweb.org.bo`, CON-006): queda para una iteración posterior. Esta propuesta los prueba solo con GreenMail.

## Capabilities

### New Capabilities

- `graduation-projects`: el proyecto de grado del estudiante, desde la presentación del perfil de proyecto hasta la entrega de la versión para la defensa final, con sus reglas de admisión, sus avisos a los docentes y la consulta de su historial.

### Modified Capabilities

- `change-log`: el registro de cambios devuelve las entradas de un registro en orden, para armar el historial de un proyecto de grado.

## Impact

- **Código:**
  - `graduationprocesses`:
    - Nuevos: los tres manejadores de comando, `GraduationProjectService`, la entidad `GraduationProject` con `ProjectStatus` y `ProjectDocumentKind`, `ProjectHistory`, `GraduationProjectRepository` y el puerto `ProfessorContacts`.
    - Cambian: `GraduationStage` (suma `PREDEFENSE_APPROVED`), `GraduationProcess.isPredefenseApproved`, `GraduationProcessRepository.findParticipantIds`, `StudentAccounts.findById`, `GraduationCommandSupport` y `GraduationRejectionReason`.
  - `audit`: `ChangeLog.history` y el record `LoggedChange`.
  - `users`: `UserProfessorContacts`, `UserStudentAccounts.findById` y `UserAccountRepository.findActiveWithRole`.
- **Base de datos:** migración `V5` con `graduation_projects`.
- **Pruebas y ejemplos:** pruebas unitarias de las reglas, pruebas de punta a punta con GreenMail (incluido un PDF de 25 MB) y archivos `src/test/http/11` a `13`.
- **Modelo:** la realización de diseño de UC-004, si el código se aparta de ella, y `README.md` si cambia.
