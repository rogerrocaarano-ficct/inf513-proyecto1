# `usuarios.desactivar`: desactivar una cuenta

[Guía de comandos](README.md) › Usuarios (`usuarios`) · UC-001

Desactiva una cuenta. Una cuenta desactivada conserva sus datos y su historial, pero el sistema no atiende los comandos que llegan desde su dirección.

**Sintaxis:**

```text
usuarios.desactivar[correo]
```

**Parámetros:** 1. **Adjuntos:** ninguno. **Cuerpo:** no se lee.

| # | Parámetro | Obligatorio | Tipo | Formato / valores | Descripción |
| --- | --- | --- | --- | --- | --- |
| 1 | `correo` | Sí | Correo | No distingue mayúsculas | Correo de la cuenta que se desactiva |

**Quién puede usarlo:**

| Rol de la cuenta | La pueden desactivar |
| --- | --- |
| `estudiante`, `administrativo`, `docente`, `docente_taller` | director, administrador |
| `director`, `administrador` | administrador |

El administrativo, los docentes y los estudiantes no desactivan cuentas.

**Rechazos** (la cuenta no cambia ni recibe aviso):

| Situación | Mensaje |
| --- | --- |
| Ninguna cuenta tiene ese correo | `No existe una cuenta con el correo X.` |
| La cuenta ya está desactivada | `La cuenta X ya está desactivada.` |
| Es la única cuenta de administrador activa | `No se puede desactivar la única cuenta de administrador activa.` |
| Es la propia cuenta de quien envía el comando | `No puede desactivar su propia cuenta.` |

**Respuesta:** quien envió el comando recibe "Cuenta desactivada" con el nombre y el correo de la cuenta. La persona de la cuenta recibe un aviso con el nombre de quien la desactivó, que le indica comunicarse con la Dirección de Carrera si cree que es un error.

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
