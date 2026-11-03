package grupo03sc.mailapp.graduationprocesses.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class InitialDocumentTests {

    @ParameterizedTest
    @CsvSource({
            "carta.pdf, application/pdf",
            "cv.PNG, image/png",
            "cedula.jpg, image/jpeg",
            "boleta.jpeg, image/jpeg",
            "carta.pdf, application/octet-stream"})
    void acceptsPngJpgAndPdf(String fileName, String contentType) {
        assertThatCode(() -> InitialDocument.check(new DocumentFile(fileName, contentType, new byte[10])))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @CsvSource({
            "hojadevida.docx, application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "carta.pdf, image/png",
            "carta, application/pdf"})
    void rejectsOtherFormats(String fileName, String contentType) {
        assertThatThrownBy(() -> InitialDocument.check(new DocumentFile(fileName, contentType, new byte[10])))
                .isInstanceOfSatisfying(GraduationRuleViolationException.class,
                        e -> assertThat(e.getReason()).isEqualTo(GraduationRejectionReason.INVALID_DOCUMENT));
    }

    @Test
    void acceptsExactly1MbAndRejectsMore() {
        assertThatCode(() -> InitialDocument.check(
                new DocumentFile("boleta.pdf", "application/pdf", new byte[(int) InitialDocument.MAX_SIZE_BYTES])))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> InitialDocument.check(
                new DocumentFile("boleta.pdf", "application/pdf", new byte[(int) InitialDocument.MAX_SIZE_BYTES + 1])))
                .hasMessageContaining("1 MB");
    }

    @Test
    void rejectsAnEmptyFile() {
        assertThatThrownBy(() -> InitialDocument.check(new DocumentFile("boleta.pdf", "application/pdf", new byte[0])))
                .hasMessageContaining("vacío");
    }

    @Test
    void storesTheContentTypeOfItsExtension() {
        InitialDocument document = InitialDocument.of(InitialDocumentType.REQUEST_LETTER,
                new DocumentFile("carta.PDF", "application/octet-stream", new byte[3]), "k", Instant.EPOCH);

        assertThat(document.getContentType()).isEqualTo("application/pdf");
    }
}
