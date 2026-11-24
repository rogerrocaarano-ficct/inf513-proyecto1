# Proposal

## Why

Hoy el rol de una cuenta se fija al crearla y no se puede cambiar: si un docente pasa a ser director o una cuenta se creó con el rol equivocado, no hay comando para corregirlo. FR-031 pide modificar los datos y el rol de una cuenta.

Pertenece a **UC-001 Gestión de usuarios**, flujo alternativo **A1 Modificar una cuenta**, solo en la parte del rol.

## What Changes

- Comando por correo `usuarios.cambiarRol[correo; rol]`, con exactamente 2 argumentos (el correo de la cuenta afectada y el rol nuevo) y ningún adjunto.
- Permiso doble (UC-001 A1, paso 2): quien lo pide debe poder cambiar el rol de las cuentas del rol actual y también del rol nuevo. El administrador cambia el rol de cuentas de todos los roles; el director, entre estudiante, administrativo, docente y docente de Taller de Grado (FR-050, FR-053). El administrativo y los demás roles no cambian roles. Sin permiso, el rechazo es E1.
- Rechazos: correo sin cuenta (E4); cambiar el rol de la propia cuenta (E6); quitar el rol de administrador a la única cuenta de administrador activa (E5, FR-038); rol nuevo igual al actual (regla nueva, se agrega a UC-001); pasar a estudiante una cuenta sin CI, código o teléfono, indicando cada dato que falta (E2, FR-026).
- Avisos después de confirmar el cambio: al solicitante y a la cuenta afectada, con el rol anterior, el rol nuevo y el nombre de quien hizo el cambio (FR-035).
- La ayuda (`ayuda[]`) muestra el comando a quien tiene alguno de sus permisos: al administrador y al director, no al administrativo.

Fuera de alcance: modificar los datos de una cuenta (resto de A1); FR-034 (advertir los trámites en curso de la cuenta que pierde un rol, que tampoco cumple hoy A2), que queda para un cambio que cubra A1 y A2; A4 y A5.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `user-accounts`: se agregan los requisitos del cambio de rol (comando, permiso doble, rechazos, datos del estudiante y avisos).

## Impact

- **Elementos de EA en los que se basa:** ninguno. El modelo EA está en pausa y no se consulta; UC-001 A1 no tiene diseño en EA para este cambio. Se sigue la arquitectura de referencia de AGENTS.md (ver "Diferencias con el modelo" en el design).
- **Requisitos:** FR-031 (en la parte del rol), FR-026, FR-035, FR-038, FR-050, FR-053. FR-034 sigue pendiente.
- **Código:** `bo.org.tecnoweb.cpms.users` (`domain`, `application.usermanagement`, `infrastructure`). Sin migración: la columna `role` de `user_account` ya existe y `JpaUserRepository.update` la copia.
- **Documentos:** `docs/use-cases/UC-001.md` (excepción de rol igual al actual), `docs/user-guide/usuarios.cambiarRol.md` (página nueva) y su fila en `docs/user-guide/README.md`.
- **Dependencias:** ninguna nueva.
