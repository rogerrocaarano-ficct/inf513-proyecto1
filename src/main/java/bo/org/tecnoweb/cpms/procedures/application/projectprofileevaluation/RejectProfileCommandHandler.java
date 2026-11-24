package bo.org.tecnoweb.cpms.procedures.application.projectprofileevaluation;

import bo.org.tecnoweb.cpms.mail.application.EmailCommandHandler;
import bo.org.tecnoweb.cpms.mail.domain.ReceivedEmail;
import bo.org.tecnoweb.cpms.users.application.CommandGuide;
import bo.org.tecnoweb.cpms.users.application.DocumentedCommand;
import bo.org.tecnoweb.cpms.users.domain.RolePermission;

import java.util.Set;

/** Maps the rechazarPerfil command to A3 of UC-004; the observations come from the body of the email (NFR-003). */
public class RejectProfileCommandHandler implements EmailCommandHandler, DocumentedCommand {
    /** Command name in the email subject. */
    public static final String ACTION = "proyectos.rechazarPerfil";

    /** Description of the command for the help reply (FR-066). */
    public static final CommandGuide GUIDE = new CommandGuide(ACTION,
            "proyectos.rechazarPerfil[referencia]",
            "Pedir que el tema del proyecto cambie.",
            "Cuerpo del correo: el motivo del rechazo.",
            Set.of(RolePermission.EVALUATE_PROJECT_PROFILE));
    private final ProjectProfileEvaluationUseCase useCase;

    /** Receives the use case that handles the command. */
    public RejectProfileCommandHandler(ProjectProfileEvaluationUseCase useCase) {
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
        useCase.rejectProfile(email.getFrom(), new RejectProfileCommand(email.parseArgs().getFirst(), email.getBody()));
    }

    /** {@inheritDoc} */
    @Override
    public CommandGuide guide() {
        return GUIDE;
    }
}
