package grupo03sc.mailapp.messaging.application;

import grupo03sc.mailapp.messaging.domain.MailCommand;
import java.util.Set;

/**
 * Implemented by business subsystems to handle one command (D5).
 */
public interface CommandHandler {

    /** The command name as written in the subject; it is matched ignoring case. */
    String commandName();

    /** Role names allowed to run the command; empty means any active user account. */
    Set<String> allowedRoles();

    /** Runs inside the transaction of the command; throwing rolls it back. */
    CommandReply handle(MailCommand command, Sender sender);
}
