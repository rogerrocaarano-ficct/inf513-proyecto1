package bo.org.tecnoweb.cpms.procedures.infrastructure;

import bo.org.tecnoweb.cpms.mail.application.EmailSender;
import bo.org.tecnoweb.cpms.mail.domain.File;
import bo.org.tecnoweb.cpms.procedures.application.TutorAssignmentNotifier;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProjectProfileRegisteredEvent;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProjectTutorAssignedEvent;
import bo.org.tecnoweb.cpms.storage.application.StoredFileService;
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
    private final StoredFileService storedFiles;

    /** Receives the event publisher, the mail port and the file storage that keeps the profiles. */
    MailTutorAssignmentNotifier(ApplicationEventPublisher publisher, EmailSender outbox,
            StoredFileService storedFiles) {
        this.publisher = publisher;
        this.outbox = outbox;
        this.storedFiles = storedFiles;
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
     * Sends the profile and project title to Taller (FR-057). The PDF is read from the file storage;
     * if it cannot be read, the error is logged and nothing is sent (issue #9).
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void onProfileRegistered(DegreeProjectProfileRegisteredEvent event) {
        File attachment;
        try {
            UUID id = event.profile().storedFileId();
            attachment = File.builder().withName(storedFiles.get(id).getOriginalName())
                    .withData(storedFiles.readContent(id));
        } catch (RuntimeException exception) {
            log.warn("Could not read the profile of {} to send it to Taller", event.reference(), exception);
            return;
        }
        send(event.workshop(), "Perfil de proyecto: " + event.reference(),
                "El estudiante " + event.student() + " presentó el perfil del proyecto «" + event.title() + "».",
                List.of(attachment));
    }

    /** Informs the student of the designation (FR-020). */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void onTutorAssigned(DegreeProjectTutorAssignedEvent event) {
        send(event.student(), "Tutor asignado: " + event.reference(),
                "Se designó a " + event.tutorName() + " como tutor de su proyecto de grado.", List.of());
    }

    private void send(String to, String subject, String body, List<File> files) {
        try {
            outbox.send(to, subject, body, files);
        } catch (RuntimeException exception) {
            log.warn("Could not send project notification: {}", subject, exception);
        }
    }
}
