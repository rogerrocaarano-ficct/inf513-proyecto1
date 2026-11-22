package bo.org.tecnoweb.cpms.procedures.domain;

import bo.org.tecnoweb.cpms.common.DomainValidationException;
import bo.org.tecnoweb.cpms.storage.domain.StoredFile;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Identifies and validates an initial PDF in UC-002 (FR-002, NFR-005). The PDF arrives as a stored
 * file of the file storage (issue #9); its content is read by the use case only to check the
 * header.
 */
public enum InitialDocument {
    LETTER("carta", "Carta de solicitud"),
    RESUME("cv", "Hoja de vida"),
    IDENTITY("ci", "Cédula de identidad"),
    ENROLLMENT("boleta", "Boleta de inscripción");

    private static final byte[] PDF_HEADER = "%PDF-".getBytes(StandardCharsets.US_ASCII);
    private static final long PDF_LIMIT = 25L * 1024 * 1024;
    private final String keyword;
    private final String label;

    InitialDocument(String keyword, String label) {
        this.keyword = keyword;
        this.label = label;
    }

    /**
     * Returns the Spanish document label.
     *
     * @return the label
     */
    public String label() {
        return label;
    }

    /**
     * Identifies the type of an initial document from the name of its file (FR-002).
     *
     * @param file the stored file, already validated with {@link #requirePdf(StoredFile, byte[])}
     * @return the type whose keyword appears in the name
     * @throws DomainValidationException if no type, or more than one, matches the name
     */
    public static InitialDocument classify(StoredFile file) {
        String lowerName = nameOf(file).toLowerCase(Locale.ROOT);
        String base = lowerName.endsWith(".pdf") ? lowerName.substring(0, lowerName.length() - 4) : lowerName;
        Set<String> words = Arrays.stream(base.split("[^\\p{L}\\p{N}]+")).collect(Collectors.toSet());
        List<InitialDocument> matches = Arrays.stream(values()).filter(type -> words.contains(type.keyword)).toList();
        if (matches.size() != 1) {
            throw invalid("No se pudo identificar el tipo de documento del archivo " + nameOf(file) + ".");
        }
        return matches.getFirst();
    }

    /**
     * Validates the format and size shared by procedure PDFs (NFR-005): at most 25 MB, a
     * {@code .pdf} name and the PDF header. Used by UC-002 and UC-003.
     *
     * @param file    the stored file, for its name and size
     * @param content its bytes, for the header
     * @throws DomainValidationException if the file is too large or is not a PDF
     */
    public static void requirePdf(StoredFile file, byte[] content) {
        String name = nameOf(file);
        if (file.getSize() > PDF_LIMIT) {
            throw invalid("El archivo " + name + " supera el límite de 25 MB.");
        }
        if (!name.toLowerCase(Locale.ROOT).endsWith(".pdf") || content.length < PDF_HEADER.length
                || Arrays.mismatch(content, 0, PDF_HEADER.length, PDF_HEADER, 0, PDF_HEADER.length) != -1) {
            throw invalid("El archivo " + name + " debe ser un PDF.");
        }
    }

    /**
     * Creates an attachment validation error for UC-002.
     *
     * @param message the Spanish message
     * @return the error, on the field {@code adjuntos}
     */
    public static DomainValidationException invalid(String message) {
        return new DomainValidationException(Map.of("adjuntos", message));
    }

    private static String nameOf(StoredFile file) {
        return file.getOriginalName() == null ? "" : file.getOriginalName();
    }
}
