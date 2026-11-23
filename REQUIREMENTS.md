# Requirements

## Functional Requirements

| ID | Requisito | Verificación |
| --- | --- | --- |
| FR-001 | El sistema debe registrar la solicitud de titulación de un estudiante y abrir su expediente de titulación | |
| FR-002 | El sistema debe registrar los requisitos de apertura del estudiante: carta de solicitud, hoja de vida, fotocopia de cédula de identidad y boleta de inscripción con Modalidad de Graduación | |
| FR-003 | El sistema debe registrar el tutor que el director de carrera asigna al estudiante | |
| FR-004 | El sistema debe registrar el perfil de proyecto, con el título del proyecto, que presenta el estudiante | |
| FR-005 | El sistema debe registrar la aprobación del perfil de proyecto emitida por el docente de Taller de Grado | |
| FR-006 | El sistema debe registrar los tres docentes que el director de carrera designa como tribunal evaluador del estudiante | |
| FR-007 | El sistema debe notificar a cada miembro del tribunal su designación | |
| FR-008 | El sistema debe registrar la entrega del documento del proyecto por el estudiante para su revisión | |
| FR-009 | El sistema debe registrar la revisión del tutor y de cada miembro del tribunal sobre cada versión del documento del proyecto: aprobada u observada, con sus observaciones en texto y un PDF anotado opcional | |
| FR-010 | El sistema debe registrar cada versión corregida del documento del proyecto con un comentario general del estudiante y conservar el historial de versiones | |
| FR-011 | El sistema debe registrar la aprobación del documento del proyecto por cada miembro del tribunal y por el tutor | |
| FR-012 | El sistema debe registrar la fecha, hora y lugar de la predefensa y revisión de prototipo que fija el director de carrera | |
| FR-013 | El sistema debe notificar al estudiante, al tutor y al tribunal evaluador la programación de la predefensa | |
| FR-014 | El sistema debe registrar el resultado de la predefensa y las observaciones finales que el estudiante debe subsanar antes de la defensa final | |
| FR-015 | El sistema debe registrar la fecha, hora y aula de la defensa final que fija el director de carrera, una vez aprobadas la predefensa y la versión para la defensa final | |
| FR-016 | El sistema debe registrar la calificación final asignada por el tribunal evaluador | |
| FR-017 | El sistema debe registrar la recepción de los dos ejemplares físicos del proyecto, uno para la biblioteca y otro para la Dirección de Carrera | |
| FR-018 | El sistema debe cerrar y archivar el expediente de titulación del graduado | |
| FR-019 | El sistema debe mostrar la etapa en la que se encuentra un trámite de titulación al estudiante, a los docentes que participan en él, al secretario(a) y al director de carrera | |
| FR-020 | El sistema debe notificar al estudiante y al docente designado la designación del tutor | |
| FR-021 | El sistema debe notificar al estudiante la designación de cada miembro de su tribunal evaluador | |
| FR-022 | El sistema debe registrar el rechazo o las observaciones del docente de Taller de Grado al perfil de proyecto | |
| FR-023 | El sistema debe registrar un nuevo perfil de proyecto cuando el anterior fue rechazado u observado | |
| FR-025 | El sistema debe registrar la entrega del documento del proyecto para la defensa final | |
| FR-026 | El sistema debe crear cuentas de usuario con nombre completo, dirección de correo y un rol, y para un estudiante además registro universitario, cédula de identidad y teléfono | |
| FR-027 | El sistema debe mostrar el calendario académico con las predefensas y defensas finales programadas | |
| FR-028 | El sistema debe mostrar un dashboard con indicadores de los trámites de titulación | |
| FR-029 | El sistema debe generar reportes y estadísticas de titulación | |
| FR-030 | El sistema debe mostrar la lista de las cuentas de usuario de un rol, con sus datos y su estado | |
| FR-031 | El sistema debe modificar los datos y los roles de una cuenta de usuario | |
| FR-032 | El sistema debe desactivar y reactivar cuentas de usuario; una cuenta desactivada no puede usar el sistema y conserva su historial | |
| FR-033 | El sistema debe rechazar una cuenta de usuario cuya dirección de correo o registro universitario ya pertenece a otra | |
| FR-034 | El sistema debe advertir a quien desactiva una cuenta de usuario, o le quita un rol, cuando esa cuenta participa en trámites en curso | |
| FR-035 | El sistema debe notificar al usuario por correo todo cambio en su cuenta de usuario: su creación, la modificación de sus datos o de su rol, su desactivación y su reactivación | |
| FR-036 | El sistema debe asignar un solo rol a cada cuenta de usuario | |
| FR-037 | El sistema debe permitir que cada usuario cargue o cambie la foto de perfil de su propia cuenta de usuario | |
| FR-038 | El sistema debe rechazar que se desactive, o que pierda el rol de administrador, la única cuenta de administrador activa | |
| FR-040 | El sistema debe admitir una versión nueva del documento del proyecto solo cuando el tutor observó la versión vigente, o cuando todos los miembros del tribunal la revisaron y al menos uno registró observaciones | |
| FR-041 | El sistema debe notificar al tutor cada versión entregada del documento del proyecto | |
| FR-043 | El sistema debe registrar la revisión y aprobación de la versión para la defensa final por el tutor y por los miembros del tribunal evaluador | |
| FR-044 | El sistema debe pasar una versión del documento del proyecto a los miembros del tribunal y notificarlos solo cuando el tutor la aprueba | |
| FR-045 | El sistema debe admitir una sola revisión por revisor sobre cada versión del documento del proyecto | |
| FR-046 | El sistema debe notificar al estudiante la revisión del tutor sobre cada versión: aprobada, cuando pasa al tribunal, u observada, con sus observaciones | |
| FR-047 | El sistema debe notificar al estudiante el resultado y las observaciones de cada miembro del tribunal cuando los tres revisaron una versión | |
| FR-048 | El sistema debe notificar al estudiante, al secretario(a) de dirección de carrera y al director de carrera cuando el documento del proyecto queda aprobado | |
| FR-049 | El sistema debe permitir que cada usuario modifique la dirección de correo y el teléfono de su propia cuenta de usuario | |
| FR-050 | El sistema debe permitir que el director de carrera gestione las cuentas de usuario cuyos roles son docente, docente de Taller de Grado, secretario(a) de dirección de carrera o estudiante | |
| FR-051 | El sistema debe crear la cuenta de usuario del estudiante cuando la pide el secretario(a) de dirección de carrera, el director de carrera o el administrador, y cuando el secretario(a) abre su trámite de titulación, una vez verificados sus requisitos de apertura | |
| FR-052 | El sistema debe mostrar el registro de cambios al administrador, al director de carrera y al secretario(a) de dirección de carrera, a cada uno dentro de su alcance | |
| FR-053 | El sistema debe permitir que solo el administrador asigne o quite los roles de director de carrera y de administrador, y gestione las cuentas de usuario que los tienen | |
| FR-054 | El sistema debe notificar al estudiante por correo la apertura y el cierre de su trámite de titulación | |
| FR-055 | El sistema debe permitir que el secretario(a) de dirección de carrera reemplace el documento de un requisito de apertura de un trámite de titulación en curso | |
| FR-056 | El sistema debe abrir el trámite de titulación sobre la cuenta de usuario existente del estudiante, y rechazar la apertura si esa cuenta está desactivada o ya tiene un trámite de titulación en curso | |
| FR-057 | El sistema debe notificar al docente de Taller de Grado cada perfil de proyecto que presenta un estudiante | |
| FR-058 | El sistema debe admitir un perfil de proyecto solo cuando el estudiante no tiene uno o su último perfil de proyecto fue rechazado u observado | |
| FR-059 | El sistema debe mostrar al estudiante su proyecto de grado: el título del proyecto, sus perfiles de proyecto y las versiones del documento del proyecto, con el estado de cada uno | |
| FR-060 | El sistema debe notificar al estudiante cada evaluación de su perfil de proyecto: aprobado, observado o rechazado, con las observaciones del docente de Taller de Grado | |
| FR-061 | El sistema debe notificar al director de carrera cada perfil de proyecto aprobado | |
| FR-062 | El sistema debe rechazar la apertura de un trámite de titulación cuando el estudiante ya tuvo otro trámite de titulación en el mismo semestre, aunque esté cerrado | |
| FR-063 | El sistema debe notificar al estudiante, al tutor y al tribunal evaluador la programación de la defensa final | |
| FR-064 | El sistema debe notificar al estudiante el resultado de la predefensa, con las observaciones del tribunal, y la calificación final | |
| FR-065 | El sistema debe permitir que el director de carrera reprograme una predefensa o defensa final que todavía no tiene resultado, y notificar el cambio al estudiante, al tutor y al tribunal evaluador | |

