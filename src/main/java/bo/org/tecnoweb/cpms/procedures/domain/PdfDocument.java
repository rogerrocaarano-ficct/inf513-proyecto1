package bo.org.tecnoweb.cpms.procedures.domain;

import bo.org.tecnoweb.cpms.common.DomainValidationException;
import bo.org.tecnoweb.cpms.storage.domain.StoredFile;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

/**
 * Format and size rules of the PDFs that the procedures receive (NFR-005): the opening requirements
 * of UC-002 and the project profile of UC-003. The PDF arrives as a stored file of the file storage
 * (issue #9); the use case reads its content only to check the header.
 */
public final class PdfDocument {
    /** Field of the validation errors about attachments. */
    public static final String FIELD = "adjuntos";

    private static final byte[] PDF_HEADER = "%PDF-".getBytes(StandardCharsets.US_ASCII);
    private static final long PDF_LIMIT = 25L * 1024 * 1024;

    private PdfDocument() {}

    /**
     * Checks that a stored file is a PDF of at most 25 MB: a {@code .pdf} name and the PDF header.
     *
     * @param file    the stored file, for its name and size
     * @param content its bytes, for the header
     * @throws DomainValidationException if the file is too large or is not a PDF
     */
    public static void require(StoredFile file, byte[] content) {
        String name = file.getOriginalName() == null ? "" : file.getOriginalName();
        if (file.getSize() > PDF_LIMIT) {
            throw invalid("El archivo " + name + " supera el límite de 25 MB.");
        }
        if (!name.toLowerCase(Locale.ROOT).endsWith(".pdf") || content.length < PDF_HEADER.length
                || Arrays.mismatch(content, 0, PDF_HEADER.length, PDF_HEADER, 0, PDF_HEADER.length) != -1) {
            throw invalid("El archivo " + name + " debe ser un PDF.");
        }
    }

    private static DomainValidationException invalid(String message) {
        return new DomainValidationException(Map.of(FIELD, message));
    }
}
