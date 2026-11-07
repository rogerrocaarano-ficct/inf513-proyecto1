# Project Plan

| | |
| --- | --- |
| Status | Draft |
| Iteration | E1 |

## Team

| Member | Role |
| --- | --- |
| Roger Roca Arano | Guía el desarrollo: arquitectura, integración y revisión de lo que llega a `develop` |
| Francisco Javier Cabrera Claros | Desarrollo por casos de uso |
| Alejandro de los Rios Arancibia | Desarrollo por casos de uso |

El equipo trata con el docente como grupo, sin un responsable único (SRC-011).

## Milestones

| Milestone | Date | Source | Status |
| --- | --- | --- | --- |
| Presentación del alcance al docente para su aprobación | 2026-11-04 o 2026-11-05 | SRC-011 | Planned |
| Primera entrega: prueba de concepto por correo con los diez casos de uso | 2026-11-17 o 2026-11-19 | CON-001, CON-003, SRC-011 | Planned |
| Segunda entrega: aplicación web | TBD | CON-002, CON-003 | Planned |

## Iterations

| Iteration | Phase | Start | End | Goal | Status |
| --- | --- | --- | --- | --- | --- |
| I1 | Inception | 2026-10-26 | 2026-11-07 | Alcance aprobado por el docente, arquitectura candidata, riesgos priorizados y plan de Elaboración | Closed |
| E1 | Elaboration | 2026-11-07 | 2026-11-09 | Los casos de uso significativos (UC-001, UC-003, UC-004) completamente validados contra los servicios del docente (correo y base de datos) y los riesgos de infraestructura reducidos | In progress |
| E2 | Elaboration | 2026-11-10 | 2026-11-12 | Las reglas de negocio resueltas, los casos de uso propuestos validados con el docente y los casos de uso restantes detallados | Planned |
| C1 | Construction | 2026-11-13 | 2026-11-16 | Los siete casos de uso restantes implementados e integrados, y la primera entrega ensayada | Planned |
| — | — | TBD | TBD | Iteraciones de la segunda entrega, en el repositorio de la versión web (AD-009) | Planned |

## Iteration I1

### Work Items

| Item | Work | Workflows | Owner | Estimate | Due | Risks | Status |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Visión y casos de uso | Escribir la visión, los actores, los casos de uso y los requisitos | Requirements | Equipo | — | 2026-10-27 | — | Done |
| UC-001, UC-003, UC-004 | Detallar, analizar, diseñar e implementar los casos de uso significativos | All | Equipo | — | 2026-11-03 | — | Done |
| Arquitectura candidata | Decisiones arquitectónicas, subsistemas, despliegue y base de datos | Design | Equipo | — | 2026-10-29 | — | Done |
| Lista de riesgos | Identificar y priorizar los riesgos, con su estrategia | — | Roger Roca Arano | 0,5 | 2026-11-03 | — | Done |
| Plan del proyecto | Escribir este plan, con E1, E2 y C1 en detalle | — | Roger Roca Arano | 0,5 | 2026-11-03 | RSK-002, RSK-005 | Done |
| Aprobación del alcance | Presentar al docente FEAT-001…FEAT-008 y los casos de uso derivados de sus CU1–CU8, y registrar su decisión en la tabla Approval de la visión. El equipo da el alcance por aceptado mientras el docente no lo objete (SRC-012) | Requirements | Equipo | 0,5 | 2026-11-06 | RSK-003 | Done |
| Fechas de entrega | Preguntar al docente el día de la primera entrega y la fecha de la segunda. Pasa a E1 | — | Equipo | — | 2026-11-07 | RSK-002 | Moved |

### Exit Criteria

- [x] Los interesados aceptan el alcance: el equipo lo da por aceptado mientras el docente no lo objete (`../requirements/vision.md`, Approval; SRC-012), y si lo objeta ajusta los casos de uso (RSK-003).
- [x] Los casos de uso críticos están identificados y descritos: UC-001, UC-003 y UC-004 están Detailed.
- [x] Existe una arquitectura candidata viable: `../design/architecture.md`, y ya funciona con los tres casos de uso significativos.
- [x] Los riesgos principales están priorizados y tienen estrategia: `risks.md`.
- [x] Las estimaciones son razonables y el plan de Elaboración es creíble: este plan, con E1, E2 y C1 en detalle, aceptado por el equipo el 2026-11-06.

## Iteration E1

### Work Items

| Item | Work | Workflows | Owner | Estimate | Due | Risks | Status |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Servidor de correo | Verificar que el SMTP acepta las IP del equipo, probar la cadena completa (POP3, comando, respuesta SMTP) contra `mail.tecnoweb.org.bo` y enviar un adjunto de 25 MB | Testing | Roger Roca Arano | 0,5 | 2026-11-09 | RSK-001, RSK-008 | Pending |
| Base de datos | Aplicar las migraciones V1…V5 contra `db_grupo03sc` y resolver los permisos que falten | Implementation, Testing | Roger Roca Arano | 0,5 | 2026-11-09 | RSK-004 | Pending |
| Requisitos no funcionales | Proponer criterios de aceptación medibles y validarlos con el docente | Requirements | Roger Roca Arano | 0,5 | 2026-11-09 | RSK-007 | Pending |
| Fechas de entrega | Preguntar al docente el día de la primera entrega y la fecha de la segunda (viene de I1) | — | Equipo | — | 2026-11-09 | RSK-002 | Pending |
| UC-001, UC-003, UC-004 | Probar la implementación inicial contra los servidores del docente y corregir lo que falle, en el código y en el caso de uso y las realizaciones afectadas | All | Roger Roca Arano | 1,5 | 2026-11-09 | — | Pending |

