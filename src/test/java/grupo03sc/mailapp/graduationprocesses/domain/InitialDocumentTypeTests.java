package grupo03sc.mailapp.graduationprocesses.domain;

import static grupo03sc.mailapp.graduationprocesses.domain.InitialDocumentType.CURRICULUM_VITAE;
import static grupo03sc.mailapp.graduationprocesses.domain.InitialDocumentType.ENROLLMENT_SLIP;
import static grupo03sc.mailapp.graduationprocesses.domain.InitialDocumentType.ID_CARD_COPY;
import static grupo03sc.mailapp.graduationprocesses.domain.InitialDocumentType.REQUEST_LETTER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class InitialDocumentTypeTests {

    @ParameterizedTest
    @CsvSource({
            "Carta_Soto.pdf, REQUEST_LETTER",
            "CV.jpg, CURRICULUM_VITAE",
            "cv_soto.pdf, CURRICULUM_VITAE",
            "Hoja de Vida.pdf, CURRICULUM_VITAE",
            "hojadevida.png, CURRICULUM_VITAE",
            "Cédula.pdf, ID_CARD_COPY",
            "copia-CEDULA.jpeg, ID_CARD_COPY",
            "boleta-2026.png, ENROLLMENT_SLIP"})
    void recognizesTheTypeIgnoringCaseAccentsAndSeparators(String fileName, InitialDocumentType type) {
        assertThat(InitialDocumentType.fromFileName(fileName)).contains(type);
    }

    @ParameterizedTest
    @ValueSource(strings = {"foto.png", "pcvx.pdf", "documento.cv", "carta y boleta.pdf", ""})
    void recognizesNothingWhenNoTypeOrSeveralTypesMatch(String fileName) {
        assertThat(InitialDocumentType.fromFileName(fileName)).isEmpty();
    }

    @Test
    void classifiesTheFourDocuments() {
        Map<InitialDocumentType, DocumentFile> byType = InitialDocumentType.classify(
                List.of(file("Carta_Soto.pdf"), file("CV.jpg"), file("Cédula.pdf"), file("boleta-2026.png")));

        assertThat(byType).containsOnlyKeys(REQUEST_LETTER, CURRICULUM_VITAE, ID_CARD_COPY, ENROLLMENT_SLIP);
        assertThat(byType.get(ID_CARD_COPY).fileName()).isEqualTo("Cédula.pdf");
    }

    @Test
    void rejectsAnUnrecognizedAttachmentFirst() {
        assertThatThrownBy(() -> InitialDocumentType.classify(
                List.of(file("carta.pdf"), file("carta2.pdf"), file("foto.png"))))
                .isInstanceOfSatisfying(GraduationRuleViolationException.class, e -> {
                    assertThat(e.getReason()).isEqualTo(GraduationRejectionReason.UNRECOGNIZED_DOCUMENT);
                    assertThat(e.getMessage()).contains("foto.png");
                });
    }

    @Test
    void rejectsTwoAttachmentsOfTheSameType() {
        assertThatThrownBy(() -> InitialDocumentType.classify(List.of(file("carta.pdf"), file("carta2.pdf"),
                file("cv.pdf"), file("cedula.pdf"), file("boleta.pdf"))))
                .isInstanceOfSatisfying(GraduationRuleViolationException.class, e -> {
                    assertThat(e.getReason()).isEqualTo(GraduationRejectionReason.DUPLICATE_DOCUMENT);
                    assertThat(e.getMessage()).contains("carta de solicitud");
                });
    }

    @Test
    void rejectsMissingTypes() {
        assertThatThrownBy(() -> InitialDocumentType.classify(
                List.of(file("carta.pdf"), file("cv.pdf"), file("cedula.pdf"))))
                .isInstanceOfSatisfying(GraduationRuleViolationException.class, e -> {
                    assertThat(e.getReason()).isEqualTo(GraduationRejectionReason.MISSING_DOCUMENT);
                    assertThat(e.getMessage()).contains("boleta de inscripción");
                });
    }

    private static DocumentFile file(String name) {
        return new DocumentFile(name, "application/pdf", new byte[] {1});
    }
}
