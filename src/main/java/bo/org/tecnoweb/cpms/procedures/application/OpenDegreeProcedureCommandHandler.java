package bo.org.tecnoweb.cpms.procedures.application;

import bo.org.tecnoweb.cpms.mail.application.EmailCommandHandler;
import bo.org.tecnoweb.cpms.mail.domain.ReceivedEmail;

/** Maps the UC-002 opening (FR-001) email command. */
public class OpenDegreeProcedureCommandHandler implements EmailCommandHandler {
    /** Command name in the email subject. */
    public static final String ACTION = "tramites.abrirTramite";
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
        return 7;
    }

    /** {@inheritDoc} */
    @Override
    public int requiredFiles() {
        return 4;
    }

    /** {@inheritDoc} */
    @Override
    public void handle(ReceivedEmail email) {
        var args = email.parseArgs();
        useCase.open(email.getFrom(), new OpenDegreeProcedureCommand(args.get(0), args.get(1), args.get(2),
                args.get(3), args.get(4), args.get(5), args.get(6), email.getAttachmentIds()));
    }
}
