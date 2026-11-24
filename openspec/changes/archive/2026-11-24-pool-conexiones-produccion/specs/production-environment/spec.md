# Spec Delta

## ADDED Requirements

### Requirement: Conexiones limitadas a la base de tecnoweb

El entorno de producción SHALL mantener abiertas como máximo 3 conexiones contra la base de datos de tecnoweb (NFR-008), y solo 1 cuando está inactivo, porque la base la comparten los grupos de la materia. Ambos límites SHALL poder cambiarse desde las variables de entorno del servidor de despliegue, sin modificar el repositorio.

#### Scenario: Límites por defecto

- **WHEN** se despliega el entorno de producción sin definir los límites del pool
- **THEN** la aplicación abre como máximo 3 conexiones contra `db_grupo03sc` y, tras unos minutos sin trabajo, conserva solo 1 abierta

#### Scenario: Límite definido en el servidor de despliegue

- **WHEN** se define en el servidor de despliegue un tamaño máximo de pool de 5 y se vuelve a desplegar
- **THEN** la aplicación abre como máximo 5 conexiones contra la base
