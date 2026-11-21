package bo.org.tecnoweb.cpms.users.infrastructure;

import bo.org.tecnoweb.cpms.mail.application.EmailSender;
import bo.org.tecnoweb.cpms.mail.domain.ReceivedEmail;
import bo.org.tecnoweb.cpms.users.application.UserManagementEventBus;
import bo.org.tecnoweb.cpms.users.domain.UserSignedUpEvent;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Implementation of {@link UserManagementEventBus} that answers and notifies by email.
 *
 * <p>Events are published as Spring application events and the emails are sent only after the
 * transaction of the use case commits: if it rolls back, no email leaves. A failure sending an email
 * is logged and does not undo the account.
 */
@Component
class MailUserManagementEventBus implements UserManagementEventBus {

    private static final Logger log = LoggerFactory.getLogger(MailUserManagementEventBus.class);

    private final ApplicationEventPublisher publisher;
    private final EmailSender outbox;

    /**
     * Creates the bus.
     *
     * @param publisher Spring's event publisher
     * @param outbox    SMTP port of the mail subsystem
     */
    MailUserManagementEventBus(ApplicationEventPublisher publisher, EmailSender outbox) {
        this.publisher = publisher;
        this.outbox = outbox;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyUserSignedUpEvent(UserSignedUpEvent event) {
        publisher.publishEvent(event);
    }

    /**
     * Sends the confirmation to the requester and the notice to the new account (FR-035), after the
     * commit.
     *
     * @param event the created account and its requester
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void onUserSignedUp(UserSignedUpEvent event) {
        send(event.requesterUserEmail(), "Cuenta creada: " + event.signedUpUserFullName(),
                "Se creó la cuenta de " + event.signedUpUserFullName() + " (" + event.signedUpUser()
                        + ") con rol " + event.signedUpUserRole() + ".\n\n"
                        + "Esa persona ya puede enviar comandos al sistema desde su dirección de correo.");
        send(event.signedUpUser(), "Su cuenta en el sistema de trámites de titulación",
                "Hola, " + event.signedUpUserFullName() + ":\n\n"
                        + event.requesterUserFullName() + " creó su cuenta en el sistema de trámites de titulación "
                        + "con rol " + event.signedUpUserRole() + ".\n\n"
                        + "Desde esta dirección (" + event.signedUpUser() + ") puede enviar solicitudes al sistema "
                        + "escribiendo el comando en el asunto del correo. "
                        + ReceivedEmail.SYNTAX_HELP);
    }

    private void send(String to, String subject, String body) {
        try {
            outbox.send(to, subject, body, List.of());
        } catch (RuntimeException e) {
            log.error("Could not send \"{}\" to {}", subject, to, e);
        }
    }
}
