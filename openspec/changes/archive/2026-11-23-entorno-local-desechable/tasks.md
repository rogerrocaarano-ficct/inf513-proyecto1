# Tasks

## 1. Base de datos desechable en local

- [x] 1.1 Renombrar `StagingDatabaseReset` a `DisposableDatabaseReset` (y su test) con `@Profile({"local", "staging"})`, actualizar su Javadoc y los comentarios que lo nombran (`compose.staging.yaml`, `application-staging.properties`), y verificar con el test que la estrategia existe con `local` y `staging`, no con el perfil por defecto, y ejecuta clean antes de migrate
- [x] 1.2 Agregar `spring.flyway.clean-disabled=false` a `application-local.properties` con su comentario, y verificar que `./gradlew build` pasa

## 2. Archivos desechables

- [x] 2.1 Crear `storage.infrastructure.DisposableStorageReset` (`@Component`, `@Profile({"local", "staging"})`, vacía el contenido de `StorageProperties.root()` al construirse y lo registra en el log, Javadoc en inglés) y verificar con un test que borra archivos y subcarpetas de la raíz sin borrar la raíz, que no falla si la raíz no existe, y que no se crea sin esos perfiles

## 3. Prueba manual

- [x] 3.1 Ejecutar `./gradlew bootRun`, enviar un comando que cree datos y deje un archivo (por ejemplo desactivar una cuenta), detener y volver a arrancar, y verificar en el log `Successfully cleaned schema`, la carpeta `data/storage` vacía y la cuenta activa de nuevo; repetir con `compose.staging.yaml` y verificar que staging sigue igual

## 4. Documentación

- [x] 4.1 Actualizar `README.md`: el entorno local es desechable (sección "Levantar el stack de pruebas manuales" y "Detener y reiniciar"), el aviso sobre los reinicios de DevTools, y quitar de "Problemas frecuentes" y de la nota de GreenMail el paso de borrar la base a mano; verificar que no queda ninguna instrucción de `docker compose down -v` para recuperar comandos descartados
- [x] 4.2 Actualizar `AGENTS.md` ("Local environment") con el nombre `DisposableDatabaseReset` y que local también es desechable, y verificar que no queda ninguna mención de `StagingDatabaseReset` fuera de los changes archivados

## 5. Verificación

- [x] 5.1 Comparar la implementación con la arquitectura de referencia de `AGENTS.md` (la carpeta de archivos solo se toca desde `storage`, sin SQL en `src/main`, Javadoc en inglés) y verificar que `./gradlew build` pasa completo

## Workflow follow-up

- Integrar en `develop` por PR.
- Archivar el change cuando las tareas estén hechas y las pruebas pasen.
