package bo.org.tecnoweb.cpms.users.application.usermanagement;

import bo.org.tecnoweb.cpms.mail.application.EmailCommandHandler;
import bo.org.tecnoweb.cpms.mail.domain.ReceivedEmail;

/**
 * Receives the email command {@code usuarios.desactivar[email]} and runs the alternate flow A2 of
 * UC-001 with the sender as requester.
 */
public class DisableUserCommandHandler implements EmailCommandHandler {

    /** Name of the command in the email subject. */
    public static final String ACTION = "usuarios.desactivar";

    private final UserManagementUseCase useCase;

    /**
     * Creates the handler.
     *
     * @param useCase the use case it runs
     */
    public DisableUserCommandHandler(UserManagementUseCase useCase) {
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
     * <p>The only argument is the email address of the account to disable.
     */
    @Override
    public void handle(ReceivedEmail email) {
        useCase.disableUser(email.getFrom(), email.parseArgs().get(0));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int requiredArgs() {
        return 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int requiredFiles() {
        return 0;
    }
}
