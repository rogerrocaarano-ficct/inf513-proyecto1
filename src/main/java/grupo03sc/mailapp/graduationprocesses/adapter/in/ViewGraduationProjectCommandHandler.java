package grupo03sc.mailapp.graduationprocesses.adapter.in;

import grupo03sc.mailapp.graduationprocesses.application.GraduationProjectService;
import grupo03sc.mailapp.graduationprocesses.application.ProjectView;
import grupo03sc.mailapp.graduationprocesses.domain.ProjectHistoryItem;
import grupo03sc.mailapp.messaging.application.CommandHandler;
import grupo03sc.mailapp.messaging.application.CommandReply;
import grupo03sc.mailapp.messaging.application.Sender;
import grupo03sc.mailapp.messaging.domain.MailCommand;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Component;

/** ConsultarProyecto(): the student's graduation project with its history (UC-004 A4). */
@Component
class ViewGraduationProjectCommandHandler implements CommandHandler {

    static final String FORM = "ConsultarProyecto()";
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            .withZone(ZoneId.systemDefault());

    private final GraduationProjectService service;

    ViewGraduationProjectCommandHandler(GraduationProjectService service) {
        this.service = service;
    }

    @Override
    public String commandName() {
        return "ConsultarProyecto";
    }

    @Override
    public Set<String> allowedRoles() {
        return GraduationCommandSupport.STUDENT_ROLES;
    }

    @Override
    public CommandReply handle(MailCommand command, Sender sender) {
        GraduationCommandSupport.required(command.arguments(), FORM);
        Optional<ProjectView> view = service.viewProject(sender.accountId());
        return new CommandReply(view.map(ViewGraduationProjectCommandHandler::describe)
                .orElse("Todavía no presentó un perfil de proyecto."));
    }

    private static String describe(ProjectView view) {
        List<ProjectHistoryItem> history = view.history();
        StringBuilder text = new StringBuilder("Proyecto de grado: ").append(view.project().getTitle()).append("\n\n")
                .append("Documento vigente: ").append(line(history.getLast())).append("\n");
        if (history.size() > 1) {
            text.append("\nHistorial (del más reciente al más antiguo):\n");
            for (int i = history.size() - 2; i >= 0; i--) {
                text.append("- ").append(line(history.get(i))).append("\n");
            }
        }
        return text.toString();
    }

    /** One delivery: date, kind, number, comment and status. */
    private static String line(ProjectHistoryItem item) {
        StringBuilder text = new StringBuilder(DATE.format(item.submittedAt())).append(", ")
                .append(item.documentKind().label());
        if (item.versionNumber() != null) {
            text.append(", versión ").append(item.versionNumber());
        }
        if (item.generalComment() != null) {
            text.append(", \"").append(item.generalComment()).append("\"");
        }
        return text.append(": ").append(item.status().label()).toString();
    }
}
