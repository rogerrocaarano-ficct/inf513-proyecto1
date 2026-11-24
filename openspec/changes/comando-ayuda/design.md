# Design

## Context

- `mail` no conoce los usuarios ni los roles. `InboxPollerService` encuentra el `EmailCommandHandler` de cada comando por `actionKey()`, comprueba la cantidad de argumentos y adjuntos y llama a `handle`. La lista de la respuesta a un comando desconocido (`unknownCommandMessage`) es la única que conoce todos los comandos.
- Los permisos viven en `users.domain.RolePermission`. Cada caso de uso los comprueba en su método (`requireActiveRequester`, `User.hasPermissionTo`). Ningún handler declara qué permiso necesita su comando.
- Hay cuatro comandos cuyo permiso depende de un argumento: `usuarios.crearCuenta` usa `SIGNUP_<rol>`, `usuarios.listar` usa `LIST_<rol>`, y `usuarios.activar` y `usuarios.desactivar` usan `CHANGE_STATUS_<rol>`. Cada `RolePermission` ya tiene un `label` en español («crear cuentas de estudiante»).
- `procedures` ya depende de `users.domain` y `users.application`.
- La consulta del modelo EA está en pausa (ver «Diferencias con el modelo»).

## Goals / Non-Goals

**Goals:**

- Que la descripción de cada comando (sintaxis, propósito, permisos) viva junto a su handler, de modo que un comando nuevo no pueda quedar fuera de la ayuda sin que falle un test.
- Que `mail` siga sin depender de `users`.

**Non-Goals:**

- Ayuda detallada de un solo comando (`ayuda[comando]`): la cantidad de argumentos es exacta y una acción tiene un solo handler.
- Generar `docs/user-guide/` desde el código, o el código desde la guía.
- Atender a remitentes sin cuenta activa (ver la spec `command-help`).

## Decisions

### Cada handler describe su comando con un `CommandGuide`

En `users.application`, que es la raíz de los ports compartidos:

- `CommandGuide`: un record con `action`, `syntax` (el asunto con el nombre de cada argumento, `usuarios.listar[rol]`), `purpose` (para qué sirve), `notes` (adjuntos o texto en el cuerpo; vacío si no hay nada) y `permissions` (`Set<RolePermission>`: alguno de ellos basta para usar el comando).
- `DocumentedCommand`: un port con `CommandGuide guide()`. Lo implementan los 4 handlers de `users.application.usermanagement` y los 7 de `procedures`. Cada uno devuelve una constante `GUIDE` cuyo `action` es su `ACTION`.

Los conjuntos de permisos de los comandos con permiso parcial se arman con `RolePermission.toSignUp`, `toList` y `toChangeStatus` sobre todos los `Role`.

Alternativas descartadas:
- Agregar `guide()` a `mail.application.EmailCommandHandler`: haría que `mail` dependa de `users.domain.RolePermission`.
- Declarar los `CommandGuide` como beans sueltos en cada `*Configuration`: quedarían lejos del handler, y nada obligaría a escribirlos.
- Un mapa fijo de comandos y permisos dentro del caso de uso de ayuda: repetiría en `users` los comandos de `procedures`.

### El caso de uso `CommandHelpUseCase`

Está en `users.application.commandhelp`, junto con todo lo que pertenece solo a él:

- `CommandHelpUseCase(UserRepository users, List<DocumentedCommand> commands, CommandHelpNotifier notifier)`, con un único flujo `showHelp(String requesterEmail)`: obtiene el remitente activo (con el mismo rechazo de `UserNotAuthorizedException.notRegistered` y `disabled` que los demás casos de uso), filtra los comandos, los ordena por `action`, agrega la entrada de `ayuda` y pasa el resultado al notifier. No guarda nada y no levanta eventos.
- Un comando se incluye si `requester.hasPermissionTo(p)` vale para al menos un `p` de `permissions`. Si vale para algunos pero no para todos, el ítem lleva los `label` de los permisos concedidos. Si vale para todos, el ítem no lleva aclaración.
- `ShowHelpQueryHandler`: `ACTION = "ayuda"`, 0 argumentos, 0 adjuntos. Llama a `showHelp(email.getFrom())`.
- `ShowHelpQueryResult(String requesterEmail, List<ShowHelpQueryItem> commands)` y `ShowHelpQueryItem(String syntax, String purpose, String notes, List<String> allowedActions)`, siguiendo el patrón de `ListUsersQueryResult` y `ListUsersQueryItem`.
- `CommandHelpNotifier`, con `sendHelp(ShowHelpQueryResult result)`.

