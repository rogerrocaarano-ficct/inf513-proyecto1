# user-accounts Specification

## Purpose

Mantiene las cuentas de usuario del sistema, que identifican a cada persona por su dirección de correo y definen sus roles y su estado (UC-001).

## Requirements

### Requirement: Administrador inicial
El sistema SHALL crear, al arrancar, una cuenta de usuario activa con el rol de administrador, con el nombre y la dirección de correo de la configuración, si no existe ninguna cuenta de administrador activa (FR-038).

#### Scenario: Primer arranque
- **WHEN** el sistema arranca con una base de datos sin cuentas de usuario
- **THEN** existe una cuenta de usuario activa con el rol de administrador y los datos de la configuración, y su creación queda en el registro de cambios

#### Scenario: Arranque con administrador existente
- **WHEN** el sistema arranca y ya existe una cuenta de administrador activa
- **THEN** el sistema no crea otra cuenta

#### Scenario: Configuración incompleta
- **WHEN** el sistema arranca sin ninguna cuenta de administrador activa y sin el nombre o la dirección de correo del administrador inicial en la configuración
- **THEN** el sistema no arranca e indica qué dato de configuración falta

### Requirement: Dirección de correo única
El sistema SHALL guardar la dirección de correo de cada cuenta de usuario en minúsculas y SHALL impedir que dos cuentas de usuario tengan la misma dirección.

#### Scenario: Dirección con mayúsculas
- **WHEN** se guarda una cuenta de usuario con la dirección `Ana@UAGRM.edu.bo`
- **THEN** la cuenta queda con `ana@uagrm.edu.bo`, y un remitente `ANA@uagrm.edu.bo` se identifica con ella

### Requirement: Comandos de gestión de usuarios
El sistema SHALL atender los comandos `CrearUsuario(nombreCompleto; correo; roles[; registroUniversitario; cedula; telefono])`, `ListarUsuarios()`, `VerUsuario(correo)`, `ModificarUsuario(correo; campo; valor)`, `DesactivarUsuario(correo)` y `ReactivarUsuario(correo)` solo de cuentas de usuario activas con el rol de administrador o de director de carrera. La cuenta de usuario se indica por su dirección de correo.

#### Scenario: Remitente sin rol de gestión
- **WHEN** una cuenta de usuario activa que solo tiene el rol de docente envía `ListarUsuarios()`
- **THEN** el sistema no ejecuta el comando y responde que no tiene permiso para ese comando

#### Scenario: Cuenta de usuario inexistente
- **WHEN** el administrador envía `VerUsuario(nadie@uagrm.edu.bo)` y no existe esa cuenta de usuario
- **THEN** el sistema responde que la cuenta de usuario no existe

### Requirement: Nombres de los roles en los comandos
El sistema SHALL reconocer los roles por su nombre en español, separados por coma y sin distinguir mayúsculas: `estudiante`, `docente`, `docente de taller de grado`, `secretario`, `director de carrera` y `administrador`.

#### Scenario: Roles con mayúsculas y espacios
- **WHEN** el administrador envía `CrearUsuario(Luis Rojas; luis@uagrm.edu.bo; Docente , Docente de Taller de Grado)`
- **THEN** la cuenta de usuario queda con los roles de docente y de docente de Taller de Grado

#### Scenario: Rol desconocido
- **WHEN** el administrador envía un comando con el rol `decano`
- **THEN** el sistema rechaza la solicitud, indica el rol que no reconoce y no cambia ninguna cuenta de usuario

### Requirement: Creación de una cuenta de usuario
El sistema SHALL crear una cuenta de usuario activa con el nombre completo, la dirección de correo y uno o más roles, y para un estudiante además con el registro universitario, la cédula de identidad y el teléfono (FR-026). Debe registrar la creación en el registro de cambios y confirmarla a quien la solicitó.

#### Scenario: Creación de un docente
- **WHEN** el administrador envía `CrearUsuario(Ana Pérez; ana@uagrm.edu.bo; docente)`
- **THEN** existe una cuenta de usuario activa de Ana Pérez con el rol de docente, su creación queda en el registro de cambios y el administrador recibe la confirmación

