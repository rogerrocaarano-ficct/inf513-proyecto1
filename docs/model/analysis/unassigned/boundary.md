# Unassigned: Boundary Classes

## InterfazGestionUsuarios

| | |
| --- | --- |
| Actor | ACT-008 Administrador, ACT-003 Director de carrera |
| Use cases (derived) | UC-001 |
| Status | Draft |
| Iteration | I1 |

**Responsibilities:**

- Recibir las solicitudes para crear, consultar, modificar, desactivar y reactivar cuentas de usuario (UC-001)
- Devolver a quien solicita el resultado, las advertencias de trámites afectados y los motivos de rechazo (UC-001)
- Avisar por correo al usuario afectado de la creación, desactivación y reactivación de su cuenta de usuario y de los cambios de sus roles (UC-001)

**Information exchanged:**

| Direction | Information |
| --- | --- |
| In | Datos de la cuenta de usuario (nombre completo, dirección de correo, roles; para un estudiante, registro universitario, cédula de identidad y teléfono), cuenta de usuario a modificar, desactivar o reactivar |
| Out | Lista y datos de cuentas de usuario con sus roles y estado, confirmaciones, trámites afectados, motivos de rechazo, avisos de cambios al usuario afectado |
