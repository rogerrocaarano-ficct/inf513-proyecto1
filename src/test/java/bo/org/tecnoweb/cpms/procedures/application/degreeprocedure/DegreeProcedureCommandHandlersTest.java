package bo.org.tecnoweb.cpms.procedures.application.degreeprocedure;

import static bo.org.tecnoweb.cpms.procedures.ProcedureFixtures.stored;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import bo.org.tecnoweb.cpms.mail.domain.ReceivedEmail;
import bo.org.tecnoweb.cpms.mail.domain.ReceivedEmailState;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Checks both UC-002 email command contracts and their delegation. */
class DegreeProcedureCommandHandlersTest {

    /** The opening handler passes the registration number and the four stored files in order. */
    @Test
    void delegatesOpening() {
        var useCase = mock(DegreeProcedureUseCase.class);
        var handler = new OpenDegreeProcedureCommandHandler(useCase);
        var ids = List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        var email = ReceivedEmail.builder().withState(ReceivedEmailState.VALIDATED).fromSender("secretaria@cpms.test").withSubject(
                "tramites.abrirTramite[123456]")
                .withAttachments(ids.stream().map(id -> stored(id)).toList());

        handler.handle(email);

        assertThat(handler.actionKey()).isEqualTo("tramites.abrirTramite");
        assertThat(handler.requiredArgs()).isEqualTo(1);
        assertThat(handler.requiredFiles()).isEqualTo(4);
        verify(useCase).open("secretaria@cpms.test",
                new OpenDegreeProcedureCommand("123456", ids));
    }

    /** The replacement handler passes its reference and the stored file of its PDF. */
    @Test
    void delegatesReplacement() {
        var useCase = mock(DegreeProcedureUseCase.class);
        var handler = new ReplaceOpeningRequirementCommandHandler(useCase);
        var id = UUID.randomUUID();
        var email = ReceivedEmail.builder().withState(ReceivedEmailState.VALIDATED).fromSender("secretaria@cpms.test")
                .withSubject("tramites.reemplazarDocumento[2026-2-123456]")
                .withAttachments(List.of(stored(id)));

        handler.handle(email);

        assertThat(handler.actionKey()).isEqualTo("tramites.reemplazarDocumento");
        assertThat(handler.requiredArgs()).isEqualTo(1);
        assertThat(handler.requiredFiles()).isEqualTo(1);
        verify(useCase).replaceRequirement("secretaria@cpms.test", new ReplaceOpeningRequirementCommand("2026-2-123456", id));
    }
}
