package bo.org.tecnoweb.cpms.procedures.infrastructure.persistence;

import bo.org.tecnoweb.cpms.procedures.domain.DegreeProcedure;
import bo.org.tecnoweb.cpms.procedures.domain.OpeningRequirement;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.MapKeyEnumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

/**
 * JPA representation of a graduation procedure, keyed by its UUID (UC-002). Its opening requirements
 * are values of the procedure: one row per requirement with the stored file of its current version.
 */
@Entity
@Table(name = "degree_procedure")
class DegreeProcedureEntity {
    @Id
    private UUID id;
    @Column(nullable = false, unique = true)
    private String reference;
    private UUID studentId;
    private UUID openedBy;
    private Instant openedAt;
    private Instant closedAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "degree_procedure_requirement", joinColumns = @JoinColumn(name = "procedure_id"))
    @MapKeyEnumerated(EnumType.STRING)
    @MapKeyColumn(name = "requirement")
    @Column(name = "stored_file_id", nullable = false, unique = true)
    private Map<OpeningRequirement, UUID> requirements = new EnumMap<>(OpeningRequirement.class);

    /** Constructor used by JPA. */
    protected DegreeProcedureEntity() {}

    /** Maps a new aggregate for insertion. */
    DegreeProcedureEntity(DegreeProcedure procedure) {
        id = procedure.getId();
        reference = procedure.getReference();
        studentId = procedure.getStudentId();
        openedBy = procedure.getOpenedBy();
        openedAt = procedure.getOpenedAt();
        updateRequirements(procedure);
    }

    /** Maps the aggregate and the stored files of its requirements to the domain. */
    DegreeProcedure toDomain() {
        return DegreeProcedure.rehydrate(id, reference, studentId, openedBy, openedAt, closedAt, requirements);
    }

    /** Copies the stored file of the current version of each requirement (FR-055). */
    void updateRequirements(DegreeProcedure procedure) {
        requirements.putAll(procedure.getRequirements());
    }
}
