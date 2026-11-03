# Spec Delta

## ADDED Requirements

### Requirement: Cuenta del estudiante al abrir su trámite
El sistema SHALL, al abrir un trámite de titulación, obtener la cuenta de usuario activa con el rol de estudiante cuya dirección de correo y registro universitario son los indicados, o crearla con el rol de estudiante si ninguna cuenta tiene esos datos (FR-051). La creación sigue las reglas de datos, de unicidad y de aviso de una cuenta nueva, sin el alcance de quien gestiona: la autoriza la apertura del trámite.

#### Scenario: Creación por el secretario(a)
- **WHEN** el secretario(a) de dirección de carrera abre un trámite para un estudiante sin cuenta de usuario
- **THEN** existe una cuenta de usuario activa del estudiante con el rol de estudiante, su creación queda en el registro de cambios y el estudiante recibe el aviso de creación de su cuenta

#### Scenario: Cuenta existente
- **WHEN** la dirección de correo y el registro universitario indicados pertenecen a la misma cuenta de usuario activa con el rol de estudiante
- **THEN** el sistema usa esa cuenta, sin cambiar sus datos ni registrar su creación

#### Scenario: Datos de cuentas distintas
- **WHEN** la dirección de correo pertenece a una cuenta de usuario y el registro universitario a otra, o la cuenta no tiene el rol de estudiante
- **THEN** el sistema rechaza la solicitud, indica el dato repetido y no cambia nada

### Requirement: Cuenta del estudiante desactivada
El sistema SHALL rechazar, sin cambiar nada, la apertura de un trámite de titulación cuando la cuenta de usuario del estudiante está desactivada, e indicar que la cuenta está desactivada.

#### Scenario: Estudiante desactivado
- **WHEN** el secretario(a) abre un trámite con la dirección de correo y el registro universitario de una cuenta de estudiante desactivada
- **THEN** el sistema rechaza la solicitud, indica que la cuenta de usuario está desactivada y no abre el trámite
