package bo.org.tecnoweb.cpms.procedures.application.projectprofileevaluation;

import bo.org.tecnoweb.cpms.mail.application.EmailCommandHandler;
import bo.org.tecnoweb.cpms.mail.domain.ReceivedEmail;
import bo.org.tecnoweb.cpms.users.application.CommandGuide;
import bo.org.tecnoweb.cpms.users.application.DocumentedCommand;
import bo.org.tecnoweb.cpms.users.domain.RolePermission;

import java.util.Set;

/** Maps the registrarPerfil command to the basic flow of UC-004. */
public class RegisterProfileCommandHandler implements EmailCommandHandler, DocumentedCommand {
    /** Command name in the email subject. */
    public static final String ACTION = "proyectos.registrarPerfil";

    /** Description of the command for the help reply (FR-066). */
    public static final CommandGuide GUIDE = new CommandGuide(ACTION,
            "proyectos.registrarPerfil[titulo]",
            "Presentar el perfil del proyecto o una versión nueva.",
            "Adjuntos: 1 PDF con el perfil.",
            Set.of(RolePermission.REGISTER_PROJECT_PROFILE));
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

    /** {@inheritDoc} */
    @Override
    public CommandGuide guide() {
        return GUIDE;
    }
}
