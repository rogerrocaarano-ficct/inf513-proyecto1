# Diseño

## Context

En `develop`, UC-001 aporta cuentas, permisos, avisos y repositorio. El canal de correo aporta el despacho y la transacción compartida con el estado del mensaje. La revisión de UC-002 conserva los contratos y escenarios aprobados de apertura y reemplazo.

## Goals / Non-Goals

**Objetivos.** Incorporar UC-002 con agregado de dominio, puertos, adaptador JPA y manejadores específicos. Conservar la atomicidad, el índice de trámite en curso y los cuatro escenarios de aceptación.

**Fuera del alcance.** Cierre de trámites, cambios al flujo de UC-001, cambios en EA y documentos auxiliares.

## Decisions

- `DegreeProcedure` tiene identidad UUID, referencia numérica generada por PostgreSQL, estudiante, remitente y documentos iniciales. El agregado valida y emite eventos de apertura y sustitución. `InitialDocument` valida el PDF y clasifica por `carta`, `cv`, `ci` y `boleta`.
- `DegreeProcedureRepository` recibe y devuelve agregados. El adaptador JPA mapea el agregado, sus documentos y sus referencias a cuentas. El índice parcial de Flyway impide aperturas simultáneas para un estudiante. Se usa bloqueo pesimista mediante Spring Data en las lecturas que preceden a la apertura sobre una cuenta existente o a la sustitución.
- `DegreeProcedureUseCase` valida los permisos de Secretaría por `RolePermission`, reutiliza `UserRepository` para localizar e insertar cuentas y traduce colisiones de identificadores en el rechazo acordado. Conserva el límite de 25 MiB, la referencia `TR-001` y los dos comandos de correo. Ambos comandos tienen un record de argumentos.
- `DegreeProcedureNotifier` publica los eventos que procesa `MailDegreeProcedureNotifier` después de confirmar. A1 reutiliza `UserSignedUpEvent` y `UserManagementNotifier`. El aviso de alta mantiene el contenido de UC-001. A2 confirma el reemplazo a Secretaría.
- El despachador conserva la transacción de la operación y del estado del correo. Cualquier rechazo revierte el alta y los cambios del expediente.

## Risks / Trade-offs

- La referencia queda disponible después de persistir el agregado. El adaptador recupera el valor generado en la misma transacción para emitir la confirmación con la referencia definitiva.
- La clasificación por nombre exige un término inequívoco. La guía de usuario documenta las cuatro palabras y ejemplos válidos.

## Diferencias con el modelo

UC-002 sin diseño en EA; se sigue la arquitectura de referencia.

- Dominio. `DegreeProcedure`, `DegreeProcedureRepository`, `InitialDocument`, `DegreeProcedureOpenedEvent` y `DegreeProcedureDocumentReplacedEvent`. El agregado abre el expediente y reemplaza documentos. Cada expediente pertenece a una cuenta de estudiante, registra quién lo abrió y contiene un documento por tipo.
- Aplicación. `DegreeProcedureUseCase` con apertura, alta durante la apertura y reemplazo. `OpenDegreeProcedureCommandHandler`, `ReplaceInitialDocumentCommandHandler`, sus records `OpenDegreeProcedureCommand` y `ReplaceInitialDocumentCommand`, y el puerto `DegreeProcedureNotifier`.
- Cuentas. Permisos de apertura y reemplazo exclusivos de Secretaría en `RolePermission`. Consulta y creación de cuentas a través de `UserRepository`. Aviso de alta con `UserSignedUpEvent` y `UserManagementNotifier`.
- Infraestructura. `ProceduresConfiguration`, `MailDegreeProcedureNotifier`, `JpaDegreeProcedureRepository`, `DegreeProcedureEntity`, `InitialDocumentEntity` y `SpringDataDegreeProcedureRepository`.
