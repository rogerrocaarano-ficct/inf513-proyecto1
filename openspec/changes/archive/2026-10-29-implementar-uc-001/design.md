# Design

## Context

La realización de diseño de UC-001 (`docs/model/use-cases/UC-001-gestionar-usuarios/design/realization.md`) se escribió antes del esqueleto. Desde entonces cambiaron tres cosas:

- Por D5 de `esqueleto-mail-app`, Messaging autentica y autoriza al remitente.
- Cada `CommandHandler` atiende un solo comando y declara sus roles permitidos.
- `handle(command, sender)` devuelve un `CommandReply(body)`.

El código actual de Users es lo mínimo para el remitente y el administrador inicial:

- `UserAccount` no tiene los datos del estudiante ni operaciones de cambio.
- `Role` no tiene reglas.
- La tabla `user_accounts` (`V1`) ya tiene las columnas del estudiante y los índices únicos de dirección de correo y de registro universitario.

`CommandDispatcher` ejecuta cada comando en una transacción. Si el manejador lanza una excepción, deshace la transacción y responde con un error interno genérico.

## Goals / Non-Goals

**Goals:**

- Que la realización de diseño de UC-001 y el código coincidan, con D5 aplicada.
- Que un rechazo de una regla deshaga todo lo que el comando hizo y responda el motivo, igual en todos los casos de uso.

**Non-Goals:**

- Los trámites de titulación reales: solo se crea la interfaz que Users necesita.
- Una consulta paginada o filtrada de cuentas de usuario: `ListarUsuarios()` devuelve todas las del alcance, porque son pocas (una carrera).

## Decisions

### D1. Un manejador por comando

Cada comando es una clase en `users/adapter/in`: `CreateUserCommandHandler`, `ListUsersCommandHandler`, `ViewUserCommandHandler`, `UpdateUserCommandHandler`, `DeactivateUserCommandHandler` y `ReactivateUserCommandHandler`. Las seis tienen `allowedRoles() = {ADMINISTRATOR, PROGRAM_DIRECTOR}`. Cada una:

1. Convierte los argumentos.
2. Llama a una operación de `UserManagementService` con `sender.accountId()`. El servicio carga la cuenta de usuario del remitente.
3. Arma el texto de la respuesta.

La lectura de argumentos y los textos de las respuestas se comparten en `UserCommandSupport`, del mismo paquete. Los nombres en español de los roles y de los campos viven en `Role` y `AccountField` (`label()`), porque los mensajes de rechazo del dominio también los nombran.

Alternativa: un solo `UserCommandHandler` que distingue el comando por su nombre, como en la realización actual. No encaja con `CommandHandler`, que declara un único `commandName()`, y mezclaría seis flujos en una clase.

### D2. Rechazos con `CommandRejectedException` de Messaging

Messaging agrega `CommandRejectedException(message)` a su paquete `application`. Cuando un manejador la lanza, `CommandDispatcher` hace lo mismo que ante un error interno: deshace la transacción y registra el Message-ID como procesado en una transacción nueva. La diferencia es la respuesta: contesta con el mensaje de la excepción, no con el error genérico.

`UserManagementService` lanza `UserRuleViolationException(reason, detail)`, que extiende `CommandRejectedException`. Así el servicio no conoce los textos de la respuesta, y el mensaje sale de `RejectionReason` y del detalle.

Alternativa: que el manejador capture la excepción y devuelva un `CommandReply` normal. No funciona: la excepción ya cruzó un método `@Transactional` y marcó la transacción para deshacerse, así que al confirmar Spring lanza `UnexpectedRollbackException` y el remitente recibe el error genérico. Además, cada caso de uso tendría que repetir el mismo `try/catch`.

### D3. Reglas de roles en `Role`

`Role` concentra las reglas que no dependen de la base de datos:

- `canManage(managerRoles, targetRoles)`: el administrador gestiona todo. El director de carrera gestiona solo cuando todos los roles destino están en `{PROFESSOR, THESIS_WORKSHOP_PROFESSOR, PROGRAM_SECRETARY}`.
- `validateCombination(roles)`: `STUDENT`, `PROGRAM_SECRETARY` y `ADMINISTRATOR` van solos, y `THESIS_WORKSHOP_PROFESSOR` solo se combina con `PROFESSOR`. Cualquier otra combinación está permitida, por ejemplo `PROGRAM_DIRECTOR` con `PROFESSOR`.
- `participantKinds()`: `STUDENT` → `{STUDENT}`, `PROFESSOR` → `{TUTOR, TRIBUNAL_MEMBER}` y el resto → `{}`.

`UserManagementService` aplica las reglas que sí consultan la base:

