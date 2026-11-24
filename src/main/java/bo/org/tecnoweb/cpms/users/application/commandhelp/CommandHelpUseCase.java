package bo.org.tecnoweb.cpms.users.application.commandhelp;

import bo.org.tecnoweb.cpms.users.application.CommandGuide;
import bo.org.tecnoweb.cpms.users.application.DocumentedCommand;
import bo.org.tecnoweb.cpms.users.domain.RolePermission;
import bo.org.tecnoweb.cpms.users.domain.User;
import bo.org.tecnoweb.cpms.users.domain.UserNotAuthorizedException;
import bo.org.tecnoweb.cpms.users.domain.UserRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Help command (FR-066): answers the requester with the commands its role may use, with their syntax,
 * what they are for and, when the role holds only some of a command's permissions, what it may do
 * with it.
 *
 * <p>Shared work, not part of any use case of the catalog. It changes no data.
 */
public class CommandHelpUseCase {

    /** Description of the help command itself, which every active account may use. */
    public static final CommandGuide HELP_GUIDE = new CommandGuide(ShowHelpQueryHandler.ACTION,
            "ayuda[]",
            "Ver los comandos que puede usar.",
            "",
            Set.of());

    private static final Comparator<CommandGuide> BY_ACTION =
            Comparator.comparing(guide -> guide.action().toLowerCase(Locale.ROOT));

    private final UserRepository users;
    private final List<DocumentedCommand> commands;
    private final CommandHelpNotifier notifier;

    /**
     * Creates the use case.
     *
     * @param users    storage of the accounts
     * @param commands every command of the system except the help command
     * @param notifier where the use case sends the reply
     */
    public CommandHelpUseCase(UserRepository users, List<DocumentedCommand> commands, CommandHelpNotifier notifier) {
        this.users = users;
        this.commands = List.copyOf(commands);
        this.notifier = notifier;
    }

    /**
     * Sends the requester the commands its role may use, ordered by name, including the help command
     * (FR-066).
     *
     * <p>A command is listed when the role holds at least one of its permissions. When it holds only some
     * of them, the item carries their labels; when it holds all, or the command needs none, it carries
     * none. The reply is sent by the notifier after the commit.
     *
     * @param requesterEmail email address of the sender of the command
     * @throws UserNotAuthorizedException if the sender has no account or its account is disabled
     */
    public void showHelp(String requesterEmail) {
        User requester = requireActiveRequester(requesterEmail);

        List<ShowHelpQueryItem> items = Stream.concat(commands.stream().map(DocumentedCommand::guide),
                        Stream.of(HELP_GUIDE))
                .sorted(BY_ACTION)
                .map(guide -> toItem(requester, guide))
                .flatMap(Optional::stream)
                .toList();

        notifier.sendHelp(new ShowHelpQueryResult(requester.getEmailAddress(), items));
    }

    /**
     * Returns the item of a command for the requester, or empty if its role may not use it.
     */
    private static Optional<ShowHelpQueryItem> toItem(User requester, CommandGuide guide) {
        Set<RolePermission> permissions = guide.permissions();
        List<RolePermission> granted = permissions.stream().filter(requester::hasPermissionTo).toList();
        if (!permissions.isEmpty() && granted.isEmpty()) {
            return Optional.empty();
        }
        List<String> allowedActions = granted.size() == permissions.size()
                ? List.of()
                : granted.stream().map(RolePermission::getLabel).toList();
        return Optional.of(new ShowHelpQueryItem(guide.syntax(), guide.purpose(), guide.notes(), allowedActions));
    }

    /**
     * Returns the requester's account, which must exist and be active, with the same rejection as every
     * other command.
     */
    private User requireActiveRequester(String requesterEmail) {
        User requester = users.getByEmail(requesterEmail)
                .orElseThrow(() -> UserNotAuthorizedException.notRegistered(requesterEmail));
        if (!requester.isActive()) {
            throw UserNotAuthorizedException.disabled(requester.getEmailAddress());
        }
        return requester;
    }
}
