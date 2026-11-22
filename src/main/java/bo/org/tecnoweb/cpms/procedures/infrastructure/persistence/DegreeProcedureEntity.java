package bo.org.tecnoweb.cpms.procedures.infrastructure.persistence;

import bo.org.tecnoweb.cpms.procedures.domain.DegreeProcedure;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProcedure.Document;
import bo.org.tecnoweb.cpms.procedures.domain.InitialDocument;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MapKey;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

/** JPA representation of a graduation procedure (UC-002). */
@Entity
@Table(name = "degree_procedure")
class DegreeProcedureEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reference;
    @Column(nullable = false, unique = true)
    private UUID id;
    private UUID studentId;
    private UUID openedBy;
    private Instant openedAt;
    private Instant closedAt;

    @OneToMany(mappedBy = "procedure", cascade = CascadeType.ALL, orphanRemoval = true)
    @MapKey(name = "type")
    private Map<InitialDocument, InitialDocumentEntity> documents = new EnumMap<>(InitialDocument.class);

    /** Constructor used by JPA. */
    protected DegreeProcedureEntity() {}

    /** Maps a new aggregate for insertion. */
    DegreeProcedureEntity(DegreeProcedure procedure) {
        id = procedure.getId();
        studentId = procedure.getStudentId();
        openedBy = procedure.getOpenedBy();
        openedAt = procedure.getOpenedAt();
        updateDocuments(procedure);
    }

    /** Maps the aggregate and its files to the domain. */
    DegreeProcedure toDomain() {
        Map<InitialDocument, Document> files = new EnumMap<>(InitialDocument.class);
        documents.forEach((type, entity) -> files.put(type, entity.toDomain()));
        return DegreeProcedure.rehydrate(id, reference, studentId, openedBy, openedAt, closedAt, files);
    }

    /** Copies current file data while retaining each persisted document identity. */
    void updateDocuments(DegreeProcedure procedure) {
        procedure.getDocuments().forEach((type, document) -> documents
                .computeIfAbsent(type, key -> new InitialDocumentEntity(this, key)).update(document));
    }
}
