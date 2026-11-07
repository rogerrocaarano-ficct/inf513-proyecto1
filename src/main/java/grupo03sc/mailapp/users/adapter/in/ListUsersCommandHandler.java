package grupo03sc.mailapp.users.adapter.in;

import grupo03sc.mailapp.messaging.application.CommandHandler;
import grupo03sc.mailapp.messaging.application.CommandReply;
import grupo03sc.mailapp.messaging.application.Sender;
import grupo03sc.mailapp.messaging.domain.MailCommand;
import grupo03sc.mailapp.users.application.UserManagementService;
import grupo03sc.mailapp.users.domain.UserAccount;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * ListarUsuarios(): A2, the accounts within the scope of the sender.
 */
@Component
class ListUsersCommandHandler implements CommandHandler {

    private final UserManagementService service;

    ListUsersCommandHandler(UserManagementService service) {
        this.service = service;
    }

    @Override
    public String commandName() {
        return "ListarUsuarios";
    }

    @Override
    public Set<String> allowedRoles() {
        return UserCommandSupport.MANAGER_ROLES;
    }

    @Override
    public CommandReply handle(MailCommand command, Sender sender) {
        UserCommandSupport.atMost(command.arguments(), 0, "ListarUsuarios()");
        List<UserAccount> accounts = service.listAccounts(sender.accountId());
        if (accounts.isEmpty()) {
            return new CommandReply("No hay cuentas de usuario dentro de su alcance.");
        }
        return new CommandReply("Cuentas de usuario (" + accounts.size() + "):\n"
                + accounts.stream().map(UserCommandSupport::summary).collect(Collectors.joining("\n")));
    }
}
