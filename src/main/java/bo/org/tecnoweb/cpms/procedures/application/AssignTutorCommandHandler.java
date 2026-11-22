package bo.org.tecnoweb.cpms.procedures.application;

import bo.org.tecnoweb.cpms.mail.application.EmailCommandHandler;
import bo.org.tecnoweb.cpms.mail.domain.ReceivedEmail;

/** Maps the asignarTutor command to UC-003. */
public class AssignTutorCommandHandler implements EmailCommandHandler {
    /** Command name in the email subject. */
    public static final String ACTION = "proyectos.asignarTutor";
    private final TutorAssignmentUseCase useCase;

    /** Receives the use case that handles the command. */
    public AssignTutorCommandHandler(TutorAssignmentUseCase useCase) {
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
        return 2;
    }

    /** {@inheritDoc} */
    @Override
    public int requiredFiles() {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public void handle(ReceivedEmail email) {
        var arguments = email.parseArgs();
        useCase.assignTutor(email.getFrom(), new AssignTutorCommand(
                arguments.getFirst(), arguments.get(1)));
    }
}
