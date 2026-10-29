# Spec Delta

## Purpose

Permite que el sistema avise por correo a un usuario de un cambio que lo afecta, aunque ese usuario no haya enviado el comando que lo causó (NFR-IN-001).

## ADDED Requirements

### Requirement: Envío de avisos por correo
El sistema SHALL enviar un aviso por correo a la dirección indicada, con el asunto y el cuerpo que define el caso de uso que lo origina.

#### Scenario: Aviso enviado
- **WHEN** un caso de uso confirma un cambio y pide avisar a `ana@uagrm.edu.bo`
- **THEN** `ana@uagrm.edu.bo` recibe un correo con el asunto y el cuerpo indicados

### Requirement: Aviso solo de cambios confirmados
El sistema SHALL enviar un aviso solo después de que el cambio que lo origina se confirma; si el cambio se deshace, el aviso no se envía.

#### Scenario: Cambio deshecho
- **WHEN** un caso de uso pide avisar a un usuario y luego su cambio se deshace por un rechazo o un error
- **THEN** el usuario no recibe el aviso

### Requirement: Fallo de envío no deshace el cambio
El sistema SHALL conservar un cambio ya confirmado aunque el aviso no se pueda enviar, y SHALL dejar registro del fallo.

#### Scenario: Servidor de correo caído
- **WHEN** el cambio se confirma y el servidor SMTP no acepta el aviso
- **THEN** el cambio sigue guardado y el fallo queda en el registro de la aplicación
