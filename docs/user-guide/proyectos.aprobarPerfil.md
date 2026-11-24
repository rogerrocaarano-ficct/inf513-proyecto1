# `proyectos.aprobarPerfil`: aprobar un perfil

[Guía de comandos](README.md) › Proyectos (`proyectos`) · UC-004

El docente de Taller de Grado aprueba el perfil pendiente de un trámite. Desde ese momento Dirección puede asignar el tutor.

**Sintaxis:**

```text
proyectos.aprobarPerfil[referencia]
```

**Parámetros:** 1. **Adjuntos:** ninguno. **Cuerpo:** no se lee.

| # | Parámetro | Obligatorio | Tipo | Formato / valores | Descripción |
| --- | --- | --- | --- | --- | --- |
| 1 | `referencia` | Sí | Referencia | `año-periodo-registro` | Referencia del trámite cuyo perfil se aprueba |

**Quién puede usarlo:** una cuenta `docente_taller` activa.

**Rechazos:** no existe un trámite en curso con esa referencia, o su perfil no está pendiente de evaluación.

**Respuesta:** el estudiante y el director de carrera reciben el aviso del perfil aprobado. Si no hay un director activo, el perfil queda aprobado igual.

**Ejemplo:**

```text
proyectos.aprobarPerfil[2026-2-218001234]
```
