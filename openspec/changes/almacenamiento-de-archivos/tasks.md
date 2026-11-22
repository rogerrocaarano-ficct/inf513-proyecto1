# Tasks

## 1. Dominio

- [ ] 1.1 Crear `storage.domain.StoredFileNotFoundException` y `StoredFileNotCurrentException` (subclases de `DomainException`, con los mensajes de la spec) y verificar con `StoredFileExceptionsTest` el código de error y el mensaje
- [ ] 1.2 Crear `storage.domain.StoredFile` (implementa `AggregateRoot`): `builder`, `rehydrate`, `with*`, `store()` con validación por campo y `path = aaaa/mm/<id>`, `supersedeBy`, `isCurrent`, getters, con Javadoc que cite el issue #9; verificar con `StoredFileTest` (validaciones, clave, `firstVersionId` de la primera versión y de la siguiente, rechazo de `supersedeBy` sobre una versión no vigente, `pullEvents` vacío)
- [ ] 1.3 Crear el puerto `storage.domain.StoredFileRepository` (`insert`, `update`, `getById`, `getByIdForUpdate`, `getByFirstVersionId`) y verificar que compila con `./gradlew compileJava`

## 2. Aplicación

- [ ] 2.1 Crear el puerto `storage.application.FileStorage` (`write`, `read`) con su contrato en el Javadoc (no sobrescribe; borra ante rollback) y verificar que compila
- [ ] 2.2 Crear `storage.application.StoredFileService` (`store`, `storeNewVersion`, `readContent`, `getHistory`) según D1–D3 y verificar con `StoredFileServiceTest`, con dobles en memoria de los dos puertos: orden de escritura e inserción, bloqueo antes de versionar, `update` de la versión anterior, `StoredFileNotFoundException` en las tres operaciones que reciben un id, historial desde una versión intermedia, y que un archivo inválido no llega a `FileStorage`

## 3. Persistencia

- [ ] 3.1 Crear la migración `src/main/resources/db/migration/V5__stored_file.sql` según D7 y verificar que Flyway la aplica al arrancar las pruebas con Testcontainers
- [ ] 3.2 Crear `StoredFileEntity`, `SpringDataStoredFileRepository` (con `@Lock(PESSIMISTIC_WRITE)` en `findForUpdateById` y `findByFirstVersionIdOrderByStoredAtAsc`) y `JpaStoredFileRepository`, package-private en `storage.infrastructure.persistence`, sin SQL; verificar con `JpaStoredFileRepositoryTest` (Testcontainers): ida y vuelta de todos los campos, cadena de tres versiones, historial ordenado y unicidad de `next_version_id`

## 4. Disco y configuración

- [ ] 4.1 Crear `StorageProperties` (`cpms.storage.root`) y `LocalDiskFileStorage` según D4–D5; verificar con `LocalDiskFileStorageTest` (carpeta temporal y transacción de prueba): escritura y lectura, rechazo de una clave existente, rechazo de una clave fuera de la raíz, error sin transacción activa, borrado tras rollback y permanencia tras commit
- [ ] 4.2 Crear `StorageConfiguration` (package-private) con el `@Bean` de `StoredFileService`; agregar `cpms.storage.root=${CPMS_STORAGE_ROOT:}` a `application.properties` con arranque fallido y mensaje claro si queda vacía, `./data/storage` a `application-local.properties`, `data/` a `.gitignore` y una carpeta temporal en `TestcontainersConfiguration`; verificar con `StorageWiringTest` que el contexto expone `StoredFileService` y que `./gradlew bootTestRun` arranca
- [ ] 4.3 Agregar en `README.md`, sección "Entorno de desarrollo", la carpeta de archivos del perfil `local`, la variable `CPMS_STORAGE_ROOT` de producción y que esa carpeta se respalda junto con la base; verificar que los pasos descritos coinciden con la configuración

## 5. Integración y documentación compartida

- [ ] 5.1 Crear `StoredFileServiceIntegrationTest` (contexto completo, Testcontainers, carpeta temporal) que cubra cada escenario de `specs/file-storage/spec.md`: guardar, leer, versionar, historial, rechazo de una versión no vigente, dos versiones simultáneas y rollback al guardar y al versionar; verificar que pasa
- [ ] 5.2 Agregar a `AGENTS.md` la sección "File storage" en la arquitectura de referencia (subsistema `storage`, uso exclusivo de `StoredFileService`, el dueño guarda el id de la versión vigente, sin bytes en entidades JPA ni `BYTEA` nuevos, pruebas esperadas) y `storage` a la lista de subsistemas; verificar que la sección cita los nombres reales del código
- [ ] 5.3 Agregar a `GLOSSARY.md` el término "Archivo almacenado" → `StoredFile`, distinto de "Versión del documento", y verificar el orden alfabético de la tabla
- [ ] 5.4 Comparar la implementación con los elementos de EA citados en el diseño (`Design/Storage/domain` y `Design/Storage/application`: clases, operaciones, atributos y relaciones) y registrar cualquier diferencia en "Diferencias con el modelo" de `design.md`
- [ ] 5.5 Ejecutar `./gradlew build` y verificar que todas las pruebas pasan

## Workflow follow-up

- Archivar el change con `/opsx:archive` cuando las tareas estén hechas y las pruebas pasen.
- Segunda parte del issue #9 en otro change: usar `StoredFileService` desde `mail` y UC-002, quitar los `BYTEA` y eliminar `Design/Mail/application/FileStorage`.
