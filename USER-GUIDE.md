# Guía de comandos

El sistema se usa por correo electrónico (NFR-003): cada solicitud es un correo dirigido a la dirección del sistema, con el comando escrito en el asunto. El sistema revisa su buzón periódicamente, ejecuta el comando y responde por correo.

## Cómo escribir un comando

```text
comando[argumento1; argumento2; …]
```

- El **destinatario** es la dirección del sistema (en el entorno local, `sistema@cpms.local`).
- El **asunto** lleva el comando: su nombre y, entre corchetes, los argumentos separados por punto y coma.
- El **cuerpo** del correo no se lee: puede ir vacío o con cualquier texto.
- El nombre del comando **no distingue mayúsculas**: `usuarios.crearCuenta` y `USUARIOS.CREARCUENTA` son el mismo comando.
- Los argumentos **sí distinguen mayúsculas**. Los espacios al inicio y al final de cada argumento se ignoran; los de adentro se conservan (`Ana María`).
- Un argumento vacío se escribe dejando nada entre los punto y coma: `a; ; c`.
- Un comando sin argumentos se escribe con los corchetes vacíos: `comando[]`.
- Se puede responder a un correo del sistema para enviar otro comando: el texto que el cliente de correo agrega antes del comando (`Re:`, `RV:`, `Fwd:`) se ignora.
- Un argumento no puede contener `;` ni `]`.
- La cantidad de argumentos tiene que ser exactamente la que pide el comando.

Solo se atienden los comandos que llegan desde una dirección con cuenta en el sistema. Las respuestas automáticas (fuera de oficina, rebotes, listas de correo) se ignoran.

## Respuestas del sistema

| Situación | Respuesta |
| --- | --- |
| El comando se ejecutó | La respuesta propia del comando; ver cada comando |
| El asunto no tiene la forma `comando[…]` | Recuerda la forma correcta del asunto |
| El comando no existe | Indica que no existe y lista los comandos disponibles |
| Faltan o sobran argumentos o adjuntos | Indica cuántos se esperaban y cuántos llegaron |
| La dirección no tiene cuenta | Indica que la dirección no está registrada en el sistema |
| La cuenta no tiene permiso | Indica qué no tiene permiso para hacer |
| Algún dato es inválido | Lista un error por cada dato inválido |
| Error interno | Pide intentar más tarde |

## Comandos

### `usuarios.crearCuenta`: crear una cuenta de usuario

Crea la cuenta de una persona con un rol (UC-001). Recibe 8 argumentos y ningún adjunto.

```text
usuarios.crearCuenta[nombres; primer apellido; segundo apellido; CI; código; correo; teléfono; rol]
```

| # | Argumento | Obligatorio | Formato |
| --- | --- | --- | --- |
| 1 | Nombres | Sí | Solo letras (con tildes, ñ, ü…) y espacios |
| 2 | Primer apellido | Sí | Solo letras y espacios |
| 3 | Segundo apellido | No | Solo letras y espacios |
| 4 | CI (carnet de identidad) | Solo para estudiantes | Solo dígitos, hasta 7, sin complemento |
| 5 | Código | Solo para estudiantes | Solo dígitos, hasta 10: número de registro del estudiante, o código administrativo o de docente |
| 6 | Correo | Sí | Dirección de correo válida; la persona enviará sus comandos desde ella |
| 7 | Teléfono | Solo para estudiantes | Solo dígitos, hasta 8 |
| 8 | Rol | Sí | Uno de los roles de la tabla siguiente |

Los nombres y apellidos se guardan con un solo espacio entre palabras y cada palabra con mayúscula inicial: `maría   de los ÁNGELES` queda `María De Los Ángeles`.

El correo y el código no pueden pertenecer a otra cuenta.

**Roles y quién puede crearlos:**

| Rol (como se escribe) | Lo pueden crear |
| --- | --- |
| `estudiante` | administrativo, director, administrador |
| `administrativo` | director, administrador |
| `docente` | director, administrador |
| `docente_taller` (docente de Taller de Grado) | director, administrador |
| `director` | administrador |
| `administrador` | administrador |

El rol no distingue mayúsculas: `Docente` y `docente` son el mismo rol.

**Respuesta:** quien envió el comando recibe "Cuenta creada" con el nombre, el correo y el rol de la cuenta nueva. La persona de la cuenta nueva recibe un aviso en su correo con su rol.

**Ejemplos:**

```text
usuarios.crearCuenta[Ana María; Pérez; Rojas; 1234567; 218001234; ana@uagrm.bo; 71234567; estudiante]
usuarios.crearCuenta[Luis; Rojas; ; ; ; luis@uagrm.bo; ; docente]
usuarios.crearCuenta[Carla; Suárez; Vaca; ; 100; carla@uagrm.bo; ; director]
```

### `usuarios.listar`: listar las cuentas de un rol

Envía la lista de las cuentas que tienen un rol (UC-001). Recibe 1 argumento y ningún adjunto.

```text
usuarios.listar[rol]
```

El rol es obligatorio y se escribe como en `usuarios.crearCuenta`, sin distinguir mayúsculas. `usuarios.listar[]` se rechaza porque falta el argumento.

**Quién puede listar cada rol:** los mismos que pueden crear cuentas de ese rol (ver la tabla de `usuarios.crearCuenta`). El administrador lista todos los roles; el director, `docente`, `docente_taller`, `administrativo` y `estudiante`; el administrativo, `estudiante`. Docentes, docente de Taller de Grado y estudiantes no listan cuentas.

**Respuesta:** solo quien envió el comando recibe la lista; las cuentas listadas no reciben ningún correo. El asunto indica el rol y la cantidad de cuentas. Cada cuenta muestra su nombre completo, correo, CI, código y teléfono, con `-` en los datos que no tiene; las cuentas van ordenadas por primer apellido, segundo apellido y nombres. El correo es el dato que identifica a la cuenta en los demás comandos. Si no hay cuentas con ese rol, la respuesta lo dice; no es un error.

**Ejemplo:** `usuarios.listar[estudiante]` responde:

```text
Asunto: Cuentas de usuario: estudiante (2)

Cuentas con rol estudiante:

1. Beto Arce
   Correo: beto@uagrm.bo
   CI: 7654321 | Código: 218005678 | Teléfono: 70000000

2. Ana María Pérez Rojas
   Correo: ana@uagrm.bo
   CI: 1234567 | Código: 218001234 | Teléfono: 71234567
```

Sin cuentas del rol, `usuarios.listar[docente_taller]` responde con el asunto `Cuentas de usuario: docente_taller (0)` y el texto `No hay cuentas con rol docente_taller.`
