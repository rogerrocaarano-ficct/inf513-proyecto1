package grupo03sc.mailapp.graduationprocesses.domain;

import grupo03sc.mailapp.audit.application.FieldChange;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

/**
 * The graduation project of a process (UC-004). Its document evolves: first the project profile, then
 * each version of the project document. Only the current document is kept here; every delivery
 * records its data in the change log, which {@link ProjectHistory} reads back, and the files of
 * earlier documents are never deleted (FR-010).
 */
@Entity
@Table(name = "graduation_projects")
public class GraduationProject {

    /** CON-004: at most 25 MB per file. */
    public static final long MAX_SIZE_BYTES = 26_214_400;

    /** Names of the data recorded in the change log for each delivery, read back by ProjectHistory. */
    public static final String GRADUATION_PROCESS_ID = "graduationProcessId";
    public static final String DOCUMENT_KIND = "documentKind";
    public static final String TITLE = "title";
    public static final String FILE_NAME = "fileName";
    public static final String STORAGE_KEY = "storageKey";
    public static final String VERSION_NUMBER = "versionNumber";
    public static final String GENERAL_COMMENT = "generalComment";
    public static final String STATUS = "status";

    /** Generic types some mail clients declare for any file; the extension decides then. */
    private static final Set<String> GENERIC_TYPES = Set.of("application/octet-stream", "binary/octet-stream");

    private static final Set<ProjectStatus> NEW_PROFILE_ALLOWED =
            EnumSet.of(ProjectStatus.PROFILE_OBSERVED, ProjectStatus.PROFILE_REJECTED);

    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    private UUID id;