- Unicidad.
- Un solo docente de Taller de Grado activo.
- Al menos un administrador activo.
- Cuenta de usuario inexistente.

Al gestionar una cuenta de usuario existente, el alcance se comprueba sobre sus roles actuales. En `ModificarUsuario` de roles se comprueba además sobre los roles nuevos.

### D4. Datos y cambios en `UserAccount`

`UserAccount` incorpora `studentRegistrationNumber`, `idCard` y `phone`, y operaciones que devuelven los `FieldChange` para el registro de cambios:

- `changeField(field, value)`.
- `changeRoles(roles)`.
- `deactivate()` y `reactivate()`.

`create(data: UserAccountData)` reemplaza la fábrica actual y valida los datos obligatorios de E5:

- Nombre, dirección de correo y al menos un rol, para toda cuenta de usuario.
- Además, los tres datos del estudiante si tiene ese rol.

Si falta algo, lanza `UserRuleViolationException(MISSING_DATA)` con los nombres de los datos que faltan. El administrador inicial sigue usando `create`.

Los campos de `ModificarUsuario` se convierten a un enum `AccountField` (`FULL_NAME`, `EMAIL`, `ROLES`, `STUDENT_REGISTRATION_NUMBER`, `ID_CARD`, `PHONE`). El nombre en español de cada campo es su `label()`, como el de los roles. Cambiar los roles de una cuenta con `ModificarUsuario` pasa por las mismas reglas que la creación, y además comprueba el último administrador y los trámites afectados.

### D5. Unicidad con verificación previa e índice único

El servicio comprueba primero con `existsByEmailAndIdNot` y `existsByStudentRegistrationNumberAndIdNot`, para responder el dato repetido. Además guarda con `saveAndFlush`, y traduce una `DataIntegrityViolationException` de los índices `user_accounts_email_uk` o `user_accounts_registration_uk` a `DUPLICATE_EMAIL` o `DUPLICATE_REGISTRATION_NUMBER`. Así se cubre una carrera entre dos comandos, aunque hoy los comandos se procesan de a uno.

### D6. GraduationProcesses mínimo

Paquete `graduationprocesses/application` con:

- `GraduationParticipation`.
- `ParticipantKind`.
- `GraduationProcessSummary(id, studentName)`.
- `NoGraduationProcessesParticipation`, el `@Component` que implementa la interfaz y devuelve una lista vacía, porque todavía no hay trámites.

`UserManagementService` la llama al desactivar una cuenta de usuario (con todos los `ParticipantKind`) y al quitar roles (con los `participantKinds()` de los roles quitados). Las pruebas del escenario "Desactivación de un tutor con trámites" reemplazan el bean por un doble que devuelve un trámite.

Alternativa: no llamar a la interfaz hasta que exista el subsistema. Obligaría a volver a modificar `UserManagementService`, y A3.3 y A4.2 quedarían sin diseño probado.

### D7. Avisos y respuestas

Los avisos se envían con `Notifier` después de confirmar (D6 de `esqueleto-mail-app`):

- **Creación:** asunto `Cuenta de usuario creada`, con los roles.
- **Cambio de roles:** asunto `Roles actualizados`, con los roles nuevos.
- **Desactivación:** asunto `Cuenta de usuario desactivada`.
- **Reactivación:** asunto `Cuenta de usuario reactivada`.

El cuerpo es texto plano en español. Los textos de los avisos están en `UserManagementService`, que es quien los envía.

Las respuestas al gestor confirman la operación y, cuando corresponde, listan los trámites afectados. `ListarUsuarios()` devuelve una línea por cuenta de usuario: `nombre <correo> roles (estado)`. `VerUsuario` devuelve un dato por línea.

### D8. Modelo

Este cambio actualiza el modelo así:

- **Realización de diseño de UC-001:** sigue D1, D2 y D6, sin `authenticate` ni `UNAUTHORIZED_SENDER` (los hace Messaging), y con `handle(command, sender)`.
- **`messaging.md`:** documenta `CommandRejectedException`.
- **`users.md` y `graduation-processes.md`:** quedan alineados con D6.

## Risks / Trade-offs

- [Dos comandos simultáneos podrían saltarse las reglas del único docente de Taller de Grado o del último administrador, que solo comprueba el servicio] → Riesgo aceptado en la realización: el sondeo procesa los comandos de a uno.
- [La implementación vacía de `GraduationParticipation` hace que FR-034 no tenga efecto hasta que existan los trámites] → El comportamiento se prueba con un doble. El caso de uso que crea los trámites reemplaza la implementación sin tocar Users.
- [`ListarUsuarios()` sin paginación puede dar una respuesta larga] → Una carrera tiene decenas de cuentas de usuario, no miles.
