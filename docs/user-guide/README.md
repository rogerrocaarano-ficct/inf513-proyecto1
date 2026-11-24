# Guía de comandos

El sistema se usa por correo electrónico (NFR-003): cada solicitud es un correo dirigido a la dirección del sistema, con el comando escrito en el asunto. El sistema revisa su buzón periódicamente, ejecuta el comando y responde por correo.

## Cómo escribir un comando

```text
comando[parametro1; parametro2; …]
```

- El **destinatario** es la dirección del sistema (en el entorno local, `sistema@cpms.local`).
- El **asunto** lleva el comando: su nombre y, entre corchetes, los parámetros separados por punto y coma.
- El **cuerpo** del correo solo se lee en los comandos que piden texto libre, como las observaciones de `proyectos.observarPerfil` y `proyectos.rechazarPerfil`. En los demás puede ir vacío o con cualquier texto.
- El nombre del comando **no distingue mayúsculas**: `usuarios.crearCuenta` y `USUARIOS.CREARCUENTA` son el mismo comando.
- Los parámetros **sí distinguen mayúsculas**, salvo donde la tabla del comando diga lo contrario. Los espacios al inicio y al final de cada parámetro se ignoran; los de adentro se conservan (`Ana María`).
- Un parámetro vacío se escribe dejando nada entre los punto y coma: `a; ; c`.
- Un comando sin parámetros se escribe con los corchetes vacíos: `comando[]`.
- Se puede responder a un correo del sistema para enviar otro comando: el texto que el cliente de correo agrega antes del comando (`Re:`, `RV:`, `Fwd:`) se ignora.
- Un parámetro no puede contener `;` ni `]`.
- La cantidad de parámetros y de adjuntos tiene que ser exactamente la que pide el comando.

Solo se atienden los comandos que llegan desde una dirección con una cuenta activa en el sistema. Las respuestas automáticas (fuera de oficina, rebotes, listas de correo) se ignoran.

### Cómo leer cada comando

Cada comando tiene su propio archivo en esta carpeta, enlazado desde la tabla de [Comandos](#comandos), con:

- **Sintaxis:** la forma del asunto, con el nombre de cada parámetro.
- **Parámetros:** una tabla con la posición, el nombre, si es obligatorio, el tipo de dato, el formato o los valores admitidos y su descripción.
- **Adjuntos** y **cuerpo**, cuando el comando los usa.
- **Quién puede usarlo**, la **respuesta** del sistema, los **rechazos** propios del comando y un **ejemplo**.

Tipos de dato usados en las tablas:

| Tipo | Significado |
| --- | --- |
| Texto | Cualquier texto, sin `;` ni `]` |
| Nombre | Solo letras (con tildes, ñ, ü…) y espacios |
| Dígitos | Solo dígitos del 0 al 9, sin puntos, guiones ni espacios |
| Correo | Una dirección de correo electrónico válida |
| Rol | Uno de los valores de la [tabla de roles](usuarios.crearCuenta.md) de `usuarios.crearCuenta` |
| Referencia | La referencia de un trámite: `año-periodo-registro`, por ejemplo `2026-2-218001234` |

## Respuestas del sistema

| Situación | Respuesta |
| --- | --- |
| El comando se ejecutó | La respuesta propia del comando; ver cada comando |
| El asunto no tiene la forma `comando[…]` | Recuerda la forma correcta del asunto |
| El comando no existe | «El comando "X" no existe. Envíe ayuda[] para ver los comandos que puede usar.»; no lista los comandos (ver [`ayuda`](ayuda.md)) |
| Faltan o sobran parámetros o adjuntos | Indica cuántos se esperaban y cuántos llegaron |
| Un adjunto está vacío (0 bytes) | «No se pudo procesar el correo: el adjunto "X" está vacío.»; el comando no se ejecuta |
| La dirección no tiene cuenta | Indica que la dirección no está registrada en el sistema |
| La cuenta está desactivada | «La cuenta X está desactivada. Comuníquese con la Dirección de Carrera.»; el comando no se ejecuta |
| La cuenta no tiene permiso | Indica qué no tiene permiso para hacer |
| Algún dato es inválido | Lista un error por cada dato inválido |
| Error interno | Pide intentar más tarde |

Un rechazo por datos inválidos tiene esta forma:

```text
No se pudo ejecutar el comando "usuarios.desactivar":
- correo: La cuenta luis@uagrm.bo ya está desactivada.
```

## Comandos

| Paquete | Comando | Argumentos | Adjuntos | Para qué sirve |
| --- | --- | --- | --- | --- |
| Ayuda (FR-066) | [`ayuda`](ayuda.md) | Ninguno (`ayuda[]`) | Ninguno | Ver los comandos que su rol le permite usar |
| Usuarios (UC-001) | [`usuarios.crearCuenta`](usuarios.crearCuenta.md) | `nombres; primerApellido; segundoApellido; ci; codigo; correo; telefono; rol` | Ninguno | Crear una cuenta de usuario con un rol |
| Usuarios (UC-001) | [`usuarios.listar`](usuarios.listar.md) | `rol` | Ninguno | Listar las cuentas de un rol |
| Usuarios (UC-001) | [`usuarios.desactivar`](usuarios.desactivar.md) | `correo` | Ninguno | Desactivar una cuenta |
| Usuarios (UC-001) | [`usuarios.activar`](usuarios.activar.md) | `correo` | Ninguno | Activar una cuenta desactivada |
| Usuarios (UC-001) | [`usuarios.cambiarRol`](usuarios.cambiarRol.md) | `correo; rol` | Ninguno | Cambiar el rol de una cuenta |
| Trámites (UC-002) | [`tramites.abrirTramite`](tramites.abrirTramite.md) | `registro` | 4 PDF | Abrir el trámite de titulación de un estudiante |
| Trámites (UC-002) | [`tramites.reemplazarDocumento`](tramites.reemplazarDocumento.md) | `referencia` | 1 PDF | Sustituir el documento de un requisito de apertura |
| Proyectos (UC-004) | [`proyectos.registrarPerfil`](proyectos.registrarPerfil.md) | `titulo` | 1 PDF | Presentar el perfil del proyecto o una versión nueva |
| Proyectos (UC-004) | [`proyectos.aprobarPerfil`](proyectos.aprobarPerfil.md) | `referencia` | Ninguno | Aprobar el perfil pendiente |
| Proyectos (UC-004) | [`proyectos.observarPerfil`](proyectos.observarPerfil.md) | `referencia` (observaciones en el cuerpo) | Ninguno | Pedir cambios al perfil sobre el mismo tema |
| Proyectos (UC-004) | [`proyectos.rechazarPerfil`](proyectos.rechazarPerfil.md) | `referencia` (motivo en el cuerpo) | Ninguno | Pedir que el tema del proyecto cambie |
| Proyectos (UC-003) | [`proyectos.asignarTutor`](proyectos.asignarTutor.md) | `referencia; correoDocente` | Ninguno | Designar al tutor del proyecto |
