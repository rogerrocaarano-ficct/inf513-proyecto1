package bo.org.tecnoweb.cpms.users.application.commandhelp;

import bo.org.tecnoweb.cpms.mail.application.EmailCommandHandler;
import bo.org.tecnoweb.cpms.mail.domain.ReceivedEmail;

/**
 * Receives the email command {@code ayuda[]} and runs the help command (FR-066) with the sender as
 * requester.
 *
 * <p>It does not implement {@link bo.org.tecnoweb.cpms.users.application.DocumentedCommand}: the use
 * case describes it with {@link CommandHelpUseCase#HELP_GUIDE}, because the use case receives every
 * documented command and this handler depends on the use case.
 */
public class ShowHelpQueryHandler implements EmailCommandHandler {

    /** Name of the command in the email subject. */
    public static final String ACTION = "ayuda";

    private final CommandHelpUseCase useCase;

    /**
     * Creates the handler.
     *
     * @param useCase the use case it runs
     */
    public ShowHelpQueryHandler(CommandHelpUseCase useCase) {
        this.useCase = useCase;
    }

    /** {@inheritDoc} */
    @Override
    public String actionKey() {
        return ACTION;
    }

    /**
     * {@inheritDoc}
     *
     * <p>The command has no arguments; only the sender is passed.
     */
    @Override
    public void handle(ReceivedEmail email) {
        useCase.showHelp(email.getFrom());
    }

    /** {@inheritDoc} */
    @Override
    public int requiredArgs() {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public int requiredFiles() {
        return 0;
    }
}
