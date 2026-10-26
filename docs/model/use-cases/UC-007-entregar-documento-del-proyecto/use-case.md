# UC-007 Entregar documento del proyecto

| | |
| --- | --- |
| Status | Detailed |
| Iteration | I1 |
| Primary actor | ACT-001 Estudiante |
| Supporting actors | ACT-009 Servicio de correo |
| Priority | Must |
| Architecturally significant | Yes: recepción de adjuntos de hasta 25 MB por POP3, almacenamiento de archivos y versionado del documento del proyecto; no se sabe si el servidor de correo acepta ese tamaño |
| Feature | FEAT-005 |
| Functional requirements (derived) | FR-008, FR-010, FR-025, FR-040, FR-041 |

## Brief Description

El estudiante entrega el documento del proyecto para su revisión, y entrega versiones nuevas con las correcciones a las observaciones recibidas. Cada versión se revisa en dos etapas: primero la revisa el tutor, como filtro previo, y solo si la aprueba pasa a los miembros del tribunal evaluador (UC-008). El estudiante puede entregar la primera versión desde que tiene tutor asignado, y una versión nueva cuando el tutor observó la versión vigente, o cuando todos los miembros del tribunal la revisaron y al menos uno registró observaciones. Cada versión lleva un comentario general del estudiante; el sistema conserva el historial de versiones y avisa por correo al tutor. Después de aprobar la predefensa, el estudiante entrega la versión para la defensa final, que se revisa en las mismas dos etapas.

## Preconditions

- El estudiante tiene un trámite de titulación en curso con tutor asignado (UC-004).

## Basic Flow

1. El estudiante entrega el documento del proyecto en PDF con un comentario general.
2. El sistema verifica que el trámite admita una entrega: es la primera, el tutor registró observaciones a la versión vigente, o todos los miembros del tribunal revisaron la versión vigente y al menos uno registró observaciones.
3. El sistema verifica que el archivo cumpla CON-004.
4. El sistema registra el documento como una versión nueva, la deja como versión vigente para revisión y conserva las anteriores en el historial de versiones.
5. El sistema notifica al tutor, a través del Servicio de correo, que hay una versión para revisar.
6. El sistema confirma al estudiante la entrega con el número de la versión.

## Alternative Flows

### A1 Entrega para la defensa final

En el paso 4 del flujo básico, si la predefensa del trámite está aprobada (UC-010):

1. El sistema registra la versión como versión para la defensa final; la revisan el tutor y luego los miembros del tribunal evaluador, como cualquier otra versión.
2. El caso de uso continúa en el paso 5 del flujo básico.

## Exceptions

### E1 Trámite sin tutor

En el paso 1 del flujo básico, si el estudiante no tiene un trámite de titulación en curso con tutor asignado:

1. El sistema informa al estudiante que todavía no puede entregar el documento del proyecto.
2. El caso de uso termina sin registrar ninguna versión.

### E2 Entrega no admitida

En el paso 2 del flujo básico, si el tutor todavía no revisó la versión vigente, si el tutor la aprobó y algún miembro del tribunal todavía no la revisó, o si todos la aprobaron y la predefensa todavía no está aprobada:

1. El sistema informa al estudiante por qué no admite la entrega y, si corresponde, qué revisores faltan.
2. El caso de uso termina sin registrar ninguna versión.

### E3 Archivo inválido

En el paso 3 del flujo básico, si la entrega no trae un archivo, el archivo no es PDF o supera 25 MB:

1. El sistema informa al estudiante qué problema tiene el archivo.
2. El caso de uso termina sin registrar ninguna versión.

## Postconditions

- **Success:** una versión nueva del documento del proyecto quedó registrada como versión vigente para revisión, las anteriores siguen en el historial de versiones y el tutor fue notificado.
- **Failure:** no se registró ninguna versión y la versión vigente no cambió.

## Special Requirements (derived)

- CON-004

## Open Questions

- [x] A1: ¿el tutor también revisa la versión para la defensa final o solo el tribunal evaluador? Respuesta: sí la revisa. En todas las versiones el tutor revisa primero, como filtro previo, y solo las versiones que aprueba pasan al tribunal evaluador.
