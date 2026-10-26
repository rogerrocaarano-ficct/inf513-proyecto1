# UC-001 Administrar usuarios

| | |
| --- | --- |
| Status | Detailed |
| Iteration | I1 |
| Primary actor | ACT-008 Administrador |
| Supporting actors | ACT-009 Servicio de correo |
| Priority | Must |
| Architecturally significant | Yes: el sistema identifica a cada remitente por su dirección de correo y lo autoriza según su rol (NFR-SE-001); todos los demás casos de uso dependen de esto |
| Feature | FEAT-001 |
| Functional requirements (derived) | FR-026, FR-030, FR-031, FR-032, FR-033, FR-034, FR-035, FR-036, FR-038, FR-039 |

## Brief Description

El administrador crea, consulta, modifica, desactiva y reactiva las cuentas de usuario. Cada cuenta tiene nombre completo, dirección de correo y uno o más roles; la de un estudiante tiene además su registro universitario, su cédula de identidad y su teléfono. Los roles de cuenta son estudiante, docente, docente de Taller de Grado, secretario(a) de dirección de carrera, director de carrera y administrador. El estudiante, el secretario(a) y el administrador tienen un solo rol. Un docente puede tener además el rol de director de carrera, y un director de carrera puede tener o no el rol de docente. El rol de docente de Taller de Grado solo se combina con el de docente, y solo una cuenta de usuario activa de la carrera lo tiene a la vez. Ser tutor o miembro del tribunal sale de las designaciones de cada trámite, no de la cuenta. La foto de perfil es opcional y la carga cada usuario (UC-016). Así el sistema reconoce a cada remitente por su dirección de correo y le permite solo lo que corresponde a sus roles y a sus designaciones.

## Preconditions

- El administrador tiene una cuenta de usuario activa con el rol de administrador. La primera se carga al instalar el sistema.

## Basic Flow

1. El administrador solicita crear una cuenta de usuario e indica nombre completo, dirección de correo y sus roles. Si uno de los roles es estudiante, indica además registro universitario, cédula de identidad y teléfono.
2. El sistema verifica que estén todos los datos que los roles exigen, que la combinación de roles esté permitida y, si uno de los roles es docente de Taller de Grado, que ninguna otra cuenta de usuario activa lo tenga.
3. El sistema verifica que la dirección de correo y, para un estudiante, el registro universitario no pertenezcan a otra cuenta de usuario.
4. El sistema crea la cuenta de usuario activa.
5. El sistema notifica al usuario, a través del Servicio de correo, que su cuenta de usuario fue creada y con qué roles.
6. El sistema confirma al administrador que la cuenta de usuario fue creada.

## Alternative Flows

### A1 Consultar cuentas de usuario

En el paso 1 del flujo básico, si el administrador solicita consultar cuentas de usuario:

1. El administrador indica una cuenta de usuario o pide la lista de todas.
2. El sistema muestra los datos de la cuenta de usuario indicada, o la lista con el nombre completo, la dirección de correo, los roles y el estado (activa o desactivada) de cada una.
3. El caso de uso termina.

### A2 Modificar cuenta de usuario

En el paso 1 del flujo básico, si el administrador solicita modificar una cuenta de usuario:

1. El administrador indica la cuenta de usuario y los datos nuevos: nombre completo, dirección de correo, roles o, para un estudiante, registro universitario, cédula de identidad y teléfono.
2. El sistema verifica los datos nuevos como en los pasos 2 y 3 del flujo básico, sin comparar la cuenta de usuario consigo misma.
3. Si el administrador quita un rol con el que el usuario participa en trámites de titulación en curso, el sistema le advierte y le muestra esos trámites.
4. El administrador confirma los cambios.
5. El sistema guarda los cambios. Los trámites en curso en los que el usuario participaba con el rol quitado quedan pendientes de reasignación.
6. Si cambiaron los roles, el sistema notifica al usuario, a través del Servicio de correo, sus roles nuevos.
7. El sistema confirma al administrador que la cuenta de usuario fue modificada.
8. El caso de uso termina.

### A3 Desactivar cuenta de usuario

En el paso 1 del flujo básico, si el administrador solicita desactivar una cuenta de usuario:

1. El administrador indica la cuenta de usuario activa que desea desactivar.
2. Si el usuario participa en trámites de titulación en curso, el sistema advierte al administrador y le muestra esos trámites.
3. El administrador confirma la desactivación.
4. El sistema desactiva la cuenta de usuario: el usuario ya no puede usar el sistema y su historial se conserva. Los trámites en curso en los que participa quedan pendientes de reasignación.
5. El sistema notifica al usuario, a través del Servicio de correo, que su cuenta de usuario fue desactivada.
6. El sistema confirma al administrador que la cuenta de usuario fue desactivada.
7. El caso de uso termina.

