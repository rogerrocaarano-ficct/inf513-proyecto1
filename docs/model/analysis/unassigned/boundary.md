# Unassigned: Boundary Classes

## GestionProyectoUI

| | |
| --- | --- |
| Actor | ACT-001 Estudiante |
| Use cases (derived) | UC-004 |
| Status | Draft |
| Iteration | I1 |

**Responsibilities:**

- Recibir las solicitudes para presentar un perfil de proyecto, entregar una versión del documento del proyecto y consultar el proyecto de grado (UC-004)
- Devolver al estudiante el resultado, su proyecto de grado y los motivos de rechazo (UC-004)

**Information exchanged:**

| Direction | Information |
| --- | --- |
| In | Perfil de proyecto en PDF con el título del proyecto, documento del proyecto en PDF con un comentario general, solicitud de consulta |
| Out | Confirmaciones con el número de la versión, título del proyecto, perfiles de proyecto y versiones del documento del proyecto con su estado, motivos de rechazo |

## GestionTramiteUI

| | |
| --- | --- |
| Actor | ACT-002 Secretario(a) de dirección de carrera |
| Use cases (derived) | UC-003 |
| Status | Draft |
| Iteration | I1 |

**Responsibilities:**

- Recibir las solicitudes para abrir un trámite de titulación, reemplazar un documento inicial y registrar los ejemplares físicos (UC-003)
- Devolver al secretario(a) el resultado y los motivos de rechazo (UC-003)

**Information exchanged:**

| Direction | Information |
| --- | --- |
| In | Datos del estudiante (nombre completo, dirección de correo, registro universitario, cédula de identidad y teléfono), documentos iniciales digitalizados, trámite de titulación, documento inicial a reemplazar, recepción de los ejemplares físicos |
| Out | Confirmaciones, motivos de rechazo, etapa del trámite de titulación |

## GestionUsuariosUI

| | |
| --- | --- |
| Actor | ACT-008 Administrador, ACT-003 Director de carrera |
| Use cases (derived) | UC-001 |
| Status | Draft |
| Iteration | I1 |

**Responsibilities:**

- Recibir las solicitudes para crear, consultar, modificar, desactivar y reactivar cuentas de usuario (UC-001)
- Devolver a quien solicita el resultado, las advertencias de trámites afectados y los motivos de rechazo (UC-001)

**Information exchanged:**

| Direction | Information |
| --- | --- |
| In | Datos de la cuenta de usuario (nombre completo, dirección de correo, roles; para un estudiante, registro universitario, cédula de identidad y teléfono), cuenta de usuario a modificar, desactivar o reactivar |
| Out | Lista y datos de cuentas de usuario con sus roles y estado, confirmaciones, trámites afectados, motivos de rechazo |

## NotificacionUI

| | |
| --- | --- |
| Actor | ACT-010 Usuario |
| Use cases (derived) | UC-001, UC-003, UC-004 |
| Status | Draft |
| Iteration | I1 |

**Responsibilities:**

- Avisar por correo al usuario de la creación, desactivación y reactivación de su cuenta de usuario y de los cambios de sus roles (UC-001, UC-003)
- Avisar por correo al estudiante de la apertura y el cierre de su trámite de titulación (UC-003)
- Avisar por correo al docente de Taller de Grado de cada perfil de proyecto presentado, y al tutor de cada versión del documento del proyecto entregada (UC-004)

**Information exchanged:**

| Direction | Information |
| --- | --- |
| Out | Tipo de cambio, roles de la cuenta de usuario, apertura o cierre del trámite de titulación, perfil de proyecto o versión del documento del proyecto para revisar |