### Notes

- Priority: la escala es MoSCoW. **Must**: sin esto la prueba de concepto por correo no lleva un trámite de titulación de la apertura al cierre; incluye las notificaciones que hacen actuar al siguiente actor, porque en la versión por correo son la interfaz. **Should**: importante, pero el flujo funciona sin ello. **Could**: lo primero que se recorta; además tiene reglas sin definir. La prioridad no recorta el alcance de la primera entrega (los diez casos de uso, SRC-011): ordena el trabajo de E2 y C1 y define qué se recorta primero, con acuerdo del docente, si se cumple RSK-005 (2026-11-07).
- FR-008, FR-040: el estudiante puede entregar la primera versión desde que su perfil de proyecto está aprobado y su trámite tiene tutor asignado y los tres miembros del tribunal designados; la revisión no empieza con el tribunal incompleto (acordado el 2026-11-23, reemplaza lo acordado en SRC-010).
- FR-040, FR-044: cada versión se revisa en dos etapas. Primero la revisa el tutor, como filtro previo; si la observa, el estudiante entrega otra sin que el tribunal la vea. Si la aprueba, pasa a los miembros del tribunal evaluador.
- FR-004, FR-023, FR-058: el estudiante puede presentar el perfil de proyecto desde que el secretario(a) abre su trámite de titulación. El primer perfil crea el proyecto de grado. Si el docente de Taller de Grado lo rechaza u observa, el estudiante presenta un perfil nuevo, que puede cambiar el título del proyecto; los anteriores se conservan (SRC-010).
- FR-025, FR-043: la versión para la defensa final se entrega después de aprobar la predefensa y se revisa en las mismas dos etapas.
- FR-026, FR-036: cada cuenta de usuario tiene un solo rol (decisión de diseño del 2026-11-20, que reemplaza las combinaciones de roles acordadas en SRC-007). Lo que puede hacer cada rol lo definen los permisos del sistema: el director de carrera actúa también como docente porque los permisos de docente incluyen al director, y como cualquier docente puede ser designado tutor o miembro del tribunal (SRC-007). Quien administra y además participa en el trámite usa dos cuentas de usuario distintas. Docente de Taller de Grado es un rol de cuenta: la carrera tiene uno solo y revisa todos los perfiles de proyecto, pero el sistema no impide que haya más de una cuenta activa con ese rol (acordado el 2026-11-23). Ser tutor o miembro del tribunal sale de las designaciones de cada trámite, no de la cuenta de usuario.
- FR-050, FR-051, FR-053: el director de carrera crea cuentas de usuario de docentes, del docente de Taller de Grado, de secretarios(as) y de estudiantes; el secretario(a) crea y modifica cuentas de estudiantes, pero no las desactiva ni reactiva; el administrador crea cuentas de todos los roles y es el único que asigna los roles de director de carrera y de administrador (SRC-008; ampliado el 2026-11-20).
- FR-035: en la versión por correo, la creación de la cuenta de usuario solo se notifica por correo: el usuario se identifica por su dirección de correo. En la futura versión web, la notificación incluirá una contraseña de un solo uso que el usuario debe cambiar en su primer inicio de sesión (SRC-008).
- FR-037: la foto de perfil es opcional y solo la carga el propio usuario.
- FR-001 a FR-019 vienen de RF01 a RF19 de SRC-002, con el mismo número. Se ajustó su texto según lo acordado con el equipo de desarrollo el 2026-10-26:
  - Cada acción la registra quien la realiza: el estudiante presenta el perfil (FR-004) y entrega el documento (FR-008); el docente de Taller de Grado registra su aprobación (FR-005); el tutor y el tribunal registran sus observaciones (FR-009). Las observaciones solo se registran en el sistema: no se aceptan por carta, a diferencia de lo que decían RF09 y SRC-001.
  - El director de carrera fija las fechas de la predefensa y la defensa final (FR-012, FR-015), como dice SRC-001.
  - El tutor revisa y aprueba el documento junto al tribunal (FR-009, FR-011), como dice SRC-001.
