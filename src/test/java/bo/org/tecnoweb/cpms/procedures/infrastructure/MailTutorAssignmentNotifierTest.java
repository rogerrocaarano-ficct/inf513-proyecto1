package bo.org.tecnoweb.cpms.procedures.infrastructure;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;

import bo.org.tecnoweb.cpms.mail.application.EmailSender;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProjectTutorAssignedEvent;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

/** Checks event publication and routing of the UC-003 notice. */
class MailTutorAssignmentNotifierTest {
    private final EmailSender outbox = mock(EmailSender.class);
    private final ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
    private final MailTutorAssignmentNotifier notifier = new MailTutorAssignmentNotifier(publisher, outbox);
    private final DegreeProjectTutorAssignedEvent event =
            new DegreeProjectTutorAssignedEvent("student@test", "2026-2-123456", "Luis Pérez", Instant.now());

    /** The designation goes to the student after commit (FR-020). */
    @Test
    void sendsTutorDesignationToStudent() {
        notifier.notifyTutorAssigned(event);
        verify(publisher).publishEvent(event);
        verifyNoInteractions(outbox);

        notifier.onTutorAssigned(event);
        verify(outbox).send(eq("student@test"), eq("Tutor asignado: 2026-2-123456"), contains("Luis Pérez"), eq(List.of()));
    }

    /** A failed send is logged and not propagated. */
    @Test
    void failedSendIsNotPropagated() {
        doThrow(new IllegalStateException("down")).when(outbox).send(anyString(), anyString(), anyString(), anyList());

        assertThatCode(() -> notifier.onTutorAssigned(event)).doesNotThrowAnyException();
    }
}
