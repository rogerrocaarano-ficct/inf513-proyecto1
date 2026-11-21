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
