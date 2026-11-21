# UC-003 Asignar tutor y registrar perfil de proyecto

| | |
| --- | --- |
| Status | Detailed |
| Iteration | E1 |
| Primary actor | ACT-001 Estudiante (`Role.STUDENT`) [Perfil] / ACT-003 Director de carrera (`Role.DIRECTOR`) [Tutor] |
| Supporting actors | ACT-004 Docente de Taller de Grado (`Role.CAPSTONE_PROJECT_PROFESSOR`), ACT-005 Docente Tutor (`Role.PROFESSOR`), Servicio de correo |
| Priority | Must |
| Architecturally significant | Yes: ciclo de vida y versionado del aggregate `DegreeProject` y `ProjectProfile`, validación de políticas de entrega (perfil único o anterior observado/rechazado), asignación de tutor y notificaciones cruzadas multi-actor |
| Feature | FEAT-003 Registro de perfil de proyecto y designación de tutor |
| Functional requirements (derived) | FR-003, FR-004, FR-020, FR-057, FR-058, FR-059 |

## Brief Description

Permite al estudiante con expediente de titulación habilitado registrar la propuesta formal de su Proyecto de Grado adjuntando el perfil de proyecto en formato PDF con el título de la investigación. El sistema registra el proyecto de grado, archiva la versión del perfil y notifica al docente de Taller de Grado para que evalúe su viabilidad. Asimismo, permite al Director de Carrera registrar formalmente la designación del docente que actuará como tutor del proyecto del estudiante y notificar tanto al estudiante como al docente tutor. Finalmente, permite al estudiante consultar por correo el estado vigente de su proyecto y sus perfiles registrados.

## Preconditions

- **Para presentar perfil de proyecto:**
  - El remitente debe ser un estudiante con una cuenta activa (`Role.STUDENT`).
  - El estudiante debe tener un expediente de titulación en curso habilitado (`UC-002`).
  - El estudiante no debe tener un perfil de proyecto en estado de revisión o con visto bueno previo. Solo puede registrar un perfil si es el primero o si el perfil anterior fue rechazado u observado por el docente de Taller de Grado (`FR-058`).
- **Para asignar tutor:**
  - El remitente debe ser el Director de Carrera (`Role.DIRECTOR`) o el Administrador (`Role.SYSTEM_ADMIN`).
  - Debe existir un expediente de titulación activo para el registro universitario del estudiante (`FR-003`).
  - La persona designada debe tener una cuenta de usuario activa con rol de docente (`Role.PROFESSOR`) o director (`Role.DIRECTOR`).

## Basic Flow

### Subflujo 1: Presentación y Registro del Perfil de Proyecto (Estudiante)
1. El estudiante redacta un correo a `sistema@cpms.local` con el comando `proyectos.presentarPerfil[tituloDelProyecto]` en el asunto y adjunta el documento del perfil en formato PDF.
2. El sistema sondea el buzón mediante POP3, extrae el mensaje y valida que el remitente sea un usuario registrado con rol `Role.STUDENT` y con un trámite de titulación habilitado en curso.
3. El sistema valida la regla de negocio de perfil único (`FR-058`): comprueba que el estudiante no tenga un perfil pendiente de evaluación ni un perfil con visto bueno aprobado.
4. El sistema valida que el asunto contenga exactamente 1 argumento (el título del proyecto, no vacío, entre 5 y 300 caracteres) y exactamente 1 archivo adjunto en formato `.pdf` que no supere 25 MB (`NFR-005`).
5. El sistema crea la entidad `DegreeProject` asociada al trámite (si es el primer perfil entregado) y registra la versión 1 de `ProjectProfile` en estado `PRESENTADO` (`SUBMITTED`), almacenando físicamente el archivo PDF.
6. El sistema emite el evento de dominio `ProjectProfileSubmittedEvent` y persiste la transacción.
7. Tras confirmarse la transacción (`AFTER_COMMIT`), el sistema despacha dos correos por SMTP:
   - Al estudiante: confirmación de entrega con el título, versión y fecha de recepción.
   - Al docente de Taller de Grado (`Role.CAPSTONE_PROJECT_PROFESSOR`) (`FR-057`): notificación indicando que hay un nuevo perfil de proyecto listo para evaluar viabilidad.

