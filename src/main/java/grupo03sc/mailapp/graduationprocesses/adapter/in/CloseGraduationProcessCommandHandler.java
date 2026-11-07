package grupo03sc.mailapp.graduationprocesses.adapter.in;

import grupo03sc.mailapp.graduationprocesses.application.GraduationProcessService;
import grupo03sc.mailapp.graduationprocesses.application.StudentAccount;
import grupo03sc.mailapp.messaging.application.CommandHandler;
import grupo03sc.mailapp.messaging.application.CommandReply;
import grupo03sc.mailapp.messaging.application.Sender;
import grupo03sc.mailapp.messaging.domain.MailCommand;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * CerrarTramite(registro): registers the two physical copies and closes the process (UC-003 A3).
 */
@Component
class CloseGraduationProcessCommandHandler implements CommandHandler {

    static final String FORM = "CerrarTramite(registro)";

    private final GraduationProcessService service;

    CloseGraduationProcessCommandHandler(GraduationProcessService service) {
        this.service = service;
    }

    @Override
    public String commandName() {
        return "CerrarTramite";
    }

    @Override
    public Set<String> allowedRoles() {
        return GraduationCommandSupport.SECRETARY_ROLES;
    }

    @Override
    public CommandReply handle(MailCommand command, Sender sender) {
        List<String> args = GraduationCommandSupport.required(command.arguments(), FORM, "registro universitario");
        StudentAccount student = service.closeProcess(sender.accountId(), args.getFirst());
        return new CommandReply("Trámite de titulación cerrado: %s (%s).".formatted(student.fullName(), args.getFirst()));
    }
}
