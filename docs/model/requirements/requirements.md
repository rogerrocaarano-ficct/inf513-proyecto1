# Requirements

## Sources

| ID | Source | Type | Date |
| --- | --- | --- | --- |
| SRC-001 | Entrevista con el secretario de dirección de carrera ([docs/sources/2026-09-13.md](../../sources/2026-09-13.md)) | Interview | 2026-09-13 |
| SRC-002 | Requisitos funcionales del equipo de desarrollo y casos de uso aprobados por el docente de Tecnología Web ([docs/sources/2026-10-06.md](../../sources/2026-10-06.md)) | Document | 2026-10-06 |
| SRC-003 | Restricciones y requisitos de calidad indicados por el equipo de desarrollo | Sesión en I1 | 2026-10-26 |
| SRC-004 | Detalle del caso de uso Administrar usuarios, anterior a SRC-007, acordado con el equipo de desarrollo | Workshop | 2026-10-26 |
| SRC-005 | Detalle del caso de uso Entregar documento del proyecto, anterior a SRC-007, acordado con el equipo de desarrollo | Workshop | 2026-10-26 |
| SRC-006 | Detalle del caso de uso Revisar documento del proyecto, anterior a SRC-007, acordado con el equipo de desarrollo | Workshop | 2026-10-26 |
| SRC-007 | Reestructuración de los casos de uso acordada con el equipo de desarrollo ([docs/sources/2026-10-06.md](../../sources/2026-10-06.md)) | Workshop | 2026-10-27 |
| SRC-008 | Detalle del caso de uso Gestionar usuarios acordado con el equipo de desarrollo | Workshop | 2026-10-27 |
| SRC-009 | Detalle del caso de uso Gestionar trámite de titulación acordado con el equipo de desarrollo | Workshop | 2026-11-03 |
| SRC-010 | Detalle del caso de uso Gestionar proyecto de grado acordado con el equipo de desarrollo | Workshop | 2026-11-03 |
| SRC-011 | Estado de la gestión del proyecto (fechas de entrega, equipo y gestiones con el docente) informado por el equipo de desarrollo | Interview | 2026-11-03 |

## Functional Requirements

