# mail-commands Specification

## Purpose

Recibir las solicitudes de los usuarios por correo electrónico: leer el buzón por lotes, reconocer y validar el comando escrito en el asunto, entregarlo al caso de uso que lo atiende y responder al remitente cuando el comando no puede ejecutarse, sin ejecutar nunca dos veces el mismo mensaje.

## Requirements

### Requirement: Lectura del buzón por lotes

El sistema SHALL leer periódicamente el buzón POP3 configurado y tomar en cada ciclo como máximo la cantidad de mensajes configurada. Cada mensaje leído SHALL quedar guardado antes de procesarse. Leer un mensaje no lo borra del servidor.

#### Scenario: Ciclo con más mensajes que el tamaño del lote
- **GIVEN** el tamaño del lote es 10 y el buzón tiene 15 mensajes
- **WHEN** se ejecuta un ciclo de sondeo
- **THEN** el sistema guarda y procesa 10 mensajes
- **AND** los 5 restantes se procesan en ciclos posteriores

#### Scenario: Buzón vacío
- **WHEN** se ejecuta un ciclo de sondeo y el buzón no tiene mensajes
- **THEN** el sistema no guarda ningún correo ni envía ninguna respuesta

### Requirement: Un mensaje se procesa una sola vez

El sistema SHALL identificar cada mensaje por su identificador único en el servidor (UIDL) y MUST NOT guardar ni ejecutar dos veces el mismo mensaje, aunque lo vuelva a leer después de un reinicio.

#### Scenario: Mensaje leído de nuevo tras un reinicio
- **GIVEN** un mensaje ya guardado cuyo correo está en un estado final (procesado o descartado)
- **AND** el mensaje sigue en el servidor porque la aplicación se detuvo antes de borrarlo
- **WHEN** el sistema vuelve a leer ese mensaje
- **THEN** no lo guarda otra vez ni lo ejecuta
- **AND** lo borra del servidor

#### Scenario: Reinicio durante la ejecución de un comando
- **GIVEN** un correo validado cuyo caso de uso no llegó a confirmar sus cambios porque la aplicación se detuvo
- **WHEN** la aplicación se reinicia y ejecuta un ciclo de sondeo
- **THEN** el sistema ejecuta ese comando una vez

#### Scenario: Reinicio después de confirmar el comando
- **GIVEN** un comando cuyo caso de uso confirmó sus cambios
- **WHEN** la aplicación se reinicia antes de borrar el mensaje del servidor
- **THEN** el sistema no vuelve a ejecutar ese comando

### Requirement: Sintaxis del comando en el asunto

El sistema SHALL reconocer en el asunto un comando con la forma `comando[arg1; arg2; …]` (NFR-003). El nombre del comando es la palabra (letras, dígitos, `_` o `.`) inmediatamente anterior a `[`, sin distinguir mayúsculas; el texto anterior se ignora. El asunto MUST terminar en `]`.

#### Scenario: Comando con mayúsculas distintas
- **WHEN** llega un correo con asunto `Usuarios.Listar[]`
- **THEN** el sistema lo reconoce como el comando `usuarios.listar`

#### Scenario: Asunto con prefijo del cliente de correo
- **WHEN** llega un correo con asunto `RE: Fwd: usuarios.listar[]`
- **THEN** el sistema lo reconoce como el comando `usuarios.listar`

#### Scenario: Asunto sin la forma de un comando
- **WHEN** llega un correo con asunto `hola` o `usuarios.listar[] gracias`
- **THEN** el sistema descarta el correo
- **AND** responde al remitente que el asunto debe tener la forma `comando[arg1; arg2; …]`

### Requirement: Argumentos del comando

El sistema SHALL separar los argumentos del texto entre `[` y el `]` final por cada `;`, en el orden en que aparecen, respetando mayúsculas y quitando los espacios del inicio y del final de cada uno. Los espacios internos de un argumento se conservan.

