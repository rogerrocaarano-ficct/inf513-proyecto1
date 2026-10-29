# mail-commands Specification

## Purpose

Permite que los usuarios operen el sistema enviando correos cuyo asunto es un comando, y que reciban por correo la respuesta de cada comando (NFR-IN-001).

## Requirements

### Requirement: Sondeo del buzón
El sistema SHALL revisar el buzón POP3 configurado a intervalos regulares (30 segundos por defecto, configurable), procesar una sola vez cada correo nuevo dirigido a su dirección y eliminarlo del buzón después de procesarlo.

#### Scenario: Correo nuevo procesado
- **WHEN** llega un correo al buzón y pasa un intervalo de sondeo
- **THEN** el sistema procesa el correo y lo elimina del buzón

#### Scenario: Correo no se procesa dos veces
- **WHEN** el sistema ya procesó un correo y pasa otro intervalo de sondeo
- **THEN** el sistema no vuelve a procesarlo ni a responderlo

### Requirement: Correos que no se atienden
El sistema SHALL ignorar, sin procesarlos ni responderlos, los correos que no están dirigidos a su dirección, los que él mismo envió y las respuestas automáticas, para no responderse en bucle. Los correos dirigidos a otras direcciones quedan en el buzón.

#### Scenario: Correo propio en el buzón
- **WHEN** el buzón contiene un correo cuyo remitente es la dirección del sistema
- **THEN** el sistema no lo procesa ni lo responde, y lo deja en el buzón

#### Scenario: Correo dirigido a otra dirección
- **WHEN** el buzón contiene un correo que no tiene la dirección del sistema entre sus destinatarios
- **THEN** el sistema no lo procesa ni lo responde, y lo deja en el buzón

#### Scenario: Respuesta automática
- **WHEN** llega un correo marcado como respuesta automática (cabecera `Auto-Submitted` distinta de `no`)
- **THEN** el sistema lo elimina del buzón sin responderlo

### Requirement: Interpretación del comando del asunto
El sistema SHALL interpretar el asunto con la forma `Comando(arg1; arg2; …)`: el nombre del comando sin distinguir mayúsculas, y los argumentos separados por punto y coma, sin espacios al inicio ni al final.

#### Scenario: Comando con argumentos
- **WHEN** llega un correo con asunto `CrearUsuario(Ana Pérez; ana@uagrm.edu.bo; docente)`
- **THEN** el sistema identifica el comando `CrearUsuario` con los argumentos `Ana Pérez`, `ana@uagrm.edu.bo` y `docente`

#### Scenario: Comando sin argumentos
- **WHEN** llega un correo con asunto `ping()`
- **THEN** el sistema identifica el comando `Ping` sin argumentos

#### Scenario: Asunto mal formado
- **WHEN** llega un correo de un remitente autorizado con asunto `Hola` o `Ping(`
- **THEN** el sistema responde al remitente que el asunto no tiene la forma `Comando(arg1; arg2; …)`

#### Scenario: Comando desconocido
- **WHEN** llega un correo de un remitente autorizado con un comando que no existe
- **THEN** el sistema responde al remitente que el comando no existe

### Requirement: Adjuntos del comando
El sistema SHALL entregar al comando los archivos adjuntos del correo, con su nombre de archivo, para que un argumento pueda nombrarlos.

#### Scenario: Correo con adjunto
- **WHEN** llega un correo con un comando y un archivo adjunto `perfil.pdf`
- **THEN** el comando recibe el adjunto con el nombre `perfil.pdf` y su contenido

### Requirement: Identificación del remitente
El sistema SHALL identificar a quien envía un comando por la dirección de correo del remitente, sin distinguir mayúsculas, y SHALL atender el comando solo si esa dirección pertenece a una cuenta de usuario activa.

#### Scenario: Remitente registrado
- **WHEN** llega `Ping()` desde la dirección de una cuenta de usuario activa
- **THEN** el sistema ejecuta el comando

#### Scenario: Remitente desconocido
- **WHEN** llega un comando desde una dirección que no pertenece a ninguna cuenta de usuario activa
- **THEN** el sistema no ejecuta el comando y responde que el remitente no está autorizado

### Requirement: Autorización por roles del comando
El sistema SHALL ejecutar un comando solo si la cuenta de usuario del remitente tiene alguno de los roles que ese comando permite.

#### Scenario: Rol no permitido
- **WHEN** un remitente registrado envía un comando que sus roles no permiten
- **THEN** el sistema no ejecuta el comando y responde que no tiene permiso para ese comando

### Requirement: Respuesta por correo
El sistema SHALL responder cada comando atendido o rechazado con un correo al remitente, cuyo asunto cita el comando recibido y cuyo cuerpo contiene el resultado o el motivo del rechazo.

#### Scenario: Respuesta a un comando
- **WHEN** el sistema termina de atender un comando
- **THEN** el remitente recibe un correo con asunto `Re: <asunto original>` y el resultado en el cuerpo

#### Scenario: Error inesperado
- **WHEN** un comando falla por un error interno
- **THEN** el remitente recibe una respuesta que indica que el comando no se pudo ejecutar, sin detalles internos, y ningún cambio del comando queda guardado

### Requirement: Comando de diagnóstico Ping
El sistema SHALL ofrecer el comando `Ping()`, permitido a cualquier cuenta de usuario activa, que responde `Pong` sin cambiar datos.

#### Scenario: Ping
- **WHEN** una cuenta de usuario activa envía `Ping()`
- **THEN** recibe una respuesta con `Pong` en el cuerpo
