package bo.org.tecnoweb.cpms.procedures.application;

import bo.org.tecnoweb.cpms.common.DomainValidationException;
import bo.org.tecnoweb.cpms.mail.domain.File;
import bo.org.tecnoweb.cpms.mail.domain.ReceivedEmail;
import bo.org.tecnoweb.cpms.procedures.domain.InitialDocument;
import bo.org.tecnoweb.cpms.users.domain.Role;
import bo.org.tecnoweb.cpms.users.domain.User;
import bo.org.tecnoweb.cpms.users.domain.UserNotAuthorizedException;
import bo.org.tecnoweb.cpms.users.domain.UserRepository;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class DegreeProcedureUseCase {
    private final UserRepository users;
    private final DegreeProcedureRepository procedures;
    private final Consumer<Opened> onOpened;
    private final Consumer<DocumentReplaced> onReplaced;

    public DegreeProcedureUseCase(UserRepository users, DegreeProcedureRepository procedures,
            Consumer<Opened> onOpened, Consumer<DocumentReplaced> onReplaced) {
        this.users = users;
        this.procedures = procedures;
        this.onOpened = onOpened;
        this.onReplaced = onReplaced;
    }

    public void open(ReceivedEmail email) {
        User requester = requireSecretary(email.getFrom(), "abrir trámites de titulación");
        List<String> args = email.parseArgs();
        User candidate = User.builder()
                .withRole(Role.STUDENT)
                .withFirstName(args.get(0))
                .withLastName(args.get(1))
                .withSecondLastName(args.get(2))
                .withCi(args.get(3))
                .withUagrmCode(args.get(4))
                .withEmailAddress(args.get(5))
                .withPhoneNumber(args.get(6))
                .signUp(requester);
        Map<InitialDocument, File> documents = initialDocuments(email.getAttachments());
        var existingId = procedures.lockAccountByEmail(candidate.getEmailAddress());
        boolean created = existingId.isEmpty();
        User student = created ? createStudent(candidate) : requireStudent(existingId.orElseThrow(), candidate);
        long id = procedures.open(student.getId(), requester.getId(), documents)
                .orElseThrow(() -> invalid("registro", "El estudiante ya tiene un trámite de titulación en curso."));
        onOpened.accept(new Opened(requester.getEmailAddress(), student.getEmailAddress(),
                student.getFullName(), "TR-" + String.format(Locale.ROOT, "%03d", id), created));
    }

    public void replace(ReceivedEmail email) {
        User requester = requireSecretary(email.getFrom(), "reemplazar documentos iniciales");
        String reference = email.parseArgs().getFirst();
        long id = parseReference(reference);
        File file = email.getAttachments().getFirst();
        InitialDocument type = InitialDocument.classify(file);
        if (!procedures.replace(id, requester.getId(), type, file)) {
            throw invalid("referencia", "No existe un trámite en curso con esa referencia.");
        }
        onReplaced.accept(new DocumentReplaced(requester.getEmailAddress(), reference, type.label()));
    }

    private User requireSecretary(String email, String action) {
        User requester = users.getByEmail(email).orElseThrow(() -> UserNotAuthorizedException.notRegistered(email));
        if (!requester.isActive()) {
            throw UserNotAuthorizedException.disabled(requester.getEmailAddress());
        }
        if (requester.getRole() != Role.ADMINISTRATIVE) {
            throw new UserNotAuthorizedException(requester.getEmailAddress(), action);
        }
        return requester;
    }

    private User createStudent(User candidate) {
        if (procedures.isStudentCodeTaken(candidate.getUagrmCode())) {
            throw incompatibleAccount();
        }
        users.insert(candidate);
        return candidate;
    }

    private User requireStudent(UUID id, User candidate) {
        User student = users.getById(id).orElseThrow(DegreeProcedureUseCase::incompatibleAccount);
        if (student.getRole() != Role.STUDENT || !candidate.getUagrmCode().equals(student.getUagrmCode())) {
            throw incompatibleAccount();
        }
        if (!student.isActive()) {
            throw invalid("cuenta", "La cuenta del estudiante está desactivada.");
        }
        return student;
    }

    private static Map<InitialDocument, File> initialDocuments(List<File> attachments) {
        Map<InitialDocument, File> documents = new EnumMap<>(InitialDocument.class);
        for (File file : attachments) {
            InitialDocument type = InitialDocument.classify(file);
            if (documents.putIfAbsent(type, file) != null) {
                throw InitialDocument.invalid("Se recibió más de un documento de tipo " + type.label() + ".");
            }
        }
        for (InitialDocument type : InitialDocument.values()) {
            if (!documents.containsKey(type)) {
                throw InitialDocument.invalid("Falta el documento " + type.label() + ".");
            }
        }
        return documents;
    }

    private static long parseReference(String reference) {
        if (reference.matches("TR-[0-9]+")) {
            try {
                return Long.parseLong(reference.substring(3));
            } catch (NumberFormatException ignored) {
                // An out-of-range reference has the same answer as any invalid reference.
            }
        }
        throw invalid("referencia", "Ingrese la referencia del trámite, por ejemplo TR-001.");
    }

    private static DomainValidationException incompatibleAccount() {
        return invalid("cuenta", "No se pudo abrir el trámite. Verifique el correo y el registro del estudiante.");
    }

    private static DomainValidationException invalid(String field, String message) {
        return new DomainValidationException(Map.of(field, message));
    }

    public record Opened(String requester, String student, String studentName, String reference, boolean created) {}

    public record DocumentReplaced(String requester, String reference, String documentLabel) {}
}
