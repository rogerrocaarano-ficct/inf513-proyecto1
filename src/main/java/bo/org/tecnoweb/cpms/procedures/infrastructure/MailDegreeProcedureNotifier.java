package bo.org.tecnoweb.cpms.procedures.infrastructure;

import bo.org.tecnoweb.cpms.mail.application.EmailSender;
import bo.org.tecnoweb.cpms.procedures.application.DegreeProcedureNotifier;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProcedureRequirementReplacedEvent;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProcedureOpenedEvent;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/** Sends UC-002 confirmations and notices after the transaction commits. */
@Component
class MailDegreeProcedureNotifier implements DegreeProcedureNotifier {
    private static final Logger log = LoggerFactory.getLogger(MailDegreeProcedureNotifier.class);
    private final ApplicationEventPublisher publisher;
    private final EmailSender outbox;

    /** Receives the event publisher and SMTP port. */
    MailDegreeProcedureNotifier(ApplicationEventPublisher publisher, EmailSender outbox) {
        this.publisher = publisher;
        this.outbox = outbox;
    }

    /** {@inheritDoc} */
    @Override
    public void notifyOpened(DegreeProcedureOpenedEvent event) {
        publisher.publishEvent(event);
    }

    /** {@inheritDoc} */
    @Override
    public void notifyRequirementReplaced(DegreeProcedureRequirementReplacedEvent event) {
        publisher.publishEvent(event);
    }

    /** Confirms to the requester and notifies the student after commit (FR-054). */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void onOpened(DegreeProcedureOpenedEvent event) {
        String reference = event.reference();
        send(event.requester(), "Trámite abierto: " + reference,
                "Se abrió el trámite de titulación de " + event.studentName() + ".\n"
                        + "Referencia: " + reference + "\nRequisitos de apertura registrados: 4.");
        send(event.student(), "Apertura de trámite: " + reference,
                "Su trámite de titulación se abrió con la referencia " + reference + ".");
    }

    /** Confirms the replacement to the requester after commit (FR-055). */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void onReplaced(DegreeProcedureRequirementReplacedEvent event) {
        send(event.requester(), "Requisito de apertura reemplazado: " + event.reference(),
                "Se reemplazó el requisito " + event.requirementLabel() + " en el expediente "
                        + event.reference() + ".");
    }

    private void send(String to, String subject, String body) {
        try {
            outbox.send(to, subject, body, List.of());
        } catch (RuntimeException exception) {
            log.warn("Could not send procedure notification: {}", subject, exception);
        }
    }
}