| ID | Requirement | Use case | Priority | Source | Status |
| --- | --- | --- | --- | --- | --- |
| FR-001 | El sistema debe registrar la solicitud de titulación de un estudiante y abrir su expediente de titulación | UC-003 Gestionar trámite de titulación | Must | SRC-002 | Candidate |
| FR-002 | El sistema debe registrar los documentos iniciales del estudiante: carta de solicitud, hoja de vida, fotocopia de cédula de identidad y boleta de inscripción con Modalidad de Graduación | UC-003 Gestionar trámite de titulación | Must | SRC-001, SRC-002 | Candidate |
| FR-003 | El sistema debe registrar el tutor que el director de carrera asigna al estudiante | UC-005 Gestionar tribunal evaluador y tutor | Must | SRC-001, SRC-002 | Candidate |
| FR-004 | El sistema debe registrar el perfil de proyecto, con el título del proyecto, que presenta el estudiante | UC-004 Gestionar proyecto de grado | Must | SRC-001, SRC-002, SRC-010 | Candidate |
| FR-005 | El sistema debe registrar el visto bueno del perfil de proyecto emitido por el docente de Taller de Grado | UC-006 Evaluar proyecto de grado | Must | SRC-001, SRC-002 | Candidate |
| FR-006 | El sistema debe registrar los tres docentes que el director de carrera designa como tribunal evaluador del estudiante | UC-005 Gestionar tribunal evaluador y tutor | Must | SRC-001, SRC-002 | Candidate |
| FR-007 | El sistema debe notificar a cada miembro del tribunal su designación | UC-005 Gestionar tribunal evaluador y tutor | Must | SRC-002 | Candidate |
| FR-008 | El sistema debe registrar la entrega del documento del proyecto por el estudiante para su revisión | UC-004 Gestionar proyecto de grado | Must | SRC-001, SRC-002 | Candidate |
| FR-009 | El sistema debe registrar la revisión del tutor y de cada miembro del tribunal sobre cada versión del documento del proyecto: aprobada u observada, con sus observaciones en texto y un PDF anotado opcional | UC-006 Evaluar proyecto de grado | Must | SRC-001, SRC-002, SRC-006 | Candidate |
| FR-010 | El sistema debe registrar cada versión corregida del documento del proyecto con un comentario general del estudiante y conservar el historial de versiones | UC-004 Gestionar proyecto de grado | Must | SRC-001, SRC-002, SRC-005 | Candidate |
| FR-011 | El sistema debe registrar la aprobación del documento del proyecto por cada miembro del tribunal y por el tutor | UC-006 Evaluar proyecto de grado | Must | SRC-001, SRC-002 | Candidate |
| FR-012 | El sistema debe registrar la fecha, hora y lugar de la predefensa y revisión de prototipo que fija el director de carrera | UC-007 Gestionar cronograma de defensas | Must | SRC-001, SRC-002 | Candidate |
| FR-013 | El sistema debe notificar al estudiante y al tribunal evaluador la programación de la predefensa | UC-007 Gestionar cronograma de defensas | Must | SRC-001, SRC-002 | Candidate |
| FR-014 | El sistema debe registrar el resultado de la predefensa y las observaciones finales que el estudiante debe subsanar antes de la defensa final | UC-006 Evaluar proyecto de grado | Must | SRC-001, SRC-002 | Candidate |
| FR-015 | El sistema debe registrar la fecha, hora y aula de la defensa final que fija el director de carrera, una vez aprobada la predefensa | UC-007 Gestionar cronograma de defensas | Must | SRC-001, SRC-002 | Candidate |
| FR-016 | El sistema debe registrar la calificación final asignada por el tribunal evaluador y emitir los datos para el acta de graduación | UC-006 Evaluar proyecto de grado | Must | SRC-001, SRC-002 | Candidate |
| FR-017 | El sistema debe registrar la recepción de los dos ejemplares físicos del proyecto, uno para la biblioteca y otro para la Dirección de Carrera | UC-003 Gestionar trámite de titulación | Must | SRC-001, SRC-002 | Candidate |
| FR-018 | El sistema debe cerrar y archivar el expediente de titulación del graduado | UC-003 Gestionar trámite de titulación | Must | SRC-002 | Candidate |
| FR-019 | El sistema debe mostrar la etapa en la que se encuentra un trámite de titulación al estudiante, a los docentes que participan en él, al secretario(a) y al director de carrera | UC-008 Consultar trámite de titulación | Must | SRC-002 | Candidate |
| FR-020 | El sistema debe notificar al estudiante la designación de su tutor | UC-005 Gestionar tribunal evaluador y tutor | Must | SRC-001 | Candidate |
| FR-021 | El sistema debe notificar al estudiante la designación de su tribunal evaluador | UC-005 Gestionar tribunal evaluador y tutor | Must | SRC-001 | Candidate |
| FR-022 | El sistema debe registrar el rechazo o las observaciones del docente de Taller de Grado al perfil de proyecto | UC-006 Evaluar proyecto de grado | Must | SRC-001 | Candidate |
| FR-023 | El sistema debe registrar un nuevo perfil de proyecto cuando el anterior fue rechazado u observado | UC-004 Gestionar proyecto de grado | Must | SRC-001 | Candidate |
| FR-024 | El sistema debe controlar el plazo de revisión del tutor y de cada miembro del tribunal sobre cada versión del documento del proyecto | UC-006 Evaluar proyecto de grado | Must | SRC-002, SRC-006 | Candidate |
| FR-025 | El sistema debe registrar la entrega del documento del proyecto para la defensa final | UC-004 Gestionar proyecto de grado | Must | SRC-001 | Candidate |
| FR-026 | El sistema debe crear cuentas de usuario con nombre completo, dirección de correo y uno o más roles, y para un estudiante además registro universitario, cédula de identidad y teléfono | UC-001 Gestionar usuarios | Must | SRC-002, SRC-004 | Candidate |
| FR-027 | El sistema debe mostrar el calendario académico con las predefensas y defensas finales programadas | UC-008 Consultar trámite de titulación | Must | SRC-002 | Candidate |
| FR-028 | El sistema debe mostrar un dashboard con indicadores de los trámites de titulación | UC-010 Consultar estadísticas | Must | SRC-002 | Candidate |
| FR-029 | El sistema debe generar reportes y estadísticas de titulación | UC-010 Consultar estadísticas | Must | SRC-002 | Candidate |
| FR-030 | El sistema debe mostrar los datos de una cuenta de usuario y la lista de cuentas de usuario con sus roles y su estado | UC-001 Gestionar usuarios | Must | SRC-004 | Candidate |
| FR-031 | El sistema debe modificar los datos y los roles de una cuenta de usuario | UC-001 Gestionar usuarios | Must | SRC-004 | Candidate |
| FR-032 | El sistema debe desactivar y reactivar cuentas de usuario; una cuenta desactivada no puede usar el sistema y conserva su historial | UC-001 Gestionar usuarios | Must | SRC-004 | Candidate |
| FR-033 | El sistema debe rechazar una cuenta de usuario cuya dirección de correo o registro universitario ya pertenece a otra | UC-001 Gestionar usuarios | Must | SRC-004 | Candidate |
| FR-034 | El sistema debe advertir al administrador cuando la cuenta de usuario que desactiva, o el rol que quita, participa en trámites en curso, y dejar esos trámites pendientes de reasignación | UC-001 Gestionar usuarios | Must | SRC-004 | Candidate |
| FR-035 | El sistema debe notificar al usuario por correo la creación, desactivación y reactivación de su cuenta de usuario y los cambios de sus roles | UC-001 Gestionar usuarios | Must | SRC-004 | Candidate |
| FR-036 | El sistema debe rechazar las combinaciones de roles no permitidas: el estudiante, el secretario(a) de dirección de carrera y el administrador tienen un solo rol, y el docente de Taller de Grado solo se combina con el rol de docente | UC-001 Gestionar usuarios | Must | SRC-004 | Candidate |
| FR-037 | El sistema debe permitir que cada usuario cargue o cambie la foto de perfil de su propia cuenta de usuario | UC-002 Gestionar datos personales | Must | SRC-004 | Candidate |
| FR-038 | El sistema debe rechazar que se desactive, o que pierda el rol de administrador, la única cuenta de administrador activa | UC-001 Gestionar usuarios | Must | SRC-004 | Candidate |
| FR-039 | El sistema debe rechazar que una segunda cuenta de usuario activa tenga el rol de docente de Taller de Grado | UC-001 Gestionar usuarios | Must | SRC-004 | Candidate |
| FR-040 | El sistema debe admitir una versión nueva del documento del proyecto solo cuando el tutor observó la versión vigente, o cuando todos los miembros del tribunal la revisaron y al menos uno registró observaciones | UC-004 Gestionar proyecto de grado | Must | SRC-005 | Candidate |
| FR-041 | El sistema debe notificar al tutor cada versión entregada del documento del proyecto | UC-004 Gestionar proyecto de grado | Must | SRC-005 | Candidate |
| FR-042 | El sistema debe notificar a los miembros del tribunal recién designados cuando hay una versión vigente del documento del proyecto aprobada por el tutor para revisar | UC-005 Gestionar tribunal evaluador y tutor | Must | SRC-005 | Candidate |
| FR-043 | El sistema debe registrar la revisión y aprobación de la versión para la defensa final por el tutor y por los miembros del tribunal evaluador | UC-006 Evaluar proyecto de grado | Must | SRC-005 | Candidate |
| FR-044 | El sistema debe pasar una versión del documento del proyecto a los miembros del tribunal y notificarlos solo cuando el tutor la aprueba | UC-006 Evaluar proyecto de grado | Must | SRC-005 | Candidate |
| FR-045 | El sistema debe admitir una sola revisión por revisor sobre cada versión del documento del proyecto | UC-006 Evaluar proyecto de grado | Must | SRC-006 | Candidate |
| FR-046 | El sistema debe notificar al estudiante las observaciones del tutor cuando este observa una versión | UC-006 Evaluar proyecto de grado | Must | SRC-006 | Candidate |
| FR-047 | El sistema debe notificar al estudiante el resultado y las observaciones de cada miembro del tribunal cuando los tres revisaron una versión | UC-006 Evaluar proyecto de grado | Must | SRC-006 | Candidate |
| FR-048 | El sistema debe notificar al estudiante, al secretario(a) de dirección de carrera y al director de carrera cuando el documento del proyecto queda aprobado | UC-006 Evaluar proyecto de grado | Must | SRC-006 | Candidate |
| FR-049 | El sistema debe permitir que cada usuario modifique la dirección de correo y el teléfono de su propia cuenta de usuario | UC-002 Gestionar datos personales | Must | SRC-007 | Candidate |
| FR-050 | El sistema debe permitir que el director de carrera gestione las cuentas de usuario cuyos roles son docente, docente de Taller de Grado o secretario(a) de dirección de carrera | UC-001 Gestionar usuarios | Must | SRC-007, SRC-008 | Candidate |
| FR-051 | El sistema debe crear la cuenta de usuario del estudiante cuando el secretario(a) de dirección de carrera abre su trámite de titulación, una vez verificados sus documentos iniciales | UC-003 Gestionar trámite de titulación | Must | SRC-007 | Candidate |
| FR-052 | El sistema debe mostrar el registro de cambios al administrador, al director de carrera y al secretario(a) de dirección de carrera, a cada uno dentro de su alcance | UC-009 Consultar registro de cambios | Must | SRC-007 | Candidate |
| FR-053 | El sistema debe permitir que solo el administrador asigne o quite los roles de director de carrera y de administrador, y gestione las cuentas de usuario que los tienen | UC-001 Gestionar usuarios | Must | SRC-008 | Candidate |
| FR-054 | El sistema debe notificar al estudiante por correo la apertura y el cierre de su trámite de titulación | UC-003 Gestionar trámite de titulación | Must | SRC-009 | Candidate |
| FR-055 | El sistema debe permitir que el secretario(a) de dirección de carrera reemplace un documento inicial de un trámite de titulación en curso | UC-003 Gestionar trámite de titulación | Must | SRC-009 | Candidate |
| FR-056 | El sistema debe abrir el trámite de titulación sobre la cuenta de usuario existente del estudiante, y rechazar la apertura si esa cuenta está desactivada o ya tiene un trámite de titulación en curso | UC-003 Gestionar trámite de titulación | Must | SRC-009 | Candidate |
| FR-057 | El sistema debe notificar al docente de Taller de Grado cada perfil de proyecto que presenta un estudiante | UC-004 Gestionar proyecto de grado | Must | SRC-010 | Candidate |
| FR-058 | El sistema debe admitir un perfil de proyecto solo cuando el estudiante no tiene uno o su último perfil de proyecto fue rechazado u observado | UC-004 Gestionar proyecto de grado | Must | SRC-010 | Candidate |
| FR-059 | El sistema debe mostrar al estudiante su proyecto de grado: el título del proyecto, sus perfiles de proyecto y las versiones del documento del proyecto, con el estado de cada uno | UC-004 Gestionar proyecto de grado | Must | SRC-010 | Candidate |

