package bo.org.tecnoweb.cpms.users.infrastructure;

import bo.org.tecnoweb.cpms.mail.application.EmailSender;
import bo.org.tecnoweb.cpms.mail.domain.ReceivedEmail;
import bo.org.tecnoweb.cpms.users.application.usermanagement.ListUsersQueryItem;
import bo.org.tecnoweb.cpms.users.application.usermanagement.ListUsersQueryResult;
import bo.org.tecnoweb.cpms.users.application.usermanagement.UserManagementNotifier;
import bo.org.tecnoweb.cpms.users.domain.UserDisabledEvent;
import bo.org.tecnoweb.cpms.users.domain.UserEnabledEvent;
import bo.org.tecnoweb.cpms.users.domain.UserRoleChangedEvent;
import bo.org.tecnoweb.cpms.users.domain.UserSignedUpEvent;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Implementation of {@link UserManagementNotifier} that answers and notifies by email.
 *
 * <p>Events are published as Spring application events and the emails are sent only after the
 * transaction of the use case commits: if it rolls back, no email leaves. A failure sending an email
 * is logged and does not undo the account.
 */
@Component
class MailUserManagementNotifier implements UserManagementNotifier {

    private static final Logger log = LoggerFactory.getLogger(MailUserManagementNotifier.class);

    private final ApplicationEventPublisher publisher;
    private final EmailSender outbox;

    /**
     * Creates the notifier.
     *
     * @param publisher Spring's event publisher
     * @param outbox    SMTP port of the mail subsystem
     */
    MailUserManagementNotifier(ApplicationEventPublisher publisher, EmailSender outbox) {
        this.publisher = publisher;
        this.outbox = outbox;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyUserSignedUp(UserSignedUpEvent event) {
        publisher.publishEvent(event);
    }

    /** {@inheritDoc} */
    @Override
    public void notifyNewAccount(UserSignedUpEvent event) {
        publisher.publishEvent(new NewAccountNotice(event));
    }

    /** Sends only the account holder's notice after UC-002 commits. */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void onNewAccountNotice(NewAccountNotice notice) {
        sendNewAccountNotice(notice.event());
    }

    /** Selects the account-holder notice when the caller confirms its own operation. */
    record NewAccountNotice(UserSignedUpEvent event) {}

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
        sendNewAccountNotice(event);
    }

