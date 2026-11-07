package grupo03sc.mailapp.messaging.application;

/**
 * Port to the SMTP server. Every message is marked with {@code Auto-Submitted}
 * so that other systems do not answer it automatically.
 */
public interface OutgoingMail {

    void send(String to, String subject, String body);
}
