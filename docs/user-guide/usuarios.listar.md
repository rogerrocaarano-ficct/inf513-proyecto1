# `usuarios.listar`: listar las cuentas de un rol

[Guía de comandos](README.md) › Usuarios (`usuarios`) · UC-001

Envía la lista de las cuentas que tienen un rol.

**Sintaxis:**

```text
usuarios.listar[rol]
```

**Parámetros:** 1. **Adjuntos:** ninguno. **Cuerpo:** no se lee.

| # | Parámetro | Obligatorio | Tipo | Formato / valores | Descripción |
| --- | --- | --- | --- | --- | --- |
| 1 | `rol` | Sí | Rol | Como en [`usuarios.crearCuenta`](usuarios.crearCuenta.md); no distingue mayúsculas | Rol de las cuentas que se listan |

`usuarios.listar[]` se rechaza porque falta el parámetro.

**Quién puede usarlo:** los mismos que pueden crear cuentas de ese rol. El administrador lista todos los roles; el director, `docente`, `docente_taller`, `administrativo` y `estudiante`; el administrativo, `estudiante`. Docentes, docente de Taller de Grado y estudiantes no listan cuentas.

**Respuesta:** solo quien envió el comando recibe la lista; las cuentas listadas no reciben ningún correo. El asunto indica el rol y la cantidad de cuentas. Cada cuenta muestra su nombre completo, correo, CI, código, teléfono y estado (`activa` o `desactivada`), con `-` en los datos que no tiene. La lista incluye las cuentas desactivadas y va ordenada por primer apellido, segundo apellido y nombres. El correo es el dato que identifica a la cuenta en los demás comandos. Si no hay cuentas con ese rol, la respuesta lo dice; no es un error.

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
