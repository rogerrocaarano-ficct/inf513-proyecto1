package bo.org.tecnoweb.cpms.procedures.infrastructure;

import bo.org.tecnoweb.cpms.mail.application.EmailSender;
import bo.org.tecnoweb.cpms.procedures.application.projectprofileevaluation.ProjectProfileEvaluationNotifier;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProjectProfileApprovedEvent;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProjectProfileObservedEvent;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProjectProfileRegisteredEvent;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProjectProfileRejectedEvent;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/** Delivers the UC-004 notices after commit. */
@Component
class MailProjectProfileEvaluationNotifier implements ProjectProfileEvaluationNotifier {
    private static final Logger log = LoggerFactory.getLogger(MailProjectProfileEvaluationNotifier.class);
    private final ApplicationEventPublisher publisher;
    private final EmailSender outbox;

    /** Receives the event publisher and the mail port. */
    MailProjectProfileEvaluationNotifier(ApplicationEventPublisher publisher, EmailSender outbox) {
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
    public void notifyProfileApproved(DegreeProjectProfileApprovedEvent event) {
        publisher.publishEvent(event);
    }

    /** {@inheritDoc} */
    @Override
    public void notifyProfileObserved(DegreeProjectProfileObservedEvent event) {
        publisher.publishEvent(event);
    }

    /** {@inheritDoc} */
    @Override
    public void notifyProfileRejected(DegreeProjectProfileRejectedEvent event) {
        publisher.publishEvent(event);
    }

    /**
     * Sends the profile and the project title to the workshop teacher (FR-057), attaching the stored PDF
     * of the version.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void onProfileRegistered(DegreeProjectProfileRegisteredEvent event) {
        String subject = event.profile().version() == 1 ? "Perfil de proyecto: " + event.reference()
                : "Perfil de proyecto (versión " + event.profile().version() + "): " + event.reference();
        send(event.workshop(), subject,
                "El estudiante " + event.student() + " presentó el perfil del proyecto «" + event.title() + "».\n"
                        + "Evalúelo con proyectos.aprobarPerfil, proyectos.observarPerfil o proyectos.rechazarPerfil"
                        + " y la referencia " + event.reference() + ".",
                List.of(event.profile().storedFileId()));
    }

    /** Tells the student, and the director if there is one, that the profile was approved (FR-060, FR-061). */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void onProfileApproved(DegreeProjectProfileApprovedEvent event) {
        send(event.student(), "Perfil aprobado: " + event.reference(),
                "El docente de Taller de Grado aprobó el perfil de su proyecto «" + event.title() + "».", List.of());
        if (event.director() == null) {
            log.warn("No active director to notify of the approved profile of {}", event.reference());
            return;
        }
        send(event.director(), "Perfil aprobado: " + event.reference(),
                "El perfil del proyecto «" + event.title() + "» del trámite " + event.reference()
                        + " fue aprobado. Puede asignar el tutor con proyectos.asignarTutor.", List.of());
    }

    /** Sends the observations to the student (FR-060). */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void onProfileObserved(DegreeProjectProfileObservedEvent event) {
        send(event.student(), "Perfil observado: " + event.reference(),
                "El docente de Taller de Grado observó el perfil de su proyecto «" + event.title() + "».\n\n"
                        + "Observaciones:\n" + event.observations() + "\n\n"
                        + "Presente una versión corregida con proyectos.registrarPerfil.", List.of());
    }

    /** Sends the rejection and its reason to the student (FR-060). */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void onProfileRejected(DegreeProjectProfileRejectedEvent event) {
        send(event.student(), "Perfil rechazado: " + event.reference(),
                "El docente de Taller de Grado rechazó el perfil de su proyecto «" + event.title()
                        + "»: el tema debe cambiar.\n\n"
                        + "Observaciones:\n" + event.observations() + "\n\n"
                        + "Presente un perfil con otro tema con proyectos.registrarPerfil.", List.of());
    }

    private void send(String to, String subject, String body, List<UUID> attachmentIds) {
        try {
            outbox.send(to, subject, body, attachmentIds);
        } catch (RuntimeException exception) {
            log.warn("Could not send profile notification: {}", subject, exception);
        }
    }
}
