# UC-003 Gestionar trámite de titulación: Analysis Realization

| | |
| --- | --- |
| Status | Draft |
| Iteration | I1 |
| Use case | [UC-003](../use-case.md) |

## Participating Classes

| Class | Stereotype | Package | Responsibility in this use case |
| --- | --- | --- | --- |
| Secretario(a) de dirección de carrera | actor | — | Solicita abrir el trámite de titulación, reemplazar documentos iniciales y registrar los ejemplares físicos |
| Estudiante | actor | — | Recibe el aviso de la apertura y del cierre de su trámite de titulación |
| GestionTramiteUI | boundary | [Unassigned](../../../analysis/unassigned/package.md) | Recibe las solicitudes del secretario(a) y le devuelve confirmaciones y rechazos |
| NotificacionUI | boundary | [Unassigned](../../../analysis/unassigned/package.md) | Avisa por correo al estudiante de la creación de su cuenta de usuario y de la apertura y el cierre de su trámite |
| GestorTramites | control | [Unassigned](../../../analysis/unassigned/package.md) | Coordina cada solicitud: verifica las reglas, aplica el cambio, lo registra y notifica; pide la cuenta del estudiante a GestorUsuarios |
| GestorUsuarios | control | [Unassigned](../../../analysis/unassigned/package.md) | Obtiene la cuenta de usuario existente del estudiante o la crea, registra la creación y la avisa, como en UC-001 |
| TramiteTitulacion | entity | [Unassigned](../../../analysis/unassigned/package.md) | Se abre con sus documentos iniciales, admite reemplazarlos mientras está en curso, y se cierra con el expediente archivado después de la calificación final |
| DocumentoInicial | entity | [Unassigned](../../../analysis/unassigned/package.md) | Guarda cada documento inicial digitalizado y verifica su formato y tamaño |
| CuentaUsuario | entity | [Unassigned](../../../analysis/unassigned/package.md) | Verifica que los datos únicos del estudiante no estén repetidos, crea su cuenta de usuario o indica la existente |
| RegistroCambios | entity | [Unassigned](../../../analysis/unassigned/package.md) | Guarda quién cambió qué trámite de titulación o cuenta de usuario y cuándo |

## Structural View

| From | To | Meaning |
| --- | --- | --- |
| Secretario(a) de dirección de carrera | GestionTramiteUI | Interactúa con |
| GestionTramiteUI | GestorTramites | Pide gestionar trámites de titulación |
| GestorTramites | NotificacionUI | Pide avisar al estudiante |
| NotificacionUI | Estudiante | Avisa por correo |
| GestorTramites | TramiteTitulacion | Abre, actualiza y cierra |
| GestorTramites | DocumentoInicial | Verifica los documentos iniciales |
| GestorTramites | GestorUsuarios | Pide obtener o crear la cuenta de usuario del estudiante; enlace entre controles porque reutiliza la lógica de cuentas de usuario de UC-001 |
| GestorTramites | RegistroCambios | Registra cada cambio del trámite |
| GestorUsuarios | CuentaUsuario | Busca y crea la cuenta de usuario |
| GestorUsuarios | RegistroCambios | Registra la creación de la cuenta de usuario |
| GestorUsuarios | NotificacionUI | Pide avisar la creación de la cuenta de usuario |
| TramiteTitulacion | DocumentoInicial | Cada trámite tiene sus documentos iniciales (see entity.md) |
| TramiteTitulacion | CuentaUsuario | Cada trámite tiene un estudiante y docentes designados (see entity.md) |
| RegistroCambios | CuentaUsuario | Cada cambio indica quién lo hizo y a qué cuenta de usuario afecta (see entity.md) |
| RegistroCambios | TramiteTitulacion | Un cambio puede afectar a un trámite de titulación (see entity.md) |

## Behavioral View

### Basic Flow

| # | UC step | From | To | Message |
| --- | --- | --- | --- | --- |
| 1 | 1 | Secretario(a) de dirección de carrera | GestionTramiteUI | abrir trámite |
| 2 | 1 | GestionTramiteUI | GestorTramites | abrir trámite |
| 3 | 2 | GestorTramites | DocumentoInicial | verificar documentos |
| 4 | 2 | GestorTramites | TramiteTitulacion | verificar carpeta completa |
| 5 | 3 | GestorTramites | GestorUsuarios | obtener cuenta |
| 6 | 3 | GestorUsuarios | CuentaUsuario | verificar datos únicos |
| 7 | 4 | GestorUsuarios | CuentaUsuario | crear cuenta |
| 8 | 4 | GestorUsuarios | RegistroCambios | registrar cambio |
| 9 | 4 | GestorUsuarios | NotificacionUI | notificar creación |
| 10 | 4 | NotificacionUI | Estudiante | avisar |
| 11 | 4 | GestorUsuarios | GestorTramites | devolver cuenta |
| 12 | 5 | GestorTramites | TramiteTitulacion | abrir |
| 13 | 5 | GestorTramites | RegistroCambios | registrar cambio |
| 14 | 6 | GestorTramites | NotificacionUI | notificar apertura |
| 15 | 6 | NotificacionUI | Estudiante | avisar |
| 16 | 7 | GestorTramites | GestionTramiteUI | confirmar |
| 17 | 7 | GestionTramiteUI | Secretario(a) de dirección de carrera | mostrar confirmación |

