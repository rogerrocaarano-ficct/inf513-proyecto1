package bo.org.tecnoweb.cpms.users.application.commandhelp;

/**
 * Outgoing port of the help command (FR-066): sends its reply.
 */
public interface CommandHelpNotifier {

    /**
     * Sends the commands the requester may use, after the commit.
     *
     * @param result the commands and the requester
     */
    void sendHelp(ShowHelpQueryResult result);
}
