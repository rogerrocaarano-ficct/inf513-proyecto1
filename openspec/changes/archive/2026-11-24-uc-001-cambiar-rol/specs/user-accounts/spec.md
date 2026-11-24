## ADDED Requirements

### Requirement: Comando para cambiar el rol de una cuenta

El sistema SHALL cambiar el rol de una cuenta con el comando `usuarios.cambiarRol[correo; rol]`, con exactamente 2 argumentos, el correo de la cuenta afectada y el rol nuevo, y ningún adjunto. El correo y el rol se comparan sin distinguir mayúsculas; el rol es una de las etiquetas aceptadas por `usuarios.crearCuenta`. El solicitante es el remitente del correo. La cuenta conserva sus datos y su estado, y sigue teniendo un solo rol (FR-036).

#### Scenario: Cambiar el rol de una cuenta
- **GIVEN** existe la cuenta de docente `luis@uagrm.bo`
- **WHEN** el administrador envía `usuarios.cambiarRol[luis@uagrm.bo; director]`
- **THEN** la cuenta `luis@uagrm.bo` tiene el rol director y conserva sus datos

#### Scenario: Correo y rol sin distinguir mayúsculas
- **GIVEN** existe la cuenta de docente `luis@uagrm.bo`
- **WHEN** un director envía `usuarios.cambiarRol[Luis@UAGRM.bo; Docente_Taller]`
- **THEN** la cuenta `luis@uagrm.bo` tiene el rol docente de Taller de Grado

#### Scenario: Rol desconocido
- **WHEN** un director envía `usuarios.cambiarRol[luis@uagrm.bo; decano]`
- **THEN** el sistema no cambia ninguna cuenta y responde que `decano` no es un rol válido, con la lista de roles aceptados

#### Scenario: Falta un argumento
- **WHEN** un director envía `usuarios.cambiarRol[luis@uagrm.bo]`
- **THEN** el sistema no cambia ninguna cuenta y responde que el comando requiere 2 argumentos

### Requirement: Quién puede cambiar el rol de cada cuenta

El solicitante SHALL tener permiso sobre el rol actual de la cuenta y sobre el rol nuevo (UC-001 A1). El administrador lo tiene sobre todos los roles; el director, sobre estudiante, administrativo, docente y docente de Taller de Grado (FR-050, FR-053); nadie más. Sin permiso, el sistema MUST NOT cambiar la cuenta y responde que no tiene permiso para cambiar el rol de cuentas de ese rol (E1).

#### Scenario: Director cambia un docente a docente de Taller de Grado
- **GIVEN** existe la cuenta de docente `luis@uagrm.bo`
- **WHEN** un director envía `usuarios.cambiarRol[luis@uagrm.bo; docente_taller]`
- **THEN** la cuenta `luis@uagrm.bo` tiene el rol docente de Taller de Grado

#### Scenario: Director intenta dar el rol de director
- **GIVEN** existe la cuenta de docente `luis@uagrm.bo`
- **WHEN** un director envía `usuarios.cambiarRol[luis@uagrm.bo; director]`
- **THEN** la cuenta sigue con el rol docente y el director recibe que no tiene permiso para cambiar el rol de cuentas de director

#### Scenario: Director intenta cambiar el rol de otro director
- **GIVEN** existe la cuenta de director `carla@uagrm.bo`
- **WHEN** otro director envía `usuarios.cambiarRol[carla@uagrm.bo; docente]`
- **THEN** la cuenta sigue con el rol director y el director recibe que no tiene permiso para cambiar el rol de cuentas de director

#### Scenario: Administrativo intenta cambiar un rol
- **GIVEN** existe la cuenta de estudiante `ana@uagrm.bo`
- **WHEN** un administrativo envía `usuarios.cambiarRol[ana@uagrm.bo; docente]`
- **THEN** la cuenta sigue con el rol estudiante y el administrativo recibe que no tiene permiso para cambiar el rol de cuentas de estudiante

### Requirement: Cuenta inexistente al cambiar el rol

