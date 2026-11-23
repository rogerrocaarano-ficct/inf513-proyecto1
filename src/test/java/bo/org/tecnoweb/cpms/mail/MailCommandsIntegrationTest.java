package bo.org.tecnoweb.cpms.mail;

import static org.assertj.core.api.Assertions.assertThat;

import bo.org.tecnoweb.cpms.TestcontainersConfiguration;
import bo.org.tecnoweb.cpms.common.DomainException;
import bo.org.tecnoweb.cpms.mail.application.EmailCommandHandler;
import bo.org.tecnoweb.cpms.mail.application.InboxPollerService;
import bo.org.tecnoweb.cpms.mail.application.ReceivedEmailRepository;
import bo.org.tecnoweb.cpms.mail.domain.Attachment;
import bo.org.tecnoweb.cpms.mail.domain.ReceivedEmail;
import bo.org.tecnoweb.cpms.mail.domain.ReceivedEmailState;
import bo.org.tecnoweb.cpms.storage.application.StoredFileService;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.activation.DataHandler;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Whole mail channel with the real adapters: GreenMail (POP3 and SMTP) and PostgreSQL.
 * The command handlers exist only in the tests; use cases add their own.
 */
@SpringBootTest(properties = {
        "cpms.mail.pop3.port=3110",
        "cpms.mail.pop3.username=sistema",
        "cpms.mail.pop3.password=secret",
        "spring.mail.port=3025"})
@Import({TestcontainersConfiguration.class, MailCommandsIntegrationTest.Handlers.class})
class MailCommandsIntegrationTest {

    private static final String SYSTEM = "sistema@cpms.test";
    private static final String ANA = "ana@uagrm.bo";

