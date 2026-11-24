# `tramites.abrirTramite`: abrir un trámite

[Guía de comandos](README.md) › Trámites (`tramites`) · UC-002

Secretaría abre el trámite de titulación de un estudiante. El trámite se abre sobre la cuenta de estudiante: si el estudiante todavía no tiene cuenta, créala antes con [`usuarios.crearCuenta`](usuarios.crearCuenta.md) y el rol `estudiante`.

**Sintaxis:**

```text
tramites.abrirTramite[registro]
```

**Parámetros:** 1. **Adjuntos:** 4. **Cuerpo:** no se lee.

| # | Parámetro | Obligatorio | Tipo | Formato / valores | Descripción |
| --- | --- | --- | --- | --- | --- |
| 1 | `registro` | Sí | Dígitos | Hasta 10 | Registro universitario del estudiante (el `codigo` de su cuenta) |

**Adjuntos:** un PDF por cada requisito de apertura, de hasta 25 MB cada uno. El sistema reconoce el requisito por una palabra en el nombre del archivo, separada del resto por guiones, guiones bajos, puntos o espacios; cada nombre debe contener una sola de esas palabras. El escaneo de la carta física es suficiente.

| Requisito | Palabra en el nombre | Ejemplo de nombre |
| --- | --- | --- |
| Carta de solicitud | `carta` | `carta_solicitud.pdf` |
| Hoja de vida | `cv` | `cv.pdf` |
| Cédula de identidad | `ci` | `ci.pdf` |
| Boleta de inscripción con Modalidad de Graduación | `boleta` | `boleta.pdf` |

**Quién puede usarlo:** una cuenta `administrativo` activa.

**Rechazos:**

- Ninguna cuenta tiene ese registro.
- La cuenta no es de estudiante o está desactivada.
- El estudiante ya tiene un trámite en curso.
- El estudiante ya tuvo un trámite en el semestre vigente, aunque esté cerrado.
- Un adjunto no es PDF, pesa más de 25 MB o no se puede identificar su requisito.

**Respuesta:** Secretaría recibe la referencia del trámite, formada por el semestre vigente y el registro, por ejemplo `2026-2-218001234`. El estudiante recibe el aviso de apertura con la misma referencia.

**Ejemplo:**

```text
Asunto: tramites.abrirTramite[218001234]
Adjuntos: carta_solicitud.pdf, cv.pdf, ci.pdf, boleta.pdf
```
