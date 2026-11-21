## ADDED Requirements

### Requirement: Comandos para desactivar y activar una cuenta

El sistema SHALL desactivar una cuenta con el comando `usuarios.desactivar[email]` y activarla de nuevo con `usuarios.activar[email]`, cada uno con exactamente 1 argumento, el correo de la cuenta afectada, y ningún adjunto. El correo se compara sin distinguir mayúsculas. El solicitante es el remitente del correo.

#### Scenario: Desactivar una cuenta
- **GIVEN** existe la cuenta activa de estudiante `ana@uagrm.bo`
- **WHEN** un director envía `usuarios.desactivar[ana@uagrm.bo]`
- **THEN** la cuenta `ana@uagrm.bo` queda desactivada y conserva sus datos

#### Scenario: Activar una cuenta
- **GIVEN** la cuenta de estudiante `ana@uagrm.bo` está desactivada
- **WHEN** un director envía `usuarios.activar[Ana@UAGRM.bo]`
- **THEN** la cuenta `ana@uagrm.bo` queda activa

#### Scenario: Sin argumento
- **WHEN** un director envía `usuarios.desactivar[]`
- **THEN** el sistema no cambia ninguna cuenta y responde que el comando requiere 1 argumento

### Requirement: Quién puede desactivar y activar cada cuenta

Desactivar y activar requieren el mismo permiso, según el rol de la cuenta afectada. El administrador SHALL poder cambiar el estado de cuentas de todos los roles; el director, de estudiante, administrativo, docente y docente de Taller de Grado. Ningún otro rol cambia estados. Sin permiso, el sistema MUST NOT cambiar la cuenta y responde que no tiene permiso para cambiar el estado de cuentas de ese rol (E1).

#### Scenario: Director desactiva un docente
- **GIVEN** existe la cuenta activa de docente `luis@uagrm.bo`
- **WHEN** un director envía `usuarios.desactivar[luis@uagrm.bo]`
- **THEN** la cuenta `luis@uagrm.bo` queda desactivada

#### Scenario: Director intenta desactivar a otro director
- **GIVEN** existe la cuenta activa de director `carla@uagrm.bo`
- **WHEN** otro director envía `usuarios.desactivar[carla@uagrm.bo]`
- **THEN** la cuenta sigue activa y el director recibe que no tiene permiso para cambiar el estado de cuentas de director

#### Scenario: Administrador desactiva un director
- **GIVEN** existe la cuenta activa de director `carla@uagrm.bo`
- **WHEN** el administrador envía `usuarios.desactivar[carla@uagrm.bo]`
- **THEN** la cuenta `carla@uagrm.bo` queda desactivada

#### Scenario: Administrativo intenta desactivar un estudiante
- **GIVEN** existe la cuenta activa de estudiante `ana@uagrm.bo`
- **WHEN** un administrativo envía `usuarios.desactivar[ana@uagrm.bo]`
- **THEN** la cuenta sigue activa y el administrativo recibe que no tiene permiso para cambiar el estado de cuentas de estudiante

### Requirement: Cuenta afectada inexistente

Si ninguna cuenta tiene el correo indicado, el sistema SHALL rechazar el comando con «No existe una cuenta con el correo X.» y MUST NOT cambiar ninguna cuenta.

#### Scenario: Correo sin cuenta
- **WHEN** un director envía `usuarios.desactivar[nadie@uagrm.bo]`
- **THEN** el director recibe que no existe una cuenta con el correo `nadie@uagrm.bo`

### Requirement: Cuenta ya en el estado pedido

Desactivar una cuenta ya desactivada, o activar una cuenta ya activa, SHALL rechazarse con «La cuenta X ya está desactivada.» o «La cuenta X ya está activa.». La cuenta afectada MUST NOT recibir ningún aviso.

#### Scenario: Desactivar dos veces
- **GIVEN** la cuenta de estudiante `ana@uagrm.bo` está desactivada
- **WHEN** un director envía `usuarios.desactivar[ana@uagrm.bo]`
- **THEN** el director recibe que la cuenta `ana@uagrm.bo` ya está desactivada
- **AND** `ana@uagrm.bo` no recibe ningún correo

#### Scenario: Activar una cuenta activa
- **GIVEN** la cuenta de estudiante `ana@uagrm.bo` está activa
- **WHEN** un director envía `usuarios.activar[ana@uagrm.bo]`
- **THEN** el director recibe que la cuenta `ana@uagrm.bo` ya está activa

### Requirement: No desactivar la propia cuenta

El sistema SHALL rechazar que el solicitante desactive su propia cuenta, y MUST NOT cambiarla.

#### Scenario: Administrador intenta desactivarse
- **GIVEN** hay dos cuentas de administrador activas, `admin@cpms.local` y `admin2@cpms.local`
- **WHEN** `admin@cpms.local` envía `usuarios.desactivar[admin@cpms.local]`
- **THEN** la cuenta sigue activa y el administrador recibe que no puede desactivar su propia cuenta

