package grupo03sc.mailapp.graduationprocesses.adapter.in;

import grupo03sc.mailapp.graduationprocesses.application.GraduationProcessService;
import grupo03sc.mailapp.graduationprocesses.application.OpenedProcess;
import grupo03sc.mailapp.graduationprocesses.application.StudentData;
import grupo03sc.mailapp.messaging.application.CommandHandler;
import grupo03sc.mailapp.messaging.application.CommandReply;
import grupo03sc.mailapp.messaging.application.Sender;
import grupo03sc.mailapp.messaging.domain.MailCommand;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * AbrirTramite(nombreCompleto; correo; registro; cedula; telefono), with the four initial documents
 * attached: UC-003 basic flow and A1.
 */
@Component
class OpenGraduationProcessCommandHandler implements CommandHandler {

    static final String FORM = "AbrirTramite(nombreCompleto; correo; registro; cedula; telefono)";

    private final GraduationProcessService service;

    OpenGraduationProcessCommandHandler(GraduationProcessService service) {
        this.service = service;
    }

    @Override
    public String commandName() {
        return "AbrirTramite";
    }

    @Override
    public Set<String> allowedRoles() {
        return GraduationCommandSupport.SECRETARY_ROLES;
    }

    @Override
    public CommandReply handle(MailCommand command, Sender sender) {
        List<String> args = GraduationCommandSupport.required(command.arguments(), FORM,
                "nombre completo", "dirección de correo", "registro universitario", "cédula de identidad", "teléfono");
        OpenedProcess opened = service.openProcess(sender.accountId(),
                new StudentData(args.get(0), args.get(1), args.get(2), args.get(3), args.get(4)),
                GraduationCommandSupport.files(command.attachments()));
        String reply = "Trámite de titulación abierto: %s (%s).".formatted(opened.student().fullName(), args.get(2));
        if (opened.student().created()) {
            reply += " Se creó la cuenta de usuario del estudiante.";
        }
        return new CommandReply(reply);
    }
}
