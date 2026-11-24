package bo.org.tecnoweb.cpms.users.application.commandhelp;

import java.util.List;

/**
 * Reply of the help command (FR-066): the commands the requester may use, ordered by name.
 *
 * @param requesterEmail email address of the requester, who receives the reply
 * @param commands       the commands, always including the help command
 */
public record ShowHelpQueryResult(String requesterEmail, List<ShowHelpQueryItem> commands) {

    /**
     * Keeps an unmodifiable copy of the commands.
     */
    public ShowHelpQueryResult {
        commands = List.copyOf(commands);
    }

    /**
     * Tells whether the role of the requester may use no command besides the help command.
     *
     * @return {@code true} if the help command is the only one listed
     */
    public boolean onlyHelp() {
        return commands.size() == 1;
    }
}
