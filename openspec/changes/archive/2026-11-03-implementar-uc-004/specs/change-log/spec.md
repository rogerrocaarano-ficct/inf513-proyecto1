# Spec Delta

## ADDED Requirements

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
