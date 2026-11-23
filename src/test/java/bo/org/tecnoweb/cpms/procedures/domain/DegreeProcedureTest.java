package bo.org.tecnoweb.cpms.procedures.domain;

import static bo.org.tecnoweb.cpms.procedures.ProcedureFixtures.*;
import static org.assertj.core.api.Assertions.*;

import bo.org.tecnoweb.cpms.common.DomainValidationException;
import bo.org.tecnoweb.cpms.users.domain.Role;
import bo.org.tecnoweb.cpms.users.domain.User;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

/** Tests the opening and replacement rules of UC-002. */
class DegreeProcedureTest {
    /** Checks complete opening, its reference and its domain event (FR-002, FR-054). */
    @Test
    void opensWithFourDocumentsAndTheReferenceOfSemesterAndCode() {
        var student = student();
        var procedure = DegreeProcedure.builder().open(student, secretary(), SEMESTER, documents());

        assertThat(procedure.getRequirements()).hasSize(4);
        assertThat(procedure.getStudentId()).isEqualTo(student.getId());
        assertThat(procedure.getReference()).isEqualTo(REFERENCE);
        assertThat(procedure.pullEvents()).singleElement().isInstanceOfSatisfying(DegreeProcedureOpenedEvent.class,
                event -> {
                    assertThat(event.reference()).isEqualTo(REFERENCE);
                    assertThat(event.student()).isEqualTo(student.getEmailAddress());
                });
        assertThat(procedure.pullEvents()).isEmpty();
    }

    /** An account of another role is rejected without comparing roles in the procedure (FR-056). */
    @Test
    void rejectsAnAccountThatIsNotAStudent() {
        var professor = User.builder().withRole(Role.PROFESSOR).withEmailAddress("luis@cpms.test")
                .withFirstName("Luis").withLastName("Vaca").withUagrmCode("777");
        var procedure = DegreeProcedure.builder();

        assertThatThrownBy(() -> procedure.open(professor, secretary(), SEMESTER, documents()))
                .isInstanceOfSatisfying(DomainValidationException.class, e -> assertThat(e.getErrors())
                        .containsExactly(Map.entry(DegreeProcedure.CODE_FIELD, "La cuenta del registro 777 no es de estudiante.")));
        assertThat(procedure.pullEvents()).isEmpty();
    }

    /** A disabled student account is rejected (FR-056). */
    @Test
    void rejectsADisabledStudent() {
        var disabled = User.rehydrate().withId(UUID.randomUUID()).withRole(Role.STUDENT)
                .withEmailAddress("ana@cpms.test").withFirstName("Ana").withLastName("Pérez")
                .withUagrmCode("123456").withActive(false);

        assertThatThrownBy(() -> DegreeProcedure.builder().open(disabled, secretary(), SEMESTER, documents()))
                .isInstanceOfSatisfying(DomainValidationException.class,
                        e -> assertThat(e.getErrors()).containsOnlyKeys("cuenta"));
    }

    /** A repeated requirement leaves the aggregate untouched (FR-002). */
    @Test
    void rejectsARepeatedRequirementWithoutPartialOpening() {
        var procedure = DegreeProcedure.builder();

        assertThatThrownBy(() -> procedure.open(student(), secretary(), SEMESTER,
                List.of(pdf("carta.pdf"), pdf("ci.pdf"), pdf("ci_copia.pdf"), pdf("cv.pdf"))))
                .isInstanceOfSatisfying(DomainValidationException.class, e -> assertThat(e.getErrors())
                        .containsExactly(Map.entry(PdfDocument.FIELD,
                                "Se recibió más de un documento para el requisito Cédula de identidad.")));
        assertThat(procedure.getRequirements()).isEmpty();
        assertThat(procedure.getReference()).isNull();
        assertThat(procedure.pullEvents()).isEmpty();
    }

    /** Every opening requirement needs its document (FR-002). */
    @Test
    void rejectsAMissingRequirement() {
        assertThatThrownBy(() -> DegreeProcedure.builder().open(student(), secretary(), SEMESTER,
                List.of(pdf("carta.pdf"), pdf("cv.pdf"), pdf("ci.pdf"))))
                .isInstanceOfSatisfying(DomainValidationException.class, e -> assertThat(e.getErrors())
                        .containsExactly(Map.entry(PdfDocument.FIELD, "Falta el requisito Boleta de inscripción.")));
    }

    /** The replacement updates only the selected type and rejects closed procedures (FR-055). */
    @Test
    void replacesOnlyOneDocumentAndRejectsClosedProcedure() {
        var student = student();
        var secretary = secretary();
        var opened = DegreeProcedure.builder().open(student, secretary, SEMESTER, documents());
        var persisted = DegreeProcedure.rehydrate(opened.getId(), REFERENCE, student.getId(), secretary.getId(),
                opened.getOpenedAt(), null, opened.getRequirements());

        var corrected = pdf("carta_corregida.pdf");
        persisted.replaceRequirement(secretary, corrected);

        assertThat(persisted.getRequirements().get(OpeningRequirement.LETTER)).isEqualTo(corrected.getId());
        assertThat(persisted.requirementOf(OpeningRequirement.RESUME)).isEqualTo(opened.requirementOf(OpeningRequirement.RESUME));
        assertThat(persisted.pullEvents()).singleElement().isInstanceOfSatisfying(
                DegreeProcedureRequirementReplacedEvent.class, event -> assertThat(event.reference()).isEqualTo(REFERENCE));

        var closed = DegreeProcedure.rehydrate(opened.getId(), REFERENCE, student.getId(), secretary.getId(),
                opened.getOpenedAt(), Instant.now(), opened.getRequirements());
        assertThatThrownBy(() -> closed.replaceRequirement(secretary, pdf("carta_corregida.pdf")))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    void parsesAValidReference() {
        assertThat(DegreeProcedure.parseReference(" 2026-2-218001234 ")).isEqualTo("2026-2-218001234");
        assertThat(DegreeProcedure.parseReference("2027-1-1")).isEqualTo("2027-1-1");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"TR-001", "2026-3-218001234", "26-2-218001234", "2026-2-", "2026-2-12345678901",
            "2026-2-21800123A"})
    void rejectsAMalformedReference(String reference) {
        assertThatThrownBy(() -> DegreeProcedure.parseReference(reference))
                .isInstanceOfSatisfying(DomainValidationException.class, e -> assertThat(e.getErrors())
                        .containsExactly(Map.entry(DegreeProcedure.REFERENCE_FIELD,
                                "Ingrese la referencia del trámite, por ejemplo 2026-2-218001234.")));
    }
}
