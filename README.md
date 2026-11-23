# Sistema de Gestión de Trámites de Proyecto de Grado

Proyecto de la materia Tecnología Web (INF513): Sistema de Gestión de Trámites de Proyecto de Grado para la carrera de Ingeniería en Redes y Telecomunicaciones, Facultad de ingeniería en Ciencias de la Computación y Telecomunicaciones (FICCT), Universidad Autónoma Gabriel René Moreno (UAGRM).

## Problema

El trámite de titulación por Proyecto de Grado de la carrera de Ingeniería en Redes y Telecomunicaciones pasa por muchas etapas y personas. El estudiante solicita el trámite y presenta su perfil de proyecto. El docente de Taller de Grado evalúa su viabilidad, el director de carrera designa al tutor y al tribunal evaluador, y el tribunal revisa el documento hasta aprobarlo. Luego se programan la predefensa y la defensa final, y al terminar se emite el acta de graduación y se archivan los ejemplares físicos. Hoy todo se lleva en papel: carpetas físicas, cartas dirigidas a la Dirección de Carrera y notificaciones personales a cargo del secretario(a) de dirección de carrera.

Por eso nadie sabe en qué etapa está un trámite sin acudir a la secretaría a preguntar. Esto afecta a los estudiantes, a los docentes del proceso y a la propia dirección. Tampoco se pueden obtener estadísticas de titulación sin revisar las carpetas una por una.

Con el sistema, cada etapa del trámite queda registrada en un expediente digital que todos los participantes pueden consultar según su rol, y la dirección obtiene reportes y estadísticas a partir de esos registros. Criterio de éxito: cualquier participante consulta el estado de un trámite sin acudir a la secretaría, y el 100% de los trámites del semestre quedan registrados en el sistema.

### Documentos para iniciar el trámite

1. carta de solicitud dirigida al director de carrera con: nombre completo, registro, número de teléfono de contacto.
2. hoja de vida (CV)
3. fotocopia de cédula de identidad
4. boleta de inscripción del semestre en curso con Modalidad de Graduación inscrita

### Flujo

| Paso | Actor | Acción |
| --- | --- | --- |
| 1 | Estudiante | Inicia trámite de titulación por modalidad de graduación: Proyecto de Grado |
| 2 | Secretario(a) de dirección de carrera | Revisar que la documentación presentada por el estudiante incluya los documentos para iniciar el trámite. |
| 3 | Estudiante | Presentar documento: Perfil de proyecto |
| 4 | Docente de Taller de Grado | Evaluar viabilidad del perfil de proyecto presentado por el estudiante (1) |
| 5 | Director de carrera | Asignar tribunal evaluador y tutor |
| 6 | Secretario(a) de dirección de carrera | Notificar al estudiante la designación de tribunal y tutor |
| 7 | Estudiante | Presentar documento del proyecto para revisión |
| 8 | Tribunal evaluador y tutor | Revisar y aprobar el documento (2) |
| 9 | Director de carrera | Programar predefensa y revisión de prototipo |
| 10 | Secretario(a) de dirección de carrera | Notificar al estudiante la fecha de la predefensa y revisión de prototipo |
| 11 | Estudiante | Realizar predefensa del proyecto |
| 12 | Tribunal evaluador | Realizar observaciones finales al proyecto y aprobar para defensa final |
| 13 | Estudiante | Presentar documento para defensa final |
| 14 | Director de carrera | Asignar fecha para defensa final |
| 15 | Estudiante | Realizar defensa final del proyecto | 
| 16 | Tribunal evaluador | Asignar calificación al proyecto |
| 17 | Secretario(a) de dirección de carrera | Archivar ejemplares en físico del proyecto. |

1. El docente de taller de grado puede rechazar el proyecto presentado por el estudiante o solicitar cambios al perfil de proyecto, el estudiante debe subsanar estas observaciones y presentar un nuevo documento.
2. El estudiante recibe correcciones a su proyecto por parte del tribunal y su tutor asignados hasta que el documento esté apto para la predefensa.

## Casos de Uso Aprobados

