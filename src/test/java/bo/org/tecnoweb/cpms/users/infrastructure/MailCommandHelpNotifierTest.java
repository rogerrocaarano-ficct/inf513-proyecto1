package bo.org.tecnoweb.cpms.users.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import bo.org.tecnoweb.cpms.mail.application.EmailSender;
import bo.org.tecnoweb.cpms.users.application.commandhelp.ShowHelpQueryItem;
import bo.org.tecnoweb.cpms.users.application.commandhelp.ShowHelpQueryResult;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

/** Text and delivery of the help reply (FR-066). */
class MailCommandHelpNotifierTest {

    private final ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
    private final EmailSender outbox = mock(EmailSender.class);
    private final MailCommandHelpNotifier notifier = new MailCommandHelpNotifier(publisher, outbox);

    private static final ShowHelpQueryItem HELP =
            new ShowHelpQueryItem("ayuda[]", "Ver los comandos que puede usar.", "", List.of());

    @Test
    void bodyWithAttachmentsAndAPartialPermission() {
        ShowHelpQueryResult result = new ShowHelpQueryResult("secretaria@uagrm.bo", List.of(HELP,
                new ShowHelpQueryItem("tramites.abrirTramite[registro]",
                        "Abrir el trámite de titulación de un estudiante.", "Adjuntos: 4 PDF.", List.of()),
                new ShowHelpQueryItem("usuarios.crearCuenta[rol]", "Crear una cuenta de usuario con un rol.", "",
                        List.of("crear cuentas de estudiante"))));

        assertThat(MailCommandHelpNotifier.helpBody(result)).isEqualTo("""
                Comandos que puede usar:

                ayuda[]
                  Ver los comandos que puede usar.

                tramites.abrirTramite[registro]
                  Abrir el trámite de titulación de un estudiante.
                  Adjuntos: 4 PDF.

                usuarios.crearCuenta[rol]
                  Crear una cuenta de usuario con un rol.
                  Puede: crear cuentas de estudiante.
                """);
    }

    @Test
    void bodyOfARoleWithoutCommands() {
        ShowHelpQueryResult result = new ShowHelpQueryResult("docente@uagrm.bo", List.of(HELP));

        assertThat(MailCommandHelpNotifier.helpBody(result)).isEqualTo("""
                Por ahora su cuenta no tiene comandos disponibles.

                Comandos que puede usar:

                ayuda[]
                  Ver los comandos que puede usar.
                """);
    }

    @Test
    void publishesTheResultAndSendsItToTheRequesterAfterTheCommit() {
        ShowHelpQueryResult result = new ShowHelpQueryResult("ana@uagrm.bo", List.of(HELP));

        notifier.sendHelp(result);
        notifier.onHelp(result);

        verify(publisher).publishEvent(result);
        verify(outbox).send("ana@uagrm.bo", "Comandos que puede usar", MailCommandHelpNotifier.helpBody(result),
                List.of());
    }

    @Test
    void aFailedSendIsNotRethrown() {
        doThrow(new IllegalStateException("SMTP caído")).when(outbox).send(anyString(), anyString(), anyString(),
                anyList());

        assertThatCode(() -> notifier.onHelp(new ShowHelpQueryResult("ana@uagrm.bo", List.of(HELP))))
                .doesNotThrowAnyException();
        verify(outbox).send(anyString(), anyString(), anyString(), any());
    }
}
