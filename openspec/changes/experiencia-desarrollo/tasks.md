# Tasks

## 1. Puerto fijo de PostgreSQL

- [x] 1.1 En `compose.yaml`, publicar PostgreSQL en `"15432:5432"` con un comentario que explique por qué no se usa el 5432; verificar con `docker compose config` que el puerto publicado es 15432
- [x] 1.2 En `README.md` ("Entorno de desarrollo"), añadir el 15432 a los puertos que deben estar libres, mostrar en la tabla de servicios `localhost:15432` con la base, el usuario y la contraseña, e indicar que quien ya tenga el stack levantado recree el contenedor (`docker compose up -d postgres`); verificar leyendo la sección

## 2. Tests compilables con ECJ

- [x] 2.1 En `JpaReceivedEmailRepositoryTest.attachmentTableKeepsNoContent` y `JpaDegreeProcedureRepositoryTest.entitiesKeepNoFileContent`, usar `.<Class<?>>map(Field::getType)`; verificar que el IDE ya no marca errores y que los dos tests pasan con `./gradlew test --tests '*JpaReceivedEmailRepositoryTest' --tests '*JpaDegreeProcedureRepositoryTest'`

## 3. Cuentas de prueba del entorno local

- [x] 3.1 Crear `users.infrastructure.LocalTestAccounts` (`@Component`, `@Profile("local")`, package-private, `ApplicationRunner`) con las 11 cuentas del diseño: cada una pasa por `User.signUp` con un solicitante administrador en memoria, se descartan sus eventos, se salta si `getByEmail` ya la encuentra y se guarda con `insert`. Con Javadoc en inglés que cite UC-001. Verificar que compila
- [x] 3.2 Crear `LocalTestAccountsTest` (con un doble en memoria de `UserRepository`, como `InitialAdminAccountTest`): primer arranque, con 11 cuentas, los roles y cantidades de la spec, todas activas, dominio `@test.local` y sin eventos; arranque posterior, sin inserciones y conservando una cuenta desactivada; y una dirección ya existente que se salta. Verificar que pasa
- [x] 3.3 En `UsersWiringTest` (o en un test de wiring equivalente), comprobar que sin el perfil `local` no existe el bean `LocalTestAccounts`; verificar que pasa
- [x] 3.4 En `application-local.properties`, cambiar `cpms.users.admin-email` a `admin@test.local`; verificar con `./gradlew bootRun` que el log informa del administrador y de las 11 cuentas, y que un segundo arranque no crea ninguna
- [x] 3.5 En `README.md`, actualizar los pasos de prueba manual: el administrador `admin@test.local`, la lista de cuentas de prueba (dirección y rol) y que se entra en Roundcube con cualquiera de ellas; revisar si `USER-GUIDE.md` menciona `admin@cpms.local` y actualizarlo si es así

## 4. Verificación final

- [x] 4.1 Comparar la implementación con la arquitectura de referencia de `AGENTS.md`: sin SQL en `src/main`, la clase en `infrastructure`, el dominio reutilizado sin repetir reglas y ningún correo enviado; corregir lo que no cumpla
- [x] 4.2 Ejecutar `./gradlew build` (con Docker en marcha) y verificar que todo pasa

## Workflow follow-up

- Mostrar al usuario los archivos y el mensaje antes de hacer commit en la rama `experiencia-desarrollo`
- Archivar el change con `/opsx:archive` cuando los tests pasen
