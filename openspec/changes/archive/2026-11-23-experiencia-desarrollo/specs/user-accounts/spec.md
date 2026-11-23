## ADDED Requirements

### Requirement: Cuentas de prueba del entorno local

Al arrancar en el entorno local de desarrollo, el sistema SHALL crear cuentas de prueba con direcciones `@test.local`: un administrativo, un docente de Taller de Grado, un director, cinco docentes y tres estudiantes. Las cuentas cumplen las mismas reglas de datos que una cuenta creada por correo y no reciben aviso por correo. Si una dirección ya tiene cuenta, no se modifica. Fuera del entorno local no se crea ninguna.

#### Scenario: Primer arranque local
- **GIVEN** el sistema arranca en el entorno local y no hay cuentas `@test.local`
- **WHEN** termina de arrancar
- **THEN** existen 1 cuenta administrativo, 1 docente de Taller de Grado, 1 director, 5 docentes y 3 estudiantes con direcciones `@test.local`, todas activas
- **AND** no se envía ningún correo por esas cuentas

#### Scenario: Arranque local posterior
- **GIVEN** las cuentas de prueba ya existen y una de ellas fue desactivada
- **WHEN** el sistema arranca de nuevo en el entorno local
- **THEN** no se crea ninguna cuenta repetida y la cuenta desactivada sigue desactivada

#### Scenario: Fuera del entorno local
- **WHEN** el sistema arranca fuera del entorno local
- **THEN** no se crea ninguna cuenta `@test.local`