### Notes

- FR-008, FR-040: el estudiante puede entregar la primera versión desde que su perfil de proyecto tiene visto bueno y tiene tutor asignado (SRC-010). Si el tribunal se designa después, revisa la versión vigente que el tutor ya aprobó (FR-042).
- FR-040, FR-044: cada versión se revisa en dos etapas. Primero la revisa el tutor, como filtro previo; si la observa, el estudiante entrega otra sin que el tribunal la vea. Si la aprueba, pasa a los miembros del tribunal evaluador.
- FR-004, FR-023, FR-058: el estudiante puede presentar el perfil de proyecto desde que el secretario(a) abre su trámite de titulación. El primer perfil crea el proyecto de grado. Si el docente de Taller de Grado lo rechaza u observa, el estudiante presenta un perfil nuevo, que puede cambiar el título del proyecto; los anteriores se conservan (SRC-010).
- FR-025, FR-043: la versión para la defensa final se entrega después de aprobar la predefensa y se revisa en las mismas dos etapas.
- FR-026, FR-036: una cuenta de usuario puede tener varios roles. El director de carrera es también docente: su cuenta de usuario tiene siempre los dos roles, y como cualquier docente puede ser designado tutor o miembro del tribunal (SRC-007). El estudiante, el secretario(a) y el administrador tienen un solo rol: quien administra y además participa en el trámite usa dos cuentas de usuario distintas. Docente de Taller de Grado es un rol de cuenta: la carrera tiene uno solo y revisa todos los perfiles de proyecto (FR-039). Ser tutor o miembro del tribunal sale de las designaciones de cada trámite, no de la cuenta de usuario.
- FR-050, FR-053: el director de carrera gestiona las cuentas de usuario de docentes, del docente de Taller de Grado y de secretarios(as), y puede asignar esos roles; el administrador gestiona todas las cuentas de usuario y es el único que asigna el rol de director de carrera (SRC-008).
- FR-035: en la versión por correo, la creación de la cuenta de usuario solo se notifica por correo: el usuario se identifica por su dirección de correo. En la futura versión web, la notificación incluirá una contraseña de un solo uso que el usuario debe cambiar en su primer inicio de sesión (SRC-008).
- FR-037: la foto de perfil es opcional y solo la carga el propio usuario.
- FR-001 a FR-019 vienen de RF01 a RF19 de SRC-002, con el mismo número. Se ajustó su texto según lo acordado con el equipo de desarrollo el 2026-10-26:
  - Cada acción la registra quien la realiza: el estudiante presenta el perfil (FR-004) y entrega el documento (FR-008); el docente de Taller de Grado registra su visto bueno (FR-005); el tutor y el tribunal registran sus observaciones (FR-009). Las observaciones solo se registran en el sistema: no se aceptan por carta, a diferencia de lo que decían RF09 y SRC-001.
  - El director de carrera fija las fechas de la predefensa y la defensa final (FR-012, FR-015), como dice SRC-001.
  - El tutor revisa y aprueba el documento junto al tribunal (FR-009, FR-011), como dice SRC-001.
