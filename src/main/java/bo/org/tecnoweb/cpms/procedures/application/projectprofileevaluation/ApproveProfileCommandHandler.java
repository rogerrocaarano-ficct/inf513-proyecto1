package bo.org.tecnoweb.cpms.procedures.application.projectprofileevaluation;

import bo.org.tecnoweb.cpms.mail.application.EmailCommandHandler;
import bo.org.tecnoweb.cpms.mail.domain.ReceivedEmail;
import bo.org.tecnoweb.cpms.users.application.CommandGuide;
import bo.org.tecnoweb.cpms.users.application.DocumentedCommand;
import bo.org.tecnoweb.cpms.users.domain.RolePermission;

import java.util.Set;

/**
 * Maps the aprobarPerfil command to A1 of UC-004. Its single argument identifies the procedure, so it
 * is passed without a command record.
 */
public class ApproveProfileCommandHandler implements EmailCommandHandler, DocumentedCommand {
    /** Command name in the email subject. */
    public static final String ACTION = "proyectos.aprobarPerfil";

    /** Description of the command for the help reply (FR-066). */
    public static final CommandGuide GUIDE = new CommandGuide(ACTION,
            "proyectos.aprobarPerfil[referencia]",
            "Aprobar el perfil pendiente.",
            "",
            Set.of(RolePermission.EVALUATE_PROJECT_PROFILE));
    private final ProjectProfileEvaluationUseCase useCase;

    /** Receives the use case that handles the command. */
    public ApproveProfileCommandHandler(ProjectProfileEvaluationUseCase useCase) {
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
        useCase.approveProfile(email.getFrom(), email.parseArgs().getFirst());
    }

    /** {@inheritDoc} */
    @Override
    public CommandGuide guide() {
        return GUIDE;
    }
}