### A4 Reactivar cuenta de usuario

En el paso 1 del flujo básico, si el administrador solicita reactivar una cuenta de usuario:

1. El administrador indica la cuenta de usuario desactivada que desea reactivar.
2. El sistema reactiva la cuenta de usuario.
3. El sistema notifica al usuario, a través del Servicio de correo, que su cuenta de usuario fue reactivada.
4. El sistema confirma al administrador que la cuenta de usuario fue reactivada.
5. El caso de uso termina.

## Exceptions

### E1 Datos incompletos

En el paso 2 del flujo básico o en A2.2, si falta un dato que los roles exigen:

1. El sistema informa al administrador qué dato falta.
2. El caso de uso termina sin crear ni modificar la cuenta de usuario.

### E2 Dirección de correo o registro universitario repetido

En el paso 3 del flujo básico o en A2.2, si la dirección de correo o el registro universitario ya pertenece a otra cuenta de usuario:

1. El sistema informa al administrador qué dato está repetido.
2. El caso de uso termina sin crear ni modificar la cuenta de usuario.

### E3 Cuenta de usuario no encontrada

En A1.1, A2.1, A3.1 o A4.1, si la cuenta de usuario indicada no existe o no está en el estado que la operación requiere (activa para desactivar, desactivada para reactivar):

1. El sistema informa al administrador que no puede aplicar la operación a esa cuenta de usuario.
2. El caso de uso termina sin cambios.

### E4 Cambios cancelados

En A2.4 o A3.3, si el administrador no confirma:

1. El caso de uso termina sin modificar ni desactivar la cuenta de usuario.

### E5 Último administrador activo

En A2.1 o A3.1, si el administrador quita el rol de administrador o desactiva la única cuenta de usuario con ese rol que está activa:

1. El sistema informa al administrador que siempre debe existir un administrador activo.
2. El caso de uso termina sin modificar ni desactivar la cuenta de usuario.

### E6 Combinación de roles no permitida

En el paso 2 del flujo básico o en A2.2, si la combinación de roles no está permitida (el estudiante, el secretario(a) o el administrador con otro rol, o el docente de Taller de Grado sin el rol de docente o con un rol distinto de docente):

1. El sistema informa al administrador qué roles no se pueden combinar.
2. El caso de uso termina sin crear ni modificar la cuenta de usuario.

### E7 Docente de Taller de Grado ya asignado

En el paso 2 del flujo básico, en A2.2 o en A4.1, si otra cuenta de usuario activa ya tiene el rol de docente de Taller de Grado:

1. El sistema informa al administrador qué cuenta de usuario tiene ese rol; para asignarlo a otro docente, primero debe quitárselo a esa cuenta (A2).
2. El caso de uso termina sin crear, modificar ni reactivar la cuenta de usuario.

## Postconditions

- **Success:** la cuenta de usuario quedó creada, modificada, desactivada o reactivada, o el administrador recibió los datos consultados. El usuario fue notificado si su cuenta de usuario se creó, desactivó o reactivó, o si cambiaron sus roles.
- **Failure:** ninguna cuenta de usuario cambió.

## Special Requirements (derived)

- None

## Open Questions

- [x] ¿Cada usuario puede cambiar su propia foto de perfil o solo el administrador? Respuesta: cada usuario puede cambiar la suya (ver UC-016).
- [x] ¿La foto de perfil es obligatoria al crear la cuenta de usuario o se puede cargar después? Respuesta: es opcional y la carga cada usuario (UC-016).
- [x] A2: ¿qué pasa con los trámites en curso si se cambia el rol de un usuario que participa en ellos? Respuesta: una cuenta de usuario puede tener varios roles y el administrador los puede modificar; si quita un rol con el que el usuario participa en trámites en curso, el sistema advierte y esos trámites quedan pendientes de reasignación.
- [x] A3: ¿se puede desactivar la última cuenta de administrador activa? Respuesta: no; siempre debe existir un administrador activo (E5).
- [x] E6: ¿el rol de administrador se puede combinar con otros roles? Respuesta: no; quien administra y además participa en el trámite usa dos cuentas de usuario distintas.
- [x] ¿Cómo se registra el docente de Taller de Grado? Respuesta: es un rol de cuenta que solo se combina con el de docente; la carrera tiene un solo docente de Taller de Grado, que revisa todos los perfiles de proyecto (E6, E7).
- [x] E6: ¿el director de carrera tiene siempre también el rol de docente? Respuesta: no siempre; sin el rol de docente no puede ser designado tutor ni miembro del tribunal.
