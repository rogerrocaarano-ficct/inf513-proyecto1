# Unassigned: Entity Classes

## CuentaUsuario

| | |
| --- | --- |
| Use cases (derived) | UC-001 |
| Status | Draft |
| Iteration | I1 |

**Responsibilities:**

- Guardar los datos, los roles y el estado de un usuario, y su historial al desactivarse (UC-001)
- Asegurar que la dirección de correo y el registro universitario no pertenecen a otra cuenta de usuario (UC-001)
- Asegurar que siempre queda una cuenta de administrador activa (UC-001)

**Attributes:**

| Attribute | Description |
| --- | --- |
| nombre completo | Nombre y apellidos del usuario |
| dirección de correo | Dirección con la que el usuario se identifica ante el sistema |
| registro universitario | Solo para un estudiante |
| cédula de identidad | Solo para un estudiante |
| teléfono | Solo para un estudiante |
| foto de perfil | Opcional; la carga el propio usuario |
| estado | Activa o desactivada |

**Related to:**

- Rol: cada cuenta de usuario tiene uno o más roles (many to many)

## RegistroCambios

| | |
| --- | --- |
| Use cases (derived) | UC-001 |
| Status | Draft |
| Iteration | I1 |

**Responsibilities:**

- Guardar quién hizo cada cambio, cuándo y sobre qué dato (UC-001)

**Attributes:**

| Attribute | Description |
| --- | --- |
| fecha y hora | Cuándo se hizo el cambio |
| dato cambiado | Qué dato cambió y su valor anterior y nuevo |

**Related to:**

- CuentaUsuario: cada cambio lo hizo una cuenta de usuario (many to one)
- CuentaUsuario: un cambio puede afectar a una cuenta de usuario (many to one, optional)

## Rol

| | |
| --- | --- |
| Use cases (derived) | UC-001 |
| Status | Draft |
| Iteration | I1 |

**Responsibilities:**

- Conocer qué roles puede asignar quien tiene cada rol: el administrador, todos; el director de carrera, docente, docente de Taller de Grado y secretario(a) de dirección de carrera (UC-001)
- Asegurar las combinaciones de roles permitidas y que solo una cuenta de usuario activa tenga el rol de docente de Taller de Grado (UC-001)

**Attributes:**

| Attribute | Description |
| --- | --- |
| nombre | Estudiante, docente, docente de Taller de Grado, secretario(a) de dirección de carrera, director de carrera o administrador |

**Related to:**

- CuentaUsuario: un rol lo tienen muchas cuentas de usuario (many to many)

## TramiteTitulacion

| | |
| --- | --- |
| Use cases (derived) | UC-001 |
| Status | Draft |
| Iteration | I1 |

**Responsibilities:**

- Conocer las cuentas de usuario que participan en él y quedar pendiente de reasignación cuando una deja de poder participar (UC-001)

**Attributes:**

| Attribute | Description |
| --- | --- |
| estado | En curso o cerrado |
| pendiente de reasignación | Indica que falta reasignar a un participante |

**Related to:**

- CuentaUsuario: cada trámite es de un estudiante y tiene docentes designados como tutor y miembros del tribunal (many to many)
