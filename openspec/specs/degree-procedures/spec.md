# degree-procedures Specification

## Purpose

Habilitar la apertura de un expediente de titulación y el reemplazo de sus documentos iniciales por correo.

## Requirements

### Requirement: Apertura de trámite de titulación

El sistema SHALL permitir a Secretaría abrir un trámite por correo con siete datos de cuenta y cuatro PDF iniciales, uno por tipo, de hasta 25 MiB cada uno.

#### Scenario: Estudiante con cuenta activa

- **WHEN** correo y registro corresponden a la misma cuenta de estudiante activa sin trámite en curso
- **THEN** el sistema abre el trámite sobre esa cuenta, guarda los cuatro documentos, confirma la referencia a Secretaría y avisa al estudiante.

#### Scenario: Estudiante sin cuenta

- **WHEN** correo y registro están libres y la solicitud es válida
- **THEN** el sistema crea la cuenta bajo las reglas de UC-001, abre el trámite en la misma transacción y envía al estudiante los avisos de alta y apertura.

#### Scenario: Apertura rechazada

- **WHEN** el remitente carece de autorización, los datos o adjuntos son inválidos o la cuenta es incompatible, está desactivada o tiene un trámite en curso
- **THEN** el sistema informa el rechazo sin crear una cuenta ni un expediente parcial.

### Requirement: Reemplazo de documento inicial

El sistema SHALL admitir que Secretaría sustituya un documento inicial por su referencia de trámite y un PDF cuyo nombre identifique el tipo. El documento sustituido SHALL conservarse como versión anterior del nuevo.

#### Scenario: Trámite en curso

- **WHEN** la referencia identifica un trámite en curso y el adjunto es válido
- **THEN** el sistema sustituye el documento de ese tipo, conserva los demás y confirma a Secretaría.
- **AND** el PDF recibido pasa a ser la versión vigente del documento y el sustituido queda como su versión anterior, con su contenido sin cambios.

#### Scenario: Trámite no disponible

- **WHEN** el trámite no existe, está cerrado o el remitente carece de autorización
- **THEN** el sistema rechaza la solicitud sin sustituir documentos.
