# Actors

## Actor Catalog

| ID | Actor | Type | Kind | Description | Use cases (derived) |
| --- | --- | --- | --- | --- | --- |
| [ACT-001](ACT-001-estudiante.md) | Estudiante | Primary | Human | Postulante que realiza el trámite de titulación por Proyecto de Grado. | UC-003, UC-004, UC-008 |
| [ACT-002](ACT-002-secretario-de-direccion-de-carrera.md) | Secretario(a) de dirección de carrera | Primary | Human | Personal de la Dirección de Carrera que recibe los documentos del trámite y lleva el expediente. | UC-003, UC-008, UC-009 |
| [ACT-003](ACT-003-director-de-carrera.md) | Director de carrera | Primary | Human | Autoridad de la carrera, y también docente, que designa tutores y tribunales y programa las defensas. | UC-001, UC-005, UC-007, UC-009, UC-010 |
| [ACT-004](ACT-004-docente.md) | Docente | Primary | Human | Actor abstracto: docente de la carrera que participa en trámites de titulación como docente de Taller de Grado, tutor o miembro del tribunal. | UC-008 |
| [ACT-005](ACT-005-docente-de-taller-de-grado.md) | Docente de Taller de Grado | Primary | Human | Docente de la materia Taller de Grado, único en la carrera, que evalúa la viabilidad de todos los perfiles de proyecto. | UC-006 |
| [ACT-006](ACT-006-docente-tutor.md) | Docente Tutor | Primary | Human | Docente designado como tutor del proyecto de grado de un estudiante, que lo guía y revisa el documento del proyecto antes que el tribunal evaluador. | UC-006 |
| [ACT-007](ACT-007-docente-del-tribunal.md) | Docente del Tribunal | Primary | Human | Docente designado como uno de los tres miembros del tribunal evaluador de un estudiante. | UC-006 |
| [ACT-008](ACT-008-administrador.md) | Administrador | Primary | Human | Responsable técnico que crea y mantiene las cuentas de usuario y sus roles. | UC-001, UC-009 |
| [ACT-010](ACT-010-usuario.md) | Usuario | Primary | Human | Actor abstracto: persona con una cuenta de usuario en el sistema. | UC-001, UC-002 |

## Actor Generalization

| Child actor | Parent actor | Additional use cases of the child |
| --- | --- | --- |
| ACT-001 Estudiante | ACT-010 Usuario | UC-003 Gestionar trámite de titulación, UC-004 Gestionar proyecto de grado, UC-008 Consultar trámite de titulación |
| ACT-002 Secretario(a) de dirección de carrera | ACT-010 Usuario | UC-003 Gestionar trámite de titulación, UC-008 Consultar trámite de titulación, UC-009 Consultar registro de cambios |
| ACT-004 Docente | ACT-010 Usuario | UC-008 Consultar trámite de titulación |
| ACT-008 Administrador | ACT-010 Usuario | UC-001 Gestionar usuarios, UC-009 Consultar registro de cambios |
| ACT-003 Director de carrera | ACT-004 Docente | UC-001 Gestionar usuarios, UC-005 Gestionar tribunal evaluador y tutor, UC-007 Gestionar cronograma de defensas, UC-009 Consultar registro de cambios, UC-010 Consultar estadísticas |
| ACT-005 Docente de Taller de Grado | ACT-004 Docente | UC-006 Evaluar proyecto de grado |
| ACT-006 Docente Tutor | ACT-004 Docente | UC-006 Evaluar proyecto de grado |
| ACT-007 Docente del Tribunal | ACT-004 Docente | UC-006 Evaluar proyecto de grado |

## Open Questions

- [ ] ACT-001, ACT-002, ACT-003, ACT-004, ACT-005, ACT-006, ACT-007, ACT-008: ¿cuántos usuarios hay de cada rol, con qué frecuencia usan el sistema y qué experiencia técnica tienen? (ask: Secretario(a) de dirección de carrera)
- [ ] ACT-008: ¿quién cumple el rol de administrador en la carrera? (ask: Director de carrera)