**Sin record `ShowHelpQuery`:** el comando no tiene argumentos, y el único dato es el remitente, que en todos los casos de uso se pasa aparte (`requesterEmail`). Un record vacío no agregaría nada.

**La entrada de `ayuda` la arma el caso de uso:** es la constante `CommandHelpUseCase.HELP_GUIDE`, sin permisos (la ven todos). `ShowHelpQueryHandler` no implementa `DocumentedCommand`, porque el caso de uso recibe todos los `DocumentedCommand` y el handler depende del caso de uso: si lo implementara, Spring encontraría una dependencia circular entre los beans.

### La respuesta

`MailCommandHelpNotifier`, en `users.infrastructure`, publica el resultado y lo envía en `@TransactionalEventListener(phase = AFTER_COMMIT)`, como `MailUserManagementNotifier.sendUserList`. Si el envío falla, se registra en el log y no se relanza el error. Forma del cuerpo:

```text
Comandos que puede usar:

usuarios.crearCuenta[nombres; primerApellido; segundoApellido; ci; codigo; correo; telefono; rol]
  Crear una cuenta de usuario con un rol.
  Puede: crear cuentas de estudiante.

tramites.abrirTramite[registro]
  Abrir el trámite de titulación de un estudiante.
  Adjuntos: 4 PDF.

ayuda[]
  Ver los comandos que puede usar.
```

Cuando el único comando es `ayuda`, el cuerpo empieza con «Por ahora su cuenta no tiene comandos disponibles.». La respuesta no enlaza a `docs/user-guide/`, porque los usuarios no tienen acceso al repositorio.

### Comando desconocido

`InboxPollerService.unknownCommandMessage` pasa a devolver «El comando "X" no existe. Envíe ayuda[] para ver los comandos que puede usar.». `mail` nombra `ayuda[]` solo como texto, sin depender de `users`. Desaparece la rama «No hay comandos disponibles».

### Wiring

`UsersConfiguration` declara `CommandHelpUseCase` (con `List<DocumentedCommand>`) y `ShowHelpQueryHandler`. Para que Spring reconozca los handlers como `DocumentedCommand` antes de crearlos, los métodos `@Bean` existentes ya devuelven el tipo concreto del handler.

## Risks / Trade-offs

- [Un comando nuevo sin `DocumentedCommand` no aparece en la ayuda] → `UsersWiringTest` comprueba que todo `EmailCommandHandler` del contexto, salvo `ShowHelpQueryHandler`, es un `DocumentedCommand` cuyo `guide().action()` coincide con su `actionKey()`, y que la cantidad de argumentos de `syntax` coincide con `requiredArgs()`.
- [La sintaxis y el propósito quedan escritos dos veces, en el `GUIDE` y en `docs/user-guide/`] → la regla de `AGENTS.md` ya obliga a actualizar la guía en el mismo cambio que agrega o cambia un comando. Se acepta la duplicación.
- [`mail` menciona `ayuda[]` sin saber si existe] → el test de integración de comando desconocido envía después `ayuda[]` y comprueba que se atiende.
- [Un rol nuevo, o un permiso que cambia de roles, cambia la ayuda] → la ayuda se calcula desde `RolePermission`, así que se actualiza sola.

## Migration Plan

Sin migraciones ni configuración. Se despliega con el resto de `develop`. Para volver atrás, se revierte el commit.

## Diferencias con el modelo

Trabajo compartido sin diseño en EA (la consulta del modelo está en pausa); se sigue la arquitectura de referencia. Elementos que el cambio crea, para llevarlos a EA con `ea-drift`:

- `users.application`: el record `CommandGuide` (`action`, `syntax`, `purpose`, `notes`, `permissions`) y la interfaz `DocumentedCommand` (`guide()`).
- `users.application.commandhelp`: `CommandHelpUseCase` (`showHelp(requesterEmail)`, `HELP_GUIDE`), `ShowHelpQueryHandler`, `ShowHelpQueryResult`, `ShowHelpQueryItem` y la interfaz `CommandHelpNotifier` (`sendHelp(result)`).
- Relaciones: `CommandHelpUseCase` usa `UserRepository`, `DocumentedCommand` y `CommandHelpNotifier`. Los 11 handlers de comando realizan `DocumentedCommand`.
- `users.infrastructure.MailCommandHelpNotifier` no se modela (infraestructura).
