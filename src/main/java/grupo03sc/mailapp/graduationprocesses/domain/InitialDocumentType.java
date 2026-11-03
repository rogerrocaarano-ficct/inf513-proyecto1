package grupo03sc.mailapp.graduationprocesses.domain;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The four initial documents, recognized by a word of the attachment's file name (NFR-IN-001, D3):
 * without the extension, ignoring case, accents and separators. {@code cv} only counts as a
 * separate word.
 */
public enum InitialDocumentType {
    REQUEST_LETTER("carta de solicitud", List.of("carta"), false),
    CURRICULUM_VITAE("hoja de vida", List.of("hojadevida"), true),
    ID_CARD_COPY("fotocopia de cédula de identidad", List.of("cedula"), false),
    ENROLLMENT_SLIP("boleta de inscripción", List.of("boleta"), false);

    private static final Pattern MARKS = Pattern.compile("\\p{M}+");
    private static final Pattern SEPARATORS = Pattern.compile("[^a-z0-9]+");
    private static final Pattern CV_WORD = Pattern.compile("(^|[^a-z0-9])cv([^a-z0-9]|$)");

    private final String label;
    private final List<String> keywords;
    private final boolean alsoCv;

    InitialDocumentType(String label, List<String> keywords, boolean alsoCv) {
        this.label = label;
        this.keywords = keywords;
        this.alsoCv = alsoCv;
    }

    public String label() {
        return label;
    }

    /** The type named by the file name, or empty if it names none or more than one. */
    public static Optional<InitialDocumentType> fromFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return Optional.empty();
        }
        String words = normalize(withoutExtension(fileName));
        String compact = SEPARATORS.matcher(words).replaceAll("");
        List<InitialDocumentType> matches = Arrays.stream(values())
                .filter(type -> type.keywords.stream().anyMatch(compact::contains)
                        || type.alsoCv && CV_WORD.matcher(words).find())
                .toList();
        return matches.size() == 1 ? Optional.of(matches.getFirst()) : Optional.empty();
    }

    /**
     * Assigns each file to its type: rejects a file that names no type or more than one, then two
     * files of the same type, then the types without a file.
     */
    public static Map<InitialDocumentType, DocumentFile> classify(List<DocumentFile> files) {
        List<String> unrecognized = files.stream()
                .filter(file -> fromFileName(file.fileName()).isEmpty())
                .map(file -> file.fileName() == null ? "(sin nombre)" : file.fileName())
                .toList();
        if (!unrecognized.isEmpty()) {
            throw new GraduationRuleViolationException(GraduationRejectionReason.UNRECOGNIZED_DOCUMENT,
                    String.join(", ", unrecognized)
                            + " (el nombre debe contener carta, hojadevida o cv, cedula o boleta)");
        }
        Map<InitialDocumentType, DocumentFile> byType = new EnumMap<>(InitialDocumentType.class);
        for (DocumentFile file : files) {
            InitialDocumentType type = fromFileName(file.fileName()).orElseThrow();
            if (byType.putIfAbsent(type, file) != null) {
                throw new GraduationRuleViolationException(GraduationRejectionReason.DUPLICATE_DOCUMENT,
                        type.label() + " (" + byType.get(type).fileName() + ", " + file.fileName() + ")");
            }
        }
        List<InitialDocumentType> missing = new ArrayList<>(Arrays.asList(values()));
        missing.removeAll(byType.keySet());
        if (!missing.isEmpty()) {
            throw new GraduationRuleViolationException(GraduationRejectionReason.MISSING_DOCUMENT,
                    missing.stream().map(InitialDocumentType::label).collect(Collectors.joining(", ")));
        }
        return byType;
    }

    private static String withoutExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot > 0 ? fileName.substring(0, dot) : fileName;
    }

    private static String normalize(String text) {
        return MARKS.matcher(Normalizer.normalize(text, Normalizer.Form.NFD)).replaceAll("").toLowerCase(Locale.ROOT);
    }
}