### Subflujo 2: Asignación de Tutor (Director de Carrera)
8. El Director de Carrera envía un correo a `sistema@cpms.local` con el comando `tramites.asignarTutor[registroEstudiante; correoDocenteTutor]` en el asunto.
9. El sistema valida que el remitente sea un usuario activo con rol `Role.DIRECTOR` o `Role.SYSTEM_ADMIN`.
10. El sistema verifica que el asunto contenga exactamente 2 argumentos y que no incluya adjuntos.
11. El sistema busca el expediente activo del estudiante mediante su registro universitario y valida que el correo provisto corresponda a una cuenta activa con rol `Role.PROFESSOR` o `Role.DIRECTOR`.
12. El sistema asigna formalmente al docente como tutor en el expediente de titulación (`FR-003`) y emite el evento de dominio `TutorAssignedEvent`.
13. El sistema persiste el cambio en la base de datos.
14. Tras confirmarse la transacción (`AFTER_COMMIT`), el sistema despacha tres correos:
   - Al Director de Carrera: confirmación de asignación del tutor.
   - Al estudiante (`FR-020`): notificación informando quién ha sido designado como su tutor de proyecto.
   - Al docente tutor: notificación formal comunicándole su asignación como tutor del estudiante.

## Alternative Flows

### A1 Presentación de nuevo perfil tras observación o rechazo (FR-023, FR-058)
En el paso 3 del Subflujo 1, el estudiante ya contaba con un perfil previo registrado, pero su estado fue evaluado por el docente de Taller de Grado como `RECHAZADO` u `OBSERVADO`:
1. El sistema admite la nueva entrega y crea un nuevo registro `ProjectProfile` incrementando el número de versión (ej. versión 2).
2. Si el título proporcionado en el comando difiere del anterior, el sistema actualiza el título del proyecto en `DegreeProject` (`FR-004`).
3. El sistema almacena el nuevo archivo PDF y conserva íntegro el historial del perfil anterior (`FR-023`).
4. El caso de uso continúa en el paso 6 del Subflujo 1.

### A2 Consulta del estado del proyecto de grado por el estudiante (FR-059)
El estudiante desea conocer en cualquier momento el estado de su proyecto, el tutor asignado o las versiones de su perfil:
1. El estudiante envía un correo con el comando `proyectos.consultarProyecto[]` (sin argumentos ni adjuntos).
2. El sistema valida que el remitente sea un estudiante con trámite activo.
3. El sistema responde por correo con un informe estructurado que incluye: título del proyecto, versión y estado del perfil vigente (`PRESENTADO`, `OBSERVADO`, `RECHAZADO` o `CON_VISTO_BUENO`), datos del tutor asignado (o aviso de "Pendiente de asignación") y el historial resumido de entregas.
4. El caso de uso finaliza exitosamente.

## Exceptions

### E1 Estudiante sin trámite de titulación activo
En el paso 2 del Subflujo 1, el remitente no cuenta con un expediente de titulación abierto o habilitado:
1. El sistema rechaza el comando respondiendo: *"No cuenta con un trámite de titulación habilitado en curso. Primero debe iniciar su trámite ante la Dirección de Carrera."*
2. El caso de uso finaliza sin guardar el archivo.

### E2 Perfil de proyecto ya vigente o con visto bueno (FR-058)
En el paso 3 del Subflujo 1, el estudiante ya tiene un perfil en revisión (`PRESENTADO`) o su perfil ya fue aprobado (`CON_VISTO_BUENO`):
1. El sistema interrumpe el procesamiento y responde: *"Ya cuenta con un perfil de proyecto vigente en evaluación o aprobado. Solo puede presentar un nuevo perfil si el anterior fue rechazado u observado."*
2. El caso de uso finaliza sin registrar el documento.

### E3 Adjunto del perfil faltante, corrupto o excede 25 MB (NFR-005)
En el paso 4 del Subflujo 1, el correo no incluye adjunto, no es un archivo PDF válido o supera el límite de 25 MB:
1. El sistema responde indicando el error específico (ej. *"Debe adjuntar exactamente un archivo en formato PDF correspondiente al Perfil de Proyecto"* o *"El archivo excede el tamaño máximo permitido de 25 MB"*).
2. El caso de uso finaliza sin persistir datos.

### E4 Remitente no autorizado para asignar tutor
En el paso 9 del Subflujo 2, el remitente carece de rol `Role.DIRECTOR` o `Role.SYSTEM_ADMIN`:
1. El sistema rechaza la solicitud respondiendo: *"No tiene permiso para asignar tutores a los trámites de titulación."*
2. El caso de uso finaliza sin cambios.

