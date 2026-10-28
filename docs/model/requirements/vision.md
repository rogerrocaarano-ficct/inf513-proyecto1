# Vision

## Problem

El trámite de titulación por Proyecto de Grado de la carrera de Ingeniería en Redes y Telecomunicaciones pasa por muchas etapas y personas. El estudiante solicita el trámite y presenta su perfil de proyecto. El docente de Taller de Grado evalúa su viabilidad, el director de carrera designa al tutor y al tribunal evaluador, y el tribunal revisa el documento hasta aprobarlo. Luego se programan la predefensa y la defensa final, y al terminar se emite el acta de graduación y se archivan los ejemplares físicos. Hoy todo se lleva en papel: carpetas físicas, cartas dirigidas a la Dirección de Carrera y notificaciones personales a cargo del secretario(a) de dirección de carrera.

Por eso nadie sabe en qué etapa está un trámite sin acudir a la secretaría a preguntar. Esto afecta a los estudiantes, a los docentes del proceso y a la propia dirección. Tampoco se pueden obtener estadísticas de titulación sin revisar las carpetas una por una.

Con el sistema, cada etapa del trámite queda registrada en un expediente digital que todos los participantes pueden consultar según su rol, y la dirección obtiene reportes y estadísticas a partir de esos registros. Criterio de éxito: cualquier participante consulta el estado de un trámite sin acudir a la secretaría, y el 100% de los trámites del semestre quedan registrados en el sistema.

## Stakeholders

| ID | Stakeholder | Main interest |
| --- | --- | --- |
| STK-001 | Dirección de Carrera (director de carrera y secretario(a) de dirección de carrera) | Registrar el trámite y sus documentos sin carpetas en papel, designar tutores y tribunales, programar las evaluaciones, dejar de atender consultas de estado en persona y conocer el estado y las estadísticas de titulación de la carrera |
| STK-002 | Docentes del proceso (docente de Taller de Grado, tutor y tribunal evaluador) | Evaluar la viabilidad de los perfiles de proyecto, seguir y revisar el avance de los proyectos, registrar observaciones y calificar la predefensa y la defensa final |
| STK-003 | Estudiante titulando | Saber en qué etapa está su trámite, qué observaciones debe subsanar y cuándo son sus evaluaciones |
| STK-004 | Docente de Tecnología Web (Balcazar Veizaga, Evans) | Aprobar el alcance y los casos de uso del proyecto y evaluarlo como trabajo de la materia |

## Key Features

| ID | Feature | Need it addresses | Use cases (derived) | Priority |
| --- | --- | --- | --- | --- |
| FEAT-001 | Gestión de usuarios | Todos los participantes del trámite necesitan una cuenta con sus roles para usar el sistema (CU1, SRC-002) | UC-001, UC-002 | Must |
| FEAT-002 | Habilitación académica de solicitudes | La solicitud y los documentos iniciales del estudiante se reciben en papel y no se sabe si el trámite fue habilitado (CU2, SRC-002) | UC-003 | Must |
| FEAT-003 | Asignación de tutor y registro del perfil de proyecto | La designación del tutor y la entrega del perfil de proyecto no quedan registradas en ningún lugar consultable (CU3, SRC-002) | UC-004, UC-005 | Must |
| FEAT-004 | Evaluación de viabilidad y conformación del tribunal | La evaluación del perfil y la designación de los tres miembros del tribunal se comunican por papel o en persona (CU4, SRC-002) | UC-005, UC-006 | Must |
| FEAT-005 | Revisión, versionado y control de plazos del tribunal | Las observaciones del tribunal, las correcciones del estudiante y sus plazos quedan dispersos en cartas y versiones impresas (CU5, SRC-002) | UC-004, UC-006 | Must |
| FEAT-006 | Programación y registro de evaluaciones | La programación de la predefensa y la defensa final, sus resultados, el acta de graduación, la entrega de ejemplares y el cierre del expediente se registran en papel (CU6, SRC-002) | UC-003, UC-006, UC-007 | Must |
| FEAT-007 | Monitoreo, trazabilidad, dashboard y calendario académico | Nadie sabe en qué etapa está un trámite ni qué evaluaciones vienen sin preguntar en la secretaría (CU7, SRC-002) | UC-008, UC-009, UC-010 | Must |
| FEAT-008 | Reportes y estadísticas | La dirección no puede obtener estadísticas de titulación sin revisar las carpetas una por una (CU8, SRC-002) | UC-010 | Must |

## Scope

**In scope:**

- FEAT-001, FEAT-002, FEAT-003, FEAT-004, FEAT-005, FEAT-006, FEAT-007, FEAT-008, en dos entregas que incluyen todas las funcionalidades: primero una prueba de concepto operada por correo electrónico (CON-001) y después la versión final, una aplicación web que la reemplaza (CON-002).

**Out of scope:**

- Ninguno por ahora.

## Open Questions

- [ ] FEAT-001 … FEAT-008: ¿acepta el docente que sus CU1–CU8 se modelen como funcionalidades, con los casos de uso derivados de cada una? Ver la decisión de modelado en `docs/sources/2026-10-06.md` (ask: Docente de Tecnología Web)
- [ ] FEAT-006: ¿acepta el docente incluir la entrega de ejemplares físicos y el cierre del expediente (RF17, RF18) en su CU6? (ask: Docente de Tecnología Web)
- [ ] FEAT-003: el tutor se asigna junto con el tribunal, después de evaluar la viabilidad del perfil; ¿se mantiene en CU3 o pasa a CU4? (ask: Docente de Tecnología Web)
- [ ] Criterio de éxito: ¿la dirección de carrera lo acepta como medida de éxito? (ask: Director de carrera)
- [x] Alcance: ¿qué funcionalidades entran en la primera entrega (por correo) y cuáles en la segunda (web)? (ask: Docente de Tecnología Web)

## Approval

| Stakeholder | Decision | Date |
| --- | --- | --- |
| <!-- STK-001 --> | <!-- Accepted / Accepted with changes / Rejected --> | <!-- YYYY-MM-DD --> |
