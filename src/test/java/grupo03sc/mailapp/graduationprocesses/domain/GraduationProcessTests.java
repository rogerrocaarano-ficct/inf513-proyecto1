package grupo03sc.mailapp.graduationprocesses.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import grupo03sc.mailapp.audit.application.FieldChange;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GraduationProcessTests {

    private static final Instant NOW = Instant.parse("2026-11-03T12:00:00Z");
    private final UUID student = UUID.randomUUID();

    @Test
    void opensInProgressAtTheOpeningStageWithTheFourDocuments() {
        GraduationProcess process = GraduationProcess.open(student, fourDocuments(), NOW);

        assertThat(process.getStatus()).isEqualTo(GraduationProcessStatus.IN_PROGRESS);
        assertThat(process.getStage()).isEqualTo(GraduationStage.OPENED);
        assertThat(process.getInitialDocuments()).hasSize(4);
        assertThat(process.describe()).contains(
                FieldChange.created("status", "IN_PROGRESS"),
                FieldChange.created("stage", "OPENED"),
                FieldChange.created("studentAccountId", student),
                FieldChange.created("initialDocuments.ENROLLMENT_SLIP", "ENROLLMENT_SLIP.pdf"));
    }

    @Test
    void doesNotOpenWithoutTheFourTypes() {
        List<InitialDocument> three = fourDocuments().subList(0, 3);

        assertThatThrownBy(() -> GraduationProcess.open(student, three, NOW))
                .isInstanceOfSatisfying(GraduationRuleViolationException.class,
                        e -> assertThat(e.getReason()).isEqualTo(GraduationRejectionReason.MISSING_DOCUMENT));
    }

    @Test
    void replacesTheFileInPlaceAndReturnsTheReplacedKey() {
        GraduationProcess process = GraduationProcess.open(student, fourDocuments(), NOW);

        DocumentReplacement replacement = process.replaceInitialDocument(InitialDocumentType.ENROLLMENT_SLIP,
                new DocumentFile("boleta-nueva.pdf", "application/pdf", new byte[2]), "nueva", NOW);

        assertThat(replacement.replacedStorageKey()).isEqualTo("key-ENROLLMENT_SLIP");
        assertThat(replacement.change()).isEqualTo(
                new FieldChange("initialDocuments.ENROLLMENT_SLIP", "ENROLLMENT_SLIP.pdf", "boleta-nueva.pdf"));
        assertThat(process.getInitialDocuments()).hasSize(4)
                .filteredOn(d -> d.getType() == InitialDocumentType.ENROLLMENT_SLIP)
                .singleElement().extracting(InitialDocument::getStorageKey).isEqualTo("nueva");
    }

    @Test
    void closesOnlyWithTheFinalGrade() {
        GraduationProcess process = GraduationProcess.open(student, fourDocuments(), NOW);

        assertThatThrownBy(() -> process.close(NOW))
                .isInstanceOfSatisfying(GraduationRuleViolationException.class, e -> {
                    assertThat(e.getReason()).isEqualTo(GraduationRejectionReason.NO_FINAL_GRADE);
                    assertThat(e.getMessage()).contains("apertura");
                });
    }

    @Test
    void closingRegistersTheCopiesAndArchives() throws Exception {
        GraduationProcess process = GraduationProcess.open(student, fourDocuments(), NOW);
        setStage(process, GraduationStage.FINAL_GRADE_RECORDED);

        List<FieldChange> changes = process.close(NOW);

        assertThat(process.getStatus()).isEqualTo(GraduationProcessStatus.CLOSED);
        assertThat(changes).containsExactly(
                new FieldChange("status", "IN_PROGRESS", "CLOSED"),
                FieldChange.created("physicalCopiesReceivedAt", NOW),
                FieldChange.created("closedAt", NOW));
        assertThatThrownBy(() -> process.replaceInitialDocument(InitialDocumentType.REQUEST_LETTER,
                new DocumentFile("carta.pdf", "application/pdf", new byte[1]), "x", NOW))
                .isInstanceOfSatisfying(GraduationRuleViolationException.class,
                        e -> assertThat(e.getReason()).isEqualTo(GraduationRejectionReason.PROCESS_NOT_FOUND));
    }

    @Test
    void theStagesAreDeclaredInTheOrderOfTheProcess() {
        assertThat(GraduationStage.values()).containsExactly(GraduationStage.OPENED,
                GraduationStage.PREDEFENSE_APPROVED, GraduationStage.FINAL_GRADE_RECORDED);
    }

    @Test
    void thePredefenseIsApprovedFromItsStageOn() throws Exception {
        GraduationProcess process = GraduationProcess.open(student, fourDocuments(), NOW);
        assertThat(process.isPredefenseApproved()).isFalse();

        setStage(process, GraduationStage.PREDEFENSE_APPROVED);
        assertThat(process.isPredefenseApproved()).isTrue();

        setStage(process, GraduationStage.FINAL_GRADE_RECORDED);
        assertThat(process.isPredefenseApproved()).isTrue();
    }

    private static List<InitialDocument> fourDocuments() {
        return Arrays.stream(InitialDocumentType.values())
                .map(type -> InitialDocument.of(type, new DocumentFile(type.name() + ".pdf", "application/pdf",
                        new byte[1]), "key-" + type.name(), NOW))
                .toList();
    }

    /** UC-006 sets the final grade stage; until then the tests set it directly. */
    private static void setStage(GraduationProcess process, GraduationStage stage) throws Exception {
        Field field = GraduationProcess.class.getDeclaredField("stage");
        field.setAccessible(true);
        field.set(process, stage);
    }
}
