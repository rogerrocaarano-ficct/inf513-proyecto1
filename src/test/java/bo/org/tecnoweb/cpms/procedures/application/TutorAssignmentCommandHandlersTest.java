package bo.org.tecnoweb.cpms.procedures.application;

import static bo.org.tecnoweb.cpms.procedures.application.DegreeProcedureCommandHandlersTest.stored;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import bo.org.tecnoweb.cpms.mail.domain.ReceivedEmail;
import bo.org.tecnoweb.cpms.mail.domain.ReceivedEmailState;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Checks UC-003 email contracts. */
class TutorAssignmentCommandHandlersTest {
    /** Passes the title and the stored file of the PDF to A1 (FR-004, issue #9). */
    @Test
    void delegatesProfileSubmission() {
        var useCase = mock(TutorAssignmentUseCase.class);
        var handler = new RegisterInitialProfileCommandHandler(useCase);
        var id = java.util.UUID.randomUUID();
        handler.handle(ReceivedEmail.builder().withState(ReceivedEmailState.VALIDATED).fromSender("student@test")
                .withSubject("proyectos.registrarPerfil[Redes]").withAttachments(List.of(stored(id))));

        assertThat(handler.actionKey()).isEqualTo("proyectos.registrarPerfil");
        assertThat(handler.requiredArgs()).isEqualTo(1);
        assertThat(handler.requiredFiles()).isEqualTo(1);
        verify(useCase).registerInitialProfile("student@test", new RegisterInitialProfileCommand("Redes", id));
    }

    /** Passes the reference and teacher account to the basic flow (FR-003). */
    @Test
    void delegatesTutorAssignment() {
        var useCase = mock(TutorAssignmentUseCase.class);
        var handler = new AssignTutorCommandHandler(useCase);
        handler.handle(ReceivedEmail.builder().withState(ReceivedEmailState.VALIDATED).fromSender("director@test")
                .withSubject("proyectos.asignarTutor[TR-001; docente@test]"));

        assertThat(handler.actionKey()).isEqualTo("proyectos.asignarTutor");
        assertThat(handler.requiredArgs()).isEqualTo(2);
        assertThat(handler.requiredFiles()).isZero();
        verify(useCase).assignTutor("director@test", new AssignTutorCommand("TR-001", "docente@test"));
    }
}
