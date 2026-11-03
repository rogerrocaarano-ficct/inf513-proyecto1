# UC-004 Gestionar proyecto de grado

| | |
| --- | --- |
| Status | Detailed |
| Iteration | I1 |
| Primary actor | ACT-001 Estudiante |
| Supporting actors | ACT-005 Docente de Taller de Grado, ACT-006 Docente Tutor |
| Priority | Must |
| Architecturally significant | Yes: recibe los archivos del proyecto de grado, de hasta 25 MB, como adjuntos de correo (NFR-IN-001, CON-004) y conserva el historial de versiones del documento del proyecto |
| Feature | FEAT-003, FEAT-005 |
| Functional requirements (derived) | FR-004, FR-008, FR-010, FR-023, FR-025, FR-040, FR-041, FR-057, FR-058, FR-059 |

## Brief Description

El estudiante presenta su perfil de proyecto y, una vez viable y con tutor asignado, entrega las versiones del documento del proyecto hasta su aprobación, incluida la versión para la defensa final. El proyecto de grado nace con el perfil de proyecto y conserva todas sus versiones.

## Preconditions

- El estudiante tiene una cuenta de usuario activa y un trámite de titulación en curso, abierto por el secretario(a) de dirección de carrera (UC-003).

## Basic Flow

1. El estudiante presenta el perfil de proyecto en PDF con el título del proyecto.
2. El sistema verifica que el estudiante todavía no tiene un perfil de proyecto.
3. El sistema verifica que el archivo cumple CON-004.
4. El sistema crea el proyecto de grado del estudiante con el título indicado, registra el perfil de proyecto como pendiente de evaluación y registra el cambio en el registro de cambios.
5. El sistema notifica al docente de Taller de Grado que hay un perfil de proyecto para evaluar.
6. El sistema confirma al estudiante la presentación del perfil de proyecto.

## Alternative Flows

### A1 Presentar un perfil de proyecto nuevo

En el paso 2, si el último perfil de proyecto del estudiante fue rechazado u observado por el docente de Taller de Grado (UC-006).

1. El sistema verifica que el archivo cumple CON-004.
2. El sistema registra el perfil de proyecto nuevo como pendiente de evaluación, conserva los anteriores en el proyecto de grado, cambia el título del proyecto por el indicado y registra el cambio en el registro de cambios.
3. El caso de uso continúa en el paso 5 del flujo básico.

### A2 Entregar una versión del documento del proyecto

En el paso 1, si el estudiante entrega el documento del proyecto en PDF con un comentario general.

1. El sistema verifica que el perfil de proyecto vigente tiene visto bueno y que el estudiante tiene tutor asignado (UC-005).
2. El sistema verifica que el proyecto de grado admite una versión nueva: es la primera; el tutor observó la versión vigente; todos los miembros del tribunal revisaron la versión vigente y al menos uno registró observaciones; o el documento del proyecto está aprobado, la predefensa está aprobada y todavía no hay versión para la defensa final.
3. El sistema verifica que el archivo cumple CON-004.
4. El sistema registra el documento como una versión nueva con su número, la deja como versión vigente para revisión, conserva las anteriores en el historial de versiones y registra el cambio en el registro de cambios.
5. El sistema notifica al tutor que hay una versión para revisar.
6. El sistema confirma al estudiante la entrega con el número de la versión.
7. El caso de uso termina.

### A3 Entregar la versión para la defensa final

En el paso A2.4, si la predefensa del estudiante está aprobada (UC-006).

1. El sistema registra la versión como versión para la defensa final; la revisan el tutor y luego los miembros del tribunal evaluador, como cualquier otra versión (UC-006).
2. El caso de uso continúa en el paso A2.5.

### A4 Consultar el proyecto de grado

En el paso 1, si el estudiante solicita consultar su proyecto de grado.

1. El sistema muestra el título del proyecto, los perfiles de proyecto con su estado y las versiones del documento del proyecto con su número, fecha, comentario general y estado de revisión; si el estudiante todavía no presentó un perfil de proyecto, se lo indica.
2. El caso de uso termina.

## Exceptions

### E1 Perfil de proyecto no admitido

En el paso 2, si el último perfil de proyecto del estudiante está pendiente de evaluación o tiene visto bueno.

1. El sistema rechaza el perfil de proyecto e indica el motivo.
2. El caso de uso termina sin cambios.

### E2 Perfil sin visto bueno o sin tutor

En el paso A2.1, si el perfil de proyecto vigente no tiene visto bueno o el estudiante no tiene tutor asignado.

1. El sistema informa al estudiante que todavía no puede entregar el documento del proyecto e indica lo que falta.
2. El caso de uso termina sin cambios.

### E3 Versión no admitida

En el paso A2.2, si el tutor todavía no revisó la versión vigente; si el tutor la aprobó y algún miembro del tribunal todavía no la revisó; si todos la aprobaron y la predefensa todavía no está aprobada; o si la versión para la defensa final ya está aprobada.

1. El sistema informa al estudiante por qué no admite la versión y, si corresponde, qué revisores faltan.
2. El caso de uso termina sin cambios.

### E4 Archivo inválido

En los pasos 3, A1.1 o A2.3, si la solicitud no trae el archivo, el archivo no es PDF o supera 25 MB.

1. El sistema informa al estudiante qué problema tiene el archivo.
2. El caso de uso termina sin cambios.

### E5 Datos incompletos

En los pasos 1 o A2, si falta el título del proyecto del perfil de proyecto o el comentario general de la versión.

1. El sistema rechaza la solicitud e indica los datos que faltan.
2. El caso de uso termina sin cambios.

## Postconditions

- **Success:** queda registrado un perfil de proyecto pendiente de evaluación y el docente de Taller de Grado fue notificado, o queda registrada una versión nueva del documento del proyecto como versión vigente para revisión, con las anteriores en el historial de versiones, y el tutor fue notificado; el cambio queda en el registro de cambios. En una consulta, el sistema no cambia nada.
- **Failure:** el proyecto de grado no cambia: no se registra ningún perfil de proyecto ni ninguna versión, y el estudiante conoce el motivo del rechazo.

## Special Requirements (derived)

- CON-004

## Open Questions

None.
