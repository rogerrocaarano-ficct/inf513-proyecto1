# Tasks

## 1. Renombre del puerto de salida

- [x] 1.1 Renombrar `UserManagementEventBus` → `UserManagementNotifier`, `notifyUserSignedUpEvent` → `notifyUserSignedUp`, el atributo `eventBus` → `notifier` de `UserManagementUseCase`, `MailUserManagementEventBus` → `MailUserManagementNotifier`, y sus referencias en `UsersConfiguration` y los tests (D5); verificar que no queda ninguna mención a `EventBus` en `src/` y que `./gradlew build` pasa sin cambiar ningún test de crear cuenta salvo los nombres

## 2. Dominio de usuarios

- [x] 2.1 Agregar a `RolePermission` los seis `LIST_*` con sus etiquetas y la operación estática `toList(role)` con `switch` exhaustivo (D2); verificar con pruebas unitarias en `RoleTest` la tabla completa de la spec "Quién puede listar cada rol" y que cada `LIST_<rol>` tiene los mismos roles que su `SIGNUP_<rol>`
- [x] 2.2 Agregar `getByRole(role)` al puerto `UserRepository` y a `JpaUserRepository`, con la consulta ordenada por primer apellido, segundo apellido (`nullsFirst`) y nombres (D4); verificar en `JpaUserRepositoryTest` (Testcontainers) que solo devuelve cuentas del rol, el orden (incluida una cuenta sin segundo apellido) y una lista vacía

## 3. Aplicación

- [x] 3.1 Implementar los records `ListUsersQuery`, `ListUsersQueryItem` y `ListUsersQueryResult` (D3), y la operación `sendUserList(result)` en `UserManagementNotifier`; verificar que `./gradlew compileJava` pasa
- [x] 3.2 Implementar `UserManagementUseCase.listUsers(requesterEmail, query)` según D1; verificar en `UserManagementUseCaseTest` con dobles de los puertos: remitente sin cuenta (E1), rol vacío o desconocido, rol fuera del alcance (E1 sin leer cuentas), lista con los datos de cada cuenta en el orden del repositorio, lista vacía, y que en los rechazos no se llama a `sendUserList`
- [x] 3.3 Implementar `ListUsersQueryHandler` (`usuarios.listar`, 1 argumento, 0 adjuntos) y registrarlo como bean en `UsersConfiguration`; verificar con una prueba unitaria que el argumento llega a `ListUsersQuery.role` y el solicitante es el remitente, y en `UsersWiringTest` que el handler queda registrado en el canal de correo

## 4. Infraestructura de respuesta

- [x] 4.1 Implementar en `MailUserManagementNotifier` el envío de la lista después del commit, con el asunto, el cuerpo y el texto de lista vacía de D5; verificar con una prueba que arma el cuerpo de una lista con una cuenta completa y otra con datos vacíos (`-`) y el de una lista vacía

## 5. Verificación integrada y documentación

- [x] 5.1 Escribir una prueba de extremo a extremo con GreenMail y PostgreSQL (como `SignUpByEmailIntegrationTest`): un administrativo lista estudiantes, un director lista docentes y no puede listar directores, un docente no puede listar, un remitente desconocido, `usuarios.listar[]` y un rol desconocido; verificar las respuestas, el orden, que las cuentas listadas no reciben correo y que `./gradlew build` pasa
- [x] 5.2 Agregar a USER-GUIDE.md la sección de `usuarios.listar[rol]`: argumento, roles que puede listar cada rol, datos de cada cuenta, orden, lista vacía y un ejemplo de respuesta; verificar que los ejemplos coinciden con lo que responde la prueba 5.1
- [x] 5.3 Probar a mano en el entorno local: con `./gradlew bootRun`, entrar a Roundcube como `admin@cpms.local`, crear dos cuentas de docente y enviar `usuarios.listar[docente]`; verificar la respuesta en el buzón
- [x] 5.4 Revisar la implementación contra los elementos de EA citados en el design (`Design/Users/domain`, `Design/Users/application`, diagrama "UC-001A3"); verificar con la skill `ea-drift users` que las únicas diferencias son las registradas en "Diferencias con el modelo"

## Workflow follow-up

- Archivar el change cuando las tareas estén hechas (`/opsx:archive`) e integrar la rama `UC-001-gestion-de-usuarios` en `develop`.
- Al archivar, actualizar el `## Purpose` de `openspec/specs/user-accounts/spec.md`, que hoy habla solo de crear cuentas.
