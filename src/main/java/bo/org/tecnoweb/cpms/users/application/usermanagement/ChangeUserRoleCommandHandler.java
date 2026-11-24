package bo.org.tecnoweb.cpms.users.application.usermanagement;

import bo.org.tecnoweb.cpms.mail.application.EmailCommandHandler;
import bo.org.tecnoweb.cpms.mail.domain.ReceivedEmail;
import bo.org.tecnoweb.cpms.users.application.CommandGuide;
import bo.org.tecnoweb.cpms.users.application.DocumentedCommand;
import bo.org.tecnoweb.cpms.users.domain.RolePermission;
import java.util.List;

/**
 * Receives the email command {@code usuarios.cambiarRol[email; role]} and runs the alternate flow A1 of
 * UC-001, role only (FR-031), with the sender as requester.
 */
public class ChangeUserRoleCommandHandler implements EmailCommandHandler, DocumentedCommand {

    /** Name of the command in the email subject. */
    public static final String ACTION = "usuarios.cambiarRol";

    /** Description of the command for the help reply (FR-066). */
    public static final CommandGuide GUIDE = new CommandGuide(ACTION,
            "usuarios.cambiarRol[correo; rol]",
            "Cambiar el rol de una cuenta.",
            "",
            CommandGuide.forEveryRole(RolePermission::toChangeRole));

    private final UserManagementUseCase useCase;

    /**
     * Creates the handler.
     *
     * @param useCase the use case it runs
     */
    public ChangeUserRoleCommandHandler(UserManagementUseCase useCase) {
        this.useCase = useCase;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String actionKey() {
        return ACTION;
    }

    /**
     * {@inheritDoc}
     *
     * <p>The arguments are the email address of the account and its new role.
     */
    @Override
    public void handle(ReceivedEmail email) {
        List<String> args = email.parseArgs();
        useCase.changeUserRole(email.getFrom(), new ChangeUserRoleCommand(args.get(0), args.get(1)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int requiredArgs() {
        return 2;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int requiredFiles() {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public CommandGuide guide() {
        return GUIDE;
    }
}
