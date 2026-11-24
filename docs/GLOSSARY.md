# Glosario de términos

| Término | Definición | Término adoptado en el código |
| --- | --- | --- |
| Acta de graduación | Documento oficial que registra la calificación final de la defensa final y la conclusión de la titulación de un estudiante | |
| Adjunto | Archivo que llega con un correo recibido o que se envía con una respuesta o notificación. Se guarda como archivo almacenado y el correo conserva solo su referencia, en su orden; no tiene identidad propia | `Attachment` |
| Administrador | Rol técnico que crea y mantiene las cuentas de usuario y sus roles | `Role.SYSTEM_ADMIN` (etiqueta `administrador`) |
| Archivo almacenado | Archivo que el sistema guarda en su carpeta de almacenamiento, con su nombre original, tipo, tamaño y fecha; cada versión de un documento es un archivo almacenado distinto, y la numera el registro dueño del documento (por ejemplo, las versiones del perfil de proyecto); el enlace entre versiones dentro del almacenamiento está en desuso. No confundir con la versión del documento, que es la entrega del estudiante con sus revisiones | `StoredFile` |
| Biblioteca | Biblioteca de la universidad que recibe uno de los dos ejemplares físicos del proyecto | |
| Calendario académico | Todas las fechas que establece la facultad: los semestres y las evaluaciones programadas, como las predefensas y defensas finales | `AcademicCalendar` (por ahora solo da el semestre vigente) |
| Calificación final | Nota que el tribunal evaluador asigna al proyecto al concluir la defensa final | |
| Cédula de identidad | Número del documento de identidad de una persona (CI), registrado en su cuenta de usuario; obligatorio para el estudiante | `ci` |
| Comando | Solicitud que el usuario escribe en el asunto de un correo con la forma `Comando[arg1; arg2; …]`, con los argumentos separados por punto y coma, en la prueba de concepto por correo | `EmailCommandHandler` (atiende un comando; su nombre es `actionKey`) |
| Correo recibido | Correo que el sistema lee del buzón y guarda para procesar el comando de su asunto | `ReceivedEmail` |
| Cronograma de defensas | Fechas, horas y lugares de las defensas que programa el director de carrera | |
| Cuenta de usuario | Registro de un usuario en el sistema con su nombre completo, dirección de correo, un rol, foto de perfil opcional y estado (activa o desactivada); la de un estudiante incluye su registro universitario, cédula de identidad y teléfono | `User` |
| Dashboard | Panel con indicadores de los trámites de titulación en curso | |
| Datos personales | Datos de contacto de una cuenta de usuario que mantiene el propio usuario: dirección de correo, teléfono y foto de perfil | |
| Defensa | Presentación del proyecto de grado ante el tribunal evaluador, en una de sus dos etapas: la predefensa, interna, y la defensa final, pública | |
| Defensa final | Defensa pública del proyecto de grado ante el tribunal evaluador, tras aprobar la predefensa, en la que se asigna la calificación final | |
| Dirección de Carrera | Unidad que administra la carrera, encabezada por el director de carrera, que recibe las cartas del trámite y conserva un ejemplar físico del proyecto | |
| Director de carrera | Autoridad de la carrera, y también docente, que designa al tutor y al tribunal evaluador y programa la predefensa y la defensa final | `Role.DIRECTOR` (etiqueta `director`) |
| Docente | Profesor de la carrera que puede participar en un trámite de titulación como docente de Taller de Grado, tutor o miembro del tribunal | `Role.PROFESSOR` (etiqueta `docente`) |
| Docente de Taller de Grado | Docente de la materia Taller de Grado, único en la carrera, que evalúa la viabilidad de todos los perfiles de proyecto; en el sistema, rol de cuenta | `Role.CAPSTONE_PROJECT_PROFESSOR` (etiqueta `docente_taller`) |
| Documento del proyecto | Documento escrito del Proyecto de Grado que el estudiante entrega para la revisión del tribunal evaluador y del tutor, en una o más versiones | |
| Ejemplar físico | Copia impresa del proyecto aprobado que se archiva: una en la biblioteca y otra en la Dirección de Carrera | |
| Estadísticas | Indicadores y reportes de titulación de la carrera, obtenidos de los trámites de titulación registrados | |
| Estudiante | Postulante que realiza el trámite de titulación por Proyecto de Grado | `Role.STUDENT` (etiqueta `estudiante`) |
| Evaluación | Predefensa o defensa final de un estudiante ante el tribunal evaluador | |
| Estado del perfil | Situación de una versión del perfil de proyecto: pendiente mientras el docente de Taller de Grado no la evalúa; aprobado; observado, cuando pide cambios sobre el mismo tema; o rechazado, cuando el tema debe cambiar | `ProjectProfileStatus` (`PENDING`, `APPROVED`, `OBSERVED`, `REJECTED`) |
| Evaluación del perfil | Decisión del docente de Taller de Grado sobre la viabilidad de un perfil de proyecto pendiente: lo aprueba, lo observa o lo rechaza, con sus observaciones y su fecha | `DegreeProject.approveProfile`, `observeProfile`, `rejectProfile` |
| Expediente de titulación | Registro de todos los documentos, designaciones, evaluaciones y resultados de un trámite de titulación, que se abre con la solicitud y se archiva al cerrar el trámite | |
| Foto de perfil | Imagen que identifica a un usuario en su cuenta de usuario | |
| Miembro del tribunal | Docente designado como uno de los tres integrantes del tribunal evaluador de un estudiante | |
| Modalidad de Graduación | Forma por la que el estudiante obtiene el título; en este sistema, Proyecto de Grado | |
| Observación | Corrección o cambio que un docente pide al estudiante sobre su perfil de proyecto o su documento del proyecto, y que el estudiante debe subsanar. Las del perfil se escriben en el cuerpo del correo | `ProjectProfile.observations` (perfil) |
| Perfil de proyecto | Documento breve con la propuesta del proyecto que el estudiante presenta para que el docente de Taller de Grado evalúe su viabilidad. Si lo observan o lo rechazan, el estudiante presenta una versión nueva; cada versión se numera en orden desde 1 y las anteriores se conservan con su evaluación | `ProjectProfile` (cada versión, en `DegreeProject.profiles`; su número, `ProjectProfile.version`) |
| Predefensa | Defensa interna del proyecto de grado y revisión del prototipo ante el tribunal evaluador, que habilita la defensa final | |
| Procedimientos | Subsistema que gestiona los flujos de la facultad referidos al trámite de titulación | paquete `procedures` |
| Proyecto de Grado | Proyecto que el estudiante desarrolla y defiende para titularse en la Modalidad de Graduación del mismo nombre; nace con el perfil de proyecto y avanza con las versiones del documento del proyecto hasta la defensa final | `DegreeProject` |
| Referencia del trámite | Identificador del expediente que figura en las respuestas y se usa en comandos como el de sustitución de documentos. Se forma con el semestre en que se abre el trámite y el registro universitario del estudiante. Ejemplo `2026-2-218001234` | `DegreeProcedure.reference` |
| Registro de cambios | Historial de los cambios hechos en el sistema: quién cambió qué dato y cuándo | |
| Registro universitario | Código que la universidad asigna a cada estudiante y que no se repite entre estudiantes | `uagrmCode` (que también guarda el código administrativo o de docente de las demás cuentas) |
| Requisitos de apertura | Requisitos para iniciar el trámite de titulación por Proyecto de Grado: carta de solicitud, hoja de vida, fotocopia de cédula de identidad y boleta de inscripción con Modalidad de Graduación. El estudiante los presenta en la oficina y el secretario(a) adjunta un PDF por cada uno al abrir el trámite | `OpeningRequirement` |
| Revisión | Resultado que un revisor registra sobre una versión del documento del proyecto: aprobada u observada, con sus observaciones en texto y un PDF anotado opcional | |
| Revisor | Tutor o miembro del tribunal evaluador designado en un trámite para revisar el documento del proyecto | |
| Rol | Función general que una cuenta de usuario tiene en el sistema, una por cuenta: estudiante, docente, docente de Taller de Grado, secretario(a) de dirección de carrera, director de carrera o administrador | `Role` |
| Secretario(a) de dirección de carrera | Personal de la Dirección de Carrera que recibe los documentos del trámite y lleva el expediente de titulación | `Role.ADMINISTRATIVE` (etiqueta `administrativo`) |
| Semestre | Periodo académico en que se divide el año, escrito año y periodo (1 o 2), por ejemplo `2026-2`; un estudiante abre como máximo un trámite de titulación por semestre | `Semester` |
| Servicio de correo | Servicio de correo electrónico por el que el sistema recibe solicitudes y envía respuestas y notificaciones | `EmailInbox` (recepción), `EmailSender` (envío) |
| Solicitud de titulación | Pedido del estudiante para iniciar el trámite de titulación, que acompaña a los requisitos de apertura | |
| Título del proyecto | Nombre del proyecto de grado que el estudiante indica al presentar su perfil de proyecto; un perfil de proyecto nuevo puede cambiarlo | `DegreeProject.title` |
| Trámite de titulación | Proceso que sigue un estudiante desde su solicitud de titulación hasta el archivo de su expediente | `DegreeProcedure` |
| Tribunal evaluador | Grupo de tres docentes que revisa el documento del proyecto y evalúa la predefensa y la defensa final de un estudiante | |
| Tutor | Docente que guía al estudiante en su proyecto y revisa el documento del proyecto junto al tribunal evaluador | `DegreeProject.tutorId` referencia su cuenta `User` |
| Usuario | Persona con una cuenta de usuario en el sistema | |
| Versión del documento | Cada entrega sucesiva del documento del proyecto, con un comentario general del estudiante y sus observaciones; la última entregada es la versión vigente | |
| Versión para la defensa final | Versión del documento del proyecto que el estudiante entrega después de aprobar la predefensa, y que el tutor y el tribunal evaluador revisan antes de la defensa final | |
| Visto bueno | Aprobación formal que emite un docente sobre el documento del proyecto. Para el perfil de proyecto se dice que está aprobado (ver Estado del perfil) | |
