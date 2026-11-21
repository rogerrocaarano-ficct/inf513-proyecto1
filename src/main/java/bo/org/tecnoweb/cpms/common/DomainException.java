package bo.org.tecnoweb.cpms.common;

/**
 * A domain rule rejected an operation.
 *
 * <p>The message is user-facing text in Spanish, sent as is to the user who requested the
 * operation. The error code identifies the rule for logs and tests.
 */
public abstract class DomainException extends RuntimeException {

    /** Identifier of the rule that was broken. */
    private final String errorCode;

    /**
     * Creates the exception.
     *
     * @param errorCode identifier of the rule that was broken
     * @param message   user-facing explanation, in Spanish
     */
    protected DomainException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Returns the identifier of the rule that was broken.
     *
     * @return the error code
     */
    public String getErrorCode() {
        return errorCode;
    }
}
