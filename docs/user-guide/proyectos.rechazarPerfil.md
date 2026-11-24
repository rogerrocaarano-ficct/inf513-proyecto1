# `proyectos.rechazarPerfil`: rechazar un perfil

[Guía de comandos](README.md) › Proyectos (`proyectos`) · UC-004

El docente de Taller de Grado pide que el tema del proyecto cambie. Funciona igual que [`proyectos.observarPerfil`](proyectos.observarPerfil.md).

**Sintaxis:**

```text
proyectos.rechazarPerfil[referencia]
```

**Parámetros:** 1. **Adjuntos:** ninguno. **Cuerpo:** obligatorio.

| # | Parámetro | Obligatorio | Tipo | Formato / valores | Descripción |
| --- | --- | --- | --- | --- | --- |
| 1 | `referencia` | Sí | Referencia | `año-periodo-registro` | Referencia del trámite cuyo perfil se rechaza |

**Cuerpo:** el motivo del rechazo, en texto libre; no puede ir vacío. Se guarda tal como llega.

**Quién puede usarlo:** una cuenta `docente_taller` activa.

**Rechazos:** los mismos que [`proyectos.observarPerfil`](proyectos.observarPerfil.md).

**Respuesta:** el estudiante recibe el motivo y puede presentar un perfil con otro tema con [`proyectos.registrarPerfil`](proyectos.registrarPerfil.md). El sistema no compara títulos: si el tema cambió lo juzga el docente al evaluar el perfil nuevo.

**Ejemplo:**

```text
Asunto: proyectos.rechazarPerfil[2026-2-218001234]
Cuerpo: El tema no es viable con los recursos de la carrera.
```
