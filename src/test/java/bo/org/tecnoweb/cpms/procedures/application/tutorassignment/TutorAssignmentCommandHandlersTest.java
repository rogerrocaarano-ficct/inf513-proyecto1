package bo.org.tecnoweb.cpms.procedures.application.tutorassignment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import bo.org.tecnoweb.cpms.mail.domain.ReceivedEmail;
import bo.org.tecnoweb.cpms.mail.domain.ReceivedEmailState;
import bo.org.tecnoweb.cpms.users.domain.RolePermission;
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

    /** The command describes itself for the help reply (FR-066). */
    @Test
    void describesTheCommandForTheHelpReply() {
        var guide = new AssignTutorCommandHandler(mock(TutorAssignmentUseCase.class)).guide();

        assertThat(guide.action()).isEqualTo(AssignTutorCommandHandler.ACTION);
        assertThat(guide.syntax()).isEqualTo("proyectos.asignarTutor[referencia; correoDocente]");
        assertThat(guide.permissions()).containsExactly(RolePermission.ASSIGN_TUTOR);
    }
}
