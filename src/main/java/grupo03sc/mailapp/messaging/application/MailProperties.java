package grupo03sc.mailapp.messaging.application;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("app.mail")
public record MailProperties(
        String address,
        @DefaultValue("30s") Duration pollInterval,
        @DefaultValue("true") boolean pollingEnabled,
        Pop3 pop3) {

    public record Pop3(String host, int port, String user, String password) {
    }
}
