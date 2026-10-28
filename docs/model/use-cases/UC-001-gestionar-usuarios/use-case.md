# UC-001 Gestionar usuarios

| | |
| --- | --- |
| Status | Detailed |
| Iteration | I1 |
| Primary actor | ACT-008 Administrador, ACT-003 Director de carrera |
| Supporting actors | None |
| Priority | Must |
| Architecturally significant | Yes: el sistema identifica a cada remitente por su dirección de correo y lo autoriza según sus roles (NFR-SE-001); todos los demás casos de uso dependen de esto |
| Feature | FEAT-001 |
| Functional requirements (derived) | FR-026, FR-030, FR-031, FR-032, FR-033, FR-034, FR-035, FR-036, FR-038, FR-039, FR-050, FR-053 |

## Brief Description

El administrador y el director de carrera crean, consultan, modifican, desactivan y reactivan cuentas de usuario y sus roles, cada uno dentro de su alcance: el administrador gestiona todas las cuentas, y el director de carrera, las de los docentes, del docente de Taller de Grado y de los secretarios(as) de dirección de carrera. Así cada participante del trámite de titulación puede usar el sistema según sus roles.

## Preconditions

- El usuario que realiza el caso de uso tiene una cuenta de usuario activa con el rol de administrador o de director de carrera.

## Basic Flow

1. El administrador solicita crear una cuenta de usuario con el nombre completo, la dirección de correo y los roles, y para un estudiante además el registro universitario, la cédula de identidad y el teléfono.
2. El sistema verifica que el administrador puede asignar los roles indicados.
3. El sistema verifica que la dirección de correo y el registro universitario no pertenecen a otra cuenta de usuario.
4. El sistema verifica que la combinación de roles está permitida y que no habría una segunda cuenta de usuario activa con el rol de docente de Taller de Grado.
5. El sistema crea la cuenta de usuario activa con sus roles y registra el cambio en el registro de cambios.
6. El sistema notifica por correo al nuevo usuario la creación de su cuenta de usuario y sus roles.
7. El sistema confirma al administrador la creación de la cuenta de usuario.

## Alternative Flows

### A1 El director de carrera gestiona la cuenta de usuario

En el paso 1, si quien realiza el caso de uso es el director de carrera.

1. El director de carrera realiza el flujo básico o los flujos A2 a A5.
2. El sistema solo le permite gestionar cuentas de usuario cuyos roles son docente, docente de Taller de Grado o secretario(a) de dirección de carrera, y solo asignar o quitar esos roles.
3. El caso de uso continúa en el paso 2 del flujo básico.

### A2 Consultar cuentas de usuario

En el paso 1, si el administrador solicita la lista de cuentas de usuario o los datos de una de ellas.

1. El sistema muestra la lista de cuentas de usuario, dentro del alcance de quien consulta, con sus roles y su estado, o los datos de la cuenta de usuario solicitada.
2. El caso de uso termina.

### A3 Modificar una cuenta de usuario

En el paso 1, si el administrador solicita modificar los datos o los roles de una cuenta de usuario.

1. El administrador indica la cuenta de usuario y los datos o roles nuevos.
2. El sistema verifica las reglas de los pasos 2 a 4 y que no se quita el rol de administrador a la única cuenta de administrador activa.
3. Si se quita un rol que participa en trámites de titulación en curso, el sistema advierte al administrador y deja esos trámites pendientes de reasignación.
4. El sistema guarda los cambios y los registra en el registro de cambios.
5. Si cambiaron los roles, el sistema notifica por correo al usuario sus roles nuevos.
6. El sistema confirma al administrador la modificación.
7. El caso de uso termina.

### A4 Desactivar una cuenta de usuario

En el paso 1, si el administrador solicita desactivar una cuenta de usuario.

1. El sistema verifica que la cuenta de usuario está dentro del alcance del administrador y que no es la única cuenta de administrador activa.
2. Si la cuenta de usuario participa en trámites de titulación en curso, el sistema advierte al administrador y deja esos trámites pendientes de reasignación.
3. El sistema desactiva la cuenta de usuario, conserva su historial y registra el cambio en el registro de cambios.
4. El sistema notifica por correo al usuario la desactivación de su cuenta de usuario.
5. El sistema confirma al administrador la desactivación.
6. El caso de uso termina.

### A5 Reactivar una cuenta de usuario

En el paso 1, si el administrador solicita reactivar una cuenta de usuario desactivada.

1. El sistema verifica que la cuenta de usuario está dentro del alcance del administrador y que no habría una segunda cuenta de usuario activa con el rol de docente de Taller de Grado.
2. El sistema reactiva la cuenta de usuario y registra el cambio en el registro de cambios.
3. El sistema notifica por correo al usuario la reactivación de su cuenta de usuario.
4. El sistema confirma al administrador la reactivación.
5. El caso de uso termina.

## Exceptions

### E1 Rol o cuenta de usuario fuera del alcance

En los pasos 2, A1.2, A3.2, A4.1 o A5.1, si quien realiza el caso de uso no puede asignar o quitar alguno de los roles, o gestionar la cuenta de usuario indicada (p. ej., el director de carrera intenta asignar el rol de director de carrera).

1. El sistema rechaza la solicitud e indica el motivo.
2. El caso de uso termina sin cambios.

### E2 Dirección de correo o registro universitario repetido

En los pasos 3 o A3.2, si la dirección de correo o el registro universitario ya pertenece a otra cuenta de usuario.

1. El sistema rechaza la solicitud e indica el dato repetido.
2. El caso de uso termina sin cambios.

### E3 Combinación de roles no permitida

En los pasos 4 o A3.2, si la combinación de roles no está permitida, o habría una segunda cuenta de usuario activa con el rol de docente de Taller de Grado.

1. El sistema rechaza la solicitud e indica la regla que no se cumple.
2. El caso de uso termina sin cambios.

### E4 Única cuenta de administrador activa

En los pasos A3.2 o A4.1, si la solicitud dejaría al sistema sin una cuenta de administrador activa.

1. El sistema rechaza la solicitud e indica el motivo.
2. El caso de uso termina sin cambios.

### E5 Datos incompletos

En los pasos 1 o A3.1, si falta un dato obligatorio de la cuenta de usuario.

1. El sistema rechaza la solicitud e indica los datos que faltan.
2. El caso de uso termina sin cambios.

## Postconditions

- **Success:** la cuenta de usuario queda creada, modificada, desactivada o reactivada según lo solicitado, el cambio queda en el registro de cambios y el usuario afectado recibe la notificación por correo; en una consulta, el sistema no cambia nada.
- **Failure:** ninguna cuenta de usuario cambia y quien realizó la solicitud conoce el motivo del rechazo.

## Special Requirements (derived)

- None

## Open Questions

- [x] ¿Qué roles puede asignar el director de carrera, y quién gestiona una cuenta de usuario con roles de distintos alcances? (ask: Director de carrera) Respuesta: el director de carrera gestiona las cuentas de usuario de docentes, del docente de Taller de Grado y de secretarios(as), y asigna esos roles; solo el administrador asigna el rol de director de carrera (FR-050, FR-053).
- [ ] A3, A4: el destino de los trámites pendientes de reasignación sigue abierto en `requirements.md` (FR-034). (ask: Secretario(a) de dirección de carrera)
