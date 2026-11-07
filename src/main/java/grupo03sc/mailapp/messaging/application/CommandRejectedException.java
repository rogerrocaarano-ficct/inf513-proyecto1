package grupo03sc.mailapp.messaging.application;

/**
 * Thrown by a command handler to reject the command: Messaging rolls back everything the command
 * did and replies with the message of the exception (D2).
 */
public class CommandRejectedException extends RuntimeException {

    public CommandRejectedException(String message) {
        super(message);
    }
}
