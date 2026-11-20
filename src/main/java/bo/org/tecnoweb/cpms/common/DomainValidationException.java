package bo.org.tecnoweb.cpms.common;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * One or more input values are invalid.
 *
 * <p>Each invalid field is reported with its own user-facing message, so the user can fix all of
 * them at once (NFR-001).
 */
public class DomainValidationException extends DomainException {

    /** Error code shared by every validation exception. */
    public static final String ERROR_CODE = "VALIDATION_ERROR";

    /** Invalid fields mapped to their user-facing messages, in the order they were found. */
    private final Map<String, String> errors;

    /**
     * Creates the exception.
     *
     * @param errors invalid fields mapped to their user-facing messages, in the order they were found
     * @throws IllegalArgumentException if {@code errors} is {@code null} or empty
     */
    public DomainValidationException(Map<String, String> errors) {
        super(ERROR_CODE, "Los datos enviados no son válidos.");
        if (errors == null || errors.isEmpty()) {
            throw new IllegalArgumentException("A validation exception needs at least one error");
        }
        this.errors = Collections.unmodifiableMap(new LinkedHashMap<>(errors));
    }

    /**
     * Returns the invalid fields.
     *
     * @return an unmodifiable map from each invalid field to its user-facing message, in the order
     *         the errors were found
     */
    public Map<String, String> getErrors() {
        return errors;
    }
}