- FR-002: SRC-001 incluye la carta de solicitud entre los documentos iniciales; RF02 la omitía. El estudiante entrega los documentos iniciales en la oficina; el secretario(a) verifica que la carpeta esté completa, abre el trámite de titulación y crea la cuenta de usuario del estudiante (FR-051, SRC-007).
- FR-002, FR-055: el secretario(a) digitaliza los documentos iniciales y los adjunta al abrir el trámite; el sistema los guarda en el expediente de titulación y solo admite reemplazarlos mientras el trámite está en curso (SRC-009).
- FR-051, FR-056: los datos del estudiante que registra el secretario(a) son los de su cuenta de usuario (FR-026); el trámite no agrega datos propios. Si la dirección de correo y el registro universitario ya pertenecen a una misma cuenta de usuario activa de estudiante, el trámite se abre sobre ella sin crear otra. La cuenta nueva se crea con las mismas reglas y el mismo aviso por correo que en UC-001 (FR-033, FR-035), pero sin el alcance de FR-050 y FR-053: la autoriza la apertura del trámite (SRC-009).
- FR-017, FR-018: la recepción de los dos ejemplares físicos se registra en una sola solicitud, solo después de la calificación final (FR-016), y en el mismo paso el sistema cierra el trámite y archiva el expediente de titulación. La cuenta de usuario del estudiante sigue activa, para que pueda consultar su trámite archivado (UC-008) (SRC-009).
- FR-003: el tutor se asigna después del visto bueno del perfil, junto con el tribunal (paso 5 de SRC-001).
- FR-011: el documento queda apto para la predefensa solo cuando lo aprueban los tres miembros del tribunal y el tutor.
- SRC-007: los casos de uso se reestructuraron el 2026-10-27, numerados de nuevo desde UC-001. El detalle acordado en SRC-004, SRC-005 y SRC-006 corresponde a casos de uso anteriores y se vuelve a detallar en los nuevos.
- NFR-IN-001, CON-006: el servidor de correo es infraestructura, no un actor. Los destinatarios de las notificaciones son los usuarios; cómo se envían se resuelve en el diseño.

