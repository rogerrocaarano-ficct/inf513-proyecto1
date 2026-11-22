# UC-003 Registrar perfil de proyecto y asignar tutor

| | |
| --- | --- |
| Status | Detailed |
| Iteration | E1 |
| Primary actor | Estudiante (`Role.STUDENT`) en el flujo básico y A1; Director de carrera (`Role.DIRECTOR`) en A2 |
| Supporting actors | Docente de Taller de Grado (`Role.CAPSTONE_PROJECT_PROFESSOR`), que recibe el aviso de cada perfil presentado y lo evalúa en UC-004 |
| Priority | Must |
| Architecturally significant | Yes: conserva el historial de perfiles de un proyecto de grado, aplica la regla de admisión de FR-058, es el primer caso de uso que recibe un archivo adjunto (NFR-005) y depende de UC-002 (trámite abierto) y de UC-004 (visto bueno) |
| Feature | Ninguno definido en el repositorio |
| Functional requirements (derived) | FR-003, FR-004, FR-020, FR-023, FR-057, FR-058 |

> **Borrador para revisión.** El modelo de EA se reemplaza por este documento. Los nombres de comandos y argumentos son **propuestas** (ver Open Questions). Todo lo demás sale de [README.md](README.md), [REQUIREMENTS.md](REQUIREMENTS.md), [GLOSSARY.md](GLOSSARY.md) y [USER-GUIDE.md](USER-GUIDE.md).

## Brief Description

El estudiante presenta el título y el perfil de su proyecto de grado; el sistema registra el perfil y avisa al docente de Taller de Grado, que evalúa su viabilidad en UC-004. Si el perfil fue rechazado u observado, el estudiante presenta uno nuevo y los anteriores se conservan. Una vez que el perfil tiene visto bueno, el director de carrera designa al tutor y el sistema lo registra y lo notifica al estudiante.

## Preconditions

- **Flujo básico y A1:**
  - El remitente tiene una cuenta activa con rol estudiante.
  - El estudiante tiene un trámite de titulación abierto (UC-002).
- **A2:**
  - El remitente tiene una cuenta activa con rol director.
  - El perfil de proyecto del estudiante tiene el visto bueno del docente de Taller de Grado (UC-004).

## Basic Flow

Presentar el perfil de proyecto.

1. El estudiante envía al sistema un correo con el comando `proyectos.presentarPerfil[título]` y el perfil de proyecto como adjunto.
2. El sistema identifica al estudiante por la dirección del remitente y verifica que tiene una cuenta activa de estudiante y un trámite de titulación abierto.
3. El sistema verifica que el estudiante aún no tiene un perfil de proyecto (FR-058).
4. El sistema valida el título y el adjunto.
5. El sistema crea el proyecto de grado del estudiante y registra el título y el perfil presentado (FR-004).
6. El sistema confirma la presentación al estudiante y notifica al docente de Taller de Grado (FR-057).

## Alternative Flows

### A1 Presentar un nuevo perfil

En el paso 3 del flujo básico, si el estudiante ya tiene un perfil y el último fue rechazado u observado por el docente de Taller de Grado (UC-004):

1. El sistema admite la presentación (FR-058) y continúa en el paso 4.
2. En el paso 5, en lugar de crear otro proyecto de grado, registra el nuevo perfil en el proyecto existente, conserva los perfiles anteriores (FR-023) y toma el título indicado, que puede ser distinto del anterior.
3. El caso de uso continúa en el paso 6.

### A2 Asignar tutor

Después de que el docente de Taller de Grado registra el visto bueno del perfil (UC-004):

1. El director de carrera envía al sistema un correo con el comando `tramites.asignarTutor[registro del estudiante; correo del docente]`, sin adjuntos.
2. El sistema identifica al director por la dirección del remitente y verifica que tiene una cuenta activa con rol director.
3. El sistema localiza el trámite de titulación abierto del estudiante y verifica que su perfil de proyecto tiene visto bueno.
4. El sistema verifica que el correo indicado pertenece a una cuenta activa que puede ser designada tutor.
5. El sistema registra al docente como tutor del trámite (FR-003). La designación no cambia el rol de su cuenta.
6. El sistema confirma la designación al director y la notifica al estudiante (FR-020).

## Exceptions

### E1 Remitente sin cuenta o sin permiso

En el paso 2 del flujo básico o de A2, si la dirección del remitente no tiene cuenta, la cuenta está desactivada o su rol no puede ejecutar el comando:

