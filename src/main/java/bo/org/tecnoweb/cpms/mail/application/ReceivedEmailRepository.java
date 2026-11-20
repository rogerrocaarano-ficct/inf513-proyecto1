package bo.org.tecnoweb.cpms.mail.application;

import bo.org.tecnoweb.cpms.mail.domain.ReceivedEmail;
import java.util.List;
import java.util.Optional;

/**
 * Port to persist received emails.
 */
public interface ReceivedEmailRepository {

    /**
     * Saves an email, or the new state of an email already saved.
     *
     * <p>A new email whose mailboxId is already stored is not inserted again, so reading the same
     * message twice never creates two emails.
     *
     * @param email the email to save
     */
    void store(ReceivedEmail email);

    /**
     * Returns the emails that still have to be validated or executed.
     *
     * @return the emails in {@link bo.org.tecnoweb.cpms.mail.domain.ReceivedEmailState#RECEIVED} or
     *         {@link bo.org.tecnoweb.cpms.mail.domain.ReceivedEmailState#VALIDATED}, in arrival order
     */
    List<ReceivedEmail> getNotProcessed();

    /**
     * Finds the email of a message.
     *
     * @param mailboxId the UIDL of the message
     * @return the stored email, or empty if the message was never stored
     */
    Optional<ReceivedEmail> findByMailboxId(String mailboxId);
}
