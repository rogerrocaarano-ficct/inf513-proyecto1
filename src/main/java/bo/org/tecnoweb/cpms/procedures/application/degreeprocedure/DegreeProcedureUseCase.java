package bo.org.tecnoweb.cpms.procedures.application.degreeprocedure;

import bo.org.tecnoweb.cpms.common.DomainValidationException;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProcedure;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProcedureRequirementReplacedEvent;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProcedureOpenedEvent;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProcedureRepository;
import bo.org.tecnoweb.cpms.procedures.domain.OpeningRequirement;
import bo.org.tecnoweb.cpms.procedures.domain.PdfDocument;
import bo.org.tecnoweb.cpms.procedures.domain.Semester;
import bo.org.tecnoweb.cpms.storage.application.StoredFileService;
import bo.org.tecnoweb.cpms.storage.domain.StoredFile;
import bo.org.tecnoweb.cpms.users.domain.RolePermission;
import bo.org.tecnoweb.cpms.users.domain.User;
import bo.org.tecnoweb.cpms.users.domain.UserNotAuthorizedException;
import bo.org.tecnoweb.cpms.users.domain.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Opens graduation procedures and replaces the documents of their opening requirements (UC-002). The procedure is opened on a
 * student account created beforehand by UC-001, found by its registration number. The PDFs arrive as
 * files the mail channel already stored; the use case validates them and adopts them as documents
 * (issue #9).
 */
public class DegreeProcedureUseCase {
    private final UserRepository users;
    private final DegreeProcedureRepository procedures;
    private final DegreeProcedureNotifier notifier;
    private final StoredFileService storedFiles;
    private final AcademicCalendar calendar;

    /** Receives the repositories, the notification port, the file storage and the academic calendar of UC-002. */
    public DegreeProcedureUseCase(UserRepository users, DegreeProcedureRepository procedures,
            DegreeProcedureNotifier notifier, StoredFileService storedFiles, AcademicCalendar calendar) {
        this.users = users;
        this.procedures = procedures;
        this.notifier = notifier;
        this.storedFiles = storedFiles;
        this.calendar = calendar;
    }

    /**
     * Basic flow: opens the procedure of an existing student account, found by its registration
     * number, with one PDF per opening requirement (FR-001, FR-002, FR-051, FR-056). A student opens at most
     * one procedure per semester.
     *
     * @param requesterEmail the address of the sender
     * @param command        the registration number and the stored files of the attachments
     * @throws UserNotAuthorizedException if the sender may not open procedures
     * @throws DomainValidationException  if no account has the registration number, the account
     *                                    cannot hold a procedure, already has one ongoing or in the
     *                                    semester, or a PDF is invalid
     */
    public void open(String requesterEmail, OpenDegreeProcedureCommand command) {
        User requester = requireActiveRequester(requesterEmail, RolePermission.OPEN_DEGREE_PROCEDURE);
        User student = requireAccount(command.code());
        if (procedures.existsOpenForStudent(student.getId())) {
            throw new DomainValidationException(Map.of(DegreeProcedure.CODE_FIELD,
                    "El estudiante ya tiene un trámite de titulación en curso."));
        }
        Semester semester = calendar.currentSemester();
        if (procedures.existsByReference(DegreeProcedure.referenceOf(semester, student))) {
            throw new DomainValidationException(Map.of(DegreeProcedure.CODE_FIELD,
                    "El estudiante ya tiene un trámite de titulación en el semestre " + semester + "."));
        }
        List<StoredFile> documents = command.attachmentIds().stream().map(this::requirePdf).toList();
        DegreeProcedure procedure = DegreeProcedure.builder().open(student, requester, semester, documents);
        procedures.insert(procedure);
        for (var event : procedure.pullEvents()) {
            if (event instanceof DegreeProcedureOpenedEvent opened) {
                notifier.notifyOpened(opened);
            }
        }
    }

    /**
     * A2: replaces the document of an opening requirement in an ongoing procedure (FR-055). The
     * received PDF becomes the current version of the requirement and the replaced one is kept as its
     * previous version (issue #9).
     */
    public void replaceRequirement(String requesterEmail, ReplaceOpeningRequirementCommand command) {
        User requester = requireActiveRequester(requesterEmail, RolePermission.REPLACE_OPENING_REQUIREMENT);
        DegreeProcedure procedure = procedures.getOpenByReferenceForUpdate(DegreeProcedure.parseReference(command.reference()))
                .orElseThrow(DegreeProcedure::unavailable);
        StoredFile file = requirePdf(command.attachmentId());
        UUID previousId = procedure.requirementOf(OpeningRequirement.classify(file));
        procedure.replaceRequirement(requester, file);
        storedFiles.appendVersion(previousId, file.getId());
        procedures.update(procedure);
        for (var event : procedure.pullEvents()) {
            if (event instanceof DegreeProcedureRequirementReplacedEvent replaced) {
                notifier.notifyRequirementReplaced(replaced);
            }
        }
    }

    /**
     * Finds and locks the account of a registration number (FR-056).
     *
     * @param code the registration number written in the command
     * @return the account
     * @throws DomainValidationException if the number is empty or no account has it
     */
    private User requireAccount(String code) {
        if (code == null || code.isBlank()) {
            throw new DomainValidationException(Map.of(DegreeProcedure.CODE_FIELD,
                    "Ingrese el registro universitario del estudiante."));
        }
        String value = code.strip();
        return users.getByUagrmCodeForUpdate(value).orElseThrow(() -> new DomainValidationException(Map.of(
                DegreeProcedure.CODE_FIELD,
                "No existe una cuenta con el registro " + value + ". Créela primero con usuarios.crearCuenta.")));
    }

    /**
     * Reads a received file from the file storage and checks it is a valid PDF (NFR-005).
     *
     * @param id the stored file of an attachment
     * @return the stored file
     */
    private StoredFile requirePdf(UUID id) {
        StoredFile file = storedFiles.get(id);
        PdfDocument.require(file, storedFiles.readContent(id));
        return file;
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
}
