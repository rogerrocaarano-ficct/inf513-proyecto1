# Architecture

| | |
| --- | --- |
| Status | Draft |
| Iteration | I1 |

## Architectural Decisions

| ID | Decision | Rationale | Alternatives considered | Requirements | Status |
| --- | --- | --- | --- | --- | --- |
| AD-001 | La primera entrega es una sola aplicación backend, `mail-app`, que se opera completamente por correo electrónico | La prueba de concepto no tiene interfaz web: cada caso de uso se ejecuta enviando y recibiendo correos | Separar recepción de correos y lógica en dos aplicaciones: agrega despliegue y comunicación sin beneficio para una prueba de concepto | CON-001, NFR-IN-001 | Accepted |
| AD-002 | `mail-app` sigue la arquitectura hexagonal (puertos y adaptadores): el correo y la base de datos son adaptadores; el dominio y la aplicación no dependen de ellos | El correo se reemplaza por la web en la segunda entrega; aislar el dominio de la presentación reduce el cambio y permite probar la lógica sin servidor de correo | Arquitectura en capas: más simple, pero el dominio queda más acoplado a la infraestructura | CON-001, CON-002, NFR-IN-001 | Accepted |
| AD-003 | `mail-app` se construye con Java 25 y Spring Boot 4 | Versiones actuales, elegidas por el equipo | Java 21 (LTS) con Spring Boot 3.5: más estable y con más material, pero más antiguo | CON-001 | Accepted |
| AD-004 | Los datos se guardan en PostgreSQL, en la base `db_grupo03sc` de `db.tecnoweb.org.bo`, con Spring Data JPA (Hibernate) | La base la impone el docente; JPA es el acceso habitual en Spring Boot y reduce el SQL escrito a mano | Spring JDBC / JdbcClient: SQL explícito y control total, pero más código de mapeo | CON-007 | Accepted |
| AD-005 | `mail-app` recibe los correos consultando el buzón por POP3 cada 30 segundos (configurable) con una tarea `@Scheduled` y Jakarta Mail, y envía las respuestas y notificaciones por SMTP con `JavaMailSender`, a través de `mail.tecnoweb.org.bo` | El servidor solo ofrece POP3 sin notificaciones; el sondeo es simple de depurar y 30 segundos dan respuestas rápidas sin cargar el servidor del docente | Spring Integration Mail: adaptador ya hecho, pero suma un framework; sondeo cada 60 segundos: menos carga, respuestas más lentas | CON-006, NFR-IN-001 | Accepted |
| AD-006 | El comando del asunto, `Comando(arg1; arg2; …)`, se interpreta con un analizador propio (expresión regular y división por punto y coma) y un registro que asocia cada nombre de comando a su manejador | La gramática es simple y fija; un analizador propio no agrega dependencias | ANTLR: gramática formal y buenos mensajes de error, desproporcionado para este formato | NFR-IN-001 | Accepted |
| AD-007 | La autorización es propia: el adaptador de correo busca la cuenta de usuario activa por la dirección del remitente, y cada comando declara los roles que lo pueden ejecutar; la participación en un trámite la verifica la capa de aplicación | No hay HTTP ni sesiones; el remitente es la única identidad (NFR-IN-001) | Spring Security con un contexto armado por cada correo y `@PreAuthorize`: reutilizable en la web, pero forzado para el correo | NFR-SE-001, NFR-IN-001 | Accepted |
| AD-008 | La auditoría es un servicio propio: cada caso de uso registra explícitamente sus cambios en el registro de cambios (quién, cuándo, qué dato, valor anterior y nuevo) | El registro de cambios es un concepto del dominio que se consulta en UC-009; un servicio propio decide exactamente qué se guarda | Hibernate Envers: automático por entidad, pero guarda versiones completas en tablas `_AUD` en lugar de cambios legibles | NFR-SE-002, FR-052 | Accepted |
| AD-009 | La segunda entrega reemplaza `mail-app` por una aplicación web con backend en Laravel y frontend en React | Lo exige la restricción del proyecto; sus aplicaciones y estilos se definen cuando empiece esa entrega | — | CON-002 | Proposed |
| AD-010 | Las claves primarias son UUID versión 7 (ordenados por tiempo), generados por la aplicación con `@UuidGenerator(style = VERSION_7)` de Hibernate; las tablas se nombran en plural y en `snake_case` | Los UUID no revelan cuántos registros hay y pueden generarse antes de guardar; la versión 7 mantiene el orden de inserción y evita la fragmentación de índices que causan los UUID aleatorios | `BIGINT` con identidad: más compacto, pero secuencial y expuesto; UUID versión 4: aleatorio, fragmenta los índices | CON-007 | Accepted |

## Applications

| Application | Kind | Architectural style | Technical layers | Subsystem parts it holds | Decision |
| --- | --- | --- | --- | --- | --- |
| mail-app | Backend | Hexagonal (puertos y adaptadores) | Adaptadores de entrada (sondeo POP3, analizador y registro de comandos, autorización por remitente); Aplicación (servicios de casos de uso y puertos); Dominio (entidades y reglas); Adaptadores de salida (persistencia JPA, envío SMTP) | Todo Users, GraduationProcesses, Audit y Messaging | AD-001, AD-002 |

## Design Mechanisms

| Analysis mechanism | Design mechanism | Technology | Decision |
| --- | --- | --- | --- |
| AM-001 Security | Identificación por la dirección del remitente y autorización por los roles que declara cada comando, en el adaptador de entrada; verificación de participación en el trámite en la capa de aplicación | Propia, sin framework de seguridad | AD-007 |
| AM-002 Distribution | Sondeo del buzón, interpretación del comando del asunto y respuesta o notificación por correo, en el subsistema Messaging | Jakarta Mail (POP3), Spring `JavaMailSender` (SMTP), `@Scheduled` | AD-005, AD-006 |
| AM-003 Audit | Servicio de registro de cambios del subsistema Audit, llamado por cada caso de uso dentro de la misma transacción | Spring Data JPA sobre PostgreSQL | AD-008 |

## Architecture Views

| View | Content | Described in |
| --- | --- | --- |
| Use-case view | Architecturally significant use cases | [Use-case catalog](../use-cases/use-cases.md) (Arch. significant = Yes) |
| Logical view | Subsystems, their interfaces and the applications they live in | [Subsystems](subsystems/subsystems.md), [Applications](#applications) |
| Implementation view | Components and their dependencies | Pending: implementation workflow |
| Deployment view | Nodes, infrastructure and allocation of applications | [Deployment](deployment.md) |

## Open Questions

- [ ] AD-003: Spring Boot 4 es reciente; ¿se confirma que sus dependencias (Jakarta Mail, driver de PostgreSQL, Hibernate) funcionan con Java 25 antes de empezar la implementación? (ask: Equipo de desarrollo)
- [ ] AD-001: ¿en qué servidor corre `mail-app` durante las pruebas y la presentación (laboratorio o servidor de despliegue de contingencia)? (ask: Equipo de desarrollo)
