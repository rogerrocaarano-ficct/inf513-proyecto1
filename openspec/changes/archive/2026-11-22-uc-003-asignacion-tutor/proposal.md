# Propuesta

## Motivo

UC-003 incorpora el registro del primer perfil y de la designación del tutor al trámite abierto en UC-002. El registro permite que Taller reciba el perfil y que el estudiante reciba la designación.

## Cambios

- En A1, el estudiante presenta su primer perfil con el título y el archivo. La entrega crea el proyecto y se notifica a Taller (FR-004, FR-057, FR-058).
- En el flujo básico, Dirección asigna un docente como tutor después del visto bueno de Taller. El estudiante recibe la designación (FR-003, FR-005, FR-020).
- UC-004 registra la evaluación y la designación del tribunal. Tutor y tribunal se pueden asignar en momentos distintos. UC-005 incorpora el reenvío del perfil observado o rechazado (FR-023, FR-058).

## Capacidades

### Capacidades nuevas

- `degree-projects` registra el perfil inicial del proyecto de grado y la designación del tutor.

### Capacidades modificadas

Ninguna.

## Impacto

El subsistema `procedures` reutiliza el trámite de UC-002, las cuentas de UC-001 y el canal de correo. Cambian la persistencia de proyectos, el glosario, la nota de FR-003, la guía de comandos y las pruebas. Se sigue la arquitectura de referencia porque el diseño de UC-003 en EA no está disponible en esta sesión.
