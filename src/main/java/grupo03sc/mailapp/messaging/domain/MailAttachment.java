package grupo03sc.mailapp.messaging.domain;

/**
 * A file attached to a command email, named by its file name.
 */
public record MailAttachment(String fileName, String contentType, byte[] content) {
}
