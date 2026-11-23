package bo.org.tecnoweb.cpms.procedures.infrastructure;

import static bo.org.tecnoweb.cpms.procedures.ProcedureFixtures.pdfContent;
import static org.mockito.Mockito.*;

import bo.org.tecnoweb.cpms.mail.application.EmailSender;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProjectProfileRegisteredEvent;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProjectTutorAssignedEvent;
import bo.org.tecnoweb.cpms.procedures.domain.ProjectProfile;
import bo.org.tecnoweb.cpms.procedures.ProcedureFixtures.Storage;
import java.util.UUID;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

/** Checks event publication and routing of both UC-003 notices. */
class MailTutorAssignmentNotifierTest {
    private final EmailSender outbox = mock(EmailSender.class);
    private final ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
    private final Storage storage = new Storage();
    private final MailTutorAssignmentNotifier notifier = new MailTutorAssignmentNotifier(publisher, outbox,
            storage.service);

    /** Publication defers SMTP; Taller receives the stored profile (FR-057, issue #9). */
    @Test
    void sendsProfileToWorkshop() {
        var profile = new ProjectProfile(storage.pdf("perfil.pdf"), false);
        var event = new DegreeProjectProfileRegisteredEvent("student@test", "workshop@test", "TR-001", "Redes",
                profile, Instant.now());

        notifier.notifyProfileRegistered(event);
        verify(publisher).publishEvent(event);
        verifyNoInteractions(outbox);

        notifier.onProfileRegistered(event);
        verify(outbox).send(eq("workshop@test"), eq("Perfil de proyecto: TR-001"), contains("Redes"),
                argThat(files -> files.size() == 1 && files.getFirst().getName().equals("perfil.pdf")
                        && Arrays.equals(files.getFirst().getData(), pdfContent("perfil.pdf"))));
    }

    /** A profile that cannot be read is logged and not propagated nor sent (issue #9). */
    @Test
    void unreadableProfileIsNotPropagated() {
        var event = new DegreeProjectProfileRegisteredEvent("student@test", "workshop@test", "TR-001", "Redes",
                new ProjectProfile(UUID.randomUUID(), false), Instant.now());

        notifier.onProfileRegistered(event);

        verifyNoInteractions(outbox);
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
