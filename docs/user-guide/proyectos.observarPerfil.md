# `proyectos.observarPerfil`: observar un perfil

[Guía de comandos](README.md) › Proyectos (`proyectos`) · UC-004

El docente de Taller de Grado pide cambios sobre el mismo tema, por ejemplo de forma.

**Sintaxis:**

```text
proyectos.observarPerfil[referencia]
```

**Parámetros:** 1. **Adjuntos:** ninguno. **Cuerpo:** obligatorio.

| # | Parámetro | Obligatorio | Tipo | Formato / valores | Descripción |
| --- | --- | --- | --- | --- | --- |
| 1 | `referencia` | Sí | Referencia | `año-periodo-registro` | Referencia del trámite cuyo perfil se observa |

**Cuerpo:** las observaciones, en texto libre; no puede ir vacío. El sistema guarda el cuerpo tal como llega, incluidas la firma o el mensaje citado si se dejan, así que conviene escribir solo las observaciones.

**Quién puede usarlo:** una cuenta `docente_taller` activa.

**Rechazos:** no existe un trámite en curso con esa referencia, su perfil no está pendiente de evaluación, o el cuerpo está vacío (`Escriba las observaciones en el cuerpo del correo.`).

**Respuesta:** el estudiante recibe las observaciones y puede presentar una versión nueva con [`proyectos.registrarPerfil`](proyectos.registrarPerfil.md).

**Ejemplo:**

```text
Asunto: proyectos.observarPerfil[2026-2-218001234]
Cuerpo: Corregir el formato de la bibliografía.
```
