# Proposal

## Why

UC-003 abre el trámite de titulación, la entidad de la que dependen todos los demás casos de uso del trámite, y es arquitectónicamente significativo: cruza de GraduationProcesses a Users para crear la cuenta del estudiante y es el primer caso de uso que recibe y guarda archivos adjuntos. Su modelo ya está detallado, analizado y diseñado, y UC-004 parte de su postcondición, así que es el siguiente caso de uso que se implementa.

## What Changes

- Comandos del trámite de titulación por correo, permitidos al secretario(a) de dirección de carrera:
  - `AbrirTramite(nombreCompleto; correo; registro; cedula; telefono)` con los cuatro documentos iniciales adjuntos (FR-001, FR-002, FR-051).
  - `ReemplazarDocumento(registro)` con un adjunto (FR-055).
  - `CerrarTramite(registro)`: registra la recepción de los dos ejemplares físicos, cierra el trámite y archiva el expediente (FR-017, FR-018).
- Reglas del trámite:
  - Los cuatro documentos iniciales, en PNG, JPG/JPEG o PDF de hasta 1 MB (CON-005).
  - Un solo trámite en curso por estudiante, abierto sobre su cuenta de usuario si ya existe (FR-056).
  - Reemplazo de documentos solo con el trámite en curso, y cierre solo después de la calificación final.
- Cuenta del estudiante: Users la obtiene o la crea con el rol de estudiante, las reglas y el aviso de UC-001, sin el alcance de quien gestiona (FR-051). Se rechaza si la cuenta existente está desactivada.
- Avisos por correo al estudiante de la apertura y del cierre de su trámite (FR-054).
- Archivos (AD-012): un subsistema Storage nuevo los guarda en el sistema de archivos del servidor y los borra si el comando se deshace.
- Adjuntos (NFR-IN-001):
  - El sistema reconoce el tipo de cada documento por una palabra de su nombre de archivo, sin nombrarlo en el comando.
  - Messaging decodifica los nombres de archivo codificados (RFC 2047 y RFC 2231).
  - Messaging entrega al comando solo las partes con `Content-Disposition: attachment`; las imágenes inline (firmas, imágenes pegadas en el cuerpo) se ignoran.
- `GraduationParticipation` deja la implementación provisional de UC-001 y busca en los trámites reales.
- Docker: directorio de archivos con permisos para el usuario `mailapp`, volumen `files` y variable `FILES_ROOT`.
- Ejemplos `.http` de los tres comandos con la API de Mailpit, incluidos adjuntos, una imagen inline y los rechazos.

Fuera de esta propuesta:

- Registrar la calificación final (UC-006). Hasta entonces, la etapa `FINAL_GRADE_RECORDED` que exige el cierre solo se alcanza con un cambio directo en la base de datos, en las pruebas y en el ejemplo `.http`.
- Dar de baja un trámite abandonado, que sigue como pregunta abierta en UC-003.
- El destino de los trámites pendientes de reasignación (FR-034).

## Capabilities

### New Capabilities

- `graduation-processes`: apertura del trámite de titulación con sus documentos iniciales y la cuenta del estudiante, reemplazo de documentos, cierre con los ejemplares físicos, avisos al estudiante y búsqueda de los trámites en curso de una cuenta de usuario.

### Modified Capabilities

- `user-accounts`: la cuenta del estudiante se obtiene o se crea al abrir su trámite de titulación, sin el alcance de quien gestiona, y se rechaza una cuenta existente desactivada.
- `mail-commands`: los adjuntos del comando son solo las partes adjuntas, con su nombre de archivo decodificado, y no se nombran como argumento.

## Impact

- **Código:**
  - Paquete `graduationprocesses` completo: manejadores de los tres comandos, servicio, entidades `GraduationProcess` e `InitialDocument`, repositorio, puerto `StudentAccounts` e implementación real de `GraduationParticipation`.
  - Paquete nuevo `storage` con `FileStorage`.
  - `users`: implementación de `StudentAccounts`, `obtainStudentAccount`, `findByStudentRegistrationNumber` y el motivo `INACTIVE_ACCOUNT`. Además, `GraduationProcessSummary` pasa a llevar el id de la cuenta del estudiante en lugar de su nombre, y Users resuelve el nombre para las respuestas de UC-001, que no cambian.
  - `messaging`: `Pop3MailboxPoller` (decodificación de nombres y solo partes adjuntas).
- **Base de datos:** migración `V4` con `graduation_processes`, `graduation_process_participants` e `initial_documents`.
- **Infraestructura:** `Dockerfile`, `docker-compose.yml`, `.env.example` y `application.properties` (`app.files.root`).
- **Pruebas y ejemplos:** pruebas de integración por correo con varios adjuntos, y archivos `src/test/http/08` a `10`.
- **Modelo:**
  - Las realizaciones de diseño de UC-003 y de UC-001.
  - `docs/model/design/subsystems/messaging.md`, por el filtrado y la decodificación de adjuntos.
  - `docs/model/use-cases/use-cases.md` y `README.md`, si cambian.
