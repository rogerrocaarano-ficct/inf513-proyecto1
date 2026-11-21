package bo.org.tecnoweb.cpms.mail.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import bo.org.tecnoweb.cpms.mail.domain.File;
import bo.org.tecnoweb.cpms.mail.domain.ReceivedEmail;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

class MailAdaptersTest {

    private static final String SYSTEM = "sistema@cpms.test";

    @RegisterExtension
    static final GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP_POP3);

    private GreenMailUser system;
    private Pop3EmailInbox inbox;

    private static final MailProperties PROPERTIES = new MailProperties(SYSTEM, 10, Duration.ofSeconds(30), false,
            new MailProperties.Pop3("localhost", ServerSetupTest.POP3.getPort(), "sistema", "secret"));

    @BeforeEach
    void setUp() {
        system = greenMail.setUser(SYSTEM, "sistema", "secret");
        inbox = new Pop3EmailInbox(PROPERTIES);
    }

    private static MimeMessage message(String from, String subject, String body) throws Exception {
        MimeMessage message = new MimeMessage(Session.getInstance(new Properties()));
        message.setFrom(new InternetAddress(from));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(SYSTEM));
        message.setSubject(subject, StandardCharsets.UTF_8.name());
        message.setText(body, StandardCharsets.UTF_8.name());
        return message;
    }

    @Nested
    class Reading {

        @Test
        void pollReadsTheMessageWithoutDeletingIt() throws Exception {
            system.deliver(message("ana@uagrm.bo", "usuarios.listar[]", "hola"));

            List<ReceivedEmail> first = inbox.poll(10);
            List<ReceivedEmail> second = inbox.poll(10);

            assertThat(first).singleElement().satisfies(email -> {
                assertThat(email.getFrom()).isEqualTo("ana@uagrm.bo");
                assertThat(email.getSubject()).isEqualTo("usuarios.listar[]");
                assertThat(email.getBody()).isEqualToIgnoringNewLines("hola");
                assertThat(email.getMailboxId()).isNotBlank();
            });
            assertThat(second).singleElement()
                    .extracting(ReceivedEmail::getMailboxId).isEqualTo(first.getFirst().getMailboxId());
        }

        @Test
        void pollReadsAtMostTheRequestedMessages() throws Exception {
            for (int i = 0; i < 3; i++) {
                system.deliver(message("ana@uagrm.bo", "cmd[" + i + "]", ""));
            }

            assertThat(inbox.poll(2)).hasSize(2);
        }

        @Test
        void subjectWithAccentsIsDecoded() throws Exception {
            system.deliver(message("ana@uagrm.bo", "usuarios.crearCuenta[José Peñaranda]", ""));

            assertThat(inbox.poll(10)).singleElement()
                    .extracting(ReceivedEmail::getSubject).isEqualTo("usuarios.crearCuenta[José Peñaranda]");
        }

        @Test
        void attachmentsAreRead() throws Exception {
            MimeMessage message = new MimeMessage(Session.getInstance(new Properties()));
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom("ana@uagrm.bo");
            helper.setTo(SYSTEM);
            helper.setSubject("cmd[]");
            helper.setText("cuerpo");
            helper.addAttachment("perfil.pdf", new ByteArrayResource(new byte[] {1, 2, 3}));
            system.deliver(message);

            ReceivedEmail email = inbox.poll(10).getFirst();

            assertThat(email.getBody()).isEqualToIgnoringNewLines("cuerpo");
            assertThat(email.getAttachments()).singleElement().satisfies(file -> {
                assertThat(file.getName()).isEqualTo("perfil.pdf");
                assertThat(file.getData()).containsExactly(1, 2, 3);
            });
        }

        @Test
        void deleteRemovesOnlyTheMessageWithThatId() throws Exception {
            system.deliver(message("ana@uagrm.bo", "cmd[1]", ""));
            system.deliver(message("ana@uagrm.bo", "cmd[2]", ""));
            List<ReceivedEmail> polled = inbox.poll(10);

            inbox.delete(polled.getFirst().getMailboxId());

            assertThat(inbox.poll(10)).singleElement()
                    .extracting(ReceivedEmail::getMailboxId).isEqualTo(polled.get(1).getMailboxId());
        }
    }

    @Nested
    class AutomaticEmails {

        private void assertIgnoredAndDeleted(MimeMessage automatic) throws Exception {
            system.deliver(automatic);
            system.deliver(message("ana@uagrm.bo", "cmd[]", ""));

            assertThat(inbox.poll(10)).singleElement().extracting(ReceivedEmail::getFrom).isEqualTo("ana@uagrm.bo");
            assertThat(greenMail.getReceivedMessagesForDomain("cpms.test")).hasSize(1);
        }

        @Test
        void autoSubmittedIsIgnored() throws Exception {
            MimeMessage message = message("ana@uagrm.bo", "Fuera de la oficina", "");
            message.setHeader("Auto-Submitted", "auto-replied");
            assertIgnoredAndDeleted(message);
        }

        @Test
        void bounceIsIgnored() throws Exception {
            assertIgnoredAndDeleted(message("MAILER-DAEMON@mail.tecnoweb.org.bo", "Undelivered Mail", ""));
        }

        @Test
        void bulkMailIsIgnored() throws Exception {
            MimeMessage message = message("noticias@uagrm.bo", "Boletín", "");
            message.setHeader("Precedence", "bulk");
            assertIgnoredAndDeleted(message);
        }

        @Test
        void mailFromTheSystemItselfIsIgnored() throws Exception {
            assertIgnoredAndDeleted(message(SYSTEM, "Re: cmd[]", ""));
        }

        @Test
        void autoSubmittedNoIsNotIgnored() throws Exception {
            MimeMessage message = message("ana@uagrm.bo", "cmd[]", "");
            message.setHeader("Auto-Submitted", "no");
            system.deliver(message);

            assertThat(inbox.poll(10)).hasSize(1);
        }
    }

    @Nested
    class Sending {

        @Test
        void sendsFromTheSystemAddress() throws Exception {
            JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
            javaMailSender.setHost("localhost");
            javaMailSender.setPort(ServerSetupTest.SMTP.getPort());
            SmtpEmailSender sender = new SmtpEmailSender(javaMailSender, PROPERTIES);

            sender.send("ana@uagrm.bo", "Re: cmd[]", "El comando \"cmd\" no existe.",
                    List.of(File.builder().withName("detalle.txt").withData("x".getBytes(StandardCharsets.UTF_8))));

            MimeMessage[] received = greenMail.getReceivedMessagesForDomain("uagrm.bo");
            assertThat(received).hasSize(1);
            MimeMessage reply = received[0];
            assertThat(((InternetAddress) reply.getFrom()[0]).getAddress()).isEqualTo(SYSTEM);
            assertThat(reply.getSubject()).isEqualTo("Re: cmd[]");
            assertThat(com.icegreen.greenmail.util.GreenMailUtil.getBody(reply)).contains("no existe");
        }
    }
}
