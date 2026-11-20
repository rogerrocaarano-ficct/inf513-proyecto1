package bo.org.tecnoweb.cpms.mail;

import static org.assertj.core.api.Assertions.assertThat;

import bo.org.tecnoweb.cpms.TestcontainersConfiguration;
import bo.org.tecnoweb.cpms.common.DomainException;
import bo.org.tecnoweb.cpms.mail.application.EmailCommandHandler;
import bo.org.tecnoweb.cpms.mail.application.InboxPollerService;
import bo.org.tecnoweb.cpms.mail.application.ReceivedEmailRepository;
import bo.org.tecnoweb.cpms.mail.domain.ReceivedEmail;
import bo.org.tecnoweb.cpms.mail.domain.ReceivedEmailState;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

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

    @BeforeEach
    void setUp() {
        jdbc.update("DELETE FROM received_email");
        handlers.calls.clear();
        system = greenMail.setUser(SYSTEM, "sistema", "secret");
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class Handlers {

        final List<List<String>> calls = new ArrayList<>();

        @Bean
        EmailCommandHandler greetHandler() {
            return handler("prueba.saludar", 2, email -> calls.add(email.parseArgs()));
        }

        @Bean
        EmailCommandHandler failingHandler() {
            return handler("prueba.fallar", 0, email -> {
                throw new RuleBroken();
            });
        }

        private static EmailCommandHandler handler(String action, int args, java.util.function.Consumer<ReceivedEmail> body) {
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
                    return 0;
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
}