    private void sendNewAccountNotice(UserSignedUpEvent event) {
        send(event.signedUpUser(), "Su cuenta en el sistema de trámites de titulación",
                "Hola, " + event.signedUpUserFullName() + ":\n\n"
                        + event.requesterUserFullName() + " creó su cuenta en el sistema de trámites de titulación "
                        + "con rol " + event.signedUpUserRole() + ".\n\n"
                        + "Desde esta dirección (" + event.signedUpUser() + ") puede enviar solicitudes al sistema "
                        + "escribiendo el comando en el asunto del correo. "
                        + ReceivedEmail.SYNTAX_HELP);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendUserList(ListUsersQueryResult queryResult) {
        publisher.publishEvent(queryResult);
    }

    /**
     * Sends the list to the requester, after the commit (UC-001 A3).
     *
     * @param result the accounts and the requester
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void onUserList(ListUsersQueryResult result) {
        send(result.requesterUserEmail(), userListSubject(result), userListBody(result));
    }

    /**
     * Subject of the list: {@code Cuentas de usuario: <role> (<count>)}.
     *
     * @param result the list
     * @return the subject
     */
    static String userListSubject(ListUsersQueryResult result) {
        return "Cuentas de usuario: " + result.role() + " (" + result.users().size() + ")";
    }

    /**
     * Body of the list: one numbered entry per account, with {@code -} for missing data and its status,
     * or a notice when there are no accounts.
     *
     * @param result the list
     * @return the body
     */
    static String userListBody(ListUsersQueryResult result) {
        if (result.users().isEmpty()) {
            return "No hay cuentas con rol " + result.role() + ".";
        }
        StringBuilder body = new StringBuilder("Cuentas con rol ").append(result.role()).append(":\n");
        int number = 1;
        for (ListUsersQueryItem user : result.users()) {
            body.append('\n').append(number++).append(". ").append(user.fullName()).append('\n')
                    .append("   Correo: ").append(user.emailAddress()).append('\n')
                    .append("   CI: ").append(orDash(user.ci()))
                    .append(" | Código: ").append(orDash(user.code()))
                    .append(" | Teléfono: ").append(orDash(user.phoneNumber())).append('\n')
                    .append("   Estado: ").append(user.active() ? "activa" : "desactivada").append('\n');
        }
        return body.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyUserDisabled(UserDisabledEvent event) {
        publisher.publishEvent(event);
    }

    /**
     * Sends the confirmation to the requester and the notice to the disabled account (FR-035), after
     * the commit.
     *
     * @param event the disabled account and its requester
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void onUserDisabled(UserDisabledEvent event) {
        userDisabledMails(event).forEach(mail -> send(mail.to(), mail.subject(), mail.body()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyUserEnabled(UserEnabledEvent event) {
        publisher.publishEvent(event);
    }

    /**
     * Sends the confirmation to the requester and the notice to the enabled account (FR-035), after the
     * commit.
     *
     * @param event the enabled account and its requester
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void onUserEnabled(UserEnabledEvent event) {
        userEnabledMails(event).forEach(mail -> send(mail.to(), mail.subject(), mail.body()));
    }

    /**
     * Emails of a disabled account: the confirmation to the requester and the notice to the account.
     *
     * @param event the disabled account and its requester
     * @return the two emails
     */
    static List<OutgoingMail> userDisabledMails(UserDisabledEvent event) {
        return List.of(
                new OutgoingMail(event.requesterUserEmail(), "Cuenta desactivada: " + event.disabledUserFullName(),
                        "Se desactivó la cuenta de " + event.disabledUserFullName() + " (" + event.disabledUserEmail()
                                + ").\n\n"
                                + "Esa persona ya no puede enviar comandos al sistema; su historial se conserva."),
                new OutgoingMail(event.disabledUserEmail(),
                        "Su cuenta en el sistema de trámites de titulación fue desactivada",
                        "Hola, " + event.disabledUserFullName() + ":\n\n"
                                + event.requesterUserFullName()
                                + " desactivó su cuenta en el sistema de trámites de titulación.\n\n"
                                + "Desde ahora el sistema no atenderá los comandos que envíe desde esta dirección. "
                                + "Si cree que es un error, comuníquese con la Dirección de Carrera."));
    }

    /**
     * Emails of an enabled account: the confirmation to the requester and the notice to the account.
     *
     * @param event the enabled account and its requester
     * @return the two emails
     */
    static List<OutgoingMail> userEnabledMails(UserEnabledEvent event) {
        return List.of(
                new OutgoingMail(event.requesterUserEmail(), "Cuenta activada: " + event.enabledUserFullName(),
                        "Se activó la cuenta de " + event.enabledUserFullName() + " (" + event.enabledUserEmail()
                                + ").\n\n"
                                + "Esa persona ya puede volver a enviar comandos al sistema."),
                new OutgoingMail(event.enabledUserEmail(),
                        "Su cuenta en el sistema de trámites de titulación fue activada",
                        "Hola, " + event.enabledUserFullName() + ":\n\n"
                                + event.requesterUserFullName()
                                + " activó nuevamente su cuenta en el sistema de trámites de titulación.\n\n"
                                + "Desde esta dirección (" + event.enabledUserEmail()
                                + ") puede volver a enviar solicitudes al sistema."));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyUserRoleChanged(UserRoleChangedEvent event) {
        publisher.publishEvent(event);
    }

    /**
     * Sends the confirmation to the requester and the notice to the account whose role changed (UC-001
     * A1, FR-035), after the commit.
     *
     * @param event the account, its previous and new roles, and the requester
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void onUserRoleChanged(UserRoleChangedEvent event) {
        userRoleChangedMails(event).forEach(mail -> send(mail.to(), mail.subject(), mail.body()));
    }

    /**
     * Emails of a role change: the confirmation to the requester and the notice to the account.
     *
     * @param event the account, its previous and new roles, and the requester
     * @return the two emails
     */
    static List<OutgoingMail> userRoleChangedMails(UserRoleChangedEvent event) {
        return List.of(
                new OutgoingMail(event.requesterUserEmail(), "Rol cambiado: " + event.changedUserFullName(),
                        "Se cambió el rol de la cuenta de " + event.changedUserFullName() + " ("
                                + event.changedUserEmail() + ") de " + event.previousRole() + " a "
                                + event.newRole() + "."),
                new OutgoingMail(event.changedUserEmail(),
                        "Su rol en el sistema de trámites de titulación cambió",
                        "Hola, " + event.changedUserFullName() + ":\n\n"
                                + event.requesterUserFullName() + " cambió el rol de su cuenta de "
                                + event.previousRole() + " a " + event.newRole() + ".\n\n"
                                + "Desde ahora puede usar los comandos de ese rol; envíe ayuda[] para verlos. "
                                + "Si cree que es un error, comuníquese con la Dirección de Carrera."));
    }

    /**
     * One email to send.
     *
     * @param to      recipient address
     * @param subject subject
     * @param body    plain-text body
     */
    record OutgoingMail(String to, String subject, String body) {
    }

    private static String orDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private void send(String to, String subject, String body) {
        try {
            outbox.send(to, subject, body, List.of());
        } catch (RuntimeException e) {
            log.error("Could not send \"{}\" to {}", subject, to, e);
        }
    }
}
