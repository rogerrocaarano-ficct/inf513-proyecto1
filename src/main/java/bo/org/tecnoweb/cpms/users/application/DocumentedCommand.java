package bo.org.tecnoweb.cpms.users.application;

/**
 * Port that every email command handler implements so that the help command lists it (FR-066).
 *
 * <p>The handler of the help command itself does not implement it: the help use case describes it.
 */
public interface DocumentedCommand {

    /**
     * Returns the description of the command.
     *
     * @return the guide, whose {@link CommandGuide#action()} is the action of the handler
     */
    CommandGuide guide();
}
