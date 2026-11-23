# Spec Delta

## ADDED Requirements

### Requirement: Registro del tráfico de correo en la consola

El sistema SHALL registrar en la consola cada conexión al servidor POP3, cada correo que entra (su UIDL, el remitente, la acción del comando y la cantidad de adjuntos), cada correo automático que ignora, cada mensaje que borra del servidor y cada correo que envía por SMTP (destinatario y cantidad de adjuntos). Las conexiones y las lecturas de un buzón vacío MUST registrarse en un nivel de detalle que no aparezca por omisión.

#### Scenario: Correo que entra
- **WHEN** el sistema lee del buzón un correo con asunto `usuarios.listar[]`, enviado por `ana@uagrm.bo` y sin adjuntos
- **THEN** la consola muestra su UIDL, el remitente `ana@uagrm.bo`, la acción `usuarios.listar` y 0 adjuntos

#### Scenario: Correo que entra sin un comando reconocible
- **WHEN** el sistema lee del buzón un correo con asunto `hola`
- **THEN** la consola muestra su UIDL y su remitente, e indica que no tiene una acción válida

#### Scenario: Correo automático ignorado
- **WHEN** el sistema lee del buzón un rebote del servidor de correo
- **THEN** la consola muestra que ignoró ese mensaje, con su UIDL y su remitente

#### Scenario: Mensaje borrado del servidor
- **WHEN** el sistema borra del servidor el mensaje de un correo procesado
- **THEN** la consola muestra el UIDL del mensaje borrado

#### Scenario: Correo que sale
- **WHEN** el sistema envía por SMTP un correo a `ana@uagrm.bo` con 1 adjunto
- **THEN** la consola muestra el destinatario `ana@uagrm.bo` y 1 adjunto

#### Scenario: Ciclo con el buzón vacío
- **GIVEN** el nivel de detalle de la consola es el de por omisión
- **WHEN** se ejecuta un ciclo de sondeo y el buzón no tiene mensajes
- **THEN** la consola no muestra ninguna línea del canal de correo

#### Scenario: Conexión al servidor POP3 con el nivel de detalle activado
- **GIVEN** el nivel de detalle del canal de correo está activado
- **WHEN** se ejecuta un ciclo de sondeo
- **THEN** la consola muestra la conexión al servidor POP3 y cuántos mensajes tiene el buzón

### Requirement: Registros sin datos del comando

Los registros del tráfico de correo MUST NOT incluir los argumentos del comando, el asunto completo de un correo que entra ni el asunto de un correo que sale, porque contienen datos personales.

#### Scenario: Comando con datos personales
- **WHEN** entra un correo con asunto `usuarios.crearCuenta[Ana; Pérez; ana@uagrm.bo; ESTUDIANTE; 218000001; 7654321; 70000000]`
- **THEN** la consola muestra la acción `usuarios.crearCuenta`
- **AND** no muestra ninguno de sus argumentos

#### Scenario: Respuesta a un comando con datos personales
- **WHEN** el sistema responde un error al correo anterior con el asunto `Re: usuarios.crearCuenta[…]`
- **THEN** la consola muestra el destinatario del envío
- **AND** no muestra el asunto ni ninguno de los argumentos

#### Scenario: Mensaje leído de nuevo con un UIDL ya procesado
- **GIVEN** un correo procesado con el comando `usuarios.crearCuenta[…]`
- **WHEN** el sistema vuelve a leer un mensaje con el mismo UIDL y lo borra sin ejecutarlo
- **THEN** la advertencia de la consola muestra la acción del correo guardado y la del leído
- **AND** no muestra sus asuntos ni sus argumentos
