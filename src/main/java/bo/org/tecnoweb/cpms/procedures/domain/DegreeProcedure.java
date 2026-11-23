package bo.org.tecnoweb.cpms.procedures.domain;

import bo.org.tecnoweb.cpms.common.AggregateRoot;
import bo.org.tecnoweb.cpms.common.DomainEvent;
import bo.org.tecnoweb.cpms.common.DomainValidationException;
import bo.org.tecnoweb.cpms.storage.domain.StoredFile;
import bo.org.tecnoweb.cpms.users.domain.User;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Graduation procedure and its initial documents (UC-002, FR-001, FR-002, FR-055). It is identified
 * by its {@code UUID}; its reference, {@code <semester>-<registration number>}, is the business
 * identifier used in replies and commands.
 */
public final class DegreeProcedure implements AggregateRoot {
    /** Field of the errors about the student's registration number. */
    public static final String CODE_FIELD = "registro";
    /** Field of the errors about the procedure reference. */
    public static final String REFERENCE_FIELD = "referencia";

    private static final Pattern REFERENCE = Pattern.compile("\\d{4}-[12]-\\d{1,10}");

    private UUID id;
    private String reference;
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
    public static DegreeProcedure rehydrate(UUID id, String reference, UUID studentId, UUID openedBy,
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

    /**
     * Opens the procedure of a student account with one valid PDF per document type (FR-002, FR-056).
     * The reference is built from the semester and the student's registration number. Each document
     * adopts a stored file already validated with {@link InitialDocument#requirePdf} (issue #9).
     *
     * @param student     the account of the student, found by its registration number
     * @param requester   the account that opens the procedure
     * @param semester    the current semester
     * @param attachments the four initial PDFs
     * @return this procedure
     * @throws DomainValidationException if the account is not an active student account, or a
     *                                   document is missing or repeated
     */
    public DegreeProcedure open(User student, User requester, Semester semester, List<StoredFile> attachments) {
        if (openedAt != null) {
            throw new IllegalStateException("Procedure already opened");
        }
        if (!student.isStudent()) {
            throw new DomainValidationException(Map.of(CODE_FIELD,
                    "La cuenta del registro " + student.getUagrmCode() + " no es de estudiante."));
        }
        if (!student.isActive()) {
            throw new DomainValidationException(Map.of("cuenta", "La cuenta del estudiante está desactivada."));
        }
        Map<InitialDocument, Document> validated = new EnumMap<>(InitialDocument.class);
        Instant now = Instant.now();
        for (StoredFile file : attachments) {
            InitialDocument type = InitialDocument.classify(file);
            if (validated.putIfAbsent(type, new Document(file.getId(), requester.getId(), now)) != null) {
                throw InitialDocument.invalid("Se recibió más de un documento de tipo " + type.label() + ".");
            }
        }
        for (InitialDocument type : InitialDocument.values()) {
            if (!validated.containsKey(type)) {
                throw InitialDocument.invalid("Falta el documento " + type.label() + ".");
            }
        }
        documents.putAll(validated);
        studentId = student.getId();
        reference = referenceOf(semester, student);
        openedAt = now;
        openedBy = requester.getId();
        events.add(new DegreeProcedureOpenedEvent(requester.getEmailAddress(), student.getEmailAddress(),
                student.getFullName(), reference, now));
        return this;
    }

    /**
     * Replaces one initial document while the procedure is open (FR-055). The document adopts the
     * stored file, already validated with {@link InitialDocument#requirePdf}; the use case links it
     * as the next version of the replaced one, which is kept (issue #9).
     */
    public void replaceInitialDocument(User requester, StoredFile file) {
        if (openedAt == null || closedAt != null) {
            throw unavailable();
        }
        InitialDocument type = InitialDocument.classify(file);
        Instant now = Instant.now();
        documents.put(type, new Document(file.getId(), requester.getId(), now));
        events.add(new DegreeProcedureDocumentReplacedEvent(requester.getEmailAddress(), reference, type.label(), now));
    }

    /** Reports that the given reference has no ongoing procedure. */
    public static DomainValidationException unavailable() {
        return new DomainValidationException(Map.of(REFERENCE_FIELD, "No existe un trámite en curso con esa referencia."));
    }

    /**
     * Builds the reference of the procedure a student opens in a semester (UC-002).
     *
     * @param semester the semester of the opening
     * @param student  the student account
     * @return the reference, for example {@code 2026-2-218001234}
     */
    public static String referenceOf(Semester semester, User student) {
        return semester + "-" + student.getUagrmCode();
    }

    /**
     * Checks a procedure reference written in an email command (UC-002, UC-003).
     *
     * @param reference the reference, {@code <year>-<period>-<registration number>}
     * @return the reference without surrounding spaces
     * @throws DomainValidationException if it does not have that form
     */
    public static String parseReference(String reference) {
        String value = reference == null ? "" : reference.strip();
        if (!REFERENCE.matcher(value).matches()) {
            throw new DomainValidationException(Map.of(REFERENCE_FIELD,
                    "Ingrese la referencia del trámite, por ejemplo 2026-2-218001234."));
        }
        return value;
    }

    /** Returns the aggregate UUID. */
    @Override
    public UUID getId() {
        return id;
    }

    /** Returns the reference, {@code <semester>-<registration number>}, once opened. */
    public String getReference() {
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

    /**
     * Returns the current document of a type.
     *
     * @param type the document type
     * @return the document, whose stored file is the current version
     * @throws IllegalStateException if the procedure has no document of that type
     */
    public Document documentOf(InitialDocument type) {
        Document document = documents.get(type);
        if (document == null) {
            throw new IllegalStateException("Procedure " + id + " has no document " + type);
        }
        return document;
    }

    /**
     * Stored file and audit metadata of an initial document (FR-002, FR-055). The name, type and
     * content of the PDF are kept by the file storage (issue #9).
     *
     * @param storedFileId the stored file of the current version
     * @param updatedBy    the account that registered or replaced it
     * @param updatedAt    when it was registered or replaced
     */
    public record Document(UUID storedFileId, UUID updatedBy, Instant updatedAt) {}
}
