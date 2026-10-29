package grupo03sc.mailapp.messaging;

import static grupo03sc.mailapp.messaging.MailTestSupport.ADMIN;
import static grupo03sc.mailapp.messaging.MailTestSupport.SYSTEM;
import static grupo03sc.mailapp.messaging.MailTestSupport.body;
import static grupo03sc.mailapp.messaging.MailTestSupport.receivedBy;
import static grupo03sc.mailapp.messaging.MailTestSupport.sendCommand;
import static org.assertj.core.api.Assertions.assertThat;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import grupo03sc.mailapp.TestcontainersConfiguration;
import grupo03sc.mailapp.audit.application.ChangeLogEntryRepository;
import grupo03sc.mailapp.messaging.adapter.in.Pop3MailboxPoller;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import({TestcontainersConfiguration.class, TestCommands.class})
class MailCommandsTests {

    @RegisterExtension
    static GreenMailExtension greenMail = MailTestSupport.greenMail();

    @Autowired
    private Pop3MailboxPoller poller;

    @Autowired
    private ChangeLogEntryRepository changeLog;

    @Test
    void repliesToACommandQuotingTheSubject() throws Exception {
        sendCommand(ADMIN, "Eco(a; b)");

        poller.poll();

        MimeMessage reply = onlyReplyTo(ADMIN);
        assertThat(reply.getSubject()).isEqualTo("Re: Eco(a; b)");
        assertThat(reply.getFrom()[0].toString()).isEqualTo(SYSTEM);
        assertThat(reply.getHeader("Auto-Submitted")).containsExactly("auto-replied");
        assertThat(body(reply)).isEqualTo("args=[a, b] adjuntos=");
    }

    @Test
    void matchesTheCommandNameIgnoringCase() throws Exception {
        sendCommand("ADMIN@mailapp.test", "ping()");

        poller.poll();

        assertThat(body(onlyReplyTo(ADMIN))).isEqualTo("Pong");
    }

    @Test
    void processesEachEmailOnceAndRemovesItFromTheMailbox() throws Exception {
        sendCommand(ADMIN, "Ping()");

        poller.poll();
        poller.poll();

        assertThat(receivedBy(greenMail, ADMIN)).hasSize(1);
        assertThat(greenMail.getUserManager().getUser(SYSTEM)).isNotNull();
        assertThat(greenMail.getManagers().getImapHostManager()
                .getInbox(greenMail.getUserManager().getUser(SYSTEM)).getMessageCount()).isZero();
    }

    @Test
    void doesNotRunAnEmailAlreadyRecordedAsProcessed() throws Exception {
        String messageId = "<duplicado-" + java.util.UUID.randomUUID() + "@mailapp.test>";
        MailTestSupport.send(withMessageId(MailTestSupport.message(ADMIN, SYSTEM, "Ping()"), messageId));
        poller.poll();
        MailTestSupport.send(withMessageId(MailTestSupport.message(ADMIN, SYSTEM, "Ping()"), messageId));

        poller.poll();

        assertThat(receivedBy(greenMail, ADMIN)).hasSize(1);
        assertThat(greenMail.getManagers().getImapHostManager()
                .getInbox(greenMail.getUserManager().getUser(SYSTEM)).getMessageCount()).isZero();
    }

    private static MimeMessage withMessageId(MimeMessage source, String messageId) throws Exception {
        return new MimeMessage(source) {
            @Override
            protected void updateMessageID() throws jakarta.mail.MessagingException {
                setHeader("Message-ID", messageId);
            }
        };
    }

    @Test
    void passesTheAttachmentsToTheCommand() throws Exception {
        MailTestSupport.sendWithAttachment(ADMIN, "Eco(perfil.pdf)", "perfil.pdf",
                "contenido".getBytes(StandardCharsets.UTF_8));

        poller.poll();

        assertThat(body(onlyReplyTo(ADMIN))).isEqualTo("args=[perfil.pdf] adjuntos=perfil.pdf:contenido");
    }

    @Test
    void rejectsAnUnknownSender() throws Exception {
        sendCommand("intruso@mailapp.test", "Ping()");

        poller.poll();

        assertThat(body(onlyReplyTo("intruso@mailapp.test"))).startsWith("El remitente no está autorizado");
    }

    @Test
    void rejectsAMalformedSubject() throws Exception {
        sendCommand(ADMIN, "Hola");

        poller.poll();

        assertThat(body(onlyReplyTo(ADMIN))).startsWith("El asunto no tiene la forma");
    }

    @Test
    void rejectsAnUnknownCommand() throws Exception {
        sendCommand(ADMIN, "NoExiste()");

        poller.poll();

        assertThat(body(onlyReplyTo(ADMIN))).isEqualTo("El comando NoExiste no existe.");
    }

    @Test
    void rejectsACommandThatTheSenderRolesDoNotAllow() throws Exception {
        sendCommand(ADMIN, "SoloDirector()");

        poller.poll();

        assertThat(body(onlyReplyTo(ADMIN))).isEqualTo("No tiene permiso para ejecutar el comando SoloDirector.");
    }

    @Test
    void repliesWithAGenericMessageAndKeepsNoChangesWhenACommandFails() throws Exception {
        sendCommand(ADMIN, "Falla()");

        poller.poll();

        assertThat(body(onlyReplyTo(ADMIN))).startsWith("No se pudo ejecutar el comando por un error interno")
                .doesNotContain("fallo simulado");
        assertThat(changeLog.findByTargetTypeAndTargetIdOrderByChangedAt("Prueba", TestCommands.FAILING_TARGET)).isEmpty();
        assertThat(receivedBy(greenMail, "avisado@mailapp.test")).isEmpty();
    }

    @Test
    void ignoresItsOwnEmailsAndEmailsForOtherAddressesAndLeavesThem() throws Exception {
        greenMail.setUser("buzon@mailapp.test", "buzon@mailapp.test", "x");
        MailTestSupport.send(MailTestSupport.message(SYSTEM, SYSTEM, "Ping()"));

        poller.poll();

        assertThat(receivedBy(greenMail, SYSTEM)).hasSize(1);
        assertThat(greenMail.getManagers().getImapHostManager()
                .getInbox(greenMail.getUserManager().getUser(SYSTEM)).getMessageCount()).isEqualTo(1);
    }

    @Test
    void discardsAutomaticRepliesWithoutAnswering() throws Exception {
        MimeMessage autoReply = MailTestSupport.message(ADMIN, SYSTEM, "Fuera de la oficina");
        autoReply.setHeader("Auto-Submitted", "auto-replied");
        MailTestSupport.send(autoReply);

        poller.poll();

        assertThat(receivedBy(greenMail, ADMIN)).isEmpty();
        assertThat(greenMail.getManagers().getImapHostManager()
                .getInbox(greenMail.getUserManager().getUser(SYSTEM)).getMessageCount()).isZero();
    }

    private static MimeMessage onlyReplyTo(String recipient) {
        List<MimeMessage> replies = receivedBy(greenMail, recipient);
        assertThat(replies).hasSize(1);
        return replies.getFirst();
    }
}
