## RENAMED Requirements

- FROM: `### Requirement: Reemplazo de documento inicial`
- TO: `### Requirement: Reemplazo de requisito de apertura`

## MODIFIED Requirements

### Requirement: Apertura de trámite de titulación

El sistema SHALL permitir a Secretaría abrir un trámite por correo con el registro universitario de una cuenta de estudiante existente y un PDF por cada requisito de apertura (carta de solicitud, hoja de vida, cédula de identidad y boleta de inscripción), de hasta 25 MiB cada uno. La apertura no crea cuentas: la cuenta del estudiante se crea antes con la gestión de usuarios (UC-001).

#### Scenario: Estudiante con cuenta activa

- **WHEN** el registro corresponde a una cuenta de estudiante activa sin trámite en curso ni trámite en el semestre vigente
- **THEN** el sistema abre el trámite sobre esa cuenta, guarda un documento por requisito, confirma la referencia a Secretaría y avisa al estudiante.

#### Scenario: Estudiante sin cuenta

- **WHEN** ninguna cuenta tiene el registro indicado
- **THEN** el sistema rechaza la apertura e indica que la cuenta del estudiante se crea primero con el comando de creación de cuentas.

#### Scenario: Apertura rechazada

- **WHEN** el remitente carece de autorización, el registro o los adjuntos son inválidos, falta o se repite un requisito, la cuenta del registro no es de estudiante, está desactivada, tiene un trámite en curso o ya tiene un trámite en el semestre vigente
- **THEN** el sistema informa el rechazo sin abrir un expediente parcial y sin cambiar la cuenta.

### Requirement: Reemplazo de requisito de apertura

El sistema SHALL admitir que Secretaría sustituya el documento de un requisito de apertura por su referencia de trámite y un PDF cuyo nombre identifique el requisito. El documento sustituido SHALL conservarse como versión anterior del nuevo.

#### Scenario: Trámite en curso

- **WHEN** la referencia identifica un trámite en curso y el adjunto es válido
- **THEN** el sistema sustituye el documento de ese requisito, conserva los demás y confirma a Secretaría indicando el requisito.
- **AND** el PDF recibido pasa a ser la versión vigente del documento y el sustituido queda como su versión anterior, con su contenido sin cambios.

#### Scenario: Trámite no disponible

- **WHEN** el trámite no existe, está cerrado o el remitente carece de autorización
- **THEN** el sistema rechaza la solicitud sin sustituir documentos.
