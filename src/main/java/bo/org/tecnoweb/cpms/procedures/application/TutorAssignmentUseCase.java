package bo.org.tecnoweb.cpms.procedures.application;

import bo.org.tecnoweb.cpms.common.DomainValidationException;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProcedure;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProcedureRepository;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProject;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProjectProfileRegisteredEvent;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProjectRepository;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProjectTutorAssignedEvent;
import bo.org.tecnoweb.cpms.procedures.domain.InitialDocument;
import bo.org.tecnoweb.cpms.storage.application.StoredFileService;
import bo.org.tecnoweb.cpms.storage.domain.StoredFile;
import bo.org.tecnoweb.cpms.users.domain.Role;
import bo.org.tecnoweb.cpms.users.domain.RolePermission;
import bo.org.tecnoweb.cpms.users.domain.User;
import bo.org.tecnoweb.cpms.users.domain.UserNotAuthorizedException;
import bo.org.tecnoweb.cpms.users.domain.UserRepository;
import java.util.Map;

/**
 * Registers the initial profile and the tutor designation (UC-003). The profile PDF arrives as a
 * file the mail channel already stored; the use case validates it and adopts it (issue #9).
 */
public class TutorAssignmentUseCase {
    private final UserRepository users;
    private final DegreeProcedureRepository procedures;
    private final DegreeProjectRepository projects;
    private final TutorAssignmentNotifier notifier;
    private final StoredFileService storedFiles;

    /** Receives the repositories, notification port and file storage for UC-003. */
    public TutorAssignmentUseCase(UserRepository users, DegreeProcedureRepository procedures,
            DegreeProjectRepository projects, TutorAssignmentNotifier notifier, StoredFileService storedFiles) {
        this.users = users;
        this.procedures = procedures;
        this.projects = projects;
        this.notifier = notifier;
        this.storedFiles = storedFiles;
    }

    /** Basic flow: designates the tutor after the workshop approval (FR-003, FR-020). */
    public void assignTutor(String requesterEmail, AssignTutorCommand command) {
        requireActiveRequester(requesterEmail, RolePermission.ASSIGN_TUTOR);
        DegreeProcedure procedure = procedures.getOpenByReferenceForUpdate(
                DegreeProcedure.parseReference(command.reference())).orElseThrow(DegreeProcedure::unavailable);
        DegreeProject project = projects.getByProcedureId(procedure.getId())
                .orElseThrow(() -> invalid("perfil", "El trámite todavía no tiene un perfil de proyecto."));
        User tutor = users.getByEmail(command.tutorEmail()).filter(User::isActive)
                .filter(user -> user.hasPermissionTo(RolePermission.BE_PROJECT_TUTOR))
                .orElseThrow(() -> invalid("tutor", "Ingrese el correo de un docente activo."));
        User student = users.getById(procedure.getStudentId()).orElseThrow();
        project.assignTutor(tutor.getId(), tutor.getFullName(), student.getEmailAddress(),
                DegreeProcedure.referenceLabel(procedure.getReference()));
        projects.update(project);
        for (var event : project.pullEvents()) {
            if (event instanceof DegreeProjectTutorAssignedEvent assigned) {
                notifier.notifyTutorAssigned(assigned);
            }
        }
    }

    /** A1: creates the project with the student's first profile (FR-004, FR-057, FR-058). */
    public void registerInitialProfile(String requesterEmail, RegisterInitialProfileCommand command) {
        User student = requireActiveRequester(requesterEmail, RolePermission.REGISTER_INITIAL_PROFILE);
        DegreeProcedure procedure = procedures.getOpenByStudentForUpdate(student.getId())
                .orElseThrow(() -> invalid("trámite", "Necesita un trámite de titulación en curso."));
        if (projects.getByProcedureId(procedure.getId()).isPresent()) {
            throw DegreeProject.alreadyRegistered();
        }
        User workshop = users.getByRole(Role.CAPSTONE_PROJECT_PROFESSOR).stream().filter(User::isActive)
                .findFirst().orElseThrow(() -> invalid("perfil",
                        "No hay un docente de Taller de Grado activo. Intente nuevamente más adelante."));
        StoredFile file = storedFiles.get(command.attachmentId());
        InitialDocument.requirePdf(file, storedFiles.readContent(file.getId()));
        DegreeProject project = DegreeProject.builder().withProcedureId(procedure.getId()).withTitle(command.title())
                .registerInitialProfile(file, student.getEmailAddress(), workshop.getEmailAddress(),
                        DegreeProcedure.referenceLabel(procedure.getReference()));
        projects.insert(project);
        for (var event : project.pullEvents()) {
            if (event instanceof DegreeProjectProfileRegisteredEvent registered) {
                notifier.notifyProfileRegistered(registered);
            }
        }
    }

    private User requireActiveRequester(String email, RolePermission permission) {
        User requester = users.getByEmail(email).orElseThrow(() -> UserNotAuthorizedException.notRegistered(email));
        if (!requester.isActive()) {
            throw UserNotAuthorizedException.disabled(requester.getEmailAddress());
        }
        if (!requester.hasPermissionTo(permission)) {
            throw new UserNotAuthorizedException(requester.getEmailAddress(), permission.getLabel());
        }
        return requester;
    }

    private static DomainValidationException invalid(String field, String message) {
        return new DomainValidationException(Map.of(field, message));
    }
}
