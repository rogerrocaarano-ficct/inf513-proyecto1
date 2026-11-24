package bo.org.tecnoweb.cpms.procedures.application.degreeprocedure;

import bo.org.tecnoweb.cpms.mail.application.EmailCommandHandler;
import bo.org.tecnoweb.cpms.mail.domain.ReceivedEmail;
import bo.org.tecnoweb.cpms.users.application.CommandGuide;
import bo.org.tecnoweb.cpms.users.application.DocumentedCommand;
import bo.org.tecnoweb.cpms.users.domain.RolePermission;

import java.util.Set;

/** Maps the UC-002 opening (FR-001) email command. */
public class OpenDegreeProcedureCommandHandler implements EmailCommandHandler, DocumentedCommand {
    /** Command name in the email subject. */
    public static final String ACTION = "tramites.abrirTramite";

    /** Description of the command for the help reply (FR-066). */
    public static final CommandGuide GUIDE = new CommandGuide(ACTION,
            "tramites.abrirTramite[registro]",
            "Abrir el trámite de titulación de un estudiante.",
            "Adjuntos: 4 PDF, uno por requisito, con carta, cv, ci o boleta en el nombre del archivo.",
            Set.of(RolePermission.OPEN_DEGREE_PROCEDURE));
    private final DegreeProcedureUseCase useCase;

    /** Receives the use case to which the command delegates. */
    public OpenDegreeProcedureCommandHandler(DegreeProcedureUseCase useCase) {
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
        return 4;
    }

    /** {@inheritDoc} */
    @Override
    public void handle(ReceivedEmail email) {
        useCase.open(email.getFrom(), new OpenDegreeProcedureCommand(email.parseArgs().get(0), email.getAttachmentIds()));
    }

    /** {@inheritDoc} */
    @Override
    public CommandGuide guide() {
        return GUIDE;
    }
}
