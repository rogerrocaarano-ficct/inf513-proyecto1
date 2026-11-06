# Project Plan

| | |
| --- | --- |
| Status | Draft |
| Iteration | I1 |

## Team

| Member | Role |
| --- | --- |
| Integrante A | Guía el desarrollo: arquitectura, integración y revisión de lo que llega a `develop` |
| Integrante B | Desarrollo por casos de uso |
| Integrante C | Desarrollo por casos de uso |

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
| I1 | Inception | 2026-10-26 | 2026-11-05 | Alcance aprobado por el docente, arquitectura candidata, riesgos priorizados y plan de Elaboración | In progress |
| E1 | Elaboration | 2026-11-05 | 2026-11-07 | La línea base ejecutable (UC-001, UC-003, UC-004) probada contra los servidores del docente y los riesgos de infraestructura reducidos | Planned |
| C1 | Construction | 2026-11-08 | 2026-11-16 | Los siete casos de uso restantes implementados e integrados, y la primera entrega ensayada | Planned |
| — | — | TBD | TBD | Iteraciones de la segunda entrega, en el repositorio de la versión web (AD-009) | Planned |

## Iteration I1

### Work Items

| Item | Work | Workflows | Owner | Estimate | Due | Risks | Status |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Visión y casos de uso | Escribir la visión, los actores, los casos de uso y los requisitos | Requirements | Equipo | — | 2026-10-27 | — | Done |
| UC-001, UC-003, UC-004 | Detallar, analizar, diseñar e implementar los casos de uso significativos | All | Equipo | — | 2026-11-03 | — | Done |
| Arquitectura candidata | Decisiones arquitectónicas, subsistemas, despliegue y base de datos | Design | Equipo | — | 2026-10-29 | — | Done |
| Lista de riesgos | Identificar y priorizar los riesgos, con su estrategia | — | Integrante A | 0,5 | 2026-11-03 | — | Done |
| Plan del proyecto | Escribir este plan, con E1 y C1 en detalle | — | Integrante A | 0,5 | 2026-11-03 | RSK-002, RSK-005 | In progress |
| Aprobación del alcance | Presentar al docente FEAT-001…FEAT-008 y los casos de uso derivados de sus CU1–CU8, y registrar su decisión en la tabla Approval de la visión. El equipo da el alcance por aceptado mientras el docente no lo objete (SRC-012) | Requirements | Equipo | 0,5 | 2026-11-06 | RSK-003 | Done |
| Fechas de entrega | Preguntar al docente el día de la primera entrega y la fecha de la segunda | — | Equipo | — | 2026-11-05 | RSK-002 | Pending |

### Exit Criteria

- [x] Los interesados aceptan el alcance: el equipo lo da por aceptado mientras el docente no lo objete (`../requirements/vision.md`, Approval; SRC-012), y si lo objeta ajusta los casos de uso (RSK-003).
- [x] Los casos de uso críticos están identificados y descritos: UC-001, UC-003 y UC-004 están Detailed.
- [x] Existe una arquitectura candidata viable: `../design/architecture.md`, y ya funciona con los tres casos de uso significativos.
- [x] Los riesgos principales están priorizados y tienen estrategia: `risks.md`.
- [ ] Las estimaciones son razonables y el plan de Elaboración es creíble: este plan, cuando el equipo lo acepte.

## Iteration E1

### Work Items

