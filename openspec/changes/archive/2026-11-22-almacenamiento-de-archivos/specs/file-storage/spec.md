# Spec Delta

## Purpose

Guardar los archivos que recibe o genera el sistema en una carpeta del entorno, con sus metadatos en la base de datos, y conservar sus versiones para reconstruir el historial de cada archivo.

## ADDED Requirements

### Requirement: Guardar un archivo en disco
El sistema SHALL guardar el contenido de cada archivo en la carpeta de almacenamiento configurada y registrar en la base de datos solo sus metadatos: identificador, nombre original, tipo de contenido, tamaño, clave en disco y fecha de guardado. La base de datos MUST NOT guardar el contenido del archivo.

#### Scenario: Archivo nuevo guardado
- **WHEN** se guarda un archivo `perfil.pdf` de tipo `application/pdf` con 1200 bytes
- **THEN** existe en la carpeta de almacenamiento un archivo con esos 1200 bytes
- **AND** la base de datos registra su nombre original, tipo, tamaño, clave y fecha, sin su contenido

#### Scenario: Clave del archivo en disco
- **WHEN** se guarda un archivo en noviembre de 2026
- **THEN** su clave tiene la forma `2026/11/<identificador>`, sin extensión y sin usar el nombre original

### Requirement: Validar los datos del archivo
El sistema SHALL rechazar un archivo sin nombre original, sin tipo de contenido o sin contenido, con un mensaje en español por campo, y MUST NOT escribir nada en disco ni en la base de datos. Los límites de tamaño y formato de cada documento los valida el caso de uso que lo recibe.

#### Scenario: Archivo vacío
- **WHEN** se intenta guardar un archivo de 0 bytes
- **THEN** el sistema rechaza la operación con un mensaje de validación en español
- **AND** no se escribe ningún archivo en la carpeta de almacenamiento

#### Scenario: Archivo sin nombre
- **WHEN** se intenta guardar un archivo cuyo nombre original está vacío
- **THEN** el sistema rechaza la operación con un mensaje de validación en español

### Requirement: Leer el contenido de un archivo
El sistema SHALL devolver el contenido exacto de un archivo guardado a partir de su identificador. Si no existe un archivo con ese identificador, MUST rechazar la operación con el mensaje "El archivo solicitado no existe.".

#### Scenario: Lectura de un archivo guardado
- **WHEN** se pide el contenido de un archivo guardado
- **THEN** el sistema devuelve los mismos bytes que se guardaron

#### Scenario: Identificador inexistente
- **WHEN** se pide el contenido de un identificador que no corresponde a ningún archivo
- **THEN** el sistema rechaza la operación con el mensaje "El archivo solicitado no existe."

### Requirement: Crear una versión nueva de un archivo
El sistema SHALL guardar una versión nueva de un archivo como un archivo distinto, enlazado a la versión anterior, y MUST conservar la versión anterior y su contenido sin cambios. La versión nueva pasa a ser la vigente.

#### Scenario: Versión nueva de la versión vigente
- **WHEN** se guarda una versión nueva del archivo vigente `v1`
- **THEN** se crea el archivo `v2` con su propio contenido en disco
- **AND** `v1` queda enlazado a `v2` y deja de ser la versión vigente
- **AND** el contenido de `v1` sigue disponible sin cambios

#### Scenario: Versión anterior inexistente
- **WHEN** se pide una versión nueva de un identificador que no corresponde a ningún archivo
- **THEN** el sistema rechaza la operación con el mensaje "El archivo solicitado no existe."

### Requirement: Solo se versiona la versión vigente
El sistema MUST rechazar una versión nueva a partir de una versión que ya tiene siguiente, con el mensaje "Solo se puede crear una versión nueva a partir de la versión vigente del archivo.", de modo que la cadena de versiones de un archivo nunca se bifurque, aunque dos versiones nuevas se pidan al mismo tiempo.

#### Scenario: Versión nueva de una versión anterior
- **GIVEN** un archivo con las versiones `v1` y `v2`, donde `v2` es la vigente
- **WHEN** se pide una versión nueva a partir de `v1`
- **THEN** el sistema rechaza la operación con el mensaje "Solo se puede crear una versión nueva a partir de la versión vigente del archivo."
- **AND** no queda ningún archivo nuevo en disco ni en la base de datos

#### Scenario: Dos versiones nuevas simultáneas
- **WHEN** dos operaciones piden al mismo tiempo una versión nueva de la misma versión vigente
- **THEN** solo una de ellas crea la versión siguiente
- **AND** la otra es rechazada con el mensaje "Solo se puede crear una versión nueva a partir de la versión vigente del archivo."

### Requirement: Reconstruir el historial de versiones
El sistema SHALL devolver, a partir del identificador de cualquier versión de un archivo, todas las versiones de su cadena ordenadas de la más antigua a la más reciente, e indicar cuál es la vigente.

#### Scenario: Historial desde una versión intermedia
- **GIVEN** un archivo con las versiones `v1`, `v2` y `v3`
- **WHEN** se pide el historial a partir de `v2`
- **THEN** el sistema devuelve `v1`, `v2` y `v3` en ese orden
- **AND** solo `v3` figura como vigente

#### Scenario: Archivo sin versiones posteriores
- **WHEN** se pide el historial de un archivo que nunca se versionó
- **THEN** el sistema devuelve solo ese archivo, como vigente

### Requirement: Sin archivos huérfanos ante un rollback
El sistema MUST borrar de la carpeta de almacenamiento los archivos escritos durante una transacción que se revierte, y MUST NOT sobrescribir ni borrar archivos de transacciones ya confirmadas.

#### Scenario: Rollback después de guardar
- **WHEN** se guarda un archivo dentro de una transacción y luego la transacción se revierte
- **THEN** el archivo no queda en la carpeta de almacenamiento
- **AND** la base de datos no registra sus metadatos

#### Scenario: Rollback al versionar
- **GIVEN** un archivo vigente `v1` confirmado
- **WHEN** se guarda una versión nueva y luego la transacción se revierte
- **THEN** `v1` sigue siendo la versión vigente, con su contenido en disco sin cambios
- **AND** el archivo de la versión nueva no queda en la carpeta de almacenamiento

### Requirement: Carpeta de almacenamiento configurable
El sistema SHALL guardar los archivos en la carpeta indicada por la propiedad `cpms.storage.root`, que en producción toma el valor de la variable de entorno `CPMS_STORAGE_ROOT`.

#### Scenario: Carpeta configurada
- **GIVEN** `cpms.storage.root` apunta a una carpeta del entorno
- **WHEN** se guarda un archivo
- **THEN** el archivo queda dentro de esa carpeta, en la ruta que indica su clave
