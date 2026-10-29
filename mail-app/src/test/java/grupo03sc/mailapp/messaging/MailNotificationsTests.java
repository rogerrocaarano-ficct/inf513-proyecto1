package grupo03sc.mailapp.messaging;

import static grupo03sc.mailapp.messaging.MailTestSupport.ADMIN;
import static grupo03sc.mailapp.messaging.MailTestSupport.body;
import static grupo03sc.mailapp.messaging.MailTestSupport.receivedBy;
import static grupo03sc.mailapp.messaging.MailTestSupport.sendCommand;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import grupo03sc.mailapp.TestcontainersConfiguration;
import grupo03sc.mailapp.messaging.adapter.in.Pop3MailboxPoller;
import grupo03sc.mailapp.messaging.application.Notifier;
import grupo03sc.mailapp.audit.application.ChangeLog;
import grupo03sc.mailapp.audit.application.ChangeLogEntryRepository;
import grupo03sc.mailapp.audit.application.FieldChange;
import jakarta.mail.internet.MimeMessage;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
@Import({TestcontainersConfiguration.class, TestCommands.class})
class MailNotificationsTests {

    @RegisterExtension
    static GreenMailExtension greenMail = MailTestSupport.greenMail();

    @Autowired
    private Pop3MailboxPoller poller;

    @Autowired
    private Notifier notifier;

    @Autowired
    private ChangeLog changeLog;

    @Autowired
    private ChangeLogEntryRepository entries;

    @Autowired
    private TransactionTemplate transaction;

    @Test
    void sendsTheNoticeOnceTheCommandCommits() throws Exception {
        sendCommand(ADMIN, "Avisar(ana@uagrm.edu.bo)");

        poller.poll();

        List<MimeMessage> notices = receivedBy(greenMail, "ana@uagrm.edu.bo");
        assertThat(notices).singleElement().satisfies(notice -> {
            assertThat(notice.getSubject()).isEqualTo("Aviso de prueba");
            assertThat(body(notice)).isEqualTo("Cuerpo del aviso");
        });
    }

    @Test
    void doesNotSendTheNoticeWhenTheChangeIsRolledBack() {
        transaction.executeWithoutResult(status -> {
            notifier.notify("ana@uagrm.edu.bo", "Aviso", "x");
            status.setRollbackOnly();
        });

        assertThat(receivedBy(greenMail, "ana@uagrm.edu.bo")).isEmpty();
    }

    @Test
    void keepsTheCommittedChangeWhenTheNoticeCannotBeSent() {
        UUID target = UUID.randomUUID();
        greenMail.stop();

        assertThatCode(() -> transaction.executeWithoutResult(status -> {
            changeLog.record(UUID.randomUUID(), "Prueba", target, List.of(FieldChange.created("dato", "x")));
            notifier.notify("ana@uagrm.edu.bo", "Aviso", "x");
        })).doesNotThrowAnyException();

        assertThat(entries.findByTargetTypeAndTargetIdOrderByChangedAt("Prueba", target)).hasSize(1);
        greenMail.start();
    }
}
