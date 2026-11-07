package grupo03sc.mailapp.messaging.application;

/**
 * Interface provided by Messaging: sends a notice by email once the current transaction commits.
 */
public interface Notifier {

    void notify(String recipientEmail, String subject, String body);
}
