# Proposal

## Why

Hoy `tramites.abrirTramite` recibe los siete datos de una cuenta de usuario y, según el caso, crea la cuenta del estudiante o reutiliza la existente. El comando es en la práctica un `usuarios.crearCuenta` con adjuntos, y `DegreeProcedureUseCase` reimplementa reglas de UC-001 (candidato con `signUp`, emparejamiento de la cuenta existente, doble chequeo de permisos y comparación directa de roles). Además, la referencia del trámite (`TR-001`) es la clave autoincremental de la tabla: la asigna la base de datos al insertar, el agregado no la conoce al abrirse y el `UUID` del agregado no es su clave primaria.

Pertenece a **UC-002 Habilitación académica de solicitudes**, flujo básico (apertura del trámite de titulación por Proyecto de Grado) y A2 (reemplazo de documento inicial), y toca la forma en que UC-003 identifica el trámite. Es parte de la corrección de inconsistencias del subsistema Procedimientos (`procedures`).

## What Changes

- **BREAKING** — **Apertura por registro**: `tramites.abrirTramite` recibe un solo argumento, el registro universitario del estudiante, y los cuatro PDF iniciales. La cuenta del estudiante se crea antes con `usuarios.crearCuenta` (UC-001); la apertura ya no crea cuentas. Si no hay una cuenta con ese registro, la apertura se rechaza.
- La cuenta del registro debe poder tener un trámite de titulación (estudiante), estar activa y no tener un trámite en curso (FR-056). La comprobación de que es una cuenta de estudiante la responde la propia cuenta, sin comparar roles en el trámite.
- **BREAKING** — **Referencia del trámite**: pasa de `TR-<n>` asignado por la base de datos a `<semestre>-<registro>` (por ejemplo `2026-2-218001234`), construida por el agregado al abrirse con el semestre vigente. La usan `tramites.reemplazarDocumento` y `proyectos.asignarTutor`.
- **Un trámite por semestre**: un estudiante no puede abrir dos trámites de titulación en el mismo semestre, aunque el primero esté cerrado.
- **Semestre vigente**: lo da un puerto de calendario académico. Mientras no exista el caso de uso de gestión del calendario académico, su adaptador lee el valor de la configuración.
- **Identidad del trámite**: el `UUID` del agregado pasa a ser la clave primaria; la referencia queda como dato único. Migración nueva; no se edita `V4`.
- La apertura se confirma con la referencia dentro del evento de apertura; desaparece el paso de la referencia por separado al notificador.
- Se eliminan del dominio de trámites las reglas de emparejamiento de cuentas y la creación de la cuenta del estudiante.

Fuera de alcance: la carga de documentos por el propio estudiante, la gestión del calendario académico y los cursos de verano, el modelo de EA (se reconstruirá después desde el código) y cambiar el nombre de UC-002 en `README.md`.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `degree-procedures`: la apertura recibe el registro de una cuenta de estudiante existente y deja de crear cuentas; se agrega la referencia del trámite por semestre y registro, con un trámite por estudiante y semestre.

## Impact

- **Modelo de EA**: UC-002 no tiene diseño en EA (no hay caso de uso ni paquete `Design/Procedures`). Por decisión del equipo, el modelo no se consulta en este change; se sigue la arquitectura de referencia de `AGENTS.md`.
- **Requisitos**: FR-001, FR-002, FR-051, FR-055, FR-056 sin cambio de texto. Cambia la nota de FR-051/FR-056 en `REQUIREMENTS.md` (la cuenta se crea por UC-001 y la apertura la referencia) y se agrega la regla de un trámite por estudiante y semestre (acuerdo del 2026-11-23).
- **Código**: `procedures` (dominio, caso de uso, handlers, notificadores, persistencia, configuración y pruebas); `users.domain` (`UserRepository` busca por registro con bloqueo; consulta nueva `User.isStudent()`); migración `V10`.
- **Configuración**: propiedad nueva `cpms.academic.current-semester`.
- **Dependencias**: ninguna nueva.
- **Documentos**: `USER-GUIDE.md` (`tramites.abrirTramite`, `tramites.reemplazarDocumento`, `proyectos.asignarTutor`), `GLOSSARY.md` (referencia del trámite, semestre, calendario académico, subsistema Procedimientos), `REQUIREMENTS.md`.
