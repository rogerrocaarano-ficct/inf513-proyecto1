package bo.org.tecnoweb.cpms.procedures.infrastructure;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;

import bo.org.tecnoweb.cpms.mail.application.EmailSender;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProjectProfileApprovedEvent;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProjectProfileObservedEvent;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProjectProfileRegisteredEvent;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProjectProfileRejectedEvent;
import bo.org.tecnoweb.cpms.procedures.domain.ProjectProfile;
import bo.org.tecnoweb.cpms.procedures.domain.ProjectProfileStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

/** Checks event publication and routing of the UC-004 notices. */
class MailProjectProfileEvaluationNotifierTest {
    private static final String REFERENCE = "2026-2-123456";
    private final EmailSender outbox = mock(EmailSender.class);
    private final ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
    private final MailProjectProfileEvaluationNotifier notifier =
            new MailProjectProfileEvaluationNotifier(publisher, outbox);

    private static DegreeProjectProfileRegisteredEvent registered(UUID storedFileId, int version) {
        return new DegreeProjectProfileRegisteredEvent("student@test", "workshop@test", REFERENCE, "Redes",
                new ProjectProfile(version, storedFileId, "Redes", ProjectProfileStatus.PENDING, null, null),
                Instant.now());
    }

    /** Publication defers SMTP; Taller receives the stored PDF of the version (FR-057). */
    @Test
    void sendsProfileToWorkshop() {
        UUID profileId = UUID.randomUUID();
        var event = registered(profileId, 1);

        notifier.notifyProfileRegistered(event);
        verify(publisher).publishEvent(event);
        verifyNoInteractions(outbox);

        notifier.onProfileRegistered(event);
        verify(outbox).send(eq("workshop@test"), eq("Perfil de proyecto: " + REFERENCE), contains("Redes"),
                eq(List.of(profileId)));
    }

    /** The subject of a new version tells its number (FR-023). */
    @Test
    void namesTheVersionOfANewProfile() {
        notifier.onProfileRegistered(registered(UUID.randomUUID(), 2));

        verify(outbox).send(eq("workshop@test"), eq("Perfil de proyecto (versión 2): " + REFERENCE), anyString(),
                anyList());
    }

    /** The approval goes to the student and the director (FR-060, FR-061). */
    @Test
    void sendsApprovalToStudentAndDirector() {
        var event = new DegreeProjectProfileApprovedEvent("student@test", "director@test", REFERENCE, "Redes",
                Instant.now());

        notifier.notifyProfileApproved(event);
        verify(publisher).publishEvent(event);
        verifyNoInteractions(outbox);

        notifier.onProfileApproved(event);
        verify(outbox).send(eq("student@test"), eq("Perfil aprobado: " + REFERENCE), contains("Redes"), eq(List.of()));
        verify(outbox).send(eq("director@test"), eq("Perfil aprobado: " + REFERENCE),
                contains("proyectos.asignarTutor"), eq(List.of()));
    }

    /** Without a director only the student is notified (FR-061). */
    @Test
    void sendsApprovalOnlyToStudentWithoutDirector() {
        notifier.onProfileApproved(new DegreeProjectProfileApprovedEvent("student@test", null, REFERENCE, "Redes",
                Instant.now()));

        verify(outbox).send(eq("student@test"), anyString(), anyString(), anyList());
        verifyNoMoreInteractions(outbox);
    }

    /** The observations go to the student (FR-060). */
    @Test
    void sendsObservationsToStudent() {
        var event = new DegreeProjectProfileObservedEvent("student@test", REFERENCE, "Redes", "Corregir la forma.",
                Instant.now());

        notifier.notifyProfileObserved(event);
        verify(publisher).publishEvent(event);

        notifier.onProfileObserved(event);
        verify(outbox).send(eq("student@test"), eq("Perfil observado: " + REFERENCE), contains("Corregir la forma."),
                eq(List.of()));
    }

    /** The rejection and its reason go to the student (FR-060). */
    @Test
    void sendsRejectionToStudent() {
        var event = new DegreeProjectProfileRejectedEvent("student@test", REFERENCE, "Redes", "Tema no viable.",
                Instant.now());

        notifier.notifyProfileRejected(event);
        verify(publisher).publishEvent(event);

        notifier.onProfileRejected(event);
        verify(outbox).send(eq("student@test"), eq("Perfil rechazado: " + REFERENCE), contains("Tema no viable."),
                eq(List.of()));
    }

    /** A failed send is logged and not propagated. */
    @Test
    void failedSendIsNotPropagated() {
        doThrow(new IllegalStateException("not found")).when(outbox).send(anyString(), anyString(), anyString(),
                anyList());

        assertThatCode(() -> notifier.onProfileRegistered(registered(UUID.randomUUID(), 1)))
                .doesNotThrowAnyException();
        assertThatCode(() -> notifier.onProfileApproved(new DegreeProjectProfileApprovedEvent("student@test",
                "director@test", REFERENCE, "Redes", Instant.now()))).doesNotThrowAnyException();
    }
}
