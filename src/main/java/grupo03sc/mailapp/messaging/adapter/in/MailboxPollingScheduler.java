package grupo03sc.mailapp.messaging.adapter.in;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Runs the poller every {@code app.mail.poll-interval}; disabled with {@code app.mail.polling-enabled=false}.
 */
@Configuration(proxyBeanMethods = false)
@EnableScheduling
@ConditionalOnBooleanProperty(name = "app.mail.polling-enabled", matchIfMissing = true)
class MailboxPollingScheduler {

    private final Pop3MailboxPoller poller;

    MailboxPollingScheduler(Pop3MailboxPoller poller) {
        this.poller = poller;
    }

    @Scheduled(fixedDelayString = "${app.mail.poll-interval:30s}", initialDelayString = "${app.mail.poll-interval:30s}")
    void poll() {
        poller.poll();
    }
}
