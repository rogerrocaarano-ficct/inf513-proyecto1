package bo.org.tecnoweb.cpms.procedures.application;

import static bo.org.tecnoweb.cpms.procedures.ProcedureFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import bo.org.tecnoweb.cpms.mail.domain.ReceivedEmail;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Checks both UC-002 email command contracts and their delegation. */
class DegreeProcedureCommandHandlersTest {
    /** The opening handler passes all seven arguments and four attachments. */
    @Test
    void delegatesOpening() {
        var useCase = mock(DegreeProcedureUseCase.class);
        var handler = new OpenDegreeProcedureCommandHandler(useCase);
        var files = documents();
        var email = ReceivedEmail.builder().fromSender("secretaria@cpms.test").withSubject(
                "tramites.abrirTramite[Ana; Pérez; ; 1234; 123456; ana@cpms.test; 7123456]").withAttachments(files);

        handler.handle(email);

        assertThat(handler.actionKey()).isEqualTo("tramites.abrirTramite");
        assertThat(handler.requiredArgs()).isEqualTo(7);
        assertThat(handler.requiredFiles()).isEqualTo(4);
        verify(useCase).open("secretaria@cpms.test",
                new OpenDegreeProcedureCommand("Ana", "Pérez", "", "1234", "123456", "ana@cpms.test", "7123456", files));
    }

    /** The replacement handler passes its reference and PDF. */
    @Test
    void delegatesReplacement() {
        var useCase = mock(DegreeProcedureUseCase.class);
        var handler = new ReplaceInitialDocumentCommandHandler(useCase);
        var file = pdf("ci.pdf");
        var email = ReceivedEmail.builder().fromSender("secretaria@cpms.test")
                .withSubject("tramites.reemplazarDocumento[TR-001]")
                .withAttachments(List.of(file));

        handler.handle(email);

        assertThat(handler.actionKey()).isEqualTo("tramites.reemplazarDocumento");
        assertThat(handler.requiredArgs()).isEqualTo(1);
        assertThat(handler.requiredFiles()).isEqualTo(1);
        verify(useCase).replace("secretaria@cpms.test", new ReplaceInitialDocumentCommand("TR-001", file));
    }
}
