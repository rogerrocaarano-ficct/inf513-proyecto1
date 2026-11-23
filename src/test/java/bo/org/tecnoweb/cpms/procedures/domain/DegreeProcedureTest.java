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

        assertThat(procedure.getDocuments()).hasSize(4);
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

    /** An invalid attachment set leaves the aggregate untouched (FR-002). */
    @Test
    void rejectsDuplicateDocumentWithoutPartialOpening() {
        var procedure = DegreeProcedure.builder();

        assertThatThrownBy(() -> procedure.open(student(), secretary(), SEMESTER,
                List.of(pdf("carta.pdf"), pdf("ci.pdf"), pdf("ci_copia.pdf"), pdf("cv.pdf"))))
                .isInstanceOf(DomainValidationException.class);
        assertThat(procedure.getDocuments()).isEmpty();
        assertThat(procedure.getReference()).isNull();
        assertThat(procedure.pullEvents()).isEmpty();
    }

    /** The replacement updates only the selected type and rejects closed procedures (FR-055). */
    @Test
    void replacesOnlyOneDocumentAndRejectsClosedProcedure() {
        var student = student();
        var secretary = secretary();
        var opened = DegreeProcedure.builder().open(student, secretary, SEMESTER, documents());
        var persisted = DegreeProcedure.rehydrate(opened.getId(), REFERENCE, student.getId(), secretary.getId(),
                opened.getOpenedAt(), null, opened.getDocuments());

        var corrected = pdf("carta_corregida.pdf");
        persisted.replaceInitialDocument(secretary, corrected);

        assertThat(persisted.getDocuments().get(InitialDocument.LETTER).storedFileId()).isEqualTo(corrected.getId());
        assertThat(persisted.documentOf(InitialDocument.RESUME)).isEqualTo(opened.documentOf(InitialDocument.RESUME));
        assertThat(persisted.pullEvents()).singleElement().isInstanceOfSatisfying(
                DegreeProcedureDocumentReplacedEvent.class, event -> assertThat(event.reference()).isEqualTo(REFERENCE));

        var closed = DegreeProcedure.rehydrate(opened.getId(), REFERENCE, student.getId(), secretary.getId(),
                opened.getOpenedAt(), Instant.now(), opened.getDocuments());
        assertThatThrownBy(() -> closed.replaceInitialDocument(secretary, pdf("carta_corregida.pdf")))
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
