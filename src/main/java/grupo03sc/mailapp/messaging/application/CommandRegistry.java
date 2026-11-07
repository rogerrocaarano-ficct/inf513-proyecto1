package grupo03sc.mailapp.messaging.application;

import java.util.Optional;

/**
 * Interface provided by Messaging: maps command names to their handlers.
 * Every {@link CommandHandler} bean is registered on startup.
 */
public interface CommandRegistry {

    void register(String commandName, CommandHandler handler);

    Optional<CommandHandler> find(String commandName);
}