### E5 Trámite de estudiante no encontrado
En el paso 11 del Subflujo 2, no se encuentra un expediente activo asociado al registro universitario indicado:
1. El sistema responde: *"No se encontró un trámite de titulación activo para el registro universitario [registroEstudiante]."*
2. El caso de uso finaliza sin cambios.

### E6 Docente tutor inexistente o rol no docente
En el paso 11 del Subflujo 2, la dirección de correo indicada para el tutor no existe en el sistema, está desactivada o su rol no es `Role.PROFESSOR` ni `Role.DIRECTOR`:
1. El sistema responde: *"El usuario [correoDocenteTutor] no es un docente activo válido para ser designado como tutor."*
2. El caso de uso finaliza sin cambios.

## Postconditions

- **Success:** Queda registrado el proyecto de grado y almacenada la nueva versión del perfil de proyecto en estado presentado, notificando al docente de Taller de Grado; o bien, queda registrada la designación del docente tutor en el expediente, notificando al estudiante y al tutor.
- **Failure:** No se modifican ni persisten entidades de proyecto ni de trámite, no se almacenan archivos en disco y se envía un correo de error detallado al remitente.

## Special Requirements (derived)

- **NFR-001:** Mensajes de error específicos con detalle de parámetros inválidos.
- **NFR-002:** Restricción de permisos por rol para presentación y asignación.
- **NFR-003:** Arquitectura guiada por correo electrónico con comandos en el asunto.
- **NFR-005:** Límite máximo de 25 MB para el PDF del perfil de proyecto.
- **NFR-007 y NFR-008:** Soporte de correo y persistencia PostgreSQL institucional.

## Open Questions

- [ ] ¿El Director de Carrera puede asignar al tutor antes de que el perfil de proyecto tenga visto bueno de viabilidad por Taller de Grado, o es un requisito estricto previo? (ask: Docente de la materia / Dirección)

---

## Contrato Base de Comandos (Email-Driven API)

### Comando 1: `proyectos.presentarPerfil`

Permite al estudiante entregar formalmente el documento de su Perfil de Proyecto.

#### 1. Sintaxis en el Asunto del Correo
```text
proyectos.presentarPerfil[tituloDelProyecto]
```

#### 2. Argumentos de Entrada (Posicionales)
| # | Parámetro | Tipo | Obligatorio | Formato / Reglas de Validación |
| :---: | :--- | :--- | :---: | :--- |
| **1** | `tituloDelProyecto` | String | Sí | Texto de entre 5 y 300 caracteres. No puede estar vacío ni compuesto solo por espacios. Se normalizan espacios múltiples. |

#### 3. Especificación de Documentos Adjuntos Requeridos
* **Cantidad exacta:** Exactamente **1 archivo adjunto**.
* **Formato permitido:** Únicamente `.pdf`.
* **Clasificación por nombre de archivo:** El nombre debe contener la palabra clave `perfil` (ej. `perfil_de_grado.pdf`, `perfil-v1.pdf`).
* **Tamaño máximo:** 25 MB (`NFR-005`).

#### 4. Matriz de Autorización
| Rol | ¿Puede ejecutarlo? |
| :--- | :---: |
| `estudiante` (`Role.STUDENT`) | Sí (siempre que tenga trámite habilitado en curso) |
| `director` (`Role.DIRECTOR`) | No |
| `docente` (`Role.PROFESSOR`) | No |
| `docente_taller` (`Role.CAPSTONE_PROJECT_PROFESSOR`) | No |
| `administrativo` (`Role.ADMINISTRATIVE`) | No |
| `administrador` (`Role.SYSTEM_ADMIN`) | No |

#### 5. Respuestas y Notificaciones por Correo

##### A. Respuesta de Éxito al Estudiante
* **Asunto:** `Re: proyectos.presentarPerfil - Perfil recibido`
* **Cuerpo:**
  ```text
  Perfil de proyecto recibido satisfactoriamente.
  Proyecto: Diseño de una red de telecomunicaciones para zonas rurales
  Versión del perfil: 1
  Estado: Presentado para evaluación de viabilidad
  Archivo registrado: perfil_v1.pdf (Tamaño: 2.4 MB)
  ```

