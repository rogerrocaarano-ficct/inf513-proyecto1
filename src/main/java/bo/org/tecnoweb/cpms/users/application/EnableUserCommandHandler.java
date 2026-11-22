package bo.org.tecnoweb.cpms.users.application;

import bo.org.tecnoweb.cpms.mail.application.EmailCommandHandler;
import bo.org.tecnoweb.cpms.mail.domain.ReceivedEmail;

/**
 * Receives the email command {@code usuarios.activar[email]} and runs the alternate flow A2 of UC-001
 * with the sender as requester.
 */
public class EnableUserCommandHandler implements EmailCommandHandler {

    /** Name of the command in the email subject. */
    public static final String ACTION = "usuarios.activar";

    private final UserManagementUseCase useCase;

    /**
     * Creates the handler.
     *
     * @param useCase the use case it runs
     */
    public EnableUserCommandHandler(UserManagementUseCase useCase) {
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
     * <p>The only argument is the email address of the account to enable.
     */
    @Override
    public void handle(ReceivedEmail email) {
        useCase.enableUser(email.getFrom(), email.parseArgs().get(0));
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
