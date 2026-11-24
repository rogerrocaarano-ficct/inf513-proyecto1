# `usuarios.cambiarRol`: cambiar el rol de una cuenta

[Guía de comandos](README.md) › Usuarios (`usuarios`) · UC-001

Cambia el rol de una cuenta. La cuenta conserva sus datos y su estado, y sigue teniendo un solo rol.

**Sintaxis:**

```text
usuarios.cambiarRol[correo; rol]
```

**Parámetros:** 2. **Adjuntos:** ninguno. **Cuerpo:** no se lee.

| # | Parámetro | Obligatorio | Tipo | Formato / valores | Descripción |
| --- | --- | --- | --- | --- | --- |
| 1 | `correo` | Sí | Correo | No distingue mayúsculas | Correo de la cuenta cuyo rol cambia |
| 2 | `rol` | Sí | Texto | Uno de los valores de la [tabla de roles](usuarios.crearCuenta.md) de `usuarios.crearCuenta`; no distingue mayúsculas | Rol nuevo de la cuenta |

**Quién puede usarlo:** hace falta poder cambiar el rol de las cuentas del rol actual **y** del rol nuevo.

| Rol actual o nuevo | Pueden cambiarlo |
| --- | --- |
| `estudiante`, `administrativo`, `docente`, `docente_taller` | director, administrador |
| `director`, `administrador` | administrador |

Por ejemplo, un director puede pasar un docente a `docente_taller`, pero no a `director`. El administrativo, los docentes y los estudiantes no cambian roles.

**Rechazos** (la cuenta no cambia ni recibe aviso):

| Situación | Mensaje |
| --- | --- |
| El rol no existe | `"X" no es un rol válido. Roles aceptados: …` |
| Ninguna cuenta tiene ese correo | `No existe una cuenta con el correo X.` |
| Es la única cuenta de administrador activa y deja de ser administrador | `No se puede quitar el rol de administrador a la única cuenta de administrador activa.` |
| Es la propia cuenta de quien envía el comando | `No puede cambiar el rol de su propia cuenta.` |
| La cuenta ya tiene ese rol | `La cuenta X ya tiene el rol Y.` |
| Pasa a `estudiante` sin CI, código o teléfono | `Es obligatorio para estudiantes.`, en cada dato que falta |

Una cuenta que debe pasar a `estudiante` sin esos datos no puede cambiar de rol: hay que crear la cuenta de estudiante con [`usuarios.crearCuenta`](usuarios.crearCuenta.md).

**Respuesta:** quien envió el comando recibe "Rol cambiado" con el nombre y el correo de la cuenta, el rol anterior y el nuevo. La persona de la cuenta recibe un aviso con el nombre de quien cambió su rol, el rol anterior y el nuevo.

**Ejemplo:** `usuarios.cambiarRol[luis@uagrm.bo; director]` enviado por el administrador Ana Rojas responde:

```text
Asunto: Rol cambiado: Luis Rojas

Se cambió el rol de la cuenta de Luis Rojas (luis@uagrm.bo) de docente a director.
```

Y `luis@uagrm.bo` recibe:

```text
Asunto: Su rol en el sistema de trámites de titulación cambió

Hola, Luis Rojas:

Ana Rojas cambió el rol de su cuenta de docente a director.

Desde ahora puede usar los comandos de ese rol; envíe ayuda[] para verlos. Si cree que es un error, comuníquese con la Dirección de Carrera.
```

Un docente sin datos de estudiante que se quiere pasar a `estudiante`:

```text
No se pudo ejecutar el comando "usuarios.cambiarrol":
- CI: Es obligatorio para estudiantes.
- código: Es obligatorio para estudiantes.
- teléfono: Es obligatorio para estudiantes.
```
