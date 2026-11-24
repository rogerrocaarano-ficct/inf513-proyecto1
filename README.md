# Sistema de Gestión de Trámites de Proyecto de Grado

Proyecto de la materia Tecnología Web (INF513): Sistema de Gestión de Trámites de Proyecto de Grado para la carrera de Ingeniería en Redes y Telecomunicaciones, Facultad de ingeniería en Ciencias de la Computación y Telecomunicaciones (FICCT), Universidad Autónoma Gabriel René Moreno (UAGRM).

## Documentación

El problema que resuelve el sistema, el flujo del trámite, los casos de uso aprobados, su alcance y el plan del proyecto (equipo, hitos, iteraciones y riesgos) están en la [documentación](docs/README.md), junto con los requisitos, el glosario y la guía de comandos.

## Entorno de desarrollo

### Requisitos

| Requisito | Versión | Para qué |
| --- | --- | --- |
| Git | cualquiera reciente | Clonar el repositorio y trabajar con las ramas |
| JDK | 25 (por ejemplo, Eclipse Temurin 25) | Compilar y ejecutar; Gradle no lo descarga, tiene que estar instalado |
| Docker | Docker Desktop (Windows o macOS) o Docker Engine con Compose v2 (Linux) | Base de datos, servidor de correo y webmail locales; también lo usan las pruebas |
| Navegador | cualquiera | Webmail para enviar comandos y leer las respuestas |

No hace falta instalar Gradle: el repositorio trae su wrapper (`./gradlew`, o `gradlew.bat` en Windows).

Puertos que deben estar libres en la máquina: **8025** (webmail), **2525**, **1110** y **1143** (correo local) y **15432** (base de datos) para el entorno de pruebas manuales, y **3025**, **3110** y **3143** para las pruebas automáticas.

### Comprobar la instalación

Con Docker en ejecución, desde la raíz del repositorio:

```bash
./gradlew build
```

Compila el proyecto y ejecuta todas las pruebas automáticas. Las pruebas levantan su propio PostgreSQL con Testcontainers y su propio servidor de correo embebido; no necesitan el entorno de pruebas manuales.

### Levantar el stack de pruebas manuales

```bash
./gradlew bootRun
```

Este comando levanta los contenedores de `compose.yaml` y arranca la aplicación con el perfil `local` (`src/main/resources/application-local.properties`). La primera vez descarga las imágenes, así que tarda más.

| Servicio | Dirección | Uso |
| --- | --- | --- |
| PostgreSQL 17 | `localhost:15432`, base `cpms`, usuario `cpms`, contraseña `cpms` | Base de datos de la aplicación; se vacía en cada arranque de la aplicación |
| GreenMail | SMTP `localhost:2525`, POP3 `localhost:1110`, IMAP `localhost:1143` | Servidor de correo con un buzón por dirección; el buzón del sistema es `sistema@cpms.local` |
| Roundcube | <http://localhost:8025> | Webmail para enviar comandos y leer las respuestas |

Los archivos que guarda la aplicación (documentos adjuntos y sus versiones) van a la carpeta `data/storage` de la raíz del repositorio, que git ignora. La base de datos guarda solo sus datos descriptivos, así que la carpeta y la base van juntas.

