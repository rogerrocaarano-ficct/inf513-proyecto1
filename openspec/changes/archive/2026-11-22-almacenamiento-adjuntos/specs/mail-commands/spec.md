# Spec Delta

## ADDED Requirements

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

## MODIFIED Requirements

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
