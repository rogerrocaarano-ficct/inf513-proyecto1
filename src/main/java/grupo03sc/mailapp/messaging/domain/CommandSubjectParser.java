package grupo03sc.mailapp.messaging.domain;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads subjects of the form {@code Command(arg1; arg2; ...)} (AD-006).
 */
public final class CommandSubjectParser {

    private static final Pattern COMMAND = Pattern.compile("^\\s*([A-Za-z][A-Za-z0-9_]*)\\s*\\((.*)\\)\\s*$", Pattern.DOTALL);

    private CommandSubjectParser() {
    }

    /** Empty when the subject does not have the command form. */
    public static Optional<ParsedSubject> parse(String subject) {
        if (subject == null) {
            return Optional.empty();
        }
        Matcher matcher = COMMAND.matcher(subject);
        if (!matcher.matches()) {
            return Optional.empty();
        }
        String inner = matcher.group(2);
        List<String> arguments = inner.isBlank()
                ? List.of()
                : Arrays.stream(inner.split(";", -1)).map(String::strip).toList();
        return Optional.of(new ParsedSubject(matcher.group(1), arguments));
    }
}
