package bo.org.tecnoweb.cpms.procedures.application;

import static bo.org.tecnoweb.cpms.procedures.application.DegreeProcedureCommandHandlersTest.stored;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import bo.org.tecnoweb.cpms.mail.application.EmailCommandHandler;
import bo.org.tecnoweb.cpms.mail.domain.ReceivedEmail;
import bo.org.tecnoweb.cpms.mail.domain.ReceivedEmailState;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Checks the UC-004 email contracts. */
class ProjectProfileEvaluationCommandHandlersTest {
    private final ProjectProfileEvaluationUseCase useCase = mock(ProjectProfileEvaluationUseCase.class);

    private static ReceivedEmail email(String from, String subject, String body) {
        return ReceivedEmail.builder().withState(ReceivedEmailState.VALIDATED).fromSender(from)
                .withSubject(subject).withBody(body);
    }

    private static void assertCounts(EmailCommandHandler handler, String action, int args, int files) {
        assertThat(handler.actionKey()).isEqualTo(action);
        assertThat(handler.requiredArgs()).isEqualTo(args);
        assertThat(handler.requiredFiles()).isEqualTo(files);
    }

    /** Passes the title and the stored file of the PDF to the basic flow (FR-004, FR-023). */
    @Test
    void delegatesProfileSubmission() {
        var handler = new RegisterProfileCommandHandler(useCase);
        var id = UUID.randomUUID();
        handler.handle(email("student@test", "proyectos.registrarPerfil[Redes]", null)
                .withAttachments(List.of(stored(id))));

        assertCounts(handler, "proyectos.registrarPerfil", 1, 1);
        verify(useCase).registerProfile("student@test", new RegisterProfileCommand("Redes", id));
    }

    /** Passes only the reference to A1 (FR-005). */
    @Test
    void delegatesApproval() {
        var handler = new ApproveProfileCommandHandler(useCase);
        handler.handle(email("taller@test", "proyectos.aprobarPerfil[2026-2-123456]", "ignorado"));

        assertCounts(handler, "proyectos.aprobarPerfil", 1, 0);
        verify(useCase).approveProfile("taller@test", "2026-2-123456");
    }

    /** Passes the reference and the body of the email to A2 (FR-022, NFR-003). */
    @Test
    void delegatesObservationWithTheBody() {
        var handler = new ObserveProfileCommandHandler(useCase);
        handler.handle(email("taller@test", "proyectos.observarPerfil[2026-2-123456]", "Corregir la bibliografía."));

        assertCounts(handler, "proyectos.observarPerfil", 1, 0);
        verify(useCase).observeProfile("taller@test",
                new ObserveProfileCommand("2026-2-123456", "Corregir la bibliografía."));
    }

    /** Passes the reference and the body of the email to A3 (FR-022, NFR-003). */
    @Test
    void delegatesRejectionWithTheBody() {
        var handler = new RejectProfileCommandHandler(useCase);
        handler.handle(email("taller@test", "proyectos.rechazarPerfil[2026-2-123456]", "El tema no es viable."));

        assertCounts(handler, "proyectos.rechazarPerfil", 1, 0);
        verify(useCase).rejectProfile("taller@test", new RejectProfileCommand("2026-2-123456", "El tema no es viable."));
    }
}
