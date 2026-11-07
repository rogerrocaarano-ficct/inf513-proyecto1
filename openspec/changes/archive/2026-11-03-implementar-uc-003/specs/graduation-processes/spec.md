# Spec Delta

## Purpose

Lleva el trámite de titulación de cada estudiante desde su apertura, con los documentos iniciales y la cuenta de usuario del estudiante, hasta el cierre y archivo de su expediente de titulación.

## ADDED Requirements

### Requirement: Comandos del trámite de titulación
El sistema SHALL atender los comandos `AbrirTramite(nombreCompleto; correo; registro; cedula; telefono)`, `ReemplazarDocumento(registro)` y `CerrarTramite(registro)` solo de cuentas de usuario activas con el rol de secretario(a) de dirección de carrera. El trámite se indica por el registro universitario de su estudiante.

#### Scenario: Remitente sin el rol de secretario(a)
- **WHEN** el administrador envía `CerrarTramite(219000001)`
- **THEN** el sistema no ejecuta el comando y responde que no tiene permiso para ese comando

#### Scenario: Faltan datos del estudiante
- **WHEN** el secretario(a) envía `AbrirTramite(Juan Soto; juan@uagrm.edu.bo; 219000001)` con los cuatro documentos iniciales
- **THEN** el sistema rechaza la solicitud, indica los datos que faltan y no abre ningún trámite

### Requirement: Reconocimiento de los documentos iniciales
El sistema SHALL reconocer el tipo de cada documento inicial adjunto por una palabra de su nombre de archivo, sin distinguir mayúsculas ni tildes y sin contar la extensión: `carta` (carta de solicitud), `hojadevida` o `cv` (hoja de vida), `cedula` (fotocopia de cédula de identidad) y `boleta` (boleta de inscripción). `cv` solo cuenta como palabra separada.

#### Scenario: Nombres con mayúsculas y tildes
- **WHEN** el secretario(a) abre un trámite con los adjuntos `Carta_Soto.pdf`, `CV.jpg`, `Cédula.pdf` y `boleta-2026.png`
- **THEN** el sistema reconoce la carta de solicitud, la hoja de vida, la fotocopia de cédula de identidad y la boleta de inscripción

#### Scenario: Adjunto que no se reconoce
- **WHEN** uno de los adjuntos se llama `foto.png`
- **THEN** el sistema rechaza la solicitud, indica el adjunto que no reconoce y no abre ningún trámite

#### Scenario: Dos adjuntos del mismo tipo
- **WHEN** los adjuntos incluyen `carta.pdf` y `carta2.pdf`
- **THEN** el sistema rechaza la solicitud e indica el tipo de documento repetido

### Requirement: Formato y tamaño de los documentos iniciales
El sistema SHALL aceptar solo documentos iniciales en imagen PNG o JPG/JPEG, o en PDF, de hasta 1 MB cada uno (CON-005), y rechazar sin cambios cualquier otro, indicando el documento y el motivo.

#### Scenario: Formato no permitido
- **WHEN** la hoja de vida se adjunta como `hojadevida.docx`
- **THEN** el sistema rechaza la solicitud, indica el documento y el formato, y no abre ningún trámite

#### Scenario: Documento demasiado grande
- **WHEN** la boleta de inscripción pesa más de 1 MB
- **THEN** el sistema rechaza la solicitud, indica el documento y el tamaño máximo, y no abre ningún trámite

### Requirement: Apertura del trámite de titulación
El sistema SHALL abrir el trámite de titulación del estudiante con sus cuatro documentos iniciales y su cuenta de usuario, en curso y en la etapa de apertura (FR-001, FR-002, FR-051). Debe registrar la apertura en el registro de cambios, avisar por correo al estudiante y confirmarla al secretario(a).

#### Scenario: Apertura con cuenta nueva
- **WHEN** el secretario(a) envía `AbrirTramite(Juan Soto; juan@uagrm.edu.bo; 219000001; 1234567; 70000000)` con los cuatro documentos iniciales, y no existe ninguna cuenta de usuario con esa dirección de correo ni con ese registro universitario
- **THEN** existe una cuenta de usuario activa de Juan Soto con el rol de estudiante y un trámite en curso suyo con los cuatro documentos, ambos quedan en el registro de cambios, Juan Soto recibe el aviso de creación de su cuenta y el de apertura de su trámite, y el secretario(a) recibe la confirmación