| Item | Work | Workflows | Owner | Estimate | Due | Risks | Status |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Servidor de correo | Verificar que el SMTP acepta las IP del equipo, probar la cadena completa (POP3, comando, respuesta SMTP) contra `mail.tecnoweb.org.bo` y enviar un adjunto de 25 MB | Testing | Integrante A | 1 | 2026-11-07 | RSK-001, RSK-008 | Pending |
| Base de datos | Aplicar las migraciones V1…V5 contra `db_grupo03sc` y resolver los permisos que falten | Implementation, Testing | Integrante B | 1 | 2026-11-07 | RSK-004 | Pending |
| Requisitos no funcionales | Proponer criterios de aceptación medibles y validarlos con el docente | Requirements | Integrante C | 0,5 | 2026-11-07 | RSK-007 | Pending |
| Reglas de negocio | Reunir las preguntas abiertas para el secretario(a) y el director de carrera y obtener sus respuestas | Requirements | Equipo | 0,5 | 2026-11-07 | RSK-006 | Pending |
| UC-001, UC-003, UC-004 | Ejecutar las pruebas y revisar las realizaciones de análisis y diseño contra sus casos de uso (Status Reviewed) | Analysis, Design, Testing | Integrante A | 0,5 | 2026-11-07 | — | Pending |
| UC-002, UC-005, UC-007 | Detallar los casos de uso con los que empieza C1 | Requirements | Integrante C, A, B | 0,5 | 2026-11-07 | RSK-005 | Pending |

### Exit Criteria

- [ ] La arquitectura es estable: no se esperan cambios estructurales mayores.
- [ ] Los casos de uso significativos están implementados y probados en la línea base, también contra los servidores del docente.
- [ ] Los riesgos principales están mitigados o tienen un plan claro.
- [ ] El modelo de casos de uso está casi completo y los requisitos son estables.
- [ ] El plan de Construcción es realista y el equipo lo acepta.

## Iteration C1

### Work Items

| Item | Work | Workflows | Owner | Estimate | Due | Risks | Status |
| --- | --- | --- | --- | --- | --- | --- | --- |
| UC-005 | Gestionar tribunal evaluador y tutor | All | Integrante A | 2 | 2026-11-10 | RSK-005, RSK-006 | Pending |
| UC-006 | Evaluar proyecto de grado, con las etapas que dejaron abiertas UC-003 y UC-004 | All | Integrante A | 3 | 2026-11-13 | RSK-005, RSK-006 | Pending |
| UC-007 | Gestionar cronograma de defensas | All | Integrante B | 2 | 2026-11-10 | RSK-005 | Pending |
| UC-008 | Consultar trámite de titulación | All | Integrante B | 2 | 2026-11-13 | RSK-005 | Pending |
| UC-002 | Gestionar datos personales | All | Integrante C | 1 | 2026-11-09 | RSK-005 | Pending |
| UC-009 | Consultar registro de cambios | All | Integrante C | 1,5 | 2026-11-11 | RSK-005 | Pending |
| UC-010 | Consultar estadísticas | All | Integrante C | 2 | 2026-11-13 | RSK-005, RSK-006 | Pending |
| Pruebas del sistema | Probar de punta a punta los diez casos de uso por correo contra los servidores del docente | Testing | Equipo | 1 | 2026-11-15 | RSK-001, RSK-004 | Pending |
| Guía de comandos | Escribir la guía de comandos para los usuarios y la presentación | — | Integrante B | 0,5 | 2026-11-15 | RSK-010 | Pending |
| Ensayo | Ensayar la presentación desde el laboratorio | Testing | Equipo | 0,5 | 2026-11-16 | RSK-001 | Pending |

### Exit Criteria

- [ ] Los diez casos de uso acordados están implementados.
- [ ] El producto es estable y pasó las pruebas del sistema contra los servidores del docente.
- [ ] La prueba de concepto está lista para la primera entrega.
- [ ] Existe la guía de comandos para los usuarios.
- [ ] El docente acepta la primera entrega.

## Open Questions

- [ ] C1: ¿los tres integrantes pueden trabajar el fin de semana del 7 y 8 de noviembre? E1 termina un sábado y C1 empieza un domingo (ask: Equipo de desarrollo)
- [ ] C1: si la primera entrega es el 2026-11-19, ¿se usan los dos días extra como margen o se mueve el fin de C1? (ask: Equipo de desarrollo)
