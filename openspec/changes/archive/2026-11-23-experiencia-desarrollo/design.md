# Design

## Context

Ver `proposal.md` (Why). Estado actual relevante:

- `compose.yaml` publica PostgreSQL con `"5432"`, sin puerto de host, así que Docker elige uno libre en cada arranque. `spring-boot-docker-compose` lee ese puerto y configura el `DataSource` solo, de modo que la aplicación no depende del puerto; quien sí depende es el cliente de base de datos del desarrollador.
- `users.infrastructure.InitialAdminAccount` es un `ApplicationRunner` que crea el administrador de `cpms.users.admin-email` con `UserRepository.insert`, sin solicitante ni aviso (requisito "Administrador inicial" de `user-accounts`). `application-local.properties` lo configura con `admin@cpms.local`.
- `User.signUp(requester)` normaliza y valida los datos y levanta `UserSignedUpEvent`. El aviso por correo solo sale si alguien pasa ese evento al notificador.
- Los dos tests usan `Arrays.stream(...getDeclaredFields()).map(Field::getType)`. `javac` infiere `Stream<Class<?>>`, mientras que ECJ infiere `Stream<Class<capture of ?>>`, con lo que `doesNotContain(byte[].class)` no es aplicable.

## Goals / Non-Goals

**Goals:**
- Un entorno local reproducible: el mismo puerto de base de datos y las mismas cuentas de prueba en cada máquina.
- Que el proyecto compile sin errores también en el IDE.

**Non-Goals:**
- Datos de prueba de otros subsistemas (trámites, proyectos, archivos).
- Cambiar el comportamiento de producción o de los tests automáticos.
- Buzones o contraseñas para las cuentas `@test.local`: GreenMail acepta cualquier dirección sin autenticación, así que ya se puede entrar con ellas en Roundcube.

## Decisions

### 1. Puerto 15432 en el host

`"15432:5432"`. El 5432 se descarta porque un PostgreSQL instalado en la máquina lo ocupa a menudo, y el 5433 porque suele tomarlo una segunda instalación. Se sigue el mismo criterio que GreenMail, que ya usa puertos de host distintos de los estándar. `spring-boot-docker-compose` sigue leyendo el puerto del contenedor, así que `application-local.properties` no cambia por esto.

### 2. Tipo explícito en el `map` de los tests

`.<Class<?>>map(Field::getType)`. Fija el tipo para los dos compiladores sin cambiar la aserción. Se descartó usar una lambda `f -> f.getType()`, porque ECJ también captura el comodín en ese caso, y también convertir a `List<Class<?>>` con una variable intermedia, que da más código para lo mismo.

### 3. `LocalTestAccounts`: un `ApplicationRunner` con `@Profile("local")` en `users.infrastructure`

Sigue el patrón de `InitialAdminAccount`: un `@Component` package-private que recibe `UserRepository` por constructor. `@Profile("local")` asegura que solo exista con `./gradlew bootRun`. Ni producción ni los tests (`TestCpmsApplication`, Testcontainers) activan ese perfil.

Se descartaron dos alternativas:
- **Migración Flyway con datos** (por ejemplo, en una ubicación solo para `local`): rompe la regla de que el único SQL es el esquema y se salta las reglas del dominio.
- **Llamar a `UserManagementUseCase.signUpUser`**: exige un solicitante que exista en la base y envía avisos por correo, que la spec excluye.

### 4. Cada cuenta pasa por `User.signUp` y sus eventos se descartan

Cada cuenta se construye con `User.builder()` y se registra con `signUp(requester)`, para que cumpla las mismas reglas de datos (normalización, formato, datos exigidos al estudiante) que una cuenta creada por correo (Reference Architecture, regla 6). El solicitante es un `User` en memoria con rol `SYSTEM_ADMIN`, que no se guarda. Así el seed no depende del orden respecto a `InitialAdminAccount`. Los eventos se vacían con `pullEvents()` y no se pasan a ningún notificador, así que no sale ningún correo. Luego se guarda con `users.insert`.

Idempotencia: antes de crear cada cuenta se consulta `users.getByEmail`. Si la dirección ya existe, se salta sin modificarla, de modo que se conserva una cuenta desactivada. Al final se registra en el log cuántas cuentas se crearon.

### 5. Las cuentas, como constantes en la clase

Las once cuentas se declaran en la propia clase, como una lista de builders o un record privado de datos. Son datos de prueba fijos y no configuración de despliegue, por eso no van a `application-local.properties`. Los datos son ficticios:

| Correo | Rol | Nombre | Datos de estudiante (CI, código, teléfono) |
| --- | --- | --- | --- |
| `administrativo@test.local` | administrativo | Carla Méndez Suárez | — |
| `taller@test.local` | docente_taller | Jorge Salvatierra Ríos | — |
| `director@test.local` | director | Mónica Vargas Peña | — |
| `docente1@test.local` … `docente5@test.local` | docente | cinco nombres ficticios | — |
| `estudiante1@test.local` … `estudiante3@test.local` | estudiante | tres nombres ficticios | CI `9000001`…`9000003`, código `900000001`…`900000003`, teléfono `70000001`…`70000003` |

Los nombres concretos de docentes y estudiantes se fijan en la implementación: solo letras y espacios.

### 6. El administrador sale de `InitialAdminAccount` con `admin@test.local`

En `application-local.properties`, `cpms.users.admin-email=admin@test.local`. Se reutiliza el mecanismo del requisito "Administrador inicial" en lugar de crear el administrador dos veces, y todas las cuentas de prueba quedan en el mismo dominio.

## Risks / Trade-offs

- [Una base local ya tiene `admin@cpms.local` y otras cuentas creadas a mano] → Se conservan. Ahora conviven dos administradores, lo que no molesta. Para empezar de cero basta con `docker compose down -v`, como ya explica el README.
- [Un código de estudiante del seed coincide con uno creado a mano en la base local] → Los códigos `9000000xx` difícilmente se usan a mano. Si `insert` falla por la restricción de unicidad, el arranque falla con un error claro, y se resuelve recreando la base local.
- [El puerto 15432 también está ocupado en la máquina de alguien] → El README ya explica cómo cambiar un puerto en `compose.yaml`.
- [Quien tenga el stack levantado sigue con el puerto anterior] → El README indica que hay que recrear el contenedor (`docker compose up -d postgres`).

## Diferencias con el modelo

El trabajo es compartido y no tiene caso de uso ni diseño en EA; se sigue la arquitectura de referencia. Lo único que se crea es `users.infrastructure.LocalTestAccounts`, una clase de infraestructura, que no se modela en EA. No se crean ni cambian clases de dominio o de aplicación, operaciones ni relaciones.
