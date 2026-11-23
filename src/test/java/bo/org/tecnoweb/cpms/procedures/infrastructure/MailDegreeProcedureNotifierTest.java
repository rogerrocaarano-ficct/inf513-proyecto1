package bo.org.tecnoweb.cpms.procedures.infrastructure;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import bo.org.tecnoweb.cpms.mail.application.EmailSender;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProcedureRequirementReplacedEvent;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProcedureOpenedEvent;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

/** Verifies event publication and listener email behavior for UC-002. */
class MailDegreeProcedureNotifierTest {
    private final ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
    private final EmailSender outbox = mock(EmailSender.class);
    private final MailDegreeProcedureNotifier notifier = new MailDegreeProcedureNotifier(publisher, outbox);

    /** Publishes first, then sends both FR-054 emails even if the first one fails. */
    @Test
    void openingPublishesAndAttemptsBothEmails() {
        var event = new DegreeProcedureOpenedEvent("secretaria@cpms.test", "ana@cpms.test", "Ana Pérez",
                "2026-2-123456", Instant.now());
        notifier.notifyOpened(event);

        verify(publisher).publishEvent(event);
        verifyNoInteractions(outbox);
        doThrow(new IllegalStateException("SMTP failure")).when(outbox)
                .send(eq(event.requester()), anyString(), anyString(), anyList());

        notifier.onOpened(event);

        verify(outbox).send(eq(event.requester()), eq("Trámite abierto: 2026-2-123456"),
                contains("Requisitos de apertura registrados: 4."), anyList());
        verify(outbox).send(eq(event.student()), eq("Apertura de trámite: 2026-2-123456"), contains("2026-2-123456"), anyList());
    }

    /** Confirms an FR-055 replacement to the requester. */
    @Test
    void replacementPublishesAndConfirms() {
        var event = new DegreeProcedureRequirementReplacedEvent("secretaria@cpms.test", "2026-2-123456",
                "Carta de solicitud", Instant.now());
        notifier.notifyRequirementReplaced(event);
        verify(publisher).publishEvent(event);
        verifyNoInteractions(outbox);

        notifier.onReplaced(event);

        verify(outbox).send(eq(event.requester()), eq("Requisito de apertura reemplazado: 2026-2-123456"),
                eq("Se reemplazó el requisito Carta de solicitud en el expediente 2026-2-123456."), anyList());
    }
}
