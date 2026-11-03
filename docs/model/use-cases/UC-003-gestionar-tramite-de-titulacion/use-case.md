# UC-003 Gestionar trámite de titulación

| | |
| --- | --- |
| Status | Detailed |
| Iteration | I1 |
| Primary actor | ACT-002 Secretario(a) de dirección de carrera |
| Supporting actors | ACT-001 Estudiante |
| Priority | Must |
| Architecturally significant | Yes: crea el trámite de titulación, la entidad central del subsistema GraduationProcesses de la que dependen los demás casos de uso; cruza al subsistema Users para crear la cuenta del estudiante (FR-051), y recibe y guarda los documentos iniciales como adjuntos de correo (CON-005) |
| Feature | FEAT-002, FEAT-006 |
| Functional requirements (derived) | FR-001, FR-002, FR-017, FR-018, FR-051, FR-054, FR-055, FR-056 |

## Brief Description

El secretario(a) de dirección de carrera abre el trámite de titulación cuando el estudiante se presenta en la oficina con sus documentos iniciales: verifica que la carpeta esté completa, registra los datos del estudiante y crea su cuenta de usuario. Mientras dura el trámite mantiene sus datos, y al final registra la recepción de los ejemplares físicos y cierra y archiva el expediente de titulación. Así cada trámite queda registrado de principio a fin.

## Preconditions

- El secretario(a) de dirección de carrera tiene una cuenta de usuario activa con ese rol.

## Basic Flow

1. El secretario(a) de dirección de carrera, una vez verificado que la carpeta del estudiante está completa, solicita abrir su trámite de titulación con el nombre completo, la dirección de correo, el registro universitario, la cédula de identidad y el teléfono del estudiante, y sus documentos iniciales digitalizados: carta de solicitud, hoja de vida, fotocopia de cédula de identidad y boleta de inscripción con Modalidad de Graduación.
2. El sistema verifica que están los cuatro documentos iniciales y que cada uno es una imagen PNG o JPG/JPEG, o un PDF, de hasta 1 MB.
3. El sistema verifica que la dirección de correo y el registro universitario no pertenecen a otra cuenta de usuario.
4. El sistema crea la cuenta de usuario activa del estudiante con el rol de estudiante, registra el cambio en el registro de cambios y le notifica por correo la creación de su cuenta de usuario, como en UC-001.
5. El sistema abre el trámite de titulación del estudiante con su expediente de titulación, guarda en él los documentos iniciales y registra el cambio en el registro de cambios.
6. El sistema notifica por correo al estudiante la apertura de su trámite de titulación.
7. El sistema confirma al secretario(a) la apertura del trámite de titulación.

## Alternative Flows

### A1 El estudiante ya tiene una cuenta de usuario

En el paso 3, si la dirección de correo y el registro universitario pertenecen a una misma cuenta de usuario activa, con el rol de estudiante y sin otro trámite de titulación en curso.

1. El sistema usa esa cuenta de usuario para el trámite, sin crear otra ni cambiar sus datos.
2. El caso de uso continúa en el paso 5 del flujo básico.

### A2 Reemplazar un documento inicial

En el paso 1, si el secretario(a) solicita reemplazar un documento inicial de un trámite de titulación.

1. El secretario(a) indica el trámite de titulación, el documento inicial que reemplaza y el documento nuevo digitalizado.
2. El sistema verifica que el trámite de titulación está en curso y que el documento nuevo cumple el formato y el tamaño del paso 2.
3. El sistema reemplaza el documento inicial en el expediente de titulación y registra el cambio en el registro de cambios.
4. El sistema confirma al secretario(a) el reemplazo.
5. El caso de uso termina.

### A3 Registrar los ejemplares físicos y cerrar el trámite

En el paso 1, si el secretario(a) solicita registrar la recepción de los ejemplares físicos de un trámite de titulación.

1. El secretario(a) indica el trámite de titulación y registra la recepción de los dos ejemplares físicos: uno para la biblioteca y otro para la Dirección de Carrera.
2. El sistema verifica que el trámite de titulación está en curso y que el tribunal evaluador ya registró la calificación final de la defensa final.
3. El sistema registra la recepción de los ejemplares físicos, cierra el trámite de titulación, archiva su expediente de titulación y registra el cambio en el registro de cambios.
4. El sistema notifica por correo al estudiante el cierre de su trámite de titulación.
5. El sistema confirma al secretario(a) el cierre del trámite de titulación.
6. El caso de uso termina.

## Exceptions

### E1 Datos incompletos

En el paso 1, si falta un dato obligatorio del estudiante.

1. El sistema rechaza la solicitud e indica los datos que faltan.
2. El caso de uso termina sin cambios.

### E2 Documento inicial faltante o no válido

En los pasos 2 o A2.2, si falta un documento inicial, o un documento no es una imagen PNG o JPG/JPEG ni un PDF, o supera 1 MB.

1. El sistema rechaza la solicitud e indica el documento y el motivo.
2. El caso de uso termina sin cambios.

### E3 Dirección de correo o registro universitario repetido

En el paso 3, si la dirección de correo o el registro universitario pertenece a otra cuenta de usuario y no se cumple la condición de A1 (p. ej., la dirección de correo es de una cuenta y el registro universitario de otra, o la cuenta no tiene el rol de estudiante).

1. El sistema rechaza la solicitud e indica el dato repetido.
2. El caso de uso termina sin cambios.

### E4 Cuenta de usuario del estudiante no disponible

En el paso 3, si la cuenta de usuario del estudiante está desactivada o ya tiene un trámite de titulación en curso.

1. El sistema rechaza la solicitud e indica el motivo.
2. El caso de uso termina sin cambios.

### E5 Trámite de titulación no en curso

En los pasos A2.2 o A3.2, si el trámite de titulación indicado no existe o ya está cerrado.

1. El sistema rechaza la solicitud e indica el motivo.
2. El caso de uso termina sin cambios.

### E6 Sin calificación final

En el paso A3.2, si el tribunal evaluador todavía no registró la calificación final de la defensa final.

1. El sistema rechaza la solicitud e indica la etapa en la que está el trámite de titulación.
2. El caso de uso termina sin cambios.

## Postconditions

- **Success:** según lo solicitado, existe un trámite de titulación en curso del estudiante, con su expediente de titulación y sus documentos iniciales, y el estudiante tiene una cuenta de usuario activa con el rol de estudiante; o el documento inicial queda reemplazado; o el trámite de titulación queda cerrado, con su expediente archivado y la recepción de los ejemplares físicos registrada. Cada cambio queda en el registro de cambios, y el estudiante recibe la notificación por correo de la creación de su cuenta de usuario, de la apertura o del cierre.
- **Failure:** ningún trámite de titulación ni cuenta de usuario cambia, y el secretario(a) conoce el motivo del rechazo.

## Special Requirements (derived)

- CON-005

## Open Questions

- [ ] Si el estudiante abandona el trámite de titulación antes de terminarlo, ¿el secretario(a) puede darlo de baja, y qué pasa con su cuenta de usuario? (ask: Secretario(a) de dirección de carrera)
