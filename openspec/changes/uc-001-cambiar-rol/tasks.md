# Tasks

## 1. Reglas de dominio

- [x] 1.1 Agregar a `RolePermission` los seis `CHANGE_ROLE_*` con sus etiquetas y `toChangeRole(role)` con `switch` exhaustivo (D3); verificar en `RoleTest` la tabla de la spec "Quién puede cambiar el rol de cada cuenta", que el administrativo no tiene ninguno y la etiqueta de un permiso
- [x] 1.2 Cambiar `User.validate()` por `validate(Role role)` y llamarlo desde `signUp` con el rol de la cuenta (D4); verificar que las pruebas existentes de `UserTest` siguen pasando
- [x] 1.3 Implementar el record `UserRoleChangedEvent` y `User.changeRole(requester, newRole)`, que rechaza el mismo rol con error en `rol`, valida con el rol nuevo antes de asignarlo y emite el evento (D4, D5); verificar en `UserTest` el cambio de rol y los datos del evento, el rechazo del mismo rol, el paso a estudiante sin CI, código ni teléfono (un error por campo, el rol no cambia), el paso a estudiante con los datos, y que un rechazo no emite eventos
- [x] 1.4 Agregar a `docs/use-cases/UC-001.md` la excepción de A1 "el rol nuevo es el que la cuenta ya tiene" (tabla de excepciones, columna Dónde `A1`); verificar que la tabla conserva sus columnas y que la numeración de las excepciones sigue siendo correlativa

## 2. Aplicación

- [x] 2.1 Separar `requireAffectedUser` en `requireUser` y `requirePermission`, y extraer `isOnlyActiveAdmin` en `UserManagementUseCase` (D2); verificar que las pruebas de `disableUser` y `enableUser` en `UserManagementUseCaseTest` siguen pasando sin cambios
- [x] 2.2 Crear el record `ChangeUserRoleCommand`, agregar `notifyUserRoleChanged(event)` a `UserManagementNotifier` e implementar `changeUserRole(requesterEmail, command)` según D1 y D5; verificar en `UserManagementUseCaseTest` con dobles de los puertos: éxito con `update` y el evento notificado; rol desconocido; correo sin cuenta; sin permiso sobre el rol actual (director sobre director, administrativo sobre estudiante); sin permiso sobre el rol nuevo (director da el rol de director); propia cuenta; única cuenta de administrador activa que pasa a otro rol; administrador que pasa a otro rol cuando hay otro activo; cuenta desactivada a la que se cambia el rol; y que en los rechazos no se llama a `update` ni al notifier
- [x] 2.3 Implementar `ChangeUserRoleCommandHandler` (`usuarios.cambiarRol`, 2 argumentos, 0 adjuntos, con su `CommandGuide`) y registrarlo en `UsersConfiguration` (D6); verificar con `ChangeUserRoleCommandHandlerTest` que los argumentos y el remitente llegan al caso de uso, en `UsersWiringTest` que queda registrado en el canal de correo, y en `CommandHelpUseCaseTest` que el administrador ve el comando sin acciones parciales, el director con las cuatro etiquetas de sus permisos y el administrativo no lo ve

## 3. Respuestas por correo

- [x] 3.1 Implementar en `MailUserManagementNotifier` los dos correos del cambio de rol después del commit, con los asuntos y textos de D7; verificar en `MailUserManagementNotifierTest` el destinatario, el asunto y el cuerpo de los dos correos
- [x] 3.2 Crear `docs/user-guide/usuarios.cambiarRol.md` (sintaxis, tabla de parámetros, quién puede usarlo, respuestas, rechazos y un ejemplo) y agregar su fila a la tabla de comandos de `docs/user-guide/README.md`; verificar que los textos de las respuestas coinciden con D5 y D7

## 4. Verificación integrada

- [x] 4.1 Escribir `ChangeUserRoleByEmailIntegrationTest` con GreenMail y PostgreSQL (como `ChangeUserStatusByEmailIntegrationTest`): el administrador cambia un docente a director y los dos reciben su correo, y el nuevo director puede usar un comando de director; un director intenta dar el rol de director; un administrativo intenta cambiar un rol; mismo rol; el administrador intenta cambiar su propio rol; un docente sin datos pasa a estudiante; verificar que `./gradlew build` pasa
- [x] 4.2 Probar a mano en el entorno local: con `./gradlew bootRun`, como `admin@cpms.local` en Roundcube crear un docente, cambiarlo a docente de Taller de Grado y enviar `ayuda[]` desde su buzón; verificar los correos en los dos buzones y que la ayuda muestra los comandos del rol nuevo
- [x] 4.3 Revisar la implementación contra la arquitectura de referencia de AGENTS.md (UC-001 A1 no tiene diseño en EA): paquete del caso de uso, un método público por flujo, record del comando, handler sin lógica, notifier como puerto, beans en `UsersConfiguration`, Javadoc con `UC-001` y los FR; verificar que lo creado coincide con la lista de "Diferencias con el modelo" del design

## Workflow follow-up

- Archivar el change cuando las tareas estén hechas (`/opsx:archive`) y abrir el PR de la rama hacia `develop`.
- Cambio aparte: FR-034 (advertir los trámites en curso) para A1 y A2.
