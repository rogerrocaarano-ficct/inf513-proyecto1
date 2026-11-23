package bo.org.tecnoweb.cpms.mail.application;

import java.util.List;
import java.util.UUID;

/**
 * Port to send email (SMTP). Every email is sent from the system address.
 */
public interface EmailSender {

    /**
     * Sends an email. The attachments are files already kept in file storage (issue #9): the
     * adapter reads their name, media type and content from it.
     *
     * @param to            recipient address
     * @param subject       subject
     * @param body          plain text body
     * @param attachmentIds identifiers of the stored files to attach, in order; may be empty
     * @throws RuntimeException if the email cannot be built or sent, or an attachment cannot be read
     */
    void send(String to, String subject, String body, List<UUID> attachmentIds);
}
