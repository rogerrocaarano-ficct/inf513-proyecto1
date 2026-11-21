## Purpose

Crear las cuentas de usuario con las que los participantes del trámite de titulación usan el sistema: quién puede crear qué cuentas, qué datos se exigen y cómo se normalizan, que no se repitan, cómo se avisa de la creación y cómo existe la primera cuenta de administrador.

## ADDED Requirements

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
