# Spec Delta

## Purpose

Lleva el proyecto de grado de cada estudiante dentro de su trámite de titulación, desde la presentación del perfil de proyecto hasta la entrega de la versión para la defensa final, con su documento vigente, su estado y su historial.

## ADDED Requirements

### Requirement: Comandos del proyecto de grado
El sistema SHALL atender los comandos `PresentarPerfil(titulo)`, `EntregarVersion(comentario)` y `ConsultarProyecto()` solo de cuentas de usuario activas con el rol de estudiante, y SHALL aplicarlos al trámite de titulación en curso del remitente.

#### Scenario: Remitente sin el rol de estudiante
- **WHEN** un docente envía `ConsultarProyecto()`
- **THEN** el sistema no ejecuta el comando y responde que no tiene permiso para ese comando

#### Scenario: Estudiante sin trámite en curso
- **WHEN** un estudiante sin trámite de titulación en curso envía `PresentarPerfil(Sistema de inventario)` con un PDF adjunto
- **THEN** el sistema rechaza la solicitud, indica que no hay un trámite de titulación en curso y no cambia nada

### Requirement: Datos del comando
El sistema SHALL rechazar sin cambios `PresentarPerfil` sin título y `EntregarVersion` sin comentario general, indicando el dato que falta, y cualquiera de los dos con más argumentos que su forma.

#### Scenario: Perfil sin título
- **WHEN** el estudiante envía `PresentarPerfil()` con un PDF adjunto
- **THEN** el sistema rechaza la solicitud, indica que falta el título del proyecto y no cambia nada

#### Scenario: Versión sin comentario general
- **WHEN** el estudiante envía `EntregarVersion()` con un PDF adjunto
- **THEN** el sistema rechaza la solicitud, indica que falta el comentario general y no cambia nada

### Requirement: Formato y tamaño del documento
El sistema SHALL aceptar en `PresentarPerfil` y `EntregarVersion` un solo adjunto, en PDF, no vacío y de hasta 25 MB (CON-004), y rechazar sin cambios cualquier otro, indicando el motivo.

#### Scenario: Documento de 25 MB
- **WHEN** el estudiante entrega un PDF de exactamente 25 MB con un comando admitido
- **THEN** el sistema registra el documento

#### Scenario: Documento demasiado grande
- **WHEN** el estudiante entrega un PDF de 25 MB y un byte
- **THEN** el sistema rechaza la solicitud, indica el tamaño máximo y no cambia nada

#### Scenario: Formato no permitido
- **WHEN** el estudiante entrega `perfil.docx`
- **THEN** el sistema rechaza la solicitud, indica que el documento debe ser un PDF y no cambia nada

#### Scenario: Cantidad de adjuntos distinta de uno
- **WHEN** el estudiante envía `PresentarPerfil(Sistema de inventario)` sin adjuntos o con dos
- **THEN** el sistema rechaza la solicitud, indica que se espera un solo adjunto y no cambia nada

### Requirement: Presentación del primer perfil de proyecto
El sistema SHALL, con `PresentarPerfil(titulo)` de un estudiante cuyo trámite en curso no tiene proyecto de grado, crear el proyecto de grado con ese título y el perfil de proyecto pendiente de evaluación (FR-004). Debe registrar el cambio, avisar al docente de Taller de Grado (FR-057) y confirmarlo al estudiante.

#### Scenario: Primer perfil
- **WHEN** el estudiante envía `PresentarPerfil(Sistema de inventario)` con `perfil.pdf`, y su trámite en curso no tiene proyecto de grado
- **THEN** el trámite tiene un proyecto de grado titulado "Sistema de inventario" con el perfil pendiente de evaluación, el cambio queda en el registro de cambios, el docente de Taller de Grado recibe un aviso con el nombre del estudiante y el título, y el estudiante recibe la confirmación

#### Scenario: Sin docente de Taller de Grado activo
- **WHEN** el estudiante presenta su primer perfil y no hay ninguna cuenta activa con el rol de docente de Taller de Grado
- **THEN** el sistema registra el perfil, no envía el aviso, y la confirmación al estudiante indica que no hay un docente de Taller de Grado a quien avisar

### Requirement: Perfil de proyecto nuevo
El sistema SHALL admitir un perfil de proyecto nuevo solo cuando el perfil vigente fue observado o rechazado (FR-023, FR-058). El perfil nuevo reemplaza al vigente con su título y queda pendiente de evaluación; el anterior queda en el historial. En cualquier otro estado, SHALL rechazar el perfil sin cambios e indicar el motivo.

#### Scenario: Perfil nuevo tras una observación
- **WHEN** el perfil vigente del estudiante está observado y el estudiante envía `PresentarPerfil(Sistema de ventas)` con un PDF
- **THEN** el proyecto de grado se titula "Sistema de ventas", el perfil vigente es el nuevo y está pendiente de evaluación, el anterior aparece en el historial, y el docente de Taller de Grado recibe el aviso

#### Scenario: Perfil pendiente de evaluación
- **WHEN** el perfil vigente está pendiente de evaluación y el estudiante presenta otro
- **THEN** el sistema rechaza el perfil, indica que el vigente está pendiente de evaluación y no cambia nada

#### Scenario: Perfil con visto bueno
- **WHEN** el perfil vigente tiene visto bueno, o el estudiante ya entregó una versión del documento del proyecto, y el estudiante presenta otro perfil
- **THEN** el sistema rechaza el perfil e indica el motivo, sin cambios

