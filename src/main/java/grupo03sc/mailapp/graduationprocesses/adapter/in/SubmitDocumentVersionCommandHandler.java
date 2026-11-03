package grupo03sc.mailapp.graduationprocesses.adapter.in;

import grupo03sc.mailapp.graduationprocesses.application.GraduationProjectService;
import grupo03sc.mailapp.graduationprocesses.domain.GraduationProject;
import grupo03sc.mailapp.graduationprocesses.domain.ProjectDocumentKind;
import grupo03sc.mailapp.messaging.application.CommandHandler;
import grupo03sc.mailapp.messaging.application.CommandReply;
import grupo03sc.mailapp.messaging.application.Sender;
import grupo03sc.mailapp.messaging.domain.MailCommand;
import java.util.Set;
import org.springframework.stereotype.Component;

/** EntregarVersion(comentario), with the project document in PDF attached: UC-004 A2 and A3. */
@Component
class SubmitDocumentVersionCommandHandler implements CommandHandler {

    static final String FORM = "EntregarVersion(comentario)";

    private final GraduationProjectService service;

    SubmitDocumentVersionCommandHandler(GraduationProjectService service) {
        this.service = service;
    }

    @Override
    public String commandName() {
        return "EntregarVersion";
    }

    @Override
    public Set<String> allowedRoles() {
        return GraduationCommandSupport.STUDENT_ROLES;
    }

    @Override
    public CommandReply handle(MailCommand command, Sender sender) {
        String comment = GraduationCommandSupport.required(command.arguments(), FORM, "comentario general").getFirst();
        GraduationProject project = service.submitVersion(sender.accountId(), comment,
                GraduationCommandSupport.files(command.attachments()));
        String reply = "Versión %d del documento del proyecto entregada".formatted(project.getVersionNumber());
        if (project.getDocumentKind() == ProjectDocumentKind.FINAL_DEFENSE_VERSION) {
            reply += " como versión para la defensa final";
        }
        return new CommandReply(reply + ".");
    }
}
