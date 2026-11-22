package bo.org.tecnoweb.cpms.procedures.application;

import bo.org.tecnoweb.cpms.mail.application.EmailCommandHandler;
import bo.org.tecnoweb.cpms.mail.domain.ReceivedEmail;

/** Maps the registrarPerfil command to UC-003. */
public class RegisterInitialProfileCommandHandler implements EmailCommandHandler {
    /** Command name in the email subject. */
    public static final String ACTION = "proyectos.registrarPerfil";
    private final TutorAssignmentUseCase useCase;

    /** Receives the use case that handles the command. */
    public RegisterInitialProfileCommandHandler(TutorAssignmentUseCase useCase) {
        this.useCase = useCase;
    }

    /** {@inheritDoc} */
    @Override
    public String actionKey() {
        return ACTION;
    }

    /** {@inheritDoc} */
    @Override
    public int requiredArgs() {
        return 1;
    }

    /** {@inheritDoc} */
    @Override
    public int requiredFiles() {
        return 1;
    }

    /** {@inheritDoc} */
    @Override
    public void handle(ReceivedEmail email) {
        useCase.registerInitialProfile(email.getFrom(), new RegisterInitialProfileCommand(
                email.parseArgs().getFirst(), email.getAttachments().getFirst()));
    }
}
