package bo.org.tecnoweb.cpms.mail.application;

import bo.org.tecnoweb.cpms.mail.domain.File;
import java.util.List;

/**
 * Port to send email (SMTP). Every email is sent from the system address.
 */
public interface EmailSender {

    /**
     * Sends an email.
     *
     * @param to          recipient address
     * @param subject     subject
     * @param body        plain text body
     * @param attachments files to attach; may be empty
     * @throws RuntimeException if the email cannot be built or sent
     */
    void send(String to, String subject, String body, List<File> attachments);
}
