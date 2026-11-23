# user-accounts Specification

## Purpose

Gestionar las cuentas de usuario con las que los participantes del trámite de titulación usan el sistema (UC-001). Para crear cuentas: quién puede crear qué cuentas, qué datos se exigen y cómo se normalizan, que no se repitan, cómo se avisa de la creación y cómo existe la primera cuenta de administrador. Para listarlas: quién puede listar las cuentas de cada rol, qué datos muestra la lista, en qué orden y a quién se envía. Para desactivarlas y activarlas: quién puede cambiar el estado de cada cuenta, qué cambios se rechazan, cómo se avisa del cambio y que una cuenta desactivada no puede usar el sistema.

## Requirements

### Requirement: Comando para crear una cuenta

El sistema SHALL crear una cuenta de usuario con el comando `usuarios.crearCuenta[nombres; primer apellido; segundo apellido; CI; código; correo; teléfono; rol]`, con exactamente 8 argumentos y ningún adjunto. El solicitante es el remitente del correo.

#### Scenario: Comando completo
- **GIVEN** el remitente es una cuenta de administrador
- **WHEN** llega `usuarios.crearCuenta[Ana María; Pérez; Rojas; 1234567; 218001234; ana@uagrm.bo; 71234567; estudiante]`
- **THEN** el sistema crea la cuenta de Ana María Pérez Rojas con rol estudiante

#### Scenario: Faltan argumentos
- **WHEN** llega `usuarios.crearCuenta` con 7 argumentos
- **THEN** el sistema no crea la cuenta y responde que se esperaban 8 argumentos

### Requirement: Un solo rol por cuenta

Cada cuenta SHALL tener exactamente un rol, indicado por su etiqueta sin distinguir mayúsculas: `estudiante`, `administrativo`, `docente`, `docente_taller`, `director` o `administrador`. Un rol que no es ninguno de estos se rechaza como dato inválido.

#### Scenario: Rol con mayúsculas
- **WHEN** se crea una cuenta con el rol `Docente`
- **THEN** la cuenta queda con el rol docente

#### Scenario: Rol desconocido
- **WHEN** se crea una cuenta con el rol `rector`
- **THEN** el sistema no crea la cuenta y responde que el rol no es válido, con los roles aceptados

### Requirement: Quién puede crear cada cuenta

El sistema SHALL crear la cuenta solo si el rol del solicitante permite crear cuentas del rol pedido: el administrador, de todos los roles; el director, de docente, docente de Taller de Grado, administrativo y estudiante; el administrativo, de estudiante. Docentes, docente de Taller de Grado y estudiantes no crean cuentas.

#### Scenario: Director crea un docente
- **GIVEN** el remitente tiene rol director
- **WHEN** pide crear una cuenta con rol docente
- **THEN** el sistema crea la cuenta

#### Scenario: Director intenta crear un director
- **GIVEN** el remitente tiene rol director
- **WHEN** pide crear una cuenta con rol director
- **THEN** el sistema no crea la cuenta y responde que no tiene permiso para crear cuentas de ese rol

#### Scenario: Administrativo crea un estudiante
- **GIVEN** el remitente tiene rol administrativo
- **WHEN** pide crear una cuenta con rol estudiante
- **THEN** el sistema crea la cuenta

#### Scenario: Docente intenta crear una cuenta
- **GIVEN** el remitente tiene rol docente
- **WHEN** pide crear cualquier cuenta
- **THEN** el sistema no crea la cuenta y responde que no tiene permiso

### Requirement: Remitente sin cuenta

Cuando el remitente del comando no tiene una cuenta de usuario, el sistema MUST NOT crear la cuenta pedida y SHALL responder al remitente que no está registrado (UC-001, E1).

#### Scenario: Remitente desconocido
- **GIVEN** ninguna cuenta tiene la dirección `extrano@gmail.com`
- **WHEN** esa dirección envía `usuarios.crearCuenta[...]`
- **THEN** el sistema no crea ninguna cuenta y le responde que no está registrado en el sistema

### Requirement: Datos obligatorios y formato

El sistema SHALL rechazar la cuenta si: los nombres o el primer apellido están vacíos; los nombres o apellidos contienen algo distinto de letras y espacios; el correo no es una dirección válida; el teléfono no son solo dígitos o tiene más de 8; el código no son solo dígitos o tiene más de 10; el CI no son solo dígitos o tiene más de 7. El segundo apellido puede ir vacío.

#### Scenario: Nombres con tildes y eñe
- **WHEN** se crea una cuenta con nombres `José Ñandú` y primer apellido `Güemes`
- **THEN** los datos se aceptan

#### Scenario: Varios datos inválidos
- **WHEN** se crea una cuenta con el primer apellido vacío y el teléfono `7123-4567`
- **THEN** el sistema no crea la cuenta y responde un error por cada campo: el primer apellido y el teléfono

#### Scenario: Correo inválido
- **WHEN** se crea una cuenta con el correo `ana.uagrm.bo`
- **THEN** el sistema no crea la cuenta y responde que el correo no es válido

