package grupo03sc.mailapp.graduationprocesses.domain;

import grupo03sc.mailapp.audit.application.FieldChange;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.hibernate.annotations.UuidGenerator;

/**
 * The graduation process of a student, which is also its record: its initial documents and its
 * archive on closing (UC-003). It keeps the student's user account id, without depending on Users.
 */
@Entity
@Table(name = "graduation_processes")
public class GraduationProcess {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    private UUID id;

    @Column(name = "student_account_id", nullable = false)
    private UUID studentAccountId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GraduationProcessStatus status = GraduationProcessStatus.IN_PROGRESS;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GraduationStage stage = GraduationStage.OPENED;

    @Column(name = "opened_at", nullable = false)
    private Instant openedAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    @Column(name = "physical_copies_received_at")
    private Instant physicalCopiesReceivedAt;

    @Column(name = "pending_reassignment", nullable = false)
    private boolean pendingReassignment;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "graduation_process_id", nullable = false)
    private List<InitialDocument> initialDocuments = new ArrayList<>();

    protected GraduationProcess() {
    }

    /** A process in progress, at the opening stage, with one document of each initial type. */
    public static GraduationProcess open(UUID studentAccountId, List<InitialDocument> documents, Instant now) {
        Set<InitialDocumentType> types = documents.stream().map(InitialDocument::getType)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(InitialDocumentType.class)));
        if (types.size() != documents.size() || types.size() != InitialDocumentType.values().length) {
            Set<InitialDocumentType> missing = EnumSet.allOf(InitialDocumentType.class);
            missing.removeAll(types);
            throw new GraduationRuleViolationException(GraduationRejectionReason.MISSING_DOCUMENT,
                    missing.stream().map(InitialDocumentType::label).collect(Collectors.joining(", ")));
        }
        GraduationProcess process = new GraduationProcess();
        process.studentAccountId = studentAccountId;
        process.openedAt = now;
        process.initialDocuments.addAll(documents);
        return process;
    }

    /** Replaces the file of the initial document of that type, while the process is in progress. */
    public DocumentReplacement replaceInitialDocument(InitialDocumentType type, DocumentFile file, String storageKey,
            Instant now) {
        checkInProgress();
        InitialDocument document = initialDocument(type);
        String replacedKey = document.getStorageKey();
        return new DocumentReplacement(document.replaceFile(file, storageKey, now), replacedKey);
    }

    /**
     * Registers the receipt of the two physical copies, closes the process and archives its record;
     * only after the final grade (FR-017, FR-018).
     */
    public List<FieldChange> close(Instant now) {
        checkInProgress();
        if (stage != GraduationStage.FINAL_GRADE_RECORDED) {
            throw new GraduationRuleViolationException(GraduationRejectionReason.NO_FINAL_GRADE,
                    "está en la etapa de " + stage.label());
        }
        status = GraduationProcessStatus.CLOSED;
        physicalCopiesReceivedAt = now;
        closedAt = now;
        return List.of(
                new FieldChange("status", GraduationProcessStatus.IN_PROGRESS.name(), status.name()),
                FieldChange.created("physicalCopiesReceivedAt", now),
                FieldChange.created("closedAt", now));
    }

    /** Leaves the process pending reassignment of a participant (FR-034). */
    public void markPendingReassignment() {
        pendingReassignment = true;
    }

    /** The initial data of the process, for the change log. */
    public List<FieldChange> describe() {
        List<FieldChange> changes = new ArrayList<>(List.of(
                FieldChange.created("status", status),
                FieldChange.created("stage", stage),
                FieldChange.created("studentAccountId", studentAccountId)));
        initialDocuments.stream()
                .sorted(Comparator.comparing(InitialDocument::getType))
                .forEach(document -> changes.add(
                        FieldChange.created(InitialDocument.property(document.getType()), document.getFileName())));
        return changes;
    }

    private InitialDocument initialDocument(InitialDocumentType type) {
        return initialDocuments.stream().filter(document -> document.getType() == type).findFirst()
                .orElseThrow(() -> new IllegalStateException("Process without " + type));
    }

    private void checkInProgress() {
        if (status != GraduationProcessStatus.IN_PROGRESS) {
            throw new GraduationRuleViolationException(GraduationRejectionReason.PROCESS_NOT_FOUND, "está cerrado");
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getStudentAccountId() {
        return studentAccountId;
    }

    public GraduationProcessStatus getStatus() {
        return status;
    }

    public GraduationStage getStage() {
        return stage;
    }

    public boolean isPendingReassignment() {
        return pendingReassignment;
    }

    public List<InitialDocument> getInitialDocuments() {
        return List.copyOf(initialDocuments);
    }
}
