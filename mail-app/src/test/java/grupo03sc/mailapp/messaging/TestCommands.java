package grupo03sc.mailapp.messaging;

import grupo03sc.mailapp.audit.application.ChangeLog;
import grupo03sc.mailapp.audit.application.FieldChange;
import grupo03sc.mailapp.messaging.application.CommandHandler;
import grupo03sc.mailapp.messaging.application.CommandRejectedException;
import grupo03sc.mailapp.messaging.application.CommandReply;
import grupo03sc.mailapp.messaging.application.Notifier;
import grupo03sc.mailapp.messaging.application.Sender;
import grupo03sc.mailapp.messaging.domain.MailAttachment;
import grupo03sc.mailapp.messaging.domain.MailCommand;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Commands that exist only in the tests, to exercise Messaging without business logic.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestCommands {

    /** Target of the change log entries written by the failing command. */
    public static final UUID FAILING_TARGET = UUID.randomUUID();

    /** Target of the change log entries written by the rejected command. */
    public static final UUID REJECTED_TARGET = UUID.randomUUID();

    @Bean
    CommandHandler echoCommand() {
        return handler("Eco", Set.of(), (command, sender) -> new CommandReply(
                "args=" + command.arguments() + " adjuntos=" + command.attachments().stream()
                        .map(a -> a.fileName() + ":" + new String(a.content())).collect(Collectors.joining(","))));
    }

    @Bean
    CommandHandler directorOnlyCommand() {
        return handler("SoloDirector", Set.of("PROGRAM_DIRECTOR"), (command, sender) -> new CommandReply("hecho"));
    }

    @Bean
    CommandHandler failingCommand(ChangeLog changeLog, Notifier notifier) {
        return handler("Falla", Set.of(), (command, sender) -> {
            changeLog.record(sender.accountId(), "Prueba", FAILING_TARGET, List.of(FieldChange.created("dato", "x")));
            notifier.notify("avisado@mailapp.test", "Aviso que no debe llegar", "x");
            throw new IllegalStateException("fallo simulado");
        });
    }

    @Bean
    CommandHandler rejectedCommand(ChangeLog changeLog, Notifier notifier) {
        return handler("Rechaza", Set.of(), (command, sender) -> {
            changeLog.record(sender.accountId(), "Prueba", REJECTED_TARGET, List.of(FieldChange.created("dato", "x")));
            notifier.notify("avisado@mailapp.test", "Aviso que no debe llegar", "x");
            throw new CommandRejectedException("Solicitud rechazada: motivo de prueba.");
        });
    }

    @Bean
    CommandHandler notifyingCommand(Notifier notifier) {
        return handler("Avisar", Set.of(), (command, sender) -> {
            notifier.notify(command.arguments().getFirst(), "Aviso de prueba", "Cuerpo del aviso");
            return new CommandReply("avisado");
        });
    }

    private static CommandHandler handler(String name, Set<String> roles, Body body) {
        return new CommandHandler() {
            @Override
            public String commandName() {
                return name;
            }

            @Override
            public Set<String> allowedRoles() {
                return roles;
            }

            @Override
            public CommandReply handle(MailCommand command, Sender sender) {
                return body.handle(command, sender);
            }
        };
    }

    interface Body {
        CommandReply handle(MailCommand command, Sender sender);
    }

    static String names(List<MailAttachment> attachments) {
        return attachments.stream().map(MailAttachment::fileName).collect(Collectors.joining(","));
    }
}