#### Scenario: CI con complemento
- **WHEN** se crea una cuenta con el CI `1234567-1A`
- **THEN** el sistema no crea la cuenta y responde que el CI debe tener solo dígitos, hasta 7

### Requirement: Datos exigidos al estudiante

Para una cuenta con rol estudiante, el sistema SHALL exigir teléfono, código (registro universitario) y CI. Para los demás roles pueden ir vacíos, y si tienen valor cumplen el mismo formato.

#### Scenario: Estudiante sin CI
- **WHEN** se crea una cuenta de estudiante con el CI vacío
- **THEN** el sistema no crea la cuenta y responde que el CI es obligatorio

#### Scenario: Docente sin teléfono
- **WHEN** se crea una cuenta de docente con teléfono, código y CI vacíos
- **THEN** el sistema crea la cuenta

### Requirement: Normalización de nombres

El sistema SHALL guardar nombres y apellidos sin espacios al inicio ni al final, con un solo espacio entre palabras y con la primera letra de cada palabra en mayúscula y las demás en minúscula.

#### Scenario: Nombre con espacios y mayúsculas irregulares
- **WHEN** se crea una cuenta con nombres `  maría   de los   ÁNGELES `
- **THEN** la cuenta guarda los nombres `María De Los Ángeles`

### Requirement: Correo y código únicos

El sistema MUST NOT crear una cuenta cuyo correo o código ya pertenezca a otra cuenta (FR-033), y SHALL responder un error por cada dato repetido.

#### Scenario: Correo repetido
- **GIVEN** existe una cuenta con el correo `ana@uagrm.bo`
- **WHEN** se pide crear otra cuenta con ese correo
- **THEN** el sistema no crea la cuenta y responde que el correo ya pertenece a otra cuenta

#### Scenario: Código repetido
- **GIVEN** existe una cuenta con el código `218001234`
- **WHEN** se pide crear otra cuenta con ese código
- **THEN** el sistema no crea la cuenta y responde que el código ya pertenece a otra cuenta

### Requirement: Aviso de la cuenta creada

Cuando la cuenta se crea, el sistema SHALL enviar, solo después de confirmar la creación, una respuesta al solicitante con los datos de la cuenta y un aviso a la dirección de la cuenta nueva con su rol (FR-035). Si la creación falla, no se envía ninguno de los dos.

#### Scenario: Cuenta creada
- **WHEN** un director crea la cuenta del docente `luis@uagrm.bo`
- **THEN** el director recibe la confirmación con los datos de la cuenta
- **AND** `luis@uagrm.bo` recibe un aviso de que tiene una cuenta con rol docente

#### Scenario: Creación rechazada
- **WHEN** la creación de una cuenta se rechaza por un dato inválido
- **THEN** la cuenta nueva no recibe ningún aviso
- **AND** el solicitante recibe solo el error

### Requirement: Administrador inicial

Al arrancar, si hay una dirección de administrador configurada y ninguna cuenta la usa, el sistema SHALL crear con esa dirección una cuenta con rol administrador, sin solicitante ni aviso por correo. Si la dirección ya tiene cuenta, no la modifica.

#### Scenario: Primer arranque
- **GIVEN** la dirección de administrador configurada es `admin@cpms.local` y no hay cuentas
- **WHEN** el sistema arranca
- **THEN** existe una cuenta `admin@cpms.local` con rol administrador

#### Scenario: Arranque posterior
- **GIVEN** la cuenta `admin@cpms.local` ya existe
- **WHEN** el sistema arranca de nuevo
- **THEN** no se crea otra cuenta ni se modifica la existente

#### Scenario: Sin dirección configurada
- **GIVEN** no hay dirección de administrador configurada
- **WHEN** el sistema arranca
- **THEN** no crea ninguna cuenta

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

### Requirement: Cuentas de prueba del entorno local

Al arrancar en el entorno local de desarrollo, el sistema SHALL crear cuentas de prueba con direcciones `@test.local`: un administrativo, un docente de Taller de Grado, un director, cinco docentes y tres estudiantes. Las cuentas cumplen las mismas reglas de datos que una cuenta creada por correo y no reciben aviso por correo. Si una dirección ya tiene cuenta, no se modifica. Fuera del entorno local no se crea ninguna.

#### Scenario: Primer arranque local
- **GIVEN** el sistema arranca en el entorno local y no hay cuentas `@test.local`
- **WHEN** termina de arrancar
- **THEN** existen 1 cuenta administrativo, 1 docente de Taller de Grado, 1 director, 5 docentes y 3 estudiantes con direcciones `@test.local`, todas activas
- **AND** no se envía ningún correo por esas cuentas

#### Scenario: Arranque local posterior
- **GIVEN** las cuentas de prueba ya existen y una de ellas fue desactivada
- **WHEN** el sistema arranca de nuevo en el entorno local
- **THEN** no se crea ninguna cuenta repetida y la cuenta desactivada sigue desactivada

#### Scenario: Fuera del entorno local
- **WHEN** el sistema arranca fuera del entorno local
- **THEN** no se crea ninguna cuenta `@test.local`
