package bo.org.tecnoweb.cpms.procedures.application.tutorassignment;

import bo.org.tecnoweb.cpms.mail.application.EmailCommandHandler;
import bo.org.tecnoweb.cpms.mail.domain.ReceivedEmail;
import bo.org.tecnoweb.cpms.users.application.CommandGuide;
import bo.org.tecnoweb.cpms.users.application.DocumentedCommand;
import bo.org.tecnoweb.cpms.users.domain.RolePermission;

import java.util.Set;

/** Maps the asignarTutor command to UC-003. */
public class AssignTutorCommandHandler implements EmailCommandHandler, DocumentedCommand {
    /** Command name in the email subject. */
    public static final String ACTION = "proyectos.asignarTutor";

    /** Description of the command for the help reply (FR-066). */
    public static final CommandGuide GUIDE = new CommandGuide(ACTION,
            "proyectos.asignarTutor[referencia; correoDocente]",
            "Designar al tutor del proyecto.",
            "",
            Set.of(RolePermission.ASSIGN_TUTOR));
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

    /** {@inheritDoc} */
    @Override
    public CommandGuide guide() {
        return GUIDE;
    }
}
