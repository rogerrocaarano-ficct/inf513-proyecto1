# Unassigned: Entity Classes

## CuentaUsuario

| | |
| --- | --- |
| Use cases (derived) | UC-001, UC-003 |
| Status | Draft |
| Iteration | I1 |

**Responsibilities:**

- Guardar los datos, los roles y el estado de un usuario, y su historial al desactivarse (UC-001, UC-003)
- Asegurar que la dirección de correo y el registro universitario no pertenecen a otra cuenta de usuario (UC-001, UC-003)
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

## DocumentoInicial

| | |
| --- | --- |
| Use cases (derived) | UC-003 |
| Status | Draft |
| Iteration | I1 |

**Responsibilities:**

- Guardar el documento inicial digitalizado y su tipo (UC-003)
- Asegurar que es una imagen PNG o JPG/JPEG, o un PDF, de hasta 1 MB (UC-003)

**Attributes:**

| Attribute | Description |
| --- | --- |
| tipo | Carta de solicitud, hoja de vida, fotocopia de cédula de identidad o boleta de inscripción con Modalidad de Graduación |
| archivo | Documento digitalizado |
| fecha de carga | Cuándo se guardó el documento |

**Related to:**

- TramiteTitulacion: cada documento inicial pertenece a un trámite de titulación (many to one)

## RegistroCambios

| | |
| --- | --- |
| Use cases (derived) | UC-001, UC-003 |
| Status | Draft |
| Iteration | I1 |

**Responsibilities:**

- Guardar quién hizo cada cambio, cuándo y sobre qué dato (UC-001, UC-003)

**Attributes:**

| Attribute | Description |
| --- | --- |
| fecha y hora | Cuándo se hizo el cambio |
| dato cambiado | Qué dato cambió y su valor anterior y nuevo |

**Related to:**

- CuentaUsuario: cada cambio lo hizo una cuenta de usuario (many to one)
- CuentaUsuario: un cambio puede afectar a una cuenta de usuario (many to one, optional)
- TramiteTitulacion: un cambio puede afectar a un trámite de titulación (many to one, optional)

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
| Use cases (derived) | UC-001, UC-003 |
| Status | Draft |
| Iteration | I1 |

Representa también el expediente de titulación: los documentos del trámite y su archivo al cerrarse.

**Responsibilities:**

- Conocer las cuentas de usuario que participan en él y quedar pendiente de reasignación cuando una deja de poder participar (UC-001)
- Guardar los documentos iniciales, asegurar que estén los cuatro y admitir su reemplazo solo mientras está en curso (UC-003)
- Asegurar que un estudiante no tenga más de un trámite de titulación en curso (UC-003)
- Conocer su etapa, y cerrarse con la recepción de los ejemplares físicos y el expediente archivado solo después de la calificación final (UC-003)

**Attributes:**

| Attribute | Description |
| --- | --- |
| estado | En curso o cerrado; cerrado significa que el expediente de titulación está archivado |
| etapa | Etapa del trámite, de la apertura a la calificación final |
| fecha de apertura | Cuándo se abrió el trámite |
| fecha de cierre | Cuándo se cerró el trámite y se archivó su expediente |
| ejemplares físicos recibidos | Recepción del ejemplar para la biblioteca y del ejemplar para la Dirección de Carrera |
| pendiente de reasignación | Indica que falta reasignar a un participante |

**Related to:**

- CuentaUsuario: cada trámite es de un estudiante y tiene docentes designados como tutor y miembros del tribunal (many to many)
- DocumentoInicial: cada trámite tiene sus cuatro documentos iniciales (one to many)
