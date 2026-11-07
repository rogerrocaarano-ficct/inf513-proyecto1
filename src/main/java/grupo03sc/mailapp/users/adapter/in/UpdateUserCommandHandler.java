package grupo03sc.mailapp.users.adapter.in;

import static grupo03sc.mailapp.users.adapter.in.UserCommandSupport.required;

import grupo03sc.mailapp.messaging.application.CommandHandler;
import grupo03sc.mailapp.messaging.application.CommandReply;
import grupo03sc.mailapp.messaging.application.Sender;
import grupo03sc.mailapp.messaging.domain.MailCommand;
import grupo03sc.mailapp.users.application.AccountChangeResult;
import grupo03sc.mailapp.users.application.UserManagementService;
import grupo03sc.mailapp.users.domain.AccountField;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * ModificarUsuario(correo; campo; valor): A3, changes one field or the roles.
 */
@Component
class UpdateUserCommandHandler implements CommandHandler {

    private final UserManagementService service;

    UpdateUserCommandHandler(UserManagementService service) {
        this.service = service;
    }

    @Override
    public String commandName() {
        return "ModificarUsuario";
    }

    @Override
    public Set<String> allowedRoles() {
        return UserCommandSupport.MANAGER_ROLES;
    }

    @Override
    public CommandReply handle(MailCommand command, Sender sender) {
        List<String> args = command.arguments();
        UserCommandSupport.atMost(args, 3, "ModificarUsuario(correo; campo; valor)");
        String email = required(args, 0, "dirección de correo");
        AccountField field = UserCommandSupport.field(required(args, 1, "campo"));
        String value = UserCommandSupport.argument(args, 2);
        AccountChangeResult result = field == AccountField.ROLES
                ? service.updateRoles(sender.accountId(), email, UserCommandSupport.roles(value))
                : service.updateField(sender.accountId(), email, field, value);
        return new CommandReply(UserCommandSupport.withAffectedProcesses(
                "Cuenta de usuario modificada:\n" + UserCommandSupport.details(result.account()),
                result.affectedProcesses()));
    }
}
