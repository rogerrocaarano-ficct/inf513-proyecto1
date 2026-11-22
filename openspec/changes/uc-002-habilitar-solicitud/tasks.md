# Tareas

## 1. Dominio y persistencia

- [x] 1.1 Implementar el agregado, documentos, eventos, permisos y repositorio de dominio. Probar las reglas de apertura y sustitución.
- [x] 1.2 Incorporar la migración de UC-002 y el repositorio JPA. Probar persistencia y unicidad con Testcontainers.

## 2. Aplicación y correo

- [x] 2.1 Implementar el caso de uso y los dos handlers con sus contratos. Probar permisos, vinculación, alta, rechazos y delegación con dobles de los puertos.
- [x] 2.2 Implementar el puerto y adaptador de notificación, reutilizar el aviso de alta de UC-001 y probar los eventos, los avisos y el cableado.

## 3. Aceptación

- [x] 3.1 Actualizar `USER-GUIDE.md` y `GLOSSARY.md`. Verificar que `auxiliary-docs/` quede fuera de la rama.
- [x] 3.2 Conservar los cuatro escenarios de integración por correo y ejecutar el build completo.
- [x] 3.3 Cotejar la implementación con la arquitectura de referencia de `AGENTS.md` y validar los artefactos con OpenSpec.
