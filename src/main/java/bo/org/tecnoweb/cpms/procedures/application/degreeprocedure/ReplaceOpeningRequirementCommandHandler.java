package bo.org.tecnoweb.cpms.procedures.application.degreeprocedure;

import bo.org.tecnoweb.cpms.mail.application.EmailCommandHandler;
import bo.org.tecnoweb.cpms.mail.domain.ReceivedEmail;

/** Maps the UC-002 email command that replaces the document of an opening requirement (FR-055). */
public class ReplaceOpeningRequirementCommandHandler implements EmailCommandHandler {
    /** Command name in the email subject. */
    public static final String ACTION = "tramites.reemplazarDocumento";
    private final DegreeProcedureUseCase useCase;

    /** Receives the use case to which the command delegates. */
    public ReplaceOpeningRequirementCommandHandler(DegreeProcedureUseCase useCase) {
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
        useCase.replaceRequirement(email.getFrom(), new ReplaceOpeningRequirementCommand(
                email.parseArgs().getFirst(), email.getAttachmentIds().getFirst()));
    }
}
