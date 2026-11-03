package grupo03sc.mailapp.graduationprocesses.adapter.in;

import grupo03sc.mailapp.graduationprocesses.application.GraduationProjectService;
import grupo03sc.mailapp.graduationprocesses.application.SubmittedProfile;
import grupo03sc.mailapp.messaging.application.CommandHandler;
import grupo03sc.mailapp.messaging.application.CommandReply;
import grupo03sc.mailapp.messaging.application.Sender;
import grupo03sc.mailapp.messaging.domain.MailCommand;
import java.util.Set;
import org.springframework.stereotype.Component;

/** PresentarPerfil(titulo), with the project profile in PDF attached: UC-004 basic flow and A1. */
@Component
class SubmitProjectProfileCommandHandler implements CommandHandler {

    static final String FORM = "PresentarPerfil(titulo)";

    private final GraduationProjectService service;

    SubmitProjectProfileCommandHandler(GraduationProjectService service) {
        this.service = service;
    }

    @Override
    public String commandName() {
        return "PresentarPerfil";
    }

    @Override
    public Set<String> allowedRoles() {
        return GraduationCommandSupport.STUDENT_ROLES;
    }

    @Override
    public CommandReply handle(MailCommand command, Sender sender) {
        String title = GraduationCommandSupport.required(command.arguments(), FORM, "título del proyecto").getFirst();
        SubmittedProfile submitted = service.submitProfile(sender.accountId(), title,
                GraduationCommandSupport.files(command.attachments()));
        String notice = submitted.professorNotified() ? "Se avisó al docente de Taller de Grado."
                : "No hay un docente de Taller de Grado activo a quien avisar.";
        return new CommandReply("Perfil de proyecto presentado: %s. %s".formatted(submitted.project().getTitle(), notice));
    }
}