### Exit Criteria

- [ ] La arquitectura es estable: no se esperan cambios estructurales mayores.
- [ ] Los casos de uso significativos (UC-001, UC-003, UC-004) están implementados y completamente validados en la línea base contra los servidores del docente.
- [ ] Los riesgos de infraestructura (RSK-001, RSK-004, RSK-008) están mitigados o tienen un plan claro.

## Iteration E2

### Work Items

| Item | Work | Workflows | Owner | Estimate | Due | Risks | Status |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Reglas de negocio | Reunir todas las preguntas abiertas sobre las reglas de negocio para el secretario(a) y el director de carrera, obtener sus respuestas y registrarlas donde corresponda | Requirements | Equipo | 0,5 | 2026-11-12 | RSK-006 | Pending |
| Validación de los casos de uso | Validar con el docente los casos de uso propuestos (UC-001…UC-010, derivados de sus CU1–CU8) y registrar su decisión | Requirements | Equipo | 0,5 | 2026-11-12 | RSK-003 | Pending |
| UC-002, UC-005…UC-010 | Detallar los casos de uso restantes (Detailed) | Requirements | Roger Roca Arano | 2 | 2026-11-12 | RSK-005, RSK-006 | Pending |

### Exit Criteria

- [ ] Los riesgos principales están mitigados o tienen un plan claro.
- [ ] El modelo de casos de uso está casi completo y los requisitos son estables: el docente validó los casos de uso propuestos, las reglas de negocio están resueltas y los casos de uso restantes están Detailed.
- [ ] El plan de Construcción es realista y el equipo lo acepta.

## Iteration C1

### Work Items

| Item | Work | Workflows | Owner | Estimate | Due | Risks | Status |
| --- | --- | --- | --- | --- | --- | --- | --- |
| UC-005 | Gestionar tribunal evaluador y tutor | All | Roger Roca Arano | 1 | 2026-11-13 | RSK-005, RSK-006 | Pending |
| UC-006 | Evaluar proyecto de grado, con las etapas que dejaron abiertas UC-003 y UC-004 | All | Roger Roca Arano | 1,5 | 2026-11-15 | RSK-005, RSK-006 | Pending |
| UC-007 | Gestionar cronograma de defensas | All | Francisco Javier Cabrera Claros | 1,5 | 2026-11-14 | RSK-005 | Pending |
| UC-008 | Consultar trámite de titulación | All | Francisco Javier Cabrera Claros | 1 | 2026-11-15 | RSK-005 | Pending |
| UC-002 | Gestionar datos personales | All | Alejandro de los Rios Arancibia | 0,5 | 2026-11-13 | RSK-005 | Pending |
| UC-009 | Consultar registro de cambios | All | Alejandro de los Rios Arancibia | 0,5 | 2026-11-13 | RSK-005 | Pending |
| UC-010 | Consultar estadísticas | All | Alejandro de los Rios Arancibia | 1,5 | 2026-11-15 | RSK-005, RSK-006 | Pending |
| Pruebas del sistema | Probar de punta a punta los diez casos de uso por correo contra los servidores del docente | Testing | Equipo | 0,5 | 2026-11-16 | RSK-001, RSK-004 | Pending |
| Guía de comandos | Escribir la guía de comandos para los usuarios y la presentación: cada integrante documenta los comandos de sus casos de uso | — | Equipo | 0,5 | 2026-11-15 | RSK-010 | Pending |
| Ensayo | Ensayar la presentación desde el laboratorio | Testing | Equipo | 0,5 | 2026-11-16 | RSK-001 | Pending |

Si la primera entrega es el 2026-11-19, el 2026-11-17 y el 2026-11-18 son margen para terminar el trabajo de C1 que quede pendiente, sin trabajo nuevo planificado. El fin de C1 no se mueve.

### Exit Criteria

- [ ] Los diez casos de uso acordados están implementados.
- [ ] El producto es estable y pasó las pruebas del sistema contra los servidores del docente.
- [ ] La prueba de concepto está lista para la primera entrega.
- [ ] Existe la guía de comandos para los usuarios.
- [ ] El docente acepta la primera entrega.

## Open Questions

- [x] E1, C1: ¿los tres integrantes pueden trabajar el fin de semana del 7 y 8 de noviembre? E1 termina un sábado y C1 empieza un domingo (ask: Equipo de desarrollo). Respuesta: E1 va del 2026-11-07 al 2026-11-09, E2 del 2026-11-10 al 2026-11-12 y C1 del 2026-11-13 al 2026-11-16
- [x] E2: ¿cuánto estima cada trabajo de E2? (ask: Equipo de desarrollo). Respuesta: se ajustaron a los tres días de E2; Roger Roca Arano las validó
- [x] C1: con C1 entre el 2026-11-14 y el 2026-11-16, las estimaciones de sus trabajos (13,5 días de casos de uso entre tres integrantes en tres días) no entran; ¿se reparten de nuevo, se recortan o se mueve el fin de C1? (ask: Equipo de desarrollo). Respuesta: C1 empieza el 2026-11-13 y las estimaciones se ajustaron a sus cuatro días (dos y medio por integrante para sus casos de uso, medio para la guía de comandos y uno para las pruebas y el ensayo); Roger Roca Arano las validó
- [x] C1: si la primera entrega es el 2026-11-19, ¿se usan los dos días extra como margen o se mueve el fin de C1? (ask: Equipo de desarrollo). Respuesta: son margen para el trabajo pendiente de C1 (Iteration C1)
