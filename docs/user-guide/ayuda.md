# `ayuda`: ver los comandos que puede usar

[Guía de comandos](README.md) › Ayuda · FR-066

Envía la lista de los comandos que el rol de su cuenta le permite usar, con la sintaxis de cada uno y para qué sirve.

**Sintaxis:**

```text
ayuda[]
```

**Parámetros:** ninguno. **Adjuntos:** ninguno. **Cuerpo:** no se lee.

`ayuda[usuarios.listar]` se rechaza porque sobra un parámetro: la ayuda no se pide para un solo comando.

**Quién puede usarlo:** cualquier cuenta activa.

**Respuesta:** el asunto es `Comandos que puede usar`. Cada comando, ordenado por nombre, muestra:

- su sintaxis, con el nombre de cada parámetro;
- para qué sirve;
- los adjuntos que pide o el texto que se escribe en el cuerpo, cuando corresponde;
- qué puede hacer con él su rol, cuando el permiso depende de un parámetro y el rol tiene solo una parte. Por ejemplo, el administrativo puede usar `usuarios.crearCuenta` solo para crear cuentas de estudiante.

La lista incluye siempre `ayuda[]`. Si el rol no tiene ningún otro comando, la respuesta empieza diciendo que por ahora la cuenta no tiene comandos disponibles.

**Rechazos:** los mismos que para cualquier comando: la dirección no tiene cuenta, o la cuenta está desactivada. En ese caso no se envía la lista.

**Ejemplo:** un estudiante envía `ayuda[]` y recibe:

```text
Asunto: Comandos que puede usar

Comandos que puede usar:

ayuda[]
  Ver los comandos que puede usar.

proyectos.registrarPerfil[titulo]
  Presentar el perfil del proyecto o una versión nueva.
  Adjuntos: 1 PDF con el perfil.
```

Un administrativo ve, entre otros:

```text
usuarios.crearCuenta[nombres; primerApellido; segundoApellido; ci; codigo; correo; telefono; rol]
  Crear una cuenta de usuario con un rol.
  Puede: crear cuentas de estudiante.
```
