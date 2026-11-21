# UC-003 — Registrar el perfil de proyecto y asignar tutor

| Campo | Valor |
| --- | --- |
| Estado | Detallado en borrador |
| Iteración | Por confirmar |
| Actores principales | Estudiante para el perfil y Dirección de Carrera para la designación |
| Actor de apoyo | Docente de Taller de Grado, que recibe el perfil y registra su evaluación en UC-004 |
| Prioridad | Must |
| Significativo para la arquitectura | Sí. Integra adjuntos, historial de perfiles y una transición que depende de otro caso de uso |
| Caso aprobado | [UC-003 en README.md](README.md#casos-de-uso-aprobados) |
| Requisitos funcionales | FR-003, FR-004, FR-020, FR-023, FR-057 y FR-058 |

Los flujos siguen [use-case.md](use-case.md) y [REQUIREMENTS.md](REQUIREMENTS.md). Los nombres de comandos, argumentos y ejemplos son propuestas para revisión con el modelo de EA. La sintaxis común se toma de [USER-GUIDE.md](USER-GUIDE.md).

## Descripción breve

El estudiante presenta el título y su perfil de proyecto. El sistema avisa al docente de Taller cada vez que admite un perfil. Tras el visto bueno registrado en UC-004, Dirección designa al tutor y el estudiante recibe la notificación.

## Precondiciones

- El estudiante tiene un trámite abierto mediante UC-002 y envía el perfil desde su cuenta de estudiante habilitada.
- Dirección cuenta con una cuenta habilitada con rol `director` para registrar la designación. La designación requiere el visto bueno del perfil en UC-004.

## Flujo básico

1. El estudiante envía el comando de presentación con el título y el perfil adjunto.
2. El sistema identifica al estudiante por su correo, comprueba que tiene un trámite abierto y valida el título y el adjunto.
3. Como es la primera presentación, el sistema crea el proyecto de grado y registra el título y el perfil.
4. El sistema confirma la presentación al estudiante y notifica al docente de Taller de Grado.
5. En UC-004, el docente de Taller registra el visto bueno del perfil.
6. Dirección envía el comando de designación, identificando el trámite y el docente elegido.
7. El sistema verifica la autorización, el visto bueno y que la persona designada pueda actuar como docente. Registra el tutor para el trámite. La nota de FR-003 sitúa la asignación junto con la designación del tribunal.
8. El sistema confirma la designación a Dirección y la notifica al estudiante.

## Flujos alternativos

### A1 — Presentar un nuevo perfil

En el paso 3, si el último perfil fue rechazado u observado en UC-004, el sistema registra un nuevo perfil en el mismo proyecto y conserva los anteriores. La nueva presentación puede cambiar el título. El flujo continúa en el paso 4 con un nuevo aviso a Taller.

## Excepciones

| Punto | Condición | Respuesta y resultado |
| --- | --- | --- |
| 2 o 7 | Remitente sin cuenta o permiso, asunto inválido, cantidad incorrecta o datos inválidos | Informa el motivo según la guía de comandos y termina la operación. |
| 2 | Estudiante sin trámite abierto, título vacío o adjunto inválido | Indica el motivo del rechazo y conserva el proyecto previo, si existe. |
| 3 | Ya hay un perfil y el último no fue rechazado ni observado | Rechaza la nueva presentación e informa la condición para volver a presentarlo. |
| 7 | Trámite no localizable o fuera del alcance de Dirección, perfil sin visto bueno o docente no elegible | Rechaza la designación e informa el motivo aplicable. |

## Postcondiciones

- **Éxito de presentación.** El perfil queda registrado en el proyecto, los anteriores se conservan y Taller recibe el aviso.
- **Éxito de designación.** El tutor queda registrado para ese trámite y el estudiante recibe el aviso. La designación no cambia el rol de la cuenta del docente.
- **Fallo.** La operación rechazada no registra un perfil ni cambia un título o una designación existentes. Si la designación falla, el perfil ya presentado se conserva.

## Requisitos especiales

- NFR-001, NFR-002 y NFR-003. Errores detallados, autorización por rol y alcance, comandos en el asunto y clasificación de adjuntos por palabras en el nombre.
- NFR-005. Límite de 25 MB si el perfil se admite en PDF.

## Contratos base de correo

El destinatario local es `sistema@cpms.local`. El cuerpo se ignora. Se aplican las respuestas comunes de la guía de comandos y los rechazos descritos arriba.

### Presentar perfil

`proyectos.presentarPerfil[titulo]`

**Remitente:** estudiante titular del trámite. **Argumentos:** un título obligatorio en texto, con las restricciones generales del asunto. El trámite se identifica a partir de la cuenta remitente. **Adjuntos:** uno. Propuesta para el perfil: `perfil.pdf`, hasta 25 MB. **Resultado:** confirma el registro al estudiante y avisa a Taller de Grado tanto en la primera presentación como en las posteriores admitidas.

```text
proyectos.presentarPerfil[Sistema de seguimiento de trámites]
```

### Asignar tutor

`tramites.asignarTutor[referenciaTramite; correoDocente]`

**Remitente:** Dirección de Carrera. **Argumentos:** referencia obligatoria del trámite, con el mismo formato que devuelve UC-002, y correo válido de la cuenta de la persona designada. Según la nota de FR-026 y FR-036, el director también puede actuar como docente. **Adjuntos:** ninguno. **Resultado:** registra al tutor, confirma a Dirección y notifica la designación al estudiante según FR-020.

```text
tramites.asignarTutor[TR-001; luis@cpms.local]
```

## Preguntas abiertas

- [ ] Confirmar con el responsable del modelo la sintaxis de los comandos, el formato admitido para el perfil y las reglas de longitud del título.
- [ ] Confirmar en EA si tutor y tribunal se registran en una sola operación o por separado. El contrato de asignación individual es provisional.
- [ ] Confirmar en EA los criterios de elegibilidad del tutor, el comportamiento ante una designación existente y si corresponde incluir la consulta de FR-059 en este caso de uso.
