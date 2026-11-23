package bo.org.tecnoweb.cpms.procedures.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import bo.org.tecnoweb.cpms.mail.domain.ReceivedEmail;
import bo.org.tecnoweb.cpms.mail.domain.ReceivedEmailState;
import org.junit.jupiter.api.Test;

/** Checks the UC-003 email contract. */
class TutorAssignmentCommandHandlersTest {
    /** Passes the reference and teacher account to the basic flow (FR-003). */
    @Test
    void delegatesTutorAssignment() {
        var useCase = mock(TutorAssignmentUseCase.class);
        var handler = new AssignTutorCommandHandler(useCase);
        handler.handle(ReceivedEmail.builder().withState(ReceivedEmailState.VALIDATED).fromSender("director@test")
                .withSubject("proyectos.asignarTutor[2026-2-123456; docente@test]"));

        assertThat(handler.actionKey()).isEqualTo("proyectos.asignarTutor");
        assertThat(handler.requiredArgs()).isEqualTo(2);
        assertThat(handler.requiredFiles()).isZero();
        verify(useCase).assignTutor("director@test", new AssignTutorCommand("2026-2-123456", "docente@test"));
    }
}