#### Scenario: Creación de un estudiante
- **WHEN** el administrador envía `CrearUsuario(Juan Soto; juan@uagrm.edu.bo; estudiante; 219000001; 1234567; 70000000)`
- **THEN** existe una cuenta de usuario activa de Juan Soto con el rol de estudiante, el registro universitario, la cédula de identidad y el teléfono

### Requirement: Datos obligatorios y válidos
El sistema SHALL rechazar, sin cambiar nada, una solicitud a la que le falta un dato obligatorio de la cuenta de usuario o cuya dirección de correo no tiene un formato válido, e indicar los datos que faltan o el dato inválido.

#### Scenario: Estudiante sin registro universitario
- **WHEN** el administrador envía `CrearUsuario(Juan Soto; juan@uagrm.edu.bo; estudiante)`
- **THEN** el sistema rechaza la solicitud, indica que faltan el registro universitario, la cédula de identidad y el teléfono, y no crea la cuenta de usuario

#### Scenario: Dirección de correo inválida
- **WHEN** el administrador envía `CrearUsuario(Ana Pérez; ana-uagrm; docente)`
- **THEN** el sistema rechaza la solicitud e indica que la dirección de correo no es válida

### Requirement: Registro universitario único
El sistema SHALL rechazar, sin cambiar nada, una cuenta de usuario cuyo registro universitario ya pertenece a otra (FR-033), e indicar el dato repetido. Lo mismo vale para la dirección de correo.

#### Scenario: Registro universitario repetido
- **WHEN** el administrador crea un estudiante con un registro universitario que ya tiene otra cuenta de usuario
- **THEN** el sistema rechaza la solicitud, indica que el registro universitario está repetido y no crea la cuenta de usuario

#### Scenario: Dirección de correo repetida
- **WHEN** el administrador envía `CrearUsuario` con una dirección de correo que ya tiene otra cuenta de usuario
- **THEN** el sistema rechaza la solicitud, indica que la dirección de correo está repetida y no crea la cuenta de usuario

### Requirement: Combinaciones de roles permitidas
El sistema SHALL rechazar, sin cambiar nada, una cuenta de usuario con una combinación de roles no permitida (FR-036): el estudiante, el secretario(a) de dirección de carrera y el administrador tienen un solo rol, y el docente de Taller de Grado solo se combina con el rol de docente.

#### Scenario: Estudiante con otro rol
- **WHEN** el administrador crea una cuenta de usuario con los roles de estudiante y docente
- **THEN** el sistema rechaza la solicitud e indica la regla que no se cumple

#### Scenario: Docente de Taller de Grado con docente
- **WHEN** el administrador crea una cuenta de usuario con los roles de docente y de docente de Taller de Grado, y no hay otro docente de Taller de Grado activo
- **THEN** la cuenta de usuario queda creada con los dos roles

#### Scenario: Director de carrera con docente
- **WHEN** el administrador crea una cuenta de usuario con los roles de director de carrera y docente
- **THEN** la cuenta de usuario queda creada con los dos roles

### Requirement: Un solo docente de Taller de Grado activo
El sistema SHALL rechazar, sin cambiar nada, toda creación, modificación o reactivación que dejaría dos cuentas de usuario activas con el rol de docente de Taller de Grado (FR-039).

#### Scenario: Segundo docente de Taller de Grado
- **WHEN** ya existe una cuenta de usuario activa con el rol de docente de Taller de Grado y el administrador crea otra con ese rol
- **THEN** el sistema rechaza la solicitud e indica que ya hay un docente de Taller de Grado activo

#### Scenario: Reactivación de un segundo docente de Taller de Grado
- **WHEN** hay un docente de Taller de Grado activo y el administrador reactiva otra cuenta de usuario desactivada con ese rol
- **THEN** el sistema rechaza la solicitud y la cuenta de usuario sigue desactivada

### Requirement: Alcance del director de carrera
El sistema SHALL permitir que el director de carrera gestione solo las cuentas de usuario cuyos roles son docente, docente de Taller de Grado o secretario(a) de dirección de carrera, y solo asigne o quite esos roles (FR-050). Los roles de director de carrera y de administrador, y las cuentas de usuario que los tienen, solo los gestiona el administrador (FR-053).

