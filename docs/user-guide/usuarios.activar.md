# `usuarios.activar`: activar una cuenta

[Guía de comandos](README.md) › Usuarios (`usuarios`) · UC-001

Activa de nuevo una cuenta desactivada. Una cuenta nueva siempre está activa.

**Sintaxis:**

```text
usuarios.activar[correo]
```

**Parámetros:** 1. **Adjuntos:** ninguno. **Cuerpo:** no se lee.

| # | Parámetro | Obligatorio | Tipo | Formato / valores | Descripción |
| --- | --- | --- | --- | --- | --- |
| 1 | `correo` | Sí | Correo | No distingue mayúsculas | Correo de la cuenta que se activa |

**Quién puede usarlo:** los mismos que pueden desactivar la cuenta (ver [`usuarios.desactivar`](usuarios.desactivar.md)).

**Rechazos** (la cuenta no cambia ni recibe aviso):

| Situación | Mensaje |
| --- | --- |
| Ninguna cuenta tiene ese correo | `No existe una cuenta con el correo X.` |
| La cuenta ya está activa | `La cuenta X ya está activa.` |

**Respuesta:** quien envió el comando recibe "Cuenta activada" con el nombre y el correo de la cuenta. La persona de la cuenta recibe un aviso con el nombre de quien la activó.

**Ejemplo:**

```text
usuarios.activar[luis@uagrm.bo]
```
