# UC-002 Habilitar académicamente solicitudes

| | |
| --- | --- |
| Status | Detailed |
| Iteration | E1 |
| Primary actor | ACT-002 Secretario(a) de dirección de carrera (`Role.ADMINISTRATIVE`) o director(a) de carrera (`Role.DIRECTOR`) |
| Supporting actors | ACT-001 Estudiante (`Role.STUDENT`), Servicio de correo |
| Priority | Must |
| Architecturally significant | Yes: manejo y almacenamiento de 4 adjuntos PDF (hasta 25 MB c/u), creación o vinculación transaccional con cuenta de usuario y apertura del expediente digital con eventos de dominio |
| Feature | FEAT-002 Habilitación académica y apertura de expediente |
| Functional requirements (derived) | FR-001, FR-002, FR-051, FR-054, FR-055, FR-056 |

## Brief Description

Secretaría o Dirección registra los documentos iniciales de un estudiante (carta de solicitud, hoja de vida, fotocopia de cédula de identidad y boleta de inscripción) para abrir su expediente digital de titulación por la modalidad de Proyecto de Grado. El sistema valida los adjuntos, vincula una cuenta activa de estudiante sin trámite en curso o crea una cuenta nueva, abre el trámite y notifica al estudiante y al remitente por correo. Secretaría puede reemplazar documentos iniciales mientras el trámite esté en curso.

## Preconditions

- El remitente del comando debe ser un usuario registrado y activo con rol de secretario(a) (`Role.ADMINISTRATIVE`) o director de carrera (`Role.DIRECTOR`).
- El estudiante ha entregado previamente en la oficina de secretaría la carpeta física con los 4 documentos iniciales requeridos.
- El PDF escaneado de la carta de solicitud física es suficiente.
- El estudiante no debe tener otro trámite de titulación activo en curso (`FR-056`).
- Si el estudiante ya tiene una cuenta de usuario registrada, esta no debe encontrarse desactivada (`FR-056`).

## Basic Flow

1. Secretaría o Dirección redacta un correo dirigido a la dirección del sistema (`sistema@cpms.local`) con el comando `tramites.abrirTramite[nombres; primerApellido; segundoApellido; ci; registro; correo; telefono]` en el asunto y adjunta los 4 documentos digitalizados en formato PDF.
2. El sistema sondea el buzón mediante POP3, extrae el mensaje y valida que el remitente tenga permisos (`RolePermission.OPEN_DEGREE_PROCEDURE`).
3. El sistema valida que el asunto contenga exactamente 7 argumentos y que vengan exactamente 4 archivos adjuntos.
4. El sistema clasifica los 4 archivos adjuntos examinando palabras clave en su nombre de archivo (`carta`/`solicitud`, `cv`/`vida`, `ci`/`cedula`, `boleta`/`inscripcion`), y verifica que sean formato `.pdf` y que ninguno supere los 25 MB (`NFR-005`).
5. El sistema reutiliza las reglas de validación y normalización que UC-001 aplica al crear una cuenta de estudiante (`NFR-001`). Se conserva la implementación de UC-001 como base para este caso de uso y los posteriores.
6. El sistema busca el correo y el registro universitario. Si ambos están libres, crea una cuenta con rol `Role.STUDENT` (`FR-051`). Si ambos pertenecen a la misma cuenta de estudiante, sigue A1. Si solo coincide uno, apuntan a cuentas distintas o pertenecen a una cuenta de otro rol, sigue E7.
7. El sistema crea la entidad del trámite (`DegreeProcedure`) en estado habilitado (`HABILITADO`), almacena los metadatos y archivos de los 4 documentos iniciales en el expediente de titulación (`FR-001`, `FR-002`) y registra el evento de dominio `ProcedureOpenedEvent`.
8. El sistema persiste los cambios en la base de datos dentro de una única transacción.
9. Tras confirmarse la transacción (`AFTER_COMMIT`), el sistema despacha dos correos por SMTP:
   - Al remitente: respuesta de confirmación con el código de expediente generado y el resumen del estudiante habilitado.
   - Al estudiante (`FR-054`): notificación formal informando la apertura de su expediente de titulación y habilitándolo a presentar su perfil de proyecto.

## Alternative Flows

### A1 Estudiante con cuenta de usuario previa activa (FR-056)
En el paso 6 del flujo básico, el sistema detecta que el correo electrónico y el registro universitario pertenecen a la misma cuenta con rol `Role.STUDENT`:
1. El sistema verifica que la cuenta esté activa (E6) y que no tenga un trámite de titulación en curso (E5).
2. El sistema vincula el nuevo trámite de titulación a la cuenta existente sin crear una nueva.
3. El caso de uso continúa en el paso 7 del flujo básico.

