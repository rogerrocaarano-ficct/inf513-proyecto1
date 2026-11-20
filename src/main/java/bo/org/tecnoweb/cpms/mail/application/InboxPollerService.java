package bo.org.tecnoweb.cpms.mail.application;

import bo.org.tecnoweb.cpms.common.DomainException;
import bo.org.tecnoweb.cpms.common.DomainValidationException;
import bo.org.tecnoweb.cpms.mail.domain.ReceivedEmail;
import bo.org.tecnoweb.cpms.mail.domain.ReceivedEmailState;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionOperations;

/**
 * Processes the mailbox in batches.
 *
 * <p>Each cycle reads and stores the new messages, validates every pending email, dispatches it to
 * the {@link EmailCommandHandler} of its command, answers errors to the sender and deletes the
 * message from the server once its email reaches a final state. Success replies are sent by each
 * use case, not here.
 *
 * <p>A message is never executed twice: emails are identified by their UIDL, and the use case and
 * the {@link ReceivedEmailState#PROCESSED} state are committed in the same transaction.
 */
public class InboxPollerService {

    private static final Logger log = LoggerFactory.getLogger(InboxPollerService.class);

    private final EmailInbox inbox;
    private final Map<String, EmailCommandHandler> handlers;
    private final int batchSize;
    private final ReceivedEmailRepository receivedEmails;
    private final EmailSender outbox;
    private final TransactionOperations transactions;

    /**
     * Creates the service.
     *
     * @param inbox          mailbox to read
     * @param handlers       command handlers of every use case; their action keys must be unique
     *                       without case
     * @param batchSize      maximum messages read per cycle
     * @param receivedEmails storage of the received emails
     * @param outbox         sender of the error replies
     * @param transactions   runs the use case and the state change in one transaction
     * @throws IllegalArgumentException if {@code batchSize} is less than 1
     * @throws IllegalStateException    if two handlers declare the same action
     */
    public InboxPollerService(EmailInbox inbox, List<EmailCommandHandler> handlers, int batchSize,
            ReceivedEmailRepository receivedEmails, EmailSender outbox, TransactionOperations transactions) {
        if (batchSize < 1) {
            throw new IllegalArgumentException("batchSize must be at least 1");
        }
        this.inbox = inbox;
        this.handlers = indexByActionKey(handlers);
        this.batchSize = batchSize;
        this.receivedEmails = receivedEmails;
        this.outbox = outbox;
        this.transactions = transactions;
    }

    private static Map<String, EmailCommandHandler> indexByActionKey(List<EmailCommandHandler> handlers) {
        Map<String, EmailCommandHandler> index = new TreeMap<>();
        for (EmailCommandHandler handler : handlers) {
            String key = handler.actionKey().strip().toLowerCase(Locale.ROOT);
            EmailCommandHandler previous = index.putIfAbsent(key, handler);
            if (previous != null) {
                throw new IllegalStateException("Two email command handlers declare the action '" + key + "': "
                        + previous.getClass().getName() + " and " + handler.getClass().getName());
            }
        }
        return index;
    }

    /**
     * Runs one polling cycle.
     *
     * <p>A failure reading the mailbox or processing one email is logged and does not stop the
     * cycle: emails left pending are taken up again by the next cycle.
     */
    public void pollAndProcess() {
        storeNewMessages();
        for (ReceivedEmail email : receivedEmails.getNotProcessed()) {
            try {
                dispatch(email);
            } catch (RuntimeException e) {
                log.error("Could not process email {} ({})", email.getId(), email.getMailboxId(), e);
            }
            if (email.getState().isFinal()) {
                deleteFromServer(email.getMailboxId());
            }
        }
    }

    /**
     * Reads one batch and stores the messages not seen before. A message read again whose email is
     * already final is only deleted from the server.
     */
    private void storeNewMessages() {
        List<ReceivedEmail> polled;
        try {
            polled = inbox.poll(batchSize);
        } catch (RuntimeException e) {
            log.error("Could not read the mailbox; pending emails are still processed", e);
            return;
        }
        for (ReceivedEmail email : polled) {
            Optional<ReceivedEmail> stored = receivedEmails.findByMailboxId(email.getMailboxId());
            if (stored.isEmpty()) {
                receivedEmails.store(email);
            } else if (stored.get().getState().isFinal()) {
                // Read again after a restart that happened before the message was deleted.
                deleteFromServer(email.getMailboxId());
            }
            // RECEIVED or VALIDATED: getNotProcessed() takes it up.
        }
    }

    private void dispatch(ReceivedEmail email) {
        Optional<EmailCommandHandler> handler = validate(email);
        if (handler.isEmpty()) {
            return;
        }
        execute(email, handler.get());
    }

