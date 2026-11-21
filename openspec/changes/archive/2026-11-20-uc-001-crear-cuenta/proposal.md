# Proposal

## Why

Todos los participantes del trámite necesitan una cuenta con su rol para usar el sistema (UC-001). Sin cuentas, el canal de correo ya integrado responde a cualquier comando "comando desconocido", y ningún otro caso de uso puede autorizar a su remitente. Crear cuentas es el primer paso para presentar el sistema y para implementar UC-002 en adelante.

Pertenece a **UC-001 Gestión de usuarios**, flujo básico **Crear Cuenta**.

## What Changes

- Comando por correo `usuarios.crearCuenta[nombres; primer apellido; segundo apellido; CI; código; correo; teléfono; rol]`, que crea una cuenta de usuario con un solo rol.
- Autorización según el rol de quien envía el comando: el administrador del sistema crea cuentas de todos los roles; el director, de docente, docente de Taller de Grado, administrativo y estudiante; el administrativo, de estudiante. Un remitente sin cuenta o sin permiso recibe el rechazo (E1).
- Validación y normalización de los datos de la cuenta, con un error por campo (NFR-001).
- Rechazo de una cuenta cuyo correo o código ya pertenece a otra (FR-033).
- Notificación por correo al solicitante y a la cuenta nueva, después de confirmar la creación (FR-035).
- Cuenta de administrador inicial, creada al arrancar a partir de configuración, para que exista quien cree las demás.
- **Cambios de requisitos** (REQUIREMENTS.md), para reflejar el diseño acordado:
  - FR-026 y FR-036 y su nota: una cuenta tiene **un solo rol**; el director actúa como docente porque los permisos de docente incluyen a DIRECTOR.
  - FR-050: el director también crea cuentas de **estudiante** y de administrativo.
  - FR-051: la cuenta del estudiante también se crea en UC-001, por el administrativo, el director o el administrador.

Fuera de alcance: los flujos alternos A1 a A5 de UC-001 (actualizar, desactivar, listar, actualizar correo y teléfono propios), el estado de la cuenta (FR-032) y la apertura del trámite (UC-002).

## Capabilities

### New Capabilities

- `user-accounts`: creación de cuentas de usuario por correo: autorización por rol, validación y normalización de los datos, unicidad, notificación y cuenta de administrador inicial.

### Modified Capabilities

Ninguna. El canal de correo (`mail-commands`) se usa tal como está: el caso de uso se conecta registrando un `EmailCommandHandler`.

## Impact

- **Elementos de EA en los que se basa:**
  - Requisitos: caso de uso `UC-001 Gestión de usuarios` (escenario básico "Crear Cuenta", excepción E1).
  - Análisis: `Análisis/Subsistemas funcionales/Administración`, diagrama de comunicación "UC-001 BF Crear Cuenta" (`ClienteEmail`, `GestorEmail`, `InterpreteComandos`, `GestorUsuarios`, `Usuario`, `Rol`).
  - `Design/Users/domain`: `User`, `Role`, `RolePermission`, `UserRepository`, `UserSignedUpEvent`, `UserNotAuthorizedException`.
  - `Design/Users/application`: `UserManagementUseCase`, `SignUpUserCommand`, `SignUpUserCommandHandler`, `UserManagementEventBus`.
  - `Design/Mail/application`: `EmailCommandHandler`, `EmailSender`, ya implementados.
  - `Design/common`: `AggregateRoot`, `DomainEvent`, `DomainException`, `DomainValidationException`, ya implementados.
  - Diagrama de clases `Design/Users` "Users".
- **Requisitos:** FR-026, FR-033, FR-035, FR-036, FR-050, FR-051, FR-053; NFR-001, NFR-003.
- **Código:** paquete nuevo `bo.org.tecnoweb.cpms.users` (`domain`, `application`, `infrastructure`); migración Flyway de la tabla de cuentas; propiedad `CPMS_ADMIN_EMAIL` y su valor en el perfil `local`.
- **Documentos:** REQUIREMENTS.md (FR-026, FR-036, FR-050, FR-051 y sus notas), GLOSSARY.md (términos adoptados en el código para cuenta de usuario, rol y registro universitario), README.md (cuenta de administrador del entorno local y comando de ejemplo), USER-GUIDE.md (nuevo: guía de comandos).
- **Dependencias:** ninguna nueva.