- FR-002: SRC-001 incluye la carta de solicitud entre los requisitos de apertura; RF02 la omitía. El estudiante entrega los requisitos de apertura en la oficina; el secretario(a) verifica que la carpeta esté completa, abre el trámite de titulación y crea la cuenta de usuario del estudiante (FR-051, SRC-007).
- FR-002, FR-055: el secretario(a) digitaliza los requisitos de apertura y los adjunta al abrir el trámite; el sistema los guarda en el expediente de titulación y solo admite reemplazarlos mientras el trámite está en curso (SRC-009).
- FR-051, FR-056: los datos del estudiante que registra el secretario(a) son los de su cuenta de usuario (FR-026); el trámite no agrega datos propios. La cuenta del estudiante se crea con la gestión de usuarios (UC-001), con sus reglas y su aviso por correo (FR-033, FR-035), y el trámite se abre después sobre esa cuenta, identificada por su registro universitario. Si ninguna cuenta tiene ese registro, la apertura se rechaza (SRC-009; acordado el 2026-11-23).
- FR-001, FR-056, FR-062: un estudiante abre como máximo un trámite de titulación por semestre, aunque el anterior del mismo semestre esté cerrado. El trámite se identifica por su referencia, formada por el semestre en que se abre y el registro universitario del estudiante (acordado el 2026-11-23).
- FR-017, FR-018: la recepción de los dos ejemplares físicos se registra en una sola solicitud, solo después de aprobar la defensa final (FR-016), y en el mismo paso el sistema cierra el trámite y archiva el expediente de titulación. La cuenta de usuario del estudiante sigue activa, para que pueda consultar su trámite archivado (UC-007) (SRC-009).
- FR-012 a FR-016, FR-063 a FR-065: el director de carrera programa la predefensa y la defensa final y registra sus resultados, que decide el tribunal evaluador. Una evaluación se programa en una fecha y hora futuras, sin que ningún miembro del tribunal tenga otra evaluación a la misma fecha y hora. El resultado de la predefensa siempre lleva observaciones; si es reprobada, el trámite se cierra (FR-054) y el estudiante vuelve a inscribir la Modalidad de Graduación en un semestre siguiente y abre un trámite nuevo (FR-062). La calificación final va de 0 a 100 y la defensa final se aprueba con 51 o más; si es reprobada, el trámite se cierra igual que con la predefensa reprobada. La emisión del acta de graduación está fuera del alcance (acordado el 2026-11-23).
- FR-003, FR-006: el tutor y los miembros del tribunal se designan después de la aprobación del perfil (FR-005), por separado y en cualquier orden; cada miembro del tribunal se designa en su propia solicitud hasta completar tres docentes distintos. En un mismo trámite el tutor no puede ser miembro del tribunal; el docente de Taller de Grado sí puede ser tutor o miembro del tribunal. Una designación no se cambia: la reasignación no está contemplada (acordado el 2026-11-23).
- FR-022, FR-060: el docente de Taller de Grado observa el perfil cuando pide cambios sobre el mismo tema, por ejemplo de forma, y lo rechaza cuando el tema debe cambiar. En ambos casos el estudiante presenta una versión nueva, cuyo título puede cambiar; si el tema cambió lo juzga el docente, no el sistema (acordado el 2026-11-23).
- FR-061: el aviso al director de carrera no condiciona la aprobación: si no hay un director activo, el perfil queda aprobado igual (acordado el 2026-11-23).
- FR-011: el documento queda apto para la predefensa solo cuando lo aprueban los tres miembros del tribunal y el tutor.
- SRC-007: los casos de uso se reestructuraron el 2026-10-27, numerados de nuevo desde UC-001. El detalle acordado en SRC-004, SRC-005 y SRC-006 corresponde a casos de uso anteriores y se vuelve a detallar en los nuevos.
- NFR-IN-001, CON-006: el servidor de correo es infraestructura, no un actor. Los destinatarios de las notificaciones son los usuarios; cómo se envían se resuelve en el diseño.

