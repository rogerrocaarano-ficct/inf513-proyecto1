package bo.org.tecnoweb.cpms.procedures.domain;

import static bo.org.tecnoweb.cpms.procedures.ProcedureFixtures.pdf;
import static bo.org.tecnoweb.cpms.procedures.ProcedureFixtures.pdfContent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import bo.org.tecnoweb.cpms.common.DomainValidationException;
import bo.org.tecnoweb.cpms.storage.domain.StoredFile;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

/** Checks the PDF rules of NFR-005 and the classification by name of UC-002, on stored files. */
class InitialDocumentTest {

    private static StoredFile file(String name, long size) {
        return StoredFile.builder().withOriginalName(name).withContentType("application/pdf").withSize(size).store();
    }

    private static void assertRejected(Runnable check, String message) {
        assertThatThrownBy(check::run).isInstanceOfSatisfying(DomainValidationException.class,
                e -> assertThat(e.getErrors()).containsEntry("adjuntos", message));
    }

    @Test
    void acceptsAPdfUpTo25Mb() {
        assertThatCode(() -> InitialDocument.requirePdf(file("perfil.pdf", 25L * 1024 * 1024),
                pdfContent("perfil.pdf"))).doesNotThrowAnyException();
    }

    @Test
    void rejectsAFileLargerThan25MbBeforeLookingAtItsContent() {
        assertRejected(() -> InitialDocument.requirePdf(file("perfil.pdf", 25L * 1024 * 1024 + 1), new byte[0]),
                "El archivo perfil.pdf supera el límite de 25 MB.");
    }

    @Test
    void rejectsANameWithoutPdfExtensionOrAContentWithoutPdfHeader() {
        assertRejected(() -> InitialDocument.requirePdf(pdf("perfil.txt"), pdfContent("perfil.txt")),
                "El archivo perfil.txt debe ser un PDF.");
        assertRejected(() -> InitialDocument.requirePdf(pdf("perfil.pdf"),
                "no es un pdf".getBytes(StandardCharsets.US_ASCII)), "El archivo perfil.pdf debe ser un PDF.");
        assertRejected(() -> InitialDocument.requirePdf(pdf("perfil.pdf"), new byte[] {'%'}),
                "El archivo perfil.pdf debe ser un PDF.");
    }

    @Test
    void classifiesByAKeywordOfTheName() {
        assertThat(InitialDocument.classify(pdf("Carta_Solicitud.pdf"))).isEqualTo(InitialDocument.LETTER);
        assertThat(InitialDocument.classify(pdf("boleta-2026.pdf"))).isEqualTo(InitialDocument.ENROLLMENT);
        assertRejected(() -> InitialDocument.classify(pdf("documento.pdf")),
                "No se pudo identificar el tipo de documento del archivo documento.pdf.");
        assertRejected(() -> InitialDocument.classify(pdf("carta y cv.pdf")),
                "No se pudo identificar el tipo de documento del archivo carta y cv.pdf.");
    }
}
