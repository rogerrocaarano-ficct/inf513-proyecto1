package grupo03sc.mailapp.messaging.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Sends notices after commit (D6): a rolled back change sends nothing, and a send failure
 * is logged without undoing the committed change.
 */
@Component
class TransactionalNotifier implements Notifier {

    private static final Logger log = LoggerFactory.getLogger(TransactionalNotifier.class);

    private final OutgoingMail outgoingMail;

    TransactionalNotifier(OutgoingMail outgoingMail) {
        this.outgoingMail = outgoingMail;
    }

    @Override
    public void notify(String recipientEmail, String subject, String body) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    send(recipientEmail, subject, body);
                }
            });
        } else {
            send(recipientEmail, subject, body);
        }
    }

    private void send(String recipientEmail, String subject, String body) {
        try {
            outgoingMail.send(recipientEmail, subject, body);
        } catch (RuntimeException e) {
            log.error("Could not send notice '{}' to {}", subject, recipientEmail, e);
        }
    }
}
