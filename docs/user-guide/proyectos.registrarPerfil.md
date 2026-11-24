# `proyectos.registrarPerfil`: registrar un perfil

[Guía de comandos](README.md) › Proyectos (`proyectos`) · UC-004

El estudiante presenta el perfil de su proyecto de grado. Se usa el mismo comando para la primera entrega y para una versión nueva:

- **Primera entrega:** el sistema crea el proyecto con el perfil pendiente de evaluación.
- **Versión nueva:** solo si el último perfil fue observado o rechazado. El título puede cambiar. La versión anterior se conserva con sus observaciones.

**Sintaxis:**

```text
proyectos.registrarPerfil[titulo]
```

**Parámetros:** 1. **Adjuntos:** 1. **Cuerpo:** no se lee.

| # | Parámetro | Obligatorio | Tipo | Formato / valores | Descripción |
| --- | --- | --- | --- | --- | --- |
| 1 | `titulo` | Sí | Texto | No vacío | Título del proyecto de grado |

**Adjuntos:** el perfil en un PDF de hasta 25 MB.

**Quién puede usarlo:** una cuenta `estudiante` activa con un trámite en curso.

**Rechazos:**

- El estudiante no tiene un trámite en curso.
- El perfil anterior está pendiente de evaluación o ya fue aprobado.
- No hay un docente de Taller de Grado activo; se puede reintentar cuando haya uno.
- El título está vacío o el adjunto no es un PDF válido.

**Respuesta:** el sistema envía el perfil al docente de Taller de Grado para su evaluación.

**Ejemplo:**

```text
Asunto: proyectos.registrarPerfil[Redes comunitarias]
Adjuntos: perfil.pdf
```
