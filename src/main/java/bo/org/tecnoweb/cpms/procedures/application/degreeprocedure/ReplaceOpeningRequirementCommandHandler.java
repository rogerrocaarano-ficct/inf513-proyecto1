package bo.org.tecnoweb.cpms.procedures.application.degreeprocedure;

import bo.org.tecnoweb.cpms.mail.application.EmailCommandHandler;
import bo.org.tecnoweb.cpms.mail.domain.ReceivedEmail;
import bo.org.tecnoweb.cpms.users.application.CommandGuide;
import bo.org.tecnoweb.cpms.users.application.DocumentedCommand;
import bo.org.tecnoweb.cpms.users.domain.RolePermission;

import java.util.Set;

/** Maps the UC-002 email command that replaces the document of an opening requirement (FR-055). */
public class ReplaceOpeningRequirementCommandHandler implements EmailCommandHandler, DocumentedCommand {
    /** Command name in the email subject. */
    public static final String ACTION = "tramites.reemplazarDocumento";

    /** Description of the command for the help reply (FR-066). */
    public static final CommandGuide GUIDE = new CommandGuide(ACTION,
            "tramites.reemplazarDocumento[referencia]",
            "Sustituir el documento de un requisito de apertura.",
            "Adjuntos: 1 PDF, con carta, cv, ci o boleta en el nombre del archivo.",
            Set.of(RolePermission.REPLACE_OPENING_REQUIREMENT));
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

    /** {@inheritDoc} */
    @Override
    public CommandGuide guide() {
        return GUIDE;
    }
}