### Requirement: Requisitos para entregar el documento del proyecto
El sistema SHALL rechazar sin cambios `EntregarVersion` cuando el trámite no tiene proyecto de grado, cuando el perfil vigente no tiene visto bueno o cuando el trámite no tiene tutor asignado, indicando lo que falta (FR-008).

#### Scenario: Perfil sin visto bueno
- **WHEN** el perfil vigente está pendiente de evaluación y el estudiante envía `EntregarVersion(Primera versión)` con un PDF
- **THEN** el sistema rechaza la entrega, indica que el perfil de proyecto todavía no tiene visto bueno y no cambia nada

#### Scenario: Sin tutor asignado
- **WHEN** el perfil vigente tiene visto bueno, el trámite no tiene tutor y el estudiante entrega una versión
- **THEN** el sistema rechaza la entrega, indica que todavía no tiene tutor asignado y no cambia nada

### Requirement: Entrega de una versión del documento del proyecto
El sistema SHALL registrar una versión nueva con el número siguiente y el comentario general, en revisión del tutor, solo si es la primera, si el tutor o el tribunal observó la vigente, o si la vigente está aprobada y la predefensa también (FR-008, FR-010, FR-040). Debe registrar el cambio, avisar al tutor (FR-041) y confirmar el número al estudiante.

#### Scenario: Primera versión
- **WHEN** el perfil vigente tiene visto bueno, el trámite tiene tutor y el estudiante envía `EntregarVersion(Primera versión)` con `documento.pdf`
- **THEN** el proyecto de grado tiene la versión 1 en revisión del tutor, el perfil queda en el historial, el cambio queda en el registro de cambios, el tutor recibe un aviso con el nombre del estudiante, el título, el número y el comentario, y el estudiante recibe "Versión 1 del documento del proyecto entregada."

#### Scenario: Versión corregida tras observaciones
- **WHEN** la versión 2 está observada por el tutor, o por el tribunal, y el estudiante entrega otra
- **THEN** el proyecto de grado tiene la versión 3 en revisión del tutor y la versión 2 aparece en el historial

#### Scenario: Versión en revisión
- **WHEN** la versión vigente está en revisión del tutor o del tribunal y el estudiante entrega otra
- **THEN** el sistema rechaza la entrega, indica que la versión vigente todavía está en revisión y no cambia nada

#### Scenario: Versión aprobada sin predefensa
- **WHEN** la versión vigente está aprobada, la predefensa no está aprobada y el estudiante entrega otra
- **THEN** el sistema rechaza la entrega, indica que la predefensa todavía no está aprobada y no cambia nada

### Requirement: Versión para la defensa final
El sistema SHALL registrar como versión para la defensa final toda versión entregada después de aprobar la predefensa (FR-025), con las mismas reglas de las demás versiones, y SHALL rechazar sin cambios una versión nueva cuando la versión para la defensa final ya está aprobada.

#### Scenario: Entrega después de la predefensa
- **WHEN** la versión 4 está aprobada, la predefensa está aprobada y el estudiante entrega otra
- **THEN** el proyecto de grado tiene la versión 5 como versión para la defensa final, en revisión del tutor, y el estudiante recibe la confirmación que lo indica

#### Scenario: Versión para la defensa final aprobada
- **WHEN** la versión para la defensa final está aprobada y el estudiante entrega otra
- **THEN** el sistema rechaza la entrega, indica que la versión para la defensa final ya está aprobada y no cambia nada

### Requirement: Consulta del proyecto de grado
El sistema SHALL responder a `ConsultarProyecto()` con el título del proyecto, el documento vigente con su tipo, su número de versión y su estado, y el historial de los perfiles y versiones anteriores con su fecha, su comentario general y su último estado (FR-059). Si el trámite no tiene proyecto de grado, SHALL indicar que el estudiante todavía no presentó un perfil, sin rechazar el comando.

#### Scenario: Proyecto con historial
- **WHEN** el estudiante presentó dos perfiles, el segundo con visto bueno, entregó la versión 1 y envía `ConsultarProyecto()`
- **THEN** la respuesta muestra el título, la versión 1 en revisión del tutor, y en el historial los dos perfiles con su fecha y su estado (observado y con visto bueno)

#### Scenario: Sin proyecto de grado
- **WHEN** el estudiante con trámite en curso y sin perfil envía `ConsultarProyecto()`
- **THEN** la respuesta indica que todavía no presentó un perfil de proyecto

### Requirement: Conservación de los documentos anteriores
El sistema SHALL conservar el archivo de cada perfil y versión entregados mientras exista el trámite, también cuando otro los reemplaza como documento vigente (FR-010).

#### Scenario: Perfil reemplazado
- **WHEN** el estudiante presenta un perfil nuevo tras una observación
- **THEN** el archivo del perfil anterior sigue guardado y el historial lo cita

### Requirement: Rechazo sin cambios
El sistema SHALL deshacer todo lo que hizo un comando del proyecto de grado que se rechaza: el proyecto de grado, su registro de cambios, el archivo guardado y los avisos.

#### Scenario: Rechazo después de guardar el archivo
- **WHEN** un comando del proyecto de grado se rechaza o falla después de guardar el archivo adjunto
- **THEN** el proyecto de grado y el registro de cambios quedan como estaban, el archivo no queda guardado y no se envía ningún aviso