#### Scenario: Espacios alrededor de los argumentos
- **WHEN** llega el comando `cmd[ Ana María ;   Pérez;ana@uagrm.bo ]`
- **THEN** sus argumentos son `Ana María`, `Pérez` y `ana@uagrm.bo`, en ese orden

#### Scenario: Comando sin argumentos
- **WHEN** llega el comando `cmd[]`
- **THEN** el comando tiene 0 argumentos

#### Scenario: Argumento vacío
- **WHEN** llega el comando `cmd[a;;b]`
- **THEN** el comando tiene 3 argumentos y el segundo está vacío

### Requirement: Comando desconocido

Cuando ningún caso de uso atiende el comando reconocido, el sistema SHALL descartar el correo y responder al remitente que el comando no existe, con la lista de comandos disponibles.

#### Scenario: Comando inexistente
- **WHEN** llega el comando `usuarios.borrarTodo[]` y ningún caso de uso lo atiende
- **THEN** el sistema descarta el correo
- **AND** responde al remitente que el comando no existe e incluye los comandos disponibles

### Requirement: Cantidad de argumentos y adjuntos

Antes de ejecutar un comando, el sistema SHALL comprobar que la cantidad de argumentos y la de adjuntos sean exactamente las que declara el caso de uso que lo atiende. Si no coinciden, SHALL descartar el correo sin ejecutarlo y responder al remitente la cantidad esperada y la recibida.

#### Scenario: Faltan argumentos
- **GIVEN** un comando que requiere 4 argumentos y 0 adjuntos
- **WHEN** llega ese comando con 3 argumentos
- **THEN** el sistema descarta el correo sin ejecutar el caso de uso
- **AND** responde que se esperaban 4 argumentos y llegaron 3

#### Scenario: Adjuntos de más
- **GIVEN** un comando que requiere 0 adjuntos
- **WHEN** llega ese comando con 1 adjunto
- **THEN** el sistema descarta el correo sin ejecutar el caso de uso
- **AND** responde que se esperaban 0 adjuntos y llegó 1

### Requirement: Adjuntos guardados en el almacenamiento de archivos

Al guardar un correo leído del buzón, el sistema SHALL guardar el contenido de cada adjunto en el almacenamiento de archivos, en la misma transacción que el correo, y registrar en el correo solo la referencia a cada archivo, en su orden. La base de datos MUST NOT guardar el contenido de los adjuntos. Los archivos se conservan aunque el correo se descarte o su comando falle.

#### Scenario: Correo con dos adjuntos
- **WHEN** el sistema guarda un correo leído del buzón con dos adjuntos
- **THEN** cada adjunto queda guardado en el almacenamiento de archivos con su nombre, tipo de contenido y tamaño
- **AND** el correo registra las dos referencias en el orden de los adjuntos, sin su contenido

#### Scenario: Comando pendiente tras un reinicio
- **GIVEN** un correo guardado con un adjunto, que no llegó a un estado final porque la aplicación se detuvo
- **WHEN** la aplicación se reinicia y ejecuta un ciclo de sondeo
- **THEN** el sistema ejecuta el comando una vez con el mismo archivo guardado, sin volver a guardarlo

#### Scenario: Correo descartado con adjuntos
- **WHEN** un correo con un adjunto se descarta porque su comando no existe
- **THEN** el archivo del adjunto sigue en el almacenamiento de archivos, referenciado por el correo

#### Scenario: Falla al guardar un adjunto
- **WHEN** el almacenamiento de archivos falla al guardar el segundo adjunto de un correo
- **THEN** el correo no queda guardado y no queda ninguno de sus archivos
- **AND** el mensaje sigue en el servidor para un ciclo posterior

### Requirement: Adjunto vacío

Cuando un correo leído del buzón tiene un adjunto sin contenido, el sistema SHALL descartar el correo sin ejecutarlo, sin guardar ninguno de sus adjuntos, y responder al remitente indicando el nombre del adjunto vacío.

