package bo.org.tecnoweb.cpms.procedures.infrastructure;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import bo.org.tecnoweb.cpms.mail.application.EmailSender;
import bo.org.tecnoweb.cpms.procedures.domain.DegreeProcedureDocumentReplacedEvent;
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
                Instant.now());
        var notice = new MailDegreeProcedureNotifier.OpeningNotice(event, "TR-001");
        notifier.notifyOpened(event, "TR-001");

        verify(publisher).publishEvent(notice);
        verifyNoInteractions(outbox);
        doThrow(new IllegalStateException("SMTP failure")).when(outbox)
                .send(eq(event.requester()), anyString(), anyString(), anyList());

        notifier.onOpened(notice);

        verify(outbox).send(eq(event.requester()), eq("Trámite abierto: TR-001"), contains("Ana Pérez"), anyList());
        verify(outbox).send(eq(event.student()), eq("Apertura de trámite: TR-001"), contains("TR-001"), anyList());
    }

    /** Confirms an FR-055 replacement to the requester. */
    @Test
    void replacementPublishesAndConfirms() {
        var event = new DegreeProcedureDocumentReplacedEvent("secretaria@cpms.test", "TR-001",
                "Carta de solicitud", Instant.now());
        notifier.notifyDocumentReplaced(event);
        verify(publisher).publishEvent(event);
        verifyNoInteractions(outbox);

        notifier.onReplaced(event);

        verify(outbox).send(eq(event.requester()), eq("Documento inicial reemplazado: TR-001"),
                contains("Carta de solicitud"), anyList());
    }
}