| ID | Caso de uso | ¿Qué soluciona? | Prioridad |
| --- | --- | --- | --- |
| [UC-001](use-cases/UC-001.md) | Gestión de usuarios | Todos los participantes del trámite necesitan una cuenta con sus roles para usar el sistema | Must |
| [UC-002](use-cases/UC-002.md) | Habilitación académica de solicitudes | La solicitud y los requisitos de apertura del estudiante se reciben en papel y no se sabe si el trámite fue habilitado | Must |
| [UC-003](use-cases/UC-003.md) | Asignación de tutor y designación de tribunal | La designación del tutor y de los miembros del tribunal se comunica por papel o en persona | Must |
| [UC-004](use-cases/UC-004.md) | Evaluación del perfil de proyecto | La entrega del perfil de proyecto y su evaluación de viabilidad no quedan registradas en ningún lugar consultable | Must |
| [UC-005](use-cases/UC-005.md) | Revisión y versionado del documento del proyecto | Las observaciones del tutor y del tribunal y las correcciones del estudiante quedan dispersas en cartas y versiones impresas | Must |
| UC-006 | Programación y registro de evaluaciones | La programación de la predefensa y la defensa final, sus resultados, el acta de graduación, la entrega de ejemplares y el cierre del expediente se registran en papel | Could |
| UC-007 | Monitoreo, trazabilidad, dashboard y calendario académico | Nadie sabe en qué etapa está un trámite ni qué evaluaciones vienen sin preguntar en la secretaría | Should |
| UC-008 | Reportes y estadísticas | La dirección no puede obtener estadísticas de titulación sin revisar las carpetas una por una | Could |

### Alcance

**In scope:**

- Prueba de concepto del sistema mediante una arquitectura Email Driven.
- 

**Out of scope:**

- Ninguno por ahora.

## Plan del proyecto

### Equipo

| Member | Role |
| --- | --- |
| Roger Roca Arano | Guía el desarrollo: arquitectura, integración y revisión de lo que llega a `develop` |
| Francisco Javier Cabrera Claros | Desarrollo por casos de uso |
| Alejandro de los Rios Arancibia | Desarrollo por casos de uso |

### Hitos

| Meta | Fecha | Status |
| --- | --- | --- |
| Arquitectura estable diseñada e implementada con al menos un caso de uso | TBD | Considerado |
| Implementación de los casos de uso con prioridad `Must` | TBD | Considerado |
| Implementación de casos de uso restantes | TBD | Considerado |
| Pruebas contra entorno de producción | TBD | Considerado |
| Presentación final del proyecto | 2026-11-24 | Planeado |


### Iteraciones

| Iteración | Inicio | Fin | Objetivo | Estado |
| --- | --- | --- | --- | --- |
| I1 | 2026-10-26 | 2026-11-07 | Alcance aprobado por el docente, arquitectura candidata, riesgos priorizados y plan de Elaboración | Completado |
| E1 | 2026-11-08 | 2026-11-15 | Análisis y diseño de la arquitectura base de los casos de uso significativos | Incompleto |
| C1 | 2026-11-16 | 2026-11-26 | Construcción del sistema y pruebas | Retrasado |


### Riesgos

| ID | Riesgo | Respuesta | Estado |
| --- | --- | --- | --- |
| RSK-001 | El servidor de correo del docente rechaza los correos de la aplicación porque su SMTP valida al remitente por dirección IP y la IP del laboratorio o del servidor de contingencia no está permitida, o el servidor no está disponible; sin correo, ningún caso de uso se puede presentar | Solicitar añadir a la lista blanca del servidor la dirección IP del entorno de producción. verificar que el SMTP las acepta. Probar la cadena completa (sondeo POP3, comando, respuesta SMTP) contra el servidor SMTP de producción. | Abierto |
| RSK-002 | El servidor de correo no acepta adjuntos de hasta 25 MB, y los usuarios no pueden entregar el perfil ni el documento del proyecto | Enviar un adjunto de 25 MB a través de producción y confirmar el límite del buzón | Abierto |
| RSK-003 | El sondeo del buzón cada 30 segundos sobrecarga el servidor de correo, y las solicitudes de los usuarios no se procesan | Ajustar el intervalo de sondeo a un valor aceptable | Abierto |
| RSK-004 | Los usuarios escriben mal el comando del asunto o el nombre de los adjuntos, y sus solicitudes fallan | Responder cada error con la sintaxis correcta del comando. Preparar una guía de comandos para la presentación | Abierto |

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

La sintaxis de los comandos, cada comando con sus argumentos y las respuestas del sistema están en la [guía de comandos](USER-GUIDE.md).

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
