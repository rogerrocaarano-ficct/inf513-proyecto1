package bo.org.tecnoweb.cpms.procedures.application;

import static bo.org.tecnoweb.cpms.procedures.ProcedureFixtures.pdf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import bo.org.tecnoweb.cpms.mail.domain.ReceivedEmail;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Checks UC-003 email contracts. */
class TutorAssignmentCommandHandlersTest {
    /** Passes the title and PDF to A1 (FR-004). */
    @Test
    void delegatesProfileSubmission() {
        var useCase = mock(TutorAssignmentUseCase.class);
        var handler = new RegisterInitialProfileCommandHandler(useCase);
        var file = pdf("perfil.pdf");
        handler.handle(ReceivedEmail.builder().fromSender("student@test")
                .withSubject("proyectos.registrarPerfil[Redes]").withAttachments(List.of(file)));

        assertThat(handler.actionKey()).isEqualTo("proyectos.registrarPerfil");
        assertThat(handler.requiredArgs()).isEqualTo(1);
        assertThat(handler.requiredFiles()).isEqualTo(1);
        verify(useCase).registerInitialProfile("student@test", new RegisterInitialProfileCommand("Redes", file));
    }

    /** Passes the reference and teacher account to the basic flow (FR-003). */
    @Test
    void delegatesTutorAssignment() {
        var useCase = mock(TutorAssignmentUseCase.class);
        var handler = new AssignTutorCommandHandler(useCase);
        handler.handle(ReceivedEmail.builder().fromSender("director@test")
                .withSubject("proyectos.asignarTutor[TR-001; docente@test]"));

        assertThat(handler.actionKey()).isEqualTo("proyectos.asignarTutor");
        assertThat(handler.requiredArgs()).isEqualTo(2);
        assertThat(handler.requiredFiles()).isZero();
        verify(useCase).assignTutor("director@test", new AssignTutorCommand("TR-001", "docente@test"));
    }
}
