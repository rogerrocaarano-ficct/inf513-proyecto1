# Tasks

## 1. Requisito y glosario

- [x] 1.1 Agregar FR-066 a `docs/REQUIREMENTS.md`: «El sistema debe responder por correo, a quien lo solicite, los comandos que su rol le permite usar, con su sintaxis y para qué sirve cada uno». Verificar que la tabla conserva sus columnas
- [x] 1.2 Agregar a `docs/GLOSSARY.md` los términos «Ayuda» (`CommandHelpUseCase`, comando `ayuda`) y «Guía de comando» (`CommandGuide`), y verificar que la tabla conserva sus columnas

## 2. Descripción de cada comando

- [x] 2.1 Crear en `users.application` el record `CommandGuide` y la interfaz `DocumentedCommand`, con Javadoc que cite FR-066, y un `CommandGuideTest` que compruebe que `permissions` no admite `null` y queda inmodificable
- [x] 2.2 Hacer que `SignUpUserCommandHandler`, `ListUsersQueryHandler`, `DisableUserCommandHandler` y `EnableUserCommandHandler` implementen `DocumentedCommand` con una constante `GUIDE` (permisos armados con `toSignUp`, `toList` y `toChangeStatus` sobre todos los `Role`). Verificar con los tests de cada handler que `guide().action()` es `ACTION` y que `permissions` contiene un permiso por rol
- [x] 2.3 Hacer que los 7 handlers de `procedures` implementen `DocumentedCommand` con su `GUIDE` (sintaxis, propósito, adjuntos o cuerpo, y permiso según el design), y verificar con sus tests que `guide().action()` es `ACTION`

## 3. Caso de uso de ayuda

- [x] 3.1 Crear `users.application.commandhelp` con `CommandHelpUseCase` (`showHelp`, `HELP_GUIDE`), `ShowHelpQueryResult`, `ShowHelpQueryItem` y `CommandHelpNotifier`. Verificar con `CommandHelpUseCaseTest`, usando dobles de `UserRepository`, de los comandos y del notifier: estudiante, director, administrativo (permiso parcial con su `label`), administrador (sin aclaración), docente (solo `ayuda`), orden por nombre, remitente sin cuenta y cuenta desactivada (con el mismo rechazo y sin llamar al notifier)
- [x] 3.2 Crear `ShowHelpQueryHandler` (`ACTION = "ayuda"`, 0 argumentos, 0 adjuntos) y verificar con `ShowHelpQueryHandlerTest` que llama a `showHelp` con el remitente
- [x] 3.3 Crear `users.infrastructure.MailCommandHelpNotifier` (envío `AFTER_COMMIT`, falla registrada sin relanzar) con el formato del design, incluido el aviso de rol sin comandos. Verificar con `MailCommandHelpNotifierTest` el destinatario, el cuerpo con permisos parciales y adjuntos, y el aviso
- [x] 3.4 Declarar `CommandHelpUseCase` y `ShowHelpQueryHandler` en `UsersConfiguration` y extender `UsersWiringTest` para comprobar que el contexto arranca sin dependencias circulares y que todo `EmailCommandHandler`, salvo `ShowHelpQueryHandler`, es un `DocumentedCommand` cuyo `guide().action()` coincide con `actionKey()` y cuya `syntax` tiene `requiredArgs()` argumentos
- [x] 3.5 Agregar `HelpByEmailIntegrationTest` (GreenMail): un estudiante y un administrativo envían `ayuda[]` y reciben solo sus comandos; un docente recibe el aviso de rol sin comandos; una dirección sin cuenta recibe el rechazo de no registrada; `ayuda[x]` recibe el error de cantidad de argumentos
- [x] 3.6 Crear `docs/user-guide/ayuda.md` con el formato de las demás páginas (sintaxis, parámetros, quién puede usarlo, respuesta, rechazos y ejemplo), agregar su fila a la tabla de comandos de `docs/user-guide/README.md`, y verificar que el ejemplo coincide con la respuesta del test de integración

## 4. Comando desconocido

- [x] 4.1 Cambiar `InboxPollerService.unknownCommandMessage` para que remita a `ayuda[]` sin listar comandos, y actualizar sus tests unitarios y el de integración de comando desconocido para que comprueben el texto nuevo y que la respuesta no contiene ningún nombre de comando del sistema
- [x] 4.2 Actualizar la fila «El comando no existe» de «Respuestas del sistema» en `docs/user-guide/README.md` y verificar que coincide con el texto de 4.1

## 5. Verificación

- [x] 5.1 Ejecutar `./gradlew build` con Docker activo y verificar que pasan todos los tests
- [x] 5.2 Comprobar la implementación contra la arquitectura de referencia de `AGENTS.md` (sin diseño en EA): `mail` no importa nada de `users`, el caso de uso no usa Spring, el notifier envía solo después del commit, no hay SQL ni dependencias nuevas, y los nombres coinciden con «Diferencias con el modelo» del design

## Workflow follow-up

- Crear la rama `comando-ayuda` desde `develop` antes de aplicar.
- Archivar el cambio cuando esté integrado en `develop` y los tests pasen.
