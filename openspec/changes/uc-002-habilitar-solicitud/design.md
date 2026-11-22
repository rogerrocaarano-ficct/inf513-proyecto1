## Context

El contrato sigue `auxiliary-docs/flujo-uc-02-codex.md` y las observaciones de `Observaciones.jpeg`.

## Decisions

- Solo Secretaría ejecuta ambos comandos. La cuenta remitente debe estar activa.
- Se reutiliza la validación de UC-001. Correo y registro deben estar libres o identificar al mismo estudiante activo.
- La transacción del despachador abarca el alta, el expediente y sus documentos. Las restricciones de PostgreSQL impiden más de un trámite en curso por estudiante. Los avisos salen después de confirmar.
- El reemplazo identifica el expediente por una referencia generada del tipo `TR-<número>` y el documento por una palabra en el nombre del PDF. Se conserva el expediente y se sustituye el archivo indicado.
- Se adoptan provisionalmente PDF de hasta 25 MiB y las palabras `carta`, `cv`, `ci` y `boleta` como términos separados. Para la carta basta el escaneo físico.

## Diferencias con el modelo

La revisión en EA queda pendiente. Deben cotejarse nombres, relaciones, referencia del trámite, permisos y contratos de correo. También queda pendiente confirmar si se requiere conservar el archivo sustituido.