#### Scenario: Un adjunto de 0 bytes
- **WHEN** llega un comando con dos adjuntos y el segundo, `boleta.pdf`, tiene 0 bytes
- **THEN** el sistema descarta el correo sin ejecutar el caso de uso y sin guardar archivos
- **AND** responde al remitente que el adjunto `boleta.pdf` está vacío

### Requirement: Ejecución del comando

Un correo que cumple el contrato de su comando SHALL entregarse al caso de uso que lo atiende, con el remitente, los argumentos y los identificadores de los archivos guardados de sus adjuntos, en su orden. La confirmación de los cambios del caso de uso y el registro de que el correo fue procesado MUST ocurrir juntos: o se confirman los dos, o ninguno.

#### Scenario: Comando válido
- **WHEN** llega un comando reconocido que cumple su contrato
- **THEN** el sistema lo entrega a su caso de uso una vez
- **AND** el correo queda procesado

#### Scenario: Comando con adjuntos
- **WHEN** llega un comando válido con dos adjuntos
- **THEN** el caso de uso recibe los identificadores de los dos archivos guardados, en el orden de los adjuntos, y no su contenido

#### Scenario: Falla al confirmar los cambios
- **GIVEN** un comando válido
- **WHEN** el caso de uso falla y sus cambios se deshacen
- **THEN** el correo no queda registrado como procesado dentro de esos cambios

### Requirement: Respuesta de errores de ejecución

Cuando la ejecución de un comando termina con un error, el sistema SHALL deshacer los cambios del caso de uso, marcar el correo como procesado (no se reintenta) y responder al remitente. Un error de regla de negocio se responde con su mensaje en español; un error de validación lista un error por campo (NFR-001); cualquier otro error se responde como error interno, sin detalles técnicos.

#### Scenario: Regla de negocio incumplida
- **WHEN** el caso de uso rechaza el comando por una regla de negocio
- **THEN** el sistema responde al remitente con el mensaje de esa regla
- **AND** el correo queda procesado y no se vuelve a ejecutar

#### Scenario: Datos inválidos
- **WHEN** el caso de uso rechaza el comando porque dos campos son inválidos
- **THEN** la respuesta al remitente indica el error de cada uno de los dos campos

#### Scenario: Error inesperado
- **WHEN** la ejecución falla por un error que no es de negocio ni de validación
- **THEN** el sistema responde al remitente que ocurrió un error interno, sin detalles técnicos
- **AND** registra el error en el log

### Requirement: Respuestas de éxito a cargo del caso de uso

El canal de correo MUST NOT enviar respuesta cuando un comando se ejecuta con éxito: la respuesta de éxito y las notificaciones a otros usuarios las envía el caso de uso, solo después de confirmar sus cambios.

#### Scenario: Comando exitoso
- **WHEN** un comando se ejecuta con éxito
- **THEN** el canal de correo no envía ninguna respuesta propia
- **AND** el remitente recibe solo la respuesta que envía el caso de uso

### Requirement: Correos automáticos

El sistema MUST NOT responder ni ejecutar correos generados automáticamente: respuestas automáticas, rebotes, correos de listas o masivos y correos enviados por la propia dirección del sistema. Esos correos se ignoran.

#### Scenario: Rebote de un correo enviado por el sistema
- **WHEN** llega un aviso de no entrega del servidor de correo
- **THEN** el sistema no ejecuta ningún comando ni envía respuesta

#### Scenario: Respuesta automática de ausencia
- **WHEN** llega un correo marcado como generado automáticamente
- **THEN** el sistema no ejecuta ningún comando ni envía respuesta

### Requirement: Borrado del mensaje en el servidor

El sistema SHALL borrar del servidor cada mensaje cuyo correo quedó procesado o descartado, y MUST NOT borrar un mensaje cuyo correo aún no llegó a un estado final.

#### Scenario: Correo procesado
- **WHEN** un correo queda procesado
- **THEN** su mensaje se borra del servidor

#### Scenario: Correo pendiente
- **GIVEN** un correo guardado que aún no fue validado ni ejecutado
- **WHEN** termina el ciclo de sondeo
- **THEN** su mensaje sigue en el servidor
