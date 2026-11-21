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
| UC-001 | Gestión de usuarios | Todos los participantes del trámite necesitan una cuenta con sus roles para usar el sistema | Must |
| UC-002 | Habilitación académica de solicitudes | La solicitud y los documentos iniciales del estudiante se reciben en papel y no se sabe si el trámite fue habilitado | Must |
| UC-003 | Asignación de tutor y registro del perfil de proyecto | La designación del tutor y la entrega del perfil de proyecto no quedan registradas en ningún lugar consultable | Must |
| UC-004 | Evaluación de viabilidad y conformación del tribunal | La evaluación del perfil y la designación de los miembros del tribunal se comunican por papel o en persona | Must |
| UC-005 | Revisión, versionado y control de plazos del tribunal | Las observaciones del tribunal, las correcciones del estudiante y sus plazos quedan dispersos en cartas y versiones impresas | Must |
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

Puertos que deben estar libres en la máquina: **8025** (webmail), **2525**, **1110** y **1143** (correo local) para el entorno de pruebas manuales, y **3025**, **3110** y **3143** para las pruebas automáticas. La base de datos usa un puerto libre que elige Docker.

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
| PostgreSQL 17 | Puerto asignado por Docker; la aplicación lo detecta sola | Base de datos de la aplicación; los datos se conservan en el volumen `cpms_postgres-data` |
| GreenMail | SMTP `localhost:2525`, POP3 `localhost:1110`, IMAP `localhost:1143` | Servidor de correo con un buzón por dirección; el buzón del sistema es `sistema@cpms.local` |
| Roundcube | <http://localhost:8025> | Webmail para enviar comandos y leer las respuestas |

La aplicación está lista cuando la consola muestra `Started CpmsApplication`.

### Probar un comando

1. Abrir <http://localhost:8025>.
2. Iniciar sesión con cualquier dirección del dominio `cpms.local` y cualquier contraseña, por ejemplo `ana@cpms.local`. Cada dirección tiene su propio buzón, así que se pueden simular varios usuarios (director, secretario(a), estudiante…) entrando con direcciones distintas.
3. Redactar un correo a `sistema@cpms.local` con el comando en el asunto, por ejemplo `usuarios.listar[]`. El cuerpo puede ir vacío.
4. La aplicación revisa el buzón cada 10 segundos. La respuesta llega a la bandeja de entrada del mismo usuario en Roundcube.

La sintaxis de los comandos es `comando[arg1; arg2; …]` (NFR-003). Mientras no haya casos de uso implementados, todo comando recibe la respuesta "El comando … no existe".

### Detener y reiniciar

- `Ctrl+C` detiene la aplicación. Los contenedores siguen en ejecución, así que el siguiente `./gradlew bootRun` arranca más rápido y conserva los correos.
- `docker compose down` detiene los contenedores y conserva la base de datos.
- `docker compose down -v` detiene los contenedores y además **borra la base de datos**.

GreenMail guarda los correos en memoria: se pierden cada vez que se reinicia su contenedor. Además, al recrearlo vuelve a numerar los mensajes desde 1, y la base todavía guarda correos con esos mismos números: los comandos nuevos se borrarían sin ejecutarse (el log de la aplicación lo avisa con un `WARN` "Deleting message … without executing it"). Por eso, **después de recrear GreenMail, borrar también la base con `docker compose down -v`**.

### Problemas frecuentes

| Síntoma | Causa y solución |
| --- | --- |
| `bootRun` falla con un error de Docker Compose | Docker no está en ejecución: iniciar Docker Desktop y volver a ejecutar |
| Un contenedor no arranca porque un puerto está ocupado | Otro programa usa ese puerto: liberarlo, o cambiarlo en `compose.yaml` y en `application-local.properties` |
| Un comando no recibe respuesta y el log muestra `Deleting message … without executing it` | Se recreó GreenMail sin borrar la base: ejecutar `docker compose down -v` y volver a levantar |
| El build pide Java 25 | Instalar un JDK 25 y comprobar con `java -version` |

### Ejecutar contra el servidor de producción

El perfil `local` solo se usa con `bootRun`. Fuera de él, la aplicación toma la configuración del correo de variables de entorno (NFR-007):

| Variable | Valor |
| --- | --- |
| `CPMS_MAIL_POP3_HOST`, `CPMS_MAIL_SMTP_HOST` | `mail.tecnoweb.org.bo` |
| `CPMS_MAIL_USERNAME`, `CPMS_MAIL_PASSWORD` | Usuario y contraseña del buzón del sistema |
| `CPMS_MAIL_ADDRESS` | Dirección del sistema, remitente de las respuestas |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://db.tecnoweb.org.bo:5432/db_grupo03sc` (NFR-008) |
| `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` | Usuario y contraseña de la base que entrega el docente |
