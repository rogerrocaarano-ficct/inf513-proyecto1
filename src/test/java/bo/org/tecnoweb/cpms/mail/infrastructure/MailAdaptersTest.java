package bo.org.tecnoweb.cpms.mail.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import bo.org.tecnoweb.cpms.mail.domain.Attachment;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import bo.org.tecnoweb.cpms.mail.domain.ReceivedEmail;
import bo.org.tecnoweb.cpms.mail.domain.ReceivedEmailState;
import bo.org.tecnoweb.cpms.storage.application.StoredFileService;
import bo.org.tecnoweb.cpms.storage.domain.StoredFile;
import bo.org.tecnoweb.cpms.storage.domain.StoredFileNotFoundException;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
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
            assertThat(email.getState()).isEqualTo(ReceivedEmailState.FETCHED);
            assertThat(email.getAttachments()).singleElement().isInstanceOfSatisfying(Attachment.Content.class,
                    file -> {
                        assertThat(file.name()).isEqualTo("perfil.pdf");
                        assertThat(file.contentType()).isEqualTo("application/pdf");
                        assertThat(file.data()).containsExactly(1, 2, 3);
                    });
        }

        @Test
        void attachmentsAreReadInMessageOrder() throws Exception {
            MimeMessage message = new MimeMessage(Session.getInstance(new Properties()));
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom("ana@uagrm.bo");
            helper.setTo(SYSTEM);
            helper.setSubject("cmd[]");
            helper.setText("cuerpo");
            helper.addAttachment("carta.pdf", new ByteArrayResource(new byte[] {1}));
            helper.addAttachment("cv.pdf", new ByteArrayResource(new byte[] {2}));
            system.deliver(message);

            ReceivedEmail email = inbox.poll(10).getFirst();

            assertThat(email.getAttachments()).containsExactly(
                    new Attachment.Content("carta.pdf", "application/pdf", new byte[] {1}),
                    new Attachment.Content("cv.pdf", "application/pdf", new byte[] {2}));
        }

        @Test
        void attachmentTypeIsReducedToItsBaseType() throws Exception {
            MimeMessage message = new MimeMessage(Session.getInstance(new Properties()));
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom("ana@uagrm.bo");
            helper.setTo(SYSTEM);
            helper.setSubject("cmd[]");
            helper.setText("cuerpo");
            helper.addAttachment("perfil.pdf", new ByteArrayResource(new byte[] {1}), "Application/PDF; name=perfil.pdf");
            system.deliver(message);

            ReceivedEmail email = inbox.poll(10).getFirst();

            assertThat(email.getAttachments()).singleElement()
                    .extracting(attachment -> ((Attachment.Content) attachment).contentType())
                    .isEqualTo("application/pdf");
        }

        @Test
        void attachmentWithoutAValidTypeIsAnOctetStream() {
            assertThat(Pop3EmailInbox.baseType(null)).isEqualTo("application/octet-stream");
            assertThat(Pop3EmailInbox.baseType("  ")).isEqualTo("application/octet-stream");
            assertThat(Pop3EmailInbox.baseType("pdf")).isEqualTo("application/octet-stream");
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

        private final StoredFileService storedFiles = mock(StoredFileService.class);
        private SmtpEmailSender sender;

        @BeforeEach
        void setUpSender() {
            JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
            javaMailSender.setHost("localhost");
            javaMailSender.setPort(ServerSetupTest.SMTP.getPort());
            sender = new SmtpEmailSender(javaMailSender, PROPERTIES, storedFiles);
        }

        @Test
        void sendsFromTheSystemAddress() throws Exception {
            sender.send("ana@uagrm.bo", "Re: cmd[]", "El comando \"cmd\" no existe.", List.of());

            MimeMessage[] received = greenMail.getReceivedMessagesForDomain("uagrm.bo");
            assertThat(received).hasSize(1);
            MimeMessage reply = received[0];
            assertThat(((InternetAddress) reply.getFrom()[0]).getAddress()).isEqualTo(SYSTEM);
            assertThat(reply.getSubject()).isEqualTo("Re: cmd[]");
            assertThat(com.icegreen.greenmail.util.GreenMailUtil.getBody(reply)).contains("no existe");
        }

        @Test
        void attachesTheStoredFileWithItsNameTypeAndContent() throws Exception {
            UUID id = UUID.randomUUID();
            StoredFile metadata = mock(StoredFile.class);
            when(metadata.getOriginalName()).thenReturn("perfil.pdf");
            when(metadata.getContentType()).thenReturn("application/pdf");
            when(storedFiles.get(id)).thenReturn(metadata);
            when(storedFiles.readContent(id)).thenReturn(new byte[] {1, 2, 3});

            sender.send("taller@uagrm.bo", "Perfil de proyecto: 2026-2-123456", "Perfil adjunto.", List.of(id));

            MimeMessage sent = greenMail.getReceivedMessagesForDomain("uagrm.bo")[0];
            BodyPart attachment = ((Multipart) sent.getContent()).getBodyPart(1);
            assertThat(attachment.getFileName()).isEqualTo("perfil.pdf");
            assertThat(attachment.getContentType()).startsWith("application/pdf");
            assertThat(attachment.getInputStream().readAllBytes()).containsExactly(1, 2, 3);
        }

        @Test
        void missingStoredFileIsNotSent() {
            UUID id = UUID.randomUUID();
            when(storedFiles.get(id)).thenThrow(new StoredFileNotFoundException(id));

            assertThatThrownBy(() -> sender.send("taller@uagrm.bo", "Perfil", "Perfil adjunto.", List.of(id)))
                    .isInstanceOf(StoredFileNotFoundException.class);
            assertThat(greenMail.getReceivedMessages()).isEmpty();
        }
    }

    /**
     * Console trace of the POP3 and SMTP traffic, without command arguments or subjects.
     */
    @Nested
    @ExtendWith(OutputCaptureExtension.class)
    class Logging {

        private static final String PERSONAL_DATA_SUBJECT =
                "usuarios.crearCuenta[Ana; Pérez; ana@uagrm.bo; ESTUDIANTE; 218000001; 7654321; 70000000]";

        private final Logger inboxLogger = (Logger) LoggerFactory.getLogger(Pop3EmailInbox.class);
        private Level previousLevel;

        @BeforeEach
        void rememberLevel() {
            previousLevel = inboxLogger.getLevel();
            inboxLogger.setLevel(Level.INFO);
        }

        @AfterEach
        void restoreLevel() {
            inboxLogger.setLevel(previousLevel);
        }

        @Test
        void connectionIsLoggedAtDebugWithoutThePassword(CapturedOutput output) {
            inboxLogger.setLevel(Level.DEBUG);

            inbox.poll(10);

            assertThat(output.getOut())
                    .contains("Connecting to POP3 server localhost:" + ServerSetupTest.POP3.getPort() + " as sistema")
                    .contains("POP3 inbox has 0 messages; read 0")
                    .doesNotContain("secret");
        }

        @Test
        void emptyInboxWritesNothingAtInfo(CapturedOutput output) {
            inbox.poll(10);

            assertThat(output.getOut())
                    .doesNotContain("Connecting to POP3 server")
                    .doesNotContain("POP3 inbox has");
        }

        @Test
        void receivedEmailIsLoggedWithItsActionButNotItsArguments(CapturedOutput output) throws Exception {
            system.deliver(message("ana@uagrm.bo", PERSONAL_DATA_SUBJECT, ""));

            String uid = inbox.poll(10).getFirst().getMailboxId();

            assertThat(output.getOut())
                    .contains("Received email " + uid
                            + " from ana@uagrm.bo: action usuarios.crearcuenta (0 attachments)")
                    .contains("POP3 inbox has 1 messages; read 1")
                    .doesNotContain("Pérez")
                    .doesNotContain("218000001")
                    .doesNotContain("7654321")
                    .doesNotContain("70000000");
        }

        @Test
        void subjectWithoutCommandIsLoggedAsNoValidAction(CapturedOutput output) throws Exception {
            system.deliver(message("ana@uagrm.bo", "hola", ""));

            String uid = inbox.poll(10).getFirst().getMailboxId();

            assertThat(output.getOut())
                    .contains("Received email " + uid + " from ana@uagrm.bo: no valid action (0 attachments)");
        }

        @Test
        void ignoredAutomaticEmailIsLoggedWithItsSender(CapturedOutput output) throws Exception {
            system.deliver(message("MAILER-DAEMON@mail.tecnoweb.org.bo", "Undelivered Mail", ""));

            inbox.poll(10);

            assertThat(output.getOut())
                    .containsPattern("Ignoring automatic email \\S+ from MAILER-DAEMON@mail.tecnoweb.org.bo");
        }

        @Test
        void deletedMessageIsLogged(CapturedOutput output) throws Exception {
            system.deliver(message("ana@uagrm.bo", "cmd[]", ""));
            String uid = inbox.poll(10).getFirst().getMailboxId();

            inbox.delete(uid);

            assertThat(output.getOut()).contains("Deleted message " + uid + " from the POP3 server");
        }

        @Test
        void deletingAMissingMessageLogsNothing(CapturedOutput output) {
            inbox.delete("missing-uid");

            assertThat(output.getOut()).doesNotContain("Deleted message");
        }

        @Test
        void sentEmailIsLoggedWithoutItsSubject(CapturedOutput output) {
            JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
            javaMailSender.setHost("localhost");
            javaMailSender.setPort(ServerSetupTest.SMTP.getPort());
            SmtpEmailSender sender = new SmtpEmailSender(javaMailSender, PROPERTIES, mock(StoredFileService.class));

            sender.send("ana@uagrm.bo", "Re: " + PERSONAL_DATA_SUBJECT, "No se pudo ejecutar el comando.", List.of());

            assertThat(output.getOut())
                    .contains("Sent email to ana@uagrm.bo (0 attachments)")
                    .doesNotContain("usuarios.crearCuenta")
                    .doesNotContain("218000001")
                    .doesNotContain("7654321");
        }
    }
}
