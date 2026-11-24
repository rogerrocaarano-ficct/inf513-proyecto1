package bo.org.tecnoweb.cpms.users.application.usermanagement;

import bo.org.tecnoweb.cpms.mail.application.EmailCommandHandler;
import bo.org.tecnoweb.cpms.mail.domain.ReceivedEmail;
import bo.org.tecnoweb.cpms.users.application.CommandGuide;
import bo.org.tecnoweb.cpms.users.application.DocumentedCommand;
import bo.org.tecnoweb.cpms.users.domain.RolePermission;
import java.util.List;

/**
 * Receives the email command {@code usuarios.crearCuenta[firstName; lastName; secondLastName; ci; code;
 * email; phoneNumber; role]} and runs the basic flow of UC-001 with the sender as requester.
 */
public class SignUpUserCommandHandler implements EmailCommandHandler, DocumentedCommand {

    /** Name of the command in the email subject. */
    public static final String ACTION = "usuarios.crearCuenta";

    /** Description of the command for the help reply (FR-066). */
    public static final CommandGuide GUIDE = new CommandGuide(ACTION,
            "usuarios.crearCuenta[nombres; primerApellido; segundoApellido; ci; codigo; correo; telefono; rol]",
            "Crear una cuenta de usuario con un rol.",
            "",
            CommandGuide.forEveryRole(RolePermission::toSignUp));

    private final UserManagementUseCase useCase;

    /**
     * Creates the handler.
     *
     * @param useCase the use case it runs
     */
    public SignUpUserCommandHandler(UserManagementUseCase useCase) {
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
     * <p>Maps the arguments, in subject order, to a {@link SignUpUserCommand}.
     */
    @Override
    public void handle(ReceivedEmail email) {
        List<String> args = email.parseArgs();
        SignUpUserCommand command = new SignUpUserCommand(
                args.get(0), args.get(1), args.get(2), args.get(7),
                args.get(3), args.get(4), args.get(5), args.get(6));
        useCase.signUpUser(email.getFrom(), command);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int requiredArgs() {
        return 8;
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
