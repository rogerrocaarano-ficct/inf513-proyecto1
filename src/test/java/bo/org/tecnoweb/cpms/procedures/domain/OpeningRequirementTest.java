package bo.org.tecnoweb.cpms.procedures.domain;

import static bo.org.tecnoweb.cpms.procedures.ProcedureFixtures.pdf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import bo.org.tecnoweb.cpms.common.DomainValidationException;
import org.junit.jupiter.api.Test;

/** Checks how an attachment is matched to the opening requirement it fulfills (UC-002, FR-002). */
class OpeningRequirementTest {

    private static void assertRejected(String name) {
        assertThatThrownBy(() -> OpeningRequirement.classify(pdf(name)))
                .isInstanceOfSatisfying(DomainValidationException.class, e -> assertThat(e.getErrors())
                        .containsEntry("adjuntos", "No se pudo identificar el requisito del archivo " + name + "."));
    }

    @Test
    void classifiesByAKeywordOfTheName() {
        assertThat(OpeningRequirement.classify(pdf("Carta_Solicitud.pdf"))).isEqualTo(OpeningRequirement.LETTER);
        assertThat(OpeningRequirement.classify(pdf("cv.pdf"))).isEqualTo(OpeningRequirement.RESUME);
        assertThat(OpeningRequirement.classify(pdf("ci-anverso.pdf"))).isEqualTo(OpeningRequirement.IDENTITY);
        assertThat(OpeningRequirement.classify(pdf("boleta-2026.pdf"))).isEqualTo(OpeningRequirement.ENROLLMENT);
    }

    @Test
    void rejectsANameWithoutKeywordOrWithSeveral() {
        assertRejected("documento.pdf");
        assertRejected("carta y cv.pdf");
    }
}
