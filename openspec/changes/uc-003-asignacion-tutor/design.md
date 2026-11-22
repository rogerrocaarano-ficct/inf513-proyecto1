# Diseño

## Contexto

El trámite de UC-002 tiene identidad UUID y referencia pública. UC-003 usa el canal de correo y las cuentas de UC-001. FR-005 pertenece a UC-004.

## Decisiones

- `DegreeProject` es un agregado de `procedures.domain`, relacionado uno a uno por UUID con `DegreeProcedure`. Conserva el título, `ProjectProfile` inicial y el UUID del tutor. Su primera entrega deja el perfil pendiente. La designación exige el visto bueno persistido y el proyecto sin tutor. No hay comando para aprobar el perfil en UC-003.
- La aplicación bloquea el trámite en curso antes de leer o escribir su proyecto. La restricción única sobre el UUID del trámite protege también la integridad en persistencia.
- `proyectos.registrarPerfil[titulo]` recibe un PDF y encuentra el trámite por el estudiante autenticado. `proyectos.asignarTutor[referencia; correoTutor]` recibe dos argumentos y cero adjuntos.
- La aplicación reutiliza `UserRepository` y declara permisos de presentación, designación y elegibilidad del tutor. Taller se obtiene por su rol y cuenta activa. Si no hay destinatario activo, la entrega se rechaza antes de guardar.
- El perfil se guarda como binario en PostgreSQL mediante JPA. El canal conserva su transacción y el adaptador SMTP envía después del commit.
- Los tests preparan un perfil aprobado directamente como dato de prueba. UC-004 será el productor del visto bueno en operación.

## Diferencias con el modelo

UC-003 sin diseño en EA; se sigue la arquitectura de referencia.

- Dominio: `DegreeProject`, `ProjectProfile`, `DegreeProjectRepository`, `DegreeProjectProfileRegisteredEvent` y `DegreeProjectTutorAssignedEvent`. Operaciones `registerInitialProfile` y `assignTutor`.
- Aplicación: `TutorAssignmentUseCase.assignTutor` (básico) y `registerInitialProfile` (A1), `RegisterInitialProfileCommand`, `AssignTutorCommand`, sus handlers y `TutorAssignmentNotifier`.
- Infraestructura: `MailTutorAssignmentNotifier`, `JpaDegreeProjectRepository`, `DegreeProjectEntity`, `SpringDataDegreeProjectRepository` y beans en `ProceduresConfiguration`. El agregado referencia trámite y tutor por identidad. Se añade la consulta de trámite abierto por estudiante al repositorio existente.
