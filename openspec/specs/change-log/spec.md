# change-log Specification

## Purpose

Guarda el historial de los cambios hechos en el sistema, con quién los hizo, cuándo y sobre qué dato, para que se puedan consultar después (NFR-SE-002, UC-009).

## Requirements

### Requirement: Registro de cada cambio
El sistema SHALL registrar cada dato que un caso de uso cambia, con la cuenta de usuario que hizo el cambio, la fecha y hora, el tipo y el identificador del registro afectado, el dato, su valor anterior y su valor nuevo.

#### Scenario: Cambio de un dato
- **WHEN** una cuenta de usuario cambia el teléfono de un registro de `700000` a `711111`
- **THEN** el registro de cambios guarda quién lo hizo, cuándo, el registro afectado, el dato `teléfono`, `700000` y `711111`

#### Scenario: Creación de un registro
- **WHEN** una cuenta de usuario crea un registro
- **THEN** el registro de cambios guarda cada dato inicial con el valor anterior vacío

### Requirement: Registro en la misma transacción
El sistema SHALL guardar el registro de un cambio en la misma transacción que el cambio: si el cambio se deshace, su registro tampoco queda.

#### Scenario: Cambio rechazado
- **WHEN** un caso de uso registra un cambio y luego la operación se rechaza y se deshace
- **THEN** el registro de cambios no tiene ese cambio

### Requirement: Registro inalterable
El sistema SHALL impedir que se modifique o se borre una entrada del registro de cambios.

#### Scenario: Intento de modificar el historial
- **WHEN** cualquier operación del sistema intenta modificar o borrar una entrada del registro de cambios
- **THEN** la entrada queda igual

### Requirement: Consulta del historial de un registro
El sistema SHALL devolver las entradas del registro de cambios de un registro, por su tipo y su identificador, en el orden en que se hicieron, con la cuenta de usuario, la fecha y hora, el dato, el valor anterior y el valor nuevo de cada una. Las entradas de un mismo cambio comparten la fecha y hora.

#### Scenario: Historial en orden
- **WHEN** un registro se crea y luego cambia dos veces, y se consulta su historial
- **THEN** el historial devuelve primero las entradas de la creación y después las de cada cambio, en ese orden

#### Scenario: Entradas de un mismo cambio
- **WHEN** un caso de uso registra en un solo cambio tres datos de un registro
- **THEN** las tres entradas del historial tienen la misma fecha y hora

#### Scenario: Registro sin cambios
- **WHEN** se consulta el historial de un registro que no tiene entradas
- **THEN** el historial está vacío
