package bo.org.tecnoweb.cpms.procedures.application.projectprofileevaluation;

import bo.org.tecnoweb.cpms.mail.application.EmailCommandHandler;
import bo.org.tecnoweb.cpms.mail.domain.ReceivedEmail;

/** Maps the registrarPerfil command to the basic flow of UC-004. */
public class RegisterProfileCommandHandler implements EmailCommandHandler {
    /** Command name in the email subject. */
    public static final String ACTION = "proyectos.registrarPerfil";
    private final ProjectProfileEvaluationUseCase useCase;

    /** Receives the use case that handles the command. */
    public RegisterProfileCommandHandler(ProjectProfileEvaluationUseCase useCase) {
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
        useCase.registerProfile(email.getFrom(), new RegisterProfileCommand(
                email.parseArgs().getFirst(), email.getAttachmentIds().getFirst()));
    }
}
