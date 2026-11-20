# Sistema de Gestión de Trámites de Proyecto de Grado

Proyecto de la materia Tecnología Web (INF513): Sistema de Gestión de Trámites de Proyecto de Grado para la carrera de Ingeniería en Redes y Telecomunicaciones, Facultad de ingeniería en Ciencias de la Computación y Telecomunicaciones (FICCT), Universidad Autónoma Gabriel René Moreno (UAGRM).

## Problema

El trámite de titulación por Proyecto de Grado de la carrera de Ingeniería en Redes y Telecomunicaciones pasa por muchas etapas y personas. El estudiante solicita el trámite y presenta su perfil de proyecto. El docente de Taller de Grado evalúa su viabilidad, el director de carrera designa al tutor y al tribunal evaluador, y el tribunal revisa el documento hasta aprobarlo. Luego se programan la predefensa y la defensa final, y al terminar se emite el acta de graduación y se archivan los ejemplares físicos. Hoy todo se lleva en papel: carpetas físicas, cartas dirigidas a la Dirección de Carrera y notificaciones personales a cargo del secretario(a) de dirección de carrera.

Por eso nadie sabe en qué etapa está un trámite sin acudir a la secretaría a preguntar. Esto afecta a los estudiantes, a los docentes del proceso y a la propia dirección. Tampoco se pueden obtener estadísticas de titulación sin revisar las carpetas una por una.

Con el sistema, cada etapa del trámite queda registrada en un expediente digital que todos los participantes pueden consultar según su rol, y la dirección obtiene reportes y estadísticas a partir de esos registros. Criterio de éxito: cualquier participante consulta el estado de un trámite sin acudir a la secretaría, y el 100% de los trámites del semestre quedan registrados en el sistema.

### Documentos para iniciar el trámite

1. carta de solicitud dirigida al director de carrera con: nombre completo, registro, número de teléfono de contacto.
2. hoja de vida (CV)
3. fotocopia de cédula de identidad
4. boleta de inscripción del semestre en curso con Modalidad de Graduación inscrita

### Flujo

| Paso | Actor | Acción |
| --- | --- | --- |
| 1 | Estudiante | Inicia trámite de titulación por modalidad de graduación: Proyecto de Grado |
| 2 | Secretario(a) de dirección de carrera | Revisar que la documentación presentada por el estudiante incluya los documentos para iniciar el trámite. |
| 3 | Estudiante | Presentar documento: Perfil de proyecto |
| 4 | Docente de Taller de Grado | Evaluar viabilidad del perfil de proyecto presentado por el estudiante (1) |
| 5 | Director de carrera | Asignar tribunal evaluador y tutor |
| 6 | Secretario(a) de dirección de carrera | Notificar al estudiante la designación de tribunal y tutor |
| 7 | Estudiante | Presentar documento del proyecto para revisión |
| 8 | Tribunal evaluador y tutor | Revisar y aprobar el documento (2) |
| 9 | Director de carrera | Programar predefensa y revisión de prototipo |
| 10 | Secretario(a) de dirección de carrera | Notificar al estudiante la fecha de la predefensa y revisión de prototipo |
| 11 | Estudiante | Realizar predefensa del proyecto |
| 12 | Tribunal evaluador | Realizar observaciones finales al proyecto y aprobar para defensa final |
| 13 | Estudiante | Presentar documento para defensa final |
| 14 | Director de carrera | Asignar fecha para defensa final |
| 15 | Estudiante | Realizar defensa final del proyecto | 
| 16 | Tribunal evaluador | Asignar calificación al proyecto |
| 17 | Secretario(a) de dirección de carrera | Archivar ejemplares en físico del proyecto. |

1. El docente de taller de grado puede rechazar el proyecto presentado por el estudiante o solicitar cambios al perfil de proyecto, el estudiante debe subsanar estas observaciones y presentar un nuevo documento.
2. El estudiante recibe correcciones a su proyecto por parte del tribunal y su tutor asignados hasta que el documento esté apto para la predefensa.

## Casos de Uso Aprobados

