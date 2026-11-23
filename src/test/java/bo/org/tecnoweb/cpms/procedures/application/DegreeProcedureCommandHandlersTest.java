package bo.org.tecnoweb.cpms.procedures.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import bo.org.tecnoweb.cpms.mail.domain.Attachment;
import bo.org.tecnoweb.cpms.mail.domain.ReceivedEmail;
import bo.org.tecnoweb.cpms.mail.domain.ReceivedEmailState;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Checks both UC-002 email command contracts and their delegation. */
class DegreeProcedureCommandHandlersTest {

    /** An attachment the mail channel already stored (issue #9); handlers receive validated emails. */
    static Attachment stored(UUID storedFileId) {
        return new Attachment.Stored(storedFileId);
    }

    /** The opening handler passes all seven arguments and the four stored files in order. */
    @Test
    void delegatesOpening() {
        var useCase = mock(DegreeProcedureUseCase.class);
        var handler = new OpenDegreeProcedureCommandHandler(useCase);
        var ids = List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        var email = ReceivedEmail.builder().withState(ReceivedEmailState.VALIDATED).fromSender("secretaria@cpms.test").withSubject(
                "tramites.abrirTramite[Ana; Pérez; ; 1234; 123456; ana@cpms.test; 7123456]")
                .withAttachments(ids.stream().map(DegreeProcedureCommandHandlersTest::stored).toList());

        handler.handle(email);

        assertThat(handler.actionKey()).isEqualTo("tramites.abrirTramite");
        assertThat(handler.requiredArgs()).isEqualTo(7);
        assertThat(handler.requiredFiles()).isEqualTo(4);
        verify(useCase).open("secretaria@cpms.test",
                new OpenDegreeProcedureCommand("Ana", "Pérez", "", "1234", "123456", "ana@cpms.test", "7123456", ids));
    }

    /** The replacement handler passes its reference and the stored file of its PDF. */
    @Test
    void delegatesReplacement() {
        var useCase = mock(DegreeProcedureUseCase.class);
        var handler = new ReplaceInitialDocumentCommandHandler(useCase);
        var id = UUID.randomUUID();
        var email = ReceivedEmail.builder().withState(ReceivedEmailState.VALIDATED).fromSender("secretaria@cpms.test")
                .withSubject("tramites.reemplazarDocumento[TR-001]")
                .withAttachments(List.of(stored(id)));

        handler.handle(email);

        assertThat(handler.actionKey()).isEqualTo("tramites.reemplazarDocumento");
        assertThat(handler.requiredArgs()).isEqualTo(1);
        assertThat(handler.requiredFiles()).isEqualTo(1);
        verify(useCase).replace("secretaria@cpms.test", new ReplaceInitialDocumentCommand("TR-001", id));
    }
}
