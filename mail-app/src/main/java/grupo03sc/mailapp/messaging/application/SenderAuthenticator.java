package grupo03sc.mailapp.messaging.application;

import java.util.Optional;

/**
 * Messaging port implemented by Users (D5): identifies the sender by their email address.
 */
public interface SenderAuthenticator {

    /** The active user account with that address, ignoring case. */
    Optional<Sender> authenticate(String senderEmail);
}
