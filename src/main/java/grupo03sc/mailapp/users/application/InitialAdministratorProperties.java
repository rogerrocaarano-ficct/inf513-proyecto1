package grupo03sc.mailapp.users.application;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.initial-admin")
public record InitialAdministratorProperties(String name, String email) {
}
