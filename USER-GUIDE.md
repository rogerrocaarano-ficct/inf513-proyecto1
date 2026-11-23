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

Solo se atienden los comandos que llegan desde una dirección con una cuenta activa en el sistema. Las respuestas automáticas (fuera de oficina, rebotes, listas de correo) se ignoran.

## Respuestas del sistema

| Situación | Respuesta |
| --- | --- |
| El comando se ejecutó | La respuesta propia del comando; ver cada comando |
| El asunto no tiene la forma `comando[…]` | Recuerda la forma correcta del asunto |
| El comando no existe | Indica que no existe y lista los comandos disponibles |
| Faltan o sobran argumentos o adjuntos | Indica cuántos se esperaban y cuántos llegaron |
| Un adjunto está vacío (0 bytes) | «No se pudo procesar el correo: el adjunto "X" está vacío.»; el comando no se ejecuta |
| La dirección no tiene cuenta | Indica que la dirección no está registrada en el sistema |
| La cuenta está desactivada | «La cuenta X está desactivada. Comuníquese con la Dirección de Carrera.»; el comando no se ejecuta |
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

**Respuesta:** solo quien envió el comando recibe la lista; las cuentas listadas no reciben ningún correo. El asunto indica el rol y la cantidad de cuentas. Cada cuenta muestra su nombre completo, correo, CI, código, teléfono y estado (`activa` o `desactivada`), con `-` en los datos que no tiene; la lista incluye las cuentas desactivadas, y van ordenadas por primer apellido, segundo apellido y nombres. El correo es el dato que identifica a la cuenta en los demás comandos. Si no hay cuentas con ese rol, la respuesta lo dice; no es un error.

**Ejemplo:** `usuarios.listar[estudiante]` responde:

```text
Asunto: Cuentas de usuario: estudiante (2)

Cuentas con rol estudiante:

1. Beto Arce
   Correo: beto@uagrm.bo
   CI: 7654321 | Código: 218005678 | Teléfono: 70000000
   Estado: desactivada

2. Ana María Pérez Rojas
   Correo: ana@uagrm.bo
   CI: 1234567 | Código: 218001234 | Teléfono: 71234567
   Estado: activa
```

Sin cuentas del rol, `usuarios.listar[docente_taller]` responde con el asunto `Cuentas de usuario: docente_taller (0)` y el texto `No hay cuentas con rol docente_taller.`

### `usuarios.desactivar` y `usuarios.activar`: desactivar y activar una cuenta

Desactiva una cuenta o la activa de nuevo (UC-001). Cada uno recibe 1 argumento, el correo de la cuenta, y ningún adjunto. El correo no distingue mayúsculas.

```text
usuarios.desactivar[correo]
usuarios.activar[correo]
```

Una cuenta desactivada conserva sus datos, pero el sistema no atiende los comandos que llegan desde su dirección. Una cuenta nueva siempre está activa.

**Quién puede desactivar y activar cada cuenta:**

| Rol de la cuenta | Lo pueden desactivar y activar |
| --- | --- |
| `estudiante`, `administrativo`, `docente`, `docente_taller` | director, administrador |
| `director`, `administrador` | administrador |

El administrativo, los docentes y los estudiantes no desactivan ni activan cuentas.

**Se rechaza, sin cambiar la cuenta:**

- Si ninguna cuenta tiene ese correo: `No existe una cuenta con el correo X.`
- Si la cuenta ya está desactivada (al desactivar) o activa (al activar): `La cuenta X ya está desactivada.` o `La cuenta X ya está activa.` La cuenta no recibe ningún aviso.
- Si es la única cuenta de administrador activa: `No se puede desactivar la única cuenta de administrador activa.`
- Si es la propia cuenta de quien envía el comando: `No puede desactivar su propia cuenta.`

**Respuesta:** quien envió el comando recibe "Cuenta desactivada" o "Cuenta activada" con el nombre y el correo de la cuenta. La persona de la cuenta recibe un aviso con el nombre de quien hizo el cambio; al desactivarla, el aviso le indica que se comunique con la Dirección de Carrera si cree que es un error.

**Ejemplo:** `usuarios.desactivar[luis@uagrm.bo]` enviado por la directora Carla Suarez responde:

```text
Asunto: Cuenta desactivada: Luis Rojas

Se desactivó la cuenta de Luis Rojas (luis@uagrm.bo).

Esa persona ya no puede enviar comandos al sistema; su historial se conserva.
```

Y `luis@uagrm.bo` recibe:

```text
Asunto: Su cuenta en el sistema de trámites de titulación fue desactivada

Hola, Luis Rojas:

Carla Suarez desactivó su cuenta en el sistema de trámites de titulación.

Desde ahora el sistema no atenderá los comandos que envíe desde esta dirección. Si cree que es un error, comuníquese con la Dirección de Carrera.
```

Un rechazo se ve así:

```text
No se pudo ejecutar el comando "usuarios.desactivar":
- correo: La cuenta luis@uagrm.bo ya está desactivada.
```

### `tramites.abrirTramite`

Secretaría abre el trámite de titulación desde una cuenta administrativa activa (UC-002). El trámite se abre sobre la cuenta de estudiante: si el estudiante todavía no tiene cuenta, créala antes con `usuarios.crearCuenta` y el rol `estudiante`.

```text
tramites.abrirTramite[218001234]
```

El único argumento es el registro universitario del estudiante.

Adjunta un PDF por cada requisito de apertura: la carta de solicitud, la hoja de vida, la copia de la cédula y la boleta de inscripción con Modalidad de Graduación. Cada PDF puede pesar hasta 25 MB. Usa las palabras `carta`, `cv`, `ci` y `boleta` en los nombres, por ejemplo `carta_solicitud.pdf`, `cv.pdf`, `ci.pdf` y `boleta.pdf`. El escaneo de la carta física es suficiente.

El sistema rechaza la apertura si ninguna cuenta tiene ese registro, si la cuenta no es de estudiante o está desactivada, si el estudiante ya tiene un trámite en curso o si ya tuvo un trámite en el semestre vigente, aunque esté cerrado.

Secretaría recibe la referencia del trámite, formada por el semestre vigente y el registro, por ejemplo `2026-2-218001234`. El estudiante recibe el aviso de apertura con la misma referencia.

### `tramites.reemplazarDocumento`

Secretaría puede sustituir el documento de un requisito de apertura mientras el trámite esté en curso. Usa la referencia recibida al abrirlo y adjunta un PDF con la palabra que identifica el requisito.

```text
tramites.reemplazarDocumento[2026-2-218001234]
```

Por ejemplo, adjunta `carta_corregida.pdf` para sustituir la carta. Secretaría recibe una confirmación que identifica el trámite y el requisito sustituido. El documento anterior se conserva como versión anterior.

### `proyectos.registrarPerfil`

Desde tu cuenta de estudiante activa, envía el título como único argumento y adjunta un PDF de hasta 25 MB.

```text
proyectos.registrarPerfil[Redes comunitarias]
```

Necesitas un trámite en curso y esta debe ser tu primera entrega. El sistema registra el proyecto y envía el perfil al docente de Taller de Grado. Si no hay un docente de Taller activo, recibirás un rechazo y podrás reintentar cuando haya uno.

### `proyectos.asignarTutor`

Desde la cuenta activa de Dirección, indica la referencia del trámite y el correo del docente activo. Envía el correo sin adjuntos.

```text
proyectos.asignarTutor[2026-2-218001234; docente@cpms.local]
```

El perfil requiere el visto bueno de Taller de Grado y el proyecto debe estar sin tutor. El estudiante recibe por correo la designación. El visto bueno se registra mediante UC-004.
