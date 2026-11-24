package bo.org.tecnoweb.cpms.users.application.commandhelp;

import java.util.List;

/**
 * One command of the help reply (FR-066).
 *
 * @param syntax         the subject with the name of each argument
 * @param purpose        what the command is for
 * @param notes          the attachments or the body text it needs; empty if none
 * @param allowedActions what the requester may do with it when its role holds only some of the
 *                       command's permissions, e.g. {@code crear cuentas de estudiante}; empty otherwise
 */
public record ShowHelpQueryItem(String syntax, String purpose, String notes, List<String> allowedActions) {

    /**
     * Keeps an unmodifiable copy of the allowed actions.
     */
    public ShowHelpQueryItem {
        allowedActions = List.copyOf(allowedActions);
    }
}