## Non-Functional Requirements

### Usability

| ID | Requirement | Fit criterion | Scope | Priority | Source |
| --- | --- | --- | --- | --- | --- |
| NFR-US-001 | La aplicación web se puede usar desde un celular | TBD | Global | Must | SRC-003 |

### Security and Privacy

| ID | Requirement | Fit criterion | Scope | Priority | Source |
| --- | --- | --- | --- | --- | --- |
| NFR-SE-001 (A) | Cada usuario solo consulta y modifica la información que su rol le permite, y solo de los trámites en los que participa | TBD | Global | Must | SRC-003 |
| NFR-SE-002 (A) | Cada cambio en el sistema queda registrado con quién lo hizo, cuándo y sobre qué dato | TBD | Global | Must | SRC-007 |

### Interfaces

| ID | Requirement | Fit criterion | Scope | Priority | Source |
| --- | --- | --- | --- | --- | --- |
| NFR-IN-001 (A) | El sistema recibe las solicitudes de los usuarios y les envía respuestas y notificaciones por correo electrónico. Cada solicitud es un comando que el usuario escribe en el asunto del correo con la forma de una llamada a función, `Comando(arg1; arg2; …)`, con los argumentos separados por punto y coma. Los archivos se envían como adjuntos del correo, sin nombrarlos en el comando: el sistema reconoce qué documento es cada adjunto por una palabra de su nombre de archivo. El sistema identifica al usuario por la dirección de correo del remitente | TBD | Global | Must | SRC-003, SRC-009 |