#### Scenario: Director de carrera crea un secretario
- **WHEN** el director de carrera envía `CrearUsuario(Rosa Vaca; rosa@uagrm.edu.bo; secretario)`
- **THEN** la cuenta de usuario queda creada

#### Scenario: Director de carrera asigna un rol fuera de su alcance
- **WHEN** el director de carrera envía `CrearUsuario` con el rol de director de carrera
- **THEN** el sistema rechaza la solicitud, indica que no puede asignar ese rol y no crea la cuenta de usuario

#### Scenario: Director de carrera gestiona una cuenta fuera de su alcance
- **WHEN** el director de carrera envía `DesactivarUsuario` con la dirección de correo de un estudiante o del administrador
- **THEN** el sistema rechaza la solicitud, indica que no puede gestionar esa cuenta de usuario y no la cambia

### Requirement: Consulta de cuentas de usuario
El sistema SHALL responder a `ListarUsuarios()` con las cuentas de usuario dentro del alcance de quien consulta, con su nombre completo, dirección de correo, roles y estado, y a `VerUsuario(correo)` con todos los datos de esa cuenta de usuario, sin cambiar nada (FR-030).

#### Scenario: Lista del administrador
- **WHEN** el administrador envía `ListarUsuarios()`
- **THEN** recibe todas las cuentas de usuario, activas y desactivadas, con sus roles y su estado

#### Scenario: Lista del director de carrera
- **WHEN** el director de carrera envía `ListarUsuarios()`
- **THEN** recibe solo las cuentas de usuario de docentes, del docente de Taller de Grado y de secretarios(as)

#### Scenario: Datos de una cuenta de usuario
- **WHEN** el administrador envía `VerUsuario(juan@uagrm.edu.bo)`
- **THEN** recibe el nombre completo, la dirección de correo, los roles, el estado, el registro universitario, la cédula de identidad y el teléfono de esa cuenta de usuario

### Requirement: Modificación de una cuenta de usuario
El sistema SHALL cambiar un dato de una cuenta de usuario con `ModificarUsuario(correo; campo; valor)`, con `campo` en `nombre`, `correo`, `roles`, `registro`, `cedula` o `telefono` sin distinguir mayúsculas, aplicando las mismas reglas que en la creación, y registrar el cambio con su valor anterior y su valor nuevo (FR-031).

#### Scenario: Cambio de teléfono
- **WHEN** el administrador envía `ModificarUsuario(juan@uagrm.edu.bo; telefono; 71111111)`
- **THEN** la cuenta de usuario queda con el teléfono `71111111`, el registro de cambios guarda el teléfono anterior y el nuevo, y el administrador recibe la confirmación

#### Scenario: Cambio de roles
- **WHEN** el administrador envía `ModificarUsuario(ana@uagrm.edu.bo; roles; docente, docente de taller de grado)`
- **THEN** la cuenta de usuario queda con los dos roles

#### Scenario: Campo desconocido
- **WHEN** el administrador envía `ModificarUsuario(ana@uagrm.edu.bo; edad; 40)`
- **THEN** el sistema rechaza la solicitud, indica los campos que se pueden modificar y no cambia la cuenta de usuario

### Requirement: Desactivación y reactivación
El sistema SHALL desactivar una cuenta de usuario con `DesactivarUsuario(correo)` y reactivarla con `ReactivarUsuario(correo)`, conservando sus datos y su historial, y registrar el cambio de estado (FR-032). Una cuenta de usuario desactivada no puede enviar comandos.

#### Scenario: Desactivación
- **WHEN** el administrador envía `DesactivarUsuario(ana@uagrm.edu.bo)`
- **THEN** la cuenta de usuario queda desactivada y un comando enviado después desde esa dirección no se ejecuta

#### Scenario: Reactivación
- **WHEN** el administrador envía `ReactivarUsuario(ana@uagrm.edu.bo)` para una cuenta de usuario desactivada
- **THEN** la cuenta de usuario queda activa con los mismos datos y roles

### Requirement: Siempre un administrador activo
El sistema SHALL rechazar, sin cambiar nada, que se desactive, o que pierda el rol de administrador, la única cuenta de administrador activa (FR-038).

