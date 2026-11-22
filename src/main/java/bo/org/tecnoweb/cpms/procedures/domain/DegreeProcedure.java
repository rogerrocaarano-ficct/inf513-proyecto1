package bo.org.tecnoweb.cpms.procedures.domain;

import bo.org.tecnoweb.cpms.common.AggregateRoot;
import bo.org.tecnoweb.cpms.common.DomainEvent;
import bo.org.tecnoweb.cpms.common.DomainValidationException;
import bo.org.tecnoweb.cpms.mail.domain.File;
import bo.org.tecnoweb.cpms.users.domain.Role;
import bo.org.tecnoweb.cpms.users.domain.User;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/** Graduation procedure and its initial documents (UC-002, FR-001, FR-002, FR-055). */
public final class DegreeProcedure implements AggregateRoot {
    private UUID id;
    private Long reference;
    private UUID studentId;
    private UUID openedBy;
    private Instant openedAt;
    private Instant closedAt;
    private final Map<InitialDocument, Document> documents = new EnumMap<>(InitialDocument.class);
    private final List<DomainEvent> events = new ArrayList<>();

    private DegreeProcedure() {}

    /** Begins a new procedure with a fresh identity. */
    public static DegreeProcedure builder() {
        var procedure = new DegreeProcedure();
        procedure.id = UUID.randomUUID();
        return procedure;
    }

    /** Restores a persisted procedure without raising events. */
    public static DegreeProcedure rehydrate(UUID id, long reference, UUID studentId, UUID openedBy,
            Instant openedAt, Instant closedAt, Map<InitialDocument, Document> documents) {
        var procedure = new DegreeProcedure();
        procedure.id = id;
        procedure.reference = reference;
        procedure.studentId = studentId;
        procedure.openedBy = openedBy;
        procedure.openedAt = openedAt;
        procedure.closedAt = closedAt;
        procedure.documents.putAll(documents);
        return procedure;
    }

    /** Assigns the student associated with the procedure. */
    public DegreeProcedure withStudent(User student) {
        this.studentId = student.getId();
        return this;
    }

    /** Opens a procedure with one valid PDF per document type (FR-002, FR-056). */
    public DegreeProcedure open(User student, User requester, List<File> attachments) {
        if (openedAt != null) {
            throw new IllegalStateException("Procedure already opened");
        }
        if (student.getRole() != Role.STUDENT || !student.isActive() || !student.getId().equals(studentId)) {
            throw new DomainValidationException(Map.of("cuenta", "La cuenta del estudiante no está habilitada."));
        }
        Map<InitialDocument, Document> validated = new EnumMap<>(InitialDocument.class);
        Instant now = Instant.now();
        for (File file : attachments) {
            InitialDocument type = InitialDocument.classify(file);
            if (validated.putIfAbsent(type, new Document(file.getName(), file.getData(), requester.getId(), now)) != null) {
                throw InitialDocument.invalid("Se recibió más de un documento de tipo " + type.label() + ".");
            }
        }
        for (InitialDocument type : InitialDocument.values()) {
            if (!validated.containsKey(type)) {
                throw InitialDocument.invalid("Falta el documento " + type.label() + ".");
            }
        }
        documents.putAll(validated);
        openedAt = now;
        openedBy = requester.getId();
        events.add(new DegreeProcedureOpenedEvent(requester.getEmailAddress(), student.getEmailAddress(),
                student.getFullName(), now));
        return this;
    }

    /** Replaces one initial document while the procedure is open (FR-055). */
    public void replaceInitialDocument(User requester, File file) {
        if (openedAt == null || closedAt != null) {
            throw unavailable();
        }
        InitialDocument type = InitialDocument.classify(file);
        Instant now = Instant.now();
        documents.put(type, new Document(file.getName(), file.getData(), requester.getId(), now));
        events.add(new DegreeProcedureDocumentReplacedEvent(requester.getEmailAddress(),
                referenceLabel(reference), type.label(), now));
    }

    /** Reports that the given reference has no ongoing procedure. */
    public static DomainValidationException unavailable() {
        return new DomainValidationException(Map.of("referencia", "No existe un trámite en curso con esa referencia."));
    }

    /** Validates that an existing account can be reused for the opening (FR-056). */
    public static User requireMatchingStudent(User existing, User candidate) {
        if (existing.getRole() != Role.STUDENT || !candidate.getUagrmCode().equals(existing.getUagrmCode())) {
            throw incompatibleAccount();
        }
        if (!existing.isActive()) {
            throw new DomainValidationException(Map.of("cuenta", "La cuenta del estudiante está desactivada."));
        }
        return existing;
    }

    /** Reports that the supplied account identifiers cannot be used together (FR-033, FR-056). */
    public static DomainValidationException incompatibleAccount() {
        return new DomainValidationException(Map.of("cuenta",
                "No se pudo abrir el trámite. Verifique el correo y el registro del estudiante."));
    }

    /** Formats a persisted reference for use in email commands. */
    public static String referenceLabel(long reference) {
        return "TR-" + String.format(Locale.ROOT, "%03d", reference);
    }

    /** Parses the public procedure reference used by the email commands (UC-002, UC-003). */
    public static long parseReference(String reference) {
        if (reference != null && reference.matches("TR-[0-9]+")) {
            try {
                return Long.parseLong(reference.substring(3));
            } catch (NumberFormatException ignored) {
                // Out-of-range references receive the same validation error.
            }
        }
        throw new DomainValidationException(Map.of("referencia", "Ingrese la referencia del trámite, por ejemplo TR-001."));
    }

    /** Returns the aggregate UUID. */
    @Override
    public UUID getId() {
        return id;
    }

    /** Returns the generated public reference, if persisted. */
    public Long getReference() {
        return reference;
    }

    /** Returns the student account identifier. */
    public UUID getStudentId() {
        return studentId;
    }

    /** Returns the opening requester identifier. */
    public UUID getOpenedBy() {
        return openedBy;
    }

    /** Returns when the procedure was opened. */
    public Instant getOpenedAt() {
        return openedAt;
    }

    /** Returns when the procedure was closed, or null while open. */
    public Instant getClosedAt() {
        return closedAt;
    }

    /** Returns the current document by type. */
    public Map<InitialDocument, Document> getDocuments() {
        return Map.copyOf(documents);
    }

    /** Returns and clears pending domain events. */
    @Override
    public List<DomainEvent> pullEvents() {
        List<DomainEvent> pending = List.copyOf(events);
        events.clear();
        return pending;
    }

    /** Content and audit metadata of an initial document (FR-002, FR-055). */
    public record Document(String filename, byte[] content, UUID updatedBy, Instant updatedAt) {}
}
