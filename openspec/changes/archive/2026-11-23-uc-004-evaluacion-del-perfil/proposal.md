# Propuesta

## Motivo

Pertenece a **UC-004 Evaluación del perfil de proyecto**, que está descrito en `use-cases/UC-004.md`. Hoy el estudiante presenta su perfil (`proyectos.registrarPerfil`), pero el perfil queda pendiente para siempre: ningún comando lo aprueba, lo observa ni lo rechaza. Por eso la asignación del tutor (UC-003) se rechaza siempre, y un estudiante al que le observaron o rechazaron el perfil no puede presentarlo de nuevo.

El caso de uso no tiene diseño en EA (el modelo está en pausa); se sigue la arquitectura de referencia de `AGENTS.md`.

## Cambios

- El docente de Taller de Grado evalúa el perfil pendiente de un trámite: lo **aprueba**, lo **observa** (cambios de forma) o lo **rechaza** (cambio de tema). Las observaciones se toman del cuerpo del correo y no pueden estar vacías (FR-005, FR-022).
- El estudiante presenta con el mismo comando el primer perfil o una versión nueva, cuando el último perfil fue observado o rechazado. Cada versión se numera en orden desde 1 y se guarda en el proyecto, no en el historial de archivos del almacenamiento, que pasa a estar en desuso. El título del proyecto puede cambiar (FR-023, FR-058).
- Se conservan todas las versiones del perfil con su estado, sus observaciones y la fecha de su evaluación.
- El estudiante recibe cada evaluación con las observaciones, y el director de carrera recibe el aviso del perfil aprobado (requisitos nuevos).
- La presentación del perfil pasa de UC-003 a UC-004. La asignación del tutor exige ahora el perfil vigente **aprobado**.
- Documentos: FR-005 dice "aprobación" en lugar de "visto bueno"; se agregan los dos FR de notificación; NFR-003 admite texto libre en el cuerpo del correo; la nota de FR-003 cita UC-003 para el tribunal; se actualizan el glosario y la guía de usuario.

## Capacidades

### Capacidades nuevas

- `project-profiles`: presentación del perfil de proyecto, sus versiones y su evaluación por el docente de Taller de Grado.

### Capacidades modificadas

- `degree-projects`: el requisito "Primera entrega del perfil" se retira y pasa a `project-profiles`. La asignación del tutor exige el perfil **vigente** aprobado, en lugar del "visto bueno", que ahora sí se puede producir.

## Impacto

- `procedures.domain`: `DegreeProject` pasa de un perfil a una lista de versiones con estado; eventos nuevos de aprobación, observación y rechazo.
- `procedures.application`: caso de uso nuevo `ProjectProfileEvaluationUseCase` con su notifier y tres comandos nuevos (`proyectos.aprobarPerfil`, `proyectos.observarPerfil`, `proyectos.rechazarPerfil`). `TutorAssignmentUseCase` se queda solo con la asignación del tutor.
- `users.domain`: un permiso nuevo para evaluar perfiles.
- Persistencia: una migración Flyway nueva para las versiones del perfil, que mueve los datos existentes.
- Canal de correo: sin cambios; es el primer comando que usa el cuerpo del correo, que `ReceivedEmail` ya conserva.
- Documentos: `REQUIREMENTS.md`, `GLOSSARY.md`, `USER-GUIDE.md` y el propósito de la spec `degree-projects`.
