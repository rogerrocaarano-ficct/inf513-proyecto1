## MODIFIED Requirements

### Requirement: Apertura de trámite de titulación

El sistema SHALL permitir a Secretaría abrir un trámite por correo con el registro universitario de una cuenta de estudiante existente y cuatro PDF iniciales, uno por tipo, de hasta 25 MiB cada uno. La apertura no crea cuentas: la cuenta del estudiante se crea antes con la gestión de usuarios (UC-001).

#### Scenario: Estudiante con cuenta activa

- **WHEN** el registro corresponde a una cuenta de estudiante activa sin trámite en curso ni trámite en el semestre vigente
- **THEN** el sistema abre el trámite sobre esa cuenta, guarda los cuatro documentos, confirma la referencia a Secretaría y avisa al estudiante.

#### Scenario: Estudiante sin cuenta

- **WHEN** ninguna cuenta tiene el registro indicado
- **THEN** el sistema rechaza la apertura e indica que la cuenta del estudiante se crea primero con el comando de creación de cuentas.

#### Scenario: Apertura rechazada

- **WHEN** el remitente carece de autorización, el registro o los adjuntos son inválidos, la cuenta del registro no es de estudiante, está desactivada, tiene un trámite en curso o ya tiene un trámite en el semestre vigente
- **THEN** el sistema informa el rechazo sin abrir un expediente parcial y sin cambiar la cuenta.

## ADDED Requirements

### Requirement: Referencia del trámite por semestre y registro

El sistema SHALL identificar cada trámite de titulación con una referencia formada por el semestre vigente al abrirlo y el registro universitario del estudiante, con la forma `<año>-<periodo>-<registro>` (por ejemplo `2026-2-218001234`), y SHALL usarla en las respuestas y en los comandos que actúan sobre un trámite.

#### Scenario: Referencia asignada al abrir

- **WHEN** Secretaría abre el trámite del registro `218001234` en el semestre vigente `2026-2`
- **THEN** la confirmación a Secretaría y el aviso al estudiante muestran la referencia `2026-2-218001234`.

#### Scenario: Referencia usada en otro comando

- **WHEN** un comando que actúa sobre un trámite recibe la referencia `2026-2-218001234` de un trámite en curso
- **THEN** el sistema actúa sobre ese trámite.

#### Scenario: Referencia mal escrita

- **WHEN** un comando recibe una referencia que no tiene la forma `<año>-<periodo>-<registro>`
- **THEN** el sistema rechaza el comando con un mensaje que muestra un ejemplo de referencia válida.

### Requirement: Un trámite por estudiante y semestre

El sistema SHALL rechazar la apertura de un trámite de titulación cuando el estudiante ya tiene un trámite abierto en el semestre vigente, esté en curso o cerrado.

#### Scenario: Segundo trámite en el mismo semestre

- **WHEN** el estudiante ya tuvo un trámite en el semestre vigente, aunque esté cerrado, y Secretaría intenta abrir otro
- **THEN** el sistema rechaza la apertura e informa que el estudiante ya tiene un trámite en ese semestre.

#### Scenario: Trámite en otro semestre

- **WHEN** el único trámite anterior del estudiante es de otro semestre y está cerrado
- **THEN** el sistema permite abrir el trámite en el semestre vigente.

### Requirement: Semestre vigente configurado

El sistema SHALL tomar el semestre vigente de su configuración, con la forma `<año>-<periodo>` y periodo 1 o 2, hasta que exista la gestión del calendario académico, y SHALL negarse a iniciar si el valor falta o es inválido.

#### Scenario: Semestre configurado

- **WHEN** la configuración indica el semestre `2026-2`
- **THEN** los trámites abiertos usan `2026-2` en su referencia.

#### Scenario: Semestre inválido

- **WHEN** la configuración no indica el semestre o indica un valor como `2026-3`
- **THEN** la aplicación no inicia e informa el valor inválido.
