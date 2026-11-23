package bo.org.tecnoweb.cpms.procedures.infrastructure.persistence;

import bo.org.tecnoweb.cpms.procedures.domain.DegreeProcedure.Document;
import bo.org.tecnoweb.cpms.procedures.domain.InitialDocument;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * Initial document of a procedure: the reference to its stored PDF and its audit metadata (UC-002,
 * FR-002, FR-055). The PDF itself is kept by the file storage (issue #9).
 */
@Entity
@Table(name = "initial_document")
class InitialDocumentEntity {
    @Id
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "procedure_id")
    private DegreeProcedureEntity procedure;
    @Enumerated(EnumType.STRING)
    @Column(name = "document_type")
    private InitialDocument type;
    @Column(name = "stored_file_id", nullable = false, unique = true)
    private UUID storedFileId;
    private UUID updatedBy;
    private Instant updatedAt;

    /** Constructor used by JPA. */
    protected InitialDocumentEntity() {}

    /** Creates a document for the specified procedure and type. */
    InitialDocumentEntity(DegreeProcedureEntity procedure, InitialDocument type) {
        this.id = UUID.randomUUID();
        this.procedure = procedure;
        this.type = type;
    }

    /** Updates the stored file and audit metadata (FR-055). */
    void update(Document document) {
        storedFileId = document.storedFileId();
        updatedBy = document.updatedBy();
        updatedAt = document.updatedAt();
    }

    /** Rehydrates the domain document. */
    Document toDomain() {
        return new Document(storedFileId, updatedBy, updatedAt);
    }
}