### Requirement: Último administrador activo

El sistema SHALL rechazar la desactivación de la única cuenta de administrador activa (FR-038), y MUST NOT cambiarla.

#### Scenario: Única cuenta de administrador activa
- **GIVEN** `admin@cpms.local` y `admin2@cpms.local` tienen rol administrador y solo `admin2@cpms.local` está activa
- **WHEN** se pide desactivar `admin2@cpms.local`
- **THEN** la cuenta sigue activa y el solicitante recibe que no se puede desactivar la única cuenta de administrador activa

### Requirement: Remitente con cuenta desactivada

Una cuenta desactivada no puede usar el sistema (FR-032): todo comando de cuentas de usuario cuyo remitente tenga una cuenta desactivada SHALL rechazarse con «La cuenta X está desactivada. Comuníquese con la Dirección de Carrera.» (E1), sin ejecutar el comando.

#### Scenario: Cuenta desactivada intenta listar
- **GIVEN** la cuenta de administrativo `secretaria@uagrm.bo` está desactivada
- **WHEN** `secretaria@uagrm.bo` envía `usuarios.listar[estudiante]`
- **THEN** recibe que su cuenta está desactivada y que se comunique con la Dirección de Carrera
- **AND** no recibe la lista

#### Scenario: Cuenta desactivada intenta crear una cuenta
- **GIVEN** la cuenta de director `carla@uagrm.bo` está desactivada
- **WHEN** `carla@uagrm.bo` envía un `usuarios.crearCuenta[...]` válido
- **THEN** no se crea la cuenta y la directora recibe que su cuenta está desactivada

### Requirement: Avisos del cambio de estado

Cuando una cuenta se desactiva o se activa, el sistema SHALL enviar, solo después de confirmar el cambio, una confirmación al solicitante y un aviso a la cuenta afectada con el nombre de quien hizo el cambio (FR-035). El aviso de desactivación indica que se comunique con la Dirección de Carrera si cree que es un error. Si el cambio se rechaza, la cuenta afectada no recibe nada.

#### Scenario: Cuenta desactivada
- **WHEN** un director desactiva la cuenta `luis@uagrm.bo`
- **THEN** el director recibe la confirmación «Cuenta desactivada» con el nombre y el correo de la cuenta
- **AND** `luis@uagrm.bo` recibe un aviso de que su cuenta fue desactivada por el director

#### Scenario: Cuenta activada
- **WHEN** un director activa la cuenta `luis@uagrm.bo`
- **THEN** el director recibe la confirmación «Cuenta activada»
- **AND** `luis@uagrm.bo` recibe un aviso de que puede volver a enviar solicitudes al sistema

### Requirement: Estado de las cuentas nuevas y existentes

Una cuenta nueva, incluida la del administrador inicial, SHALL quedar activa. Las cuentas que existían antes de este cambio quedan activas.

#### Scenario: Cuenta recién creada
- **WHEN** un director crea la cuenta del docente `luis@uagrm.bo`
- **THEN** la cuenta `luis@uagrm.bo` está activa y puede enviar comandos

## MODIFIED Requirements

### Requirement: Datos de cada cuenta listada

Por cada cuenta, la lista SHALL mostrar el nombre completo, el correo, el CI, el código, el teléfono y el estado (`activa` o `desactivada`); un dato vacío se muestra como `-`. La lista incluye las cuentas desactivadas. La lista MUST NOT mostrar el identificador interno de la cuenta: el correo es el dato que identifica a la cuenta.

#### Scenario: Estudiante con todos los datos
- **GIVEN** existe la cuenta de estudiante de Ana María Pérez Rojas, `ana@uagrm.bo`, CI `1234567`, código `218001234`, teléfono `71234567`
- **WHEN** un administrativo lista las cuentas con rol estudiante
- **THEN** la entrada de esa cuenta muestra su nombre completo, `ana@uagrm.bo`, `1234567`, `218001234` y `71234567`

#### Scenario: Docente sin datos opcionales
- **GIVEN** existe la cuenta de docente de Luis Rojas, `luis@uagrm.bo`, sin CI, código ni teléfono
- **WHEN** un director lista las cuentas con rol docente
- **THEN** la entrada de esa cuenta muestra `-` como CI, código y teléfono

#### Scenario: Cuenta desactivada en la lista
- **GIVEN** la cuenta de estudiante `beto@uagrm.bo` está desactivada y `ana@uagrm.bo` está activa
- **WHEN** un administrativo lista las cuentas con rol estudiante
- **THEN** la lista incluye las dos cuentas, con «Estado: desactivada» para `beto@uagrm.bo` y «Estado: activa» para `ana@uagrm.bo`
