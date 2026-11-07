package grupo03sc.mailapp.users.adapter.in;

import grupo03sc.mailapp.messaging.application.CommandHandler;
import grupo03sc.mailapp.messaging.application.CommandReply;
import grupo03sc.mailapp.messaging.application.Sender;
import grupo03sc.mailapp.messaging.domain.MailCommand;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Diagnostic command: checks the whole mail circuit without changing data.
 */
@Component
class PingCommandHandler implements CommandHandler {

    @Override
    public String commandName() {
        return "Ping";
    }

    @Override
    public Set<String> allowedRoles() {
        return Set.of();
    }

    @Override
    public CommandReply handle(MailCommand command, Sender sender) {
        return new CommandReply("Pong");
    }
}