#### Scenario: Desactivar al único administrador
- **WHEN** hay una sola cuenta de administrador activa y el administrador envía `DesactivarUsuario` con su propia dirección de correo
- **THEN** el sistema rechaza la solicitud e indica que el sistema quedaría sin un administrador activo

#### Scenario: Quitar el rol al único administrador
- **WHEN** hay una sola cuenta de administrador activa y se le cambian los roles a otro rol
- **THEN** el sistema rechaza la solicitud y la cuenta de usuario conserva el rol de administrador

### Requirement: Aviso al usuario afectado
El sistema SHALL avisar por correo al usuario de la cuenta de usuario cuando se crea, cuando se desactiva, cuando se reactiva o cuando cambian sus roles, indicando sus roles cuando corresponde (FR-035). Un cambio rechazado no genera aviso.

#### Scenario: Aviso de creación
- **WHEN** el administrador crea la cuenta de usuario de `ana@uagrm.edu.bo` con el rol de docente
- **THEN** `ana@uagrm.edu.bo` recibe un correo que le informa la creación de su cuenta de usuario y su rol de docente

#### Scenario: Sin aviso por un cambio de teléfono
- **WHEN** el administrador cambia solo el teléfono de una cuenta de usuario
- **THEN** el usuario no recibe un aviso

#### Scenario: Sin aviso por un rechazo
- **WHEN** el sistema rechaza una solicitud de crear una cuenta de usuario
- **THEN** nadie recibe un aviso de creación

### Requirement: Trámites afectados
El sistema SHALL, cuando se desactiva una cuenta de usuario o se le quita un rol que participa en trámites de titulación en curso, dejar esos trámites pendientes de reasignación en la misma transacción e indicarlos en la respuesta a quien gestiona (FR-034).

#### Scenario: Desactivación de un tutor con trámites
- **WHEN** el administrador desactiva la cuenta de usuario de un docente que es tutor en un trámite en curso
- **THEN** ese trámite queda pendiente de reasignación y la respuesta al administrador lo indica

#### Scenario: Sin trámites afectados
- **WHEN** el administrador desactiva una cuenta de usuario que no participa en trámites en curso
- **THEN** la respuesta al administrador no menciona trámites

### Requirement: Cuenta del estudiante al abrir su trámite
El sistema SHALL, al abrir un trámite de titulación, obtener la cuenta de usuario activa con el rol de estudiante cuya dirección de correo y registro universitario son los indicados, o crearla con el rol de estudiante si ninguna cuenta tiene esos datos (FR-051). La creación sigue las reglas de datos, de unicidad y de aviso de una cuenta nueva, sin el alcance de quien gestiona: la autoriza la apertura del trámite.

#### Scenario: Creación por el secretario(a)
- **WHEN** el secretario(a) de dirección de carrera abre un trámite para un estudiante sin cuenta de usuario
- **THEN** existe una cuenta de usuario activa del estudiante con el rol de estudiante, su creación queda en el registro de cambios y el estudiante recibe el aviso de creación de su cuenta

#### Scenario: Cuenta existente
- **WHEN** la dirección de correo y el registro universitario indicados pertenecen a la misma cuenta de usuario activa con el rol de estudiante
- **THEN** el sistema usa esa cuenta, sin cambiar sus datos ni registrar su creación

#### Scenario: Datos de cuentas distintas
- **WHEN** la dirección de correo pertenece a una cuenta de usuario y el registro universitario a otra, o la cuenta no tiene el rol de estudiante
- **THEN** el sistema rechaza la solicitud, indica el dato repetido y no cambia nada

### Requirement: Cuenta del estudiante desactivada
El sistema SHALL rechazar, sin cambiar nada, la apertura de un trámite de titulación cuando la cuenta de usuario del estudiante está desactivada, e indicar que la cuenta está desactivada.

#### Scenario: Estudiante desactivado
- **WHEN** el secretario(a) abre un trámite con la dirección de correo y el registro universitario de una cuenta de estudiante desactivada
- **THEN** el sistema rechaza la solicitud, indica que la cuenta de usuario está desactivada y no abre el trámite
