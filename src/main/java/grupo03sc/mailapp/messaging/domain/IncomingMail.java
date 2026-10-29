package grupo03sc.mailapp.messaging.domain;

import java.util.List;

/**
 * The parts of a received email that decide whether and how it is handled.
 */
public record IncomingMail(String messageId, String from, List<String> recipients, String autoSubmitted,
        String subject, List<MailAttachment> attachments) {

    public IncomingMail {
        recipients = List.copyOf(recipients);
        attachments = List.copyOf(attachments);
    }
}
