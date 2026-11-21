# Tasks

## 1. Estado de la cuenta en el dominio y la base de datos

- [x] 1.1 Agregar a `User` el campo `active`, `isActive()` y `withActive(active)`; `builder()` deja la cuenta activa (D3); verificar en `UserTest` que una cuenta nueva y una registrada con `signUp` están activas
- [x] 1.2 Agregar la migración `V3__user_account_active.sql` (`active BOOLEAN NOT NULL DEFAULT TRUE`), el campo en `UserAccountEntity` y su copia en `JpaUserRepository.copyFields` y `toDomain` (D6); verificar en `JpaUserRepositoryTest` (Testcontainers) que `update` guarda una cuenta desactivada, que `getByEmail` y `getByRole` la devuelven con su estado, y que una fila insertada sin la columna queda activa

## 2. Reglas de dominio

- [x] 2.1 Agregar a `RolePermission` los seis `CHANGE_STATUS_*` con sus etiquetas y `toChangeStatus(role)` con `switch` exhaustivo (D5); verificar en `RoleTest` la tabla completa de la spec "Quién puede desactivar y activar cada cuenta", que el administrativo no tiene ninguno y la etiqueta de un permiso
- [x] 2.2 Implementar los records `UserDisabledEvent` y `UserEnabledEvent`, y `User.disable(requester)` y `User.enable(requester)`, que cambian el estado, rechazan con error en `correo` si ya estaba en ese estado y emiten el evento (D3, D4); verificar en `UserTest` el cambio de estado, los datos del evento y que un rechazo no emite eventos
- [x] 2.3 Agregar `UserNotAuthorizedException.disabled(email)` con el mensaje de la spec "Remitente con cuenta desactivada" (D2); verificar en `UserNotAuthorizedExceptionTest` el mensaje y el código de error

## 3. Aplicación

- [x] 3.1 Agregar a `UserManagementUseCase` el método privado `requireActiveRequester` y usarlo en `signUpUser` y `listUsers` (D2); verificar en `UserManagementUseCaseTest` que un solicitante desactivado recibe `disabled` en los dos flujos sin que se inserte una cuenta ni se envíe la lista, y que las pruebas existentes siguen pasando
- [x] 3.2 Agregar `notifyUserDisabled(event)` y `notifyUserEnabled(event)` a `UserManagementNotifier` e implementar `disableUser(requesterEmail, userEmail)` y `enableUser(requesterEmail, userEmail)` según D1; verificar en `UserManagementUseCaseTest` con dobles de los puertos: éxito de cada uno con `update` y el evento notificado, correo sin cuenta, sin permiso (director sobre director, administrativo sobre estudiante), propia cuenta, único administrador activo, administrador desactivable cuando hay otro activo, cuenta ya en ese estado, y que en los rechazos no se llama a `update` ni al notifier
- [x] 3.3 Implementar `DisableUserCommandHandler` (`usuarios.desactivar`) y `EnableUserCommandHandler` (`usuarios.activar`), con 1 argumento y 0 adjuntos, y registrarlos en `UsersConfiguration` (D8); verificar con una prueba unitaria por handler que el argumento y el remitente llegan al caso de uso, y en `UsersWiringTest` que los dos quedan registrados en el canal de correo
- [x] 3.4 Agregar el componente `active` a `ListUsersQueryItem` y copiarlo en `listUsers` (D7); verificar en `UserManagementUseCaseTest` que la lista incluye una cuenta desactivada con `active = false`

## 4. Respuestas por correo

- [x] 4.1 Implementar en `MailUserManagementNotifier` los avisos de desactivación y activación después del commit, con los asuntos y textos de D7; verificar con pruebas en `MailUserManagementNotifierTest` el asunto y el cuerpo de los cuatro correos
- [x] 4.2 Agregar la línea `Estado: activa` / `Estado: desactivada` a `userListBody` (D7); verificar en `MailUserManagementNotifierTest` el cuerpo de una lista con una cuenta activa y otra desactivada

## 5. Verificación integrada y documentación

- [x] 5.1 Escribir `ChangeUserStatusByEmailIntegrationTest` con GreenMail y PostgreSQL (como `ListUsersByEmailIntegrationTest`): un director desactiva un docente y los dos reciben su correo; el docente desactivado intenta listar y recibe que su cuenta está desactivada; el director lo activa y el docente puede listar otra vez si tiene permiso; un director intenta desactivar a otro director; un administrativo intenta desactivar un estudiante; correo sin cuenta; desactivar dos veces; el administrador intenta desactivarse; la lista muestra el estado; verificar que `./gradlew build` pasa
- [x] 5.2 Agregar a USER-GUIDE.md las secciones de `usuarios.desactivar[email]` y `usuarios.activar[email]` (quién puede usarlas, rechazos, avisos y un ejemplo de respuesta), la línea `Estado:` en la de `usuarios.listar` y la respuesta a una cuenta desactivada; verificar que los ejemplos coinciden con lo que responde la prueba 5.1
- [x] 5.3 Probar a mano en el entorno local: con `./gradlew bootRun`, como `admin@cpms.local` en Roundcube crear un docente, desactivarlo, entrar como el docente y enviar un comando, y activarlo; verificar los correos en los dos buzones y la migración `V3` sobre la base local existente
- [x] 5.4 Revisar la implementación contra los elementos de EA citados en el design (`Design/Users/domain`, `Design/Users/application`, diagrama "UC-001-A2"); verificar con la skill `ea-drift users` que las únicas diferencias son las registradas en "Diferencias con el modelo"

## Workflow follow-up

- Archivar el change cuando las tareas estén hechas (`/opsx:archive`) y abrir el PR de la rama hacia `develop`.
