# Spec Delta

## Purpose

Mantiene las cuentas de usuario del sistema, que identifican a cada persona por su dirección de correo y definen sus roles y su estado (UC-001).

## ADDED Requirements

### Requirement: Administrador inicial
El sistema SHALL crear, al arrancar, una cuenta de usuario activa con el rol de administrador, con el nombre y la dirección de correo de la configuración, si no existe ninguna cuenta de administrador activa (FR-038).

#### Scenario: Primer arranque
- **WHEN** el sistema arranca con una base de datos sin cuentas de usuario
- **THEN** existe una cuenta de usuario activa con el rol de administrador y los datos de la configuración, y su creación queda en el registro de cambios

#### Scenario: Arranque con administrador existente
- **WHEN** el sistema arranca y ya existe una cuenta de administrador activa
- **THEN** el sistema no crea otra cuenta

#### Scenario: Configuración incompleta
- **WHEN** el sistema arranca sin ninguna cuenta de administrador activa y sin el nombre o la dirección de correo del administrador inicial en la configuración
- **THEN** el sistema no arranca e indica qué dato de configuración falta

### Requirement: Dirección de correo única
El sistema SHALL guardar la dirección de correo de cada cuenta de usuario en minúsculas y SHALL impedir que dos cuentas de usuario tengan la misma dirección.

#### Scenario: Dirección con mayúsculas
- **WHEN** se guarda una cuenta de usuario con la dirección `Ana@UAGRM.edu.bo`
- **THEN** la cuenta queda con `ana@uagrm.edu.bo`, y un remitente `ANA@uagrm.edu.bo` se identifica con ella
