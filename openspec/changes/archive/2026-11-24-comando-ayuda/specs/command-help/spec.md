# Spec Delta

## Purpose

Permitir que cada usuario consulte por correo los comandos que su rol le permite usar, con su sintaxis y para qué sirve cada uno, sin tener que leer la guía de comandos (FR-066).

## ADDED Requirements

### Requirement: Comando de ayuda

El sistema SHALL atender el comando `ayuda[]`, sin argumentos ni adjuntos, y responder por correo al remitente con la lista de los comandos que su rol le permite usar (FR-066). La respuesta SHALL incluir siempre `ayuda[]`.

#### Scenario: Estudiante pide ayuda
- **GIVEN** una cuenta activa de estudiante
- **WHEN** envía el comando `ayuda[]`
- **THEN** recibe una respuesta que incluye `proyectos.registrarPerfil` y `ayuda`
- **AND** la respuesta no incluye ningún comando `usuarios.*` ni `tramites.*`

#### Scenario: Director pide ayuda
- **GIVEN** una cuenta activa de director
- **WHEN** envía el comando `ayuda[]`
- **THEN** recibe una respuesta que incluye `usuarios.crearCuenta`, `usuarios.listar`, `usuarios.activar`, `usuarios.desactivar`, `proyectos.asignarTutor` y `ayuda`
- **AND** la respuesta no incluye `proyectos.registrarPerfil` ni `tramites.abrirTramite`

#### Scenario: Comando de ayuda con mayúsculas distintas
- **WHEN** una cuenta activa envía el comando `AYUDA[]`
- **THEN** recibe la misma respuesta que con `ayuda[]`

#### Scenario: Ayuda con argumentos
- **WHEN** una cuenta activa envía el comando `ayuda[usuarios.listar]`
- **THEN** el sistema no lo ejecuta y responde que el comando `ayuda` requiere 0 argumentos y llegó 1

### Requirement: Contenido de cada comando en la ayuda

Para cada comando que lista, la ayuda SHALL mostrar su sintaxis, con el nombre de cada argumento, y para qué sirve. Cuando el comando pide adjuntos o texto en el cuerpo del correo, SHALL indicarlo. Los comandos SHALL aparecer ordenados por nombre.

#### Scenario: Comando con adjuntos
- **GIVEN** una cuenta activa de administrativo
- **WHEN** envía el comando `ayuda[]`
- **THEN** la respuesta muestra `tramites.abrirTramite[registro]`, para qué sirve, y que pide 4 adjuntos PDF

#### Scenario: Comando con texto en el cuerpo
- **GIVEN** una cuenta activa de docente de Taller de Grado
- **WHEN** envía el comando `ayuda[]`
- **THEN** la respuesta muestra `proyectos.observarPerfil[referencia]` e indica que las observaciones van en el cuerpo del correo

### Requirement: Permisos parciales en la ayuda

Cuando el rol del remitente tiene solo una parte de los permisos que un comando admite, porque el permiso depende de un argumento, la ayuda SHALL listar el comando junto con lo que el remitente puede hacer con él. Si el rol tiene todos los permisos del comando, no se agrega aclaración.

#### Scenario: Administrativo y la creación de cuentas
- **GIVEN** una cuenta activa de administrativo, que solo puede crear y listar cuentas de estudiante
- **WHEN** envía el comando `ayuda[]`
- **THEN** la respuesta incluye `usuarios.crearCuenta` y aclara que puede crear cuentas de estudiante
- **AND** incluye `usuarios.listar` y aclara que puede listar cuentas de estudiante
- **AND** no incluye `usuarios.activar` ni `usuarios.desactivar`

#### Scenario: Administrador con todos los permisos de un comando
- **GIVEN** una cuenta activa de administrador
- **WHEN** envía el comando `ayuda[]`
- **THEN** la respuesta incluye `usuarios.crearCuenta` sin ninguna aclaración de lo que puede hacer, porque puede crear cuentas de todos los roles

### Requirement: Rol sin comandos

Cuando el rol del remitente no tiene ningún comando además de `ayuda[]`, la respuesta SHALL decir que por ahora su cuenta no tiene comandos disponibles.

#### Scenario: Docente pide ayuda
- **GIVEN** una cuenta activa de docente, cuyo rol no tiene comandos
- **WHEN** envía el comando `ayuda[]`
- **THEN** recibe una respuesta que dice que por ahora su cuenta no tiene comandos disponibles

### Requirement: Ayuda solo para cuentas activas

El comando `ayuda[]` SHALL atenderse solo para un remitente con una cuenta activa. Un remitente sin cuenta o con la cuenta desactivada SHALL recibir el mismo rechazo que con cualquier otro comando, sin la lista de comandos.

#### Scenario: Remitente sin cuenta
- **WHEN** una dirección sin cuenta envía el comando `ayuda[]`
- **THEN** recibe la respuesta de que su dirección no está registrada en el sistema
- **AND** no recibe ninguna lista de comandos

#### Scenario: Cuenta desactivada
- **GIVEN** una cuenta desactivada
- **WHEN** envía el comando `ayuda[]`
- **THEN** recibe la respuesta de que su cuenta está desactivada
- **AND** no recibe ninguna lista de comandos

### Requirement: La ayuda no cambia datos

El comando `ayuda[]` MUST NOT cambiar ningún dato del sistema. La respuesta SHALL enviarse después de que el correo quede procesado.

#### Scenario: Ayuda pedida dos veces
- **GIVEN** una cuenta activa
- **WHEN** envía el comando `ayuda[]` dos veces
- **THEN** recibe dos respuestas iguales y ningún dato del sistema cambia
