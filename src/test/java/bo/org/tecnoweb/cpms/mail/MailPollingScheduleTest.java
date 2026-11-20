package bo.org.tecnoweb.cpms.mail;

import static org.assertj.core.api.Assertions.assertThat;

import bo.org.tecnoweb.cpms.TestcontainersConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;

/**
 * With polling enabled, cycles run on their own at the configured interval.
 *
 * <p>The context is closed after the class so its scheduler does not keep polling the mailbox of
 * other tests that use the same GreenMail ports.
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(properties = {
        "cpms.mail.polling-enabled=true",
        "cpms.mail.poll-interval=200ms",
        "cpms.mail.pop3.port=3110",
        "cpms.mail.pop3.username=sistema",
        "cpms.mail.pop3.password=secret",
        "spring.mail.port=3025"})
@Import(TestcontainersConfiguration.class)
class MailPollingScheduleTest {

    @RegisterExtension
    static final GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP_POP3)
            .withPerMethodLifecycle(false);

    @Test
    void aCycleRunsWithoutBeingCalled() throws Exception {
        greenMail.setUser("sistema@cpms.test", "sistema", "secret");
        GreenMailUtil.sendTextEmail("sistema@cpms.test", "ana@uagrm.bo", "comando.inexistente[]", "",
                ServerSetupTest.SMTP);

        Instant deadline = Instant.now().plus(Duration.ofSeconds(15));
        while (greenMail.getReceivedMessagesForDomain("uagrm.bo").length == 0 && Instant.now().isBefore(deadline)) {
            Thread.sleep(100);
        }

        assertThat(greenMail.getReceivedMessagesForDomain("uagrm.bo"))
                .as("reply sent by a scheduled cycle")
                .hasSize(1);
    }
}
