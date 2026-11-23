# Spec Delta

## MODIFIED Requirements

### Requirement: Reemplazo de documento inicial

El sistema SHALL admitir que Secretaría sustituya un documento inicial por su referencia de trámite y un PDF cuyo nombre identifique el tipo. El documento sustituido SHALL conservarse como versión anterior del nuevo.

#### Scenario: Trámite en curso

- **WHEN** la referencia identifica un trámite en curso y el adjunto es válido
- **THEN** el sistema sustituye el documento de ese tipo, conserva los demás y confirma a Secretaría.
- **AND** el PDF recibido pasa a ser la versión vigente del documento y el sustituido queda como su versión anterior, con su contenido sin cambios.

#### Scenario: Trámite no disponible

- **WHEN** el trámite no existe, está cerrado o el remitente carece de autorización
- **THEN** el sistema rechaza la solicitud sin sustituir documentos.