### Constraints

| ID | Constraint | Fit criterion | Scope | Priority | Source |
| --- | --- | --- | --- | --- | --- |
| CON-001 (A) | La primera entrega es una prueba de concepto que se opera completamente por correo electrónico, construida en Java con Spring Boot | Cada caso de uso se ejecuta solo enviando y recibiendo correos; el código usa Java y Spring Boot | Global | Must | SRC-003 |
| CON-002 (A) | La segunda entrega, posterior a la primera, es la versión final del sistema: una aplicación web con backend en Laravel y frontend en React que reemplaza a la prueba de concepto por correo | El backend usa Laravel y el frontend usa React | Global | Must | SRC-003 |
| CON-003 | Fechas de la primera y la segunda entrega | Primera entrega: 2026-11-17 o 2026-11-19, por confirmar. Segunda entrega: TBD | Global | Must | SRC-003, SRC-011 |
| CON-004 | El perfil de proyecto, el documento del proyecto y el PDF anotado de una revisión se entregan en PDF con un tamaño máximo de 25 MB | El sistema rechaza archivos que no son PDF o que superan 25 MB | UC-004, UC-006 | Must | SRC-003, SRC-006 |
| CON-005 | Los documentos iniciales del trámite se entregan como imagen PNG o JPG/JPEG, o como PDF, con un tamaño máximo de 1 MB por archivo | El sistema rechaza archivos que no son PNG, JPG/JPEG o PDF, o que superan 1 MB | UC-003 | Must | SRC-003 |
| CON-006 (A) | Para la presentación del proyecto, el sistema usa el servidor de correo provisto por el docente de Tecnología Web, `mail.tecnoweb.org.bo`: recibe por POP3 y envía por SMTP, en los puertos por defecto (110 y 25), sin cifrado. El SMTP no pide contraseña y valida al remitente por su dirección IP: el equipo pide al docente que agregue a la lista de direcciones permitidas su dirección IP pública para las pruebas y la de su servidor de despliegue de contingencia; la presentación se hace desde la dirección IP del laboratorio | El sistema recibe y envía todos sus correos a través de `mail.tecnoweb.org.bo` con esa configuración | Global | Must | SRC-003 |
| CON-007 (A) | Para la presentación del proyecto, el sistema usa la base de datos PostgreSQL provista por el docente de Tecnología Web: base `db_grupo03sc` en el servidor `db.tecnoweb.org.bo`, puerto por defecto (5432), con el usuario y la contraseña que entrega el docente | El sistema guarda todos sus datos en `db_grupo03sc` de `db.tecnoweb.org.bo` | Global | Must | SRC-003 |
| CON-008 | La foto de perfil se entrega como imagen PNG o JPG/JPEG con un tamaño máximo de 1 MB | El sistema rechaza fotos que no son PNG o JPG/JPEG, o que superan 1 MB | UC-002 | Must | SRC-004 |

## Out of Scope

- Ninguno por ahora.

## Open Questions

