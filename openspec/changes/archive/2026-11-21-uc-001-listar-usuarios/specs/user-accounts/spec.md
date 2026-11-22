## ADDED Requirements

### Requirement: Comando para listar cuentas

El sistema SHALL responder con la lista de las cuentas de un rol al comando `usuarios.listar[rol]`, con exactamente 1 argumento y ningún adjunto. El rol es obligatorio y se indica por su etiqueta, sin distinguir mayúsculas. El solicitante es el remitente del correo.

#### Scenario: Listar un rol
- **GIVEN** el remitente es una cuenta de administrador
- **WHEN** llega `usuarios.listar[docente]`
- **THEN** el sistema le responde con la lista de las cuentas con rol docente

#### Scenario: Rol con mayúsculas
- **GIVEN** el remitente es una cuenta de administrador
- **WHEN** llega `usuarios.listar[Estudiante]`
- **THEN** el sistema le responde con la lista de las cuentas con rol estudiante

#### Scenario: Sin argumento
- **WHEN** llega `usuarios.listar[]`
- **THEN** el sistema no lista cuentas y responde que el comando requiere 1 argumento

#### Scenario: Rol vacío o desconocido
- **WHEN** llega `usuarios.listar[ ]` o `usuarios.listar[rector]`
- **THEN** el sistema no lista cuentas y responde que el rol no es válido, con los roles aceptados

### Requirement: Quién puede listar cada rol

El sistema SHALL listar las cuentas de un rol solo si el rol del solicitante permite crear cuentas de ese rol: el administrador lista todos los roles; el director, docente, docente de Taller de Grado, administrativo y estudiante; el administrativo, estudiante. Docentes, docente de Taller de Grado y estudiantes no listan cuentas.

#### Scenario: Director lista docentes
- **GIVEN** el remitente tiene rol director
- **WHEN** pide listar las cuentas con rol docente
- **THEN** el sistema le responde con la lista

#### Scenario: Director intenta listar directores
- **GIVEN** el remitente tiene rol director
- **WHEN** pide listar las cuentas con rol director
- **THEN** el sistema no lista cuentas y responde que no tiene permiso para listar cuentas de ese rol

#### Scenario: Administrativo lista estudiantes
- **GIVEN** el remitente tiene rol administrativo
- **WHEN** pide listar las cuentas con rol estudiante
- **THEN** el sistema le responde con la lista

#### Scenario: Administrativo intenta listar docentes
- **GIVEN** el remitente tiene rol administrativo
- **WHEN** pide listar las cuentas con rol docente
- **THEN** el sistema no lista cuentas y responde que no tiene permiso

#### Scenario: Docente intenta listar
- **GIVEN** el remitente tiene rol docente
- **WHEN** pide listar las cuentas de cualquier rol
- **THEN** el sistema no lista cuentas y responde que no tiene permiso

### Requirement: Remitente sin cuenta al listar

Cuando el remitente de `usuarios.listar` no tiene una cuenta de usuario, el sistema MUST NOT enviarle ninguna lista y SHALL responderle que no está registrado (UC-001, E1).

#### Scenario: Remitente desconocido
- **GIVEN** ninguna cuenta tiene la dirección `extrano@gmail.com`
- **WHEN** esa dirección envía `usuarios.listar[estudiante]`
- **THEN** el sistema no le envía ninguna lista y le responde que no está registrado en el sistema

### Requirement: Datos de cada cuenta listada

Por cada cuenta, la lista SHALL mostrar el nombre completo, el correo, el CI, el código y el teléfono; un dato vacío se muestra como `-`. La lista MUST NOT mostrar el identificador interno de la cuenta: el correo es el dato que identifica a la cuenta. La lista no muestra el estado de la cuenta.

#### Scenario: Estudiante con todos los datos
- **GIVEN** existe la cuenta de estudiante de Ana María Pérez Rojas, `ana@uagrm.bo`, CI `1234567`, código `218001234`, teléfono `71234567`
- **WHEN** un administrativo lista las cuentas con rol estudiante
- **THEN** la entrada de esa cuenta muestra su nombre completo, `ana@uagrm.bo`, `1234567`, `218001234` y `71234567`

#### Scenario: Docente sin datos opcionales
- **GIVEN** existe la cuenta de docente de Luis Rojas, `luis@uagrm.bo`, sin CI, código ni teléfono
- **WHEN** un director lista las cuentas con rol docente
- **THEN** la entrada de esa cuenta muestra `-` como CI, código y teléfono

### Requirement: Orden de la lista

El sistema SHALL ordenar la lista por primer apellido, luego segundo apellido y luego nombres.

#### Scenario: Dos cuentas con distinto apellido
- **GIVEN** existen las cuentas de estudiante de Carla Suárez y de Ana Pérez
- **WHEN** se listan las cuentas con rol estudiante
- **THEN** la cuenta de Ana Pérez aparece antes que la de Carla Suárez

### Requirement: Lista vacía

Cuando no hay cuentas del rol pedido, el sistema SHALL responder al solicitante que no hay cuentas con ese rol, sin tratarlo como un error.

#### Scenario: Ninguna cuenta del rol
- **GIVEN** no existe ninguna cuenta con rol docente_taller
- **WHEN** un administrador lista las cuentas con rol docente_taller
- **THEN** el sistema le responde que no hay cuentas con rol docente_taller

### Requirement: Respuesta del listado

El sistema SHALL enviar la lista solo al solicitante, en el cuerpo del correo, con el rol y la cantidad de cuentas. Listar MUST NOT modificar ninguna cuenta ni enviar avisos a las cuentas listadas.

#### Scenario: Solo responde al solicitante
- **WHEN** un director lista las cuentas con rol docente y hay dos
- **THEN** el director recibe un correo con las dos cuentas y la cantidad
- **AND** las cuentas listadas no reciben ningún correo
