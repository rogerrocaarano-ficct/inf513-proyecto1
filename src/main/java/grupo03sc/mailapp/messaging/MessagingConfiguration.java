package grupo03sc.mailapp.messaging;

import grupo03sc.mailapp.messaging.application.MailProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(MailProperties.class)
class MessagingConfiguration {
}
