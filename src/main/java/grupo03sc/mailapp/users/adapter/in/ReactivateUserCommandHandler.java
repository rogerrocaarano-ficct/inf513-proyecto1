package grupo03sc.mailapp.users.adapter.in;

import grupo03sc.mailapp.messaging.application.CommandHandler;
import grupo03sc.mailapp.messaging.application.CommandReply;
import grupo03sc.mailapp.messaging.application.Sender;
import grupo03sc.mailapp.messaging.domain.MailCommand;
import grupo03sc.mailapp.users.application.UserManagementService;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * ReactivarUsuario(correo): A5.
 */
@Component
class ReactivateUserCommandHandler implements CommandHandler {

    private final UserManagementService service;

    ReactivateUserCommandHandler(UserManagementService service) {
        this.service = service;
    }

    @Override
    public String commandName() {
        return "ReactivarUsuario";
    }

    @Override
    public Set<String> allowedRoles() {
        return UserCommandSupport.MANAGER_ROLES;
    }

    @Override
    public CommandReply handle(MailCommand command, Sender sender) {
        UserCommandSupport.atMost(command.arguments(), 1, "ReactivarUsuario(correo)");
        String email = UserCommandSupport.required(command.arguments(), 0, "dirección de correo");
        return new CommandReply("Cuenta de usuario reactivada: "
                + service.reactivateAccount(sender.accountId(), email).getEmail() + ".");
    }
}