## Requisitos no Funcionales

### Usabilidad

| ID | Requisito | Verificación |
| --- | --- | --- |
| NFR-001 | El sistema entrega mensajes de error con validación de parámetros de entrada detallada. | |

### Seguridad y privacidad

| ID | Requisito | Verificación |
| --- | --- | --- |
| NFR-002 | Cada usuario solo consulta y modifica la información que su rol le permite, y solo de los trámites en los que participa | |
| NFR-SE-002 | Cada cambio en el sistema queda registrado con quién lo hizo, cuándo y sobre qué dato | |

### Interfaces

| ID | Requisito | Verificación |
| --- | --- | --- |
| NFR-003 | El sistema recibe las solicitudes de los usuarios y les envía respuestas y notificaciones por correo electrónico. Cada solicitud es un comando que el usuario escribe en el asunto del correo con la forma de una llamada a función, `Comando[arg1; arg2; …]`, con los argumentos separados por punto y coma. Los archivos se envían como adjuntos del correo, sin nombrarlos en el comando: el sistema reconoce qué documento es cada adjunto por una palabra de su nombre de archivo. El texto libre de una solicitud, como las observaciones, se escribe en el cuerpo del correo. | |

### Restricciones

| ID | Restricción | Verificación |
| --- | --- | --- |
| NFR-004 | El sistema debe estar implementado en Java. | |
| NFR-005 | Los documentos en formato PDF recibidos por el sistema deben tener un tamaño máximo de 25MB. | |
| NFR-006 | Las imágenes en formato PNG o JPG/JPEG recibidas por el sistema no deben superar un tamaño máximo de 1MB. | |
| NFR-007 | El sistema usa el servidor de correo provisto por el docente de Tecnología Web, `mail.tecnoweb.org.bo`: recibe por POP3 y envía por SMTP, en los puertos por defecto (110 y 25), sin cifrado. El SMTP no pide contraseña y valida al remitente por su dirección IP. | |
| NFR-008 | El sistema usa la base de datos PostgreSQL provista por el docente de Tecnología Web: base `db_grupo03sc` en el servidor `db.tecnoweb.org.bo`, puerto por defecto (5432), con el usuario y la contraseña que entrega el docente | |
