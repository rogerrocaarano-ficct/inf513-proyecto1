# Design

## Context

El `Dockerfile` construye la aplicación y la ejecuta como usuario `cpms` con la carpeta `/var/lib/cpms/storage` creada y sin `VOLUME`. El perfil por defecto (`application.properties`) ya está pensado para producción: POP3 110 y SMTP 25 fijos (NFR-007), y lee de variables de entorno el host y las credenciales del correo, la dirección del sistema, el primer administrador, el semestre vigente y `CPMS_STORAGE_ROOT`; la base se configura con `SPRING_DATASOURCE_*`. `compose.staging.yaml` es el precedente para Dokploy.

## Goals / Non-Goals

**Goals:**

- Un archivo Compose que Dokploy levanta desde `main` con solo variables de entorno cargadas en su interfaz.
- Que un despliegue con configuración incompleta falle antes de arrancar la aplicación.

**Non-Goals:**

- Copias de seguridad de la base o de los archivos (la base es del docente; el volumen se respalda aparte).
- Cambios en el código o en `application.properties`.
- Pipeline de CI o registro de imágenes.

## Decisions

### `compose.production.yaml` con un único servicio `app`

- `build: .` con el mismo `Dockerfile` que staging, sin `SPRING_PROFILES_ACTIVE`: se usa el perfil por defecto. Así `StagingDatabaseReset` y `LocalTestAccounts` no se activan nunca en producción.
- Sin `ports` ni dominio: la aplicación no tiene interfaz HTTP; solo hace conexiones salientes a tecnoweb.
- `restart: unless-stopped`.
- Alternativa descartada: un servicio de tipo Application de Dokploy con el `Dockerfile`. Funciona, pero deja la definición del volumen y las variables solo en la interfaz de Dokploy; el Compose las deja versionadas junto a las de staging.

### Variables de entorno

Obligatorias, con `${VAR:?mensaje}` para que `docker compose` falle si faltan:

| Variable | Uso |
| --- | --- |
| `CPMS_MAIL_ADDRESS` | Dirección del sistema |
| `CPMS_MAIL_USERNAME`, `CPMS_MAIL_PASSWORD` | Buzón POP3 del sistema |
| `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` | Usuario de `db_grupo03sc` |

Con valor por defecto en el Compose, sobreescribibles desde Dokploy:

| Variable | Valor por defecto |
| --- | --- |
| `CPMS_MAIL_POP3_HOST`, `CPMS_MAIL_SMTP_HOST` | `mail.tecnoweb.org.bo` (NFR-007) |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://db.tecnoweb.org.bo:5432/db_grupo03sc` (NFR-008) |
| `CPMS_ADMIN_EMAIL` | vacío (no crea administrador) |
| `CPMS_CURRENT_SEMESTER` | `2026-2` |

`CPMS_STORAGE_ROOT` se fija en el Compose a `/var/lib/cpms/storage`, la carpeta del volumen; no se configura desde Dokploy.

### Volumen con nombre `cpms-storage`

Montado en `/var/lib/cpms/storage`. Al crearse vacío, Docker copia el dueño de la carpeta de la imagen (`cpms`), así que la aplicación puede escribir sin pasos extra. Dokploy conserva los volúmenes con nombre entre despliegues (requisito "Archivos conservados entre despliegues").

### Despliegue en Dokploy

Proyecto Compose, rama `main`, archivo `compose.production.yaml`, despliegue automático al recibir un push; variables en la pestaña Environment, nunca en git. Sin dominio.

## Diferencias con el modelo

Trabajo compartido sin caso de uso y sin diseño en EA; se sigue la arquitectura de referencia. No crea clases, operaciones ni relaciones: solo un archivo de despliegue.

## Risks / Trade-offs

- [El SMTP de tecnoweb valida al remitente por IP (NFR-007): si no acepta la IP del servidor de Dokploy, las respuestas no salen] → El `README.md` lo indica como requisito previo; un envío rechazado queda en el log (`MailSendException`) sin detener la aplicación.
- [El servidor de Dokploy puede no llegar a `mail.tecnoweb.org.bo` o `db.tecnoweb.org.bo` (red universitaria)] → Comprobar la conectividad antes del primer despliegue; si la base no responde, la aplicación no arranca y el log de Dokploy lo muestra.
- [Producción y staging en el mismo servidor] → Proyectos Compose distintos (`cpms-production`, `cpms-staging`), sin puertos publicados ni volúmenes compartidos.
- [Perder el volumen `cpms-storage` deja metadatos en la base sin archivo] → No borrar el volumen al eliminar el proyecto; el `README.md` indica respaldarlo junto con la base.

## Migration Plan

1. Integrar el change en `develop` y luego `develop` en `main`.
2. En Dokploy, crear el proyecto Compose (rama `main`, `compose.production.yaml`) y cargar las variables obligatorias; desplegar.
3. Comprobar en los logs `Started CpmsApplication` y ningún error de conexión POP3 o SMTP; enviar un comando real al buzón del sistema.

Rollback: volver a desplegar un commit anterior de `main` desde Dokploy; el volumen y la base se mantienen.
