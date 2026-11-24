# `tramites.reemplazarDocumento`: reemplazar un documento de apertura

[Guía de comandos](README.md) › Trámites (`tramites`) · UC-002

Secretaría sustituye el documento de un requisito de apertura mientras el trámite esté en curso. El documento anterior se conserva como versión anterior.

**Sintaxis:**

```text
tramites.reemplazarDocumento[referencia]
```

**Parámetros:** 1. **Adjuntos:** 1. **Cuerpo:** no se lee.

| # | Parámetro | Obligatorio | Tipo | Formato / valores | Descripción |
| --- | --- | --- | --- | --- | --- |
| 1 | `referencia` | Sí | Referencia | `año-periodo-registro` | Referencia recibida al abrir el trámite |

**Adjuntos:** un PDF de hasta 25 MB con la palabra del requisito que sustituye en el nombre (`carta`, `cv`, `ci` o `boleta`, como en [`tramites.abrirTramite`](tramites.abrirTramite.md)).

**Quién puede usarlo:** una cuenta `administrativo` activa.

**Rechazos:** no existe un trámite en curso con esa referencia, o el adjunto no es un PDF válido o no se puede identificar su requisito.

**Respuesta:** Secretaría recibe una confirmación que identifica el trámite y el requisito sustituido.

**Ejemplo:**

```text
Asunto: tramites.reemplazarDocumento[2026-2-218001234]
Adjuntos: carta_corregida.pdf
```
