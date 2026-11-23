package bo.org.tecnoweb.cpms.procedures.application.projectprofileevaluation;

import bo.org.tecnoweb.cpms.mail.application.EmailCommandHandler;
import bo.org.tecnoweb.cpms.mail.domain.ReceivedEmail;

/** Maps the observarPerfil command to A2 of UC-004; the observations come from the body of the email (NFR-003). */
public class ObserveProfileCommandHandler implements EmailCommandHandler {
    /** Command name in the email subject. */
    public static final String ACTION = "proyectos.observarPerfil";
    private final ProjectProfileEvaluationUseCase useCase;

    /** Receives the use case that handles the command. */
    public ObserveProfileCommandHandler(ProjectProfileEvaluationUseCase useCase) {
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
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public void handle(ReceivedEmail email) {
        useCase.observeProfile(email.getFrom(), new ObserveProfileCommand(email.parseArgs().getFirst(), email.getBody()));
    }
}