#### Scenario: Apertura incompleta
- **WHEN** el secretario(a) abre un trámite sin adjuntar la boleta de inscripción
- **THEN** el sistema rechaza la solicitud, indica el documento que falta y no crea la cuenta de usuario ni el trámite

### Requirement: Un solo trámite en curso por estudiante
El sistema SHALL abrir el trámite sobre la cuenta de usuario existente del estudiante, sin crear otra ni avisar de su creación, cuando la dirección de correo y el registro universitario pertenecen a la misma cuenta activa con el rol de estudiante; y SHALL rechazar la apertura si esa cuenta ya tiene un trámite en curso (FR-056).

#### Scenario: Estudiante con cuenta y sin trámite en curso
- **WHEN** el secretario(a) abre un trámite para la dirección de correo y el registro universitario de una cuenta activa de estudiante sin trámite en curso
- **THEN** el trámite se abre sobre esa cuenta, no se crea otra, y el estudiante recibe solo el aviso de apertura

#### Scenario: Estudiante con un trámite en curso
- **WHEN** el secretario(a) abre un trámite para un estudiante que ya tiene uno en curso
- **THEN** el sistema rechaza la solicitud, indica que ya tiene un trámite en curso y no cambia nada

### Requirement: Reemplazo de un documento inicial
El sistema SHALL reemplazar el documento inicial del tipo que indica el nombre del único adjunto de `ReemplazarDocumento(registro)`, solo si el estudiante tiene un trámite en curso y el documento cumple el formato y el tamaño (FR-055). Debe registrar el cambio y confirmarlo al secretario(a).

#### Scenario: Reemplazo de la boleta
- **WHEN** el secretario(a) envía `ReemplazarDocumento(219000001)` con el adjunto `boleta.pdf`, y el estudiante tiene un trámite en curso
- **THEN** la boleta de inscripción del trámite es el archivo nuevo, el cambio queda en el registro de cambios y el secretario(a) recibe la confirmación

#### Scenario: Estudiante sin trámite en curso
- **WHEN** el secretario(a) envía `ReemplazarDocumento` con un registro universitario sin trámite en curso
- **THEN** el sistema rechaza la solicitud, indica que no hay un trámite en curso y no cambia nada

#### Scenario: Cantidad de adjuntos distinta de uno
- **WHEN** el secretario(a) envía `ReemplazarDocumento(219000001)` sin adjuntos o con dos
- **THEN** el sistema rechaza la solicitud, indica que se espera un solo adjunto y no cambia nada

### Requirement: Cierre del trámite de titulación
El sistema SHALL, con `CerrarTramite(registro)`, registrar la recepción de los dos ejemplares físicos, cerrar el trámite y archivar su expediente, solo si el trámite está en curso y ya tiene registrada la calificación final (FR-017, FR-018). Debe registrar el cambio, avisar al estudiante y confirmarlo al secretario(a).

#### Scenario: Cierre con calificación final
- **WHEN** el secretario(a) envía `CerrarTramite(219000001)` y el trámite en curso del estudiante tiene registrada la calificación final
- **THEN** el trámite queda cerrado con la fecha de recepción de los ejemplares físicos, el cambio queda en el registro de cambios, el estudiante recibe el aviso de cierre y el secretario(a) recibe la confirmación

#### Scenario: Cierre sin calificación final
- **WHEN** el secretario(a) envía `CerrarTramite(219000001)` y el trámite todavía no tiene la calificación final
- **THEN** el sistema rechaza la solicitud, indica la etapa del trámite y no cambia nada

### Requirement: Rechazo sin cambios
El sistema SHALL deshacer todo lo que hizo un comando del trámite que se rechaza: la cuenta de usuario creada, el trámite, sus documentos, los archivos guardados, sus registros de cambios y sus avisos.

#### Scenario: Rechazo después de crear la cuenta
- **WHEN** la apertura de un trámite se rechaza después de verificar la cuenta del estudiante
- **THEN** no queda la cuenta de usuario, ni el trámite, ni ningún archivo guardado, ni registros de cambios, y no se envían avisos

### Requirement: Trámites en curso de una cuenta de usuario
El sistema SHALL encontrar los trámites en curso en los que una cuenta de usuario participa como estudiante, para dejarlos pendientes de reasignación cuando esa cuenta se desactiva o pierde el rol de estudiante (FR-034).

#### Scenario: Desactivación de un estudiante con trámite en curso
- **WHEN** el administrador desactiva la cuenta de usuario de un estudiante con un trámite en curso
- **THEN** ese trámite queda pendiente de reasignación y el administrador recibe la advertencia con el nombre del estudiante
