# Proposal

## Why

El usuario no tiene cómo saber qué comandos puede usar sin leer la guía de `docs/user-guide/`. La única lista que da el sistema es la de la respuesta a un comando desconocido, que muestra los 11 comandos a cualquier remitente, aunque la mayoría no estén permitidos para su rol: a un estudiante le muestra `usuarios.desactivar` y le oculta que solo puede usar `proyectos.registrarPerfil`.

## What Changes

- Nuevo comando `ayuda[]`, sin argumentos ni adjuntos. Responde por correo al remitente con los comandos que su rol le permite usar: para cada uno, su sintaxis y para qué sirve.
- En los comandos cuyo permiso depende del rol que se escribe como argumento (`usuarios.crearCuenta`, `usuarios.listar`, `usuarios.activar`, `usuarios.desactivar`), la ayuda indica qué puede hacer el remitente con ellos (por ejemplo, «crear cuentas de estudiante»).
- Si el rol del remitente no tiene comandos, además de `ayuda[]`, la respuesta lo dice (hoy es el caso del docente).
- `ayuda[]` solo se atiende para cuentas activas: un remitente sin cuenta o con la cuenta desactivada recibe el mismo rechazo que con cualquier otro comando.
- La respuesta a un comando desconocido deja de listar todos los comandos y remite a `ayuda[]`.
- Documentación: un requisito nuevo, FR-066, en `docs/REQUIREMENTS.md`; el término «Ayuda» en `docs/GLOSSARY.md`; la página `docs/user-guide/ayuda.md`, su fila en la tabla de comandos y la fila de comando desconocido en «Respuestas del sistema» de `docs/user-guide/README.md`.

Trabajo compartido, sin caso de uso: el comando sirve a todos los roles y no pertenece a ninguno de UC-001 a UC-008.

Elementos de EA: ninguno. La consulta del modelo EA está en pausa y la ayuda no tiene diseño en EA; se sigue la arquitectura de referencia de `AGENTS.md`.

## Capabilities

### New Capabilities

- `command-help`: el comando de ayuda, que responde a cada usuario con los comandos que su rol le permite usar, con su sintaxis, su propósito y, cuando corresponde, qué puede hacer con ellos.

### Modified Capabilities

- `mail-commands`: el requisito «Comando desconocido» deja de responder con la lista de comandos disponibles y remite al comando `ayuda[]`.

## Impact

- `users`: un caso de uso nuevo con su handler y su notifier; un tipo compartido con que cada handler describe su comando; los 4 handlers de UC-001 lo implementan.
- `procedures`: sus 7 handlers describen su comando con ese tipo. `procedures` ya depende de `users`, así que no aparece una dependencia nueva.
- `mail`: cambia solo el texto de la respuesta a un comando desconocido.
- Sin migraciones, sin dependencias nuevas y sin cambios en la configuración.
- Documentación: `docs/REQUIREMENTS.md`, `docs/GLOSSARY.md` y `docs/user-guide/`.
