# `usuarios.crearCuenta`: crear una cuenta

[Guía de comandos](README.md) › Usuarios (`usuarios`) · UC-001

Crea la cuenta de una persona con un rol.

**Sintaxis:**

```text
usuarios.crearCuenta[nombres; primerApellido; segundoApellido; ci; codigo; correo; telefono; rol]
```

**Parámetros:** 8. **Adjuntos:** ninguno. **Cuerpo:** no se lee.

| # | Parámetro | Obligatorio | Tipo | Formato / valores | Descripción |
| --- | --- | --- | --- | --- | --- |
| 1 | `nombres` | Sí | Nombre | Letras y espacios | Nombre o nombres de la persona |
| 2 | `primerApellido` | Sí | Nombre | Letras y espacios | Primer apellido |
| 3 | `segundoApellido` | No | Nombre | Letras y espacios | Segundo apellido; vacío si no tiene |
| 4 | `ci` | Solo para estudiantes | Dígitos | Hasta 7, sin complemento | Carnet de identidad |
| 5 | `codigo` | Solo para estudiantes | Dígitos | Hasta 10 | Registro universitario del estudiante, o código administrativo o de docente. No puede pertenecer a otra cuenta |
| 6 | `correo` | Sí | Correo | Dirección válida | Dirección desde la que la persona enviará sus comandos. No puede pertenecer a otra cuenta |
| 7 | `telefono` | Solo para estudiantes | Dígitos | Hasta 8 | Teléfono de contacto |
| 8 | `rol` | Sí | Rol | Ver la tabla siguiente; no distingue mayúsculas | Rol de la cuenta nueva |

Los nombres y apellidos se guardan con un solo espacio entre palabras y cada palabra con mayúscula inicial: `maría   de los ÁNGELES` queda `María De Los Ángeles`.

**Roles y quién puede crearlos:**

| Rol (como se escribe) | Descripción | Lo pueden crear |
| --- | --- | --- |
| `estudiante` | Estudiante | administrativo, director, administrador |
| `administrativo` | Personal de Secretaría | director, administrador |
| `docente` | Docente | director, administrador |
| `docente_taller` | Docente de Taller de Grado | director, administrador |
| `director` | Director de carrera | administrador |
| `administrador` | Administrador del sistema | administrador |

**Respuesta:** quien envió el comando recibe "Cuenta creada" con el nombre, el correo y el rol de la cuenta nueva. La persona de la cuenta nueva recibe un aviso en su correo con su rol.

**Ejemplos:**

```text
usuarios.crearCuenta[Ana María; Pérez; Rojas; 1234567; 218001234; ana@uagrm.bo; 71234567; estudiante]
usuarios.crearCuenta[Luis; Rojas; ; ; ; luis@uagrm.bo; ; docente]
usuarios.crearCuenta[Carla; Suárez; Vaca; ; 100; carla@uagrm.bo; ; director]
```