##### B. Notificación al Docente de Taller de Grado (`FR-057`)
* **Destinatario:** Correo del único docente activo con rol `Role.CAPSTONE_PROJECT_PROFESSOR`.
* **Asunto:** `Nuevo Perfil de Proyecto para Evaluación - CPMS`
* **Cuerpo:**
  ```text
  Estimado(a) Docente de Taller de Grado,
  El estudiante Carlos Fuentes Rojas (Registro: 219012345) ha presentado su Perfil de Proyecto titulado:
  "Diseño de una red de telecomunicaciones para zonas rurales" (Versión 1).
  
  El documento se encuentra disponible en el sistema para su evaluación de viabilidad.
  ```

---

### Comando 2: `tramites.asignarTutor`

Permite a la Dirección de Carrera registrar la asignación del tutor del estudiante.

#### 1. Sintaxis en el Asunto del Correo
```text
tramites.asignarTutor[registroEstudiante; correoDocenteTutor]
```

#### 2. Argumentos de Entrada (Posicionales)
| # | Parámetro | Tipo | Obligatorio | Formato / Reglas de Validación |
| :---: | :--- | :--- | :---: | :--- |
| **1** | `registroEstudiante` | String | Sí | Registro universitario del estudiante (7 a 10 dígitos numéricos). |
| **2** | `correoDocenteTutor`| String | Sí | Dirección de correo válida de un usuario con rol `docente` o `director`. |

#### 3. Especificación de Documentos Adjuntos
* **Cantidad requerida:** 0 adjuntos (este comando no lleva archivos).

#### 4. Matriz de Autorización
| Rol | ¿Puede ejecutarlo? |
| :--- | :---: |
| `director` (`Role.DIRECTOR`) | Sí |
| `administrador` (`Role.SYSTEM_ADMIN`) | Sí |
| `administrativo` (`Role.ADMINISTRATIVE`) | No |
| `docente` (`Role.PROFESSOR`) | No |
| `docente_taller` (`Role.CAPSTONE_PROJECT_PROFESSOR`) | No |
| `estudiante` (`Role.STUDENT`) | No |

#### 5. Respuestas y Notificaciones por Correo

##### A. Respuesta de Éxito al Remitente (Director)
* **Asunto:** `Re: tramites.asignarTutor - Tutor asignado`
* **Cuerpo:**
  ```text
  Tutor asignado con éxito.
  Estudiante: Carlos Fuentes Rojas (Registro: 219012345)
  Docente Tutor: Ing. Luis Rojas (luis.rojas@uagrm.bo)
  Expediente: EXP-2026-0001
  ```

##### B. Notificación al Estudiante (`FR-020`)
* **Destinatario:** Correo del estudiante titular del trámite.
* **Asunto:** `Designación de Tutor de Proyecto de Grado - CPMS`
* **Cuerpo:**
  ```text
  Estimado(a) Carlos Fuentes Rojas,
  Se le informa que la Dirección de Carrera ha designado como su Tutor de Proyecto de Grado a:
  Docente: Ing. Luis Rojas
  Correo de contacto: luis.rojas@uagrm.bo
  ```

##### C. Notificación al Docente Tutor
* **Destinatario:** Correo del docente designado (`correoDocenteTutor`).
* **Asunto:** `Designación como Tutor de Proyecto de Grado - CPMS`
* **Cuerpo:**
  ```text
  Estimado(a) Ing. Luis Rojas,
  Se le comunica que la Dirección de Carrera lo ha designado como Tutor de Proyecto de Grado del estudiante:
  Estudiante: Carlos Fuentes Rojas (Registro: 219012345)
  Título propuesto: "Diseño de una red de telecomunicaciones para zonas rurales"
  ```

---

### Comando 3: `proyectos.consultarProyecto` (Flujo A2, FR-059)

Permite al estudiante consultar en cualquier momento el estado actual de su proyecto de grado.

#### 1. Sintaxis en el Asunto del Correo
```text
proyectos.consultarProyecto[]
```

#### 2. Argumentos y Adjuntos
* **Argumentos:** Ninguno (los corchetes deben ir vacíos).
* **Adjuntos:** 0 adjuntos.

#### 3. Matriz de Autorización
* Exclusivo para usuarios con rol `Role.STUDENT` con trámite habilitado.

#### 4. Respuesta al Estudiante por Correo
```text
Información de su Proyecto de Grado:
- Título: Diseño de una red de telecomunicaciones para zonas rurales
- Expediente: EXP-2026-0001
- Estado del Perfil: PRESENTADO (Versión 1 entregada el 2026-11-21)
- Docente de Taller de Grado: En evaluación de viabilidad
- Tutor Asignado: Ing. Luis Rojas (luis.rojas@uagrm.bo)
- Tribunal Evaluador: Pendiente de conformación
```
