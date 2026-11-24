# Documentación

Documentación del Sistema de Gestión de Trámites de Proyecto de Grado (CPMS): el problema que resuelve, sus casos de uso, el plan del proyecto y los documentos del proyecto.

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
| [UC-001](use-cases/UC-001.md) | Gestión de usuarios | Todos los participantes del trámite necesitan una cuenta con sus roles para usar el sistema | Must |
| [UC-002](use-cases/UC-002.md) | Habilitación académica de solicitudes | La solicitud y los requisitos de apertura del estudiante se reciben en papel y no se sabe si el trámite fue habilitado | Must |
| [UC-003](use-cases/UC-003.md) | Asignación de tutor y designación de tribunal | La designación del tutor y de los miembros del tribunal se comunica por papel o en persona | Must |
| [UC-004](use-cases/UC-004.md) | Evaluación del perfil de proyecto | La entrega del perfil de proyecto y su evaluación de viabilidad no quedan registradas en ningún lugar consultable | Must |
| [UC-005](use-cases/UC-005.md) | Revisión y versionado del documento del proyecto | Las observaciones del tutor y del tribunal y las correcciones del estudiante quedan dispersas en cartas y versiones impresas | Must |
| [UC-006](use-cases/UC-006.md) | Programación y registro de evaluaciones | La programación de la predefensa y la defensa final, sus resultados, la entrega de ejemplares y el cierre del expediente se registran en papel | Could |
| [UC-007](use-cases/UC-007.md) | Monitoreo, trazabilidad, dashboard y calendario académico | Nadie sabe en qué etapa está un trámite ni qué evaluaciones vienen sin preguntar en la secretaría | Should |
| [UC-008](use-cases/UC-008.md) | Reportes y estadísticas | La dirección no puede obtener estadísticas de titulación sin revisar las carpetas una por una | Could |

Cada caso de uso tiene su descripción de negocio en [`use-cases/`](use-cases/), en un archivo con el nombre de su ID (`UC-001.md`).

### Alcance

**In scope:**

- Prueba de concepto del sistema mediante una arquitectura Email Driven.
- 

**Out of scope:**

- Emisión del acta de graduación: el sistema registra la calificación final (UC-006), pero no genera el acta.

## Plan del proyecto

### Equipo

| Member | Role |
| --- | --- |
| Roger Roca Arano | Guía el desarrollo: arquitectura, integración y revisión de lo que llega a `develop` |
| Francisco Javier Cabrera Claros | Desarrollo por casos de uso |
| Alejandro de los Rios Arancibia | Desarrollo por casos de uso |

### Hitos

| Meta | Status |
| --- | --- |
| Arquitectura estable diseñada e implementada con al menos un caso de uso | Completado: canal de correo y UC-001 integrados en `develop` |
| Implementación de los casos de uso con prioridad `Must` | En curso: UC-001, UC-002 y UC-004 implementados; de UC-003 falta la designación del tribunal; UC-005 sin implementar |
| Implementación de casos de uso restantes | Pendiente: UC-006, UC-007 y UC-008 sin implementar |
| Pruebas contra entorno de producción | Completado |
| Presentación final del proyecto | Planeado |

### Iteraciones

| Iteración | Inicio | Fin | Objetivo | Estado |
| --- | --- | --- | --- | --- |
| I1 | 2026-10-26 | 2026-11-07 | Alcance aprobado por el docente, arquitectura candidata, riesgos priorizados y plan de Elaboración | Completado |
| E1 | 2026-11-08 | 2026-11-15 | Análisis y diseño de la arquitectura base de los casos de uso significativos | Incompleto |
| C1 | 2026-11-16 | 2026-11-26 | Construcción del sistema y pruebas | Retrasado |

### Riesgos

| ID | Riesgo | Respuesta | Estado |
| --- | --- | --- | --- |
| RSK-001 | El servidor de correo del docente rechaza los correos de la aplicación porque su SMTP valida al remitente por dirección IP y la IP del laboratorio o del servidor de contingencia no está permitida, o el servidor no está disponible; sin correo, ningún caso de uso se puede presentar | Solicitar añadir a la lista blanca del servidor la dirección IP del entorno de producción. verificar que el SMTP las acepta. Probar la cadena completa (sondeo POP3, comando, respuesta SMTP) contra el servidor SMTP de producción. | Cerrado |
| RSK-002 | El servidor de correo no acepta adjuntos de hasta 25 MB, y los usuarios no pueden entregar el perfil ni el documento del proyecto | Enviar un adjunto de 25 MB a través de producción y confirmar el límite del buzón | Abierto |
| RSK-003 | El sondeo del buzón cada 30 segundos sobrecarga el servidor de correo, y las solicitudes de los usuarios no se procesan | Ajustar el intervalo de sondeo a un valor aceptable | Cerrado |
| RSK-004 | Los usuarios escriben mal el comando del asunto o el nombre de los adjuntos, y sus solicitudes fallan | Responder cada error con la sintaxis correcta del comando. Preparar una guía de comandos para la presentación | Cerrado |

## Documentos

| Documento | Contenido | Para quién |
| --- | --- | --- |
| [Guía de comandos](user-guide/README.md) | Cómo escribir un comando por correo, las respuestas del sistema y una página por cada comando con sus parámetros, permisos, rechazos y ejemplos | Quienes usan el sistema: estudiantes, Secretaría, docentes, Dirección y administradores |
| [Casos de uso](#casos-de-uso-aprobados) | Descripción de negocio de cada caso de uso: objetivo, actores, precondiciones, flujos, excepciones, postcondiciones y reglas de negocio | Equipo de desarrollo y quienes validan el proceso |
| [Requisitos](REQUIREMENTS.md) | Requisitos funcionales (`FR-xxx`) y no funcionales (`NFR-xx-xxx`) | Equipo de desarrollo |
| [Glosario](GLOSSARY.md) | Términos del dominio, su definición y el nombre que toman en el código | Equipo de desarrollo |
| [Marco USDP](USDP-FRAMEWORK.md) | Referencia del Proceso Unificado: fases, flujos de trabajo, artefactos y criterios de salida (en inglés) | Equipo de desarrollo |

## Convenciones

- Cada dato vive en un solo documento. Los demás lo citan por su ID (`UC-003`, `FR-012`, `RSK-002`) en lugar de copiarlo.
- Los documentos se escriben en español, salvo el marco USDP.
- Un cambio de comportamiento actualiza en el mismo cambio los requisitos que toca, la página del comando en la guía y el glosario si aparece un término nuevo.
- Estos documentos no pasan por OpenSpec; los cambios al código sí (ver `AGENTS.md`).
