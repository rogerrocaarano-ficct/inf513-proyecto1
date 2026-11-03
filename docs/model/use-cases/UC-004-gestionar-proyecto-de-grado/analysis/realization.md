# UC-004 Gestionar proyecto de grado: Analysis Realization

| | |
| --- | --- |
| Status | Draft |
| Iteration | I1 |
| Use case | [UC-004](../use-case.md) |

## Participating Classes

| Class | Stereotype | Package | Responsibility in this use case |
| --- | --- | --- | --- |
| Estudiante | actor | — | Presenta el perfil de proyecto, entrega las versiones del documento del proyecto y consulta su proyecto de grado |
| Docente de Taller de Grado | actor | — | Recibe el aviso de cada perfil de proyecto para evaluar |
| Docente Tutor | actor | — | Recibe el aviso de cada versión del documento del proyecto para revisar |
| GestionProyectoUI | boundary | [Unassigned](../../../analysis/unassigned/package.md) | Recibe las solicitudes del estudiante y le devuelve confirmaciones, rechazos y su proyecto de grado |
| NotificacionUI | boundary | [Unassigned](../../../analysis/unassigned/package.md) | Avisa por correo al docente de Taller de Grado de un perfil de proyecto nuevo y al tutor de una versión nueva |
| GestorProyectos | control | [Unassigned](../../../analysis/unassigned/package.md) | Coordina cada solicitud: busca el trámite del estudiante, verifica las reglas, registra el perfil o la versión, lo registra en el registro de cambios y avisa al docente que corresponde |
| TramiteTitulacion | entity | [Unassigned](../../../analysis/unassigned/package.md) | Indica el trámite de titulación en curso del estudiante, su proyecto de grado, su tutor y si la predefensa está aprobada |
| ProyectoGrado | entity | [Unassigned](../../../analysis/unassigned/package.md) | Nace con el primer perfil de proyecto y guarda el documento vigente, que evoluciona de perfil de proyecto a versiones del documento del proyecto; decide si admite un perfil nuevo o una versión nueva y verifica que el archivo cumpla CON-004 |
| CuentaUsuario | entity | [Unassigned](../../../analysis/unassigned/package.md) | Indica la cuenta de usuario activa con el rol de docente de Taller de Grado |
| RegistroCambios | entity | [Unassigned](../../../analysis/unassigned/package.md) | Guarda quién cambió qué proyecto de grado y cuándo, y da su historial de perfiles y versiones |

## Structural View

| From | To | Meaning |
| --- | --- | --- |
| Estudiante | GestionProyectoUI | Interactúa con |
| GestionProyectoUI | GestorProyectos | Pide gestionar el proyecto de grado |
| GestorProyectos | NotificacionUI | Pide avisar al docente |
| NotificacionUI | Docente de Taller de Grado | Avisa por correo |
| NotificacionUI | Docente Tutor | Avisa por correo |
| GestorProyectos | TramiteTitulacion | Busca el trámite en curso, su tutor y su etapa |
| GestorProyectos | ProyectoGrado | Crea, verifica, actualiza y consulta |
| GestorProyectos | CuentaUsuario | Busca al docente de Taller de Grado |
| GestorProyectos | RegistroCambios | Registra cada cambio del proyecto de grado y obtiene su historial |
| TramiteTitulacion | ProyectoGrado | Cada trámite tiene a lo sumo un proyecto de grado (see entity.md) |
| TramiteTitulacion | CuentaUsuario | Cada trámite tiene un estudiante y docentes designados, entre ellos el tutor (see entity.md) |
| RegistroCambios | CuentaUsuario | Cada cambio indica quién lo hizo (see entity.md) |
| RegistroCambios | ProyectoGrado | Un cambio puede afectar a un proyecto de grado (see entity.md) |

## Behavioral View

### Basic Flow

| # | UC step | From | To | Message |
| --- | --- | --- | --- | --- |
| 1 | 1 | Estudiante | GestionProyectoUI | presentar perfil |
| 2 | 1 | GestionProyectoUI | GestorProyectos | presentar perfil |
| 3 | 2 | GestorProyectos | TramiteTitulacion | buscar trámite en curso |
| 4 | 2 | GestorProyectos | TramiteTitulacion | verificar sin proyecto |
| 5 | 3 | GestorProyectos | ProyectoGrado | verificar archivo |
| 6 | 4 | GestorProyectos | ProyectoGrado | crear con perfil |
| 7 | 4 | GestorProyectos | RegistroCambios | registrar cambio |
| 8 | 5 | GestorProyectos | CuentaUsuario | buscar docente de Taller de Grado |
| 9 | 5 | GestorProyectos | NotificacionUI | notificar perfil |
| 10 | 5 | NotificacionUI | Docente de Taller de Grado | avisar |
| 11 | 6 | GestorProyectos | GestionProyectoUI | confirmar |
| 12 | 6 | GestionProyectoUI | Estudiante | mostrar confirmación |

### A1 Presentar un perfil de proyecto nuevo

Después del mensaje 4 del flujo básico, que encuentra el proyecto de grado del estudiante. Desde el paso 5, sigue con los mensajes 8 a 12 del flujo básico.

