package bo.org.tecnoweb.cpms.procedures.infrastructure;

import bo.org.tecnoweb.cpms.mail.application.EmailSender;
import bo.org.tecnoweb.cpms.procedures.application.TutorAssignmentNotifier;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProjectProfileRegisteredEvent;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProjectTutorAssignedEvent;
import java.util.List;
import java.util.UUID;
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
    public void notifyProfileRegistered(DegreeProjectProfileRegisteredEvent event) {
        publisher.publishEvent(event);
    }

    /** {@inheritDoc} */
    @Override
    public void notifyTutorAssigned(DegreeProjectTutorAssignedEvent event) {
        publisher.publishEvent(event);
    }

    /**
     * Sends the profile and project title to Taller (FR-057), attaching the stored PDF of the
     * profile. If it cannot be read or sent, the error is logged and nothing is sent (issue #9).
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void onProfileRegistered(DegreeProjectProfileRegisteredEvent event) {
        send(event.workshop(), "Perfil de proyecto: " + event.reference(),
                "El estudiante " + event.student() + " presentó el perfil del proyecto «" + event.title() + "».",
                List.of(event.profile().storedFileId()));
    }

    /** Informs the student of the designation (FR-020). */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void onTutorAssigned(DegreeProjectTutorAssignedEvent event) {
        send(event.student(), "Tutor asignado: " + event.reference(),
                "Se designó a " + event.tutorName() + " como tutor de su proyecto de grado.", List.of());
    }

    private void send(String to, String subject, String body, List<UUID> attachmentIds) {
        try {
            outbox.send(to, subject, body, attachmentIds);
        } catch (RuntimeException exception) {
            log.warn("Could not send project notification: {}", subject, exception);
        }
    }
}