- [x] FR-002, CON-005: con el trámite abierto por el secretario(a), ¿los documentos iniciales se digitalizan y suben al sistema, o solo se registra su recepción? Respuesta: el secretario(a) los digitaliza y los adjunta al abrir el trámite; el sistema los guarda en el expediente de titulación (SRC-009).
- [ ] FR-034: un trámite queda pendiente de reasignación cuando se desactiva la cuenta de su tutor o de un miembro del tribunal; ¿UC-005 debe permitir reasignarlos? ¿Qué pasa con el trámite de un estudiante desactivado? (ask: Secretario(a) de dirección de carrera)
- [ ] CON-003: ¿la primera entrega es el 17 o el 19 de noviembre de 2026, y cuál es la fecha de la segunda? (ask: Docente de Tecnología Web)
- [ ] NFR-US-001, NFR-SE-001, NFR-SE-002, NFR-IN-001: criterios de aceptación medibles (ask: Docente de Tecnología Web)
- [ ] FR-024: ¿cuánto dura el plazo de revisión del tutor y de cada miembro del tribunal, quién lo fija y qué hace el sistema cuando vence? (ask: Secretario(a) de dirección de carrera)
- [ ] FR-013, FR-015: ¿se notifica también la programación de la defensa final? (ask: Secretario(a) de dirección de carrera)
- [ ] FR-016: ¿qué datos lleva el acta de graduación? (ask: Secretario(a) de dirección de carrera)
- [ ] FR-028: ¿qué indicadores muestra el dashboard? (ask: Director de carrera)
- [ ] FR-029: ¿qué reportes y estadísticas necesita la dirección? (ask: Director de carrera)
- [x] FR-005, FR-022: ¿quién designa al docente de Taller de Grado y en qué momento? Respuesta: no se designa por trámite; es un rol de cuenta que asigna el administrador (UC-001), y la carrera tiene uno solo, que revisa todos los perfiles de proyecto.
- [x] CON-001, CON-002: ¿qué casos de uso entran en la primera entrega (por correo) y cuáles en la segunda (web)? Respuesta: todos los casos de uso en ambas entregas, primero la de correo electrónico (ver Scope en `vision.md`).
- [x] CON-002: ¿la aplicación web reemplaza al sistema por correo o ambos canales funcionan a la vez sobre los mismos datos? Respuesta: la reemplaza; el sistema por correo es una prueba de concepto y la aplicación web es la versión final (ver CON-001, CON-002).
- [x] CON-005: ¿qué formatos de imagen se aceptan? Respuesta: PNG y JPG/JPEG.
- [x] CON-005: ¿los documentos iniciales se entregan como archivo además de en papel? Respuesta: hay dos caminos: el secretario(a) los recibe en papel y los digitaliza para subirlos al sistema, o el estudiante los sube directamente. Reemplazada por SRC-007: el secretario(a) recibe la carpeta en la oficina y abre el trámite (UC-003).
- [x] CON-004: ¿el perfil de proyecto (UC-004) también se entrega en PDF y con qué tamaño máximo? Respuesta: sí, en PDF, con el mismo máximo de 25 MB.
- [x] NFR-IN-001: ¿con qué formato escribe el usuario sus solicitudes por correo? Respuesta: como un comando en el asunto del correo, con la forma de una llamada a función: `Comando(args[])`.
- [x] NFR-IN-001: ¿cómo se separan los argumentos del comando y cómo se escriben los que contienen espacios o comas? Respuesta: se separan con punto y coma.
- [x] NFR-IN-001: ¿los archivos (documentos iniciales, perfil, documento del proyecto) se envían como adjuntos del correo? Respuesta: sí, como adjuntos; el comando recibe el nombre del archivo como argumento. Reemplazada en SRC-009: el sistema reconoce cada adjunto por una palabra de su nombre de archivo, y el comando no lo nombra.
- [x] NFR-IN-001, NFR-SE-001: ¿cómo identifica el sistema al usuario que envía un correo (solo por su dirección de correo u otro mecanismo)? Respuesta: por su dirección de correo.
- [x] CON-006: el SMTP valida al remitente por su dirección IP; ¿desde qué servidor o dirección IP debe ejecutarse el sistema para poder enviar correos? Respuesta: para las pruebas, el equipo pide al docente que agregue su dirección IP pública a la lista de direcciones permitidas; la presentación se hace desde la dirección IP del laboratorio. Como contingencia, el equipo tendrá su propio servidor de despliegue.
- [x] CON-006: si se usa el servidor de despliegue de contingencia, ¿el docente también agrega su dirección IP a la lista de direcciones permitidas del SMTP? Respuesta: sí, su dirección IP se incluye en el pedido al docente.
- [x] FR-027: ¿el calendario académico muestra algo más que predefensas y defensas finales? Respuesta: no.
