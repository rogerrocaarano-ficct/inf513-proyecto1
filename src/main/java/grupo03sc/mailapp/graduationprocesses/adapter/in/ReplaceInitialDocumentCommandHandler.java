package grupo03sc.mailapp.graduationprocesses.adapter.in;

import grupo03sc.mailapp.graduationprocesses.application.GraduationProcessService;
import grupo03sc.mailapp.graduationprocesses.domain.InitialDocumentType;
import grupo03sc.mailapp.messaging.application.CommandHandler;
import grupo03sc.mailapp.messaging.application.CommandReply;
import grupo03sc.mailapp.messaging.application.Sender;
import grupo03sc.mailapp.messaging.domain.MailCommand;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

/** ReemplazarDocumento(registro), with one attachment: UC-003 A2. */
@Component
class ReplaceInitialDocumentCommandHandler implements CommandHandler {

    static final String FORM = "ReemplazarDocumento(registro)";

    private final GraduationProcessService service;

    ReplaceInitialDocumentCommandHandler(GraduationProcessService service) {
        this.service = service;
    }

    @Override
    public String commandName() {
        return "ReemplazarDocumento";
    }

    @Override
    public Set<String> allowedRoles() {
        return GraduationCommandSupport.SECRETARY_ROLES;
    }

    @Override
    public CommandReply handle(MailCommand command, Sender sender) {
        List<String> args = GraduationCommandSupport.required(command.arguments(), FORM, "registro universitario");
        InitialDocumentType type = service.replaceInitialDocument(sender.accountId(), args.getFirst(),
                GraduationCommandSupport.files(command.attachments()));
        return new CommandReply("Documento inicial reemplazado: " + type.label() + ".");
    }
}
