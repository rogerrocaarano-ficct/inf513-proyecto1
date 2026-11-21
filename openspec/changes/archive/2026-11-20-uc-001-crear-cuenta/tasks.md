# Tasks

## 1. Documentos

- [x] 1.1 Actualizar REQUIREMENTS.md: FR-026 (un solo rol), FR-036 (un solo rol por cuenta en lugar de las combinaciones), FR-050 (el director también crea estudiantes y administrativos), FR-051 (la cuenta del estudiante también se crea en UC-001) y sus notas (D7); verificar que las tablas conservan sus columnas y que ningún otro FR contradice "un solo rol"
- [x] 1.2 Completar en GLOSSARY.md el término adoptado en el código de "Cuenta de usuario" (`User`), "Rol" (`Role`), "Registro universitario" (`uagrmCode`) y "Cédula de identidad" (`ci`); verificar que las filas conservan las tres columnas

## 2. Dominio de usuarios

- [x] 2.1 Implementar los enums `Role` (con etiqueta, `fromLabel`, `isAllowedTo`, `getPermissions`) y `RolePermission` (con `allowedRoles` e `isGrantedTo`) en `users.domain`, según D2 y las notas de EA; verificar con pruebas unitarias la tabla de permisos completa de la spec "Quién puede crear cada cuenta" y `fromLabel` sin distinguir mayúsculas y con una etiqueta desconocida
- [x] 2.2 Implementar `UserSignedUpEvent` y `UserNotAuthorizedException` en `users.domain`; verificar que la excepción es una `DomainException` con su mensaje en español
- [x] 2.3 Implementar el agregado `User` (builder, `rehydrate`, `with*`, getters, `hasPermissionTo`, `pullEvents`) con el correo en minúsculas; verificar con pruebas unitarias la construcción y la rehidratación sin eventos
- [x] 2.4 Implementar `User.signUp(requester)` con el permiso, la normalización y las validaciones de D3 y el evento; verificar con pruebas unitarias los escenarios de "Datos obligatorios y formato", "Datos exigidos al estudiante" y "Normalización de nombres", que un solicitante sin permiso recibe `UserNotAuthorizedException` antes de cualquier error de datos, y que se emite un solo `UserSignedUpEvent`

## 3. Aplicación

- [x] 3.1 Declarar el puerto `UserRepository` (`insert`, `update`, `getByEmail`, `getById`) y `UserManagementEventBus` (`notifyUserSignedUpEvent`); verificar que `./gradlew compileJava` pasa
- [x] 3.2 Implementar `SignUpUserCommand` (record) y `UserManagementUseCase.signUpUser` según D1; verificar con dobles de los puertos: remitente sin cuenta (E1), rol desconocido, cuenta creada que se inserta y publica un solo evento, y duplicado que propaga la `DomainValidationException` del repositorio sin publicar el evento
- [x] 3.3 Implementar `SignUpUserCommandHandler` (`usuarios.crearCuenta`, 8 argumentos, 0 adjuntos) que arma el comando en el orden de la nota de `SignUpUserCommand`; verificar con una prueba unitaria que cada argumento llega a su campo y que el solicitante es el remitente

## 4. Infraestructura

- [x] 4.1 Escribir la migración `V2__user_account.sql` (D4) y el adaptador `JpaUserRepository` con su entidad y mapeador; verificar con pruebas de integración (Testcontainers) `insert`, `getByEmail` sin distinguir mayúsculas, `getById`, y el rechazo de correo o código repetidos con un error por dato, incluidas varias cuentas sin código
- [x] 4.2 Implementar `MailUserManagementEventBus` con el envío después del commit (D5) y la configuración Spring de `users` (`UserManagementUseCase`, handler); verificar que la aplicación arranca y que el handler queda registrado en el canal de correo
- [x] 4.3 Implementar `InitialAdminAccount` y la propiedad `cpms.users.admin-email` (`CPMS_ADMIN_EMAIL`, y `admin@cpms.local` en el perfil `local`) según D6; verificar con pruebas los escenarios de "Administrador inicial"

## 5. Verificación integrada y documentación

- [x] 5.1 Escribir una prueba de extremo a extremo con GreenMail y PostgreSQL: el administrador crea un director, el director crea un docente y un estudiante, un docente intenta crear una cuenta, un remitente desconocido, un dato inválido y un correo repetido; verificar las respuestas, los avisos a las cuentas nuevas (solo cuando se crean) y las filas de `user_account`, y que `./gradlew build` pasa
- [x] 5.2 Probar a mano en el entorno local: con `./gradlew bootRun`, entrar a Roundcube como `admin@cpms.local`, crear una cuenta de director y, como ese director, una de docente; verificar las respuestas y los avisos en los buzones
- [x] 5.3 Agregar al README.md, en "Entorno de desarrollo", la cuenta `admin@cpms.local` y el comando `usuarios.crearCuenta` de ejemplo; verificar siguiendo los pasos tal como quedaron escritos
- [x] 5.5 Crear USER-GUIDE.md con la forma de los comandos, las respuestas del sistema y `usuarios.crearCuenta` (argumentos en orden, obligatoriedad, formato, roles y quién los crea, ejemplos), y enlazarla desde README.md y AGENTS.md; verificar que el README ya no repite la tabla de comandos
- [x] 5.4 Revisar la implementación contra los elementos de EA citados en el design (`Design/Users/domain`, `Design/Users/application`, diagrama "Users"); verificar con la skill `ea-drift users` que las únicas diferencias son las registradas en "Diferencias con el modelo"

## Workflow follow-up

- Archivar el change cuando las tareas estén hechas (`/opsx:archive`) e integrar la rama `UC-001-gestion-de-usuarios` en `develop`.
