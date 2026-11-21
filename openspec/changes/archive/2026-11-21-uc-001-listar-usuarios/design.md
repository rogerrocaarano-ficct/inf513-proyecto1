# Design

## Context

Ver `proposal.md` (Why) y `specs/user-accounts/spec.md`. El flujo básico de UC-001 ya está implementado: `SignUpUserCommandHandler` se conecta al canal de correo como `EmailCommandHandler`, `UserManagementUseCase` corre dentro de la transacción que abre `InboxPollerService`, y el puerto de salida de Users (hoy `UserManagementEventBus`) envía las respuestas después del commit con un `@TransactionalEventListener(AFTER_COMMIT)` en `MailUserManagementEventBus`.

El diseño sigue el diagrama de clases `Design/Users` "UC-001A3" y las notas de sus elementos en EA (`ListUsersQueryHandler`, `ListUsersQuery`, `ListUsersQueryResult`, `ListUsersQueryItem`, `UserManagementUseCase`, `UserManagementNotifier`, `UserRepository`, `RolePermission`). Este documento no repite esas notas: explica cómo se implementan.

La tabla `user_account` (V2) ya tiene todos los datos que muestra la lista; no hace falta migración.

## Goals / Non-Goals

**Goals:**
- A3 completo por correo, de punta a punta, con las clases del modelo.
- Mismo alcance por rol que al crear cuentas, expresado con permisos `LIST_*` propios.
- El puerto de salida con el nombre del modelo (`UserManagementNotifier`), sin cambiar el comportamiento de crear cuenta.

**Non-Goals:**
- Paginar o partir la lista en varios correos.
- Unificar `SIGNUP_*` y `LIST_*` en un solo permiso por rol: se reconsidera si A1 o A2 vuelven a repetir los mismos conjuntos de roles.

## Decisions

### D1. Flujo entre las clases del modelo

```
 InboxPollerService (mail) --handle(email)--> ListUsersQueryHandler              users.application
                                                | ListUsersQuery(args[0])
                                                v
                                         UserManagementUseCase.listUsers(requesterEmail, query)
                                                | requester = users.getByEmail(requesterEmail)  --> vacío: UserNotAuthorizedException.notRegistered (E1)
                                                | role = Role.fromLabel(query.role())           --> vacío o desconocido: DomainValidationException
                                                | requester.hasPermissionTo(RolePermission.toList(role))
                                                |                                               --> no: UserNotAuthorizedException (E1)
                                                | accounts = users.getByRole(role)              (ya ordenadas, D4)
                                                | result = ListUsersQueryResult(items, role, requester)
                                                | notifier.sendUserList(result)
                                                v
                                         commit (junto con PROCESSED) --> correo con la lista (D5)
```

- `ListUsersQueryHandler`: `actionKey()` = `usuarios.listar`, `requiredArgs()` = 1, `requiredFiles()` = 0. `usuarios.listar[]` da 0 argumentos y lo rechaza el canal; `usuarios.listar[ ]` da un argumento vacío y lo rechaza `Role.fromLabel`.
- Se comprueba el permiso antes de leer las cuentas: un solicitante sin permiso no recibe nada del rol pedido.
- `ListUsersQueryHandler` se registra como `@Bean` en `UsersConfiguration`, igual que `SignUpUserCommandHandler`.

### D2. Permisos `LIST_*` y `RolePermission.toList(role)`

Se agregan a la enum `RolePermission` los seis `LIST_*` del modelo, con los mismos roles que el `SIGNUP_*` del mismo rol (nota de `RolePermission` en EA), y la operación estática `toList(role)` con un `switch` exhaustivo, como `toSignUp(role)`. El `switch` exhaustivo hace que un rol nuevo no compile hasta tener su permiso de listado.

Las etiquetas siguen el patrón de las de creación, porque forman el mensaje de `UserNotAuthorizedException` ("La cuenta <correo> no tiene permiso para <etiqueta>."): `listar cuentas de estudiante`, `listar cuentas de administrativo`, `listar cuentas de docente`, `listar cuentas de docente de Taller de Grado`, `listar cuentas de director`, `listar cuentas de administrador`.

