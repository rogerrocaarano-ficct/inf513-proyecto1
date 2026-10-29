# Proposal

## Why

El esqueleto de `mail-app` ya recibe comandos por correo, identifica al remitente y registra los cambios, pero solo existe la cuenta del administrador inicial. Todos los demás casos de uso dependen de que haya cuentas de usuario con sus roles (UC-001 es arquitectónicamente significativo), así que es el primer caso de uso que se implementa.

## What Changes

- Comandos de gestión de usuarios por correo, permitidos al administrador y al director de carrera: `CrearUsuario`, `ListarUsuarios`, `VerUsuario`, `ModificarUsuario`, `DesactivarUsuario` y `ReactivarUsuario` (FR-026, FR-030, FR-031, FR-032).
- Reglas de las cuentas de usuario:
  - Datos obligatorios del estudiante: registro universitario, cédula de identidad y teléfono (FR-026).
  - Dirección de correo y registro universitario únicos (FR-033).
  - Combinaciones de roles permitidas (FR-036).
  - Un solo docente de Taller de Grado activo (FR-039).
  - Siempre una cuenta de administrador activa como mínimo (FR-038).
- Alcance de quien gestiona: el director de carrera solo gestiona las cuentas de docentes, del docente de Taller de Grado y de secretarios(as), y solo asigna esos roles. Los roles de director de carrera y de administrador los asigna únicamente el administrador (FR-050, FR-053).
- Avisos por correo al usuario afectado cuando su cuenta se crea, se desactiva o se reactiva, o cuando cambian sus roles (FR-035).
- Advertencia a quien gestiona cuando la cuenta que desactiva, o el rol que quita, participa en trámites en curso. Esos trámites quedan pendientes de reasignación (FR-034).
- Subsistema GraduationProcesses mínimo: la interfaz `GraduationParticipation` con sus tipos, y una implementación que todavía no encuentra trámites porque estos aún no existen. El caso de uso que crea los trámites la reemplaza.
- La realización de diseño de UC-001 se ajusta a D5 de `esqueleto-mail-app`: Messaging autentica y autoriza al remitente, cada comando es un `CommandHandler` con sus roles permitidos, y la respuesta es un `CommandReply(body)`.

Fuera de esta propuesta:

- El destino de los trámites pendientes de reasignación, que sigue abierto en FR-034.
- La foto de perfil, que corresponde a UC-002.
- La corrección del orden de las respuestas en el sondeo POP3.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `user-accounts`: agrega la gestión de cuentas de usuario por comandos, con sus reglas de datos, de unicidad, de roles, de alcance, de avisos y de trámites afectados.

## Impact

- **Código:** paquete `users` de `mail-app` (entidad `UserAccount` con los datos del estudiante y sus operaciones de cambio, reglas de `Role`, servicio de gestión, manejadores de comandos y repositorio), y paquete nuevo `graduationprocesses` con la interfaz `GraduationParticipation`.
- **Base de datos:** sin migraciones nuevas, porque `V1` ya tiene las columnas y los índices únicos.
- **Modelo:**
  - La realización de diseño de UC-001.
  - `docs/model/design/subsystems/users.md` y `graduation-processes.md`.
  - `docs/model/use-cases/use-cases.md`, si cambia el estado de UC-001.
