# Actors

## Actor Catalog

| ID | Actor | Type | Kind | Description | Use cases (derived) |
| --- | --- | --- | --- | --- | --- |
| [ACT-001](ACT-001-estudiante.md) | Estudiante | Primary | Human | Postulante que realiza el trámite de titulación por Proyecto de Grado. | UC-002, UC-003, UC-007, UC-012, UC-013, UC-016 |
| [ACT-002](ACT-002-secretario-de-direccion-de-carrera.md) | Secretario(a) de dirección de carrera | Primary | Human | Personal de la Dirección de Carrera que recibe los documentos del trámite y lleva el expediente. | UC-002, UC-011, UC-012, UC-013, UC-014, UC-016 |
| [ACT-003](ACT-003-director-de-carrera.md) | Director de carrera | Primary | Human | Autoridad de la carrera que designa tutores y tribunales y programa las evaluaciones. | UC-004, UC-006, UC-009, UC-012, UC-013, UC-014, UC-015, UC-016 |
| [ACT-004](ACT-004-docente.md) | Docente | Primary | Human | Docente de la carrera que participa en uno o más trámites de titulación como docente de Taller de Grado, tutor o miembro del tribunal. | UC-012, UC-013, UC-016 |
| [ACT-005](ACT-005-docente-de-taller-de-grado.md) | Docente de Taller de Grado | Primary | Human | Docente de la materia Taller de Grado, único en la carrera, que evalúa la viabilidad de todos los perfiles de proyecto. | UC-005 |
| [ACT-006](ACT-006-tutor.md) | Tutor | Primary | Human | Docente que guía al estudiante y revisa el documento del proyecto junto al tribunal evaluador. | UC-008 |
| [ACT-007](ACT-007-miembro-del-tribunal.md) | Miembro del tribunal | Primary | Human | Docente designado como uno de los tres integrantes del tribunal evaluador de un estudiante. | UC-008, UC-010 |
| [ACT-008](ACT-008-administrador.md) | Administrador | Primary | Human | Responsable técnico que crea y mantiene las cuentas de usuario y sus roles. | UC-001, UC-016 |
| [ACT-009](ACT-009-servicio-de-correo.md) | Servicio de correo | Supporting | External system | Servicio externo de correo electrónico por el que el sistema recibe solicitudes y envía respuestas y notificaciones. | UC-001, UC-004, UC-006, UC-007, UC-008, UC-009 |

## Actor Generalization

| Child actor | Parent actor | Additional use cases of the child |
| --- | --- | --- |
| ACT-005 Docente de Taller de Grado | ACT-004 Docente | UC-005 Evaluar viabilidad del perfil |
| ACT-006 Tutor | ACT-004 Docente | UC-008 Revisar documento del proyecto |
| ACT-007 Miembro del tribunal | ACT-004 Docente | UC-008 Revisar documento del proyecto, UC-010 Registrar resultado de evaluación |

## Open Questions

- [ ] ACT-001, ACT-002, ACT-003, ACT-004, ACT-005, ACT-006, ACT-007, ACT-008: ¿cuántos usuarios hay de cada rol, con qué frecuencia usan el sistema y qué experiencia técnica tienen? (ask: Secretario(a) de dirección de carrera)
- [ ] ACT-008: ¿quién cumple el rol de administrador en la carrera? (ask: Director de carrera)
- [x] ACT-009: ¿qué servicio de correo se usa (institucional u otro) y quién es su responsable? Respuesta: el servidor `mail.tecnoweb.org.bo` provisto por el docente de Tecnología Web (CON-006).
