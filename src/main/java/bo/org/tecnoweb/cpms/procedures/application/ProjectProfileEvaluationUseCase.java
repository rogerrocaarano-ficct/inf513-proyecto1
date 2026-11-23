package bo.org.tecnoweb.cpms.procedures.application;

import bo.org.tecnoweb.cpms.common.DomainEvent;
import bo.org.tecnoweb.cpms.common.DomainValidationException;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProcedure;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProcedureRepository;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProject;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProjectProfileApprovedEvent;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProjectProfileObservedEvent;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProjectProfileRegisteredEvent;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProjectProfileRejectedEvent;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProjectRepository;
import bo.org.tecnoweb.cpms.procedures.domain.PdfDocument;
import bo.org.tecnoweb.cpms.storage.application.StoredFileService;
import bo.org.tecnoweb.cpms.storage.domain.StoredFile;
import bo.org.tecnoweb.cpms.users.domain.Role;
import bo.org.tecnoweb.cpms.users.domain.RolePermission;
import bo.org.tecnoweb.cpms.users.domain.User;
import bo.org.tecnoweb.cpms.users.domain.UserNotAuthorizedException;
import bo.org.tecnoweb.cpms.users.domain.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * UC-004 Evaluación del perfil de proyecto: the student submits the profile and the workshop teacher
 * approves, observes or rejects it. The profile PDF arrives as a file the mail channel already stored;
 * the use case validates it and adopts it. The project numbers the versions; the file storage does not
 * chain them.
 */
public class ProjectProfileEvaluationUseCase {
    private final UserRepository users;
    private final DegreeProcedureRepository procedures;
    private final DegreeProjectRepository projects;
    private final ProjectProfileEvaluationNotifier notifier;
    private final StoredFileService storedFiles;

    /** Receives the repositories, the notification port and the file storage of UC-004. */
    public ProjectProfileEvaluationUseCase(UserRepository users, DegreeProcedureRepository procedures,
            DegreeProjectRepository projects, ProjectProfileEvaluationNotifier notifier,
            StoredFileService storedFiles) {
        this.users = users;
        this.procedures = procedures;
        this.projects = projects;
        this.notifier = notifier;
        this.storedFiles = storedFiles;
    }

    /**
     * Basic flow: the student submits the profile, the first one or a new version after an observation
     * or a rejection, and the workshop teacher receives it (FR-004, FR-023, FR-057, FR-058).
     *
     * @param requesterEmail the email of the student
     * @param command        the title and the stored PDF
     */
    public void registerProfile(String requesterEmail, RegisterProfileCommand command) {
        User student = requireActiveRequester(requesterEmail, RolePermission.REGISTER_PROJECT_PROFILE);
        DegreeProcedure procedure = procedures.getOpenByStudentForUpdate(student.getId())
                .orElseThrow(() -> invalid("trámite", "Necesita un trámite de titulación en curso."));
        Optional<DegreeProject> existing = projects.getByProcedureId(procedure.getId());
        User workshop = users.getByRole(Role.CAPSTONE_PROJECT_PROFESSOR).stream().filter(User::isActive)
                .findFirst().orElseThrow(() -> invalid("perfil",
                        "No hay un docente de Taller de Grado activo. Intente nuevamente más adelante."));
        StoredFile file = storedFiles.get(command.attachmentId());
        PdfDocument.require(file, storedFiles.readContent(file.getId()));
        DegreeProject project = existing.orElseGet(() -> DegreeProject.builder().withProcedureId(procedure.getId()))
                .registerProfile(file, command.title(), student.getEmailAddress(), workshop.getEmailAddress(),
                        procedure.getReference());
        if (existing.isPresent()) {
            projects.update(project);
        } else {
            projects.insert(project);
        }
        publish(project.pullEvents());
    }

    /**
     * A1: the workshop teacher approves the pending profile; the student and the director are notified
     * (FR-005, FR-060, FR-061). Without an active director, the approval is recorded all the same.
     *
     * @param requesterEmail the email of the workshop teacher
     * @param reference      the reference of the procedure
     */
    public void approveProfile(String requesterEmail, String reference) {
        requireActiveRequester(requesterEmail, RolePermission.EVALUATE_PROJECT_PROFILE);
        DegreeProcedure procedure = requireOpenProcedure(reference);
        DegreeProject project = requireProject(procedure);
        String director = users.getByRole(Role.DIRECTOR).stream().filter(User::isActive).findFirst()
                .map(User::getEmailAddress).orElse(null);
        project.approveProfile(studentEmail(procedure), director, procedure.getReference());
        projects.update(project);
        publish(project.pullEvents());
    }

    /**
     * A2: the workshop teacher observes the pending profile, asking for changes on the same topic
     * (FR-022, FR-060).
     *
     * @param requesterEmail the email of the workshop teacher
     * @param command        the reference and the observations
     */
    public void observeProfile(String requesterEmail, ObserveProfileCommand command) {
        requireActiveRequester(requesterEmail, RolePermission.EVALUATE_PROJECT_PROFILE);
        DegreeProcedure procedure = requireOpenProcedure(command.reference());
        DegreeProject project = requireProject(procedure);
        project.observeProfile(command.observations(), studentEmail(procedure), procedure.getReference());
        projects.update(project);
        publish(project.pullEvents());
    }

    /**
     * A3: the workshop teacher rejects the pending profile because its topic must change (FR-022, FR-060).
     *
     * @param requesterEmail the email of the workshop teacher
     * @param command        the reference and the reason
     */
    public void rejectProfile(String requesterEmail, RejectProfileCommand command) {
        requireActiveRequester(requesterEmail, RolePermission.EVALUATE_PROJECT_PROFILE);
        DegreeProcedure procedure = requireOpenProcedure(command.reference());
        DegreeProject project = requireProject(procedure);
        project.rejectProfile(command.observations(), studentEmail(procedure), procedure.getReference());
        projects.update(project);
        publish(project.pullEvents());
    }

    private DegreeProcedure requireOpenProcedure(String reference) {
        return procedures.getOpenByReferenceForUpdate(DegreeProcedure.parseReference(reference))
                .orElseThrow(DegreeProcedure::unavailable);
    }

    private DegreeProject requireProject(DegreeProcedure procedure) {
        return projects.getByProcedureId(procedure.getId()).orElseThrow(() -> invalid("perfil",
                "El trámite no tiene un perfil de proyecto pendiente de evaluación."));
    }

    private String studentEmail(DegreeProcedure procedure) {
        return users.getById(procedure.getStudentId()).orElseThrow().getEmailAddress();
    }

    private void publish(List<DomainEvent> events) {
        for (DomainEvent event : events) {
            switch (event) {
                case DegreeProjectProfileRegisteredEvent registered -> notifier.notifyProfileRegistered(registered);
                case DegreeProjectProfileApprovedEvent approved -> notifier.notifyProfileApproved(approved);
                case DegreeProjectProfileObservedEvent observed -> notifier.notifyProfileObserved(observed);
                case DegreeProjectProfileRejectedEvent rejected -> notifier.notifyProfileRejected(rejected);
                default -> { }
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
