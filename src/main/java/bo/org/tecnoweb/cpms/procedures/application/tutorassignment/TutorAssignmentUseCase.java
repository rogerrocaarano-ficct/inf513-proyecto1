package bo.org.tecnoweb.cpms.procedures.application.tutorassignment;

import bo.org.tecnoweb.cpms.common.DomainValidationException;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProcedure;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProcedureRepository;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProject;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProjectRepository;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProjectTutorAssignedEvent;
import bo.org.tecnoweb.cpms.users.domain.RolePermission;
import bo.org.tecnoweb.cpms.users.domain.User;
import bo.org.tecnoweb.cpms.users.domain.UserNotAuthorizedException;
import bo.org.tecnoweb.cpms.users.domain.UserRepository;
import java.util.Map;

/**
 * Registers the tutor designation (UC-003) once the profile is approved (UC-004).
 */
public class TutorAssignmentUseCase {
    private final UserRepository users;
    private final DegreeProcedureRepository procedures;
    private final DegreeProjectRepository projects;
    private final TutorAssignmentNotifier notifier;

    /** Receives the repositories and notification port for UC-003. */
    public TutorAssignmentUseCase(UserRepository users, DegreeProcedureRepository procedures,
            DegreeProjectRepository projects, TutorAssignmentNotifier notifier) {
        this.users = users;
        this.procedures = procedures;
        this.projects = projects;
        this.notifier = notifier;
    }

    /** Basic flow: designates the tutor after the profile approval (FR-003, FR-005, FR-020). */
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
                procedure.getReference());
        projects.update(project);
        for (var event : project.pullEvents()) {
            if (event instanceof DegreeProjectTutorAssignedEvent assigned) {
                notifier.notifyTutorAssigned(assigned);
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
