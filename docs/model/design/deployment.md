# Deployment

| | |
| --- | --- |
| Status | Draft |
| Iteration | I1 |

## Nodes

| Node | Kind | Platform | Capacity | Environment |
| --- | --- | --- | --- | --- |
| Servidor de pruebas de desarrollo | Server | Docker Engine con Docker Compose | TBD | Development |
| Contenedor `mail-app` | Container | `eclipse-temurin:25-jre` | — | Development |
| Contenedor `postgres` | Container | `postgres:17`, volumen `pgdata` (PostgreSQL 5432) | — | Development |
| Contenedor `mailpit` | Container | `axllent/mailpit:v1.29` (SMTP 1025, POP3 1110, web 8025) | — | Development |
| Servidor de la presentación | Server | TBD: laboratorio o servidor de despliegue de contingencia | TBD | Production |
| `mail.tecnoweb.org.bo` | External system | Servidor de correo del docente (POP3 110, SMTP 25) | — | Production |
| `db.tecnoweb.org.bo` | External system | PostgreSQL del docente, base `db_grupo03sc` | — | Production |
| Cliente de correo del usuario | Client device | Cualquier cliente de correo | — | Development, Production |

## Infrastructure

| Element | Kind | Provider | Purpose |
| --- | --- | --- | --- |
| Red de Docker Compose | Network | Docker | Conecta `mail-app`, `postgres` y `mailpit`; solo se publican los puertos web y SMTP de Mailpit y el de PostgreSQL |
| Volumen `pgdata` | Storage | Docker | Conserva los datos de PostgreSQL entre reinicios del stack |
| Lista de direcciones IP permitidas del SMTP | Firewall | Docente de Tecnología Web | El SMTP del docente solo acepta correos desde las IP registradas (CON-006) |

## Allocation

| Application | Deployable unit | Node | Instances |
| --- | --- | --- | --- |
| mail-app | Imagen de contenedor construida desde el `Dockerfile` de la raíz del repositorio | Contenedor `mail-app` en el servidor de pruebas de desarrollo; en producción, el servidor de la presentación | 1: el buzón se sondea desde un solo proceso (D4 de `esqueleto-mail-app`) |

## Connections

| From | To | Protocol and port | Security | Purpose |
| --- | --- | --- | --- | --- |
| Cliente de correo del usuario | `mailpit` | SMTP 1025 (publicado en el host) | Sin cifrado ni autenticación | Enviar comandos al sistema en desarrollo |
| Navegador del desarrollador | `mailpit` | HTTP 8025 (publicado en el host) | Sin autenticación | Ver los correos y las respuestas en desarrollo |
| `mail-app` | `mailpit` | POP3 1110 | Usuario y contraseña, sin cifrado | Leer los comandos (desarrollo) |
| `mail-app` | `mailpit` | SMTP 1025 | Sin cifrado | Enviar respuestas y avisos (desarrollo) |
| Herramienta de base de datos del desarrollador | `postgres` | PostgreSQL 5432 (publicado en el host) | Usuario y contraseña de `.env` | Revisar los datos en desarrollo |
| `mail-app` | `postgres` | PostgreSQL 5432 | Usuario y contraseña de `.env` | Datos (desarrollo) |
| `mail-app` | `mail.tecnoweb.org.bo` | POP3 110 | Usuario y contraseña del buzón, sin cifrado (CON-006) | Leer los comandos (producción) |
| `mail-app` | `mail.tecnoweb.org.bo` | SMTP 25 | Sin contraseña; el servidor valida la IP de origen (CON-006) | Enviar respuestas y avisos (producción) |
| `mail-app` | `db.tecnoweb.org.bo` | PostgreSQL 5432 | Usuario y contraseña del docente (CON-007) | Datos (producción) |
| Cliente de correo del usuario | `mail.tecnoweb.org.bo` | SMTP / POP3 del proveedor del usuario | Según el proveedor | Enviar comandos y recibir respuestas (producción) |

## Environments

| Environment | Differences from production |
| --- | --- |
| Development | Todo corre en un solo servidor con Docker Compose: PostgreSQL y Mailpit locales en lugar de los servidores del docente, sin lista de IP permitidas. Mailpit guarda todos los correos en un solo buzón y muestra una interfaz web. La configuración está en `.env` (ver `.env.example`) |

## Requirements Check

| Requirement | How the deployment meets it |
| --- | --- |
| CON-006 | En producción `mail-app` usa `mail.tecnoweb.org.bo` con POP3 110 y SMTP 25 sin cifrado; la IP del servidor de la presentación debe estar en la lista del docente |
| CON-007 | En producción `mail-app` usa `db_grupo03sc` en `db.tecnoweb.org.bo`; Flyway crea las tablas (AD-011) |
| NFR-IN-001 | Los comandos llegan por correo al buzón que `mail-app` sondea cada `POLL_INTERVAL` (30 s por defecto) |

## Open Questions

- [ ] Servidor de la presentación: ¿en qué servidor corre `mail-app` durante las pruebas contra los servidores del docente y la presentación (laboratorio o servidor de contingencia), y con qué capacidad? (ask: Equipo de desarrollo)
- [ ] Servidor de pruebas de desarrollo: ¿qué capacidad tiene? (ask: Equipo de desarrollo)