1. El sistema responde al remitente que no está registrado o que no tiene permiso para esa operación.
2. El caso de uso termina sin cambios.

### E2 Estudiante sin trámite abierto

En el paso 2 del flujo básico, si el estudiante no tiene un trámite de titulación abierto:

1. El sistema responde que el estudiante no tiene un trámite de titulación abierto.
2. El caso de uso termina sin registrar el perfil.

### E3 Perfil no admisible

En el paso 3 del flujo básico, si el estudiante ya tiene un perfil y el último no fue rechazado ni observado (está en evaluación o tiene visto bueno):

1. El sistema responde que solo puede presentar un perfil nuevo cuando el anterior fue rechazado u observado.
2. El caso de uso termina sin registrar el perfil.

### E4 Datos inválidos

En el paso 4 del flujo básico, si el título está vacío, falta el adjunto, el adjunto no es el documento esperado o excede el tamaño máximo (NFR-005):

1. El sistema responde con un error por cada dato inválido (NFR-001).
2. El caso de uso termina sin registrar el perfil.

### E5 Trámite no encontrado

En el paso 3 de A2, si el estudiante indicado no existe o no tiene un trámite de titulación abierto:

1. El sistema responde que no encontró un trámite abierto para ese estudiante.
2. El caso de uso termina sin registrar la designación.

### E6 Perfil sin visto bueno

En el paso 3 de A2, si el perfil del estudiante aún no tiene visto bueno:

1. El sistema responde que el tutor se designa después del visto bueno del perfil.
2. El caso de uso termina sin registrar la designación.

### E7 Docente no elegible

En el paso 4 de A2, si el correo no pertenece a ninguna cuenta, la cuenta está desactivada o su rol no puede ser designado tutor:

1. El sistema responde que esa persona no puede ser designada tutor.
2. El caso de uso termina sin registrar la designación.

## Postconditions

- **Success:**
  - Flujo básico y A1: el proyecto de grado tiene registrado el perfil presentado y conserva los anteriores. El estudiante recibió la confirmación y el docente de Taller de Grado el aviso.
  - A2: el tutor queda registrado en el trámite del estudiante. El director recibió la confirmación y el estudiante el aviso.
- **Failure:** la operación rechazada no registra un perfil ni cambia el título o el tutor existentes, y el remitente recibe el motivo. Si falla A2, el perfil ya presentado se conserva.

## Special Requirements (derived)

- NFR-001: mensajes de error con validación detallada de los datos de entrada.
- NFR-002: cada usuario solo hace lo que su rol le permite.
- NFR-003: las solicitudes son comandos en el asunto del correo, y el sistema reconoce cada adjunto por una palabra de su nombre de archivo.
- NFR-005: un documento PDF recibido pesa como máximo 25 MB.
- NFR-SE-002: cada cambio queda registrado con quién lo hizo, cuándo y sobre qué dato.

## Open Questions

- [ ] ¿Qué formato tiene el perfil y qué palabra del nombre de archivo lo identifica (NFR-003)? Propuesta: PDF con la palabra `perfil`. (ask: docente de la materia)
- [ ] ¿Qué reglas tiene el título (longitud mínima y máxima)? (ask: docente de la materia)
- [ ] ¿Cómo identifica el director al estudiante en `tramites.asignarTutor`? Propuesta: registro universitario (`uagrmCode`), porque UC-002 aún no define una referencia del trámite. (ask: responsable del modelo)
- [ ] ¿El director designa tutor y tribunal en una sola operación (README, paso 5; nota de FR-003) o por separado, como separan UC-003 y UC-004? (ask: responsable del modelo)
- [ ] ¿Qué pasa si el estudiante ya tiene tutor? ¿Se puede reasignar? (ask: dirección de carrera)
- [ ] ¿Qué cuentas pueden ser tutor? La nota de FR-026 dice que cualquier docente y el director; falta definir si el docente de Taller de Grado también. (ask: dirección de carrera)
- [ ] ¿Se notifica también al docente designado? FR-020 solo pide notificar al estudiante; si sí, falta un FR. (ask: docente de la materia)
- [ ] ¿La consulta del proyecto de grado (FR-059) pertenece a UC-003 o a UC-007? (ask: responsable del modelo)
- [ ] ¿Cómo se llaman los estados del perfil (presentado, con visto bueno, observado, rechazado)? Los define UC-004. (ask: responsable del modelo)
- [ ] El flujo A2 no se puede probar de punta a punta sin UC-002 y UC-004. ¿Se implementan antes o se usan dobles de prueba? (ask: responsable del desarrollo)