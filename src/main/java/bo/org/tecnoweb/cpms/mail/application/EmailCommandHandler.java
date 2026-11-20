package bo.org.tecnoweb.cpms.mail.application;

import bo.org.tecnoweb.cpms.mail.domain.ReceivedEmail;

/**
 * Port that a use case implements to receive one command by email.
 *
 * <p>The mail subsystem does not know the use cases: {@link InboxPollerService} finds the handler of
 * each email by {@link #actionKey()} and checks {@link #requiredArgs()} and {@link #requiredFiles()}
 * before calling {@link #handle(ReceivedEmail)}.
 */
public interface EmailCommandHandler {

    /**
     * Returns the command name this handler serves. It is compared without case.
     *
     * @return the command name, e.g. {@code usuarios.crearCuenta}
     */
    String actionKey();

    /**
     * Executes the use case for the email.
     *
     * <p>Runs inside the transaction that also marks the email as processed. Success replies and
     * notifications are sent by the use case after the commit. A {@link
     * bo.org.tecnoweb.cpms.common.DomainException} rolls back the transaction and its message is
     * sent to the sender.
     *
     * @param email the validated email; its arguments are read with {@link ReceivedEmail#parseArgs()}
     */
    void handle(ReceivedEmail email);

    /**
     * Returns the number of arguments the command needs.
     *
     * @return the exact number of arguments
     */
    int requiredArgs();

    /**
     * Returns the number of attachments the command needs.
     *
     * @return the exact number of attachments
     */
    int requiredFiles();
}
