package grupo03sc.mailapp.messaging.domain;

import java.util.List;

/**
 * The command name and arguments read from a subject, before the command is looked up.
 */
public record ParsedSubject(String commandName, List<String> arguments) {

    public ParsedSubject {
        arguments = List.copyOf(arguments);
    }
}