| # | UC step | From | To | Message |
| --- | --- | --- | --- | --- |
| 1 | 2 | GestorProyectos | ProyectoGrado | verificar perfil admitido |
| 2 | A1.1 | GestorProyectos | ProyectoGrado | verificar archivo |
| 3 | A1.2 | GestorProyectos | ProyectoGrado | cambiar perfil |
| 4 | A1.2 | GestorProyectos | RegistroCambios | registrar cambio |

### A2 Entregar una versión del documento del proyecto

| # | UC step | From | To | Message |
| --- | --- | --- | --- | --- |
| 1 | A2 | Estudiante | GestionProyectoUI | entregar versión |
| 2 | A2 | GestionProyectoUI | GestorProyectos | entregar versión |
| 3 | A2.1 | GestorProyectos | TramiteTitulacion | buscar trámite en curso |
| 4 | A2.1 | GestorProyectos | ProyectoGrado | verificar visto bueno |
| 5 | A2.1 | GestorProyectos | TramiteTitulacion | verificar tutor |
| 6 | A2.2 | GestorProyectos | TramiteTitulacion | consultar etapa |
| 7 | A2.2 | GestorProyectos | ProyectoGrado | verificar versión admitida |
| 8 | A2.3 | GestorProyectos | ProyectoGrado | verificar archivo |
| 9 | A2.4 | GestorProyectos | ProyectoGrado | registrar versión |
| 10 | A2.4 | GestorProyectos | RegistroCambios | registrar cambio |
| 11 | A2.5 | GestorProyectos | TramiteTitulacion | obtener tutor |
| 12 | A2.5 | GestorProyectos | NotificacionUI | notificar versión |
| 13 | A2.5 | NotificacionUI | Docente Tutor | avisar |
| 14 | A2.6 | GestorProyectos | GestionProyectoUI | confirmar |
| 15 | A2.6 | GestionProyectoUI | Estudiante | mostrar confirmación |

### A3 Entregar la versión para la defensa final

En lugar del mensaje 9 de A2, cuando la etapa del mensaje 6 indica la predefensa aprobada. Sigue con los mensajes 10 a 15 de A2.

| # | UC step | From | To | Message |
| --- | --- | --- | --- | --- |
| 1 | A3.1 | GestorProyectos | ProyectoGrado | registrar versión final |

### A4 Consultar el proyecto de grado

| # | UC step | From | To | Message |
| --- | --- | --- | --- | --- |
| 1 | A4 | Estudiante | GestionProyectoUI | consultar proyecto |
| 2 | A4 | GestionProyectoUI | GestorProyectos | consultar proyecto |
| 3 | A4.1 | GestorProyectos | TramiteTitulacion | buscar trámite en curso |
| 4 | A4.1 | GestorProyectos | ProyectoGrado | obtener proyecto |
| 5 | A4.1 | GestorProyectos | RegistroCambios | obtener historial |
| 6 | A4.1 | GestorProyectos | GestionProyectoUI | mostrar proyecto |
| 7 | A4.1 | GestionProyectoUI | Estudiante | mostrar proyecto |

### E1 a E5 Rechazos

Las excepciones E1 a E5 siguen la misma colaboración: la verificación que falla termina el flujo sin cambios.

| # | UC step | From | To | Message |
| --- | --- | --- | --- | --- |
| 1 | E1.1, E2.1, E3.1, E4.1, E5.1 | GestorProyectos | GestionProyectoUI | rechazar |
| 2 | E1.1, E2.1, E3.1, E4.1, E5.1 | GestionProyectoUI | Estudiante | mostrar rechazo |

## Special Requirements

| Requirement | Concern | Handled by |
| --- | --- | --- |
| NFR-SE-001 | Security | AM-001 |
| NFR-IN-001 | Distribution | AM-002 |
| NFR-SE-002 | Audit | AM-003 |
| CON-004 | Persistence | AM-004, que conserva también los archivos de los perfiles y versiones anteriores; ProyectoGrado verifica el formato y el tamaño |

## Feedback to Requirements

- El docente de Taller de Grado y el tutor reciben avisos del caso de uso (FR-041, FR-057), como el estudiante en UC-003. Se agregaron como actores de apoyo en `use-case.md` y en los catálogos de actores.
- A2.2 y A3 necesitan saber si la predefensa está aprobada: TramiteTitulacion lo conoce por su etapa, que cambia en UC-006. No cambia `use-case.md`.
- A2.2 y E3 necesitan el estado de revisión de la versión vigente (si el tutor la observó o aprobó y qué miembros del tribunal la revisaron). ProyectoGrado lo conoce en su estado; cómo lo registran el tutor y el tribunal se modela al analizar UC-006. No cambia `use-case.md`.
- El documento del proyecto evoluciona de estado: el perfil de proyecto y las versiones son estados sucesivos de ProyectoGrado, no entidades distintas. El historial de versiones (FR-010) y los perfiles anteriores (A1.2, A4) salen del registro de cambios, que guarda cada documento y estado anterior. No cambia `use-case.md`.

## Open Questions

- [ ] ¿La etapa de TramiteTitulacion debe reflejar los pasos de UC-004 (perfil presentado, documento en revisión), para que UC-008 muestre la etapa del trámite? (ask: Equipo de desarrollo)
