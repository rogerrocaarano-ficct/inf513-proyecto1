package bo.org.tecnoweb.cpms.mail.infrastructure;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Settings of the mail channel, bound from {@code cpms.mail.*}.
 *
 * <p>SMTP uses the standard {@code spring.mail.*} settings.
 *
 * @param address        system address: sender of the replies; emails coming from it are ignored
 * @param batchSize      maximum messages read per polling cycle
 * @param pollInterval   delay between the end of one cycle and the start of the next
 * @param pollingEnabled whether the cycles run on a schedule
 * @param pop3           mailbox server
 */
@ConfigurationProperties("cpms.mail")
public record MailProperties(
        String address,
        @DefaultValue("10") int batchSize,
        @DefaultValue("30s") Duration pollInterval,
        @DefaultValue("true") boolean pollingEnabled,
        @DefaultValue Pop3 pop3) {

    /**
     * POP3 server of the mailbox.
     *
     * @param host     server name
     * @param port     server port; 110 without encryption (NFR-007)
     * @param username mailbox user
     * @param password mailbox password
     */
    public record Pop3(
            String host,
            @DefaultValue("110") int port,
            String username,
            String password) {
    }
}
