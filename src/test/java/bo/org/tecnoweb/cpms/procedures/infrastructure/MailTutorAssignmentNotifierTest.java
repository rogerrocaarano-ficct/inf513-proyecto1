package bo.org.tecnoweb.cpms.procedures.infrastructure;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;

import bo.org.tecnoweb.cpms.mail.application.EmailSender;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProjectProfileRegisteredEvent;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProjectTutorAssignedEvent;
import bo.org.tecnoweb.cpms.procedures.domain.ProjectProfile;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

/** Checks event publication and routing of both UC-003 notices. */
class MailTutorAssignmentNotifierTest {
    private final EmailSender outbox = mock(EmailSender.class);
    private final ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
    private final MailTutorAssignmentNotifier notifier = new MailTutorAssignmentNotifier(publisher, outbox);

    private static DegreeProjectProfileRegisteredEvent profileRegistered(UUID storedFileId) {
        return new DegreeProjectProfileRegisteredEvent("student@test", "workshop@test", "TR-001", "Redes",
                new ProjectProfile(storedFileId, false), Instant.now());
    }

    /** Publication defers SMTP; Taller receives the stored profile by its id (FR-057, issue #9). */
    @Test
    void sendsProfileToWorkshop() {
        UUID profileId = UUID.randomUUID();
        var event = profileRegistered(profileId);

        notifier.notifyProfileRegistered(event);
        verify(publisher).publishEvent(event);
        verifyNoInteractions(outbox);

        notifier.onProfileRegistered(event);
        verify(outbox).send(eq("workshop@test"), eq("Perfil de proyecto: TR-001"), contains("Redes"),
                eq(List.of(profileId)));
    }

    /** A profile that cannot be read or sent is logged and not propagated (issue #9). */
    @Test
    void failedSendIsNotPropagated() {
        doThrow(new IllegalStateException("not found")).when(outbox).send(anyString(), anyString(), anyString(),
                anyList());

        assertThatCode(() -> notifier.onProfileRegistered(profileRegistered(UUID.randomUUID())))
                .doesNotThrowAnyException();
    }

    /** The designation goes to the student (FR-020). */
    @Test
    void sendsTutorDesignationToStudent() {
        var event = new DegreeProjectTutorAssignedEvent("student@test", "TR-001", "Luis Pérez", Instant.now());

        notifier.notifyTutorAssigned(event);
        verify(publisher).publishEvent(event);
        verifyNoInteractions(outbox);

        notifier.onTutorAssigned(event);
        verify(outbox).send(eq("student@test"), eq("Tutor asignado: TR-001"), contains("Luis Pérez"), eq(List.of()));
    }
}
