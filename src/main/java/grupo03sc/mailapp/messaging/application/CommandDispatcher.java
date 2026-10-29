package grupo03sc.mailapp.messaging.application;

import grupo03sc.mailapp.messaging.domain.CommandSubjectParser;
import grupo03sc.mailapp.messaging.domain.IncomingMail;
import grupo03sc.mailapp.messaging.domain.MailCommand;
import grupo03sc.mailapp.messaging.domain.ParsedSubject;
import grupo03sc.mailapp.messaging.domain.ProcessedMailMessage;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Handles one command email: authenticates and authorizes the sender, runs the command in a
 * transaction and records the email as processed in that same transaction (D4, D5). A rejected
 * or failed command is rolled back and still recorded as processed.
 */
@Service
public class CommandDispatcher {

    static final String UNAUTHORIZED_SENDER = "El remitente no está autorizado: su dirección de correo no pertenece a ninguna cuenta de usuario activa.";
    static final String MALFORMED_SUBJECT = "El asunto no tiene la forma Comando(arg1; arg2; …).";
    static final String UNKNOWN_COMMAND = "El comando %s no existe.";
    static final String FORBIDDEN_COMMAND = "No tiene permiso para ejecutar el comando %s.";
    static final String INTERNAL_ERROR = "No se pudo ejecutar el comando por un error interno. Inténtelo más tarde.";

    private static final Logger log = LoggerFactory.getLogger(CommandDispatcher.class);

    private final SenderAuthenticator authenticator;
    private final CommandRegistry registry;
    private final ProcessedMailMessageRepository processedMessages;
    private final TransactionTemplate transaction;
    private final Clock clock;

    CommandDispatcher(SenderAuthenticator authenticator, CommandRegistry registry,
            ProcessedMailMessageRepository processedMessages, TransactionTemplate transaction, Clock clock) {
        this.authenticator = authenticator;
        this.registry = registry;
        this.processedMessages = processedMessages;
        this.transaction = transaction;
        this.clock = clock;
    }

    public boolean alreadyProcessed(String messageId) {
        return processedMessages.existsByMessageId(messageId);
    }

    /** The reply to send to the sender once the transaction has committed. */
    public CommandReply dispatch(IncomingMail mail) {
        try {
            return transaction.execute(status -> {
                CommandReply reply = run(mail);
                markProcessed(mail);
                return reply;
            });
        } catch (CommandRejectedException e) {
            log.info("Command '{}' from {} rejected: {}", mail.subject(), mail.from(), e.getMessage());
            transaction.executeWithoutResult(status -> markProcessed(mail));
            return new CommandReply(e.getMessage());
        } catch (RuntimeException e) {
            log.error("Command '{}' from {} failed", mail.subject(), mail.from(), e);
            transaction.executeWithoutResult(status -> markProcessed(mail));
            return new CommandReply(INTERNAL_ERROR);
        }
    }

    private CommandReply run(IncomingMail mail) {
        Optional<Sender> sender = authenticator.authenticate(mail.from());
        if (sender.isEmpty()) {
            return new CommandReply(UNAUTHORIZED_SENDER);
        }
        Optional<ParsedSubject> parsed = CommandSubjectParser.parse(mail.subject());
        if (parsed.isEmpty()) {
            return new CommandReply(MALFORMED_SUBJECT);
        }
        String name = parsed.get().commandName();
        Optional<CommandHandler> handler = registry.find(name);
        if (handler.isEmpty()) {
            return new CommandReply(UNKNOWN_COMMAND.formatted(name));
        }
        if (!sender.get().hasAnyRole(handler.get().allowedRoles())) {
            return new CommandReply(FORBIDDEN_COMMAND.formatted(handler.get().commandName()));
        }
        MailCommand command = new MailCommand(handler.get().commandName(), parsed.get().arguments(),
                sender.get().email(), mail.attachments());
        return handler.get().handle(command, sender.get());
    }

    private void markProcessed(IncomingMail mail) {
        processedMessages.save(new ProcessedMailMessage(mail.messageId(), Instant.now(clock)));
    }
}
