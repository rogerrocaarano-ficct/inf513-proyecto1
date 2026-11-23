# Propuesta

## Why

Secretaría necesita abrir el expediente de titulación y registrar los documentos iniciales por correo (UC-002). Esta propuesta incorpora el caso en la rama derivada de `develop` con la arquitectura de referencia.

## What Changes

- Abrir el trámite con cuatro documentos iniciales, creando o vinculando la cuenta del estudiante en la misma transacción.
- Reemplazar un documento inicial mientras el trámite esté en curso.
- Confirmar ambas operaciones por correo y reutilizar el aviso de alta de UC-001 al crear la cuenta.

## Capabilities

### New Capabilities

- `degree-procedures`: apertura de expedientes y reemplazo de documentos iniciales.

### Modified Capabilities

## Impact

FR-001, FR-002, FR-033, FR-035, FR-051, FR-054, FR-055 y FR-056. Afecta a procedimientos, cuentas, persistencia, pruebas y guía de usuario. EA no está disponible para consultar el diseño de UC-002.
