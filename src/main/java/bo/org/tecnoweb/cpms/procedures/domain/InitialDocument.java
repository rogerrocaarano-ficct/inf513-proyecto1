package bo.org.tecnoweb.cpms.procedures.domain;

import bo.org.tecnoweb.cpms.common.DomainValidationException;
import bo.org.tecnoweb.cpms.mail.domain.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Identifies and validates an initial PDF in UC-002 (FR-002, NFR-005). */
public enum InitialDocument {
    LETTER("carta", "Carta de solicitud"),
    RESUME("cv", "Hoja de vida"),
    IDENTITY("ci", "Cédula de identidad"),
    ENROLLMENT("boleta", "Boleta de inscripción");

    private static final byte[] PDF_HEADER = "%PDF-".getBytes(StandardCharsets.US_ASCII);
    private static final int PDF_LIMIT = 25 * 1024 * 1024;
    private final String keyword;
    private final String label;

    InitialDocument(String keyword, String label) {
        this.keyword = keyword;
        this.label = label;
    }

    /** Returns the Spanish document label. */
    public String label() {
        return label;
    }

    /** Validates the PDF and identifies its type from the filename. */
    public static InitialDocument classify(File file) {
        String name = file.getName() == null ? "" : file.getName();
        String lowerName = name.toLowerCase(Locale.ROOT);
        byte[] content = file.getData();
        if (content.length > PDF_LIMIT) {
            throw invalid("El archivo " + name + " supera el límite de 25 MB.");
        }
        if (!lowerName.endsWith(".pdf") || content.length < PDF_HEADER.length
                || Arrays.mismatch(content, 0, PDF_HEADER.length, PDF_HEADER, 0, PDF_HEADER.length) != -1) {
            throw invalid("El archivo " + name + " debe ser un PDF.");
        }
        Set<String> words = Arrays.stream(lowerName.substring(0, lowerName.length() - 4)
                .split("[^\\p{L}\\p{N}]+")).collect(Collectors.toSet());
        List<InitialDocument> matches = Arrays.stream(values()).filter(type -> words.contains(type.keyword)).toList();
        if (matches.size() != 1) {
            throw invalid("No se pudo identificar el tipo de documento del archivo " + name + ".");
        }
        return matches.getFirst();
    }

    /** Creates an attachment validation error for UC-002. */
    public static DomainValidationException invalid(String message) {
        return new DomainValidationException(Map.of("adjuntos", message));
    }
}
