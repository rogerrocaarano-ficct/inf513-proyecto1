# Glossary

## Terms

| Term | Definition | Synonyms | Related terms | Model element (derived) | Implementation name | Source |
| --- | --- | --- | --- | --- | --- | --- |
| Acta de graduación | Documento oficial que registra la calificación final de la defensa final y la conclusión de la titulación de un estudiante |  | Defensa final, Calificación final |  | | SRC-001 |
| Administrador | Rol técnico que crea y mantiene las cuentas de usuario y sus roles |  | Usuario | ACT-008 Administrador | | SRC-003 |
| Biblioteca | Biblioteca de la universidad que recibe uno de los dos ejemplares físicos del proyecto |  | Ejemplar físico |  | | SRC-001 |
| Calendario académico | Vista de las predefensas y defensas finales programadas, por fecha |  | Predefensa, Defensa final |  | | SRC-002 |
| Calificación final | Nota que el tribunal evaluador asigna al proyecto al concluir la defensa final |  | Defensa final, Acta de graduación |  | | SRC-001 |
| Comando | Solicitud que el usuario escribe en el asunto de un correo con la forma de una llamada a función, `Comando(arg1; arg2; …)`, con los argumentos separados por punto y coma, en la prueba de concepto por correo | | Servicio de correo | | | SRC-003 |
| Cuenta de usuario | Registro de un usuario en el sistema con su nombre completo, dirección de correo, uno o más roles, foto de perfil opcional y estado (activa o desactivada); la de un estudiante incluye su registro universitario, cédula de identidad y teléfono | Cuenta (avoid) | Usuario, Rol | | | SRC-004 |
| Dashboard | Panel con indicadores de los trámites de titulación en curso |  | Trámite de titulación |  | | SRC-002 |
| Defensa final | Presentación pública del proyecto ante el tribunal evaluador, tras aprobar la predefensa, en la que se asigna la calificación final | Defensa pública (avoid) | Predefensa, Calificación final |  | | SRC-001 |
| Dirección de Carrera | Unidad que administra la carrera, encabezada por el director de carrera, que recibe las cartas del trámite y conserva un ejemplar físico del proyecto |  | Director de carrera, Secretario(a) de dirección de carrera |  | | SRC-001 |
| Director de carrera | Autoridad de la carrera que designa al tutor y al tribunal evaluador y programa la predefensa y la defensa final |  | Dirección de Carrera | ACT-003 Director de carrera | | SRC-001 |
| Docente | Profesor de la carrera que puede participar en un trámite de titulación como docente de Taller de Grado, tutor o miembro del tribunal |  | Docente de Taller de Grado, Tutor, Miembro del tribunal | ACT-004 Docente | | SRC-001 |
| Docente de Taller de Grado | Docente de la materia Taller de Grado, único en la carrera, que evalúa la viabilidad de todos los perfiles de proyecto; en el sistema, rol de cuenta que solo se combina con el de docente |  | Perfil de proyecto, Visto bueno | ACT-005 Docente de Taller de Grado | | SRC-001 |
| Documento del proyecto | Documento escrito del Proyecto de Grado que el estudiante entrega para la revisión del tribunal evaluador y del tutor, en una o más versiones | Proyecto (avoid), Tesis (avoid) | Versión del documento, Observación |  | | SRC-001 |
| Documentos iniciales | Carta de solicitud, hoja de vida, fotocopia de cédula de identidad y boleta de inscripción con Modalidad de Graduación que el estudiante presenta para iniciar el trámite | Requisitos iniciales (avoid) | Solicitud de titulación |  | | SRC-001 |
| Ejemplar físico | Copia impresa del proyecto aprobado que se archiva: una en la biblioteca y otra en la Dirección de Carrera |  | Biblioteca, Expediente de titulación |  | | SRC-001 |
| Estudiante | Postulante que realiza el trámite de titulación por Proyecto de Grado | Postulante (avoid), Titulando (avoid) | Trámite de titulación | ACT-001 Estudiante | | SRC-001 |
| Evaluación | Predefensa o defensa final de un estudiante ante el tribunal evaluador | | Predefensa, Defensa final | | | SRC-001 |
| Expediente de titulación | Registro de todos los documentos, designaciones, evaluaciones y resultados de un trámite de titulación, que se abre con la solicitud y se archiva al cerrar el trámite |  | Trámite de titulación |  | | SRC-002 |
| Foto de perfil | Imagen que identifica a un usuario en su cuenta de usuario | Foto (avoid) | Cuenta de usuario | | | SRC-002 |
| Miembro del tribunal | Docente designado como uno de los tres integrantes del tribunal evaluador de un estudiante |  | Tribunal evaluador | ACT-007 Miembro del tribunal | | SRC-001 |
| Modalidad de Graduación | Forma por la que el estudiante obtiene el título; en este sistema, Proyecto de Grado |  | Proyecto de Grado |  | | SRC-001 |
| Observación | Corrección o cambio que un docente pide al estudiante sobre su perfil de proyecto o su documento del proyecto, y que el estudiante debe subsanar |  | Documento del proyecto, Perfil de proyecto |  | | SRC-001 |
| Perfil de proyecto | Documento breve con la propuesta del proyecto que el estudiante presenta para que el docente de Taller de Grado evalúe su viabilidad | Perfil (avoid) | Docente de Taller de Grado, Visto bueno |  | | SRC-001 |
| Plazo de revisión | Tiempo del que disponen el tribunal evaluador y el tutor para revisar una versión del documento del proyecto |  | Documento del proyecto |  | | SRC-002 |
| Predefensa | Presentación preliminar del proyecto y revisión del prototipo ante el tribunal evaluador, que habilita la defensa final | Revisión de prototipo (avoid) | Defensa final |  | | SRC-001 |
| Proyecto de Grado | Modalidad de Graduación en la que el estudiante desarrolla y defiende un proyecto |  | Modalidad de Graduación |  | | SRC-001 |
| Registro universitario | Código que la universidad asigna a cada estudiante y que no se repite entre estudiantes | Registro (avoid) | Estudiante | | | SRC-001 |
| Revisión | Resultado que un revisor registra sobre una versión del documento del proyecto: aprobada u observada, con sus observaciones en texto y un PDF anotado opcional | | Revisor, Observación, Versión del documento | | | SRC-006 |
| Revisor | Tutor o miembro del tribunal evaluador designado en un trámite para revisar el documento del proyecto | | Tutor, Miembro del tribunal, Versión del documento | | | SRC-005 |
| Rol | Función general que una cuenta de usuario tiene en el sistema, una o varias por cuenta: estudiante, docente, docente de Taller de Grado, secretario(a) de dirección de carrera, director de carrera o administrador | | Cuenta de usuario, Tutor, Miembro del tribunal | | | SRC-004 |
| Secretario(a) de dirección de carrera | Personal de la Dirección de Carrera que recibe los documentos del trámite y lleva el expediente de titulación | Secretaria (avoid) | Dirección de Carrera | ACT-002 Secretario(a) de dirección de carrera | | SRC-001 |
| Servicio de correo | Servicio de correo electrónico por el que el sistema recibe solicitudes y envía respuestas y notificaciones |  |  | ACT-009 Servicio de correo | | SRC-003 |
| Solicitud de titulación | Pedido del estudiante para iniciar el trámite de titulación, que acompaña a los documentos iniciales |  | Documentos iniciales, Trámite de titulación |  | | SRC-001 |
| Trámite de titulación | Proceso que sigue un estudiante desde su solicitud de titulación hasta el archivo de su expediente | Proceso de titulación (avoid) | Expediente de titulación |  | | SRC-001 |
| Tribunal evaluador | Grupo de tres docentes que revisa el documento del proyecto y evalúa la predefensa y la defensa final de un estudiante | Tribunal calificador (avoid), Cuerpo de tribunales (avoid) | Miembro del tribunal |  | | SRC-001 |
| Tutor | Docente que guía al estudiante en su proyecto y revisa el documento del proyecto junto al tribunal evaluador | Docente tutor (avoid), Tutor académico (avoid) | Docente | ACT-006 Tutor | | SRC-001 |
| Usuario | Persona con una cuenta de usuario en el sistema |  | Administrador |  | | SRC-002 |
| Versión del documento | Cada entrega sucesiva del documento del proyecto, con un comentario general del estudiante y sus observaciones; la última entregada es la versión vigente |  | Documento del proyecto, Observación |  | | SRC-001 |
| Visto bueno | Aprobación formal que emite un docente sobre el perfil de proyecto o el documento del proyecto | Aprobación (avoid) | Perfil de proyecto, Documento del proyecto |  | | SRC-001 |

## Acronyms and Abbreviations

| Acronym | Expansion | Notes |
| --- | --- | --- |
| CI | Cédula de identidad | Uno de los documentos iniciales |
| CU | Caso de uso | En SRC-002 numera los casos de uso aprobados por el docente (CU1–CU8), que este modelo trata como funcionalidades |
| CV | Currículum vitae (hoja de vida) | Uno de los documentos iniciales |
| RF | Requisito funcional | Numeración original de SRC-002 (RF01–RF19), equivalente a FR-001–FR-019 |

## Open Questions

- [x] Predefensa: ¿"revisión de prototipo" es la misma evaluación que la predefensa o una actividad distinta en la misma sesión? Respuesta: es la misma actividad.
