package bo.org.tecnoweb.cpms.users.application.usermanagement;

import bo.org.tecnoweb.cpms.mail.application.EmailCommandHandler;
import bo.org.tecnoweb.cpms.mail.domain.ReceivedEmail;

/**
 * Receives the email command {@code usuarios.listar[role]} and runs the alternate flow A3 of UC-001
 * with the sender as requester. The role is required.
 */
public class ListUsersQueryHandler implements EmailCommandHandler {

    /** Name of the command in the email subject. */
    public static final String ACTION = "usuarios.listar";

    private final UserManagementUseCase useCase;

    /**
     * Creates the handler.
     *
     * @param useCase the use case it runs
     */
    public ListUsersQueryHandler(UserManagementUseCase useCase) {
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
     * <p>Maps the only argument to a {@link ListUsersQuery}.
     */
    @Override
    public void handle(ReceivedEmail email) {
        var query = new ListUsersQuery(email.parseArgs().get(0));
        var requester = email.getFrom();
        useCase.listUsers(requester, query);
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
