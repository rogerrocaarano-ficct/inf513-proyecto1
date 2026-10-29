package grupo03sc.mailapp.users.adapter.in;

import grupo03sc.mailapp.messaging.application.CommandHandler;
import grupo03sc.mailapp.messaging.application.CommandReply;
import grupo03sc.mailapp.messaging.application.Sender;
import grupo03sc.mailapp.messaging.domain.MailCommand;
import grupo03sc.mailapp.users.application.AccountChangeResult;
import grupo03sc.mailapp.users.application.UserManagementService;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * DesactivarUsuario(correo): A4.
 */
@Component
class DeactivateUserCommandHandler implements CommandHandler {

    private final UserManagementService service;

    DeactivateUserCommandHandler(UserManagementService service) {
        this.service = service;
    }

    @Override
    public String commandName() {
        return "DesactivarUsuario";
    }

    @Override
    public Set<String> allowedRoles() {
        return UserCommandSupport.MANAGER_ROLES;
    }

    @Override
    public CommandReply handle(MailCommand command, Sender sender) {
        UserCommandSupport.atMost(command.arguments(), 1, "DesactivarUsuario(correo)");
        String email = UserCommandSupport.required(command.arguments(), 0, "dirección de correo");
        AccountChangeResult result = service.deactivateAccount(sender.accountId(), email);
        return new CommandReply(UserCommandSupport.withAffectedProcesses(
                "Cuenta de usuario desactivada: " + result.account().getEmail() + ".", result.affectedProcesses()));
    }
}