**El entorno local es desechable**, igual que el [entorno de pruebas desplegado](#entorno-de-pruebas-desplegado). Cada vez que arranca la aplicación se vacían la base y la carpeta `data/storage`, se aplican las migraciones desde cero y se vuelven a crear las cuentas de prueba. Los trámites, las cuentas creadas por correo y los documentos de una sesión no pasan a la siguiente. Esto incluye los reinicios automáticos de Spring Boot DevTools: cada vez que cambia el código compilado mientras `bootRun` está en ejecución, la aplicación se reinicia y arranca sin datos. El log lo muestra con `Successfully cleaned schema` y `Emptied the storage folder`.

El semestre vigente, que forma parte de la referencia de cada trámite (por ejemplo `2026-2-218001234`), se toma de la variable de entorno `CPMS_CURRENT_SEMESTER` con la forma `<año>-<periodo>`; sin ella vale `2026-2`. Hay que cambiarla al empezar cada semestre, hasta que exista la gestión del calendario académico. Con un valor inválido la aplicación no arranca.

La aplicación está lista cuando la consola muestra `Started CpmsApplication`.

### Probar un comando

Al arrancar, la aplicación crea las cuentas de prueba del entorno local, todas activas y en el dominio `test.local`. Como la base empieza vacía, en cada arranque vuelven a su estado inicial: una cuenta desactivada en la sesión anterior aparece activa de nuevo. Al crearlas no se envía ningún aviso.

| Correo | Rol |
| --- | --- |
| `admin@test.local` | administrador |
| `administrativo@test.local` | administrativo |
| `director@test.local` | director |
| `taller@test.local` | docente de Taller de Grado |
| `docente1@test.local` … `docente5@test.local` | docente |
| `estudiante1@test.local` … `estudiante3@test.local` | estudiante |

El sistema solo atiende comandos de direcciones que tienen cuenta; a cualquier otra le responde que no está registrada.

1. Abrir <http://localhost:8025>.
2. Iniciar sesión con una de las cuentas de prueba, por ejemplo `admin@test.local`, con cualquier contraseña. Cada dirección tiene su propio buzón, así que para actuar como otro usuario se cierra la sesión y se entra con su dirección.
3. Redactar un correo a `sistema@cpms.local` con el comando en el asunto. Por ejemplo, para listar los docentes:

   ```text
   usuarios.listar[docente]
   ```

4. La aplicación revisa el buzón cada 10 segundos. La respuesta llega a la bandeja de entrada de quien envió el comando; cuando el comando crea una cuenta, la cuenta nueva recibe además un aviso en su propio buzón.
5. Entrar como `director@test.local` y crear, por ejemplo, la cuenta de otro docente: `usuarios.crearCuenta[Luis; Rojas; ; ; ; luis@test.local; ; docente]`.

La sintaxis de los comandos, cada comando con sus argumentos y las respuestas del sistema están en la [guía de comandos](docs/user-guide/README.md).

La consola muestra el tráfico del canal de correo:

| Línea | Cuándo |
| --- | --- |
| `Connecting to POP3 server …` | En cada ciclo, al conectarse al buzón (solo en el perfil `local`) |
| `POP3 inbox has N messages; read M` | Al terminar de leer el buzón (con el buzón vacío, solo en el perfil `local`) |
| `Received email <uid> from <remitente>: action <comando> (N attachments)` | Por cada correo que entra; `no valid action` si el asunto no es un comando |
| `Ignoring automatic email <uid> from <remitente>` | Por cada rebote o respuesta automática que se descarta |
| `Deleted message <uid> from the POP3 server` | Al borrar del servidor un correo ya atendido |
| `Sent email to <destino> (N attachments)` | Por cada correo que sale por SMTP |

Los logs nunca muestran el asunto ni los argumentos del comando, porque contienen datos personales.

### Detener y reiniciar

- `Ctrl+C` detiene la aplicación. Los contenedores siguen en ejecución, así que el siguiente `./gradlew bootRun` arranca más rápido y conserva los correos de los buzones; la base y los archivos se vacían igual.
- `docker compose down` detiene los contenedores. `docker compose down -v` además borra el volumen de la base; no hace falta para empezar de cero, porque cada arranque ya lo hace.

GreenMail guarda los correos en memoria: se pierden cada vez que se reinicia su contenedor, y al recrearlo vuelve a numerar los mensajes desde 1. Como la base se vacía en cada arranque, basta con volver a ejecutar `./gradlew bootRun` después de recrear GreenMail.

### Problemas frecuentes

| Síntoma | Causa y solución |
| --- | --- |
| `bootRun` falla con un error de Docker Compose | Docker no está en ejecución: iniciar Docker Desktop y volver a ejecutar |
| Un contenedor no arranca porque un puerto está ocupado | Otro programa usa ese puerto: liberarlo, o cambiarlo en `compose.yaml` y en `application-local.properties` |
| Un cliente de base de datos no conecta a `localhost:15432` | El contenedor de PostgreSQL se creó antes de fijar ese puerto: recrearlo con `docker compose up -d postgres` (los datos se conservan) |
| Un comando no recibe respuesta y el log muestra `Deleting message … without executing it` | Se recreó GreenMail mientras la aplicación seguía en ejecución: detenerla con `Ctrl+C` y volver a ejecutar `./gradlew bootRun`, que vacía la base |
| Los datos de la sesión anterior ya no están | Es lo esperado: el entorno local se vacía en cada arranque, también en los reinicios de DevTools |
| El build pide Java 25 | Instalar un JDK 25 y comprobar con `java -version` |

### Entorno de pruebas desplegado

Para que los testers prueben los comandos sin instalar nada, el mismo stack corre en el servidor Dokploy del equipo con `compose.staging.yaml`: la aplicación (imagen del `Dockerfile`, perfil `staging`), PostgreSQL 17, GreenMail y Roundcube. Solo el webmail es accesible desde internet; el servidor de correo y la base no publican ningún puerto.

**El entorno es desechable.** Cada vez que arranca la aplicación (en cada despliegue o reinicio) se vacían la base y los archivos, se aplican las migraciones desde cero y se vuelven a crear las cuentas de prueba. Los trámites, cuentas creadas y correos de una sesión de pruebas no se conservan. El webmail no pide contraseña real: no cargar datos personales reales.

#### Configurar el despliegue en Dokploy

1. Crear un servicio de tipo **Compose** con el repositorio de GitHub, rama `develop` y archivo `compose.staging.yaml`. Activar el despliegue automático para que cada merge a `develop` despliegue la versión nueva.
2. En **Domains**, agregar un dominio para el servicio `roundcube`, puerto `80` (con HTTPS si el dominio lo permite).
3. Opcional, en **Environment**:

   | Variable | Valor por defecto | Para qué |
   | --- | --- | --- |
   | `POSTGRES_PASSWORD` | `cpms` | Contraseña de la base interna |
   | `ROUNDCUBEMAIL_DES_KEY` | fija en el Compose | Clave de 24 caracteres de las sesiones del webmail |
   | `CPMS_CURRENT_SEMESTER` | `2026-2` | Semestre vigente, como en el entorno local |

4. Desplegar. El log del servicio `app` muestra `Successfully applied N migrations`, `Started CpmsApplication` y `Created 11 local test accounts`.

#### Probar desde el entorno desplegado

Los testers abren el dominio del webmail y siguen los pasos de [Probar un comando](#probar-un-comando): mismas cuentas de `test.local`, cualquier contraseña, y comandos dirigidos a `sistema@cpms.local`.

Si un comando no recibe respuesta y el log de `app` muestra `Deleting message … without executing it`, GreenMail se reinició solo y la aplicación no: reiniciar el servicio `app` desde Dokploy, lo que también vacía el entorno.

### Ejecutar contra el servidor de producción

En producción corre solo la aplicación, con el perfil por defecto: usa el servidor de correo (NFR-007) y la base de datos (NFR-008) de tecnoweb. Se despliega en el servidor Dokploy del equipo con `compose.production.yaml`, desde la rama `main`. La base y los archivos se conservan: nunca se limpian al arrancar.

#### Requisitos previos

- El SMTP de `mail.tecnoweb.org.bo` valida al remitente por su dirección IP (NFR-007): la IP del servidor de Dokploy tiene que estar aceptada, o las respuestas no salen (el log muestra el envío rechazado).
- El servidor de Dokploy tiene que llegar a `mail.tecnoweb.org.bo` (puertos 110 y 25) y a `db.tecnoweb.org.bo` (puerto 5432). Sin la base, la aplicación no arranca.

#### Configurar el despliegue en Dokploy

1. Crear un servicio de tipo **Compose** con el repositorio de GitHub, rama `main` y archivo `compose.production.yaml`. No lleva dominio: la aplicación no tiene interfaz web.
2. En **Environment**, cargar las variables. Las obligatorias detienen el despliegue si faltan:

   | Variable | Obligatoria | Valor |
   | --- | --- | --- |
   | `CPMS_MAIL_ADDRESS` | sí | Dirección del sistema, remitente de las respuestas |
   | `CPMS_MAIL_USERNAME`, `CPMS_MAIL_PASSWORD` | sí | Usuario y contraseña del buzón del sistema |
   | `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` | sí | Usuario y contraseña de la base que entrega el docente |
   | `CPMS_MAIL_POP3_HOST`, `CPMS_MAIL_SMTP_HOST` | no | Por defecto `mail.tecnoweb.org.bo` |
   | `SPRING_DATASOURCE_URL` | no | Por defecto `jdbc:postgresql://db.tecnoweb.org.bo:5432/db_grupo03sc` |
   | `CPMS_ADMIN_EMAIL` | no | Primer administrador, creado al arrancar si no existe; vacío por defecto |
   | `CPMS_CURRENT_SEMESTER` | no | Semestre vigente, por defecto `2026-2`; cambiarlo al empezar cada semestre |

3. Desplegar. El log del servicio `app` muestra `No active profile set` y `Started CpmsApplication`, sin `Successfully cleaned schema` ni cuentas de prueba.

Los archivos van en el volumen `cpms-storage` (carpeta `/var/lib/cpms/storage` dentro del contenedor), que se conserva entre despliegues. La base guarda solo sus datos descriptivos, así que **el volumen se respalda junto con la base** y no se borra al eliminar el proyecto en Dokploy.

Para ejecutar el JAR fuera de Dokploy, las mismas variables se definen en el entorno, y además `CPMS_STORAGE_ROOT` con una carpeta persistente y con permiso de escritura: sin ella la aplicación no arranca.