### A2 Reemplazo de un documento inicial en trámite en curso (FR-055)
Mientras el trámite de titulación se encuentre en curso (no cerrado ni archivado), el secretario(a) necesita sustituir un documento inicial defectuoso u observado:
1. El secretario(a) envía un correo con el comando `tramites.reemplazarDocumentoInicial[registroEstudiante; tipoDocumento]` adjuntando el nuevo archivo PDF.
2. El sistema valida que el remitente tenga rol administrativo y que el estudiante tenga un trámite activo en curso.
3. El sistema valida el archivo adjunto (PDF $\le$ 25 MB) y sustituye el documento indicado en el expediente.
4. El sistema responde confirmando el reemplazo y notifica al estudiante por correo.
5. El caso de uso finaliza exitosamente.

## Exceptions

### E1 Remitente no autorizado
En el paso 2 del flujo básico, el remitente no está registrado o no posee el rol `Role.ADMINISTRATIVE` ni `Role.DIRECTOR`:
1. El sistema rechaza la solicitud y envía una respuesta por correo: *"No tiene permiso para abrir trámites de titulación."*
2. El caso de uso finaliza sin realizar cambios en el sistema.

### E2 Argumentos insuficientes o inválidos
En el paso 3 o 5 del flujo básico, la cantidad de argumentos es distinta de 7 o alguno no cumple las reglas de formato:
1. El sistema interrumpe el procesamiento y genera una excepción `DomainValidationException` detallando los errores campo por campo según las reglas de UC-001.
2. El sistema responde al remitente con el listado de campos inválidos.
3. El caso de uso finaliza sin persistir datos.

### E3 Adjuntos faltantes, irreconocibles o duplicados
En el paso 3 o 4 del flujo básico, la cantidad de adjuntos es diferente de 4 o no se logra identificar uno de los tipos obligatorios mediante sus palabras clave:
1. El sistema rechaza la solicitud e indica qué documento no se pudo clasificar o cuál falta (ej. *"No se encontró el documento correspondiente a: Boleta de Inscripción"*).
2. El caso de uso finaliza sin abrir el expediente.

### E4 Archivo supera el tamaño máximo permitido (NFR-005)
En el paso 4 del flujo básico, uno o más archivos adjuntos superan los 25 MB:
1. El sistema rechaza el comando con el mensaje: *"El archivo [nombre] supera el límite máximo permitido de 25 MB."*
2. El caso de uso finaliza sin cambios.

### E5 Estudiante ya cuenta con un trámite en curso (FR-056)
En el paso 6 (o A1), el sistema detecta que el estudiante ya tiene un expediente de titulación abierto:
1. El sistema aborta la operación y responde: *"El estudiante con registro [registro] ya cuenta con un trámite de titulación en curso."*
2. El caso de uso finaliza sin abrir un segundo trámite.

### E6 Cuenta existente se encuentra desactivada (FR-056)
En el paso 6 (o A1), el estudiante ya tiene una cuenta registrada pero su estado es desactivado:
1. El sistema aborta la operación y responde: *"La cuenta de usuario del estudiante se encuentra desactivada. Debe reactivarse antes de abrir un trámite."*
2. El caso de uso finaliza sin abrir el trámite.

### E7 Correo y registro incompatibles con una cuenta de estudiante
1. El sistema rechaza la apertura y responde al remitente: *"No se pudo abrir el trámite. Verifique el correo y el registro del estudiante."*
2. Secretaría verifica y corrige los datos antes de volver a solicitar la apertura.

## Postconditions

- **Success:** Se crea el expediente de titulación (`DegreeProcedure`) en estado habilitado, se almacenan los 4 documentos iniciales digitalizados y la cuenta del estudiante queda creada o vinculada. El remitente recibe la confirmación y el estudiante recibe el aviso de apertura.
- **Failure:** No se crea el expediente, no se registran archivos adjuntos, no se altera la base de datos ni se despachan notificaciones a estudiantes; se envía un correo de error al remitente explicando el motivo del rechazo.

## Special Requirements (derived)

- **NFR-001:** Validación detallada de parámetros con mensajes de error por cada campo inválido.
- **NFR-002:** Restricción de acceso basada en roles (`RolePermission.OPEN_DEGREE_PROCEDURE`).
- **NFR-003:** Recepción por correo electrónico mediante comando en el asunto y clasificación de adjuntos por palabras clave.
- **NFR-005:** Límite máximo de 25 MB por archivo PDF.
- **NFR-007 y NFR-008:** Integración con servidores de correo POP3/SMTP y base de datos PostgreSQL institucional.

---

## Contrato Base de Comandos (Email-Driven API)

### Comando Principal: `tramites.abrirTramite`

Abre formalmente el expediente de titulación y registra los 4 documentos iniciales requeridos.

#### 1. Sintaxis en el Asunto del Correo
```text
tramites.abrirTramite[nombres; primerApellido; segundoApellido; ci; registro; correo; telefono]
```

