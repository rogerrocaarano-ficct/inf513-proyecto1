package bo.org.tecnoweb.cpms.mail.domain;

/**
 * Lifecycle of a {@link ReceivedEmail}.
 *
 * <p>An email goes {@code FETCHED -> RECEIVED -> VALIDATED -> PROCESSED}, or to
 * {@code DISCARDED} from {@code FETCHED}, {@code RECEIVED} or {@code VALIDATED}.
 * {@link #PROCESSED} and {@link #DISCARDED} are final: the email is never executed again and its
 * message is deleted from the server.
 */
public enum ReceivedEmailState {

    /**
     * Read from the server, with the content of its attachments, not stored yet. Never persisted:
     * storing its attachments makes it {@link #RECEIVED}.
     */
    FETCHED,

    /** Read from the server and stored, with only the references to its stored files; not validated yet. */
    RECEIVED,

    /** Its command has a handler and meets the handler's contract; the use case has not committed yet. */
    VALIDATED,

    /** The use case was executed, with success or with a domain error. Final. */
    PROCESSED,

    /** The use case was never called: the command could not be resolved or validated. Final. */
    DISCARDED;

    /**
     * Tells whether the state is final.
     *
     * @return {@code true} for {@link #PROCESSED} and {@link #DISCARDED}
     */
    public boolean isFinal() {
        return this == PROCESSED || this == DISCARDED;
    }
}
