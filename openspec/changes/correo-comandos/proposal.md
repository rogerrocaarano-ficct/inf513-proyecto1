# Proposal

## Why

El sistema recibe todas las solicitudes de sus usuarios por correo electrónico (NFR-003), y ningún caso de uso puede funcionar sin un canal que lea el buzón, reconozca el comando del asunto, lo entregue al caso de uso correspondiente y responda los errores. Es trabajo compartido por todos los casos de uso y va primero porque UC-001 es el primero que lo necesita.

Pertenece al trabajo compartido **correo** (no a un único caso de uso). Lo usa UC-001 y, después, el resto de los casos de uso (UC-002 a UC-008).

## What Changes

- Sondeo periódico del buzón POP3 por lotes, con persistencia de cada correo recibido antes de procesarlo.
- Ciclo de vida del correo recibido (`RECEIVED`, `VALIDATED`, `PROCESSED`, `DISCARDED`) con seguimiento del UIDL del mensaje, para no ejecutar dos veces el mismo comando si la aplicación se detiene a mitad del flujo.
- Reconocimiento del comando en el asunto con la sintaxis `comando[arg; arg; …]`: la acción sin distinguir mayúsculas e ignorando prefijos como `Re:`, y los argumentos recortados.
- Contrato `EmailCommandHandler` para que cada caso de uso registre sus comandos sin que el paquete de correo conozca los casos de uso; validación de la cantidad de argumentos y de adjuntos antes de ejecutar.
- Respuesta por correo (SMTP) al remitente cuando el comando no se reconoce, no cumple el contrato o su ejecución termina con una excepción. Las respuestas de éxito quedan a cargo de cada caso de uso.
- Borrado del mensaje en el servidor cuando su correo queda en un estado final.
- Estructura de paquetes Java del proyecto (`common`, `mail`, `users` con `domain`, `application`, `infrastructure`), registrada en `AGENTS.md`.
- Dependencia de pruebas GreenMail (servidor POP3/SMTP embebido).

Fuera de alcance: los comandos de UC-001 (`uc-001-crear-cuenta`), el almacenamiento de adjuntos fuera del correo (`FileStorage`), y la autenticación y autorización del remitente, que decide cada caso de uso.

## Capabilities

### New Capabilities

- `mail-commands`: recepción de solicitudes por correo: lectura por lotes del buzón, reconocimiento y validación del comando, despacho al caso de uso, respuesta de errores al remitente y procesamiento sin duplicados.

### Modified Capabilities

Ninguna.

## Impact

- **Elementos de EA en los que se basa** (diagrama de clases `Design/Mail` "Mail"):
  - `Design/Mail/domain`: `ReceivedEmail`, `File`, `ReceivedEmailState`.
  - `Design/Mail/application`: `InboxPollerService`, `EmailCommandHandler`, `EmailInbox`, `EmailSender`, `ReceivedEmailRepository`, `FileStorage` (solo citado; queda fuera de alcance).
  - `Design/common`: `AggregateRoot`, `DomainEvent`, `DomainException`, `DomainValidationException`.
  - Análisis: `Análisis/Subsistemas funcionales/Administración` (`ClienteEmail`, `InterpreteComandos`, `GestorEmail`, `EmailRecibido`) y el diagrama de comunicación "UC-001 BF Crear Cuenta".
- **Requisitos**: NFR-001, NFR-003, NFR-007; riesgos RSK-001, RSK-003, RSK-004.
- **Código**: paquetes nuevos `bo.org.tecnoweb.cpms.common` y `bo.org.tecnoweb.cpms.mail`; migración Flyway para los correos recibidos; propiedades del servidor de correo y del sondeo en `application.properties`.
- **Dependencias**: `com.icegreen:greenmail` solo para pruebas. Los starters de mail, JPA y Flyway ya están en el build.
- **Documentos**: `AGENTS.md` (estructura de paquetes).
