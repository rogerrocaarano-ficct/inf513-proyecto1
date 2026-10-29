package grupo03sc.mailapp.messaging.domain;

import java.util.List;

/**
 * A command received by email: its registered name, its arguments, the sender address and the attachments.
 */
public record MailCommand(String name, List<String> arguments, String senderEmail, List<MailAttachment> attachments) {

    public MailCommand {
        arguments = List.copyOf(arguments);
        attachments = List.copyOf(attachments);
    }
}
