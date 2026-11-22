package bo.org.tecnoweb.cpms.procedures.application;

import bo.org.tecnoweb.cpms.common.DomainValidationException;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProcedure;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProcedureDocumentReplacedEvent;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProcedureOpenedEvent;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProcedureRepository;
import bo.org.tecnoweb.cpms.users.application.UserManagementNotifier;
import bo.org.tecnoweb.cpms.users.domain.Role;
import bo.org.tecnoweb.cpms.users.domain.RolePermission;
import bo.org.tecnoweb.cpms.users.domain.User;
import bo.org.tecnoweb.cpms.users.domain.UserNotAuthorizedException;
import bo.org.tecnoweb.cpms.users.domain.UserRepository;
import bo.org.tecnoweb.cpms.users.domain.UserSignedUpEvent;
import java.util.Map;

/** Opens graduation procedures and replaces initial documents (UC-002). */
public class DegreeProcedureUseCase {
    private final UserRepository users;
    private final DegreeProcedureRepository procedures;
    private final UserManagementNotifier userNotifier;
    private final DegreeProcedureNotifier notifier;

    /** Receives the repositories and notification ports used by UC-002. */
    public DegreeProcedureUseCase(UserRepository users, DegreeProcedureRepository procedures,
            UserManagementNotifier userNotifier, DegreeProcedureNotifier notifier) {
        this.users = users;
        this.procedures = procedures;
        this.userNotifier = userNotifier;
        this.notifier = notifier;
    }

    /** Basic flow and A1: opens the procedure, creating the student account if needed (FR-051, FR-056). */
    public void open(String requesterEmail, OpenDegreeProcedureCommand command) {
        User requester = requireActiveRequester(requesterEmail, RolePermission.OPEN_DEGREE_PROCEDURE);
        User candidate = User.builder()
                .withRole(Role.STUDENT)
                .withFirstName(command.firstName())
                .withLastName(command.lastName())
                .withSecondLastName(command.secondLastName())
                .withCi(command.ci())
                .withUagrmCode(command.code())
                .withEmailAddress(command.email())
                .withPhoneNumber(command.phoneNumber())
                .signUp(requester);
        User student = users.getByEmailForUpdate(candidate.getEmailAddress())
                .map(existing -> DegreeProcedure.requireMatchingStudent(existing, candidate))
                .orElse(candidate);
        if (procedures.existsOpenForStudent(student.getId())) {
            throw new DomainValidationException(Map.of("registro", "El estudiante ya tiene un trámite de titulación en curso."));
        }
        DegreeProcedure procedure = DegreeProcedure.builder().withStudent(student)
                .open(student, requester, command.attachments());
        if (student == candidate) {
            registerStudent(candidate);
        }
        DegreeProcedure stored = procedures.insert(procedure);
        for (var event : procedure.pullEvents()) {
            if (event instanceof DegreeProcedureOpenedEvent opened) {
                notifier.notifyOpened(opened, DegreeProcedure.referenceLabel(stored.getReference()));
            }
        }
    }

    /** A2: replaces a document in an ongoing procedure (FR-055). */
    public void replace(String requesterEmail, ReplaceInitialDocumentCommand command) {
        User requester = requireActiveRequester(requesterEmail, RolePermission.REPLACE_INITIAL_DOCUMENT);
        DegreeProcedure procedure = procedures.getOpenByReferenceForUpdate(parseReference(command.reference()))
                .orElseThrow(DegreeProcedure::unavailable);
        procedure.replaceInitialDocument(requester, command.attachment());
        procedures.update(procedure);
        for (var event : procedure.pullEvents()) {
            if (event instanceof DegreeProcedureDocumentReplacedEvent replaced) {
                notifier.notifyDocumentReplaced(replaced);
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

    private void registerStudent(User candidate) {
        try {
            users.insert(candidate);
        } catch (DomainValidationException duplicate) {
            throw DegreeProcedure.incompatibleAccount();
        }
        for (var event : candidate.pullEvents()) {
            if (event instanceof UserSignedUpEvent signedUp) {
                userNotifier.notifyNewAccount(signedUp);
            }
        }
    }

    private static long parseReference(String reference) {
        if (reference.matches("TR-[0-9]+")) {
            try {
                return Long.parseLong(reference.substring(3));
            } catch (NumberFormatException ignored) {
                // Out-of-range references receive the same validation error.
            }
        }
        throw new DomainValidationException(Map.of("referencia", "Ingrese la referencia del trámite, por ejemplo TR-001."));
    }

}
