package bo.org.tecnoweb.cpms.mail.application;

import bo.org.tecnoweb.cpms.mail.domain.ReceivedEmail;
import java.util.List;

/**
 * Port to read the mailbox (POP3).
 *
 * <p>Messages are identified by their UIDL, never by their sequence number in the session, which
 * changes between sessions.
 */
public interface EmailInbox {

    /**
     * Reads messages without deleting them from the server.
     *
     * @param maxMessages maximum number of messages to return
     * @return up to {@code maxMessages} new emails in state
     *         {@link bo.org.tecnoweb.cpms.mail.domain.ReceivedEmailState#RECEIVED}, oldest first
     * @throws IllegalStateException if the mailbox cannot be read
     */
    List<ReceivedEmail> poll(int maxMessages);

    /**
     * Deletes a message from the server. Does nothing if the message is no longer there.
     *
     * @param mailboxId the UIDL of the message
     * @throws IllegalStateException if the mailbox cannot be reached
     */
    void delete(String mailboxId);
}
