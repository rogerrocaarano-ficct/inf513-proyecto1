package bo.org.tecnoweb.cpms.procedures.domain;

import bo.org.tecnoweb.cpms.common.DomainValidationException;
import bo.org.tecnoweb.cpms.storage.domain.StoredFile;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Requirement to open the graduation procedure by Degree Project (UC-002, FR-002): each one is
 * fulfilled by a PDF that Secretaría attaches when opening the procedure. The requirement of an
 * attachment is told by a keyword in the name of its file.
 */
public enum OpeningRequirement {
    LETTER("carta", "Carta de solicitud"),
    RESUME("cv", "Hoja de vida"),
    IDENTITY("ci", "Cédula de identidad"),
    ENROLLMENT("boleta", "Boleta de inscripción");

    private final String keyword;
    private final String label;

    OpeningRequirement(String keyword, String label) {
        this.keyword = keyword;
        this.label = label;
    }

    /**
     * Returns the Spanish name of the requirement.
     *
     * @return the label
     */
    public String label() {
        return label;
    }

    /**
     * Tells which requirement a file fulfills from the name of the file (FR-002).
     *
     * @param file the stored file, already validated with {@link PdfDocument#require(StoredFile, byte[])}
     * @return the requirement whose keyword appears in the name
     * @throws DomainValidationException if no requirement, or more than one, matches the name
     */
    public static OpeningRequirement classify(StoredFile file) {
        String name = file.getOriginalName() == null ? "" : file.getOriginalName();
        String lowerName = name.toLowerCase(Locale.ROOT);
        String base = lowerName.endsWith(".pdf") ? lowerName.substring(0, lowerName.length() - 4) : lowerName;
        Set<String> words = Arrays.stream(base.split("[^\\p{L}\\p{N}]+")).collect(Collectors.toSet());
        List<OpeningRequirement> matches = Arrays.stream(values()).filter(type -> words.contains(type.keyword)).toList();
        if (matches.size() != 1) {
            throw invalid("No se pudo identificar el requisito del archivo " + name + ".");
        }
        return matches.getFirst();
    }

    /**
     * Creates an attachment validation error for UC-002.
     *
     * @param message the Spanish message
     * @return the error, on the field {@code adjuntos}
     */
    public static DomainValidationException invalid(String message) {
        return new DomainValidationException(Map.of(PdfDocument.FIELD, message));
    }
}
