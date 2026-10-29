package grupo03sc.mailapp.users.adapter.in;

import grupo03sc.mailapp.messaging.application.CommandHandler;
import grupo03sc.mailapp.messaging.application.CommandReply;
import grupo03sc.mailapp.messaging.application.Sender;
import grupo03sc.mailapp.messaging.domain.MailCommand;
import grupo03sc.mailapp.users.application.UserManagementService;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * VerUsuario(correo): A2, the data of one account.
 */
@Component
class ViewUserCommandHandler implements CommandHandler {

    private final UserManagementService service;

    ViewUserCommandHandler(UserManagementService service) {
        this.service = service;
    }

    @Override
    public String commandName() {
        return "VerUsuario";
    }

    @Override
    public Set<String> allowedRoles() {
        return UserCommandSupport.MANAGER_ROLES;
    }

    @Override
    public CommandReply handle(MailCommand command, Sender sender) {
        UserCommandSupport.atMost(command.arguments(), 1, "VerUsuario(correo)");
        String email = UserCommandSupport.required(command.arguments(), 0, "dirección de correo");
        return new CommandReply(UserCommandSupport.details(service.findAccount(sender.accountId(), email)));
    }
}
