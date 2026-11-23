# Delta de specs

## Purpose

Registrar el perfil de proyecto que presenta el estudiante, con sus versiones, y la evaluación de viabilidad que hace el docente de Taller de Grado (UC-004).

## ADDED Requirements

### Requirement: Presentación del perfil

El sistema SHALL admitir por correo el título y un PDF del estudiante activo con trámite en curso cuando no tiene perfil o su último perfil fue observado o rechazado (FR-004, FR-023, FR-058). El perfil presentado queda pendiente.

#### Scenario: Primer perfil

- **WHEN** el estudiante sin perfil presenta un título y un PDF válidos y hay un docente de Taller activo
- **THEN** se crea el proyecto de grado del trámite con el perfil pendiente, y después del commit se envía el perfil al docente de Taller (FR-057).

#### Scenario: Versión nueva tras una observación o un rechazo

- **WHEN** el último perfil del estudiante está observado o rechazado y presenta un título y un PDF válidos
- **THEN** se registra una versión nueva pendiente con el número siguiente; el título del proyecto pasa a ser el indicado, la versión anterior y su evaluación se conservan, y se envía el perfil al docente de Taller después del commit, indicando el número de versión.

#### Scenario: Presentación rechazada

- **WHEN** falta el trámite en curso, el último perfil está pendiente o aprobado, el título está vacío, el PDF es inválido o supera 25 MB (NFR-005), el remitente carece de permiso o no hay docente de Taller activo
- **THEN** se informa el motivo y no se registra nada, de modo que el estudiante pueda reintentar.

### Requirement: Aprobación del perfil

El sistema SHALL permitir que el docente de Taller de Grado apruebe por correo el perfil pendiente de un trámite en curso, indicando su referencia (FR-005).

#### Scenario: Perfil pendiente aprobado

- **WHEN** el docente de Taller activo aprueba el perfil pendiente de un trámite en curso
- **THEN** el perfil queda aprobado con la fecha de la evaluación, y después del commit se avisa al estudiante y al director de carrera.

#### Scenario: Sin director activo

- **WHEN** el docente de Taller aprueba un perfil pendiente y no hay un director de carrera activo
- **THEN** el perfil queda aprobado igual y se avisa al estudiante; el aviso al director se omite y queda registrado en el log.

#### Scenario: Aprobación rechazada

- **WHEN** el remitente carece de permiso, el trámite no existe o no está en curso, o no tiene un perfil pendiente
- **THEN** se informa el motivo sin cambiar el perfil.

### Requirement: Observación del perfil

El sistema SHALL permitir que el docente de Taller de Grado observe por correo el perfil pendiente de un trámite en curso, con las observaciones escritas en el cuerpo del correo (FR-022).

#### Scenario: Perfil pendiente observado

- **WHEN** el docente de Taller activo observa el perfil pendiente de un trámite en curso y el cuerpo tiene texto
- **THEN** el perfil queda observado con las observaciones y la fecha, y después del commit se avisa al estudiante con las observaciones.

#### Scenario: Observación rechazada

- **WHEN** el cuerpo está vacío, el remitente carece de permiso, el trámite no existe o no está en curso, o no tiene un perfil pendiente
- **THEN** se informa el motivo sin cambiar el perfil.

### Requirement: Rechazo del perfil

El sistema SHALL permitir que el docente de Taller de Grado rechace por correo el perfil pendiente de un trámite en curso, con el motivo escrito en el cuerpo del correo (FR-022).

#### Scenario: Perfil pendiente rechazado

- **WHEN** el docente de Taller activo rechaza el perfil pendiente de un trámite en curso y el cuerpo tiene texto
- **THEN** el perfil queda rechazado con las observaciones y la fecha, y después del commit se avisa al estudiante con las observaciones.

#### Scenario: Rechazo no admitido

- **WHEN** el cuerpo está vacío, el remitente carece de permiso, el trámite no existe o no está en curso, o no tiene un perfil pendiente
- **THEN** se informa el motivo sin cambiar el perfil.

### Requirement: Historial del perfil

El sistema SHALL conservar en el proyecto de grado cada versión del perfil de proyecto, numerada en orden desde 1, con su título, su PDF, su estado y, si fue evaluada, sus observaciones y la fecha de la evaluación (FR-023, FR-059).

#### Scenario: Varias versiones

- **WHEN** un perfil observado o rechazado es seguido de una versión nueva
- **THEN** la versión anterior conserva su número, su estado, sus observaciones y su PDF, y la nueva toma el número siguiente; el historial se lee del proyecto, sin depender del historial de archivos del almacenamiento.
