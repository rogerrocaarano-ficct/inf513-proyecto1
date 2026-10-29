package grupo03sc.mailapp.messaging.application;

public class DuplicateCommandException extends RuntimeException {

    public DuplicateCommandException(String commandName) {
        super("Command already registered: " + commandName);
    }
}
