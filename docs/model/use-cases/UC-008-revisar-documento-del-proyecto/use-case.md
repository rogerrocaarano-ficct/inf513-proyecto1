# UC-008 Revisar documento del proyecto

| | |
| --- | --- |
| Status | Detailed |
| Iteration | I1 |
| Primary actor | ACT-006 Tutor, ACT-007 Miembro del tribunal |
| Supporting actors | ACT-009 Servicio de correo |
| Priority | Must |
| Architecturally significant | Yes: aprobación de los tres miembros del tribunal y del tutor sobre cada versión y control de plazos de revisión |
| Feature | FEAT-005 |
| Functional requirements (derived) | FR-009, FR-011, FR-024, FR-043, FR-044, FR-045, FR-046, FR-047, FR-048 |

## Brief Description

Los revisores revisan la versión vigente del documento del proyecto en dos etapas. Primero la revisa el tutor, como filtro previo; si la aprueba, el sistema la pasa a los tres miembros del tribunal evaluador. Cada revisor registra una sola revisión por versión: aprobada u observada, con sus observaciones en texto y, si quiere, un PDF anotado. Las observaciones solo se registran en el sistema; no se aceptan por carta. El sistema avisa por correo al estudiante cuando el tutor observa la versión y cuando todo el tribunal la revisó. El documento queda aprobado cuando lo aprueban el tutor y los tres miembros del tribunal; entonces el sistema avisa al estudiante, al secretario(a) y al director de carrera. El tutor y cada miembro del tribunal tienen un plazo de revisión, cuya duración y vencimiento están por definir.

## Preconditions

- Hay una versión vigente del documento del proyecto entregada por el estudiante (UC-007).
- El revisor tiene una cuenta de usuario activa y está designado en el trámite como tutor o como miembro del tribunal.

## Basic Flow

1. El revisor indica la versión vigente del documento del proyecto y registra su revisión: el resultado (aprobada u observada), sus observaciones en texto y, si quiere, un PDF anotado.
2. El sistema verifica que la versión esté en la etapa de revisión que le corresponde al revisor (primero el tutor, luego el tribunal evaluador) y que el revisor todavía no la haya revisado.
3. El sistema verifica que una revisión observada tenga observaciones y que el PDF anotado, si lo hay, cumpla CON-004.
4. El sistema registra la revisión.
5. Si el revisor es el tutor y aprobó la versión, el sistema la pasa a la etapa del tribunal evaluador y notifica a sus miembros, a través del Servicio de correo, que hay una versión para revisar.
6. El sistema confirma al revisor que su revisión quedó registrada.

## Alternative Flows

### A1 El tutor observa la versión

En el paso 5 del flujo básico, si el revisor es el tutor y observó la versión:

1. El sistema notifica al estudiante, a través del Servicio de correo, las observaciones del tutor y el PDF anotado, si lo hay.
2. La versión queda observada; el estudiante puede entregar una versión nueva (UC-007).
3. El caso de uso continúa en el paso 6 del flujo básico.

### A2 El tribunal aprueba el documento

En el paso 5 del flujo básico, si el revisor es miembro del tribunal, con su revisión ya revisaron los tres y los tres aprobaron:

1. El sistema registra el documento del proyecto como aprobado.
2. El sistema notifica, a través del Servicio de correo, al estudiante, al secretario(a) de dirección de carrera y al director de carrera que el documento fue aprobado y puede programarse la predefensa o, si es la versión para la defensa final, la defensa final.
3. El caso de uso continúa en el paso 6 del flujo básico.

### A3 El tribunal observa la versión

En el paso 5 del flujo básico, si el revisor es miembro del tribunal, con su revisión ya revisaron los tres y al menos uno observó:

1. El sistema notifica al estudiante, a través del Servicio de correo, el resultado de cada miembro del tribunal con sus observaciones y PDF anotados.
2. La versión queda observada; el estudiante puede entregar una versión nueva (UC-007).
3. El caso de uso continúa en el paso 6 del flujo básico.

## Exceptions

### E1 Revisión no admitida

En el paso 2 del flujo básico, si la versión no es la vigente, si todavía no está en la etapa del revisor (un miembro del tribunal antes de que el tutor la apruebe) o si el revisor ya la revisó:

1. El sistema informa al revisor por qué no admite su revisión.
2. El caso de uso termina sin registrar la revisión.

### E2 Revisión incompleta o PDF anotado inválido

En el paso 3 del flujo básico, si la revisión observada no tiene observaciones, o el PDF anotado no es PDF o supera 25 MB:

1. El sistema informa al revisor qué falta o qué problema tiene el archivo.
2. El caso de uso termina sin registrar la revisión.

## Postconditions

- **Success:** la revisión quedó registrada. Si correspondía, la versión pasó al tribunal evaluador, o el documento quedó aprobado u observado y se notificó al estudiante y, si fue aprobado, a la Dirección de Carrera.
- **Failure:** no se registró ninguna revisión y la versión sigue en la misma etapa.

## Special Requirements (derived)

- CON-004

## Open Questions

- [ ] ¿Cuánto dura el plazo de revisión, quién lo fija y qué pasa cuando vence? Aplica al tutor y a cada miembro del tribunal (ask: Secretario(a) de dirección de carrera)
- [x] ¿Se aceptan observaciones del tribunal por carta, transcritas por el secretario(a)? Respuesta: no; las observaciones solo se registran en el sistema.