### A1 El estudiante ya tiene una cuenta de usuario

| # | UC step | From | To | Message |
| --- | --- | --- | --- | --- |
| 1 | A1.1 | GestorUsuarios | CuentaUsuario | buscar cuenta |
| 2 | A1.1 | GestorUsuarios | GestorTramites | devolver cuenta |
| 3 | A1.1 | GestorTramites | TramiteTitulacion | verificar trámite en curso |
| 4 | A1.2 | GestorTramites | TramiteTitulacion | abrir |
| 5 | A1.2 | GestorTramites | RegistroCambios | registrar cambio |
| 6 | A1.2 | GestorTramites | NotificacionUI | notificar apertura |
| 7 | A1.2 | NotificacionUI | Estudiante | avisar |
| 8 | A1.2 | GestorTramites | GestionTramiteUI | confirmar |

### A2 Reemplazar un documento inicial

| # | UC step | From | To | Message |
| --- | --- | --- | --- | --- |
| 1 | A2.1 | Secretario(a) de dirección de carrera | GestionTramiteUI | reemplazar documento |
| 2 | A2.1 | GestionTramiteUI | GestorTramites | reemplazar documento |
| 3 | A2.2 | GestorTramites | TramiteTitulacion | verificar en curso |
| 4 | A2.2 | GestorTramites | DocumentoInicial | verificar documento |
| 5 | A2.3 | GestorTramites | TramiteTitulacion | reemplazar documento |
| 6 | A2.3 | GestorTramites | RegistroCambios | registrar cambio |
| 7 | A2.4 | GestorTramites | GestionTramiteUI | confirmar |
| 8 | A2.4 | GestionTramiteUI | Secretario(a) de dirección de carrera | mostrar confirmación |

### A3 Registrar los ejemplares físicos y cerrar el trámite

| # | UC step | From | To | Message |
| --- | --- | --- | --- | --- |
| 1 | A3.1 | Secretario(a) de dirección de carrera | GestionTramiteUI | registrar ejemplares |
| 2 | A3.1 | GestionTramiteUI | GestorTramites | registrar ejemplares |
| 3 | A3.2 | GestorTramites | TramiteTitulacion | verificar cierre |
| 4 | A3.3 | GestorTramites | TramiteTitulacion | cerrar |
| 5 | A3.3 | GestorTramites | RegistroCambios | registrar cambio |
| 6 | A3.4 | GestorTramites | NotificacionUI | notificar cierre |
| 7 | A3.4 | NotificacionUI | Estudiante | avisar |
| 8 | A3.5 | GestorTramites | GestionTramiteUI | confirmar |
| 9 | A3.5 | GestionTramiteUI | Secretario(a) de dirección de carrera | mostrar confirmación |

### E1 a E6 Rechazos

Las excepciones E1 a E6 siguen la misma colaboración: la verificación que falla termina el flujo sin cambios.

En E3 y E4, cuando el rechazo sale de la cuenta de usuario, GestorUsuarios lo devuelve primero a GestorTramites.

| # | UC step | From | To | Message |
| --- | --- | --- | --- | --- |
| 1 | E3.1, E4.1 | GestorUsuarios | GestorTramites | rechazar cuenta |
| 2 | E1.1, E2.1, E3.1, E4.1, E5.1, E6.1 | GestorTramites | GestionTramiteUI | rechazar |
| 3 | E1.1, E2.1, E3.1, E4.1, E5.1, E6.1 | GestionTramiteUI | Secretario(a) de dirección de carrera | mostrar rechazo |

## Special Requirements

| Requirement | Concern | Handled by |
| --- | --- | --- |
| NFR-SE-001 | Security | AM-001 |
| NFR-IN-001 | Distribution | AM-002 |
| NFR-SE-002 | Audit | AM-003 |
| CON-005 | Persistence | AM-004, y DocumentoInicial verifica el formato y el tamaño |

## Feedback to Requirements

- A3.2 verifica que el tribunal evaluador ya registró la calificación final, que se registra en UC-006. Para eso TramiteTitulacion conoce su etapa; cómo cambia la etapa en cada caso de uso se modela al analizarlos. No cambia `use-case.md`.
- GestorTramites pide la cuenta del estudiante a GestorUsuarios, que reutiliza las reglas y el aviso de creación de UC-001 (FR-033, FR-035) sin el alcance de FR-050 y FR-053, porque la autoriza la apertura del trámite (FR-051). Por eso el estudiante recibe dos avisos: la creación de su cuenta de usuario y la apertura del trámite. Se corrigieron los pasos 4, 6 y A1.2 de `use-case.md`.
- Los pasos 4 y 5 crean la cuenta de usuario y abren el trámite: si la apertura falla, la cuenta tampoco debe quedar creada, como dice la postcondición de fallo. No cambia `use-case.md`.

## Open Questions

- [ ] A3.2: ¿en qué clase queda la calificación final, y cómo se entera el trámite de titulación de que ya fue registrada? Se decide al analizar UC-006. (ask: Equipo de desarrollo)