| ID | Caso de uso | ¿Qué soluciona? | Prioridad |
| --- | --- | --- | --- |
| UC-001 | Gestión de usuarios | Todos los participantes del trámite necesitan una cuenta con sus roles para usar el sistema | Must |
| UC-002 | Habilitación académica de solicitudes | La solicitud y los documentos iniciales del estudiante se reciben en papel y no se sabe si el trámite fue habilitado | Must |
| UC-003 | Asignación de tutor y registro del perfil de proyecto | La designación del tutor y la entrega del perfil de proyecto no quedan registradas en ningún lugar consultable | Must |
| UC-004 | Evaluación de viabilidad y conformación del tribunal | La evaluación del perfil y la designación de los miembros del tribunal se comunican por papel o en persona | Must |
| UC-005 | Revisión, versionado y control de plazos del tribunal | Las observaciones del tribunal, las correcciones del estudiante y sus plazos quedan dispersos en cartas y versiones impresas | Must |
| UC-006 | Programación y registro de evaluaciones | La programación de la predefensa y la defensa final, sus resultados, el acta de graduación, la entrega de ejemplares y el cierre del expediente se registran en papel | Could |
| UC-007 | Monitoreo, trazabilidad, dashboard y calendario académico | Nadie sabe en qué etapa está un trámite ni qué evaluaciones vienen sin preguntar en la secretaría | Should |
| UC-008 | Reportes y estadísticas | La dirección no puede obtener estadísticas de titulación sin revisar las carpetas una por una | Could |

### Alcance

**In scope:**

- Prueba de concepto del sistema mediante una arquitectura Email Driven.
- 

**Out of scope:**

- Ninguno por ahora.

## Plan del proyecto

### Equipo

| Member | Role |
| --- | --- |
| Roger Roca Arano | Guía el desarrollo: arquitectura, integración y revisión de lo que llega a `develop` |
| Francisco Javier Cabrera Claros | Desarrollo por casos de uso |
| Alejandro de los Rios Arancibia | Desarrollo por casos de uso |

### Hitos

| Meta | Fecha | Status |
| --- | --- | --- |
| Arquitectura estable diseñada e implementada con al menos un caso de uso | TBD | Considerado |
| Implementación de los casos de uso con prioridad `Must` | TBD | Considerado |
| Implementación de casos de uso restantes | TBD | Considerado |
| Pruebas contra entorno de producción | TBD | Considerado |
| Presentación final del proyecto | 2026-11-24 | Planeado |


### Iteraciones

| Iteración | Inicio | Fin | Objetivo | Estado |
| --- | --- | --- | --- | --- |
| I1 | 2026-10-26 | 2026-11-07 | Alcance aprobado por el docente, arquitectura candidata, riesgos priorizados y plan de Elaboración | Completado |
| E1 | 2026-11-08 | 2026-11-15 | Análisis y diseño de la arquitectura base de los casos de uso significativos | Incompleto |
| C1 | 2026-11-16 | 2026-11-26 | Construcción del sistema y pruebas | Retrasado |


### Riesgos

| ID | Riesgo | Respuesta | Estado |
| --- | --- | --- | --- |
| RSK-001 | El servidor de correo del docente rechaza los correos de la aplicación porque su SMTP valida al remitente por dirección IP y la IP del laboratorio o del servidor de contingencia no está permitida, o el servidor no está disponible; sin correo, ningún caso de uso se puede presentar | Solicitar añadir a la lista blanca del servidor la dirección IP del entorno de producción. verificar que el SMTP las acepta. Probar la cadena completa (sondeo POP3, comando, respuesta SMTP) contra el servidor SMTP de producción. | Abierto |
| RSK-002 | El servidor de correo no acepta adjuntos de hasta 25 MB, y los usuarios no pueden entregar el perfil ni el documento del proyecto | Enviar un adjunto de 25 MB a través de producción y confirmar el límite del buzón | Abierto |
| RSK-003 | El sondeo del buzón cada 30 segundos sobrecarga el servidor de correo, y las solicitudes de los usuarios no se procesan | Ajustar el intervalo de sondeo a un valor aceptable | Abierto |
| RSK-004 | Los usuarios escriben mal el comando del asunto o el nombre de los adjuntos, y sus solicitudes fallan | Responder cada error con la sintaxis correcta del comando. Preparar una guía de comandos para la presentación | Abierto |
