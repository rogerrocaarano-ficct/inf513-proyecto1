package bo.org.tecnoweb.cpms.users.infrastructure;

import bo.org.tecnoweb.cpms.mail.application.EmailSender;
import bo.org.tecnoweb.cpms.users.application.commandhelp.CommandHelpNotifier;
import bo.org.tecnoweb.cpms.users.application.commandhelp.ShowHelpQueryItem;
import bo.org.tecnoweb.cpms.users.application.commandhelp.ShowHelpQueryResult;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Implementation of {@link CommandHelpNotifier} that answers by email (FR-066).
 *
 * <p>The reply is published as a Spring application event and sent only after the transaction of the
 * command commits. A failure sending it is logged and not rethrown.
 */
@Component
class MailCommandHelpNotifier implements CommandHelpNotifier {

    /** Subject of the help reply. */
    static final String SUBJECT = "Comandos que puede usar";

    private static final Logger log = LoggerFactory.getLogger(MailCommandHelpNotifier.class);

    private final ApplicationEventPublisher publisher;
    private final EmailSender outbox;

    /**
     * Creates the notifier.
     *
     * @param publisher Spring's event publisher
     * @param outbox    SMTP port of the mail subsystem
     */
    MailCommandHelpNotifier(ApplicationEventPublisher publisher, EmailSender outbox) {
        this.publisher = publisher;
        this.outbox = outbox;
    }

    /** {@inheritDoc} */
    @Override
    public void sendHelp(ShowHelpQueryResult result) {
        publisher.publishEvent(result);
    }

    /**
     * Sends the help reply to the requester, after the commit (FR-066).
     *
     * @param result the commands and the requester
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void onHelp(ShowHelpQueryResult result) {
        try {
            outbox.send(result.requesterEmail(), SUBJECT, helpBody(result), List.of());
        } catch (RuntimeException e) {
            log.error("Could not send \"{}\" to {}", SUBJECT, result.requesterEmail(), e);
        }
    }

    /**
     * Body of the help reply: one block per command with its syntax, what it is for, what it needs and,
     * for a partial permission, what the requester may do. A role with no command besides the help
     * command gets a notice first.
     *
     * @param result the commands
     * @return the body
     */
    static String helpBody(ShowHelpQueryResult result) {
        StringBuilder body = new StringBuilder();
        if (result.onlyHelp()) {
            body.append("Por ahora su cuenta no tiene comandos disponibles.\n\n");
        }
        body.append("Comandos que puede usar:\n");
        for (ShowHelpQueryItem item : result.commands()) {
            body.append('\n').append(item.syntax()).append('\n')
                    .append("  ").append(item.purpose()).append('\n');
            if (!item.notes().isBlank()) {
                body.append("  ").append(item.notes()).append('\n');
            }
            if (!item.allowedActions().isEmpty()) {
                body.append("  Puede: ").append(String.join(", ", item.allowedActions())).append(".\n");
            }
        }
        return body.toString();
    }
}