Alternativa descartada: reutilizar `toSignUp(role)` para listar. Ataría el listado a la creación sin que el modelo lo diga, y A1/A2 podrían necesitar otros conjuntos.

### D3. `ListUsersQuery`, `ListUsersQueryResult` y `ListUsersQueryItem` como records

Son datos de la capa de aplicación sin comportamiento, como `SignUpUserCommand`:

- `ListUsersQuery(String role)`: la etiqueta tal como llega.
- `ListUsersQueryItem(fullName, phoneNumber, code, emailAddress, role, ci)`: `fullName` = `User.getFullName()`, `code` = `getUagrmCode()`, `role` = etiqueta del rol; los datos vacíos quedan en `null`.
- `ListUsersQueryResult(List<ListUsersQueryItem> users, String role, String requesterUserEmail)`: `role` es la etiqueta normalizada (`Role.getLabel()`), no lo que escribió el usuario; `requesterUserEmail` es el correo de la cuenta del solicitante. La lista se copia con `List.copyOf`.

El use case arma el resultado (conector `Instantiate` de `UserManagementUseCase` a `ListUsersQueryResult`); la conversión de `User` a ítem es un método privado del use case.

### D4. Orden en `UserRepository.getByRole`

El orden lo da la consulta, como dice la nota de `UserRepository`: `JpaUserRepository.getByRole` llama a `SpringDataUserAccountRepository.findByRole(role, sort)` con `Sort` por `lastName`, `secondLastName` (`nullsFirst`) y `firstName`, y rehidrata cada entidad. Con `nullsFirst`, "Pérez" sin segundo apellido va antes que "Pérez Rojas".

Alternativa descartada: ordenar en el use case. Duplica en memoria lo que la base hace con la consulta, y la nota del modelo pone el orden en el repositorio.

### D5. `UserManagementNotifier.sendUserList` y renombre del puerto

- Renombres, como en el modelo: `UserManagementEventBus` → `UserManagementNotifier`, `notifyUserSignedUpEvent` → `notifyUserSignedUp`, el atributo `eventBus` → `notifier`, y la implementación `MailUserManagementEventBus` → `MailUserManagementNotifier`. También el parámetro del bean en `UsersConfiguration` y los tests que los nombran.
- `sendUserList(result)` sigue el mismo mecanismo que `notifyUserSignedUp`: publica el resultado como evento de aplicación de Spring y un `@TransactionalEventListener(AFTER_COMMIT)` envía el correo. Así la respuesta sale solo si la transacción del comando hace commit, igual que en crear cuenta.
- Formato (nota de `UserManagementNotifier`): asunto `Cuentas de usuario: <rol> (<n>)`; cuerpo con una entrada numerada por cuenta: `<n>. <nombre completo>`, y en las líneas siguientes `Correo: …` y `CI: … | Código: … | Teléfono: …`, con `-` en los datos vacíos. Si `users` está vacía, el cuerpo es `No hay cuentas con rol <rol>.`
- El cuerpo lo arma un método privado de `MailUserManagementNotifier`; los textos en español viven en la infraestructura, como los de crear cuenta.

## Risks / Trade-offs

- [El orden depende de la intercalación (*collation*) de la base: con `C`, `Álvarez` queda después de `Zapata`] → Se acepta la intercalación de la base de datos; los tests de orden usan apellidos sin tildes iniciales. Si molesta, se ordena con una intercalación en español en otro change.
- [Una lista grande da un correo largo] → Las cantidades esperadas (una carrera) caben en un correo; paginar queda fuera de alcance.
- [El renombre del puerto toca código de crear cuenta] → Es un renombre sin cambio de comportamiento; lo cubren los tests de crear cuenta existentes (`UserManagementUseCaseTest`, `SignUpByEmailIntegrationTest`).

## Diferencias con el modelo

- Las etiquetas de los permisos `LIST_*` (D2) no están en el modelo; tampoco las de los `SIGNUP_*`, que se implementaron igual.
