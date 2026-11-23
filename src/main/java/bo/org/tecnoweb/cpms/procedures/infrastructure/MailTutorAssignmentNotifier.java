package bo.org.tecnoweb.cpms.procedures.infrastructure;

import bo.org.tecnoweb.cpms.mail.application.EmailSender;
import bo.org.tecnoweb.cpms.procedures.application.TutorAssignmentNotifier;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProjectTutorAssignedEvent;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/** Delivers UC-003 notices after commit. */
@Component
class MailTutorAssignmentNotifier implements TutorAssignmentNotifier {
    private static final Logger log = LoggerFactory.getLogger(MailTutorAssignmentNotifier.class);
    private final ApplicationEventPublisher publisher;
    private final EmailSender outbox;

    /** Receives the event publisher and the mail port. */
    MailTutorAssignmentNotifier(ApplicationEventPublisher publisher, EmailSender outbox) {
        this.publisher = publisher;
        this.outbox = outbox;
    }

    /** {@inheritDoc} */
    @Override
    public void notifyTutorAssigned(DegreeProjectTutorAssignedEvent event) {
        publisher.publishEvent(event);
    }

    /** Informs the student of the designation (FR-020). A failed send is logged and not rethrown. */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void onTutorAssigned(DegreeProjectTutorAssignedEvent event) {
        String subject = "Tutor asignado: " + event.reference();
        try {
            outbox.send(event.student(), subject,
                    "Se designó a " + event.tutorName() + " como tutor de su proyecto de grado.", List.of());
        } catch (RuntimeException exception) {
            log.warn("Could not send project notification: {}", subject, exception);
        }
    }
}
