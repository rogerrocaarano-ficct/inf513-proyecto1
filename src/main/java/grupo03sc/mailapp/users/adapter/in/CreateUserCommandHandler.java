package grupo03sc.mailapp.users.adapter.in;

import static grupo03sc.mailapp.users.adapter.in.UserCommandSupport.argument;
import static grupo03sc.mailapp.users.adapter.in.UserCommandSupport.required;

import grupo03sc.mailapp.messaging.application.CommandHandler;
import grupo03sc.mailapp.messaging.application.CommandReply;
import grupo03sc.mailapp.messaging.application.Sender;
import grupo03sc.mailapp.messaging.domain.MailCommand;
import grupo03sc.mailapp.users.application.UserManagementService;
import grupo03sc.mailapp.users.domain.Role;
import grupo03sc.mailapp.users.domain.UserAccount;
import grupo03sc.mailapp.users.domain.UserAccountData;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * CrearUsuario(nombreCompleto; correo; roles[; registroUniversitario; cedula; telefono]): basic flow and A1.
 */
@Component
class CreateUserCommandHandler implements CommandHandler {

    private final UserManagementService service;

    CreateUserCommandHandler(UserManagementService service) {
        this.service = service;
    }

    @Override
    public String commandName() {
        return "CrearUsuario";
    }

    @Override
    public Set<String> allowedRoles() {
        return UserCommandSupport.MANAGER_ROLES;
    }

    @Override
    public CommandReply handle(MailCommand command, Sender sender) {
        List<String> args = command.arguments();
        UserCommandSupport.atMost(args, 6,
                "CrearUsuario(nombreCompleto; correo; roles[; registroUniversitario; cedula; telefono])");
        UserAccountData data = new UserAccountData(
                required(args, 0, "nombre completo"),
                required(args, 1, "dirección de correo"),
                UserCommandSupport.roles(required(args, 2, "roles")),
                argument(args, 3), argument(args, 4), argument(args, 5));
        UserAccount account = service.createAccount(sender.accountId(), data);
        return new CommandReply("Cuenta de usuario creada: %s <%s>, con los roles: %s.".formatted(
                account.getFullName(), account.getEmail(), Role.labels(account.getRoles())));
    }
}