Si ninguna cuenta tiene el correo indicado, el sistema SHALL rechazar el comando con «No existe una cuenta con el correo X.» y MUST NOT cambiar ninguna cuenta (E4).

#### Scenario: Correo sin cuenta
- **WHEN** un director envía `usuarios.cambiarRol[nadie@uagrm.bo; docente]`
- **THEN** el director recibe que no existe una cuenta con el correo `nadie@uagrm.bo`

### Requirement: Rol igual al actual

Si el rol nuevo es el que la cuenta ya tiene, el sistema SHALL rechazar el comando con «La cuenta X ya tiene el rol Y.». La cuenta afectada MUST NOT recibir ningún aviso.

#### Scenario: Mismo rol
- **GIVEN** existe la cuenta de docente `luis@uagrm.bo`
- **WHEN** un director envía `usuarios.cambiarRol[luis@uagrm.bo; docente]`
- **THEN** el director recibe que la cuenta `luis@uagrm.bo` ya tiene el rol docente
- **AND** `luis@uagrm.bo` no recibe ningún correo

### Requirement: No cambiar el rol de la propia cuenta

El sistema SHALL rechazar que el solicitante cambie el rol de su propia cuenta (E6), y MUST NOT cambiarla.

#### Scenario: Administrador intenta cambiar su propio rol
- **GIVEN** hay dos cuentas de administrador activas, `admin@cpms.local` y `admin2@cpms.local`
- **WHEN** `admin@cpms.local` envía `usuarios.cambiarRol[admin@cpms.local; docente]`
- **THEN** la cuenta sigue con el rol administrador y el administrador recibe que no puede cambiar el rol de su propia cuenta

### Requirement: Último administrador activo al cambiar el rol

El sistema SHALL rechazar que la única cuenta de administrador activa pierda el rol de administrador (E5, FR-038), y MUST NOT cambiarla.

#### Scenario: Única cuenta de administrador activa
- **GIVEN** `admin@cpms.local` y `admin2@cpms.local` tienen rol administrador y solo `admin2@cpms.local` está activa
- **WHEN** se pide cambiar el rol de `admin2@cpms.local` a docente
- **THEN** la cuenta sigue con el rol administrador y el solicitante recibe que no se puede quitar el rol de administrador a la única cuenta de administrador activa

### Requirement: Datos del estudiante al cambiar el rol

Una cuenta que pasa a tener el rol estudiante SHALL tener CI, código y teléfono (FR-026). Si le falta alguno, el sistema MUST NOT cambiar la cuenta y responde con cada dato que falta (E2).

#### Scenario: Docente sin código pasa a estudiante
- **GIVEN** existe la cuenta de docente `luis@uagrm.bo`, sin CI, código ni teléfono
- **WHEN** un director envía `usuarios.cambiarRol[luis@uagrm.bo; estudiante]`
- **THEN** la cuenta sigue con el rol docente y el director recibe que CI, código y teléfono son obligatorios para estudiantes

#### Scenario: Cuenta con los datos del estudiante
- **GIVEN** existe la cuenta de administrativo `beto@uagrm.bo` con CI `1234567`, código `218001234` y teléfono `71234567`
- **WHEN** un director envía `usuarios.cambiarRol[beto@uagrm.bo; estudiante]`
- **THEN** la cuenta `beto@uagrm.bo` tiene el rol estudiante

### Requirement: Avisos del cambio de rol

Cuando el rol de una cuenta cambia, el sistema SHALL enviar, solo después de confirmar el cambio, una confirmación al solicitante y un aviso a la cuenta afectada, los dos con el rol anterior y el rol nuevo; el aviso a la cuenta lleva el nombre de quien hizo el cambio (FR-035). Si el cambio se rechaza, la cuenta afectada no recibe nada.

#### Scenario: Rol cambiado
- **WHEN** el administrador cambia el rol de `luis@uagrm.bo` de docente a director
- **THEN** el administrador recibe la confirmación «Rol cambiado» con el nombre y el correo de la cuenta, el rol anterior y el nuevo
- **AND** `luis@uagrm.bo` recibe un aviso de que el administrador cambió su rol de docente a director
