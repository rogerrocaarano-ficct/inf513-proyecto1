# `proyectos.asignarTutor`: asignar el tutor

[Guía de comandos](README.md) › Proyectos (`proyectos`) · UC-003

Dirección designa al docente tutor del proyecto de grado.

**Sintaxis:**

```text
proyectos.asignarTutor[referencia; correoDocente]
```

**Parámetros:** 2. **Adjuntos:** ninguno. **Cuerpo:** no se lee.

| # | Parámetro | Obligatorio | Tipo | Formato / valores | Descripción |
| --- | --- | --- | --- | --- | --- |
| 1 | `referencia` | Sí | Referencia | `año-periodo-registro` | Referencia del trámite |
| 2 | `correoDocente` | Sí | Correo | Correo de una cuenta activa `docente`, `docente_taller` o `director` | Docente que será tutor del proyecto |

**Quién puede usarlo:** una cuenta `director` activa.

**Rechazos:** no existe un trámite en curso con esa referencia, el perfil vigente no fue aprobado con [`proyectos.aprobarPerfil`](proyectos.aprobarPerfil.md), el proyecto ya tiene un tutor asignado, o el correo no es de una cuenta activa que pueda ser tutor.

**Respuesta:** el estudiante recibe por correo la designación.

**Ejemplo:**

```text
proyectos.asignarTutor[2026-2-218001234; docente@cpms.local]
```
