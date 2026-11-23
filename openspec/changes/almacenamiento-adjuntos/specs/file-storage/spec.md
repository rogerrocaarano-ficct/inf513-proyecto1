# Spec Delta

## ADDED Requirements

### Requirement: Agregar un archivo guardado como versión nueva

El sistema SHALL enlazar un archivo ya guardado como versión siguiente de la versión vigente de otro archivo, sin copiar su contenido. El archivo agregado pasa a ser la versión vigente de esa cadena y la anterior se conserva sin cambios. Solo se puede agregar un archivo que no forma parte del historial de otro.

#### Scenario: Archivo suelto agregado a la versión vigente
- **GIVEN** un archivo vigente `v1` y un archivo guardado `x` que nunca se versionó
- **WHEN** se agrega `x` como versión nueva de `v1`
- **THEN** `v1` queda enlazado a `x` y deja de ser la versión vigente
- **AND** el historial pedido desde `v1` o desde `x` devuelve `v1` y `x`, en ese orden
- **AND** no se escribe ningún archivo nuevo en disco

#### Scenario: Versión anterior no vigente
- **GIVEN** un archivo con las versiones `v1` y `v2`, donde `v2` es la vigente
- **WHEN** se agrega un archivo suelto como versión nueva de `v1`
- **THEN** el sistema rechaza la operación con el mensaje "Solo se puede crear una versión nueva a partir de la versión vigente del archivo."

#### Scenario: Archivo que ya pertenece a un historial
- **GIVEN** un archivo `w` que ya es versión de otro archivo, o que ya tiene una versión siguiente
- **WHEN** se agrega `w` como versión nueva de un archivo vigente
- **THEN** el sistema rechaza la operación con el mensaje "El archivo ya forma parte del historial de otro archivo."

#### Scenario: Mismo archivo como versión de sí mismo
- **WHEN** se agrega un archivo como versión nueva de sí mismo
- **THEN** el sistema rechaza la operación con el mensaje "El archivo ya forma parte del historial de otro archivo."

#### Scenario: Identificador inexistente
- **WHEN** la versión anterior o el archivo a agregar no corresponden a ningún archivo guardado
- **THEN** el sistema rechaza la operación con el mensaje "El archivo solicitado no existe."

#### Scenario: Dos archivos agregados al mismo tiempo
- **WHEN** dos operaciones agregan al mismo tiempo archivos distintos como versión nueva de la misma versión vigente
- **THEN** solo una de ellas enlaza su archivo
- **AND** la otra es rechazada con el mensaje "Solo se puede crear una versión nueva a partir de la versión vigente del archivo."

### Requirement: Consultar los datos de un archivo

El sistema SHALL devolver, a partir del identificador de un archivo guardado, su nombre original, tipo de contenido, tamaño, fecha de guardado y si es la versión vigente, sin leer su contenido. Si no existe un archivo con ese identificador, MUST rechazar la operación con el mensaje "El archivo solicitado no existe.".

#### Scenario: Datos de un archivo guardado
- **WHEN** se consultan los datos de un archivo `perfil.pdf` de tipo `application/pdf` con 1200 bytes
- **THEN** el sistema devuelve ese nombre, ese tipo, 1200 bytes, su fecha de guardado y que es la versión vigente

#### Scenario: Identificador inexistente
- **WHEN** se consultan los datos de un identificador que no corresponde a ningún archivo
- **THEN** el sistema rechaza la operación con el mensaje "El archivo solicitado no existe."
