# Storage

| | |
| --- | --- |
| Type | General |
| Comes from | AM-004 |
| Status | Draft |
| Iteration | I1 |

## Responsibility

Guarda en el sistema de archivos del servidor los archivos que envían los usuarios (documentos iniciales, perfil de proyecto, documento del proyecto, PDF anotado) y los borra cuando se reemplazan (AD-012). Es general: no conoce a qué trámite ni a qué documento pertenece cada archivo; los subsistemas de negocio guardan la clave que devuelve.

## Provided Interfaces

### FileStorage

- **Used by (derived):** GraduationProcesses
- **Use cases (derived):** UC-003

| Operation | Description | Errors |
| --- | --- | --- |
| store(category: String, fileName: String, content: byte[]): String | Escribe el archivo en `<raíz>/<categoría>/<UUID>.<extensión del nombre>` y devuelve esa ruta relativa como clave. Si la transacción en curso se deshace, lo borra | UncheckedIOException: no se pudo escribir; deshace el comando |
| delete(storageKey: String): void | Borra el archivo después de confirmar la transacción en curso; si se deshace, no lo borra | None: un fallo al borrar se registra en el log |

La raíz se configura con `app.files.root` (variable de entorno `FILES_ROOT`).

## Required Interfaces

None.

## Open Questions

None.