    /**
     * Checks the subject form, the handler and its contract.
     *
     * @param email a pending email
     * @return the handler of the email, which is left {@link ReceivedEmailState#VALIDATED}; empty if
     *         a check failed, in which case the email was discarded and answered
     */
    private Optional<EmailCommandHandler> validate(ReceivedEmail email) {
        String action;
        List<String> args;
        try {
            action = email.parseAction();
            args = email.parseArgs();
        } catch (DomainValidationException e) {
            discard(email, "No se pudo procesar el correo. " + ReceivedEmail.SYNTAX_HELP);
            return Optional.empty();
        }

        EmailCommandHandler handler = handlers.get(action);
        if (handler == null) {
            discard(email, unknownCommandMessage(action));
            return Optional.empty();
        }
        if (args.size() != handler.requiredArgs()) {
            discard(email, "El comando \"" + action + "\" requiere " + count(handler.requiredArgs(), "argumento")
                    + " y se recibieron " + args.size() + ".");
            return Optional.empty();
        }
        int files = email.getAttachments().size();
        if (files != handler.requiredFiles()) {
            discard(email, "El comando \"" + action + "\" requiere " + count(handler.requiredFiles(), "adjunto")
                    + " y se recibieron " + files + ".");
            return Optional.empty();
        }
        if (email.getState() == ReceivedEmailState.RECEIVED) {
            email.withState(ReceivedEmailState.VALIDATED);
            receivedEmails.store(email);
        }
        return Optional.of(handler);
    }

    /**
     * Runs the use case and marks the email as processed in one transaction. On failure, marks it
     * as processed in a second transaction, so it is not retried, and answers the error.
     *
     * @param email   a validated email
     * @param handler the handler of its command
     */
    private void execute(ReceivedEmail email, EmailCommandHandler handler) {
        String action = email.parseAction();
        try {
            transactions.executeWithoutResult(status -> {
                handler.handle(email);
                email.markAsProcessed();
                receivedEmails.store(email);
            });
        } catch (DomainValidationException e) {
            markAsProcessedAfterFailure(email);
            reply(email, "No se pudo ejecutar el comando \"" + action + "\":\n" + e.getErrors().entrySet().stream()
                    .map(error -> "- " + error.getKey() + ": " + error.getValue())
                    .collect(Collectors.joining("\n")));
        } catch (DomainException e) {
            markAsProcessedAfterFailure(email);
            reply(email, "No se pudo ejecutar el comando \"" + action + "\": " + e.getMessage());
        } catch (RuntimeException e) {
            log.error("Unexpected error executing '{}' for email {} ({})", action, email.getId(), email.getMailboxId(), e);
            markAsProcessedAfterFailure(email);
            reply(email, "No se pudo ejecutar el comando \"" + action
                    + "\" por un error interno. Intente de nuevo más tarde.");
        }
    }

    private void markAsProcessedAfterFailure(ReceivedEmail email) {
        transactions.executeWithoutResult(status -> {
            email.markAsProcessed();
            receivedEmails.store(email);
        });
    }

    /**
     * Discards the email and answers the reason to its sender.
     *
     * @param email   the email that did not pass validation
     * @param message user-facing reason, in Spanish
     */
    private void discard(ReceivedEmail email, String message) {
        email.markAsDiscarded();
        receivedEmails.store(email);
        reply(email, message);
    }

    private String unknownCommandMessage(String action) {
        if (handlers.isEmpty()) {
            return "El comando \"" + action + "\" no existe. No hay comandos disponibles.";
        }
        return "El comando \"" + action + "\" no existe.\n\nComandos disponibles:\n" + handlers.keySet().stream()
                .map(key -> "- " + key)
                .collect(Collectors.joining("\n"));
    }

    /**
     * Sends a reply to the sender of the email with the subject {@code "Re: <original subject>"}.
     * A failure is logged and does not change the state of the email.
     *
     * @param email the email to answer
     * @param body  user-facing text, in Spanish
     */
    private void reply(ReceivedEmail email, String body) {
        if (email.getFrom() == null || email.getFrom().isBlank()) {
            log.warn("Email {} has no sender; the reply is not sent", email.getId());
            return;
        }
        String subject = email.getSubject() == null ? "" : email.getSubject().strip();
        try {
            outbox.send(email.getFrom(), "Re: " + subject, body, List.of());
        } catch (RuntimeException e) {
            log.error("Could not send the reply for email {} to {}", email.getId(), email.getFrom(), e);
        }
    }

    /**
     * Deletes a message from the server. A failure is logged: the message is deleted when it is
     * read again in a later cycle.
     *
     * @param mailboxId the UIDL of the message
     */
    private void deleteFromServer(String mailboxId) {
        try {
            inbox.delete(mailboxId);
        } catch (RuntimeException e) {
            log.error("Could not delete message {} from the server; it is deleted when read again", mailboxId, e);
        }
    }

    private static String count(int n, String noun) {
        return n + " " + (n == 1 ? noun : noun + "s");
    }
}