    @Column(name = "graduation_process_id", nullable = false, updatable = false)
    private UUID graduationProcessId;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_kind", nullable = false)
    private ProjectDocumentKind documentKind;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "size_bytes", nullable = false)
    private int sizeBytes;

    @Column(name = "storage_key", nullable = false)
    private String storageKey;

    @Column(name = "version_number")
    private Integer versionNumber;

    @Column(name = "general_comment")
    private String generalComment;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectStatus status;

    @Version
    private long version;

    protected GraduationProject() {
    }

    /**
     * Rejects a file that is not a PDF, or that is empty or larger than 25 MB (CON-004). The
     * extension decides the format; a declared content type must be PDF or a generic one.
     */
    public static void checkFile(DocumentFile file) {
        String name = file.fileName() == null ? "(sin nombre)" : file.fileName();
        String declared = file.contentType() == null ? null : file.contentType().toLowerCase(Locale.ROOT);
        if (!"pdf".equals(extension(file.fileName()))
                || declared != null && !declared.equals("application/pdf") && !GENERIC_TYPES.contains(declared)) {
            throw new GraduationRuleViolationException(GraduationRejectionReason.INVALID_FILE, name + " debe ser un PDF");
        }
        if (file.content() == null || file.content().length == 0) {
            throw new GraduationRuleViolationException(GraduationRejectionReason.INVALID_FILE, name + " está vacío");
        }
        if (file.content().length > MAX_SIZE_BYTES) {
            throw new GraduationRuleViolationException(GraduationRejectionReason.INVALID_FILE,
                    name + " supera el tamaño máximo de 25 MB");
        }
    }

    /** The project of a process, born with its first profile pending evaluation (FR-004). */
    public static GraduationProject createWithProfile(UUID graduationProcessId, String title, DocumentFile file,
            String storageKey, Instant now) {
        GraduationProject project = new GraduationProject();
        project.graduationProcessId = graduationProcessId;
        project.setProfile(title, file, storageKey, now);
        return project;
    }

    /**
     * Replaces the current profile with a new one, only when the current one was observed or rejected
     * (FR-023, FR-058), and returns the recorded data.
     */
    public List<FieldChange> submitProfile(String title, DocumentFile file, String storageKey, Instant now) {
        checkProfileAllowed();
        Snapshot before = snapshot();
        setProfile(title, file, storageKey, now);
        return before.changesTo(snapshot());
    }

    /** Rejects a new profile unless the current one was observed or rejected (FR-058). */
    public void checkProfileAllowed() {
        if (documentKind != ProjectDocumentKind.PROJECT_PROFILE) {
            throw new GraduationRuleViolationException(GraduationRejectionReason.PROFILE_NOT_ALLOWED,
                    "ya entregó el documento del proyecto");
        }
        if (!NEW_PROFILE_ALLOWED.contains(status)) {
            throw new GraduationRuleViolationException(GraduationRejectionReason.PROFILE_NOT_ALLOWED,
                    "el perfil de proyecto vigente está " + status.label());
        }
    }

    /**
     * Rejects a new version unless the profile has its approval, the tutor or the tribunal observed the
     * current version, or the current version is approved and so is the pre-defense (FR-040, FR-025).
     */
    public void checkVersionAllowed(boolean predefenseApproved) {
        if (documentKind == ProjectDocumentKind.PROJECT_PROFILE) {
            if (status != ProjectStatus.PROFILE_APPROVED) {
                throw new GraduationRuleViolationException(GraduationRejectionReason.VERSION_PREREQUISITES,
                        "el perfil de proyecto todavía no tiene visto bueno (está " + status.label() + ")");
            }
            return;
        }
        switch (status) {
            case TUTOR_OBSERVED, TRIBUNAL_OBSERVED -> {
            }
            case APPROVED -> {
                if (documentKind == ProjectDocumentKind.FINAL_DEFENSE_VERSION) {
                    throw new GraduationRuleViolationException(GraduationRejectionReason.VERSION_NOT_ALLOWED,
                            "la versión para la defensa final ya está aprobada");
                }
                if (!predefenseApproved) {
                    throw new GraduationRuleViolationException(GraduationRejectionReason.VERSION_NOT_ALLOWED,
                            "la versión " + versionNumber + " está aprobada y la predefensa todavía no está aprobada");
                }
            }
            default -> throw new GraduationRuleViolationException(GraduationRejectionReason.VERSION_NOT_ALLOWED,
                    "la versión " + versionNumber + " todavía está " + status.label());
        }
    }

    /**
     * Registers a new version with the next number and the general comment, in review by the tutor; after
     * the approved pre-defense it is the version for the final defense. Returns the recorded data.
     */
    public List<FieldChange> submitVersion(String generalComment, DocumentFile file, String storageKey,
            boolean predefenseApproved, Instant now) {
        checkVersionAllowed(predefenseApproved);
        Snapshot before = snapshot();
        this.documentKind = predefenseApproved ? ProjectDocumentKind.FINAL_DEFENSE_VERSION
                : ProjectDocumentKind.PROJECT_DOCUMENT;
        this.versionNumber = versionNumber == null ? 1 : versionNumber + 1;
        this.generalComment = generalComment;
        this.status = ProjectStatus.TUTOR_REVIEW;
        setFile(file, storageKey, now);
        return before.changesTo(snapshot());
    }

    /** The initial data of the project, for the change log. */
    public List<FieldChange> describe() {
        List<FieldChange> changes = new ArrayList<>();
        changes.add(FieldChange.created(GRADUATION_PROCESS_ID, graduationProcessId));
        changes.addAll(Snapshot.EMPTY.changesTo(snapshot()));
        return changes;
    }

    private void setProfile(String title, DocumentFile file, String storageKey, Instant now) {
        this.title = title;
        this.documentKind = ProjectDocumentKind.PROJECT_PROFILE;
        this.versionNumber = null;
        this.generalComment = null;
        this.status = ProjectStatus.PROFILE_PENDING;
        setFile(file, storageKey, now);
    }

    private void setFile(DocumentFile file, String storageKey, Instant now) {
        this.fileName = file.fileName();
        this.sizeBytes = file.content().length;
        this.storageKey = storageKey;
        this.submittedAt = now;
    }

    private Snapshot snapshot() {
        return new Snapshot(documentKind, title, fileName, storageKey, versionNumber, generalComment, status);
    }

    /**
     * The seven data each delivery records, even when unchanged, so that ProjectHistory builds each
     * document from its own entries.
     */
    private record Snapshot(ProjectDocumentKind documentKind, String title, String fileName, String storageKey,
            Integer versionNumber, String generalComment, ProjectStatus status) {

        static final Snapshot EMPTY = new Snapshot(null, null, null, null, null, null, null);

        List<FieldChange> changesTo(Snapshot after) {
            return List.of(
                    change(DOCUMENT_KIND, documentKind, after.documentKind),
                    change(TITLE, title, after.title),
                    change(FILE_NAME, fileName, after.fileName),
                    change(STORAGE_KEY, storageKey, after.storageKey),
                    change(VERSION_NUMBER, versionNumber, after.versionNumber),
                    change(GENERAL_COMMENT, generalComment, after.generalComment),
                    change(STATUS, status, after.status));
        }

        private static FieldChange change(String field, Object before, Object after) {
            return new FieldChange(field, Objects.toString(before, null), Objects.toString(after, null));
        }
    }

    private static String extension(String fileName) {
        if (fileName == null) {
            return "";
        }
        int dot = fileName.lastIndexOf('.');
        return dot < 0 ? "" : fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    public UUID getId() {
        return id;
    }

    public UUID getGraduationProcessId() {
        return graduationProcessId;
    }

    public String getTitle() {
        return title;
    }

    public ProjectDocumentKind getDocumentKind() {
        return documentKind;
    }

    public String getFileName() {
        return fileName;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public Integer getVersionNumber() {
        return versionNumber;
    }

    public String getGeneralComment() {
        return generalComment;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public ProjectStatus getStatus() {
        return status;
    }
}
