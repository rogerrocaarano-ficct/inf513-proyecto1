package grupo03sc.mailapp.graduationprocesses.domain;

import grupo03sc.mailapp.audit.application.FieldChange;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

/**
 * An initial document of a graduation process record. Its file lives in Storage (AD-012), under
 * {@link #getStorageKey()}.
 */
@Entity
@Table(name = "initial_documents")
public class InitialDocument {

    /** CON-005: at most 1 MB per file. */
    public static final long MAX_SIZE_BYTES = 1_048_576;

    private static final Map<String, String> CONTENT_TYPES = Map.of(
            "png", "image/png", "jpg", "image/jpeg", "jpeg", "image/jpeg", "pdf", "application/pdf");

    /** Generic types some mail clients declare for any file; the extension decides then. */
    private static final Set<String> GENERIC_TYPES = Set.of("application/octet-stream", "binary/octet-stream");

    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InitialDocumentType type;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private int sizeBytes;

    @Column(name = "storage_key", nullable = false)
    private String storageKey;

    @Column(name = "uploaded_at", nullable = false)
    private Instant uploadedAt;

    protected InitialDocument() {
    }

    /**
     * Rejects a file that is not a PNG or JPG/JPEG image or a PDF, or that is empty or larger than
     * 1 MB (CON-005). The extension decides the format; a declared content type must agree with it,
     * unless it is a generic one.
     */
    public static void check(DocumentFile file) {
        String name = file.fileName() == null ? "(sin nombre)" : file.fileName();
        String expected = CONTENT_TYPES.get(extension(file.fileName()));
        String declared = file.contentType() == null ? null : file.contentType().toLowerCase(Locale.ROOT);
        if (expected == null || declared != null && !declared.equals(expected) && !GENERIC_TYPES.contains(declared)) {
            throw new GraduationRuleViolationException(GraduationRejectionReason.INVALID_DOCUMENT,
                    name + " debe ser una imagen PNG o JPG/JPEG, o un PDF");
        }
        if (file.content() == null || file.content().length == 0) {
            throw new GraduationRuleViolationException(GraduationRejectionReason.INVALID_DOCUMENT, name + " está vacío");
        }
        if (file.content().length > MAX_SIZE_BYTES) {
            throw new GraduationRuleViolationException(GraduationRejectionReason.INVALID_DOCUMENT,
                    name + " supera el tamaño máximo de 1 MB");
        }
    }

    /** A document of a file already checked and stored under the storage key. */
    public static InitialDocument of(InitialDocumentType type, DocumentFile file, String storageKey, Instant now) {
        InitialDocument document = new InitialDocument();
        document.type = type;
        document.setFile(file, storageKey, now);
        return document;
    }

    /** Replaces the file in place and returns the change; the old storage key must be read before. */
    FieldChange replaceFile(DocumentFile file, String storageKey, Instant now) {
        String oldName = fileName;
        setFile(file, storageKey, now);
        return new FieldChange(property(type), oldName, fileName);
    }

    static String property(InitialDocumentType type) {
        return "initialDocuments." + type.name();
    }

    private void setFile(DocumentFile file, String storageKey, Instant now) {
        this.fileName = file.fileName();
        this.contentType = CONTENT_TYPES.get(extension(file.fileName()));
        this.sizeBytes = file.content().length;
        this.storageKey = storageKey;
        this.uploadedAt = now;
    }

    private static String extension(String fileName) {
        if (fileName == null) {
            return "";
        }
        int dot = fileName.lastIndexOf('.');
        return dot < 0 ? "" : fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    public InitialDocumentType getType() {
        return type;
    }

    public String getFileName() {
        return fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public String getStorageKey() {
        return storageKey;
    }
}