#### 2. Argumentos de Entrada (Posicionales)
| # | Parámetro | Tipo | Obligatorio | Formato / Reglas de Validación |
| :---: | :--- | :--- | :---: | :--- |
| **1** | `nombres` | String | Sí | Solo letras (con tildes, ñ, ü) y espacios. Se normaliza con mayúscula inicial en cada palabra. |
| **2** | `primerApellido` | String | Sí | Solo letras y espacios. Mayúscula inicial normalizada. |
| **3** | `segundoApellido`| String | No | Opcional. Si se provee, solo letras y espacios. |
| **4** | `ci` | String | Sí | CI de estudiante según `usuarios.crearCuenta`. |
| **5** | `registro` | String | Sí | Código de estudiante según `usuarios.crearCuenta`. |
| **6** | `correo` | String | Sí | Validación y normalización según `usuarios.crearCuenta`. |
| **7** | `telefono` | String | Sí | Teléfono de estudiante según `usuarios.crearCuenta`. |

Todos los datos de cuenta siguen la validación y normalización de [`usuarios.crearCuenta`](../USER-GUIDE.md) para el rol estudiante.

#### 3. Especificación de Documentos Adjuntos Requeridos
El correo **debe incluir exactamente 4 archivos adjuntos en formato PDF**. El sistema los clasifica automáticamente buscando palabras clave en el nombre de archivo (sin distinguir mayúsculas/minúsculas):

| Documento | Palabras Clave Aceptadas | Formato | Tamaño Máximo |
| :--- | :--- | :---: | :---: |
| Carta de Solicitud | `carta`, `solicitud` | `.pdf` | 25 MB |
| Hoja de Vida (CV) | `cv`, `vida`, `curriculum` | `.pdf` | 25 MB |
| Cédula de Identidad | `ci`, `cedula`, `identidad` | `.pdf` | 25 MB |
| Boleta de Inscripción | `boleta`, `inscripcion` | `.pdf` | 25 MB |

#### 4. Matriz de Autorización
| Rol | ¿Puede ejecutarlo? |
| :--- | :---: |
| `administrador` (`Role.SYSTEM_ADMIN`) | No |
| `director` (`Role.DIRECTOR`) | Sí |
| `administrativo` (`Role.ADMINISTRATIVE`) | Sí |
| `docente` (`Role.PROFESSOR`) | No |
| `docente_taller` (`Role.CAPSTONE_PROJECT_PROFESSOR`) | No |
| `estudiante` (`Role.STUDENT`) | No |

#### 5. Respuestas y Notificaciones por Correo

##### A. Respuesta de éxito al remitente
* **Destinatario:** Quien envió el comando.
* **Asunto:** `Re: tramites.abrirTramite - Trámite habilitado`
* **Cuerpo:**
  ```text
  Trámite de titulación habilitado con éxito.
  Expediente: EXP-2026-0001
  Estudiante: Carlos Fuentes Rojas
  Registro Universitario: 219012345
  Documentos iniciales registrados: 4
  ```

##### B. Notificación de Apertura al Estudiante (`FR-054`)
* **Destinatario:** Correo del estudiante ingresado en el comando.
* **Asunto:** `Apertura de Expediente de Titulación - CPMS`
* **Cuerpo:**
  ```text
  Estimado(a) Carlos Fuentes Rojas,
  Se le informa que la Dirección de Carrera ha abierto y habilitado formalmente su trámite de titulación por la modalidad de Proyecto de Grado bajo el expediente EXP-2026-0001.
  
  A partir de este momento puede presentar su Perfil de Proyecto enviando un correo al sistema con el comando:
  proyectos.presentarPerfil[Título del Proyecto]
  adjuntando el archivo PDF de su perfil.
  ```

##### C. Respuestas de Error
* **Error de Validación de Parámetros (`DomainValidationException`):**
  ```text
  No se pudo procesar la solicitud debido a los siguientes errores:
  - ci: debe contener solo dígitos
  - adjuntos: no se encontró el documento correspondiente a 'Boleta de Inscripción'
  ```
* **Error de Negocio (`DomainException`):**
  ```text
  Error: El estudiante con registro 219012345 ya cuenta con un trámite de titulación en curso.
  ```

---

### Comando Secundario: `tramites.reemplazarDocumentoInicial` (Flujo A2, FR-055)

Permite sustituir uno de los documentos iniciales archivados en el expediente si se requiere una corrección.

#### 1. Sintaxis en el Asunto del Correo
```text
tramites.reemplazarDocumentoInicial[registroEstudiante; tipoDocumento]
```

#### 2. Argumentos de Entrada
| # | Parámetro | Tipo | Obligatorio | Valores Válidos / Reglas |
| :---: | :--- | :--- | :---: | :--- |
| **1** | `registroEstudiante` | String | Sí | Registro universitario del estudiante titular del trámite. |
| **2** | `tipoDocumento` | String | Sí | Uno de: `carta`, `cv`, `ci`, `boleta` (sin distinguir mayúsculas). |

#### 3. Adjuntos Requeridos
* Exactamente **1 archivo adjunto en formato PDF** ($\le$ 25 MB).

#### 4. Respuesta al Remitente
```text
Documento inicial reemplazado con éxito.
Expediente: EXP-2026-0001
Documento actualizado: Carta de Solicitud
Archivo registrado: carta_solicitud_v2.pdf
```