    @RegisterExtension
    static final GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP_POP3);

    @Autowired
    private InboxPollerService service;

    @Autowired
    private ReceivedEmailRepository repository;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private Handlers handlers;

    private GreenMailUser system;

    @Autowired
    private StoredFileService storedFiles;

    @Autowired
    private TransactionTemplate transaction;

    @BeforeEach
    void setUp() {
        jdbc.update("DELETE FROM received_email");
        jdbc.update("DELETE FROM degree_project");
        jdbc.update("DELETE FROM degree_procedure_requirement");
        jdbc.update("DELETE FROM stored_file");
        handlers.calls.clear();
        handlers.attachmentCalls.clear();
        system = greenMail.setUser(SYSTEM, "sistema", "secret");
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class Handlers {

        final List<List<String>> calls = new ArrayList<>();
        final List<List<UUID>> attachmentCalls = new ArrayList<>();

        @Bean
        EmailCommandHandler greetHandler() {
            return handler("prueba.saludar", 2, 0, email -> calls.add(email.parseArgs()));
        }

        @Bean
        EmailCommandHandler failingHandler() {
            return handler("prueba.fallar", 0, 0, email -> {
                throw new RuleBroken();
            });
        }

        @Bean
        EmailCommandHandler attachmentsHandler() {
            return handler("prueba.adjuntos", 0, 2, email -> attachmentCalls.add(email.getAttachmentIds()));
        }

        private static EmailCommandHandler handler(String action, int args, int files,
                java.util.function.Consumer<ReceivedEmail> body) {
            return new EmailCommandHandler() {
                @Override
                public String actionKey() {
                    return action;
                }

                @Override
                public void handle(ReceivedEmail email) {
                    body.accept(email);
                }

                @Override
                public int requiredArgs() {
                    return args;
                }

                @Override
                public int requiredFiles() {
                    return files;
                }
            };
        }
    }

    static class RuleBroken extends DomainException {
        RuleBroken() {
            super("RULE", "La regla de prueba no se cumple.");
        }
    }

    private void receive(String subject) throws Exception {
        MimeMessage message = new MimeMessage(Session.getInstance(new Properties()));
        message.setFrom(new InternetAddress(ANA));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(SYSTEM));
        message.setSubject(subject, StandardCharsets.UTF_8.name());
        message.setText("", StandardCharsets.UTF_8.name());
        system.deliver(message);
    }

    private void receive(String subject, String name, byte[] content) throws Exception {
        receive(subject, new String[] {name}, new byte[][] {content});
    }

    private void receive(String subject, String[] names, byte[][] contents) throws Exception {
        MimeMessage message = new MimeMessage(Session.getInstance(new Properties()));
        message.setFrom(new InternetAddress(ANA));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(SYSTEM));
        message.setSubject(subject, StandardCharsets.UTF_8.name());
        MimeMultipart parts = new MimeMultipart();
        MimeBodyPart body = new MimeBodyPart();
        body.setText("");
        parts.addBodyPart(body);
        for (int i = 0; i < names.length; i++) {
            MimeBodyPart attachment = new MimeBodyPart();
            attachment.setDataHandler(new DataHandler(new ByteArrayDataSource(contents[i], "application/pdf")));
            attachment.setFileName(names[i]);
            attachment.setHeader("Content-Transfer-Encoding", "base64");
            parts.addBodyPart(attachment);
        }
        message.setContent(parts);
        system.deliver(message);
    }

    private int storedFileRows() {
        return jdbc.queryForObject("SELECT count(*) FROM stored_file", Integer.class);
    }

    private List<UUID> attachmentsOf(String subject) {
        return jdbc.queryForList("""
                SELECT a.stored_file_id FROM received_email_attachment a
                JOIN received_email e ON e.id = a.received_email_id WHERE e.subject = ? ORDER BY a.position
                """, UUID.class, subject);
    }

    private List<MimeMessage> repliesToAna() {
        return List.of(greenMail.getReceivedMessagesForDomain("uagrm.bo"));
    }

    private int messagesOnServer() throws Exception {
        return greenMail.getManagers().getImapHostManager().getInbox(system).getMessageCount();
    }

    private ReceivedEmailState stateOf(String subject) {
        return jdbc.queryForObject("SELECT state FROM received_email WHERE subject = ?", String.class, subject)
                .transform(ReceivedEmailState::valueOf);
    }

    @Test
    void validCommandIsExecutedOnceWithoutAReplyAndDeletedFromTheServer() throws Exception {
        receive("RE: Prueba.Saludar[ Ana María ; Pérez ]");

        service.pollAndProcess();
        service.pollAndProcess();

        assertThat(handlers.calls).containsExactly(List.of("Ana María", "Pérez"));
        assertThat(stateOf("RE: Prueba.Saludar[ Ana María ; Pérez ]")).isEqualTo(ReceivedEmailState.PROCESSED);
        assertThat(repliesToAna()).isEmpty();
        assertThat(messagesOnServer()).isZero();
    }

    @Test
    void unknownCommandIsAnsweredWithTheAvailableCommands() throws Exception {
        receive("usuarios.borrarTodo[]");

        service.pollAndProcess();

        assertThat(stateOf("usuarios.borrarTodo[]")).isEqualTo(ReceivedEmailState.DISCARDED);
        assertThat(repliesToAna()).singleElement().satisfies(reply -> {
            assertThat(reply.getSubject()).isEqualTo("Re: usuarios.borrarTodo[]");
            assertThat(GreenMailUtil.getBody(reply)).contains("prueba.saludar").contains("prueba.fallar");
        });
        assertThat(messagesOnServer()).isZero();
    }

    @Test
    void wrongArgumentCountIsAnsweredWithoutExecuting() throws Exception {
        receive("prueba.saludar[solo uno]");

        service.pollAndProcess();

        assertThat(handlers.calls).isEmpty();
        assertThat(stateOf("prueba.saludar[solo uno]")).isEqualTo(ReceivedEmailState.DISCARDED);
        assertThat(repliesToAna()).singleElement()
                .satisfies(reply -> assertThat(GreenMailUtil.getBody(reply)).contains("requiere 2 argumentos y se recibieron 1"));
    }

    @Test
    void domainErrorIsAnsweredAndNotRetried() throws Exception {
        receive("prueba.fallar[]");

        service.pollAndProcess();
        service.pollAndProcess();

        assertThat(stateOf("prueba.fallar[]")).isEqualTo(ReceivedEmailState.PROCESSED);
        assertThat(repliesToAna()).singleElement()
                .satisfies(reply -> assertThat(GreenMailUtil.getBody(reply)).contains("La regla de prueba no se cumple."));
        assertThat(messagesOnServer()).isZero();
    }

    @Test
    void validatedEmailLeftByARestartIsExecutedOnce() {
        ReceivedEmail validated = ReceivedEmail.builder()
                .withMailboxId("uid-restart")
                .fromSender(ANA)
                .withSubject("prueba.saludar[a; b]")
                .withState(ReceivedEmailState.VALIDATED);
        repository.store(validated);

        service.pollAndProcess();
        service.pollAndProcess();

        assertThat(handlers.calls).containsExactly(List.of("a", "b"));
        assertThat(repository.findByMailboxId("uid-restart").orElseThrow().getState())
                .isEqualTo(ReceivedEmailState.PROCESSED);
    }

    @Test
    void attachmentsAreStoredInFileStorageAndTheHandlerReceivesTheirIds() throws Exception {
        byte[] carta = "%PDF-carta".getBytes(StandardCharsets.US_ASCII);
        byte[] cv = {'%', 'P', 'D', 'F', (byte) 0xE2, (byte) 0xE3};
        receive("prueba.adjuntos[]", new String[] {"carta.pdf", "cv.pdf"}, new byte[][] {carta, cv});

        service.pollAndProcess();

        List<UUID> ids = attachmentsOf("prueba.adjuntos[]");
        assertThat(handlers.attachmentCalls).containsExactly(ids);
        assertThat(ids).extracting(id -> storedFiles.get(id).getOriginalName()).containsExactly("carta.pdf", "cv.pdf");
        assertThat(storedFiles.get(ids.get(1)).getContentType()).isEqualTo("application/pdf");
        assertThat(storedFiles.get(ids.get(1)).getSize()).isEqualTo(cv.length);
        assertThat(storedFiles.readContent(ids.get(0))).isEqualTo(carta);
        assertThat(storedFiles.readContent(ids.get(1))).isEqualTo(cv);
        assertThat(jdbc.queryForList("SELECT column_name FROM information_schema.columns "
                + "WHERE table_name = 'received_email_attachment'", String.class)).doesNotContain("data");
    }

    @Test
    void pendingEmailAfterARestartUsesItsStoredFilesWithoutStoringThemAgain() {
        byte[] pdf = "%PDF-x".getBytes(StandardCharsets.US_ASCII);
        UUID first = transaction.execute(status -> storedFiles.store("a.pdf", "application/pdf", pdf)).getId();
        UUID second = transaction.execute(status -> storedFiles.store("b.pdf", "application/pdf", pdf)).getId();
        repository.store(ReceivedEmail.builder().withMailboxId("uid-restart").fromSender(ANA)
                .withSubject("prueba.adjuntos[]").withState(ReceivedEmailState.VALIDATED)
                .withAttachments(List.of(new Attachment.Stored(first),
                        new Attachment.Stored(second))));

        service.pollAndProcess();
        service.pollAndProcess();

        assertThat(handlers.attachmentCalls).containsExactly(List.of(first, second));
        assertThat(storedFileRows()).isEqualTo(2);
    }

    @Test
    void discardedEmailKeepsItsStoredFile() throws Exception {
        receive("usuarios.borrarTodo[]", "carta.pdf", "%PDF-carta".getBytes(StandardCharsets.US_ASCII));

        service.pollAndProcess();

        assertThat(stateOf("usuarios.borrarTodo[]")).isEqualTo(ReceivedEmailState.DISCARDED);
        assertThat(attachmentsOf("usuarios.borrarTodo[]")).singleElement()
                .satisfies(id -> assertThat(storedFiles.get(id).getOriginalName()).isEqualTo("carta.pdf"));
    }

    @Test
    void emptyAttachmentDiscardsTheEmailWithoutStoringFiles() throws Exception {
        receive("prueba.adjuntos[]", new String[] {"carta.pdf", "boleta.pdf"},
                new byte[][] {"%PDF-carta".getBytes(StandardCharsets.US_ASCII), new byte[0]});

        service.pollAndProcess();

        assertThat(handlers.attachmentCalls).isEmpty();
        assertThat(stateOf("prueba.adjuntos[]")).isEqualTo(ReceivedEmailState.DISCARDED);
        assertThat(storedFileRows()).isZero();
        assertThat(repliesToAna()).singleElement().satisfies(reply ->
                assertThat(String.valueOf(reply.getContent())).contains("el adjunto \"boleta.pdf\" está vacío"));
        assertThat(messagesOnServer()).isZero();
    }
}
