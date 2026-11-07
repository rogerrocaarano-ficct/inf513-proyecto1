package grupo03sc.mailapp.messaging.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class IncomingMailFilterTests {

    private final IncomingMailFilter filter = new IncomingMailFilter("Sistema@MailApp.Test");

    @Test
    void processesACommandAddressedToTheSystem() {
        assertThat(filter.decide(mail("ana@uagrm.edu.bo", List.of("sistema@mailapp.test"), null)))
                .isEqualTo(MailDecision.PROCESS);
    }

    @Test
    void leavesEmailsSentByTheSystem() {
        assertThat(filter.decide(mail("sistema@mailapp.test", List.of("sistema@mailapp.test"), null)))
                .isEqualTo(MailDecision.LEAVE);
    }

    @Test
    void leavesEmailsAddressedToSomeoneElse() {
        assertThat(filter.decide(mail("ana@uagrm.edu.bo", List.of("otro@uagrm.edu.bo"), null)))
                .isEqualTo(MailDecision.LEAVE);
    }

    @Test
    void discardsAutomaticReplies() {
        assertThat(filter.decide(mail("ana@uagrm.edu.bo", List.of("sistema@mailapp.test"), "auto-replied")))
                .isEqualTo(MailDecision.DISCARD);
    }

    @Test
    void processesEmailsMarkedAsNotAutomatic() {
        assertThat(filter.decide(mail("ana@uagrm.edu.bo", List.of("SISTEMA@mailapp.test"), "no")))
                .isEqualTo(MailDecision.PROCESS);
    }

    private static IncomingMail mail(String from, List<String> recipients, String autoSubmitted) {
        return new IncomingMail("<id@test>", from, recipients, autoSubmitted, "Ping()", List.of());
    }
}
