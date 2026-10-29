package grupo03sc.mailapp.messaging.application;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
class DefaultCommandRegistry implements CommandRegistry {

    private final Map<String, CommandHandler> handlers = new ConcurrentHashMap<>();

    DefaultCommandRegistry(List<CommandHandler> handlers) {
        handlers.forEach(handler -> register(handler.commandName(), handler));
    }

    @Override
    public void register(String commandName, CommandHandler handler) {
        if (handlers.putIfAbsent(key(commandName), handler) != null) {
            throw new DuplicateCommandException(commandName);
        }
    }

    @Override
    public Optional<CommandHandler> find(String commandName) {
        return Optional.ofNullable(handlers.get(key(commandName)));
    }

    private static String key(String commandName) {
        return commandName.strip().toLowerCase(Locale.ROOT);
    }
}
