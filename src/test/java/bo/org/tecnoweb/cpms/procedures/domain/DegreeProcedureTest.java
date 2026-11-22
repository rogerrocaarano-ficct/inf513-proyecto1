package bo.org.tecnoweb.cpms.procedures.domain;

import static bo.org.tecnoweb.cpms.procedures.ProcedureFixtures.*;
import static org.assertj.core.api.Assertions.*;

import bo.org.tecnoweb.cpms.common.DomainValidationException;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Tests the opening and replacement rules of UC-002. */
class DegreeProcedureTest {
    /** Checks complete opening and its domain event (FR-002, FR-054). */
    @Test
    void opensWithFourDocuments() {
        var student = student();
        var procedure = DegreeProcedure.builder().withStudent(student).open(student, secretary(), documents());

        assertThat(procedure.getDocuments()).hasSize(4);
        assertThat(procedure.pullEvents()).singleElement().isInstanceOf(DegreeProcedureOpenedEvent.class);
        assertThat(procedure.pullEvents()).isEmpty();
    }

    /** An invalid attachment set leaves the aggregate untouched (FR-002). */
    @Test
    void rejectsDuplicateDocumentWithoutPartialOpening() {
        var student = student();
        var procedure = DegreeProcedure.builder().withStudent(student);

        assertThatThrownBy(() -> procedure.open(student, secretary(),
                List.of(pdf("carta.pdf"), pdf("ci.pdf"), pdf("ci_copia.pdf"), pdf("cv.pdf"))))
                .isInstanceOf(DomainValidationException.class);
        assertThat(procedure.getDocuments()).isEmpty();
        assertThat(procedure.pullEvents()).isEmpty();
    }

    /** The replacement updates only the selected type and rejects closed procedures (FR-055). */
    @Test
    void replacesOnlyOneDocumentAndRejectsClosedProcedure() {
        var student = student();
        var secretary = secretary();
        var opened = DegreeProcedure.builder().withStudent(student).open(student, secretary, documents());
        var persisted = DegreeProcedure.rehydrate(opened.getId(), 1, student.getId(), secretary.getId(),
                opened.getOpenedAt(), null, opened.getDocuments());

        var corrected = pdf("carta_corregida.pdf");
        persisted.replaceInitialDocument(secretary, corrected);

        assertThat(persisted.getDocuments().get(InitialDocument.LETTER).storedFileId()).isEqualTo(corrected.getId());
        assertThat(persisted.documentOf(InitialDocument.RESUME)).isEqualTo(opened.documentOf(InitialDocument.RESUME));
        assertThat(persisted.pullEvents()).singleElement().isInstanceOf(DegreeProcedureDocumentReplacedEvent.class);

        var closed = DegreeProcedure.rehydrate(opened.getId(), 1, student.getId(), secretary.getId(),
                opened.getOpenedAt(), Instant.now(), opened.getDocuments());
        assertThatThrownBy(() -> closed.replaceInitialDocument(secretary, pdf("carta_corregida.pdf")))
                .isInstanceOf(DomainValidationException.class);
    }
}
